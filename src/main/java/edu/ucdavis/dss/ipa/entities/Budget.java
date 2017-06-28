package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Budget is used to record information common to all budget scenarios.
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "Budgets")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Budget {
    private long id;
    private Schedule schedule;
    private long taCost, readerCost, lecturerCost;

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
    @JoinColumn(name = "ScheduleId", nullable = false)
    @NotNull
    @JsonIgnore
    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public long getTaCost() {
        return taCost;
    }

    public void setTaCost(long taCost) {
        this.taCost = taCost;
    }

    public long getReaderCost() {
        return readerCost;
    }

    public void setReaderCost(long readerCost) {
        this.readerCost = readerCost;
    }

    public long getLecturerCost() {
        return lecturerCost;
    }

    public void setLecturerCost(long lecturerCost) {
        this.lecturerCost = lecturerCost;
    }
}