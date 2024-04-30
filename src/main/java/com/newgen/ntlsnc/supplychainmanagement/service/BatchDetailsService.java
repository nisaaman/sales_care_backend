package com.newgen.ntlsnc.supplychainmanagement.service;

import com.newgen.ntlsnc.supplychainmanagement.repository.BatchDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Newaz Sharif
 * @since  30th Aug, 22
 */
@Service
public class BatchDetailsService {

    @Autowired
    BatchDetailsRepository batchDetailsRepository;

    public Map<String, Object> getBatchDetails(Long batchId){
        return batchDetailsRepository.getBatchDetails(batchId);
    }

    public boolean isThisProductHasBatch(Long productId) {
        return batchDetailsRepository.existsByProductId(productId);
    }
}
