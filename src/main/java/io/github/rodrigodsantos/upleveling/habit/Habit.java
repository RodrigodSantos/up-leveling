package io.github.rodrigodsantos.upleveling.habit;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "habits")
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Só o id do dono: o hábito nunca precisa carregar o usuário inteiro
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private HabitStatus status;

    /** Dias em que o hábito vale; vazio = todos os dias. Cada dia é uma linha na tabela habit_schedule. */
    @ElementCollection
    @CollectionTable(name = "habit_schedule", joinColumns = @JoinColumn(name = "habit_id"))
    @Column(name = "day_of_week", nullable = false)
    private Set<DayOfWeek> days = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Habit() {
    }

    public Habit(Long userId, String name, int xpReward, Set<DayOfWeek> days) {
        this.userId = userId;
        this.name = name;
        this.xpReward = xpReward;
        this.days.addAll(days);
        this.status = HabitStatus.ACTIVE;
    }

    public void update(String name, int xpReward, Set<DayOfWeek> days) {
        this.name = name;
        this.xpReward = xpReward;
        // Altera a mesma coleção em vez de trocar a referência: o Hibernate acompanha a coleção que ele carregou
        this.days.clear();
        this.days.addAll(days);
    }

    public void changeStatus(HabitStatus status) {
        this.status = status;
    }

    public void delete() {
        this.status = HabitStatus.DELETED;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public int getXpReward() {
        return xpReward;
    }

    public HabitStatus getStatus() {
        return status;
    }

    public Set<DayOfWeek> getDays() {
        return Set.copyOf(days);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
