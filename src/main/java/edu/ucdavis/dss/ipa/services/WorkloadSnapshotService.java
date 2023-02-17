package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.WorkloadSnapshot;
import java.util.List;

public interface WorkloadSnapshotService {

    WorkloadSnapshot create(long workgroupId, long budgetScenarioId);

    WorkloadSnapshot findById(long workloadSnapshotId);

    List<WorkloadSnapshot> findByWorkgroupIdAndYear(long workgroupId, long year);

    void deleteByBudgetScenarioId(long budgetScenarioId);

}
