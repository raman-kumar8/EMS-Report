package com.example.emsreportingservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class RequestListUUidsDto {
    @NotNull(message = "uuids cannot be null")
    private List<UUID> uuids;
}
