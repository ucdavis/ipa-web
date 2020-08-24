package edu.ucdavis.dss.ipa.entities;

import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.SectionGroupCostInstructorDeserializer;


@Entity
@Table(name = "SectionGroupCostInstructors")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = SectionGroupCostInstructorDeserializer.class)
public class SectionGroupCostInstructor extends BaseEntity {
    private long id;
    private Instructor instructor;
    private SectionGroupCost sectionGroupCost;
    private TeachingAssignment teachingAssignment;
    private BigDecimal cost;
    private String reason;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeachingAssignmentId")
    @JsonIgnore
    public TeachingAssignment getTeachingAssignment() {
        return teachingAssignment;
    }

    public void setTeachingAssignment(TeachingAssignment teachingAssignment) {
        this.teachingAssignment = teachingAssignment;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    @JsonProperty("instructorName")
    @Transient
    public String getInsturctorName() {
        if(instructor != null) {
            return instructor.getFullName();
        } else {
            return "";
        }
    }
}
