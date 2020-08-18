package edu.ucdavis.dss.ipa.entities;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@Entity
@Table(name = "SectionGroupCostInstructors")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SectionGroupCostInstructor extends BaseEntity {
    private long id;
    private Instructor instructor;
    private SectionGroupCost sectionGroupCost;
    private BigDecimal cost;

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
    @JoinColumn(name = "SectionGroupCostId", nullable = false)
    @NotNull
    @JsonIgnore
    public SectionGroupCost getSectionGroupCost() {
        return sectionGroupCost;
    }

    public void setSectionGroupCost(SectionGroupCost sectionGroupCost) {
        this.sectionGroupCost = sectionGroupCost;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructorId", nullable = false)
    @NotNull
    @JsonIgnore
    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }


    @JsonProperty("instructorId")
    @Transient
    public long getInsturctorId() {
        if(instructor != null) {
            return instructor.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("sectionGroupCostId")
    @Transient
    public long getSectionGroupCostId() {
        if(sectionGroupCost != null) {
            return sectionGroupCost.getId();
        } else {
            return 0;
        }
    }
}
