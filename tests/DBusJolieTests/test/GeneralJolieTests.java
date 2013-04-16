/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.*;
import static org.junit.Assert.*;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author niels
 */
public class GeneralJolieTests {
    private static String[] defaultArgs;
    private static String jpf;
    
    @BeforeClass
    public static void setUpClass() { 
        jpf = "jolie-programs";
        defaultArgs = new String[] {
            "-i", "../../jolie-src/include", 
            "-l", "../../jolie-src/javaServices/coreJavaServices/dist/coreJavaServices.jar",
            "-l", "../../jolie-src/javaServices/minitorJavaServices/dist/monitorJavaServices.jar",
            "-l", "../../jolie-src/lib/xsom/dist",
            "-l", "../../jolie-src/lib/jolie-xml/dist" 
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
    
    // Simple Hello World, uses seperate process
    @Test
    public void hello() throws Exception {
        // Arrange
        JolieSubProcess p = new JolieSubProcess(jpf+"/HelloWorld.ol", defaultArgs);
        
        // Act
        p.start();
        p.join();
        
        // Assert
        assertEquals("Hello, world!", p.getOutput());
    }
    
    // HelloFileSystem, file system usage in Jolie
    @Test
    public void file() throws Exception {
        // Arrange
        JolieThread p = new JolieThread(
                jpf+"/HelloFileSystem.ol", defaultArgs);
        
        // Act
        p.start();
        p.join();
        
        // Assert
        assertEquals("Hello file system!", p.getOutput());
    }
    
    @Test
    public void clientServer() throws Exception {
        // Arrange
        String[] testArgs = new String[] { 
            "-l", "../../jolie-src/extensions/sodep/dist/*", 
            "-l", "../../jolie-src/extensions/dbus/dist/*",
            "-l", "../../jolie-src/lib/libmatthew",
            "-l", "../../jolie-src/lib/dbus-java"
        };
        String[] args = ArrayUtils.addAll(testArgs, defaultArgs);
        JolieSubProcess server = new JolieSubProcess(jpf+"/dbusserver.ol", args);
        JolieSubProcess client = new JolieSubProcess(jpf+"/dbusclient.ol", args);
        
        // Act 
        server.start();
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("10", client.getOutput());
    }
    
    @Test
    public void parallelClients() throws Exception {
        // Arrange
        String[] testArgs = new String[] { 
            "-l", "../../jolie-src/extensions/sodep/dist/*", 
            "-l", "../../jolie-src/extensions/dbus/dist/*",
            "-l", "../../jolie-src/lib/libmatthew",
            "-l", "../../jolie-src/lib/dbus-java"
        };
        String[] args = ArrayUtils.addAll(testArgs, defaultArgs);
        JolieSubProcess server = new JolieSubProcess(jpf+"/dbusserver.ol", args);
        JolieThread client = new JolieThread(jpf+"/parallelclient.ol", args);
        
        // Act 
        server.start();
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("1014", client.getOutput());
    }
    
    @Test
    public void simpleServer() throws Exception {
        // Arrange
        String[] testArgs = new String[] { 
            "-l", "../../jolie-src/extensions/sodep/dist/*", 
            "-l", "../../jolie-src/extensions/dbus/dist/*",
            "-l", "../../jolie-src/lib/libmatthew",
            "-l", "../../jolie-src/lib/dbus-java"
        };
        String[] args = ArrayUtils.addAll(testArgs, defaultArgs);
        JolieThread server = new JolieThread(jpf+"/concurrency/server_single.ol", args);
        
        // Act 
        server.start();
        server.join();
        
        // Assert
        assertTrue(true);
    }
    
    // The jolie program to call NextPage of Okular D-Bus API
    @Test
    public void okularNextPage() throws Exception {
        // Arrange
        String[] testArgs = new String[] { 
            "jolie-programs/NextPage.ol", 
            "-l", "../../jolie-src/extensions/dbus/dist/*", // Load D-Bus extension, 
            "-l", "../../jolie-src/extensions/sodep/dist/*", 
            "-l", "../../jolie-src/lib/libmatthew",
            "-l", "../../jolie-src/lib/dbus-java" 
        };
        String[] args = ArrayUtils.addAll(testArgs, defaultArgs);
        JolieThread jt = new JolieThread(jpf+"/dbusserver.ol", args);
        
        // Act 
        jt.start();
        jt.join();
        
        // Assert
        assertTrue(true);
        
    }
}