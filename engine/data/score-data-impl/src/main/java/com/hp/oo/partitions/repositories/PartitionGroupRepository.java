package com.hp.oo.partitions.repositories;

import com.hp.oo.partitions.entities.PartitionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//import static javax.persistence.LockModeType.PESSIMISTIC_READ;

/**
 * Date: 4/23/12
 *
 * @author Dima Rassin
 */
public interface PartitionGroupRepository extends JpaRepository<PartitionGroup,Long> {

//	@Lock(PESSIMISTIC_READ)
	PartitionGroup findByName(String name);

	@Modifying
	@Query("update PartitionGroup p set p.activePartition = p.activePartition where p.name= :name")
	int lock(@Param("name") String name);
}
