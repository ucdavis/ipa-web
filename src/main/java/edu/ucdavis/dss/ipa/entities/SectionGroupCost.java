package edu.ucdavis.dss.ipa.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.SectionGroupCostDeserializer;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@Entity
@Table(name = "SectionGroupCosts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = SectionGroupCostDeserializer.class)
public class SectionGroupCost extends BaseEntity {
    private long id;
    private SectionGroup sectionGroup;
    private BudgetScenario budgetScenario;
    private Integer sectionCount;
    private Long enrollment;
    private Instructor instructor;
    private Instructor originalInstructor;
    private String reason;
    private Float cost, taCount, readerCount;
    private List<SectionGroupCostComment> sectionGroupCostComments = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId()
    {
        return this.id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SectionGroupId", nullable = true)
    @NotNull
    @JsonIgnore
    public SectionGroup getSectionGroup() {
        return sectionGroup;
    }

    public void setSectionGroup(SectionGroup sectionGroup) {
        this.sectionGroup = sectionGroup;
    }

    public Long getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Long enrollment) {
        this.enrollment = enrollment;
    }

    public Float getTaCount() {
        return taCount;
    }

    public void setTaCount(Float taCount) {
        this.taCount = taCount;
    }

    public Integer getSectionCount() {
        return sectionCount;
    }

    public void setSectionCount(Integer sectionCount) {
        this.sectionCount = sectionCount;
    }

    public Float getReaderCount() {
        return readerCount;
    }

    public void setReaderCount(Float readerCount) {
        this.readerCount = readerCount;
    }

    public Float getCost() {
        return cost;
    }

    public void setCost(Float cost) {
        this.cost = cost;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructorId", nullable = true)
    @JsonIgnore
    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OriginalInstructorId", nullable = true)
    @JsonIgnore
    public Instructor getOriginalInstructor() {
        return originalInstructor;
    }

    public void setOriginalInstructor(Instructor originalInstructor) {
        this.originalInstructor = originalInstructor;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BudgetScenarioId", nullable = false)
    @NotNull
    @JsonIgnore
    public BudgetScenario getBudgetScenario() {
        return budgetScenario;
    }

    public void setBudgetScenario(BudgetScenario budgetScenario) {
        this.budgetScenario = budgetScenario;
    }

    @JsonIgnore
    @OneToMany(mappedBy="sectionGroupCost", cascade=CascadeType.ALL, orphanRemoval = true)
    public List<SectionGroupCostComment> getSectionGroupCostComments() {
        return sectionGroupCostComments;
    }

    public void setSectionGroupCostComments(List<SectionGroupCostComment> sectionGroupCostComments) {
        this.sectionGroupCostComments = sectionGroupCostComments;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @JsonProperty("budgetScenarioId")
    @Transient
    public long getBudgetScenarioIdentification() {
        if(budgetScenario != null) {
            return budgetScenario.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("sectionGroupId")
    @Transient
    public long getSectionGroupIdentification() {
        if(sectionGroup != null) {
            return sectionGroup.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("instructorId")
    @Transient
    public long getInstructorIdentification() {
        if(instructor != null) {
            return instructor.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("originalInstructorId")
    @Transient
    public long getOriginalInstructorIdentification() {
        if(originalInstructor != null) {
            return originalInstructor.getId();
        } else {
            return 0;
        }
    }
}