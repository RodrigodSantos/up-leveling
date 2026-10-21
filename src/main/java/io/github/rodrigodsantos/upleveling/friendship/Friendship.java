package io.github.rodrigodsantos.upleveling.friendship;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Um par de pessoas: o pedido (PENDING) e, depois do aceite, a amizade (ACCEPTED) são a mesma linha.
 * Só os ids, como em Habit: listar amigos não precisa carregar as duas entidades User de cada linha.
 */
@Entity
@Table(name = "friendships")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false, updatable = false)
    private Long requesterId;

    @Column(name = "addressee_id", nullable = false, updatable = false)
    private Long addresseeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FriendshipStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    protected Friendship() {
    }

    public Friendship(Long requesterId, Long addresseeId) {
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
        this.status = FriendshipStatus.PENDING;
    }

    public void accept(LocalDateTime now) {
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = now;
    }

    /** O outro lado do par, visto por quem está logado. */
    public Long otherThan(Long userId) {
        return requesterId.equals(userId) ? addresseeId : requesterId;
    }

    public Long getId() {
        return id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public Long getAddresseeId() {
        return addresseeId;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }
}
