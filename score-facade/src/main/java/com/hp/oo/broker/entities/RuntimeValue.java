package com.hp.oo.broker.entities;

import com.hp.score.engine.data.AbstractIdentifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * User: eisentha
 * Date: 12/12/12
 */
@Entity
@Table(name = "OO_RUNTIME_VALUE_STORE")
public class RuntimeValue extends AbstractIdentifiable {

    // "KEY" is a keyword in some databases - so we use "KEY_" as the column name
    @Column(name = "KEY_", unique = true, nullable = false)
    @Size(min = 1, max = 255)
    @NotNull
    private String key;

    @Column(name = "VALUE")
    @Size(max = 4000)
    private String value;

    @Column(name = "OWNER_ID")
    @Size(max = 255)
    private String ownerId;

    public RuntimeValue() {
    }

    public RuntimeValue(String key, String value, String ownerId) {
        this.key = key;
        this.value = value;
        this.ownerId = ownerId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "RuntimeValue{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuntimeValue)) return false;

        RuntimeValue that = (RuntimeValue) o;

        return !(key != null ? !key.equals(that.key) : that.key != null);
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}