package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.globalsettings.repository.OverridingDiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Newaz Sharif
 * @since 4th August,22
 */
@Service
public class OverridingDiscountService {

    @Autowired
    OverridingDiscountRepository overridingDiscountRepository;

    public List<Map<String,Object>> getInvoiceWiseOverridingDiscountList(
            Long companyId, Long salesOfficerId){

        return overridingDiscountRepository.getInvoiceWiseAvailORDList(companyId,salesOfficerId);
    }

    public List<Map<String,Object>> getOverridingDiscountDetailsOfASalesInvoice(
            Long companyId, Long salesInvoiceId){

        return overridingDiscountRepository.getORDDetailsOfASalesInvoice(companyId,salesInvoiceId);
    }
}
