package com.hp.oo.orchestrator.entities;

import com.hp.score.engine.data.AbstractIdentifiable;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * User: hajyhia
 * Date: 1/17/13
 * Time: 11:38 AM
 */
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "OO_RUNNING_EXEC_CONFIGS")
public class RunningExecutionConfigurationImpl extends AbstractIdentifiable implements RunningExecutionConfiguration {

    @Lob
    @Column(name = "EXECUTION_CONFIGURATION", nullable = false)
    @Basic(fetch = FetchType.LAZY)
    private byte[] executionConfiguration;

    @Column(name = "CHECKSUM", nullable = false)
    private String checksum;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_TIME", nullable = false)
    private Date createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = Calendar.getInstance().getTime();
    }

    @Override
    public byte[] getExecutionConfiguration() {
        return executionConfiguration;
    }

    public void setExecutionConfiguration(byte[] executionConfiguration) {
        this.executionConfiguration = executionConfiguration;
    }

    @Override
    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public Date getCreatedTime() {
        return createdTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RunningExecutionConfigurationImpl)) return false;

        RunningExecutionConfigurationImpl that = (RunningExecutionConfigurationImpl) o;

        if (checksum != null ? !checksum.equals(that.checksum) : that.checksum != null) return false;
        if (createdTime != null ? !createdTime.equals(that.createdTime) : that.createdTime != null) return false;
        if (!Arrays.equals(executionConfiguration, that.executionConfiguration)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = executionConfiguration != null ? Arrays.hashCode(executionConfiguration) : 0;
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + (createdTime != null ? createdTime.hashCode() : 0);
        return result;
    }
}
