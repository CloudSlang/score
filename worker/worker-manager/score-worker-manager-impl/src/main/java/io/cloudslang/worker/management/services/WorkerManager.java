/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.worker.management.services;

import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.orchestrator.services.EngineVersionService;
import io.cloudslang.worker.management.WorkerConfigurationService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ch.lambdaj.Lambda.max;
import static ch.lambdaj.Lambda.on;
import static java.lang.Integer.getInteger;
import static java.lang.System.getenv;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/11/12
 * Time: 11:02
 */
public class WorkerManager implements ApplicationListener, EndExecutionCallback, WorkerRecoveryListener {

	private static final int KEEP_ALIVE_FAIL_LIMIT = 5;
	private static final String DOT_NET_PATH = getenv("WINDIR") + "/Microsoft.NET/Framework";
	private static final int DEFAULT_BURST_THREADS = 5;
	private static final int DEFAULT_QUEUE_SHRINK_INTERVAL_MILLIS = 200;
	private static final Logger logger = Logger.getLogger(WorkerManager.class);
	private static final int DEFAULT_THREAD_TIMEOUT_MINS = 1;

	@Resource
	private String workerUuid;

	@Autowired
	protected WorkerNodeService workerNodeService;

	@Autowired
	private EngineVersionService engineVersionService;

	@Autowired
	protected WorkerConfigurationService workerConfigurationService;

	@Autowired
	protected WorkerRecoveryManager recoveryManager;

	@Autowired
	protected WorkerVersionService workerVersionService;

	private LinkedBlockingQueue<Runnable> inBuffer;

	@Autowired
	@Qualifier("numberOfExecutionThreads")
	private Integer numberOfThreads;

	@Autowired(required = false)
	@Qualifier("initStartUpSleep")
	private Long initStartUpSleep = 15*1000L; // by default 15 seconds

	@Autowired(required = false)
	@Qualifier("maxStartUpSleep")
	private Long maxStartUpSleep = 10*60*1000L; // by default 10 minutes

    private int keepAliveFailCount = 0;

	private ThreadPoolExecutor executorService;

	private ConcurrentMap<Long, Queue<Future>> mapOfRunningTasks;

	private volatile boolean endOfInit = false;

    private volatile boolean initStarted = false;

	private boolean up = false;

    private volatile int threadPoolVersion = 0;

    private int burstThreadCount;
    private int threadTimeoutMinutes;
    private int waitForQueueToShrinkMillis;

	@PostConstruct
	private void init() {
		logger.info("Initialize worker with UUID: " + workerUuid);
		System.setProperty("worker.uuid", workerUuid); //do not remove!!!

		// Ability to grow the the thread pool with additional burstThreadCount threads in case of spikes.
		int burstThreadCountLocal = getInteger("cloudslang.worker.burstExecutionThreads", DEFAULT_BURST_THREADS);
		burstThreadCount = (burstThreadCountLocal > 0) ? burstThreadCountLocal : DEFAULT_BURST_THREADS;

		// Max idle time after a thread pool releases a thread. (This thread will be recreated when needed).
		// The number of threads will never decrease below numberOfThreads since we don't allow core thread to time out.
		int localThreadTimeout = getInteger("cloudslang.worker.threadTimeoutMins", DEFAULT_THREAD_TIMEOUT_MINS);
		threadTimeoutMinutes = (localThreadTimeout > 0) ? localThreadTimeout : DEFAULT_THREAD_TIMEOUT_MINS;

		// Time to wait at polling for queue to shrink under 2 * (burstThreadCount + numberOfThreads) + 1. Default is 200 millis.
		int localWaitForQueueToShrinkMillis = getInteger("cloudslang.worker.waitForQueueToShrinkMillis",
				DEFAULT_QUEUE_SHRINK_INTERVAL_MILLIS);
		waitForQueueToShrinkMillis = (localWaitForQueueToShrinkMillis > 0) ? localWaitForQueueToShrinkMillis : DEFAULT_QUEUE_SHRINK_INTERVAL_MILLIS;

		inBuffer = new LinkedBlockingQueue<>(); // infinite queue for thread pool

		// To make thread-pool version start from 1
		threadPoolVersion++;
		createExecutorService();

		// Max number of threads is burstThreadCount + numberOfThreads
		mapOfRunningTasks = new ConcurrentHashMap<>(burstThreadCount + numberOfThreads);
	}

