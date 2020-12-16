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

import io.cloudslang.engine.data.AbstractIdentifiable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "OO_QUEUE_DETAILS")
public class QueueDetails extends AbstractIdentifiable {

	@Column(name = "QUEUE_HOST", nullable = false)
	private String queueHost;
	@Column(name = "QUEUE_PORT", nullable = false)
	private int queuePort;
	@Column(name = "QUEUE_USERNAME", nullable = false)
	private String queueUsername;
	@Column(name = "QUEUE_PASSWORD", nullable = false)
	private String queuePassword;
	@Column(name = "VHOST", nullable = false)
	private String vhost;
	@Column(name = "VERSION", nullable = false)
	private int queueVersion;
	@Column(name = "ID", nullable = false, unique = true)
	private Long id;

	public String getQueueHost() {
		return queueHost;
	}

	public void setQueueHost(String queueHost) {
		this.queueHost = queueHost;
	}

	public int getQueuePort() {
		return queuePort;
	}

	public void setQueuePort(int queuePort) {
		this.queuePort = queuePort;
	}

	public String getQueueUsername() {
		return queueUsername;
	}

	public void setQueueUsername(String queueUser) {
		this.queueUsername = queueUser;
	}

	public String getQueuePassword() {
		return queuePassword;
	}

	public void setQueuePassword(String queuePassword) {
		this.queuePassword = queuePassword;
	}

	public String getVhost() {
		return vhost;
	}

	public void setVhost(String vhost) {
		this.vhost = vhost;
	}

	public int getQueueVersion() {
		return queueVersion;
	}

	public void setQueueVersion(int queueVersion) {
		this.queueVersion = queueVersion;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		QueueDetails that = (QueueDetails) o;
		return queuePort == that.queuePort && Objects.equals(queueHost, that.queueHost) && Objects
			.equals(queueUsername, that.queueUsername) && Objects.equals(queuePassword, that.queuePassword)
			&& Objects.equals(vhost, that.vhost);
	}

	@Override
	public int hashCode() {
		return Objects.hash(queueHost, queuePort, queueUsername, queuePassword, vhost);
	}

}
