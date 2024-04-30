package com.newgen.ntlsnc.common.sftp;

import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @author Newaz Sharif
 * @since 22th June, 22
 */

@Service
public class FileTransferService implements FileTransfer {
    @Autowired
    SFTPChannelConnectionService sftpChannelConnectionService;

    @Override
    public boolean uploadFileToServer(MultipartFile multipartFile,
                                      String remoteFilePath, String fileName) {
        ChannelSftp channelSftp = sftpChannelConnectionService.connectChannelSftp();

        try {

            if (channelSftp == null) return false;

            boolean isDirExist = isDirectoryExists(remoteFilePath, channelSftp);
            if(!isDirExist) {

                createDirectory(remoteFilePath, channelSftp);
                channelSftp.put(multipartFile.getInputStream(), fileName);
            } else{
                channelSftp.put(multipartFile.getInputStream(), remoteFilePath+File.separator+fileName);
            }

            return true;
        } catch(SftpException | IOException ex) {
            ex.printStackTrace();
        } finally {
            sftpChannelConnectionService.disconnectChannelSftp(channelSftp);
        }

        return false;
    }

    @Override
    public byte[] downloadFileFromServer(String remoteFilePath) {
        ChannelSftp channelSftp = sftpChannelConnectionService.connectChannelSftp();

        try {

            if (channelSftp == null) return new byte[0];

            BufferedInputStream inputStream = new BufferedInputStream(
                    channelSftp.get(remoteFilePath));

            return IOUtils.toByteArray(inputStream);

        } catch(SftpException | IOException ex) {

        } finally {
            sftpChannelConnectionService.disconnectChannelSftp(channelSftp);
        }

        return new byte[0];
    }

    @Override
    public boolean isDirectoryExists(String remoteFilePath, ChannelSftp channelSftp) {

        try {
             channelSftp.lstat(remoteFilePath);
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void createDirectory(String remoteFilePath, ChannelSftp channelSftp) throws SftpException {

        String[] folderList = remoteFilePath.split( "/" );
        for ( String folder : folderList ) {
            if ( folder.length() > 0 ) {
                try {
                    channelSftp.cd( folder );
                }
                catch ( SftpException e ) {
                    channelSftp.mkdir( folder );
                    channelSftp.cd( folder );
                }
            }
        }
    }
}
