package com.newgen.ntlsnc.salesandcollection.dto;

import com.newgen.ntlsnc.common.enums.IntactType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author marziah
 * @Date 20/04/22
 */



@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReturnProposalDetailsDto {
    private Long id;

    @NotNull(message = "Quantity is required.")
    private Integer quantity;

    private Float rate;

    @NotNull(message = "Intact Type is required.")
    private String intactType;

    @NotNull(message = "Batch is required.")
    private Long batchId;

    @NotNull(message = "Product is required.")
    private Long productId;

    @NotNull(message = "Trade Price is required.")
    private Long productTradePriceId;

    private Long tradeDiscountId;

    private Long salesReturnProposalId;

    private Long organizationId;
}
