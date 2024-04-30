package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.common.enums.SalesBookingStatus;
import com.newgen.ntlsnc.common.enums.TicketStatus;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.entity.Product;
import com.newgen.ntlsnc.globalsettings.entity.ProductTradePrice;
import com.newgen.ntlsnc.globalsettings.service.LocationService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.globalsettings.service.ProductService;
import com.newgen.ntlsnc.salesandcollection.dto.MaterialReceivePlanDto;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBookingDetailsDto;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBookingDto;
import com.newgen.ntlsnc.salesandcollection.entity.MaterialReceivePlan;
import com.newgen.ntlsnc.salesandcollection.entity.MaterialReceivePlanHistory;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBooking;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBookingDetails;
import com.newgen.ntlsnc.salesandcollection.repository.MaterialReceivePlanHistoryRepository;
import com.newgen.ntlsnc.salesandcollection.repository.MaterialReceivePlanRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBookingDetailsRepository;
import com.newgen.ntlsnc.salesandcollection.repository.SalesBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sagor
 * @date ১২/৪/২২
 */
@Service
public class MaterialReceivePlanService implements IService<MaterialReceivePlan> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    MaterialReceivePlanRepository materialReceivePlanRepository;
    @Autowired
    ProductService productService;
    @Autowired
    SalesBookingService salesBookingService;
    @Autowired
    SalesBookingDetailsService salesBookingDetailsService;
    @Autowired
    SalesBookingRepository salesBookingRepository;
    @Autowired
    SalesBookingDetailsRepository salesBookingDetailsRepository;
    @Autowired
    MaterialReceivePlanHistoryRepository materialReceivePlanHistoryRepository;
    @Autowired
    LocationService locationService;

    @Override
    @Transactional
    public MaterialReceivePlan create(Object object) {
        MaterialReceivePlanDto materialReceivePlanDto = (MaterialReceivePlanDto) object;
        MaterialReceivePlan materialReceivePlan = new MaterialReceivePlan();

        if (materialReceivePlanDto.getCompanyId() != null) {
            Organization company = organizationService.findById(materialReceivePlanDto.getCompanyId());
            materialReceivePlan.setCompany(company);
        }
        Long salesBookingDetailsId = materialReceivePlanDto.getSalesBookingDetailsId();

        if (materialReceivePlanDto.getSalesBookingDetailsId() != null) {
            materialReceivePlan.setSalesBookingDetails(
                    salesBookingService.findSalesBookingDetailsBySalesBookingDetailsId(
                            materialReceivePlanDto.getSalesBookingDetailsId()));
        }

        if (materialReceivePlanDto.getQuantity() != null) {
            materialReceivePlan.setQuantity(materialReceivePlanDto.getQuantity());
        }

        if (null == materialReceivePlanDto.getTicketDate()) {
            materialReceivePlan.setTicketDate( LocalDate.now());
        } else
            materialReceivePlan.setTicketDate(LocalDate.parse(materialReceivePlanDto.getTicketDate()));

//        materialReceivePlan.setRequireDate(
//                salesBookingService.findById(salesBookingDetailsId).getTentativeDeliveryDate());

        materialReceivePlan.setTicketStatus(TicketStatus.valueOf(materialReceivePlanDto.getTicketStatus()));

        if (null == materialReceivePlanDto.getTicketStatusDate()) {
            materialReceivePlan.setTicketStatusDate( LocalDate.now());
        } else
            materialReceivePlan.setTicketStatusDate(
                    LocalDate.parse(materialReceivePlanDto.getTicketStatusDate()));

        if (null != materialReceivePlanDto.getCommitmentDate())
            materialReceivePlan.setCommitmentDate(
                    LocalDate.parse(materialReceivePlanDto.getCommitmentDate()));

        materialReceivePlan.setNotes(materialReceivePlanDto.getNotes());

        materialReceivePlan.setOrganization(organizationService.getOrganizationFromLoginUser());

        //if (materialReceivePlanDto.getProductId() != null) {
            //materialReceivePlan.setProduct(productService.findById(materialReceivePlanDto.getProductId()));
       // }

        if (!this.validate(materialReceivePlan)) {
            return null;
        }
        materialReceivePlan = materialReceivePlanRepository.save(materialReceivePlan);

        /** MaterialReceivePlanHistory*/
        MaterialReceivePlanHistory materialReceivePlanHistory =
                new MaterialReceivePlanHistory();
        materialReceivePlanHistory.setTicketStatus(
                TicketStatus.valueOf(materialReceivePlanDto.getTicketStatus()));

        if (null == materialReceivePlanDto.getTicketStatusDate()) {
            materialReceivePlanHistory.setTicketStatusDate(LocalDate.now());
        }
        else
            materialReceivePlanHistory.setTicketStatusDate(
                    LocalDate.parse(materialReceivePlanDto.getTicketStatusDate()));

        materialReceivePlanHistory.setNotes(materialReceivePlanDto.getNotes());

        materialReceivePlanHistory.setMaterialReceivePlan(materialReceivePlan);

        materialReceivePlanHistory.setOrganization(organizationService.getOrganizationFromLoginUser());
        materialReceivePlanHistoryRepository.save(materialReceivePlanHistory);

        /**SalesBookingDetails*/
        SalesBookingDetails salesBookingDetails =
                salesBookingDetailsService.findById(salesBookingDetailsId);
        salesBookingDetails.setSalesBookingStatus(SalesBookingStatus.TICKET_REQUESTED);
        salesBookingDetails.setOrganization(organizationService.getOrganizationFromLoginUser());
        salesBookingDetailsRepository.save(salesBookingDetails);

        return materialReceivePlan;
    }

    @Override
    @Transactional
    public MaterialReceivePlan update(Long id, Object object) {
        MaterialReceivePlanDto materialReceivePlanDto = (MaterialReceivePlanDto) object;
        MaterialReceivePlan materialReceivePlan = new MaterialReceivePlan();
        //this.findById(id);
        Long salesBookingDetailsId = materialReceivePlanDto.getSalesBookingDetailsId();

        Map<String, Object> ticketMap = getTicket(salesBookingDetailsId);
        Map<String, Object> ticketDetails = (Map<String, Object>) ticketMap.get("ticketDetails");
        materialReceivePlan.setId(Long.parseLong(ticketDetails.get("id").toString()));
        materialReceivePlan.setQuantity(Float.parseFloat(ticketDetails.get("quantity").toString()));

        if (materialReceivePlanDto.getCompanyId() != null) {
            Organization company = organizationService.findById(materialReceivePlanDto.getCompanyId());
            materialReceivePlan.setCompany(company);
        } else {
            String companyId = ticketDetails.get("company_id").toString();
            Organization company = organizationService.findById(Long.parseLong((companyId)));
            materialReceivePlan.setCompany(company);
        }
        //materialReceivePlan.setTicketNo(materialReceivePlanDto.getTicketNo());
        if (null != materialReceivePlanDto.getTicketDate()) {
            materialReceivePlan.setTicketDate(LocalDate.parse(materialReceivePlanDto.getTicketDate()));
        } else {
            String date = ticketDetails.get("ticket_date_without_format").toString();
            materialReceivePlan.setTicketDate(LocalDate.parse(date));
        }

        if (null != materialReceivePlanDto.getRequireDate())
            materialReceivePlan.setRequireDate(
                    LocalDate.parse(materialReceivePlanDto.getRequireDate()));

        if (null != materialReceivePlanDto.getTicketStatus())
            materialReceivePlan.setTicketStatus(
                    TicketStatus.valueOf(materialReceivePlanDto.getTicketStatus()));

        if (null != materialReceivePlanDto.getTicketStatusDate())
        {
            materialReceivePlan.setTicketStatusDate(LocalDate.parse(materialReceivePlanDto.getTicketStatusDate()));
        }else{
            String date = ticketDetails.get("ticket_status_date_without_format").toString();
            materialReceivePlan.setTicketStatusDate(LocalDate.parse(date));
        }
        if (null != materialReceivePlanDto.getCommitmentDate())
            materialReceivePlan.setCommitmentDate(
                    LocalDate.parse(materialReceivePlanDto.getCommitmentDate()));

        materialReceivePlan.setNotes(materialReceivePlanDto.getNotes());
        if (materialReceivePlanDto.getConfirmQuantity() != null) {
            materialReceivePlan.setConfirmQuantity(materialReceivePlanDto.getConfirmQuantity());
        }
        materialReceivePlan.setOrganization(organizationService.getOrganizationFromLoginUser());

        if (materialReceivePlanDto.getSalesBookingDetailsId() != null) {
            SalesBookingDetails salesBookingDetails =
                    salesBookingService.findSalesBookingDetailsBySalesBookingDetailsId(
                            materialReceivePlanDto.getSalesBookingDetailsId());
            materialReceivePlan.setSalesBookingDetails(salesBookingDetails);
        }

        if (!this.validate(materialReceivePlan)) {
            return null;
        }
        materialReceivePlan = materialReceivePlanRepository.save(materialReceivePlan);
        MaterialReceivePlanHistory materialReceivePlanHistory = new MaterialReceivePlanHistory();
        materialReceivePlanHistory.setTicketStatus(
                TicketStatus.valueOf(materialReceivePlanDto.getTicketStatus()));

        if (null != materialReceivePlanDto.getTicketStatusDate()) {
            materialReceivePlanHistory.setTicketStatusDate(
                    LocalDate.parse(materialReceivePlanDto.getTicketStatusDate()));
        }else{
            String date = ticketDetails.get("ticket_status_date_without_format").toString();
            materialReceivePlanHistory.setTicketStatusDate(LocalDate.parse(date));
        }

        materialReceivePlanHistory.setNotes(materialReceivePlanDto.getNotes());

        materialReceivePlanHistory.setMaterialReceivePlan(materialReceivePlan);
        materialReceivePlanHistory.setOrganization(organizationService.getOrganizationFromLoginUser());
        materialReceivePlanHistoryRepository.save(materialReceivePlanHistory);

        /**SalesBookingDetails*/
        SalesBookingDetails salesBookingDetails =
                salesBookingDetailsService.findById(salesBookingDetailsId);
        salesBookingDetails.setSalesBookingStatus(
                SalesBookingStatus.valueOf(materialReceivePlanDto.getItemStatus()));
        salesBookingDetails.setOrganization(organizationService.getOrganizationFromLoginUser());
        salesBookingDetailsRepository.save(salesBookingDetails);

        return materialReceivePlan;
    }

    @Override
    public boolean delete(Long id) {
        try {
            MaterialReceivePlan materialReceivePlan = findById(id);
            materialReceivePlan.setIsDeleted(true);
            materialReceivePlanRepository.save(materialReceivePlan);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public MaterialReceivePlan findById(Long id) {
        try {
            Optional<MaterialReceivePlan> optionalMaterialReceivePlan = materialReceivePlanRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalMaterialReceivePlan.isPresent()) {
                throw new Exception("Material Receive Plan Not exist with id " + id);
            }
            return optionalMaterialReceivePlan.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<MaterialReceivePlan> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return materialReceivePlanRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public Map<String, Object> getTicket(Long bookingItemId) {
        Map<String, Object> returnMap = new HashMap<>();
        Map<String, Object> ticketDetails;

        if (bookingItemId == null) {
            return null;
        }
        ticketDetails = materialReceivePlanRepository.getTicket(bookingItemId);
        returnMap.put("ticketDetails", ticketDetails);

        return returnMap;
    }

    public List<Map<String, Object>> getTicketCompanyWise(Long companyId,Long depotId, Long semesterId) {
        try {
            return materialReceivePlanRepository.findTicketCompanyWise(companyId, depotId, semesterId);
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Map<String, Object>> getMaterialPlannerTicket(
            Long companyId, List<Long> locationIds, List<Long> categoryIds, List<Long> depotIds, LocalDate startDate,
            LocalDate endDate) {

        Map<Long, Object> childLocationMap = new HashMap<>();
        try {
           /* if (locationId != null) {
                childLocationMap =
                        locationService.getChildLocationsByParent(
                                companyId, locationId, childLocationMap);
            }
            List<Long> locationIdList = new ArrayList(childLocationMap.keySet());*/
            return materialReceivePlanRepository.getMaterialPlannerTicket(
                    companyId, locationIds, categoryIds, depotIds, startDate, endDate);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
