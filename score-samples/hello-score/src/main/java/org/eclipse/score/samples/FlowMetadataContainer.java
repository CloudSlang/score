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

import java.util.List;

/**
 * Date: 10/7/2014
 *
 * @author Bonczidai Levente
 */
public class FlowMetadataContainer {
    private List<FlowMetadata> flows;

    public List<FlowMetadata> getFlows() {
        return flows;
    }

    public void setFlows(List<FlowMetadata> flows) {
        this.flows = flows;
    }
}
