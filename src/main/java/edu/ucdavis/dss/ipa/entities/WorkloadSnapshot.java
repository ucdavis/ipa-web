package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "WorkloadSnapshots")
public class WorkloadSnapshot {
    private long id;
    private String name;
    private BudgetScenario budgetScenario;
    private Workgroup workgroup;
    private long year;
    private List<WorkloadAssignment> workloadAssignments;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BudgetScenarioId", nullable = false)
    @NotNull
    @JsonIgnore
    public BudgetScenario getBudgetScenario() {
        return budgetScenario;
    }

    public void setBudgetScenario(BudgetScenario budgetScenario) {
        this.budgetScenario = budgetScenario;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WorkgroupId", nullable = false)
    @NotNull
    @JsonIgnore
    public Workgroup getWorkgroup() {
        return workgroup;
    }

    public void setWorkgroup(Workgroup workgroup) {
        this.workgroup = workgroup;
    }

    public long getYear() {
        return year;
    }

    public void setYear(long year) {
        this.year = year;
    }

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "workloadSnapshot", cascade = {CascadeType.ALL})
    public List<WorkloadAssignment> getWorkloadAssignments() {
        return workloadAssignments;
    }

    public void setWorkloadAssignments(List<WorkloadAssignment> workloadAssignments) {
        this.workloadAssignments = workloadAssignments;
    }
}
