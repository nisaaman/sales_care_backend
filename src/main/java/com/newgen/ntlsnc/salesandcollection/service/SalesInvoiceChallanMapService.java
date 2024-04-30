package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoice;
import com.newgen.ntlsnc.salesandcollection.entity.SalesInvoiceChallanMap;
import com.newgen.ntlsnc.salesandcollection.repository.SalesInvoiceChallanMapRepository;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvDeliveryChallan;
import com.newgen.ntlsnc.supplychainmanagement.service.InvDeliveryChallanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kamal
 * @Date ১২/৯/২২
 */

@Service
public class SalesInvoiceChallanMapService implements IService<SalesInvoiceChallanMap> {
    @Autowired
    SalesInvoiceChallanMapRepository salesInvoiceChallanMapRepository;
    @Autowired
    InvDeliveryChallanService invDeliveryChallanService;

    @Override
    public SalesInvoiceChallanMap create(Object object) {
        return null;
    }

    @Override
    public SalesInvoiceChallanMap update(Long id, Object object) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public SalesInvoiceChallanMap findById(Long id) {
        return null;
    }

    @Override
    public List<SalesInvoiceChallanMap> findAll() {
        return null;
    }

    public List<SalesInvoiceChallanMap> findAllBySalesInvoiceAndIsDeletedIsFalseAndIsActiveIsTrue(SalesInvoice salesInvoice) {
        return salesInvoiceChallanMapRepository.findAllBySalesInvoiceAndIsDeletedIsFalseAndIsActiveIsTrue(salesInvoice);
    }

    @Override
    public boolean validate(Object object) {
        return false;
    }


    @Transactional
    public void createMapFromInvoice(List<InvDeliveryChallan> invDeliveryChallanList, SalesInvoice salesInvoice) {
        try {
            invDeliveryChallanService.updateDeliveryChallanListForInvoiceCreate(invDeliveryChallanList);
            List<SalesInvoiceChallanMap> salesInvoiceChallanMapList = new ArrayList<>();
            invDeliveryChallanList.forEach(deliveryChallan -> {
                SalesInvoiceChallanMap salesInvoiceChallanMap = new SalesInvoiceChallanMap();
                salesInvoiceChallanMap.setSalesInvoice(salesInvoice);
                salesInvoiceChallanMap.setInvDeliveryChallan(deliveryChallan);
                salesInvoiceChallanMap.setOrganization(salesInvoice.getOrganization());
                salesInvoiceChallanMapList.add(salesInvoiceChallanMap);
            });

            salesInvoiceChallanMapRepository.saveAll(salesInvoiceChallanMapList);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }
}
