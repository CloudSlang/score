package com.hp.score.worker.management.services;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import com.hp.score.worker.management.WorkerConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.hp.oo.engine.node.services.WorkerNodeService;

import static ch.lambdaj.Lambda.max;
import static ch.lambdaj.Lambda.on;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 20/11/12
 * Time: 11:02
 */
public class WorkerManager implements ApplicationListener, EndExecutionCallback, WorkerRecoveryListener {
	private final Logger logger = Logger.getLogger(this.getClass());
	private static final int KEEP_ALIVE_FAIL_LIMIT = 4;

	@Resource
	private String workerUuid;
	@Autowired
	protected WorkerNodeService workerNodeService;
	@Autowired
	protected WorkerConfigurationService workerConfigurationService;
	@Autowired
	protected WorkerRecoveryManager recoveryManager;
	private LinkedBlockingQueue<Runnable> inBuffer = new LinkedBlockingQueue<>();
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
	private ExecutorService executorService;
	private Map<Long, Future> mapOfRunningTasks;
	private volatile boolean endOfInit = false;
    private volatile boolean initStarted = false;
	private boolean up = false;

	@PostConstruct
	private void init() {
		logger.info("Initialize worker with UUID: " + workerUuid);
		System.setProperty("worker.uuid", workerUuid); //do not remove!!!

		executorService = new ThreadPoolExecutor(numberOfThreads,
				numberOfThreads,
				Long.MAX_VALUE, TimeUnit.NANOSECONDS,
				inBuffer,
				new WorkerThreadFactory("WorkerExecutionThread"));

		mapOfRunningTasks = new ConcurrentHashMap<>(numberOfThreads);
	}

	public void addExecution(Long executionId, Runnable runnable) {
        Future future = executorService.submit(runnable);
        mapOfRunningTasks.put(executionId, future);
	}

	@Override
	public void endExecution(Long executionId) {
		mapOfRunningTasks.remove(executionId);
	}

	public int getInBufferSize() {
		return inBuffer.size();
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
                    if(keepAliveFailCount > KEEP_ALIVE_FAIL_LIMIT){
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
				                String newWrv = workerNodeService.up(workerUuid);
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
		                //mark that worker is up and its recovery is ended - only now we can start asking for messages from queue
		                up = true;

		                workerConfigurationService.setEnabled(true);
		                workerNodeService.updateEnvironmentParams(workerUuid,
				                System.getProperty("os.name"),
				                System.getProperty("java.version"),
				                resolveDotNetVersion());
	                }
		}).start();
	}

    private void doShutdown() {
    		endOfInit = false;
            initStarted = false;
    		workerConfigurationService.setEnabled(false);
    		workerNodeService.down(workerUuid);
    		up = false;
    		logger.info("The worker is down");
    	}

	protected String resolveDotNetVersion() {
		File dotNetHome = new File(System.getenv("WINDIR") + "/Microsoft.NET/Framework");
		if (dotNetHome.isDirectory()) {
			File[] versionFolders = dotNetHome.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return file.isDirectory() && file.getName().startsWith("v");
				}
			});

			if (!ArrayUtils.isEmpty(versionFolders)) {
				String maxVersion = max(versionFolders, on(File.class).getName()).substring(1);

				return maxVersion.substring(0, 1) + ".x";
			}
		}
		return "N/A";
	}

	public boolean isUp() {
		return up;
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
        executorService.shutdownNow();

        try {
            logger.warn("Worker is in doRecovery(). Cleaning state and cancelling running tasks. It may take up to 3 minutes...");
            boolean finished = executorService.awaitTermination(3, TimeUnit.MINUTES);

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
        executorService = new ThreadPoolExecutor(numberOfThreads,
                numberOfThreads,
                Long.MAX_VALUE, TimeUnit.NANOSECONDS,
                inBuffer,
                new WorkerThreadFactory("WorkerExecutionThread"));
    }
}
