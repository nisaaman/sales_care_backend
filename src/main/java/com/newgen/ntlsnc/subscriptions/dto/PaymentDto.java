package com.newgen.ntlsnc.subscriptions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    @NotNull(message = "Name field is required.")
    private String name;

    @NotNull(message = "Amount field is required.")
    private Double amount;

    @NotNull(message = "Payment date field is required.")
    private String paymentDate;

    @NotNull(message = "Payment method field is required.")
    private String paymentMethod;

    @NotNull(message = "Subscription Package field is required.")
    private Long subscriptionPackageId;

    @NotNull(message = "Organization field is required.")
    private Long organizationId;
}
