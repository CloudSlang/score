package com.hp.score.engine.node.entities;

import com.hp.score.api.nodes.WorkerStatus;
import com.hp.score.engine.data.AbstractIdentifiable;
import com.hp.score.facade.TempConstants;
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
 * User: Amit Levin
 * Date: 08/11/2O12
 */
@Entity
@Table(name = "OO_WORKER_NODES")
@DynamicUpdate(value=true)
@SelectBeforeUpdate(value=true)
//TODO: remove interface
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof WorkerNode)) return false;

		WorkerNode that = (WorkerNode) o;

		return new EqualsBuilder()
				.append(this.uuid, that.uuid)
				.append(this.description, that.description)
				.append(this.hostName, that.hostName)
				.append(this.installPath, that.installPath)
				.append(this.active, that.active)
				.append(this.ackTime, that.ackTime)
                .append(this.ackVersion, that.ackVersion)
				.append(this.os, that.os)
				.append(this.jvm, that.jvm)
				.append(this.dotNetVersion, that.dotNetVersion)
				.append(this.groups, that.groups)
                .append(this.bulkNumber, that.bulkNumber)
                .append(this.workerRecoveryVersion, that.workerRecoveryVersion)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid,description,hostName,installPath,active,ackVersion,ackTime,os,jvm,dotNetVersion,groups,bulkNumber,workerRecoveryVersion);
	}

	@Override
	public String toString() {

		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("UUID", uuid)
				.append("active", active)
				.append("ackTime", ackTime)
                .append("ackVersion", ackVersion)
				.append("host", hostName)
				.append("path", installPath)
				.append("OS", os)
				.append("JVM", jvm)
				.append(".NET", dotNetVersion)
				.append("groups", groups)
                .append("bulkNumber", bulkNumber)
                .append("workerRecoveryVersion", workerRecoveryVersion)
				.toString()
		;
	}
}
