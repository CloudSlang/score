package com.hp.oo.orchestrator.repositories;

import com.hp.oo.orchestrator.entities.RunningExecutionConfigurationImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import javax.persistence.QueryHint;
import java.util.Date;
import java.util.List;

/**
 * User: hajyhia
 * Date: 1/17/13
 * Time: 12:47 PM
 */
public interface RunningExecutionConfigurationRepository extends JpaRepository<RunningExecutionConfigurationImpl, Long> {

    public List<RunningExecutionConfigurationImpl> findByCreatedTimeBefore(@Param("createdTime") Date createdTime);

    @Query("from RunningExecutionConfigurationImpl r where r.createdTime =  (select max(re.createdTime) from RunningExecutionConfigurationImpl re) order by r.createdTime")
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value ="true") })
    public List<RunningExecutionConfigurationImpl> findLatestTime();

}
