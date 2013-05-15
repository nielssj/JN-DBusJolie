package jolie.net;

import java.io.IOException;
import jolie.Interpreter;
import jolie.net.ports.InputPort;
import org.freedesktop.dbus.exceptions.DBusException;

public class DBusCommListener extends CommListener implements Runnable {

  private InputPort port;
  private DBusCommChannel channel;

  public DBusCommListener(Interpreter interpreter, InputPort inputPort) {
    super(interpreter, inputPort);
    this.port = inputPort;
  }

  @Override
  public void shutdown() {
    try {
      if(channel != null) {
        channel.disconnect();
      }
    } catch (IOException ex) {
      throw new RuntimeException("Failed to close D-Bus comm channel", ex);
    }
  }

  @Override
  public void run() {
    // Create comm channel
    try {
      channel = DBusCommChannelFactory.createChannel(port.location(), port);
    } catch (DBusException ex) {
      throw new RuntimeException("Failed to create comm channel for InputPort "+ex.getMessage(), ex);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to create comm channel for InputPort "+ex.getMessage(), ex);
    }

    // Start listening for method calls till shutdown is called
    try {
      while (true) {
        if (channel.listen()) {
          interpreter().commCore().scheduleReceive(channel, port);
        }
      }
    } catch (DBusException ex) {
      // Transport was closed, do nothing
    } catch (Exception ex) {
      throw new RuntimeException("Unexpected failure during listening", ex);
    }
  }
}
