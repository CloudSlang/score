import org.eclipse.score.samples.docker.actions.SSHAction;
import com.jcraft.jsch.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import com.jcraft.jsch.*;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.util.Map;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.verifyNew;

/**
 * Date: 10/15/2014
 *
 * @author lesant
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(SSHAction.class)
public class SSHActionTest {

    private static final String RETURN_CODE = "returnCode";
    private static final String SUCCESS = "0";
    private static final String FAILURE = "1";

    @Mock
    private JSch jsch;

    @Test
    public void testSendCommand() throws Exception {

        Session session = PowerMockito.mock(Session.class);
        ChannelExec channel = PowerMockito.mock(ChannelExec.class);
        InputStream inputStream = PowerMockito.mock(InputStream.class);

        String command = "mkdir testfolder";
        String userName = "user";
        String password = "pass";
        String port = "22";
        String connectionIP = "ip";

        PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jsch);

        PowerMockito.when(jsch.getSession(userName, connectionIP, Integer.parseInt(port))).thenReturn(session);
        PowerMockito.when(session.openChannel(anyString())).thenReturn(channel);
        PowerMockito.when(channel.getInputStream()).thenReturn(inputStream);
        PowerMockito.when(inputStream.read()).thenReturn(0xffffffff);


        SSHAction ssh = new SSHAction();

        Map<String, String> returnResult = ssh.execute(userName, password, connectionIP, port, command);

        verifyNew(JSch.class).withNoArguments();
        verify(jsch).getSession(userName, connectionIP, Integer.parseInt(port));
        verify(session).setPassword(password);
        verify(session).connect(anyInt());
        verify(session).openChannel(anyString());
        verify(channel).getInputStream();
        verify(channel).setCommand(command);
        verify(inputStream).read();


        assertEquals(SUCCESS, returnResult.get(RETURN_CODE));

        SSHAction ssh2 = new SSHAction();

        when(jsch.getSession(userName, connectionIP, Integer.parseInt(port))).thenThrow(new JSchException());
        when(session.openChannel(anyString())).thenReturn(channel);
        when(channel.getInputStream()).thenReturn(inputStream);
        when(inputStream.read()).thenReturn(0xffffffff);


        returnResult = ssh2.execute(userName, password, connectionIP, port, command); // gives log4j warning
        assertEquals(FAILURE, returnResult.get(RETURN_CODE));
    }
}
