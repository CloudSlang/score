package com.hp.score.samples.docker.actions;

import com.hp.oo.sdk.content.annotations.Param;
import com.jcraft.jsch.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 10/23/2014
 *
 * @author lesant
 */
public class SSHAction {
    private final static Logger logger = Logger.getLogger(SSHAction.class);
    private JSch jschSSHChannel;
    private String strUserName;
    private String strConnectionIP;
    private String strPassword;
    private Session sesConnection;
    private int intConnectionPort;

    private static final int intTimeOut = 60000;
    private static final int defaultConnectionPort = 22;

    public static final String RETURN_RESULT = "returnResult";
    private static final String RETURN_CODE = "returnCode";
    private static final String ERROR_MESSAGE = "errorMesage";
    private static final String SUCCESS_CODE = "0";
    private static final String FAILURE_CODE = "1";
    private static final int END_READ = 0xffffffff;

    final public Map<String, String> execute(@Param("username") String username, @Param("password") String password,
                                             @Param("connectionIP") String connectionIP, @Param("port") String port, @Param("command") String command) {
        Map<String, String> returnResult = new HashMap<>();
        if (!validateInputs(username, password, connectionIP, port, command)) {
            returnResult.put(RETURN_CODE, FAILURE_CODE);
            returnResult.put(ERROR_MESSAGE, "Inputs were not valid");
        }

        jschSSHChannel = new JSch();

        strUserName = username;
        strPassword = password;
        strConnectionIP = connectionIP;
        if (port == null) {
            intConnectionPort = defaultConnectionPort;
        } else {
            intConnectionPort = Integer.parseInt(port);
        }
        if (!connect(returnResult)) {
            returnResult.put(RETURN_CODE, FAILURE_CODE);
            return returnResult;
        }
        String result = sendCommand(command, returnResult);

        sesConnection.disconnect();
        returnResult.put(RETURN_RESULT, result);
        if (result != null) {
            returnResult.put(RETURN_CODE, SUCCESS_CODE);
        } else {
            returnResult.put(RETURN_CODE, FAILURE_CODE);
        }

        return returnResult;
    }

    public Boolean connect(Map<String, String> returnResult) {
        try {
            sesConnection = jschSSHChannel.getSession(strUserName,
                    strConnectionIP, intConnectionPort);
            sesConnection.setPassword(strPassword);
            sesConnection.connect(intTimeOut);
            return true;

        } catch (JSchException jschX) {
            logger.error(strConnectionIP + ":" + intConnectionPort + " - " + jschX.getMessage());
            returnResult.put(ERROR_MESSAGE, strConnectionIP + ":" + intConnectionPort + " - " + jschX.getMessage());
            return false;

        }
    }

    public String sendCommand(String command, Map<String, String> returnResult) {
        StringBuilder outputBuffer = new StringBuilder();

        try {
            Channel channel = sesConnection.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.connect();
            InputStream commandOutput = channel.getInputStream();
            int readByte = commandOutput.read();
//            if(readByte == -1){ //not sure how to get output of terminal for example if command was unknown
//                currentErrorMessage = "Unknown command.";
//                return null;
//            }
            while (readByte != END_READ) {
                outputBuffer.append((char) readByte);
                readByte = commandOutput.read();
            }
            channel.disconnect();
        } catch (JSchException | IOException e) {
            logger.warn(strConnectionIP + ":" + intConnectionPort + " - " + e.getMessage());
            returnResult.put(ERROR_MESSAGE, strConnectionIP + ":" + intConnectionPort + " - " + e.getMessage());
            return null;
        }
        return outputBuffer.toString();
    }

    private Boolean validateInputs(String username, String password, String connectionIP, String port, String command) {
          return !StringUtils.isBlank(username) && !StringUtils.isBlank(password) && !StringUtils.isBlank(connectionIP) && !StringUtils.isBlank(port)
                && !StringUtils.isBlank(command);
    }
}
