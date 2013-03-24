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
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

/**
 *
 * @author niels
 */
public class GeneralJolieTests {
    private static String[] defaultArgs;
    private static PrintStream stdOut = System.out;
    private ByteArrayOutputStream myOutBAOS;
    private PrintStream myOut;
    
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    
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
        myOutBAOS = new ByteArrayOutputStream();
        myOut = new PrintStream(myOutBAOS);
    }
    
    @After
    public void tearDown() {
        myOut.close();
    }
    
    // The simple HelloWorld program (requires no extensions)
    @Test
    public void hello() {
        // Arrange
        String[] testArgs = new String[] { 
            "jolie-programs/HelloWorld.ol"
        };
        String[] args = ArrayUtils.addAll(testArgs, defaultArgs);
        
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
        Jolie.main(args);
    }
}