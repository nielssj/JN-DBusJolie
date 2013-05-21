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
        Thread.sleep(500);
        
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
        JolieSubProcess client = new JolieSubProcess(jpf+"/parallelclient.ol", args);
        
        // Act 
        server.start();
        Thread.sleep(500);
        
        client.start();
        client.join();
        server.stop();
        
        // Assert
        assertEquals("1014", client.getOutput());
    }
}