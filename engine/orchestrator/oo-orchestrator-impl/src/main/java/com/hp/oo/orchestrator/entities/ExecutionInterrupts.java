package com.hp.oo.orchestrator.entities;

import com.hp.score.engine.data.AbstractIdentifiable;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: hajyhia
 * Date: 2/24/13
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "OO_EXECUTION_INTERRUPTS")
public class ExecutionInterrupts extends AbstractIdentifiable {
    private static final long serialVersionUID = 6709480096127567290L;

    @Column(name = "EXECUTION_ID", nullable = false)
    private String executionId;


    @Column(name = "TYPE", nullable = false)
    private String type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_TIME", nullable = false)
    private Date createdTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_TIME", nullable = false)
    private Date updatedTime;

    @Lob
    @Column(name = "EXECUTION_INTERRUPT_REGISTRY", nullable = false)
    @Basic(fetch = FetchType.LAZY)
    private byte[] executionInterruptRegistry;


    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public byte[] getExecutionInterruptRegistry() {
        return executionInterruptRegistry;
    }

    public void setExecutionInterruptRegistry(byte[] executionInterruptRegistry) {
        this.executionInterruptRegistry = executionInterruptRegistry;
    }

    @PrePersist
    protected void onCreate() {
        updatedTime = new Date();
    }

    @Override
    public boolean equals(Object o) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int hashCode() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
