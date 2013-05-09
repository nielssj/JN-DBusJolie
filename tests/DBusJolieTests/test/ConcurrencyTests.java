/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
        jpf = "jolie-programs/concurrency/";
        defaultArgs = new String[] {
            "-i", "../../jolie-src/include", 
            "-l", "../../jolie-src/javaServices/coreJavaServices/dist/coreJavaServices.jar",
            "-l", "../../jolie-src/javaServices/minitorJavaServices/dist/monitorJavaServices.jar",
            "-l", "../../jolie-src/lib/xsom/dist",
            "-l", "../../jolie-src/lib/jolie-xml/dist", 
            "-l", "../../jolie-src/extensions/sodep/dist/*", 
            "-l", "../../jolie-src/extensions/dbus/dist/*",
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
     * 1 - Single client, single server
     * 2 - Single client, concurrent server
     * 3 - Single client, sequential server
     * 
     * 4 - Concurrent(2) client, single server (client should fail)
     * 5 - Concurrent(2) client, concurrent server
     * 6 - Concurrent(2) client, sequential server
     * 
     * 7 - Sequential(2) client, single server (client should fail)
     * 8 - Sequential(2) client, concurrent server
     * 9 - Sequential(2) client, sequential server
     * 
     * IDEAS: delays, complex data, 3 clients
     */
    
    
    // 1 - Single client, single server
    @Test
    public void test1() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_single.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_single.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.join();
        
        // Assert
        assertEquals("10", client.getOutput());
    }
    
    // 2 - Single client, concurrent server
    @Test
    public void test2() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_concurrent.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_single.ol", defaultArgs);
        
        // Act 
        server.start();
        String firstLine = server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("10", client.getOutput());
    }
    
    // 3 - Single client, sequential server
    @Test
    public void test3() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_sequential.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_single.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("10", client.getOutput());
    }
    
    // 4 - Concurrent(2) client, single server (client should fail)
    @Test
    public void test4() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_single.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_concurrent.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.join();
        
        // Assert
        String es = client.getErrorStream();
        assertTrue(es.endsWith("Thrown unhandled fault: org.freedesktop.DBus.Error.NoReply"));
    }
    
    
    // 5 - Concurrent(2) client, concurrent server
    @Test
    public void test5() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_concurrent.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_concurrent.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("24", client.getOutput());
    }
    
    // 6 - Concurrent(2) client, sequential server
    @Test
    public void test6() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_sequential.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_concurrent.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("24", client.getOutput());
    }
    
    // 7 - Sequential(2) client, single server (client should fail)
    @Test
    public void test7() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_single.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_sequential.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.join();
        
        // Assert
        String es = client.getErrorStream();
        assertTrue(es.endsWith("Thrown unhandled fault: org.freedesktop.DBus.Error.NoReply"));
    }
    
    // 8 - Sequential(2) client, concurrent server
    @Test
    public void test8() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_concurrent.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_sequential.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("24", client.getOutput());
    }
    
    // 9 - Sequential(2) client, sequential server
    @Test
    public void test9() throws Exception {
        // Arrange
        JolieSubProcess server = new JolieSubProcess(jpf+"server_sequential.ol", defaultArgs);
        JolieSubProcess client = new JolieSubProcess(jpf+"client_sequential.ol", defaultArgs);
        
        // Act 
        server.start();
        server.getOutputLine(); // (Blocking) Wait for first output
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("24", client.getOutput());
    }
}