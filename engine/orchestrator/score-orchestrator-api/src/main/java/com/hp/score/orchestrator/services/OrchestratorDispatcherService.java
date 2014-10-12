package com.hp.score.orchestrator.services;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 12/1/13
 *
 * @author Dima Rassin
 */
//TODO: Add Javadoc Meir
public interface OrchestratorDispatcherService {
	void dispatch(List<? extends Serializable> messages, String bulkNumber, String wrv, String workerUuid);
}
