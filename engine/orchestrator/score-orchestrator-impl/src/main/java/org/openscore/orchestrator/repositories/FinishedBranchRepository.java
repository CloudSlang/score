/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.orchestrator.repositories;

import org.openscore.orchestrator.entities.FinishedBranch;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 07/11/13
 * Time: 14:16
 */
public interface FinishedBranchRepository extends JpaRepository<FinishedBranch, Long> {
}
