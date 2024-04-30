package com.newgen.ntlsnc.supplychainmanagement.dto;

import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.InvReturnType;
import com.newgen.ntlsnc.common.enums.QaStatus;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransactionDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author sunipa
 * @Date 22/09/22
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvClaimDetailsDto {

    private Long id;

    private Float quantityInUom = 0.0f;

    @NotNull(message = "Quantity field is required.")
    private Integer claimQuantity;

    @NotNull(message = "Batch field is required.")
    private Long batchId;

    @NotNull(message = "Product field is required.")
    private Long productId;

    @NotNull(message = "Claim type is required.")
    private String claimTypeId;

    private InvTransactionDetails invTransactionDetails;

}
