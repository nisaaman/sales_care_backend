package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.ProductTradePrice;
import com.newgen.ntlsnc.salesandcollection.entity.Proprietor;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBooking;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBookingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author kamal
 * @Date ১১/৪/২২
 */

@Repository
public interface SalesBookingDetailsRepository extends JpaRepository<SalesBookingDetails, Long> {

    List<SalesBookingDetails> findAllBySalesBooking(SalesBooking salesBooking);

    SalesBookingDetails findByIdAndIsDeletedFalse(Long salesBookingDetailsId);

    List<SalesBookingDetails> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    SalesBookingDetails findBySalesBookingIdAndProductIdAndIsDeletedFalse(Long salesBookingId, Long productId);

    List<SalesBookingDetails> findAllByProductTradePriceAndIsActiveTrueAndIsDeletedFalse(ProductTradePrice productTradePrice);

    Optional<SalesBookingDetails> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);
}
