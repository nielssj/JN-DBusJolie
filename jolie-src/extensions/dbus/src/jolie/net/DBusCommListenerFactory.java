package jolie.net;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Logger;
import jolie.Interpreter;
import jolie.net.ext.CommListenerFactory;
import jolie.net.ext.CommProtocolFactory;
import jolie.net.ports.InputPort;
import jolie.runtime.AndJarDeps;

@AndJarDeps({"unix.jar", "dbus-2.7.jar", "hexdump-0.2.jar"})
public class DBusCommListenerFactory extends CommListenerFactory {

  private static final Logger log = Logger.getLogger("jolie.net.dbus");

  public DBusCommListenerFactory(CommCore commCore) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    super(commCore);
    
    log.setUseParentHandlers(false);
    System.setProperty("java.library.path", "/usr/local/lib/jni");

    Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
    fieldSysPath.setAccessible(true);
    fieldSysPath.set(null, null);
  }

  public CommListener createListener(
          Interpreter interpreter,
          CommProtocolFactory protocolFactory,
          InputPort inputPort)
          throws IOException {
    return new DBusCommListener(interpreter, inputPort);
  }
}
