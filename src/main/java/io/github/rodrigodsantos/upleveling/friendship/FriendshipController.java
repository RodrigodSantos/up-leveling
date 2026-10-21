package io.github.rodrigodsantos.upleveling.friendship;

import io.github.rodrigodsantos.upleveling.friendship.dto.FriendRequestCreate;
import io.github.rodrigodsantos.upleveling.friendship.dto.FriendRequestResponse;
import io.github.rodrigodsantos.upleveling.friendship.dto.FriendRequestsResponse;
import io.github.rodrigodsantos.upleveling.friendship.dto.FriendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.github.rodrigodsantos.upleveling.friendship.FriendshipStatus.ACCEPTED;

@RestController
@RequestMapping("/api/friends")
@Tag(name = "Amigos")
public class FriendshipController {

    private final FriendshipService service;

    public FriendshipController(FriendshipService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Seus amigos por nome, com nível e XP")
    public List<FriendResponse> friends() {
        return service.friends();
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desfaz a amizade (dos dois lados)")
    public void remove(@PathVariable Long userId) {
        service.remove(userId);
    }

    @GetMapping("/requests")
    @Operation(summary = "Pedidos pendentes: recebidos e enviados")
    public FriendRequestsResponse requests() {
        return service.requests();
    }

    /** 201 quando cria o pedido; 200 quando a outra pessoa já tinha pedido e este envio aceitou o pedido dela. */
    @PostMapping("/requests")
    @Operation(summary = "Envia um pedido pelo código de amigo (se a pessoa já tinha te pedido, vocês viram amigos)")
    public ResponseEntity<FriendRequestResponse> sendRequest(@RequestBody @Valid FriendRequestCreate request) {
        FriendRequestResponse response = service.sendRequest(request.friendCode());
        HttpStatus status = response.status() == ACCEPTED ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/requests/{id}/accept")
    @Operation(summary = "Aceita um pedido recebido")
    public FriendResponse accept(@PathVariable Long id) {
        return service.accept(id);
    }

    @DeleteMapping("/requests/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Recusa um pedido recebido ou cancela um enviado")
    public void deleteRequest(@PathVariable Long id) {
        service.deleteRequest(id);
    }
}
