package com.newgen.ntlsnc.salesandcollection.service;

import com.newgen.ntlsnc.common.FileDownloadService;
import com.newgen.ntlsnc.common.FileUploadService;
import com.newgen.ntlsnc.common.IService;
import com.newgen.ntlsnc.common.enums.FileType;
import com.newgen.ntlsnc.globalsettings.entity.Organization;
import com.newgen.ntlsnc.globalsettings.service.DocumentService;
import com.newgen.ntlsnc.globalsettings.service.OrganizationService;
import com.newgen.ntlsnc.salesandcollection.dto.ProprietorDto;
import com.newgen.ntlsnc.salesandcollection.entity.Distributor;
import com.newgen.ntlsnc.salesandcollection.entity.Proprietor;
import com.newgen.ntlsnc.salesandcollection.repository.ProprietorRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProprietorService implements IService<Proprietor> {
    @Autowired
    OrganizationService organizationService;
    @Autowired
    DistributorService distributorService;
    @Autowired
    ProprietorRepository proprietorRepository;
    @Autowired
    FileUploadService fileUploadService;
    @Autowired
    DocumentService documentService;
    @Autowired
    FileDownloadService fileDownloadService;

    private static final String PROPRIETOR_LOGO_UPLOAD_DIRECTORY = "proprietor";

    private static final String PROPRIETOR_LOGO_UPLOAD_PREFIX = "proprietor_";

    private static final String UNDERSCORE = "_";

    @Override
    @Transactional
    public Proprietor create(Object object) {
        ProprietorDto proprietorDto = (ProprietorDto) object;
        Proprietor proprietor = new Proprietor();
        //proprietor.setOrganization(organizationService.getOrganizationFromLoginUser());
        // Distributor distributor = distributorService.findById(proprietorDto.getDistributorId());
        proprietor.setName(proprietorDto.getName());
        proprietor.setNid(proprietorDto.getNid());
        proprietor.setContactNo(proprietorDto.getContactNo());
        proprietor.setAddress(proprietorDto.getAddress());
        proprietor.setFatherName(proprietorDto.getFatherName());
        proprietor.setMotherName(proprietorDto.getMotherName());

//        proprietor.setDistributor(distributor);
//        if (!this.validate(distributor)) {
//            return null;
//        }
        Organization organization = organizationService.getOrganizationFromLoginUser();
        proprietor.setOrganization(organization);
        proprietorRepository.save(proprietor);
        return proprietorRepository.save(proprietor);
    }

    @Override
    @Transactional
    public Proprietor update(Long id, Object object) {
        ProprietorDto proprietorDto = (ProprietorDto) object;
        Proprietor proprietor = proprietorRepository.findById(proprietorDto.getId()).get();
        proprietor.setOrganization(organizationService.getOrganizationFromLoginUser());
//        if (proprietorDto.getDistributorId() != null) {
//            proprietor.setDistributor(distributorService.findById(proprietorDto.getDistributorId()));
//        }
        proprietor.setName(proprietorDto.getName());
        proprietor.setNid(proprietorDto.getNid());
        proprietor.setContactNo(proprietorDto.getContactNo());
        proprietor.setAddress(proprietorDto.getAddress());
        proprietor.setFatherName(proprietorDto.getFatherName());
        proprietor.setMotherName(proprietorDto.getMotherName());
        if (!this.validate(proprietor)) {
            return null;
        }
        return proprietorRepository.save(proprietor);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        try {
            Optional<Proprietor> optionalProprietor = proprietorRepository.findById(id);
            if (!optionalProprietor.isPresent()) {
                throw new Exception("Proprietor Not exist");
            }
            Proprietor proprietor = optionalProprietor.get();
            proprietor.setIsDeleted(true);
            proprietorRepository.save(proprietor);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Proprietor findById(Long id) {
        try {
            Optional<Proprietor> optionalProprietor = proprietorRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id);
            if (!optionalProprietor.isPresent()) {
                throw new Exception("Proprietor Not exist with id " + id);
            }
            return optionalProprietor.get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<Proprietor> findAll() {
        Organization organization = organizationService.getOrganizationFromLoginUser();
        return proprietorRepository.findAllByOrganizationAndIsDeletedFalse(organization);
    }

    @Override
    public boolean validate(Object object) {
        return true;
    }

    @Transactional
    public boolean saveAll(List<ProprietorDto> proprietorDtoList, Distributor distributor, MultipartFile[] fileList) {
        List<Proprietor> proprietorUpdatedList = new ArrayList<>();
        try {
            for (ProprietorDto pdt : proprietorDtoList) {
//                List<Proprietor> proprietorListByDistributor = proprietorRepository.findAllByDistributorAndIsActiveTrueAndIsDeletedFalse(distributor);
//                for(Proprietor deleteProprietor : proprietorListByDistributor){
//                    if(!deleteProprietor.getId().equals( pdt.getId())){
//                        deleteProprietor.setIsDeleted(true);
//                        proprietorRepository.save(deleteProprietor);
//                    }
//                }
                Proprietor proprietor = new Proprietor();
                if (pdt.getId() != null) {
                    proprietor = this.findById(pdt.getId());
                }

                proprietor.setName(pdt.getName().trim());
                proprietor.setNid(pdt.getNid().trim());
                proprietor.setContactNo(pdt.getContactNo());
                if(pdt.getAddress() != null){
                    proprietor.setAddress(pdt.getAddress().trim());
                }
                if(pdt.getFatherName() != null){
                    proprietor.setFatherName(pdt.getFatherName().trim());
                }
                if(pdt.getMotherName() != null){
                    proprietor.setMotherName(pdt.getMotherName().trim());
                }

                proprietor.setDistributor(distributor);
                Organization organization = organizationService.getOrganizationFromLoginUser();
                proprietor.setOrganization(organization);

                if (!this.validate(proprietor)) {
                    return false;
                }
                proprietorRepository.save(proprietor);
                ////////////////////////////////////////////////////////
                //File upload multiple
//                if(sanctionAmendmentFormModel.getMortgageFiles() !=null && sanctionAmendmentFormModel.getMortgageFiles().length > 0){
//                    for(MultipartFile file : sanctionAmendmentFormModel.getMortgageFiles()){
//                        if(sanctionAmendmentDetailsFormModel.getMortgageFileNameList() != null && sanctionAmendmentDetailsFormModel.getMortgageFileNameList().size() >0){
//                            for(String formFileName : sanctionAmendmentDetailsFormModel.getMortgageFileNameList()){
//                                if(formFileName.equals(file.getOriginalFilename())){
//                                   // String filePath = Constants.SANCTION_AMENDMENT_MORTGAGE_FILE_UPLOAD_DIRECTORY;
//                                    //String prefix = Constants.SANCTION_AMENDMENT_MORTGAGE_FILE_UPLOAD_PREFIX;
//                                    String tempFileOriginName= file.getOriginalFilename().split("\\|")[1];
//                                    String fileName = prefix + DateUtil.getTimestampFromUtilDate(new Date()).toString()
//                                            + Constants.UNDERSCORE + tempFileOriginName;
//                                    String uploadPath = filePath + File.separator + fileName;
//
//                                    //Creating document setup
//
//                                    String remarks = "Sanction Amendment id: " + sanctionAmendment.getId();
//                                    sanctionAmendmentMortgageDocumentService.save(tempFileOriginName, uploadPath, remarks, sanctionAmendmentDetails.getId());
//
//                                    //Saving file
//                                    fileStorageService.upload(file, filePath, fileName);
//
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
                /////////////////////////////////////////////////////////
                if (fileList != null && fileList.length > 0) {
                    for (MultipartFile file : fileList) {
                        if (pdt.getFileName().equalsIgnoreCase(file.getOriginalFilename())) {
                            String tempFileOriginName= file.getOriginalFilename().split("\\|")[1];
                            String filePath = fileUploadService.fileUpload(file, FileType.LOGO.getCode(),
                                    PROPRIETOR_LOGO_UPLOAD_DIRECTORY,
                                    organization.getId(), organization.getId());

//                            documentService.save(PROPRIETOR_LOGO_UPLOAD_DIRECTORY, filePath, proprietor.getId(),
//                                    fileUploadService.getFileNameFromFilePath(filePath), FileType.LOGO.getCode()
//                                    , organization, organization.getId());
                            documentService.save(PROPRIETOR_LOGO_UPLOAD_DIRECTORY, filePath, proprietor.getId(),
                                    tempFileOriginName, FileType.LOGO.getCode()
                                    , organization, organization.getId(), file.getSize());
                        }
                    }
                }
                proprietorUpdatedList.add(proprietor);

            }
            List<Proprietor> proprietorListByDistributor = proprietorRepository.findAllByDistributorAndIsActiveTrueAndIsDeletedFalse(distributor);
            // Deleting Proprietor
            List<Proprietor> proprietorDeleteList = proprietorListByDistributor.stream().filter(it -> !proprietorUpdatedList.contains(it)).collect(Collectors.toList());

            if (proprietorDeleteList.size() > 0) {
                for (Proprietor pp : proprietorDeleteList) {
                    pp.setIsDeleted(true);
                    proprietorRepository.save(pp);
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<ProprietorDto> getProprietorInfoByDistributor(Distributor distributor) {//Return List type Dto as we view data in form model
        List<Proprietor> proprietorList = proprietorRepository.findAllByDistributorAndIsActiveTrueAndIsDeletedFalse(distributor);//from proprietorRepository fetch distributor object using findAllByDistributorAndIsActiveTrueAndIsDeletedFalse this query and put that object as a list in proprietorList

        List<ProprietorDto> proprietorDtoList = new ArrayList<>();

        for (Proprietor proprietor : proprietorList) {// from proprietorList this entity object at 0 position, get entity and set dto
            ProprietorDto proprietorDto = new ProprietorDto();
            proprietorDto.setId(proprietor.getId());
            proprietorDto.setName(proprietor.getName());
            proprietorDto.setNid(proprietor.getNid());
            proprietorDto.setAddress(proprietor.getAddress());
            proprietorDto.setContactNo(proprietor.getContactNo());
            proprietorDto.setFatherName(proprietor.getFatherName());
            proprietorDto.setMotherName(proprietor.getMotherName());
            String filePath = documentService.getDocumentFilePath(PROPRIETOR_LOGO_UPLOAD_DIRECTORY, proprietor.getId(), FileType.LOGO);
            if ((!filePath.equals("")) && filePath != null) {
//                proprietorDto.setFilePath(filePath);
                String s = new String(Base64.encodeBase64(fileDownloadService.fileDownload(
                        documentService.getFilePath(proprietor.getId(), PROPRIETOR_LOGO_UPLOAD_DIRECTORY))));
                proprietorDto.setFilePath(s);
            }
            proprietorDtoList.add(proprietorDto);// 1st list, 2nd list contains  in proprietorDtoList
        }
        return proprietorDtoList;
    }

}

