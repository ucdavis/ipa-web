package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.WorkloadAssignment;
import edu.ucdavis.dss.ipa.entities.WorkloadSnapshot;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface WorkloadAssignmentRepository extends CrudRepository<WorkloadAssignment, Long> {
    WorkloadAssignment findById(long id);

    List<WorkloadAssignment> findByWorkloadSnapshotId(long id);
}
