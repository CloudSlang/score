/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples;

/**
 * Date: 9/9/2014
 *
 * @author Bonczidai Levente
 */
public class FlowMetadata {
    private String identifier;
    private String name;
    private String description;
    private String className;
    private String triggeringPropertiesMethodName;
    private String inputBindingsMethodName;

    public FlowMetadata() {
    }

    public FlowMetadata(String identifier, String name, String description, String className, String triggeringPropertiesMethodName, String inputBindingsMethodName) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.className = className;
        this.triggeringPropertiesMethodName = triggeringPropertiesMethodName;
        this.inputBindingsMethodName = inputBindingsMethodName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTriggeringPropertiesMethodName() {
        return triggeringPropertiesMethodName;
    }

    public void setTriggeringPropertiesMethodName(String triggeringPropertiesMethodName) {
        this.triggeringPropertiesMethodName = triggeringPropertiesMethodName;
    }

    public String getInputBindingsMethodName() {
        return inputBindingsMethodName;
    }

    public void setInputBindingsMethodName(String inputBindingsMethodName) {
        this.inputBindingsMethodName = inputBindingsMethodName;
    }

    @Override
    public String toString() {
        return "FlowMetadata{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", className='" + className + '\'' +
                ", triggeringPropertiesMethodName='" + triggeringPropertiesMethodName + '\'' +
                ", inputBindingsMethodName='" + inputBindingsMethodName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlowMetadata)) return false;

        FlowMetadata that = (FlowMetadata) o;

        return !(className != null ? !className.equals(that.className) : that.className != null)
                && !(description != null ? !description.equals(that.description) : that.description != null)
                && !(identifier != null ? !identifier.equals(that.identifier) : that.identifier != null)
                && !(inputBindingsMethodName != null ? !inputBindingsMethodName.equals(that.inputBindingsMethodName) : that.inputBindingsMethodName != null)
                && !(name != null ? !name.equals(that.name) : that.name != null)
                && !(triggeringPropertiesMethodName != null ? !triggeringPropertiesMethodName.equals(that.triggeringPropertiesMethodName) : that.triggeringPropertiesMethodName != null);
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (className != null ? className.hashCode() : 0);
        result = 31 * result + (triggeringPropertiesMethodName != null ? triggeringPropertiesMethodName.hashCode() : 0);
        result = 31 * result + (inputBindingsMethodName != null ? inputBindingsMethodName.hashCode() : 0);
        return result;
    }
}
