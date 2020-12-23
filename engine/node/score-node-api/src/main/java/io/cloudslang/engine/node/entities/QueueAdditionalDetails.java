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
package io.cloudslang.engine.node.entities;

import java.io.Serializable;

public class QueueAdditionalDetails implements Serializable {

	private static final long serialVersionUID = 275617756094575753L;

	private String exchangeName;
	private String exchangeType;
	private String robotResultsQueue;
	private String robotResultsKey;
	private String robotGroupQueuePrefix;
	private boolean useTls;
	private String serverTruststorePath;
	private char[] serverTruststorePass;
	private String tlsVersion;
	private String truststoreType;
	private String startupQueue;
	private String startupKey;
	private int startupRetries;
	private long startupRetryTimeout;
	private String heartbeatsQueue;
	private String heartbeatsKey;
	private int heartbeatsRetries;
	private long heartbeatsRetryTimeout;
	private String heartbeatsAckQueuePrefix;
	private String shutdownQueue;
	private String shutdownKey;
	private int shutdownRetries;
	private long shutdownRetryTimeout;
	private String stepStateQueue;
	private String stepStateKey;
	private long stepStateRetryInterval;
	private String runQueue;
	private String runKey;
	private String insightsFlowQueue;
	private String insightsFlowKey;
	private String insightsRunQueue;
	private String insightsRunKey;
	private String insightsEntitlementsQueue;
	private String insightsEntitlementsKey;

