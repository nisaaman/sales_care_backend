package com.newgen.ntlsnc.common.sftp;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Newaz Sharif
 * @since 22th June, 22
 */

@Service
public class SFTPChannelConnectionService {

    @Value("${sftp.host}")
    private String host;

    @Value("${sftp.port}")
    private Integer port;

    @Value("${sftp.username}")
    private String username;

    @Value("${sftp.password}")
    private String password;

    @Value("${sftp.sessionTimeout}")
    private Integer sessionTimeout;

    @Value("${sftp.channelTimeout}")
    private Integer channelTimeout;

    public ChannelSftp connectChannelSftp() {
        try {
            JSch jSch = new JSch();
            Session session = jSch.getSession(username, host);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect(sessionTimeout);
            Channel channel = session.openChannel("sftp");
            channel.connect(channelTimeout);
            return (ChannelSftp) channel;
        } catch(JSchException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public void disconnectChannelSftp(ChannelSftp channelSftp) {
        try {
            if( channelSftp == null)
                return;

            if(channelSftp.isConnected())
                channelSftp.disconnect();

            if(channelSftp.getSession() != null)
                channelSftp.getSession().disconnect();

        } catch(Exception ex) {

        }
    }

}