	private void createExecutorService() {
		ThreadPoolExecutor localExecutorService = new ThreadPoolExecutor(numberOfThreads,
				numberOfThreads + burstThreadCount,
				threadTimeoutMinutes, MINUTES,
				inBuffer,
				new WorkerThreadFactory((threadPoolVersion) + "_WorkerExecutionThread"));

		localExecutorService.allowCoreThreadTimeOut(false);
		executorService = localExecutorService;
	}

	public void addExecution(long executionId, Runnable runnable) {
		// It is possible that step 2 is already running, but step 1 that still did not clean itself from the map
		// This is because of the fact that before finishing the run in SimpleExecutionRunnable we have an in buffer shortcut
		// to resubmit unfinished SimpleExecutionRunnable to the thread pool.

		doAddExecution(executionId, runnable);
	}

	private void doAddExecution(long executionId, Runnable runnable) {
		Future future = executorService.submit(runnable);
		mapOfRunningTasks.merge(executionId, newQueue(future), this::addLists);
	}

	public void addExecutionForPolling(Runnable runnable, long executionId, Runnable lockRunnable, Runnable unlockRunnable) throws InterruptedException {
		int maxQueueSize = 2 * (numberOfThreads + burstThreadCount) + 1;

		boolean wasUnlocked = false;
		final BlockingQueue<Runnable> executorServiceQueue = executorService.getQueue();
		if (executorServiceQueue.size() > maxQueueSize) {
			unlockRunnable.run();
			wasUnlocked = true;
			while (executorServiceQueue.size() > maxQueueSize) {
				Thread.sleep(waitForQueueToShrinkMillis);
			}
		}

		if (wasUnlocked) { // lock again
			lockRunnable.run();
		}

		// It is possible that step 2 is already running, but step 1 that still did not clean itself from the map
		// This is because of the fact that before finishing the run in SimpleExecutionRunnable we have an in buffer shortcut
		// to resubmit unfinished SimpleExecutionRunnable to the thread pool.
		doAddExecution(executionId, runnable);
	}

	private Queue<Future> newQueue(Future future) {
        Queue<Future> queue = new LinkedList<>();
        queue.offer(future);
        return queue;
    }

    private Queue<Future> addLists(Queue<Future> oldValue, Queue<Future> newValue) {
        oldValue.offer(newValue.poll()); // there is only one value in newValue
        return oldValue;
    }

	@Override
	public void endExecution(Long executionId) {
        mapOfRunningTasks.merge(executionId, new LinkedList<>(),
                (queue, newValue) -> {
                    queue.poll();
                    return !queue.isEmpty() ? queue : null;
                }
        );
	}

	public int getInBufferSize() {
		return inBuffer.size();
	}

    @SuppressWarnings("unused")
    //scheduled in scoreWorkerSchedulerContext.xml
    public void interruptCanceledExecutions(){
        for(Long executionId  : mapOfRunningTasks.keySet()){
            if(workerConfigurationService.isExecutionCancelled(executionId)){
                Collection<Future> futures = mapOfRunningTasks.get(executionId);
				for(Future future : futures) {
					future.cancel(true);
				}
			}
        }
    }

	@SuppressWarnings("unused")
    //scheduled in xml
    public void workerKeepAlive() {
        if (!recoveryManager.isInRecovery()) {
            if (endOfInit) {
                try {
                    String newWrv = workerNodeService.keepAlive(workerUuid);
                    String currentWrv = recoveryManager.getWRV();
                    //do not update it!!! if it is different than we have - restart worker (clean state)
                    if(!currentWrv.equals(newWrv)){
                        logger.warn("Got new WRV from Orchestrator during keepAlive(). Going to reload...");
                        recoveryManager.doRecovery();
                    }
                    keepAliveFailCount = 0;
                } catch (Exception e) {
                    keepAliveFailCount++;
                    logger.error("Could not send keep alive to Central, keepAliveFailCount = " + keepAliveFailCount, e);
                    if(keepAliveFailCount >= KEEP_ALIVE_FAIL_LIMIT){
                        logger.error("Failed sending keepAlive for " + KEEP_ALIVE_FAIL_LIMIT + " times. Invoking worker internal recovery...");
                        recoveryManager.doRecovery();
                    }
                }
            }
        }
        else {
            if (logger.isDebugEnabled()) logger.debug("worker waits for recovery");
        }
	}

	@SuppressWarnings("unused") // called by scheduler
	public void logStatistics() {
		if (logger.isDebugEnabled()) {
			logger.debug("InBuffer size: " + getInBufferSize());
			logger.debug("Running task size: " + mapOfRunningTasks.size());
		}
	}

