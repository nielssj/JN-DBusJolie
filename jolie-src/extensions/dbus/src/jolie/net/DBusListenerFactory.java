package jolie.net;

import java.io.IOException;
import jolie.Interpreter;
import jolie.net.ext.CommListenerFactory;
import jolie.net.ext.CommProtocolFactory;
import jolie.net.ports.InputPort;
import jolie.runtime.AndJarDeps;

@AndJarDeps({"unix.jar", "dbus-2.7.jar", "hexdump-0.2.jar"})
public class DBusListenerFactory extends CommListenerFactory {

  public DBusListenerFactory(CommCore commCore) {
    super(commCore);
  }

  public CommListener createListener(
          Interpreter interpreter,
          CommProtocolFactory protocolFactory,
          InputPort inputPort)
          throws IOException {
    return new DBusListener(interpreter, inputPort);
  }
}
