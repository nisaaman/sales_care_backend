package com.newgen.ntlsnc.globalsettings.service;

import com.newgen.ntlsnc.common.enums.*;
import com.newgen.ntlsnc.globalsettings.entity.Vehicle;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author kamal
 * @Date ২৪/৭/২২
 */

@Service
public class EnumeService {

    public List<Map> findAllIntactType() {
        List<Map> list = new ArrayList<>();
        List<IntactType> intactTypes = Arrays.asList(IntactType.values());
        intactTypes.forEach( i ->{
            Map m = new HashMap();
            m.put("code",i.getCode());
            m.put("name",i.getName());
            list.add(m);
        });
        return list;
    }

    public List<Map> findAllApprovalStatus() {
        List<Map> list = new ArrayList<>();
        List<ApprovalStatus> approvalStatusList = Arrays.asList(ApprovalStatus.values());
        approvalStatusList.forEach( i ->{
            Map m = new HashMap();
            m.put("code",i.getCode());
            m.put("name",i.getName());
            list.add(m);
        });
        return list;
    }

    public List<Map> findAllCalculationType() {
        List<Map> list = new ArrayList<>();
        List<CalculationType> calculationTypes = Arrays.asList(CalculationType.values());
        calculationTypes.forEach( i ->{
            Map m = new HashMap();
            m.put("code",i.getCode());
            m.put("name",i.getName());
            list.add(m);
        });
        return list;
    }

    public List<Map> findAllApprovalStatusExceptDraft() {
        List<Map> list = new ArrayList<>();
        List<ApprovalStatus> approvalStatusList = Arrays.asList(ApprovalStatus.values());
        approvalStatusList.forEach( i -> {
            Map m = new HashMap();
            if (!i.getCode().equals("DRAFT") && !i.getCode().equals("PENDING")) {
                m.put("code", i.getCode());
                m.put("name", i.getName());
                list.add(m);
            }
        });
        return list;
    }


    public List<Map> findAllCreditDebitTransactionType() {
        List<Map> list = new ArrayList<>();
        List<CreditDebitTransactionType> creditDebitTransactionTypes = Arrays.asList(CreditDebitTransactionType.values());
        creditDebitTransactionTypes.forEach( i ->{
            Map m = new HashMap();
            m.put("code",i.getCode());
            m.put("name",i.getName());
            list.add(m);
        });
        return list;
    }

    public List<Map> getAllNoteType() {
        List<Map> list = new ArrayList<>();
        List<NoteType> noteTypes = Arrays.asList(NoteType.values());
        noteTypes.forEach( i ->{
            Map m = new HashMap();
            m.put("code",i.getCode());
            m.put("name",i.getName());
            list.add(m);
        });
        return list;
    }

    public List<Map> getAllStoreType() {
        List<Map> list = new ArrayList<>();
        List<StoreType> storeTypes = Arrays.asList(StoreType.values());
        storeTypes.forEach( i ->{
            Map m = new HashMap();
            m.put("code",i.getCode());
            m.put("name",i.getName());
            list.add(m);
        });
        return list;
    }

    public List<Map> getAllApprovalFeature() {
        List<Map> list = new ArrayList<>();
        List<ApprovalFeature> approvalFeatures = Arrays.asList(ApprovalFeature.values());
        approvalFeatures.forEach( i ->{
            Map m = new HashMap();
            m.put("code",i.getCode());
            m.put("name",i.getName());
            list.add(m);
        });
        return list;
    }

    public List<Map> getAllApprovalActor() {
        List<Map> list = new ArrayList<>();
        List<ApprovalActor> approvalActors = Arrays.asList(ApprovalActor.values());
        approvalActors.forEach( i ->{
            Map m = new HashMap();
            m.put("code",i.getCode());
            m.put("name",i.getName());
            list.add(m);
        });
        return list;
    }

    public List<Map> getAllTypeVehicles() {
        List<Map> list = new ArrayList<>();
        List<VehicleType> vehicleTypes =Arrays.asList(VehicleType.values());
        vehicleTypes.forEach( i -> {
            Map m = new HashMap();
            m.put("code", i.getCode());
            m.put("name", i.getName());
            list.add(m);
        });
        return list;
    }

    public List<Map> findStoreTypeSelected() {
        List<Map> list = new ArrayList<>();
        List<StoreType> storeTypeList = Arrays.asList(StoreType.values());
        storeTypeList.forEach( i -> {
            Map m = new HashMap();
            if (i.getCode().equals("QUARANTINE") || i.getCode().equals("REGULAR")) {
                m.put("code", i.getCode());
                m.put("name", i.getName());
                list.add(m);
            }
        });
        return list;
    }

    public List<Map<String, String>> getAllActivityFeature() {
        return ActivityFeature.getAll();
    }

    public List<Map> getAllClaimType() {
        List<Map> list = new ArrayList<>();
        List<InvReturnType> invReturnTypes =Arrays.asList(InvReturnType.values());
        invReturnTypes.forEach( i -> {
            Map m = new HashMap();
            m.put("code", i.getCode());
            m.put("name", i.getName());
            list.add(m);
        });
        return list;
    }

    public List<Map> getAllVehicleOwnership() {
        List<Map> list = new ArrayList<>();
        List<VehicleOwnership> vehicleOwnerships =Arrays.asList(VehicleOwnership.values());
        vehicleOwnerships.forEach( i -> {
            Map m = new HashMap();
            m.put("code", i.getCode());
            m.put("name", i.getName());
            list.add(m);
        });
        return list;
    }

}
