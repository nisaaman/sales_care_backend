package com.newgen.ntlsnc.supplychainmanagement.dto;

import com.newgen.ntlsnc.salesandcollection.dto.SalesReturnProposalDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author marziah
 * @Date 25/04/22
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReturnDto {

    private String returnNote;

    @NotNull(message = "Sales Return Proposal field is required.")
    private Long salesReturnProposalId;

    private Float salesReturnProposalTotalAmount;

    private List<SalesReturnDetailsDto> salesReturnDetailsDtoList;
}
