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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        Instant timestamp = Instant.now();
        ZonedDateTime PacificTime = timestamp.atZone(ZoneId.of("America/Los_Angeles"));
        snapshot.setName(budgetScenario.getName());

        snapshot.setBudgetScenario(budgetScenario);
        snapshot.setWorkgroup(workgroup);
        snapshot.setYear(year);

        snapshot = workloadSnapshotRepository.save(snapshot);

        // generate workload assignments for workgroup and year
        List<WorkloadAssignment> workloadAssignments = workloadAssignmentService.generateWorkloadAssignments(workgroupId, year, snapshot);
//        workloadAssignments = workloadAssignmentService.saveAll(workloadAssignments);

        // set snapshot id for assignments
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
