/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import jolie.Jolie;
import jolie.lang.NativeType;
import jolie.net.CommChannel;
import jolie.net.CommMessage;
import jolie.net.DBusCommChannel;
import jolie.net.DBusCommChannelFactory;
import jolie.net.ports.InputPort;
import jolie.net.ports.Interface;
import jolie.net.ports.OutputPort;
import jolie.runtime.Value;
import jolie.runtime.typing.OneWayTypeDescription;
import jolie.runtime.typing.RequestResponseTypeDescription;
import jolie.runtime.typing.Type;
import org.apache.commons.lang3.ArrayUtils;
import org.freedesktop.dbus.exceptions.DBusException;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author niels
 */
public class ConcurrencyTests {
    private static String[] defaultArgs;
    private static String jpf;
    
    @BeforeClass
    public static void setUpClass() { 
        jpf = "jolie-programs/concurrency/new/";
        defaultArgs = new String[] {
            "-i", "../../jolie-src/include", 
            "-l", "../../jolie-src/javaServices/coreJavaServices/dist/coreJavaServices.jar",
            "-l", "../../jolie-src/javaServices/minitorJavaServices/dist/monitorJavaServices.jar",
            "-l", "../../jolie-src/lib/xsom/dist",
            "-l", "../../jolie-src/lib/jolie-xml/dist", 
            "-l", "../../jolie-src/extensions/soap/dist/*",
            "-l", "../../jolie-src/extensions/http/dist/*",
            "-l", "../../jolie-src/extensions/dbus/dist/*",
            "-l", "../../jolie-src/lib/relaxngDatatype",
            "-l", "../../jolie-src/lib/wsdl4j",
            "-l", "../../jolie-src/lib/libmatthew",
            "-l", "../../jolie-src/lib/dbus-java" 
        };
    }
    
    @Before
    public void setUp() {
        System.setSecurityManager(new NoExitSecurityManager());
    }
    
    @After
    public void tearDown() {
        System.setSecurityManager(null);
    }
    
    /*
     * CONCURRENCY TESTS
     * ================
     * 
     * Full Jolie-runtime:
     * 0 - 50 concurrent twice calls -                  server_concurrent
     * 1 - 50 sequential twice calls -                  server_concurrent
     * 2 - 50 concurrent twice calls -                  server_sequential
     * 3 - 50 sequential twice calls -                  server_sequential
     * 4 - (should fail) 50 concurrent twice calls -    server_single
     * 5 - (should fail) 50 sequental twice calls -     server_single
     * 
     * CommChannel:
     * 6 - TODO, something with clever Thread.sleeps
     * 
     */
    
