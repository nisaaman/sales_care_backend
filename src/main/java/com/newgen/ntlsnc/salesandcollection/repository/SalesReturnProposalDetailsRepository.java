package com.newgen.ntlsnc.salesandcollection.repository;


import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposal;
import com.newgen.ntlsnc.salesandcollection.entity.SalesReturnProposalDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author marziah
 * @Date 20/04/22
 */

@Repository
public interface SalesReturnProposalDetailsRepository extends JpaRepository<SalesReturnProposalDetails, Long> {

    List<SalesReturnProposalDetails> findAllBySalesReturnProposal(SalesReturnProposal salesReturnProposal);

    List<SalesReturnProposalDetails> findAllBySalesReturnProposalIdAndIsActiveTrueAndIsDeletedFalse(Long salesReturnProposalId);

    List<SalesReturnProposalDetails> findAllByOrganizationAndIsDeletedFalse(Organization organization);

    Optional<SalesReturnProposalDetails> findBySalesReturnProposalIdAndProductIdAndBatchIdAndIsDeletedFalse(Long salesReturnProposalId, Long product, Long batch);

    List<SalesReturnProposalDetails> findAllBySalesReturnProposalIdAndIsActiveFalse(Long salesReturnProposalId);

    List<SalesReturnProposalDetails> findAllBySalesReturnProposalIdAndProductIdAndIsDeletedFalse(Long salesReturnProposalId, Long productId);
}
