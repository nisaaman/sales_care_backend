package com.newgen.ntlsnc.salesandcollection.repository;

import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.VatSetup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author anika
 * @Date ২৪/৪/২২
 */
@Repository
public interface VatSetupRepository extends JpaRepository<VatSetup, Long> {
    List<VatSetup> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<VatSetup> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query(value = "select a.id\n" +
            "from VatSetup a\n" +
            "where a.isDeleted is false\n" +
            "  and a.isActive is true\n" +
            "  and a.product.id = :productId \n" +
            "  and (:startDate between a.fromDate and a.toDate or :endDate between a.fromDate and a.toDate\n" +
            "    or (:startDate <= a.fromDate and :endDate >= a.toDate))")
    List<Map> getExistListByProductAndStartDateOrEndDate(@Param("productId") Long productId,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    @Query(value = "select v.id                                 id\n" +
            "     , round(v.vat, 2)                      vat\n" +
            "     , v.vat_included                       vat_included\n" +
            "     , DATE_FORMAT(v.from_date, '%d %b %Y') from_date\n" +
            "     , DATE_FORMAT(v.to_date, '%d %b %Y')   to_date\n" +
            "     , case\n" +
            "           when now() > v.to_date then 'EXPIRED'\n" +
            "           when now() between v.from_date and to_date then 'CURRENT'\n" +
            "           when now() < v.from_date then 'NOT_START_YET'\n" +
            "    end                                     status\n" +
            "from vat_setup v\n" +
            "where v.product_id = :productId \n" +
            "    and v.vat_included = :vatIncluded \n" +
            "  and v.is_deleted is false\n" +
            "  and v.is_active is true\n" +
            "order by v.from_date desc, v.to_date desc", nativeQuery = true)
    List<Map> getAllByProductAndVatIncluded(@Param("productId") Long productId, @Param("vatIncluded") Boolean vatIncluded);

    @Query(value = "select * \n" +
            "from vat_setup v \n" +
            "where v.product_id = :productId \n" +
            "  and v.is_deleted is false\n" +
            "  and v.is_active is true\n" +
            "  and now() between v.from_date and to_date", nativeQuery = true)
    Map getCurrentVatInfoByProductId(Long productId);
}
