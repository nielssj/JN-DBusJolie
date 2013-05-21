/**
 * *************************************************************************
 * Copyright (C) by Fabrizio Montesi * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU Library General Public License as * published by the Free
 * Software Foundation; either version 2 of the * License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY;
 * without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU
 * Library General Public * License along with this program; if not, write to the * Free Software Foundation, Inc., * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. * * For details about the
 * authors of this software, see the AUTHORS file. * *************************************************************************
 */
package jolie.net;

import jolie.net.ports.OutputPort;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.text.ParseException;
import java.util.logging.Logger;
import jolie.net.ext.CommChannelFactory;
import jolie.net.ports.InputPort;
import jolie.net.ports.Port;
import jolie.runtime.AndJarDeps;
import org.freedesktop.dbus.BusAddress;
import org.freedesktop.dbus.Message;
import org.freedesktop.dbus.MethodCall;
import org.freedesktop.dbus.Transport;
import org.freedesktop.dbus.exceptions.DBusException;

@AndJarDeps({"unix.jar", "dbus-2.7.jar", "hexdump-0.2.jar"})
public class DBusCommChannelFactory extends CommChannelFactory {

  private static final Logger log = Logger.getLogger("jolie.net.dbus");
  
  public DBusCommChannelFactory(CommCore commCore) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    super(commCore);

    log.setUseParentHandlers(false);
    System.setProperty("java.library.path", "/usr/local/lib/jni");

    Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
    fieldSysPath.setAccessible(true);
    fieldSysPath.set(null, null);
  }

  public static DBusCommChannel createChannel(URI location, InputPort port) throws DBusException, IOException {
    log.info(String.format("createChannel - Creating channel for InputPort:%s", System.nanoTime()));
      
    String[] parts = DBusCommChannelFactory.parseLocation(location.getSchemeSpecificPart());
    String connectionName = parts[0]; 
    String objectPath = parts[1];

    DBusCommChannel channel = DBusCommChannelFactory.create(location, connectionName, objectPath, true, port);

    boolean nameObtained = channel.obtainName(connectionName);
    if (!nameObtained) {
      log.severe(String.format("Could not obtain bus name '%s' because it was already in use or malformed", connectionName));
      throw new IOException(String.format("Could not obtain bus name '%s' because it was already in use or malformed", connectionName));
    }

    return channel;
  }

  public CommChannel createChannel(URI location, OutputPort port) throws IOException {
    log.info(String.format("createChannel - Creating channel for OutputPort:%s", System.nanoTime()));
    
    String[] parts = DBusCommChannelFactory.parseLocation(location.getPath());
    String connectionName = parts[0];
    String objectPath = parts[1];

    DBusCommChannel channel = DBusCommChannelFactory.create(location, connectionName, objectPath, false, port);
    return channel;
  }

  private static DBusCommChannel create(URI location, String connectionName, String objectPath, boolean isInputPort, Port port) {
    DBusCommChannel ret = null;
    Transport transport;
    try {
      BusAddress address = new BusAddress(System.getenv("DBUS_SESSION_BUS_ADDRESS")); // TODO: Move to location (SESSION/SYSTEM)
      transport = new Transport();
      transport.connect(address, 1000);

      // Obtain DBus ID
      Message m = new MethodCall("org.freedesktop.DBus", "/",
              "org.freedesktop.DBus", "Hello", (byte) 0, null);
      transport.mout.writeMessage(m);
      m = transport.min.readMessage();
    } catch (ParseException ex) {
      throw new RuntimeException("Failed to parse BusAddress", ex);
    } catch (DBusException ex) {
      throw new RuntimeException("Failed to register service in dbus", ex);
    } catch (IOException ex) {
      throw new RuntimeException("Failed to create transport in dbus", ex);
    }

    try {
      ret = new DBusCommChannel(transport, connectionName, objectPath, location, isInputPort, port);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to create DBusCommChannel "+ex.getMessage(), ex);
    }

    return ret;
  }

  private static String[] parseLocation(String location) {
    // Remove leading slash
    String path = location.substring(1);
    String[] parts = path.split(":", 2);

    if (parts != null) {
      if (parts.length == 2) {
        return parts;
      } else {
        throw new RuntimeException("Malformed location string, should be dbus:/[bus name]:[object path] but was: " + location);
      }
    } else {
      throw new RuntimeException("Malformed location string, should be dbus:/[bus name]:[object path] but was: " + location);
    }
  }
}
