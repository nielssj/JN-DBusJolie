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
        JolieTestProgram2 p = new JolieTestProgram2(jpf+"/HelloWorld.ol", defaultArgs);
        
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
        JolieTestProgram p = new JolieTestProgram(
                jpf+"/HelloFileSystem.ol", defaultArgs, "MyFile.txt");
        
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
            "-l", "../../jolie-src/extensions/localsocket/dist/*",
            "-l", "../../jolie-src/lib/libmatthew"
        };
        String[] args = ArrayUtils.addAll(testArgs, defaultArgs);
        JolieTestProgram2 server = new JolieTestProgram2(jpf+"/server.ol", args);
        JolieTestProgram2 client = new JolieTestProgram2(jpf+"/client.ol", args);
        
        // Act 
        server.start();
        client.start();
        client.join();
        server.join();
        
        // Assert
        assertEquals("10", client.getOutput());
    }
    
    // The jolie program to call NextPage of Okular D-Bus API
    @Test
    public void okularNextPage() {
        // Arrange
        String[] testArgs = new String[] { 
            "jolie-programs/NextPage.ol", 
            "-l", "../../jolie-src/extensions/dbus/dist/*", // Load D-Bus extension, 
            "-l", "../../jolie-src/extensions/sodep/dist/*", // TODO: Figure how to make Jolie not require a protocol when using D-Bus (Until then we just reference sodep without using it)
            "-l", "../../jolie-src/lib/libmatthew" // unix.jar (TODO: D-Bus extension should reference this on its own?)
        };
        String[] args = ArrayUtils.addAll(testArgs, defaultArgs);
    }
}