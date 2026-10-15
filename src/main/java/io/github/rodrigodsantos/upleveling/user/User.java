package io.github.rodrigodsantos.upleveling.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sem setters: o nome e o e-mail mudam juntos em {@link #updateProfile}, e a senha só por {@link #changePassword}.
 * Quem usa a classe não consegue deixar um usuário pela metade.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // Sempre gravado em minúsculas (quem normaliza é o service)
    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    // Cores de destaque salvas (matizes de 0 a 359°). ARRAY: o Hibernate grava a lista numa coluna INTEGER[]
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "accent_hues", nullable = false)
    private List<Integer> accentHues = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Exigido pelo Hibernate; protected para o resto do código usar o construtor completo. */
    protected User() {
    }

    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public void updateProfile(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    /** Substitui a lista inteira (quem valida o limite e as repetidas é o service). */
    public void replaceAccentHues(List<Integer> hues) {
        // Cópia: a lista de quem chamou não fica ligada à entidade (mudar ela depois não mudaria o usuário)
        this.accentHues = new ArrayList<>(hues);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Cópia só de leitura: a lista só muda por {@link #replaceAccentHues}. */
    public List<Integer> getAccentHues() {
        return List.copyOf(accentHues);
    }
}
