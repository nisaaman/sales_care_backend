package com.newgen.ntlsnc.common.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Newaz Sharif
 * @since 22th June, 22
 */
public interface FileTransfer {

    boolean uploadFileToServer(MultipartFile multipartFile, String remoteFilePath, String fileName);
    byte[] downloadFileFromServer(String remoteFilePath);
    boolean isDirectoryExists(String remoteFilePath, ChannelSftp channelSftp);
    void createDirectory(String remoteFilePath, ChannelSftp channelSftp) throws SftpException;

}
