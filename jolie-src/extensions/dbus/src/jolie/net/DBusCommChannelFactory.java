/***************************************************************************
 *   Copyright (C) by Fabrizio Montesi                                     *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Library General Public License as       *
 *   published by the Free Software Foundation; either version 2 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU Library General Public     *
 *   License along with this program; if not, write to the                 *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 *                                                                         *
 *   For details about the authors of this software, see the AUTHORS file. *
 ***************************************************************************/
package jolie.net;

import jolie.net.ports.OutputPort;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import jolie.net.ext.CommChannelFactory;
import jolie.net.ports.InputPort;
import jolie.runtime.AndJarDeps;
import org.freedesktop.dbus.BusAddress;
import org.freedesktop.dbus.Message;
import org.freedesktop.dbus.MethodCall;
import org.freedesktop.dbus.Transport;
import org.freedesktop.dbus.exceptions.DBusException;

import cx.ath.matthew.debug.Debug;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

@AndJarDeps({"unix.jar", "dbus-2.7.jar", "hexdump-0.2.jar", "debug-enable-1.1.jar"})
public class DBusCommChannelFactory extends CommChannelFactory
{
	public DBusCommChannelFactory( CommCore commCore )
	{
		super( commCore );
    try {
      File f = new File("/home/jan/src/JN-DBusJolie/jolie-src/dist/debug.conf");
      System.out.println(f.exists());
      System.out.println(f);
      Debug.loadConfig(f);
    } catch (IOException ex) {
      Logger.getLogger(DBusCommChannelFactory.class.getName()).log(Level.SEVERE, null, ex);
    }
	}
  
  public static DBusCommChannel createChannel( URI location, InputPort port ) throws DBusException, IOException {
    String[] parts = DBusLocationParser.parse(location.getSchemeSpecificPart());
    String connectionName = parts[0];
    String objectPath = parts[1];
    
    DBusCommChannel channel = DBusCommChannelFactory.create(location, connectionName, objectPath, true);
    
    boolean nameObtained = channel.obtainName(connectionName);   
    
    System.out.println("Name obtained: "+nameObtained);
    
     return channel;
  }

    public CommChannel createChannel( URI location, OutputPort port )
		throws IOException
	{
    String[] parts = DBusLocationParser.parse(location.getPath());
    String connectionName = parts[0];
    String objectPath = parts[1];
    
    DBusCommChannel channel = DBusCommChannelFactory.create(location, connectionName, objectPath, false);
    
     return channel;
	}
  
  private static DBusCommChannel create ( URI location, String connectionName, String objectPath, boolean isInputPort ) {
    DBusCommChannel ret = null;
    Transport transport;
    try {
        BusAddress address = new BusAddress(System.getenv("DBUS_SESSION_BUS_ADDRESS")); // TODO: Move to location (SESSION/SYSTEM)
        transport = new Transport(address);

        // Obtain DBus ID
        Message m = new MethodCall("org.freedesktop.DBus", "/",
        "org.freedesktop.DBus", "Hello", (byte) 0, null);
        transport.mout.writeMessage(m);
        m = transport.min.readMessage();
        System.out.println("Response to Hello is: "+m);
    }
    catch (ParseException ex)
    {
        throw new RuntimeException("Failed to parse BusAddress", ex);
    }
    catch (DBusException ex)
    {
        throw new RuntimeException("Failed to register service in dbus", ex);
    }
    catch (IOException ex)
    {
      throw new RuntimeException("Failed to create transport in dbus", ex);
    }
    		
		try {
      ret = new DBusCommChannel(transport, connectionName, objectPath, location, isInputPort);
    } catch (Exception ex) {
      throw new RuntimeException("Failed to create DBusCommChannel", ex);
    } 
    
    return ret;
  }
}
