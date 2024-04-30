package com.newgen.ntlsnc.globalsettings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductTradePriceDto {

    private Long id;
    private String expiryDate;

    @NotNull(message = "Trade Price  is required.")
    private Float tradePrice = 0.0f;

    private Long productId;
    private Long currencyId;
    private Long organizationId;
}
