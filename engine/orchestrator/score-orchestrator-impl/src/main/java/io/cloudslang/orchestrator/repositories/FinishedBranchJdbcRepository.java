package io.cloudslang.orchestrator.repositories;

import java.util.Collection;
import java.util.List;

public interface FinishedBranchJdbcRepository {
    List<Long> getSizeOfBlob(Collection<String> suspendedExecutionIds);
}
