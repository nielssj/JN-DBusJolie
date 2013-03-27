/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.*;
import static org.junit.Assert.*;

import jolie.*;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author niels
 */
public class GeneralJolieTests {
    private static String[] defaultArgs;
    private static PrintStream stdOut = System.out;
    private ByteArrayOutputStream myOutBAOS;
    private PrintStream myOut;
    
    @BeforeClass
    public static void setUpClass() { 
        defaultArgs = new String[] {
            "-i", "../../jolie-src/include", 
            "-l", "../../jolie-src/javaServices/coreJavaServices/dist/coreJavaServices.jar",
            "-l", "../../jolie-src/javaServices/minitorJavaServices/dist/monitorJavaServices.jar" 
        };
    }
    
    @Before
    public void setUp() {
        System.setSecurityManager(new NoExitSecurityManager());
        myOutBAOS = new ByteArrayOutputStream();
        myOut = new PrintStream(myOutBAOS);
    }
    
    @After
    public void tearDown() {
        System.setSecurityManager(null);
        System.setOut(stdOut);
        myOut.close();
    }
    
    // The simple HelloWorld program (requires no extensions)
    @Test
    public void hello() {
        // Arrange
        String[] args = ArrayUtils.addAll(defaultArgs, new String[] { 
            "jolie-programs/HelloWorld.ol"
        });
        
        // Act
        try 
        {
            System.setOut(myOut);
            Jolie.main(args);
        } 
        // Assert
        catch (NoExitSecurityManager.ExitException e) 
        {
            System.setOut(stdOut);
            assertEquals("Exit status", 0, e.status);
            assertEquals("Hello, world!\n", myOutBAOS.toString());
        }
    }
    
    @Test
    public void clientServer() {
        /*// Arrange
        String[] testArgs = new String[] { 
            "-l", "../../jolie-src/extensions/sodep/dist/*",
            "-l", "../../jolie-src/extensions/localsocket/dist/*",
            "-l", "../../jolie-src/lib/libmatthew"
        };
        String[] args = ArrayUtils.addAll(testArgs, defaultArgs);
        String[] clientArgs = ArrayUtils.addAll(args, new String[] { "jolie-programs/client.ol" });
        String[] serverArgs = ArrayUtils.addAll(args, new String[] { "jolie-programs/server.ol" });
        
        exit.expectSystemExitWithStatus(0);
        exit.checkAssertionAfterwards(new Assertion() {
            // Assert
            @Override
            public void checkAssertion() {
                System.setOut(stdOut);
                assertEquals("Hello, world!\n", myOutBAOS.toString());
            }
        });
        
        // Act
        System.setOut(myOut);
        Jolie.main(serverArgs);*/
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