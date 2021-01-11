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

public class QueueDetails implements Serializable {

	private static final long serialVersionUID = 2376774713855414142L;

	private String host;
	private int port;
	private String username;
	private char[] password;
	private String virtualHost;
	private int version;
	private QueueAdditionalDetails queueAdditionalDetails;

	public QueueDetails() {
	}

	public QueueDetails(String host, int port, String username, char[] password, String virtualHost) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.virtualHost = virtualHost;
	}

	public QueueDetails(String host, int port, String username, char[] password, String virtualHost, int version) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.virtualHost = virtualHost;
		this.version = version;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public char[] getPassword() {
		return password;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}

	public String getVirtualHost() {
		return virtualHost;
	}

	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public QueueAdditionalDetails getQueueAdditionalDetails() {
		return queueAdditionalDetails;
	}

	public void setQueueAdditionalDetails(QueueAdditionalDetails queueAdditionalDetails) {
		this.queueAdditionalDetails = queueAdditionalDetails;
	}
}
