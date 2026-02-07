package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * DTO для создания бановской карты
 */
@Getter
@Setter
@AllArgsConstructor
public class CreateCardRequest
{
    /** Идентификатор владельца карты **/
    @NotNull(message = "User id is required")
    private Long ownerId;

    /** Дата окончания срока карты **/
    @NotNull(message = "Expiration date is required")
    private Date expirationDate;
}
