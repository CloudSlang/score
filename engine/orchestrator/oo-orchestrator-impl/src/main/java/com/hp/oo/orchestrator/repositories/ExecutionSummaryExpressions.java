package com.hp.oo.orchestrator.repositories;

import ch.lambdaj.function.convert.Converter;
import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.orchestrator.entities.QExecutionSummaryEntity;
import com.hp.score.engine.data.SqlUtils;
import com.mysema.query.types.Ops;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.path.StringPath;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static ch.lambdaj.Lambda.convert;
import static com.hp.oo.enginefacade.execution.ExecutionSummary.EMPTY_BRANCH;
import static com.mysema.query.support.Expressions.*;

/**
 * User: sadane
 * Date: 22/12/13
 * Time: 12:01
 */
public class ExecutionSummaryExpressions {

    @Autowired
    private SqlUtils sqlUtils;

    private static char ESCAPE_CHAR = SqlUtils.ESCAPE_CHAR.charAt(0);

    private QExecutionSummaryEntity entity = QExecutionSummaryEntity.executionSummaryEntity;

    public BooleanExpression branchIsEmpty() {
        return entity.branchId.eq(EMPTY_BRANCH);
    }

    public BooleanExpression statusIn(List<ExecutionEnums.ExecutionStatus> statuses) {
        if (CollectionUtils.isEmpty(statuses)) return null;
        return entity.status.in(statuses);
    }

    public BooleanExpression pauseReasonIn(List<PauseReason> pauseReasons) {
        if (CollectionUtils.isEmpty(pauseReasons)) return null;
        return entity.pauseReason.in(pauseReasons);
    }

    public BooleanExpression resultStatusTypeIn(List<String> resultStatusTypes) {
        if (CollectionUtils.isEmpty(resultStatusTypes)) return null;
        return entity.resultStatusType.upper().in(convert(resultStatusTypes, new Converter<String, String>() {
            @Override
            public String convert(String from) {
                return from.toUpperCase();
            }
        }));
    }

    public BooleanExpression resultStatusTypeIsNull() {
        return entity.resultStatusType.isNull();
    }

    public BooleanExpression flowPathLike(final String flowPath) {
        if (StringUtils.isEmpty(flowPath)) return null;
        return buildLikeExpression(entity.flowPath, flowPath);
    }

    public BooleanExpression ownerLike(final String owner) {
        if (StringUtils.isEmpty(owner)) return null;
        return buildLikeExpression(entity.owner, owner);
    }

    public BooleanExpression runIdLike(final String runId) {
        if (StringUtils.isEmpty(runId)) return null;
        return buildLikeExpression(entity.executionId, runId);
    }

    public BooleanExpression flowUuidLike(final String flowUuid) {
        if (StringUtils.isEmpty(flowUuid)) return null;
        return buildLikeExpression(entity.flowUuid, flowUuid);
    }

    public BooleanExpression runNameLike(final String executionName) {
        if (StringUtils.isEmpty(executionName)) return null;
        return buildLikeExpression(entity.executionName, executionName);
    }

    public BooleanExpression startTimeBetween(final Date from, final Date to) {
        if (from == null && to == null) return null;
        return entity.startTime.between(from, to);
    }

    private BooleanExpression buildLikeExpression(StringPath field, String value) {
        String normalized = sqlUtils.escapeLikeExpression(value);
        normalized = sqlUtils.normalizeContainingLikeExpression(normalized);
        return booleanOperation(
                Ops.LIKE_ESCAPE,
                field.toUpperCase(),
                stringOperation(Ops.UPPER, constant(normalized)),
                constant(ESCAPE_CHAR));
    }
}
