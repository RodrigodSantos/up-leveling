package io.github.rodrigodsantos.upleveling.gamification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Um "cumpri o hábito". Imutável depois de criado: desfazer é apagar a linha. */
@Entity
@Table(name = "check_ins")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "habit_id", nullable = false, updatable = false)
    private Long habitId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "check_in_date", nullable = false, updatable = false)
    private LocalDate checkInDate;

    @Column(nullable = false, updatable = false)
    private int xp;

    @Column(name = "bonus_xp", nullable = false, updatable = false)
    private int bonusXp;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected CheckIn() {
    }

    public CheckIn(Long habitId, Long userId, LocalDate checkInDate, int xp, int bonusXp) {
        this.habitId = habitId;
        this.userId = userId;
        this.checkInDate = checkInDate;
        this.xp = xp;
        this.bonusXp = bonusXp;
    }

    /** XP que este check-in rendeu, bônus incluído (é o que volta ao desfazer). */
    public int totalXp() {
        return xp + bonusXp;
    }

    public int getBonusXp() {
        return bonusXp;
    }
}
