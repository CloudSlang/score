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

import io.cloudslang.score.api.nodes.WorkerStatus;
import io.cloudslang.engine.data.AbstractIdentifiable;
import io.cloudslang.score.facade.TempConstants;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 08/11/2O12
 */
@Entity
@Table(name = "OO_WORKER_NODES")
@DynamicUpdate(value=true)
@SelectBeforeUpdate(value=true)
public class WorkerNode extends AbstractIdentifiable implements Worker {
	public static final String[] DEFAULT_WORKER_GROUPS = {TempConstants.DEFAULT_GROUP};

	@Column(name = "UUID", nullable = false, unique = true, length = 48)
	private String uuid;

	@Column(name = "STATUS", nullable = false, length = 20)
	private WorkerStatus status;

	@Column(name = "IS_ACTIVE", nullable = false)
	private boolean active = true;

	@Column(name = "HOST_NAME", length = 128, nullable = false)
	private String hostName;

	@Column(name = "INSTALL_PATH", nullable = false)
	private String installPath;

	@Size(max = 255)
	@Column(name = "DESCRIPTION", length = 255)
	private String description;

	@Column(name = "PASSWORD", length = 80, nullable = false)
	private String password;

	@Size(max = 64)
	@Column(name = "OS", length = 64)
	private String os;

	@Size(max = 64)
	@Column(name = "JVM", length = 64)
	private String jvm;

	@Size(max = 16)
	@Column(name = "DOT_NET_VERSION", length = 16)
	private String dotNetVersion;

	@Column(name = "ACK_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date ackTime;

    @Column(name = "IS_DELETED", nullable = false)
    private boolean deleted = false;

    @Column(name = "ACK_VERSION", nullable = false)
    private long ackVersion;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name = "OO_WORKER_GROUPS",
			joinColumns = @JoinColumn(name = "WORKER_ID")
	)
	@Column(name = "GROUP_NAME")
	private List<String> groups = new ArrayList<>();

    @Column(name = "BULK_NUMBER", length = 48)
    private String bulkNumber;

    @Column(name = "WRV", length = 48)
    private String workerRecoveryVersion;

	@Column(name = "VERSION", length = 48, nullable = false)
	private String version = "";

	@Column(name = "VERSION_ID", length = 48, nullable = false)
	private String versionId = "";

    @Override
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

    @Override
	public WorkerStatus getStatus() {
		return status;
	}

	public void setStatus(WorkerStatus status) {
		this.status = status;
	}

    @Override
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

    @Override
	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

    @Override
	public String getInstallPath() {
		return installPath;
	}

	public void setInstallPath(String installPath) {
		this.installPath = installPath;
	}

    @Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    @Override
	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

    @Override
	public String getJvm() {
		return jvm;
	}

	public void setJvm(String jvm) {
		this.jvm = jvm;
	}

    @Override
	public String getDotNetVersion() {
		return dotNetVersion;
	}

	public void setDotNetVersion(String dotNetVersion) {
		this.dotNetVersion = dotNetVersion;
	}

	public Date getAckTime() {
		return ackTime;
	}

	public void setAckTime(Date ackTime) {
		this.ackTime = ackTime;
	}

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
	public List<String> getGroups() {
		return Collections.unmodifiableList(groups);
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

    public long getAckVersion() {
        return ackVersion;
    }

    public void setAckVersion(long ackVersion) {
        this.ackVersion = ackVersion;
    }

    public String getBulkNumber() {
        return bulkNumber;
    }

    public void setBulkNumber(String bulkNumber) {
        this.bulkNumber = bulkNumber;
    }

    public String getWorkerRecoveryVersion() {
        return workerRecoveryVersion;
    }

    public void setWorkerRecoveryVersion(String workerRecoveryVersion) {
        this.workerRecoveryVersion = workerRecoveryVersion;
    }

	@Override
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WorkerNode that = (WorkerNode) o;

		if (active != that.active) return false;
		if (deleted != that.deleted) return false;
		if (ackVersion != that.ackVersion) return false;
		if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
		if (status != that.status) return false;
		if (hostName != null ? !hostName.equals(that.hostName) : that.hostName != null) return false;
		if (installPath != null ? !installPath.equals(that.installPath) : that.installPath != null) return false;
		if (description != null ? !description.equals(that.description) : that.description != null) return false;
		if (password != null ? !password.equals(that.password) : that.password != null) return false;
		if (os != null ? !os.equals(that.os) : that.os != null) return false;
		if (jvm != null ? !jvm.equals(that.jvm) : that.jvm != null) return false;
		if (dotNetVersion != null ? !dotNetVersion.equals(that.dotNetVersion) : that.dotNetVersion != null) return false;
		if (ackTime != null ? !ackTime.equals(that.ackTime) : that.ackTime != null) return false;
		if (groups != null ? !groups.equals(that.groups) : that.groups != null) return false;
		if (bulkNumber != null ? !bulkNumber.equals(that.bulkNumber) : that.bulkNumber != null) return false;
		if (workerRecoveryVersion != null ? !workerRecoveryVersion.equals(that.workerRecoveryVersion) : that.workerRecoveryVersion != null) return false;
		if (version != null ? !version.equals(that.version) : that.version != null) return false;
		return !(versionId != null ? !versionId.equals(that.versionId) : that.versionId != null);

	}

	@Override
	public int hashCode() {
		int result = uuid != null ? uuid.hashCode() : 0;
		result = 31 * result + (status != null ? status.hashCode() : 0);
		result = 31 * result + (active ? 1 : 0);
		result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
		result = 31 * result + (installPath != null ? installPath.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		result = 31 * result + (os != null ? os.hashCode() : 0);
		result = 31 * result + (jvm != null ? jvm.hashCode() : 0);
		result = 31 * result + (dotNetVersion != null ? dotNetVersion.hashCode() : 0);
		result = 31 * result + (ackTime != null ? ackTime.hashCode() : 0);
		result = 31 * result + (deleted ? 1 : 0);
		result = 31 * result + (int) (ackVersion ^ (ackVersion >>> 32));
		result = 31 * result + (groups != null ? groups.hashCode() : 0);
		result = 31 * result + (bulkNumber != null ? bulkNumber.hashCode() : 0);
		result = 31 * result + (workerRecoveryVersion != null ? workerRecoveryVersion.hashCode() : 0);
		result = 31 * result + (version != null ? version.hashCode() : 0);
		result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "WorkerNode{" +
				"uuid='" + uuid + '\'' +
				", status=" + status +
				", active=" + active +
				", hostName='" + hostName + '\'' +
				", installPath='" + installPath + '\'' +
				", description='" + description + '\'' +
				", os='" + os + '\'' +
				", jvm='" + jvm + '\'' +
				", dotNetVersion='" + dotNetVersion + '\'' +
				", ackTime=" + ackTime +
				", deleted=" + deleted +
				", ackVersion=" + ackVersion +
				", groups=" + groups +
				", bulkNumber='" + bulkNumber + '\'' +
				", workerRecoveryVersion='" + workerRecoveryVersion + '\'' +
				", version='" + version + '\'' +
				", versionId='" + versionId + '\'' +
				'}';
	}
}
