package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.FileType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.DocumentService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.salesandcollection.dto.DistributorGuarantorDto;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.DistributorGuarantor;
import com.newgen.ntlsnc.salesandcollection.repository.DistributorGuarantorRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author marziah
 * Created on 5/4/22 10:29 AM
 */
@Service
public class DistributorGuarantorService implements IService<DistributorGuarantor> {
    @Autowired
    DistributorGuarantorRepository distributorGuarantorRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    DocumentService documentService;
    @Autowired
    FileDownloadService fileDownloadService;

    private static final String GUARANTOR_LOGO_UPLOAD_DIRECTORY = "guarantor";
    private static final String GUARANTOR_LOGO_UPLOAD_PREFIX = "guarantor_";

    private static final String UNDERSCORE = "_";

    @Override
    @Transactional
    public DistributorGuarantor create(Object object) {
        DistributorGuarantorDto distributorGuarantorDto = (DistributorGuarantorDto) object;
        DistributorGuarantor distributorGuarantor = new DistributorGuarantor();

        //distributorGuarantor.setOrganization(organizationService.getOrganizationFromLoginUser());
        // Distributor distributorId = distributorService.findById(distributorGuarantorDto.getDistributorId());
        distributorGuarantor.setName(distributorGuarantorDto.getName());
        distributorGuarantor.setNid(distributorGuarantorDto.getNid());
        distributorGuarantor.setFatherName(distributorGuarantorDto.getFatherName());
        distributorGuarantor.setMotherName(distributorGuarantorDto.getMotherName());
        distributorGuarantor.setContactNo(distributorGuarantorDto.getContactNo());
        distributorGuarantor.setAddress(distributorGuarantorDto.getAddress());
        //distributorGuarantor.setDistributor(distributorId);
        if (!this.validate(distributorGuarantor)) {
            return null;
        }

        Organization organization = organizationService.getOrganizationFromLoginUser();
        distributorGuarantor.setOrganization(organization);
        distributorGuarantorRepository.save(distributorGuarantor);

        return distributorGuarantorRepository.save(distributorGuarantor);
    }

