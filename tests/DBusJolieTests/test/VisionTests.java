import org.apache.commons.lang3.ArrayUtils;
import org.junit.*;

public class VisionTests {
    private static String[] defaultArgs;
    private static String jpf;
    
    @BeforeClass
    public static void setUpClass() { 
        jpf = "../../jolie-src/playground/vision/";
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
    
    @Test
    public void serverOkular() throws Exception {
        // Arrange
        String serverLocation = "Location_Presenter=\"socket://localhost:1337\"";
        String[] serverArgs = ArrayUtils.addAll(defaultArgs, new String[] { "-C", serverLocation });
        JolieThread server = new JolieThread(jpf+"presenter.ol okular", serverArgs);
        
        // Act 
        server.start();
        server.join();
        
        // Assert
    }
    
    @Test
    public void clientOkular() throws Exception {
        // Arrange
        String clientLocation = "Location_Presenter=\"socket://130.226.141.178:1337\"";
        String[] clientArgs = ArrayUtils.addAll(defaultArgs, new String[] { "-C", clientLocation });
        JolieThread  client = new JolieThread(jpf+"presenter.ol okular socket://130.226.141.189:1337", clientArgs);
        
        // Act 
        client.start();
        client.join();
        
        // Assert
    }
}