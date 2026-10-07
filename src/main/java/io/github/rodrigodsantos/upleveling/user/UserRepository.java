package io.github.rodrigodsantos.upleveling.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Na troca de e-mail: outro usuário, que não eu, já usa este e-mail? */
    boolean existsByEmailAndIdNot(String email, Long id);
}
