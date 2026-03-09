package com.example.up_leveling.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.annotations.Check;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "habit_schedule", uniqueConstraints = {
        @UniqueConstraint(name = "unique_habit_day", columnNames = {"habit_id", "day_of_week"})
})
@EntityListeners(AuditingEntityListener.class)
public class HabitSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Check(constraints = "day_of_week BETWEEN 1 AND 7")
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "habit_id", nullable = false, foreignKey = @ForeignKey(name = "fk_habit_schedule_habit"))
    private Habit habit;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
