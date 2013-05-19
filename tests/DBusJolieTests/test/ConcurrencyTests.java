/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.logging.Level;
import java.util.logging.Logger;
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
}