    // 0 - 50 concurrent twice calls, concurrent server (Full Jolie runtime)
    @Test
    public void test0() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_concurrent.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_50calls_concurrent.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("Responses: [0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, 74, 76, 78, 80, 82, 84, 86, 88, 90, 92, 94, 96, 98, ]", 
                client.getOutput());
    }
    
    // 1 - 50 sequential twice calls, concurrent server (Full Jolie runtime)
    @Test
    public void test1() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_concurrent.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_50calls_sequential.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("Responses: [0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, 74, 76, 78, 80, 82, 84, 86, 88, 90, 92, 94, 96, 98, ]", 
                client.getOutput());
    }
    
    // 2 - 50 concurrent twice calls, sequential server (Full Jolie runtime)
    @Test
    public void test2() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_sequential.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_50calls_concurrent.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("Responses: [0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, 74, 76, 78, 80, 82, 84, 86, 88, 90, 92, 94, 96, 98, ]", 
                client.getOutput());
    }
    
    // 3 - 50 sequential twice calls, sequential server (Full Jolie runtime)
    @Test
    public void test3() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_sequential.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_50calls_sequential.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("Responses: [0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, 74, 76, 78, 80, 82, 84, 86, 88, 90, 92, 94, 96, 98, ]", 
                client.getOutput());
    }
    
    // 4 - (Should fail) 50 concurrent twice calls, single server (Full Jolie runtime)
    @Test
    public void test4() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_single.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_50calls_concurrent.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        String es = client.getErrorStream();
        assertTrue(es.endsWith("Thrown unhandled fault: org.freedesktop.DBus.Error.NoReply"));
    }
    
    // 5 - (Should fail) 50 sequential twice calls, single server (Full Jolie runtime)
    @Test
    public void test5() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_single.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_50calls_sequential.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        String es = client.getErrorStream();
        assertTrue(es.endsWith("Thrown unhandled fault: org.freedesktop.DBus.Error.ServiceUnknown"));
    }
    
    // 6 - Output port, twice requests with tactical timing of thread sleeps
    @Test
    public void test6() throws Exception {
        // Add console logger
        Logger logger = Logger.getLogger("jolie.net.dbus");
        logger.setUseParentHandlers(false);
        logger.addHandler(new ConsoleHandler());
        logger.setLevel(Level.FINE);
        
        // Arrange
        DBusCommChannelFactory dcf = new DBusCommChannelFactory(null);
        URI loc = new URI("dbus:/org.testname:/object");
        Type t = Type.create(NativeType.INT, null, true, null);
        RequestResponseTypeDescription rtd = new RequestResponseTypeDescription(t, t, null);
        Map<String, OneWayTypeDescription> owtds = new HashMap<String, OneWayTypeDescription>();
        Map<String, RequestResponseTypeDescription> rtds = new HashMap<String, RequestResponseTypeDescription>();
        rtds.put("Twice", rtd);
        Interface iface = new Interface(owtds, rtds);

        // Make output channel
        OutputPort port = new OutputPort(null, "1", null, null, iface, true);
        CommChannel channel = dcf.createChannel(loc, port);

        int mcount = 2000;
        CommMessage[] reqs = new CommMessage[mcount];
        int[] resps = new int[mcount];
        TwiceOutputThread[] tts = new TwiceOutputThread[mcount];
        
        // Make messages
        for(int i = 0; i < mcount; i++) {
            reqs[i] = new CommMessage(i, "twice", null, Value.create(i), null);
        }
        
        // Make threads
        for(int i = 0; i < mcount; i++) {
            tts[i] = new TwiceOutputThread(reqs[i], channel, 20, 40);
        }        
        
        // Act        
        for(int i = 0; i < mcount; i++) {
            tts[i].start();
        }
        for(int i = 0; i < mcount; i++) {
            TwiceOutputThread tt = tts[i];
            tt.join();
            resps[i] = tt.getResponse();
        }
        
        // Assert
        for(int i = 0; i < mcount; i++) {
            assertTrue(resps[i] == i*2);
        }
    }
    
    // 7 - Input port, twice responses with tactical timing of thread sleeps
    @Test
    public void test7() throws Exception {
        
        // Arrange
        DBusCommChannelFactory dcf = new DBusCommChannelFactory(null);
        URI loc = new URI("dbus:/org.testname:/object");
        Type t = Type.create(NativeType.INT, null, true, null);
        RequestResponseTypeDescription rtd = new RequestResponseTypeDescription(t, t, null);
        Map<String, OneWayTypeDescription> owtds = new HashMap<String, OneWayTypeDescription>();
        Map<String, RequestResponseTypeDescription> rtds = new HashMap<String, RequestResponseTypeDescription>();
        rtds.put("twice", rtd);
        Interface iface = new Interface(owtds, rtds);

        // Make input channel
        InputPort iport = new InputPort("john", loc, null, iface, null, null);
        DBusCommChannel channel = DBusCommChannelFactory.createChannel(loc, iport);
        
        int mcount = 2000;
        CommMessage[] reqs = new CommMessage[mcount];
        int[] resps = new int[mcount];
        TwiceInputThread[] tts = new TwiceInputThread[mcount];
        
        // Listen loop
        try {
            while (true) {
                if (channel.listen()) {
                    CommMessage req = channel.recv();
                    int value = req.value().intValue();
                    reqs[value] = req;
                    
                    TwiceInputThread tt = new TwiceInputThread(req, channel, value, value);
                    tts[value] = tt;
                    tt.start();
                }
            }
        } catch (DBusException ex) {
            // Transport was closed, do nothing
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected failure during listening", ex);
        }
        
        for(int i = 0; i < mcount; i++) {
            TwiceInputThread tt = tts[i];
            tt.join();
            resps[i] = tt.getResponse();
        }
        
        // Assert
        for(int i = 0; i < mcount; i++) {
            // TODO: Something
        }
    }
    
    public class TwiceOutputThread extends Thread
    {
        private CommMessage req, resp;
        private CommChannel channel;
        private int preDelay, postDelay;

        public TwiceOutputThread(CommMessage req, CommChannel channel, int minDelay, int maxDelay)
        {
            this.req = req;
            this.resp = null;
            this.channel = channel;
            this.preDelay = minDelay + (int)(Math.random() * ((maxDelay - minDelay) + 1));
            this.postDelay = minDelay + (int)(Math.random() * ((maxDelay - minDelay) + 1));
        }

        @Override
        public void run() {
            
            // Take sleep of random length
            try {
                Thread.sleep(this.preDelay);
            } catch (InterruptedException ex) { }
            
            // Send request
            try {
                this.channel.send(this.req);
            } catch (IOException ex) { 
                return; //Failed, test will fail because return value remains null
            }
            
            // Take sleep of random length
            try {
                Thread.sleep(this.postDelay);
            } catch (InterruptedException ex) { }
            
            
            // Receive response
            try {
                this.resp = this.channel.recvResponseFor(req);
            } catch (IOException ex) {
                //Failed, test will fail because return value remains null
            }
        }
        
        public int getResponse() {
            if(resp != null) {
                return this.resp.value().intValue();
            }
            return -1;
        }
    }
    
    public class TwiceInputThread extends Thread
    {
        private CommMessage req, resp;
        private CommChannel channel;
        private int preDelay, postDelay;

        public TwiceInputThread(CommMessage req, CommChannel channel, int minDelay, int maxDelay)
        {
            this.req = req;
            this.resp = null;
            this.channel = channel;
            this.preDelay = minDelay + (int)(Math.random() * ((maxDelay - minDelay) + 1));
            this.postDelay = minDelay + (int)(Math.random() * ((maxDelay - minDelay) + 1));
        }

        @Override
        public void run() {
            
            // Take sleep of random length
            try {
                Thread.sleep(this.preDelay);
            } catch (InterruptedException ex) { }
            
            // Send response
            int value = this.req.value().intValue();
            this.resp = CommMessage.createResponse(this.req, Value.create(value*2));
            
            try {
                this.channel.send(this.resp);
            } catch (IOException ex) { 
                return; //Failed, test will fail because return value remains null
            }
            
            // Take sleep of random length
            try {
                Thread.sleep(this.postDelay);
            } catch (InterruptedException ex) { }
        }
        
        public int getResponse() {
            if(resp != null) {
                return this.resp.value().intValue();
            }
            return -1;
        }
    }
}