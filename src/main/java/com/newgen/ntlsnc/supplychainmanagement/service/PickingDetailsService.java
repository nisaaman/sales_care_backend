package com.newgen.ntlsnc.supplychainmanagement.service;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.globalsettings.dto.BankAccountDto;
import com.newgen.ntlsnc.globalsettings.entity.AccountingYear;
import com.newgen.ntlsnc.globalsettings.entity.BankAccount;
import com.newgen.ntlsnc.salesandcollection.entity.SalesOrderDetails;
import com.newgen.ntlsnc.salesandcollection.repository.SalesOrderDetailsRepository;
import com.newgen.ntlsnc.supplychainmanagement.dto.InvTransactionDetailsDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.PickingDetailsListDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.PickingDetailsSingletDto;
import com.newgen.ntlsnc.supplychainmanagement.dto.PickingDto;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransaction;
import com.newgen.ntlsnc.supplychainmanagement.entity.InvTransactionDetails;
import com.newgen.ntlsnc.supplychainmanagement.entity.Picking;
import com.newgen.ntlsnc.supplychainmanagement.entity.PickingDetails;
import com.newgen.ntlsnc.supplychainmanagement.repository.PickingDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PickingDetailsService  implements IService<PickingDetails> {
    @Autowired
    PickingService pickingService;
    @Autowired
    InvDeliveryChallanService invDeliveryChallanService;
    @Autowired
    PickingDetailsRepository pickingDetailsRepository;
    @Autowired
    SalesOrderDetailsRepository salesOrderDetailsRepository;


    public void updatePickingDetails(InvTransactionDetails invTransactionDetails, Long salesOrderDetailsId, Long pickingId) {
        try {
            if (pickingId != null) {
                PickingDetails pickingDetails = pickingDetailsRepository.getPickingDetailsByPickingIdAndSalesOrderDetailsId(salesOrderDetailsId, pickingId);
                //pickingDetails.setInvTransactionDetails(invTransactionDetails);
                pickingDetailsRepository.save(pickingDetails);
            }
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map> getProductListByPickingWise(Long pickingId) {
        return  pickingDetailsRepository.getProductListByPickingId(pickingId);
    }

    public String confirmPicking(PickingDetailsListDto pickingDetailsListDto) {
        try{
            if(pickingDetailsListDto.getPickingId() != null) {
                for (PickingDetailsSingletDto pickingDetailsSingletDto :
                        pickingDetailsListDto.getPickingDetailsSingletDtoList()) {
                    Optional<PickingDetails> optionalPickingDetails =
                            pickingDetailsRepository.findById(pickingDetailsSingletDto.getPickingDetailsId());
                    PickingDetails pickingDetails = optionalPickingDetails.get();

                    String pickingStatus =
                            pickingService.getPickedStatus(pickingDetailsListDto.getPickingId());
                    if ("CONFIRMED".equals(pickingStatus)) {
                        return "Picking has been " + pickingDetailsListDto.getStatus() +" Confirmed previously";
                    }

                    if (pickingDetailsSingletDto.getGoodQty() != null) {
                        pickingDetails.setGoodQty(pickingDetailsSingletDto.getGoodQty());
                    }
                    if (pickingDetailsSingletDto.getBadQty() != null) {
                        pickingDetails.setBadQty(pickingDetailsSingletDto.getBadQty());
                    }
                    if (pickingDetailsSingletDto.getReason() != "") {
                        pickingDetails.setReason(pickingDetailsSingletDto.getReason());
                    }
                    pickingDetailsRepository.save(pickingDetails);

                }
                pickingService.setConfirmPickingStatus(pickingDetailsListDto);
            }
            return "Picking has been " + pickingDetailsListDto.getStatus() +" Successfully";
        }catch (Exception e) {
           return  "Unable to"+ pickingDetailsListDto.getStatus();
        }
    }

    @Override
    public PickingDetails create(Object object) {
        return null;
    }

    @Override
    public PickingDetails update(Long id, Object object) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public PickingDetails findById(Long id) {
        try {
            Optional<PickingDetails> optionalPickingDetails =
                    pickingDetailsRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalPickingDetails.isPresent()) {
                throw new Exception("Picking Details Not exist with id " + id);
            }
            return optionalPickingDetails.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<PickingDetails> findAll() {
        return null;
    }

    @Override
    public boolean validate(Object object) {
        return false;
    }
}
