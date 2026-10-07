package io.github.rodrigodsantos.upleveling.gamification;

import io.github.rodrigodsantos.upleveling.shared.exception.BusinessRuleException;

import java.time.LocalDate;

/**
 * A janela de datas em que se pode fazer (ou ver) check-in: hoje ou ontem. Usada pelo check-in e pelo resumo do dia.
 */
public final class CheckInDates {

    private CheckInDates() {
    }

    /** Sem data = hoje. Retroativo só até ontem (esqueceu de marcar); futuro nunca. */
    public static LocalDate resolve(LocalDate requested, LocalDate today) {
        LocalDate date = requested == null ? today : requested;
        if (date.isAfter(today) || date.isBefore(today.minusDays(1))) {
            throw new BusinessRuleException("Só é possível usar a data de hoje ou de ontem");
        }
        return date;
    }
}
