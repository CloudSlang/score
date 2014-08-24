package com.hp.oo.engine.versioning.repositories;

import com.hp.oo.engine.versioning.entities.VersionCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * User: wahnonm
 * Date: 31/10/13
 * Time: 16:26
 */
public interface VersionRepository extends JpaRepository<VersionCounter, Long> {

    VersionCounter findByCounterName(String counterName);

    @Modifying
    @Query("update VersionCounter v set v.versionCount=v.versionCount+1 where v.counterName = :counterName")
    int incrementCounterByName(@Param("counterName") String counterName);
}
