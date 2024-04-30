package com.newgen.ntlsnc.salesandcollection.service;


import com.newgen.ntlsnc.globalsettings.entity.ProductTradePrice;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBooking;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBookingDetails;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBookingDetailsRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class SalesBookingDetailsService {

    @Autowired
    SalesBookingRepository salesBookingRepository;
    @Autowired
    SalesBookingDetailsRepository salesBookingDetailsRepository;

    public List <SalesBookingDetails> findSalesBookingProductsByBookingId(Long bookingId) {
        try {
            List<SalesBookingDetails> salesBookingProductList =
                    salesBookingRepository.findSalesBookingProducts(bookingId);

            return salesBookingProductList;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public boolean delete(Long id) {
        SalesBookingDetails salesBookingDetails = this.findById(id);
        salesBookingDetailsRepository.delete(salesBookingDetails);
        return true;
    }

    public SalesBookingDetails findById(Long id) {
        try {
            Optional<SalesBookingDetails> salesBookingDetailsOptional = salesBookingDetailsRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!salesBookingDetailsOptional.isPresent()) {
                throw new Exception("Sales Booking Details not exist");
            }
            return salesBookingDetailsOptional.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<SalesBookingDetails> getAllByProductTradePrice(ProductTradePrice productTradePrice) {
        try {
            List<SalesBookingDetails> all = salesBookingDetailsRepository.findAllByProductTradePriceAndIsActiveTrueAndIsDeletedFalse(productTradePrice);
            return all;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


}
