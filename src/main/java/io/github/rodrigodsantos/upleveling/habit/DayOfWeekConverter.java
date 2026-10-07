package io.github.rodrigodsantos.upleveling.habit;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;

/**
 * No Java, o dia é o enum DayOfWeek (MONDAY...); no banco, o número ISO de 1 (segunda) a 7 (domingo).
 * autoApply: vale para todo atributo DayOfWeek das entidades, sem precisar anotar cada um.
 */
@Converter(autoApply = true)
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, Short> {

    @Override
    public Short convertToDatabaseColumn(DayOfWeek day) {
        return day == null ? null : (short) day.getValue();
    }

    @Override
    public DayOfWeek convertToEntityAttribute(Short value) {
        return value == null ? null : DayOfWeek.of(value);
    }
}
