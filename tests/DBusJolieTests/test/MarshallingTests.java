import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author niels
 */
public class MarshallingTests {

  private static String[] defaultArgs;
  private static String jpf;

  @BeforeClass
  public static void setUpClass() {
    jpf = "jolie-programs";
    defaultArgs = new String[]{
      "-i", "../../jolie-src/include",
      "-l", "../../jolie-src/javaServices/coreJavaServices/dist/coreJavaServices.jar",
      "-l", "../../jolie-src/javaServices/minitorJavaServices/dist/monitorJavaServices.jar",
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
  /*
   * Checks for handling of simple types. The client calls a method on the server,
   * and checks that it gets the right return value.
   */
  public void simpleTypeTest() throws Exception {
    // Arrange
    JolieSubProcess server = new JolieSubProcess(jpf + "/marshalling/simpleTypesServer.ol", defaultArgs);
    JolieSubProcess client = new JolieSubProcess(jpf + "/marshalling/simpleTypesClient.ol", defaultArgs);

    // Act 
    server.start();
    client.start();
    client.join();
    server.stop();

    // Assert
    assertEquals("PassedPassedPassed", client.getOutput());
  }

  @Test
  /*
   * Check that everything given to `.params` is handled correctly. Sends an object containing simple types,
   * a map type and and array type to the server, which checks that everything is correct. The server then sends
   * the request back to the client, which checks that it is still correct.
   */
  public void paramsTest() throws Exception {
    // Arrange
    JolieSubProcess server = new JolieSubProcess(jpf + "/marshalling/paramsServer.ol", defaultArgs);
    JolieSubProcess client = new JolieSubProcess(jpf + "/marshalling/paramsClient.ol", defaultArgs);

    // Act 
    server.start();
    client.start();
    client.join();

    // Assert
    assertEquals("PassedPassedPassedPassedPassedPassed", server.getOutput());
    assertEquals("PassedPassedPassedPassedPassedPassed", client.getOutput());

    server.stop();
  }

  @Test
  /*
   * Tries to send an map with values of several types. Check that an exception is thrown,
   * since D-Bus does not support maps with values of differing types
   */
  public void mapTest() throws Exception {
    // Arrange
    JolieSubProcess client = new JolieSubProcess(jpf + "/marshalling/mapClient.ol", defaultArgs);

    // Act 
    client.start();
    client.join();

    // Assert
    assertTrue(client.getErrorStream().indexOf("DBus maps does not support several types.") != -1);
  }

  @Test
  /*
   * Tries to call a method over DBus with an object that has other properties than .params
   */
  public void paramsTest2() throws Exception {
    // Arrange
    JolieSubProcess client = new JolieSubProcess(jpf + "/marshalling/paramsClient2.ol", defaultArgs);

    // Act 
    client.start();
    client.join();

    // Assert
    assertTrue(client.getErrorStream().indexOf("Arguments to DBus must be either a single type or a map with one property named .params") != -1);
  }
}