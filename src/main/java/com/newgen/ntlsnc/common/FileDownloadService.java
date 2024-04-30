package com.newgen.ntlsnc.common;

import com.newgen.ntlsnc.common.sftp.FileTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Newaz Sharif
 * @since  1st August,22
 */
@Service
public class FileDownloadService {

    @Autowired
    FileTransferService fileTransferService;

    public byte[] fileDownload(String filePath) {
        return fileTransferService.downloadFileFromServer(filePath);
    }
}
