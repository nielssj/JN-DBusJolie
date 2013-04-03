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

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import jolie.Interpreter;
import jolie.net.ports.OutputPort;
import jolie.runtime.FaultException;
import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import org.freedesktop.dbus.BusAddress;
import org.freedesktop.dbus.Message;
import org.freedesktop.dbus.MethodCall;
import org.freedesktop.dbus.MethodReturn;
import org.freedesktop.dbus.Error;
import org.freedesktop.dbus.Transport;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.UInt64;
import org.freedesktop.dbus.exceptions.DBusException;

public class DBusCommChannel extends CommChannel
{
   Transport transport;
   String uniqueName;
   // Values: Dbus message serial
   // Key: Jolie message id
   HashMap<Long, Long> messages;
   
   OutputPort port;   
   private String outputConnectionName;
   private String outputObjectPath;
  
	public DBusCommChannel( URI location, OutputPort port)
		throws IOException, ParseException, DBusException
	{
		super( );
    
    String[] parts = DBusLocationParser.parse(location.getPath());
    this.outputConnectionName = parts[0];
    this.outputObjectPath = parts[1];
    
    System.out.printf("connectionName %s \n", this.outputConnectionName);
    System.out.printf("objectpath %s \n", this.outputObjectPath);
    
    this.port = port;
    messages = new HashMap<Long, Long>();
    
    // Initialize D-Bus connection
    BusAddress address = new BusAddress(
        System.getenv("DBUS_SESSION_BUS_ADDRESS"));
    this.transport = new Transport(address);

    Message m = new MethodCall("org.freedesktop.DBus", "/",
    "org.freedesktop.DBus", "Hello", (byte) 0, null);
    this.transport.mout.writeMessage(m);
    MethodReturn response = (MethodReturn) this.transport.min.readMessage();
    this.uniqueName = (String) response.getParameters()[0];
    System.out.println("My unique name is: "+this.uniqueName);
	}
  
  private String nativeValueToDBusString(Value value) {
    if (value.isBool()) {
      return "b";
    } else if (value.isInt()) {
      return "u";
    } else if (value.isString()) {
      return "s";
    }
    
    return "";
  }
  
	private String valueVectorToDBusString( ValueVector vector )
	{
		if ( vector.size() > 1 ) {
      String arrType = this.valueToDBusString(vector.first());
      return "a("+arrType+")";
		} else {
			return this.valueToDBusString( vector.first());
		}
	}
  
  private String valueToDBusString(Value value) {
    StringBuilder typeString = new StringBuilder();
    
    if ( value.children().isEmpty() ) {
			if ( value.isDefined() ) {
				typeString.append( this.nativeValueToDBusString( value ) );
			}
		} else {
			int size = value.children().size();
			int i = 0;
			for( Entry< String, ValueVector > child : value.children().entrySet() ) {
				if ( child.getValue().isEmpty() == false ) {
					typeString.append(this.valueVectorToDBusString( child.getValue() ));
				}
			}
		}	
    
    return typeString.toString();
  }
  
  private Object[] valueToObjectArray(Value value) {
    ArrayList<Object> objects = new ArrayList<Object>();
    
    if ( value.children().isEmpty() ) {
			if ( value.isDefined() ) {
        objects.add( this.nativeValueToObject( value ) );
			}
		} else {
			for( Entry< String, ValueVector > child : value.children().entrySet() ) {
				if ( child.getValue().isEmpty() == false ) {
					objects.add(this.valueVectorToObject( child.getValue() ));
				}
			}
		}	
    
    return objects.toArray();
  }
  
  private Object valueVectorToObject(ValueVector vector) {
    if (vector.size() > 1) {
      ArrayList<Object> objects = new ArrayList<Object>();
      
      for( int i = 0; i < vector.size(); i++ ) { 
        objects.add(this.valueToObjectArray(vector.get(i)));
      }
      return objects.toArray();
    } else {
      Value first = vector.first();
      if (first.children().isEmpty()) {
         return this.nativeValueToObject(first);
      } else {
         return this.valueToObjectArray(vector.first());
      }      
    }
  }
  
