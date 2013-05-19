
import net.jolie.test.JolieToJava;
import org.freedesktop.DBus.Introspectable;
import org.freedesktop.dbus.DBusConnection;
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

  /*
   * Checks for handling of simple types. The client calls a method on the server,
   * and checks that it gets the right return value.
   */
  @Test
  public void simpleTypesTest() throws Exception {
    // Arrange
    JolieSubProcess server = new JolieSubProcess(jpf + "/marshalling/simpleTypesServer.ol", defaultArgs);
    JolieSubProcess client = new JolieSubProcess(jpf + "/marshalling/simpleTypesClient.ol", defaultArgs);

    // Act 
    server.start();
    Thread.sleep(1000);

    DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);

    Introspectable intro = conn.getRemoteObject("org.testname", "/object", Introspectable.class);
    String data = intro.Introspect();

    assertEquals(data, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<node name=\"/object\">"
            + "<interface name=\"org.testname\">"
            + "<method name=\"testM\"><arg direction=\"in\" type=\"i\"/><arg direction=\"out\" type=\"i\"/></method>"
            + "<method name=\"getInt\"><arg direction=\"out\" type=\"i\"/></method>"
            + "<method name=\"getBool\"><arg direction=\"out\" type=\"b\"/></method>"
            + "<method name=\"getString\"><arg direction=\"out\" type=\"s\"/></method>"
            + "</interface>"
            + "<interface name=\"org.freedesktop.DBus.Introspectable\">"
            + "<method name=\"Introspect\"><arg direction=\"out\" type=\"s\"/></method>"
            + "</interface>"
            + "</node>");

    client.start();
    client.join();

    // Assert
    assertEquals("PassedPassedPassed", client.getOutput());

    server.stop();
  }

  @Test
  /*
   * Checks that complex data types are handled correctly
   */
  public void complexTypesTest() throws Exception {
    // Arrange
    JolieSubProcess server = new JolieSubProcess(jpf + "/marshalling/complexTypesServer.ol", defaultArgs);
    JolieSubProcess client = new JolieSubProcess(jpf + "/marshalling/complexTypesClient.ol", defaultArgs);

    // Act 
    server.start();
    Thread.sleep(1000);

    DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);

    Introspectable intro = conn.getRemoteObject("net.jolie.complex", "/complexServer", Introspectable.class);
    String data = intro.Introspect();

    assertEquals(data, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<node name=\"/complexServer\">"
            + "<interface name=\"net.jolie.complex\">"
            + "<method name=\"testParams\">"
            + "<arg direction=\"in\" name=\"longMapArray\" type=\"aa{sx}\"/>"
            + "<arg direction=\"in\" name=\"intArray\" type=\"ai\"/>"
            + "<arg direction=\"in\" name=\"boolMap\" type=\"a{sb}\"/>"
            + "<arg direction=\"in\" name=\"stringValue\" type=\"s\"/>"
            + "<arg direction=\"in\" name=\"intValue\" type=\"i\"/>"
            + "<arg direction=\"out\" name=\"longMapArray\" type=\"aa{sx}\"/>"
            + "<arg direction=\"out\" name=\"intArray\" type=\"ai\"/>"
            + "<arg direction=\"out\" name=\"boolMap\" type=\"a{sb}\"/>"
            + "<arg direction=\"out\" name=\"stringValue\" type=\"s\"/>"
            + "<arg direction=\"out\" name=\"intValue\" type=\"i\"/>"
            + "</method>"
            + "<method name=\"onewaymethod\">"
            + "<annotation name=\"org.freedesktop.DBus.Method.NoReply\" value=\"true\"/>"
            + "<arg direction=\"in\" type=\"i\"/>"
            + "</method></interface>"
            + "<interface name=\"org.freedesktop.DBus.Introspectable\">"
            + "<method name=\"Introspect\"><arg direction=\"out\" type=\"s\"/></method>"
            + "</interface>"
            + "</node>");

    client.start();
    client.join();

    // Assert
    assertEquals("PassedPassedPassedPassedPassedPassedPassedPassedPassedPassed", client.getOutput());
    assertEquals("PassedPassedPassedPassedPassedPassedPassedPassedPassedPassed", server.getOutput());
  }

  @Test
  /*
   * Checks that complex data types are handled correctly, even though their datatype is undefined / variant
   */
  public void variantTest() throws Exception {
    // Arrange
    JolieSubProcess server = new JolieSubProcess(jpf + "/marshalling/variantServer.ol", defaultArgs);
    JolieSubProcess client = new JolieSubProcess(jpf + "/marshalling/variantClient.ol", defaultArgs);

    // Act 
    server.start();
    Thread.sleep(1000);

    DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);

    Introspectable intro = conn.getRemoteObject("net.jolie.variant", "/variantServer", Introspectable.class);
    String data = intro.Introspect();

    assertEquals(data, "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<node name=\"/variantServer\">"
            + "<interface name=\"net.jolie.variant\">"
            + "<method name=\"testVariant\">"
            + "<arg direction=\"in\" name=\"longMapArray\" type=\"aa{sv}\"/>"
            + "<arg direction=\"in\" name=\"vArray\" type=\"av\"/>"
            + "<arg direction=\"in\" name=\"intValue\" type=\"v\"/>"
            + "<arg direction=\"out\" name=\"longMapArray\" type=\"aa{sv}\"/>"
            + "<arg direction=\"out\" name=\"vArray\" type=\"av\"/>"
            + "<arg direction=\"out\" name=\"intValue\" type=\"v\"/>"
            + "</method>"
            + "</interface>"
            + "<interface name=\"org.freedesktop.DBus.Introspectable\">"
            + "<method name=\"Introspect\">"
            + "<arg direction=\"out\" type=\"s\"/>"
            + "</method>"
            + "</interface>"
            + "</node>");

    client.start();
    client.join();

    // Assert
    assertEquals("PassedPassedPassedPassedPassedPassedPassed", client.getOutput());
    assertEquals("PassedPassedPassedPassedPassedPassedPassed", server.getOutput());
  }

  @Test
  /*
   * Check that Jolie can send a message to Java with multiple parameters
   */
  public void jolieToJavaTest() throws Exception {
    DBusConnection conn = DBusConnection.getConnection(DBusConnection.SESSION);
    conn.requestBusName("net.jolie.test");

    conn.exportObject("/Test", new JolieToJava() {
      @Override
      public String concat(String s1, String s2) {
        assertEquals(s1, "Hello");
        assertEquals(s2, "World");
        return s1 + s2;
      }

      @Override
      public boolean isRemote() {
        return false;
      }
    });

    JolieSubProcess client = new JolieSubProcess(jpf + "/marshalling/jolieToJava.ol", defaultArgs);
    client.start();
    client.join();

    assertEquals("HelloWorld", client.getOutput());
  }
}