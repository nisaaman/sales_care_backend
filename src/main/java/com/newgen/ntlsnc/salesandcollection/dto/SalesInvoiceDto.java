package com.newgen.ntlsnc.salesandcollection.dto;

import com.newgen.ntlsnc.globalsettings.entity.InvoiceNature;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.TermsAndConditions;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoice;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDeliveryChallan;
import com.newgen.ntlsnc.usermanagement.entity.ApplicationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * @author anika
 * @Date ১৯/৪/২২
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceDto {

    private Long id;

    private String invoiceNo;

    private String invoiceDate;

    @NotNull(message = "Invoice Amount is required")
    private Float invoiceAmount;

    @NotNull(message = "Vat Amount is required")
    private Float vatAmount;

    @NotNull(message = "Discount Amount is required")
    private Float discountAmount;

    @NotNull(message = "Invoice Discount is required")
    private Float invoiceDiscount;

    private String invoiceDiscountType;

    private Float remainingAmount;

    private String remarks;

    @NotNull(message = "Invoice Nature is required")
    private Long invoiceNatureId;

    @NotNull(message = "Company is required")
    private Long companyId;

    private Long termsAndConditionsId;

    @NotNull(message = "Distributor is required")
    private Long distributorId;

    private List<Long> deliveryChallanIds;

    // Invoice Acknowledge
    private Boolean isAccepted = true;
    private Long acceptedById;
    private String acceptanceDate;
    private MultipartFile file;
}