  private Object nativeValueToObject(Value value) {
    if (value.isBool()) {
      return value.boolValue();
    } else if (value.isInt()) {
      return value.intValue();
    } else if (value.isString()) {
      return value.strValue();
    } else {
      return null;
    }
  }
  
  private void printArray(Object[] arr) {
    System.out.println("-- Begin print array");
    for (Object o : arr) {
      System.out.printf("arr %s \n", o);
    }
    System.out.println("-- End print array");
  }
  
  protected void sendImpl( CommMessage message )
		throws IOException
	{
    System.out.println("sendimpl");
    System.out.printf("operationname %s\n", message.operationName());
    
    Object[] values = this.valueToObjectArray(message.value());
    String typeString = this.valueToDBusString(message.value());
    
    this.printArray(values);
    System.out.printf("typestring %s \n", typeString);
    
    MethodCall m;
    try {
       m = new MethodCall(
            this.outputConnectionName,
            this.outputObjectPath,
            null,
            message.operationName(), 
            (byte) 0, 
            typeString,
            values
           );
    } catch (DBusException e) {
      System.out.println("DBus Exception in sendimpl");
      System.out.println(e);
      throw new IOException(e);
    }
    
    this.messages.put(message.id(), m.getSerial());
    this.transport.mout.writeMessage(m);
	}
	
	protected CommMessage recvImpl()
		throws IOException
	{
    System.out.println("recvimpl");
                // TODO: Implement?
		return null;
	}
	
	protected void closeImpl()
		throws IOException
	{
    this.transport.disconnect();
	}

	public synchronized boolean isReady()
		throws IOException
	{
		// TODO: Implement?
		return true;
	}
	
	@Override
	public void disposeForInputImpl()
		throws IOException
	{
		// TODO: Implement?
		Interpreter.getInstance().commCore().registerForPolling( this );
	}
  
  private Value DBusToJolieValue(Object[] val, String signature) {
    if (val == null || val.length == 0) {
      return Value.UNDEFINED_VALUE;
    } else {
      Object v = val[0];
      
      if (v instanceof UInt32) {
        UInt32 i = (UInt32) v;
        return Value.create(i.intValue());
      } else if (v instanceof UInt16) {
        UInt16 i = (UInt16) v;
        return Value.create(i.intValue());
      } else if (v instanceof UInt64) {
        UInt64 i = (UInt64) v;
        return Value.create(i.intValue());
      } else if (v instanceof String) {
        return Value.create((String) v);
      } else if (v instanceof Boolean) {
        return Value.create((Boolean) v);
      }else {
        throw new RuntimeException("Cannot translate DBus response to Jolie");
      }
    }
  }

  @Override
  public CommMessage recvResponseFor(CommMessage request) throws IOException {
    Long requestSerial = this.messages.get(request.id());
    
    // Read response
    System.out.println("recvResponsefor");
    System.out.printf("reqest.operationName %s \n", request.operationName());
    
    Message m;
    try {
      while(true)
      {
          m = this.transport.min.readMessage();
          if (m != null) {
            if (m instanceof MethodReturn) {
                MethodReturn resp = (MethodReturn)m;
                if (requestSerial == resp.getReplySerial()) {
                  Value v = this.DBusToJolieValue(resp.getParameters(), resp.getSig());
                  return CommMessage.createResponse(request, v);
                } else {
                  System.out.println("Wrong serial");
                  System.out.println(resp);
                }
            } else if (m instanceof Error) {
              Error err = (Error) m;
              Object[] parameters = err.getParameters();
              
              return CommMessage.createFaultResponse(
                      request, 
                      new FaultException(err.getName(), (parameters != null && parameters.length > 0) ? (String) parameters[0] : "")
                    );
            } else {
              System.out.println("Message was not methodreturn");
              System.out.println(m);
            }
          } else {
            System.out.println("Message was null :(");
          }
         
      }
    } catch (DBusException e) {
      System.out.println("DBus Exception in sendimpl");
      System.out.println(e);
      throw new IOException(e);
    }
  }
}
