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

package io.cloudslang.orchestrator.entities;

import io.cloudslang.score.facade.entities.Execution;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * User: zruya
 * Date: 08/09/13
 * Time: 17:21
 */
public class SplitMessage implements Message {
	private static final long serialVersionUID = -720851148155732731L;

    private int basicSplitWeight = Integer.getInteger("basic.split.weight",3);

	private final String splitId;
    private final Execution parent;
    private final List<Execution> children;
    private final int totalNumberOfBranches;
    private final boolean executable;

    public SplitMessage(String splitId,
                        Execution parent,
                        List<Execution> children,
                        int totalNumberOfBranches,
                        boolean executable) {
        Validate.notNull(splitId, "splitId cannot be null");
        Validate.notNull(parent, "parent cannot be null");
        Validate.notNull(children, "children cannot be null");
        Validate.notEmpty(children, "cannot create a split message without any children");

        this.splitId = splitId;
        this.parent = parent;
        this.children = new ArrayList<>(children);
        this.totalNumberOfBranches = totalNumberOfBranches;
        this.executable = executable;
    }

    public Execution getParent() {
        return parent;
    }

    public List<Execution> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public String getSplitId() {
        return splitId;
    }

    public boolean isExecutable() {
        return executable;
    }

	@Override
	public int getWeight() {
		return children.size() * basicSplitWeight;
	}

	@Override
	public String getId() {
		return parent.getExecutionId().toString() + parent.getSystemContext().getBranchId();
	}

	@Override
	public List<Message> shrink(List<Message> messages) {
		return messages; // do nothing
	}

    public int getTotalNumberOfBranches() {
        return totalNumberOfBranches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplitMessage)) return false;

        SplitMessage that = (SplitMessage) o;

        return new EqualsBuilder()
                .append(this.splitId, that.splitId)
                .append(this.parent, that.parent)
                .append(this.children, that.children)
                .append(this.totalNumberOfBranches, that.totalNumberOfBranches)
                .append(this.executable, that.executable)
                .append(this.totalNumberOfBranches, that.totalNumberOfBranches)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
		        this.splitId,
		        this.parent,
		        this.children,
                this.totalNumberOfBranches,
                this.totalNumberOfBranches,
                this.executable);
    }
}