    @Override
    @Transactional
    public DistributorGuarantor update(Long id, Object object) {
        DistributorGuarantorDto distributorGuarantorDto = (DistributorGuarantorDto) object;
        DistributorGuarantor distributorGuarantor = this.findById(distributorGuarantorDto.getId());
        distributorGuarantor.setOrganization(organizationService.getOrganizationFromLoginUser());
        // Distributor distributorId = distributorService.findById(distributorGuarantorDto.getDistributorId());
        distributorGuarantor.setName(distributorGuarantorDto.getName());
        distributorGuarantor.setNid(distributorGuarantorDto.getNid());
        distributorGuarantor.setFatherName(distributorGuarantorDto.getFatherName());
        distributorGuarantor.setMotherName(distributorGuarantorDto.getMotherName());
        distributorGuarantor.setContactNo(distributorGuarantorDto.getContactNo());
        distributorGuarantor.setAddress(distributorGuarantorDto.getAddress());
        //distributorGuarantor.setDistributor(distributorId);
        if (!this.validate(distributorGuarantor)) {
            return null;
        }
        return distributorGuarantorRepository.save(distributorGuarantor);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            DistributorGuarantor distributorGuarantor = findById(id);
            distributorGuarantor.setIsDeleted(true);
            distributorGuarantorRepository.save(distributorGuarantor);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public DistributorGuarantor findById(Long id) {
        try {
            Optional<DistributorGuarantor> optionalDistributorGuarantor = distributorGuarantorRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalDistributorGuarantor.isPresent()) {
                throw new Exception("Distributor Guarantor Not exist with id " + id);
            }
            return optionalDistributorGuarantor.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<DistributorGuarantor> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return distributorGuarantorRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    public boolean saveAll(List<DistributorGuarantorDto> distributorGuarantorDtoList, Distributor distributor, MultipartFile[] fileList) {
        List<DistributorGuarantor> distributorGuarantorUpdatedList = new ArrayList<>();
        try {
            for (DistributorGuarantorDto dgt : distributorGuarantorDtoList) {
                DistributorGuarantor distributorGuarantor = new DistributorGuarantor();
                if (dgt.getId() != null) {
                    distributorGuarantor = this.findById(dgt.getId());
                }
                distributorGuarantor.setName(dgt.getName().trim());
                distributorGuarantor.setNid(dgt.getNid().trim());
                distributorGuarantor.setContactNo(dgt.getContactNo());

                if (dgt.getFatherName() != null) {
                    distributorGuarantor.setFatherName(dgt.getFatherName().trim());
                }
                if (dgt.getMotherName() != null) {
                    distributorGuarantor.setMotherName(dgt.getMotherName().trim());
                }
                if (dgt.getAddress() != null) {
                    distributorGuarantor.setAddress(dgt.getAddress().trim());
                }

                distributorGuarantor.setDistributor(distributor);
                Organization organization = organizationService.getOrganizationFromLoginUser();
                distributorGuarantor.setOrganization(organization);
                if (!this.validate(distributorGuarantor)) {
                    return false;
                }
                // distributorGuarantorList.add(distributorGuarantor);
                distributorGuarantorRepository.save(distributorGuarantor);
                if (fileList != null && fileList.length > 0) {
                    for (MultipartFile file : fileList) {
                        if (dgt.getFileName().equalsIgnoreCase(file.getOriginalFilename())) {
                            String tempFileOriginName= file.getOriginalFilename().split("\\|")[1];
                            String filePath = fileUploadService.fileUpload(file, FileType.LOGO.getCode(),
                                    GUARANTOR_LOGO_UPLOAD_DIRECTORY,
                                    organization.getId(), organization.getId());

//                            documentService.save(GUARANTOR_LOGO_UPLOAD_DIRECTORY, filePath, distributorGuarantor.getId(),
//                                    fileUploadService.getFileNameFromFilePath(filePath), FileType.LOGO.getCode()
//                                    , organization, organization.getId());
                            documentService.save(GUARANTOR_LOGO_UPLOAD_DIRECTORY, filePath, distributorGuarantor.getId(),
                                    tempFileOriginName, FileType.LOGO.getCode()
                                    , organization, organization.getId(), file.getSize());
                        }
                    }
                }
                distributorGuarantorUpdatedList.add(distributorGuarantor);
            }
            List<DistributorGuarantor> guarantorListByDistributor = distributorGuarantorRepository.findAllByDistributorAndIsActiveTrueAndIsDeletedFalse(distributor);
            // Deleting guarantor
            List<DistributorGuarantor> guarantorDeleteList = guarantorListByDistributor.stream().filter(it -> !distributorGuarantorUpdatedList.contains(it)).collect(Collectors.toList());

            if (guarantorDeleteList.size() > 0) {
                for (DistributorGuarantor dg : guarantorDeleteList) {
                    dg.setIsDeleted(true);
                    distributorGuarantorRepository.save(dg);
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public List<DistributorGuarantorDto> getDistributorInfoWithGuarantorByDistributor(Distributor distributor) {
        List<DistributorGuarantor> distributorGuarantorList = distributorGuarantorRepository.findAllByDistributorAndIsActiveTrueAndIsDeletedFalse(distributor);

        List<DistributorGuarantorDto> distributorGuarantorDtoList = new ArrayList<>();

        for (DistributorGuarantor distributorGuarantor : distributorGuarantorList) {

            DistributorGuarantorDto distributorGuarantorDto = new DistributorGuarantorDto();
            distributorGuarantorDto.setId(distributorGuarantor.getId());
            distributorGuarantorDto.setName(distributorGuarantor.getName());
            distributorGuarantorDto.setAddress(distributorGuarantor.getAddress());
            distributorGuarantorDto.setNid(distributorGuarantor.getNid());
            distributorGuarantorDto.setFatherName(distributorGuarantor.getFatherName());
            distributorGuarantorDto.setMotherName(distributorGuarantor.getMotherName());
            distributorGuarantorDto.setContactNo(distributorGuarantor.getContactNo());
            String filePath = documentService.getDocumentFilePath(GUARANTOR_LOGO_UPLOAD_DIRECTORY, distributorGuarantor.getId(), FileType.LOGO);
            if ((!filePath.equals("")) && filePath != null) {
//                distributorGuarantorDto.setFilePath(filePath);
                String s = new String(Base64.encodeBase64(fileDownloadService.fileDownload(
                        documentService.getFilePath(distributorGuarantor.getId(), GUARANTOR_LOGO_UPLOAD_DIRECTORY))));
                distributorGuarantorDto.setFilePath(s);
            }
            distributorGuarantorDtoList.add(distributorGuarantorDto);
        }
        return distributorGuarantorDtoList;
    }

}
