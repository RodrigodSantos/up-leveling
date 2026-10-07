package io.github.rodrigodsantos.upleveling.auth;

import io.github.rodrigodsantos.upleveling.auth.dto.LoginRequest;
import io.github.rodrigodsantos.upleveling.auth.dto.RegisterRequest;
import io.github.rodrigodsantos.upleveling.auth.dto.TokenResponse;
import io.github.rodrigodsantos.upleveling.shared.EmailNormalizer;
import io.github.rodrigodsantos.upleveling.shared.exception.ConflictException;
import io.github.rodrigodsantos.upleveling.shared.exception.InvalidCredentialsException;
import io.github.rodrigodsantos.upleveling.user.User;
import io.github.rodrigodsantos.upleveling.user.UserRepository;
import io.github.rodrigodsantos.upleveling.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = EmailNormalizer.normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Já existe uma conta com o e-mail '" + email + "'");
        }
        User user = userRepository.save(
                new User(request.name().trim(), email, passwordEncoder.encode(request.password())));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(EmailNormalizer.normalize(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return tokenService.generate(user);
    }
}
