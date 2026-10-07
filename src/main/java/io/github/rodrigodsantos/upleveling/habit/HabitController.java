package io.github.rodrigodsantos.upleveling.habit;

import io.github.rodrigodsantos.upleveling.habit.dto.HabitRequest;
import io.github.rodrigodsantos.upleveling.habit.dto.HabitResponse;
import io.github.rodrigodsantos.upleveling.habit.dto.HabitStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/habits")
@Tag(name = "Hábitos")
public class HabitController {

    private final HabitService service;

    public HabitController(HabitService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista os seus hábitos por nome (sem filtro: ativos e pausados)")
    public List<HabitResponse> list(@RequestParam(required = false) HabitStatus status) {
        return service.list(status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um hábito")
    public HabitResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @Operation(summary = "Cria um hábito (days vazio = todos os dias)")
    public ResponseEntity<HabitResponse> create(@RequestBody @Valid HabitRequest request) {
        HabitResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome, XP e dias do hábito")
    public HabitResponse update(@PathVariable Long id, @RequestBody @Valid HabitRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Pausa (PAUSED) ou reativa (ACTIVE) um hábito")
    public HabitResponse changeStatus(@PathVariable Long id, @RequestBody @Valid HabitStatusRequest request) {
        return service.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui um hábito (exclusão lógica: o histórico é mantido)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
