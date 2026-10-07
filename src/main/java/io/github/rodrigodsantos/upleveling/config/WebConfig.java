package io.github.rodrigodsantos.upleveling.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Páginas saem num JSON estável: {"content": [...], "page": {"size", "number", "totalElements", "totalPages"}}.
 * Sem isto, o Spring serializaria a classe PageImpl inteira, que muda entre versões.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class WebConfig {
}
