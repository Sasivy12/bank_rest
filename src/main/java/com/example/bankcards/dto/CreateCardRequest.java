package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class CreateCardRequest
{
    @NotNull(message = "User id is required")
    private Long ownerId;

    @NotNull(message = "Expiration date is required")
    private Date expirationDate;
}
