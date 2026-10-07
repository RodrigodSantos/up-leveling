package io.github.rodrigodsantos.upleveling.user;

import io.github.rodrigodsantos.upleveling.shared.DemoAccount;
import io.github.rodrigodsantos.upleveling.shared.EmailNormalizer;
import io.github.rodrigodsantos.upleveling.shared.exception.BusinessRuleException;
import io.github.rodrigodsantos.upleveling.shared.exception.ConflictException;
import io.github.rodrigodsantos.upleveling.shared.exception.ResourceNotFoundException;
import io.github.rodrigodsantos.upleveling.shared.security.CurrentUser;
import io.github.rodrigodsantos.upleveling.user.dto.ChangePasswordRequest;
import io.github.rodrigodsantos.upleveling.user.dto.UpdateProfileRequest;
import io.github.rodrigodsantos.upleveling.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tudo aqui age sobre o usuário logado: não existe rota para ler ou alterar outro usuário.
 */
@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, CurrentUser currentUser) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return UserResponse.from(loggedUser());
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = loggedUser();
        protectDemoAccount(user);
        String email = EmailNormalizer.normalize(request.email());
        if (repository.existsByEmailAndIdNot(email, user.getId())) {
            throw new ConflictException("Já existe uma conta com o e-mail '" + email + "'");
        }
        // Sem save(): a entidade veio do banco nesta transação, e o Hibernate grava a mudança no commit
        user.updateProfile(request.name().trim(), email);
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = loggedUser();
        protectDemoAccount(user);
        // 422, e não 401: o token é válido; um 401 faria o frontend deslogar o usuário por ter errado a senha
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("A senha atual está incorreta");
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    /** A conta demo é compartilhada: se um visitante trocasse a senha ou o e-mail, trancaria todos os outros. */
    private void protectDemoAccount(User user) {
        if (DemoAccount.is(user.getEmail())) {
            throw new BusinessRuleException("A conta de demonstração não pode ter o e-mail nem a senha alterados");
        }
    }

    private User loggedUser() {
        Long id = currentUser.id();
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }
}
