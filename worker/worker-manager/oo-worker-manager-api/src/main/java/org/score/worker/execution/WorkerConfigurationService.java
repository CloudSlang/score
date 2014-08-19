package org.score.worker.execution;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @since 03/06/2012
 * @version $Id$
 * Used by Score for pause/cancel runs & stay in the worker
 */
public interface WorkerConfigurationService {

	public boolean isExecutionCancelled(Long executionId);

	public boolean isExecutionPaused(Long executionId, String branchId);

	public boolean isMemberOf(String group);

	public void setEnabled(boolean enabled);

}
