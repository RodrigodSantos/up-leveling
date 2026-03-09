package com.example.up_leveling.entity;

public enum StatusType {
    ACTIVE,
    INACTIVE,
    DELETED;

    public static StatusType fromString(String status) {
        return switch (status) {
            case "ACTIVE" -> ACTIVE;
            case "INACTIVE" -> INACTIVE;
            case "DELETED" -> DELETED;
            default -> throw new IllegalArgumentException("Status inválido: " + status);
        };
    }
}
