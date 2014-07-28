package com.hp.oo.orchestrator.services;

import java.io.Serializable;
import java.util.List;

/**
 * Date: 12/1/13
 *
 * @author Dima Rassin
 */
public interface OrchestratorDispatcherService {
	void dispatch(List<? extends Serializable> messages);
	void dispatch(List<? extends Serializable> messages, String bulkNumber, String workerUuid);
}
