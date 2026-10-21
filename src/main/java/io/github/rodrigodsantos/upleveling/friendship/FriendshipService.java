package io.github.rodrigodsantos.upleveling.friendship;

import io.github.rodrigodsantos.upleveling.friendship.dto.FriendRequestResponse;
import io.github.rodrigodsantos.upleveling.friendship.dto.FriendRequestsResponse;
import io.github.rodrigodsantos.upleveling.friendship.dto.FriendResponse;
import io.github.rodrigodsantos.upleveling.gamification.CheckInRepository;
import io.github.rodrigodsantos.upleveling.gamification.UserXp;
import io.github.rodrigodsantos.upleveling.gamification.dto.ProgressResponse;
import io.github.rodrigodsantos.upleveling.shared.DemoAccount;
import io.github.rodrigodsantos.upleveling.shared.exception.BusinessRuleException;
import io.github.rodrigodsantos.upleveling.shared.exception.ConflictException;
import io.github.rodrigodsantos.upleveling.shared.exception.ResourceNotFoundException;
import io.github.rodrigodsantos.upleveling.shared.security.CurrentUser;
import io.github.rodrigodsantos.upleveling.user.FriendCode;
import io.github.rodrigodsantos.upleveling.user.User;
import io.github.rodrigodsantos.upleveling.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.rodrigodsantos.upleveling.friendship.FriendshipStatus.ACCEPTED;
import static io.github.rodrigodsantos.upleveling.friendship.FriendshipStatus.PENDING;

/**
 * Pedido → aceite → amizade. Tudo é visto pelo lado de quem está logado: "o outro" é sempre a outra ponta do par.
 * Pedido que não existe, que já foi respondido ou que é de outras pessoas responde 404, sem revelar qual foi o caso.
 */
@Service
public class FriendshipService {

    private final FriendshipRepository repository;
    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final CurrentUser currentUser;
    private final Clock clock;

