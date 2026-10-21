package io.github.rodrigodsantos.upleveling.friendship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Toda consulta inclui o usuário logado como um dos lados: ninguém lê ou mexe no par de outras pessoas. */
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /** As linhas em que o usuário aparece, de qualquer lado (pediu ou foi pedido), no status informado. */
    @Query("""
            SELECT f FROM Friendship f
            WHERE f.status = :status AND (f.requesterId = :userId OR f.addresseeId = :userId)
            ORDER BY f.createdAt DESC
            """)
    List<Friendship> findAllOf(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    /** O par entre duas pessoas, seja quem for que pediu. */
    @Query("""
            SELECT f FROM Friendship f
            WHERE (f.requesterId = :a AND f.addresseeId = :b) OR (f.requesterId = :b AND f.addresseeId = :a)
            """)
    Optional<Friendship> findBetween(@Param("a") Long a, @Param("b") Long b);

    /** Apaga direto no banco tudo em que o usuário aparece (usado ao recriar a conta demo). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM Friendship f WHERE f.requesterId = :userId OR f.addresseeId = :userId")
    void deleteAllOf(@Param("userId") Long userId);
}
