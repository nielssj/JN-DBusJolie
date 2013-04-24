package jolie.net;

import java.io.IOException;
import java.lang.reflect.Field;
import jolie.Interpreter;
import jolie.net.ext.CommListenerFactory;
import jolie.net.ext.CommProtocolFactory;
import jolie.net.ports.InputPort;
import jolie.runtime.AndJarDeps;

@AndJarDeps({"unix.jar", "dbus-2.7.jar", "hexdump-0.2.jar"})
public class DBusListenerFactory extends CommListenerFactory {

  public DBusListenerFactory(CommCore commCore) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    super(commCore);
    
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
    return new DBusListener(interpreter, inputPort);
  }
}
