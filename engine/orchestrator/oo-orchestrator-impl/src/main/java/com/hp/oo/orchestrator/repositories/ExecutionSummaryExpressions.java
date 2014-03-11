package com.hp.oo.orchestrator.repositories;

import ch.lambdaj.function.convert.Converter;
import com.hp.oo.enginefacade.execution.ComplexExecutionStatus;
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
import static com.mysema.query.support.Expressions.allOf;
import static com.mysema.query.support.Expressions.anyOf;
import static com.mysema.query.support.Expressions.booleanOperation;
import static com.mysema.query.support.Expressions.constant;
import static com.mysema.query.support.Expressions.stringOperation;

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

    public BooleanExpression complexStatusIn(List<ComplexExecutionStatus> statuses) {
        if (CollectionUtils.isEmpty(statuses)) return null;
        List<BooleanExpression> expressions = convert(statuses, new Converter<ComplexExecutionStatus, BooleanExpression>() {
            @Override
            public BooleanExpression convert(ComplexExecutionStatus from) {
                return complexStatusEq(from);
            }
        });
        return anyOf(expressions.toArray(new BooleanExpression[expressions.size()]));
    }

    public BooleanExpression complexStatusEq(ComplexExecutionStatus status) {
        return allOf(statusEq(status.getExecutionStatus()),
                resultStatusTypeEq(status.getResultStatus()),
                pauseReasonEq(status.getPauseReason()));
    }

    public BooleanExpression statusEq(ExecutionEnums.ExecutionStatus status) {
        if(status == null) {
            return null;
        } else {
            return entity.status.eq(status);
        }
    }

    public BooleanExpression resultStatusTypeEq(String result) {
        if(result == null) {
            return null;
        } else {
            return entity.resultStatusType.eq(result);
        }
    }

    public BooleanExpression pauseReasonEq(PauseReason pauseReason) {
        if(pauseReason == null) {
            return null;
        } else {
            return entity.pauseReason.eq(pauseReason);
        }
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
