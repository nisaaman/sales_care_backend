package com.newgen.ntlsnc.salesandcollection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 4th July,22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesReturnProposalDetailsViewDto {

    Map<String, Object> salesReturnProposalLifeCycle;
    List<Map<String, Object>> salesReturnProposalDetails;
    Map<String, Object> salesReturnProposalSummary;
    Map<String, Object> distributorInfo;
}