	public String getExchangeName() {
		return exchangeName;
	}

	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}

	public String getExchangeType() {
		return exchangeType;
	}

	public void setExchangeType(String exchangeType) {
		this.exchangeType = exchangeType;
	}

	public String getRobotResultsQueue() {
		return robotResultsQueue;
	}

	public void setRobotResultsQueue(String robotResultsQueue) {
		this.robotResultsQueue = robotResultsQueue;
	}

	public String getRobotResultsKey() {
		return robotResultsKey;
	}

	public void setRobotResultsKey(String robotResultsKey) {
		this.robotResultsKey = robotResultsKey;
	}

	public String getRobotGroupQueuePrefix() {
		return robotGroupQueuePrefix;
	}

	public void setRobotGroupQueuePrefix(String robotGroupQueuePrefix) {
		this.robotGroupQueuePrefix = robotGroupQueuePrefix;
	}

	public boolean isUseTls() {
		return useTls;
	}

	public void setUseTls(boolean useTls) {
		this.useTls = useTls;
	}

	public String getServerTruststorePath() {
		return serverTruststorePath;
	}

	public void setServerTruststorePath(String serverTruststorePath) {
		this.serverTruststorePath = serverTruststorePath;
	}

	public char[] getServerTruststorePass() {
		return serverTruststorePass;
	}

	public void setServerTruststorePass(char[] serverTruststorePass) {
		this.serverTruststorePass = serverTruststorePass;
	}

	public String getTlsVersion() {
		return tlsVersion;
	}

	public void setTlsVersion(String tlsVersion) {
		this.tlsVersion = tlsVersion;
	}

	public String getTruststoreType() {
		return truststoreType;
	}

	public void setTruststoreType(String truststoreType) {
		this.truststoreType = truststoreType;
	}

	public String getStartupQueue() {
		return startupQueue;
	}

	public void setStartupQueue(String startupQueue) {
		this.startupQueue = startupQueue;
	}

	public String getStartupKey() {
		return startupKey;
	}

	public void setStartupKey(String startupKey) {
		this.startupKey = startupKey;
	}

	public int getStartupRetries() {
		return startupRetries;
	}

	public void setStartupRetries(int startupRetries) {
		this.startupRetries = startupRetries;
	}

	public long getStartupRetryTimeout() {
		return startupRetryTimeout;
	}

	public void setStartupRetryTimeout(long startupRetryTimeout) {
		this.startupRetryTimeout = startupRetryTimeout;
	}

	public String getHeartbeatsQueue() {
		return heartbeatsQueue;
	}

	public void setHeartbeatsQueue(String heartbeatsQueue) {
		this.heartbeatsQueue = heartbeatsQueue;
	}

	public String getHeartbeatsKey() {
		return heartbeatsKey;
	}

	public void setHeartbeatsKey(String heartbeatsKey) {
		this.heartbeatsKey = heartbeatsKey;
	}

	public int getHeartbeatsRetries() {
		return heartbeatsRetries;
	}

	public void setHeartbeatsRetries(int heartbeatsRetries) {
		this.heartbeatsRetries = heartbeatsRetries;
	}

	public long getHeartbeatsRetryTimeout() {
		return heartbeatsRetryTimeout;
	}

	public void setHeartbeatsRetryTimeout(long heartbeatsRetryTimeout) {
		this.heartbeatsRetryTimeout = heartbeatsRetryTimeout;
	}

	public String getHeartbeatsAckQueuePrefix() {
		return heartbeatsAckQueuePrefix;
	}

	public void setHeartbeatsAckQueuePrefix(String heartbeatsAckQueuePrefix) {
		this.heartbeatsAckQueuePrefix = heartbeatsAckQueuePrefix;
	}

	public String getShutdownQueue() {
		return shutdownQueue;
	}

	public void setShutdownQueue(String shutdownQueue) {
		this.shutdownQueue = shutdownQueue;
	}

	public String getShutdownKey() {
		return shutdownKey;
	}

	public void setShutdownKey(String shutdownKey) {
		this.shutdownKey = shutdownKey;
	}

	public int getShutdownRetries() {
		return shutdownRetries;
	}

	public void setShutdownRetries(int shutdownRetries) {
		this.shutdownRetries = shutdownRetries;
	}

	public long getShutdownRetryTimeout() {
		return shutdownRetryTimeout;
	}

	public void setShutdownRetryTimeout(long shutdownRetryTimeout) {
		this.shutdownRetryTimeout = shutdownRetryTimeout;
	}

	public String getStepStateQueue() {
		return stepStateQueue;
	}

	public void setStepStateQueue(String stepStateQueue) {
		this.stepStateQueue = stepStateQueue;
	}

	public String getStepStateKey() {
		return stepStateKey;
	}

	public void setStepStateKey(String stepStateKey) {
		this.stepStateKey = stepStateKey;
	}

	public long getStepStateRetryInterval() {
		return stepStateRetryInterval;
	}

	public void setStepStateRetryInterval(long stepStateRetryInterval) {
		this.stepStateRetryInterval = stepStateRetryInterval;
	}

	public String getRunQueue() {
		return runQueue;
	}

	public void setRunQueue(String runQueue) {
		this.runQueue = runQueue;
	}

	public String getRunKey() {
		return runKey;
	}

	public void setRunKey(String runKey) {
		this.runKey = runKey;
	}

	public String getInsightsFlowQueue() {
		return insightsFlowQueue;
	}

	public void setInsightsFlowQueue(String insightsFlowQueue) {
		this.insightsFlowQueue = insightsFlowQueue;
	}

	public String getInsightsFlowKey() {
		return insightsFlowKey;
	}

	public void setInsightsFlowKey(String insightsFlowKey) {
		this.insightsFlowKey = insightsFlowKey;
	}

	public String getInsightsRunQueue() {
		return insightsRunQueue;
	}

	public void setInsightsRunQueue(String insightsRunQueue) {
		this.insightsRunQueue = insightsRunQueue;
	}

	public String getInsightsRunKey() {
		return insightsRunKey;
	}

	public void setInsightsRunKey(String insightsRunKey) {
		this.insightsRunKey = insightsRunKey;
	}

	public String getInsightsEntitlementsQueue() {
		return insightsEntitlementsQueue;
	}

	public void setInsightsEntitlementsQueue(String insightsEntitlementsQueue) {
		this.insightsEntitlementsQueue = insightsEntitlementsQueue;
	}

	public String getInsightsEntitlementsKey() {
		return insightsEntitlementsKey;
	}

	public void setInsightsEntitlementsKey(String insightsEntitlementsKey) {
		this.insightsEntitlementsKey = insightsEntitlementsKey;
	}
}
