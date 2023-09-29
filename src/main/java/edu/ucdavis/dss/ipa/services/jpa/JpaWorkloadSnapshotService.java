package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.entities.WorkloadAssignment;
import edu.ucdavis.dss.ipa.entities.WorkloadSnapshot;
import edu.ucdavis.dss.ipa.repositories.WorkloadSnapshotRepository;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.ipa.services.WorkloadAssignmentService;
import edu.ucdavis.dss.ipa.services.WorkloadSnapshotService;
import java.util.List;
import javax.inject.Inject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaWorkloadSnapshotService implements WorkloadSnapshotService {
    @Inject WorkloadSnapshotRepository workloadSnapshotRepository;
    @Inject
    BudgetScenarioService budgetScenarioService;
    @Inject WorkgroupService workgroupService;
    @Inject WorkloadAssignmentService workloadAssignmentService;

    public WorkloadSnapshot findById(long workloadSnapshotId) {
        return workloadSnapshotRepository.findById(workloadSnapshotId);
    }

    public List<WorkloadSnapshot> findByWorkgroupIdAndYear(long workgroupId, long year) {
        return workloadSnapshotRepository.findByWorkgroupIdAndYear(workgroupId, year);
    }

    @Transactional
    public WorkloadSnapshot create(long workgroupId, long budgetScenarioId) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);
        long year = budgetScenario.getBudget().getSchedule().getYear();

        WorkloadSnapshot snapshot = new WorkloadSnapshot();

        snapshot.setName(budgetScenario.getName());
        snapshot.setBudgetScenario(budgetScenario);
        snapshot.setWorkgroup(workgroup);
        snapshot.setYear(year);
        snapshot = workloadSnapshotRepository.save(snapshot);
        List<WorkloadAssignment> workloadAssignments = workloadAssignmentService.generateWorkloadAssignments(workgroupId, year, snapshot);
        snapshot.setWorkloadAssignments(workloadAssignments);

        return workloadSnapshotRepository.save(snapshot);
    }

    public void deleteByBudgetScenarioId(long budgetScenarioId) {
        WorkloadSnapshot snapshot = workloadSnapshotRepository.findByBudgetScenarioId(budgetScenarioId);

        if (snapshot != null) {
            workloadSnapshotRepository.delete(snapshot.getId());
        }
    };
}
