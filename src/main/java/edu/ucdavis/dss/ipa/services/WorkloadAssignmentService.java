package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.WorkloadAssignment;
import edu.ucdavis.dss.ipa.entities.WorkloadSnapshot;
import java.util.List;

public interface WorkloadAssignmentService {
    List<WorkloadAssignment> findByWorkloadSnapshotId(long id);

    List<WorkloadAssignment> generateWorkloadAssignments(long workgroupId, long year);
    List<WorkloadAssignment> generateWorkloadAssignments(long workgroupId, long year, WorkloadSnapshot workloadSnapshot);

    List<WorkloadAssignment> saveAll(List<WorkloadAssignment> workloadAssignments);
}
