package edu.ucdavis.dss.ipa.entities;

import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
    private InstructorType instructorType;
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
    @JsonProperty("sectionGroupCostId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public SectionGroupCost getSectionGroupCost() {
        return sectionGroupCost;
    }

    public void setSectionGroupCost(SectionGroupCost sectionGroupCost) {
        this.sectionGroupCost = sectionGroupCost;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeachingAssignmentId")
    @JsonProperty("teachingAssignmentId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public TeachingAssignment getTeachingAssignment() {
        return teachingAssignment;
    }

    public void setTeachingAssignment(TeachingAssignment teachingAssignment) {
        this.teachingAssignment = teachingAssignment;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructorId")
    @JsonProperty("instructorId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructorTypeId", nullable = true)
    @JsonProperty("instructorTypeId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    public InstructorType getInstructorType() {
        return instructorType;
    }

    public void setInstructorType(InstructorType instructorType) {
        this.instructorType = instructorType;
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

    @JsonProperty("instructorName")
    @Transient
    public String getInsturctorName() {
        if(instructor != null) {
            return instructor.getFullName();
        } else {
            return "";
        }
    }

    @JsonProperty("instructorTypeDescription")
    @Transient
    public String getInstructorTypeDescription() {
        if(instructorType != null) {
            return instructorType.getDescription();
        } else {
            return "";
        }
    }
}
