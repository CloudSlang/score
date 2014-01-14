package com.hp.oo.orchestrator.repositories;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.orchestrator.entities.QExecutionSummaryEntity;
import com.hp.score.engine.data.SqlUtils;
import com.mysema.query.types.expr.BooleanExpression;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.hp.oo.enginefacade.execution.ExecutionSummary.EMPTY_BRANCH;

/**
 * User: sadane
 * Date: 22/12/13
 * Time: 12:01
 */
@Component
public class ExecutionSummaryExpressions {

    @Autowired
    private SqlUtils sqlUtils;

    private static char ESCAPE_CHAR = SqlUtils.ESCAPE_CHAR.charAt(0);

    private QExecutionSummaryEntity entity = QExecutionSummaryEntity.executionSummaryEntity;

    public BooleanExpression branchIsEmpty() {
        return entity.branchId.eq(EMPTY_BRANCH);
    }

    public BooleanExpression statusIn(final List<ExecutionEnums.ExecutionStatus> statuses) {
        if (CollectionUtils.isEmpty(statuses)) return null;
        return entity.status.in(statuses);
    }

    public BooleanExpression pauseReasonIn(final List<PauseReason> pauseReasons) {
        if (CollectionUtils.isEmpty(pauseReasons)) return null;
        return entity.pauseReason.in(pauseReasons);
    }

    public BooleanExpression resultStatusTypeIn(final List<String> resultStatusTypes) {
        if (CollectionUtils.isEmpty(resultStatusTypes)) return null;
        return entity.resultStatusType.in(resultStatusTypes);
    }

    public BooleanExpression flowPathLike(final String flowPath) {
        if (StringUtils.isEmpty(flowPath)) return null;
        return entity.flowPath.lower().like(buildLikeExpression(flowPath), ESCAPE_CHAR);
    }

    public BooleanExpression ownerLike(final String owner) {
        if (StringUtils.isEmpty(owner)) return null;
        return entity.owner.lower().like(buildLikeExpression(owner), ESCAPE_CHAR);
    }

    public BooleanExpression startTimeBetween(final Date from, final Date to) {
        if (from == null && to == null) return null;
        return entity.startTime.between(from, to);
    }

    private String buildLikeExpression(String str) {
        String normalized = sqlUtils.escapeLikeExpression(str.toLowerCase());
        return sqlUtils.normalizeContainingLikeExpression(normalized);
    }
}