    public FriendshipService(FriendshipRepository repository, UserRepository userRepository,
                             CheckInRepository checkInRepository, CurrentUser currentUser, Clock clock) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.checkInRepository = checkInRepository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    /** Amigos por nome, cada um com o nível. Três consultas no total, quantos amigos houver (sem N+1). */
    @Transactional(readOnly = true)
    public List<FriendResponse> friends() {
        Long me = currentUser.id();
        List<Friendship> friendships = repository.findAllOf(me, ACCEPTED);
        if (friendships.isEmpty()) {
            return List.of();
        }
        List<Long> ids = friendships.stream().map(f -> f.otherThan(me)).toList();
        Map<Long, User> users = usersById(ids);
        Map<Long, Long> xp = checkInRepository.totalXpOf(ids).stream()
                .collect(Collectors.toMap(UserXp::userId, UserXp::totalXp));

        return friendships.stream()
                .map(f -> {
                    User friend = users.get(f.otherThan(me));
                    // Amigo sem nenhum check-in não volta da consulta de XP: começa do zero
                    return new FriendResponse(friend.getId(), friend.getName(),
                            ProgressResponse.of(xp.getOrDefault(friend.getId(), 0L)), f.getAcceptedAt());
                })
                .sorted(Comparator.comparing(FriendResponse::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public FriendRequestsResponse requests() {
        Long me = currentUser.id();
        List<Friendship> pending = repository.findAllOf(me, PENDING);
        Map<Long, User> users = usersById(pending.stream().map(f -> f.otherThan(me)).toList());

        List<FriendRequestResponse> received = pending.stream()
                .filter(f -> f.getAddresseeId().equals(me))
                .map(f -> toRequest(f, users.get(f.getRequesterId())))
                .toList();
        List<FriendRequestResponse> sent = pending.stream()
                .filter(f -> f.getRequesterId().equals(me))
                .map(f -> toRequest(f, users.get(f.getAddresseeId())))
                .toList();
        return new FriendRequestsResponse(received, sent);
    }

    /**
     * Envia um pedido para o dono do código. Se essa pessoa já tinha mandado um pedido para mim,
     * os dois querem a mesma coisa: em vez de criar um segundo pedido, este aceita o dela (status ACCEPTED).
     */
    @Transactional
    public FriendRequestResponse sendRequest(String typedCode) {
        User me = loggedUser();
        protectDemoAccount(me);
        String code = FriendCode.normalize(typedCode);
        User target = userRepository.findByFriendCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma conta com o código '" + code + "'"));
        if (target.getId().equals(me.getId())) {
            throw new BusinessRuleException("Esse é o seu próprio código de amigo");
        }
        // A demo é de todos os visitantes: um pedido para ela ficaria esperando um aceite que nunca vem
        if (DemoAccount.is(target.getEmail())) {
            throw new BusinessRuleException("A conta de demonstração não recebe pedidos de amizade");
        }

        Friendship existing = repository.findBetween(me.getId(), target.getId()).orElse(null);
        if (existing == null) {
            Friendship created = repository.save(new Friendship(me.getId(), target.getId()));
            return toRequest(created, target);
        }
        if (existing.getStatus() == ACCEPTED) {
            throw new ConflictException("Você e " + target.getName() + " já são amigos");
        }
        if (existing.getRequesterId().equals(me.getId())) {
            throw new ConflictException("Você já enviou um pedido para " + target.getName());
        }
        existing.accept(LocalDateTime.now(clock));
        return toRequest(existing, target);
    }

    /** Só quem recebeu o pedido pode aceitar. */
    @Transactional
    public FriendResponse accept(Long requestId) {
        User me = loggedUser();
        protectDemoAccount(me);
        Friendship request = repository.findById(requestId)
                .filter(f -> f.getStatus() == PENDING && f.getAddresseeId().equals(me.getId()))
                .orElseThrow(() -> requestNotFound(requestId));
        request.accept(LocalDateTime.now(clock));

        User friend = findUser(request.getRequesterId());
        return new FriendResponse(friend.getId(), friend.getName(),
                ProgressResponse.of(checkInRepository.totalXp(friend.getId())), request.getAcceptedAt());
    }

    /** Recusar (quem recebeu) e cancelar (quem enviou) são a mesma coisa: o pedido deixa de existir. */
    @Transactional
    public void deleteRequest(Long requestId) {
        User me = loggedUser();
        protectDemoAccount(me);
        Friendship request = repository.findById(requestId)
                .filter(f -> f.getStatus() == PENDING
                        && (f.getRequesterId().equals(me.getId()) || f.getAddresseeId().equals(me.getId())))
                .orElseThrow(() -> requestNotFound(requestId));
        repository.delete(request);
    }

    /** Desfaz a amizade dos dois lados (é uma linha só). */
    @Transactional
    public void remove(Long friendId) {
        User me = loggedUser();
        protectDemoAccount(me);
        Friendship friendship = repository.findBetween(me.getId(), friendId)
                .filter(f -> f.getStatus() == ACCEPTED)
                .orElseThrow(() -> new ResourceNotFoundException("Amigo", friendId));
        repository.delete(friendship);
    }

    private FriendRequestResponse toRequest(Friendship friendship, User other) {
        return new FriendRequestResponse(friendship.getId(), other.getId(), other.getName(), friendship.getStatus(),
                friendship.getCreatedAt());
    }

    private Map<Long, User> usersById(List<Long> ids) {
        return userRepository.findAllById(ids).stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    /** A demo é compartilhada: a lista de amigos dela vem pronta (DemoDataLoader) e nenhum visitante a altera. */
    private void protectDemoAccount(User user) {
        if (DemoAccount.is(user.getEmail())) {
            throw new BusinessRuleException("A conta de demonstração não pode alterar amizades");
        }
    }

    private ResourceNotFoundException requestNotFound(Long id) {
        return new ResourceNotFoundException("Pedido de amizade " + id + " não encontrado");
    }

    private User loggedUser() {
        return findUser(currentUser.id());
    }

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }
}
