package com.example.up_leveling.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class SuccessDTO {
    private String message;
    private Object data;
    private String timestamp;

    public SuccessDTO(String message, Object data) {
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
