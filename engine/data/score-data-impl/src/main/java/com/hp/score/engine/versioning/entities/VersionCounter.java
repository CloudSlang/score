package com.hp.score.engine.versioning.entities;

import com.hp.score.engine.data.AbstractIdentifiable;
import org.apache.commons.lang.builder.EqualsBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;


/**
 * User: wahnonm
 * Date: 30/10/13
 * Time: 11:49
 */
@Entity
@Table(name = "OO_VERSION_COUNTERS")
public class VersionCounter extends AbstractIdentifiable {

    @Column(name = "COUNTER_NAME", nullable = false, unique = true, length = 64)
    private String counterName;

    @Column(name = "COUNTER_VERSION", nullable = false)
    private long versionCount;

    private VersionCounter(){};

    public VersionCounter(String counterName) {
        this.counterName = counterName;
    }

    public String getCounterName() {
        return counterName;
    }

    public long getVersionCount() {
        return versionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionCounter that = (VersionCounter) o;
        return new EqualsBuilder()
                .append(this.counterName, that.counterName)
                .append(this.versionCount, that.versionCount)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(counterName,versionCount);
    }
}
