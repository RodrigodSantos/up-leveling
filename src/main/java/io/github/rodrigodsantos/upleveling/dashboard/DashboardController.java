package io.github.rodrigodsantos.upleveling.dashboard;

import io.github.rodrigodsantos.upleveling.dashboard.dto.CheckInHistoryItem;
import io.github.rodrigodsantos.upleveling.dashboard.dto.DailyXp;
import io.github.rodrigodsantos.upleveling.dashboard.dto.TodayResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController
@Tag(name = "Painel")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/api/today")
    @Operation(summary = "Resumo do dia: hábitos agendados, andamento, streak, XP do dia e nível (hoje ou ontem)")
    public TodayResponse today(@RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate date) {
        return service.today(date);
    }

    @GetMapping("/api/check-ins")
    @Operation(summary = "Histórico de check-ins, do mais recente para o mais antigo (máx. 100 por página)")
    public Page<CheckInHistoryItem> history(@RequestParam(required = false) Long habitId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate from,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate to,
                                            @PageableDefault(size = 20) Pageable pageable) {
        return service.history(habitId, from, to, pageable);
    }

    @GetMapping("/api/me/xp-history")
    @Operation(summary = "XP ganho por dia, para o gráfico (padrão: últimos 30 dias; dias vazios = 0)")
    public List<DailyXp> xpHistory(@RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate from,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DATE) LocalDate to) {
        return service.xpHistory(from, to);
    }
}
