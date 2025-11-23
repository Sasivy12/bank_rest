package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class CreateCardRequest
{
    @NotBlank(message = "User id is required")
    private Long ownerId;

    @NotBlank(message = "Expiration date is required")
    private Date expirationDate;
}
