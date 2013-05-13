/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import org.junit.*;
import static org.junit.Assert.*;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
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
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
        

/**
 *
 * @author niels
 */
public class BenchmarkingTests {
    private static String[] defaultArgs;
    private static String jpf;
    
    @BeforeClass
    public static void setUpClass() { 
        jpf = "jolie-programs/concurrency/";
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
    
    @Test
    public void sendTiny() throws Exception {
        // Configure client and server
        JolieSubProcess server = new JolieSubProcess(jpf+"server_concurrent.ol", defaultArgs);
        JolieThread client = new JolieThread(jpf+"client_benchmark.ol", defaultArgs);
        
        // Set up benchmark logging
        Logger logger = setUpLogger(
            new String[] {
                "sendImpl - Called", 
                "sendImpl - Returned succesfully"
            }, 
            "sendTiny.csv", "jolie.net.dbus");
        
        // Execute
        server.start();
        String firstLine = server.getOutputLine(); // (Blocking) Wait for first output from server
        client.start();
        client.join();
        server.stop();
        
        // Push memory log to file
        MemoryHandler mh = (MemoryHandler) logger.getHandlers()[0];
        mh.push();
    }
    
    @Test
    public void recvTiny() throws Exception {
        // Configure client and server
        JolieSubProcess server = new JolieSubProcess(jpf+"server_concurrent.ol", defaultArgs);
        JolieThread client = new JolieThread(jpf+"client_benchmark.ol", defaultArgs);
        
        // Set up benchmark logging
        Logger logger = setUpLogger(
            new String[] {
                "recvResponseFor - Found matching response", 
                "recvResponseFor - Returned succesfully"
            }, 
            "recvTiny.csv", "jolie.net.dbus");
        
        // Execute
        server.start();
        String firstLine = server.getOutputLine(); // (Blocking) Wait for first output from server
        client.start();
        client.join();
        server.stop();
        
        // Push memory log to file
        MemoryHandler mh = (MemoryHandler) logger.getHandlers()[0];
        mh.push();
    }
    
    
    @Test
    public void fullTiny() throws Exception {
        // Configure client and server
        JolieSubProcess server = new JolieSubProcess(jpf+"server_concurrent.ol", defaultArgs);
        JolieThread client = new JolieThread(jpf+"client_benchmark.ol", defaultArgs);
        
        // Set up benchmark logging
        Logger logger = setUpLogger(
            new String[] {
                "sendImpl - Called",
                "sendImpl - Sending",
                "sendImpl - Sent",
                "recvResponseFor - Found matching response",
                "recvResponseFor - Returned succesfully"
            },
            "fullTiny.csv", "jolie.net.dbus");
        
        // Execute
        server.start();
        String firstLine = server.getOutputLine(); // (Blocking) Wait for first output from server
        client.start();
        client.join();
        server.stop();
        
        // Push memory log to file
        MemoryHandler mh = (MemoryHandler) logger.getHandlers()[0];
        mh.push();
    }
    
    @Test
    public void fullTinySOAPSocket() throws Exception {
        // Configure client and server
        JolieSubProcess server = new JolieSubProcess("jolie-programs/benchmark/server_soap_concurrent.ol", defaultArgs);
        JolieThread client = new JolieThread("jolie-programs/benchmark/client_soap_benchmark.ol", defaultArgs);
        
        // Set up benchmark logging
        Logger logger = setUpLogger(
            new String[] {
                "sendImpl - Called",
                "sendImpl - Sending",
                "sendImpl - Sent",
                "recvImpl - Called",
                "recvImpl - Returned succesfully"
            },
            "fullTinySOAP.csv", "jolie.net.socket");
        
        // Execute
        server.start();
        String firstLine = server.getOutputLine(); // (Blocking) Wait for first output from server
        client.start();
        client.join();
        server.stop();
        
        // Push memory log to file
        MemoryHandler mh = (MemoryHandler) logger.getHandlers()[0];
        mh.push();
    }
    
    @Test
    public void fullLargeSOAPSocket() throws Exception {
        // Configure client and server
        JolieSubProcess server = new JolieSubProcess("jolie-programs/benchmark/server_soap2_concurrent.ol", defaultArgs);
        JolieThread client = new JolieThread("jolie-programs/benchmark/large_soap_benchmark.ol", defaultArgs);
        
        // Set up benchmark logging
        Logger logger = setUpLogger(
            new String[] {
                "sendImpl - Called",
                "sendImpl - Sending",
                "sendImpl - Sent",
                "recvImpl - Called",
                "recvImpl - Returned succesfully"
            },
            "fullLargeSOAP.csv", "jolie.net.socket");
        
        // Execute
        server.start();
        String firstLine = server.getOutputLine(); // (Blocking) Wait for first output from server
        client.start();
        client.join();
        server.stop();
        
        // Push memory log to file
        MemoryHandler mh = (MemoryHandler) logger.getHandlers()[0];
        mh.push();
    }
    
    private Logger setUpLogger(String[] allowedMessages, String filename, String logDomain) throws IOException {
        // Filter - Only allow entries with specific messages
        ExclusiveMessagesFilter emf = new ExclusiveMessagesFilter(allowedMessages);
        
        // File handler - Save to time stamps to file
        FileHandler fh = new FileHandler(filename);
        fh.setFormatter(new BenchLogFormatter(allowedMessages[allowedMessages.length-1]));
        fh.setFilter(emf);
        
        // Memory buffer - Store in memory during benchmark
        MemoryHandler mh = new MemoryHandler(fh, 100000, Level.OFF);
        
        // Logger - Configure logger with handlers, filter and level
        Logger logger = Logger.getLogger(logDomain);
        logger.setUseParentHandlers(false);
        logger.addHandler(mh);
        logger.setLevel(Level.FINE);
        
        return logger;
    }
    
    // *1   Initialization of CommChannel ("Hello cost")
    // *2   Send request (marshal and send off)
    // *3   Bus trip
    // *4   Receive response (read and demarshal)
    
    // *5   Introspect
    //      - Call
    //      - Parsing
    // *6   Make introspectable
    //      - Build and serialize
    
    @Test
    public void test0() throws Exception {
        
        DBusCommChannelFactory dcf = new DBusCommChannelFactory(null);
        URI loc = new URI("dbus:/org.testname7:/object");
        Type t = Type.create(NativeType.INT, null, true, null);
        RequestResponseTypeDescription rtd = new RequestResponseTypeDescription(t, t, null);
        Map<String, OneWayTypeDescription> owtds = new HashMap<String, OneWayTypeDescription>();
        Map<String, RequestResponseTypeDescription> rtds = new HashMap<String, RequestResponseTypeDescription>();
        rtds.put("Twice", rtd);
        Interface iface = new Interface(owtds, rtds);
        
        // Make output channel
        OutputPort port = new OutputPort(null, "1", null, null, iface, true);
        long oistart = System.nanoTime();
        CommChannel channel = dcf.createChannel(loc, port);
        long oiend = System.nanoTime();
        
        // Make input channel
        InputPort iport = new InputPort("john", loc, null, iface, null, null);
        long iistart = System.nanoTime();
        DBusCommChannel ichannel = DBusCommChannelFactory.createChannel(loc, iport);
        long iiend = System.nanoTime();
        
        double oinit = new Long(oiend-oistart).doubleValue()/(1000000.0);
        double iinit = new Long(iiend-iistart).doubleValue()/(1000000.0);
        System.out.println("oinit\t\tiinit");
        System.out.printf("%fms\t%fms\n\n", oinit, iinit);

        
        // Make message
        CommMessage msg = new CommMessage(1, "twice", null, Value.create(10), null);
        long start, end, recvd, marshld;
        double send, trip, recv;
        
        int wc = 1000;
        int bc = 10000;
        
        // Warm up
        System.out.println("Starting warm up..");
        for(int i = 0; i < wc; i++) {
            // Send message
            channel.send(msg);
            if(ichannel.listen()) {
                CommMessage resp = ichannel.recv();
            }
        }
        
        // Benchmark
        System.out.println("Starting benchmark..");
        double[] sends = new double[bc];
        double[] trips = new double[bc];
        double[] recvs = new double[bc];
        for(int i = 0; i < bc; i++) {
            // Send message
            start = System.nanoTime();
            channel.send(msg);
            end = System.nanoTime();
            
            // Listen for message
            recvd = -1;
            marshld = -1;
            if(ichannel.listen()) {
                recvd = System.nanoTime();
                CommMessage resp = ichannel.recv();
                marshld = System.nanoTime();
            }
            
            // Save result to memory
            sends[i] = new Long(end-start).doubleValue()/(1000000.0);
            trips[i] = new Long(recvd-end).doubleValue()/(1000000.0);
            recvs[i] = new Long(marshld-recvd).doubleValue()/(1000000.0);
        }
        
        // Mean
        double sendMean = new Mean().evaluate(sends);
        double tripMean = new Mean().evaluate(trips);
        double recvMean = new Mean().evaluate(recvs);
        System.out.println("\tsend\t\ttrip\t\trecv");
        System.out.printf("mean:\t%fms\t%fms\t%fms\n", sendMean, tripMean, recvMean);
        
        // Standard Deviation
        double sendSD = new StandardDeviation().evaluate(sends);
        double tripSD = new StandardDeviation().evaluate(trips);
        double recvSD = new StandardDeviation().evaluate(recvs);
        System.out.printf("sd:\t%fms\t%fms\t%fms\n", sendSD, tripSD, recvSD);
        
        writeToCSV(sends, trips, recvs);
        
        assertTrue(true);
    }
    
    private void writeToCSV(double[] sends, double[] trips, double[] recvs) throws Exception {
        PrintWriter pw = new PrintWriter("bench.csv", "UTF-8");
        
        for(int i = 0; i < sends.length; i++) {
            pw.printf("%f,%f,%f\n", sends[i], trips[i], recvs[i]);
        }
        
        pw.flush();
        pw.close();
    }
}