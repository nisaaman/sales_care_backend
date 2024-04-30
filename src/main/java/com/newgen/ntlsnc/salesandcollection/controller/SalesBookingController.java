package com.newgen.ntlsnc.salesandcollection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newgen.ntlsnc.common.ApiResponse;
import com.newgen.ntlsnc.common.enums.ApprovalStatus;
import com.newgen.ntlsnc.salesandcollection.dto.SalesBookingDto;
import com.newgen.ntlsnc.salesandcollection.entity.SalesBooking;
import com.newgen.ntlsnc.salesandcollection.service.SalesBookingDetailsService;
import com.newgen.ntlsnc.salesandcollection.service.SalesBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.newgen.ntlsnc.common.CommonConstant.*;

/**
 * @author kamal
 * @Date ১১/৪/২২
 */

@RestController
@RequestMapping("/api/sales-booking")
public class SalesBookingController {
    private static final String SCOPE = "Sales Booking";

    @Autowired
    SalesBookingService salesBookingService;
    @Autowired
    SalesBookingDetailsService salesBookingDetailsService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SalesBookingDto salesBookingDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesBookingService.create(salesBookingDto));
            response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody SalesBookingDto salesBookingDto) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesBookingService.update(salesBookingDto.getId(), salesBookingDto));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesBookingService.delete(id));
            response.setMessage(SCOPE + DELETE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesBookingService.findById(id));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesBookingService.findAll());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-all-sales-booking-list")
    public ResponseEntity<?> getAllSalesBookingList() {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesBookingService.findAllSalesBookingList());
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/filtered-list")
    public ResponseEntity<?> getSalesBookingFilteredList(@RequestParam Map<String, Object> searchParams) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesBookingService.getSalesBookingFilteredList(searchParams));

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/get-booking-details/{bookingId}")
    public ResponseEntity<?> getSalesBookingDetails(@PathVariable Long bookingId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    salesBookingService.getSalesBookingDetails(bookingId));

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-booking-life-cycle-status/{companyId}/{bookingId}")
    public ResponseEntity<?> getBookingLifeCycleStatus(@PathVariable Long companyId,
                                                       @PathVariable Long bookingId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    salesBookingService.getBookingLifeCycleStatus(companyId, bookingId));

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/save-booking-as-drafted")
    public ResponseEntity<?> BookingAsDraft(@RequestBody SalesBookingDto salesBookingDto) {
        ApiResponse response = new ApiResponse(false);
        try {
            SalesBooking salesBooking = salesBookingService.saveAsDraft(salesBookingDto);

            if(salesBooking!= null) {
                if(ApprovalStatus.DRAFT.equals(salesBooking.getApprovalStatus())) {
                    response.setMessage(ADDTOCART_SUCCESS_MESSAGE);
                }
                else {
                    response.setMessage(SCOPE + CREATE_SUCCESS_MESSAGE);
                    /*push notification*/
                    salesBookingService.sendPushNotificationToBookingNextApprover(salesBooking.getCompany().getId(),
                            salesBookingDto.getId(), Integer.valueOf(0));
                }
                response.setSuccess(salesBooking);



            } else {
                response.setMessage(SCOPE + CREATE_ERROR_MESSAGE+ " (Please check Booking Nature)");
            }

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-drafted-sales-booking-by-sales-booking-id/{salesBookingId}")
    public ResponseEntity<?> getDraftedSalesBookingBySalesBookingId(@PathVariable Long salesBookingId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesBookingService.findSalesBookingListForDraftedView(salesBookingId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete-sales-booking-details/{salesBookingDetailsId}")
    public ResponseEntity<?> deleteSalesBookingDetails(@PathVariable Long salesBookingDetailsId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesBookingService.deleteSalesBookingDetails(salesBookingDetailsId));
            response.setMessage("Sales Booking details" + DELETE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-sales-booking-list/{distributorId}/{companyId}")
    public ResponseEntity<?> getSalesBookingListForPaymentCollection(@PathVariable Long distributorId,@PathVariable Long companyId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesBookingService.findSalesBookingListForPaymentCollection(distributorId,companyId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-booking-to-stock-confirm/{bookingId}")
    public ResponseEntity<?> getSalesBookingToStockConfirm(@PathVariable Long bookingId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    salesBookingService.getSalesBookingToStockConfirm(bookingId));

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update-sales-booking")
    public ResponseEntity<?> updateSalesBooking(@RequestBody Map
                                                        salesBooking) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesBookingService.updateSalesBooking(
                    salesBooking));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update-sales-booking-item")
    public ResponseEntity<?> updateBookingItem(@RequestBody List<Map>
                                                           salesBookingItemList) {
        ApiResponse response = new ApiResponse(false);

        try {
            response.setSuccess(salesBookingService.updateSalesBookingItem(
                    salesBookingItemList));
            response.setMessage(SCOPE + UPDATE_SUCCESS_MESSAGE);
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-confirmed-booking-item-list/{bookingId}")
    public ResponseEntity<?> getConfirmedBookingItemList(@PathVariable Long bookingId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(
                    salesBookingService.getConfirmedBookingItemList(bookingId));

        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-pending-booking-list")
    public ResponseEntity<?> getPendingBookingList(@RequestParam Long companyId, @RequestParam Long distributorId, @RequestParam Long salesOfficerId) {
        ApiResponse response = new ApiResponse(false);
        try {
            response.setSuccess(salesBookingService.getPendingBookingListByCompanyAndDistributorAndSalesOfficer(companyId, distributorId, salesOfficerId));
        } catch (Exception ex) {
            response.setError(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/get-booking-activities/{booking_id}")
    public ResponseEntity<?> getSalesBookingActivities(
            @PathVariable Long booking_id) {

        ApiResponse response = new ApiResponse(false);
        response.setSuccess(  salesBookingService.getBookingActivitiesList(booking_id));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
