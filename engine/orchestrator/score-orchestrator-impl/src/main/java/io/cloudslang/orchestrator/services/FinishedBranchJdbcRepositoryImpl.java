package io.cloudslang.orchestrator.services;

import com.google.common.base.Joiner;
import io.cloudslang.orchestrator.repositories.FinishedBranchJdbcRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FinishedBranchJdbcRepositoryImpl implements FinishedBranchJdbcRepository {
    public static final String SELECT_STATEMENT = "Select DATALENGTH(l.BRANCH_CONTEXT) from OO_FINISHED_BRANCHES l where l.EXECUTION_ID in (:IDS)";
    private static final Logger logger = Logger.getLogger(FinishedBranchJdbcRepositoryImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Long> getSizeOfBlob(Collection<String> suspendedExecutionIds) {
        if (suspendedExecutionIds.size() > 0) {
            String ids = Joiner.on("','").skipNulls().join(suspendedExecutionIds);
            ids="'"+ids+"'";
            String sqlStat = SELECT_STATEMENT.replace(":IDS", ids);
            return jdbcTemplate.queryForList(sql(sqlStat), Long.class);
        } else {
            return new ArrayList<>();
        }
    }

    private String sql(String sql) {
        if (logger.isDebugEnabled()) {
            logger.debug("SQL: " + sql);
        }
        return sql;
    }
}
