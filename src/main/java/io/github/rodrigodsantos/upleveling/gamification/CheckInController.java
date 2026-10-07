package io.github.rodrigodsantos.upleveling.gamification;

import io.github.rodrigodsantos.upleveling.gamification.dto.CheckInRequest;
import io.github.rodrigodsantos.upleveling.gamification.dto.CheckInResponse;
import io.github.rodrigodsantos.upleveling.gamification.dto.ProgressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Tag(name = "Gamificação")
public class CheckInController {

    private final CheckInService service;

    public CheckInController(CheckInService service) {
        this.service = service;
    }

    @PostMapping("/api/habits/{id}/check-ins")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Marca o hábito como cumprido (hoje ou ontem) e devolve XP, streak e nível")
    public CheckInResponse checkIn(@PathVariable Long id, @RequestBody(required = false) CheckInRequest request) {
        return service.checkIn(id, request == null ? null : request.date());
    }

    @DeleteMapping("/api/habits/{id}/check-ins")
    @Operation(summary = "Desfaz o último check-in do dia (hoje ou ontem); o XP volta")
    public CheckInResponse undo(@PathVariable Long id,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.undo(id, date);
    }

    @GetMapping("/api/me/progress")
    @Operation(summary = "XP total, nível e progresso até o próximo nível")
    public ProgressResponse progress() {
        return service.progress();
    }
}