	public String getWorkerUuid() {
		return workerUuid;
	}

    public int getRunningTasksCount() {
        return mapOfRunningTasks.size();
    }

    public int getExecutionThreadsCount() {
        return numberOfThreads;
    }

    @Override
	public void onApplicationEvent(final ApplicationEvent applicationEvent) {
		if (applicationEvent instanceof ContextRefreshedEvent && !initStarted) {
			doStartup();
		} else if (applicationEvent instanceof ContextClosedEvent) {
			doShutdown();
		}
	}

	private void doStartup() {
		new Thread(new Runnable() {
			@Override public void run() {
				initStarted = true;
				long sleep = initStartUpSleep;
				boolean shouldRetry = true;
				while (shouldRetry) {
					try {
						String newWrv = workerNodeService.up(workerUuid, workerVersionService.getWorkerVersion(), workerVersionService.getWorkerVersionId());
						recoveryManager.setWRV(newWrv); //we do set of WRV here and in doRecovery() only!!! not in keepalive!!!
						shouldRetry = false;
						logger.info("Worker is up");
					} catch (Exception ex) {
						logger.error("Worker failed on start up, will retry in a " + sleep / 1000 + " seconds", ex);
						try {
							Thread.sleep(sleep);
						} catch (InterruptedException iex) {/*do nothing*/}
						sleep = Math.min(maxStartUpSleep, sleep * 2); // double the sleep time until max 10 minute
					}
				}

				endOfInit = true;

				//Check that this Worker is in the same version as engine - if not - stay idle
				String engineVersionId = engineVersionService.getEngineVersionId();
				if(workerVersionService.getWorkerVersionId().equals(engineVersionId)){

					//mark that worker is up and its recovery is ended - only now we can start asking for messages from queue
					up = true;
					workerConfigurationService.setEnabled(true);
					workerNodeService.updateEnvironmentParams(workerUuid,
							System.getProperty("os.name"),
							System.getProperty("java.version"),
							resolveDotNetVersion());
				}
				else{
					logger.warn("Worker's version is not equal to engine version. Won't be able to start processing flows!");
				}
			}
		}).start();
	}

	private void doShutdown() {
		endOfInit = false;
		initStarted = false;
		workerConfigurationService.setEnabled(false);
		up = false;
		logger.info("The worker is down");
	}

	protected static String resolveDotNetVersion() {
		File dotNetHome = new File(DOT_NET_PATH);
		if(dotNetHome.isDirectory()) {
			File[] versionFolders = dotNetHome.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					return file.isDirectory() && file.getName().startsWith("v");
				}
			});
			if(!ArrayUtils.isEmpty(versionFolders)) {
				String maxVersion = max(versionFolders, on(File.class).getName()).substring(1);
				return maxVersion.substring(0, 1) + ".x";
			}
		}
		return "N/A";
	}

	public boolean isUp() {
		return up;
    }

    public synchronized boolean isFromCurrentThreadPool(String threadName){
        if(threadName.startsWith(String.valueOf(threadPoolVersion))){
            if(logger.isDebugEnabled()){
                logger.debug("Current thread is from current thread pool");
            }
            return true;
        }
        else {
            logger.warn("Current thread is NOT from current thread pool!!!");
            return false;
        }
    }

    //Must clean the buffer that holds Runnables that wait for execution and also drop all the executions that currently run
    public void doRecovery() {

//        Attempts to stop all actively executing tasks, halts the
//        processing of waiting tasks, and returns a list of the tasks
//        that were awaiting execution.
//
//        This method does not wait for actively executing tasks to
//        terminate.
//
//        There are no guarantees beyond best-effort attempts to stop
//        processing actively executing tasks.  For example, typical
//        implementations will cancel via {@link Thread#interrupt}, so any
//        task that fails to respond to interrupts may never terminate.
        try {
            synchronized (this){
                executorService.shutdownNow(); //shutting down current running threads
                threadPoolVersion++;           //updating the thread pool version to a new one - so current running threads will exit
                logger.warn("Worker is in doRecovery(). Cleaning state and cancelling running tasks. It may take up to 30 seconds...");
            }

            boolean finished = executorService.awaitTermination(30, TimeUnit.SECONDS);

            if(finished){
                logger.warn("Worker succeeded to cancel running tasks during doRecovery().");
            }
            else {
                logger.warn("Not all running tasks responded to cancel.");
            }
        }
        catch (InterruptedException ex){/*ignore*/}

        mapOfRunningTasks.clear();

        //Make new executor
		createExecutorService();
	}
}
