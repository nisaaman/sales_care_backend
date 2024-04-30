package com.newgen.ntlsnc.salesandcollection.dto.paymentAdjustment;

import com.newgen.ntlsnc.salesandcollection.entity.PaymentCollection;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author liton
 * Created on 9/14/22 1:45 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdjustPaymentCollectionDto {
    @NotNull(message = "Adjusted Amount field is required.")
    private Double adjustedAmount;

    private PaymentCollectionAdjustedDto adjustedPayment;

    private List<SalesInvoiceAdjustedDto> adjustedInvoiceList;

    @NotNull(message = "Company field is required.")
    private Long companyId;
}
