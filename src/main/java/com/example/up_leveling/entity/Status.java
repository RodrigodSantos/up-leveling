package com.example.up_leveling.entity;

public enum Status {
    ACTIVE,
    INACTIVE,
    DELETED;

    public static Status fromString(String status) {
        return switch (status) {
            case "ACTIVE" -> ACTIVE;
            case "INACTIVE" -> INACTIVE;
            case "DELETED" -> DELETED;
            default -> throw new IllegalArgumentException("Status inválido: " + status);
        };
    }
}
