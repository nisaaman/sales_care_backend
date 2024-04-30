package com.newgen.ntlsnc.subscriptions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPackageDto {
    private Long id;
    @NotNull(message = "Name field is required.")
    private String name;

    @NotNull(message = "Price field is required.")
    private Double price;

    @NotNull(message = "Duration field is required.")
    private String duration;

    @NotNull(message = "Duration type field is required.")
    private String durationType;

    @NotNull(message = "Number of Unit field is required.")
    private Integer numberOfUnits;

    @NotNull(message = "Number of Users field is required.")
    private Integer numberOfUsers;
}

