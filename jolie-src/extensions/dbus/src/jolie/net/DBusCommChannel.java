/**
 * *************************************************************************
 * Copyright (C) by Fabrizio Montesi * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU Library General Public License as * published by the Free
 * Software Foundation; either version 2 of the * License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY;
 * without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU
 * Library General Public * License along with this program; if not, write to the * Free Software Foundation, Inc., * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. * * For details about the
 * authors of this software, see the AUTHORS file. *
 **************************************************************************
 */
package jolie.net;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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

public class DBusCommChannel extends CommChannel {

    private Transport transport;
    String uniqueName;
    OutputPort port;
    private String connectionName;
    private String objectPath;
    
    // Messages being executed indexed by Jolie message id
    // Values: Dbus message serial
    // Key: Jolie message id
    ConcurrentHashMap<Long, Message> messages;  
    
    // Detected messages waiting to be scheduled for execution by CommCore
    ConcurrentLinkedQueue<Message> inputQueue;  
    
    
    // Constructor: Save details and instantiate collections
    public DBusCommChannel(Transport transport, String connectionName, String objectPath, URI location)
            throws IOException, ParseException, DBusException {
        super();

        this.transport = transport;
        this.connectionName = connectionName;
        this.objectPath = objectPath;
        this.messages = new ConcurrentHashMap<Long, Message>();
        this.inputQueue = new ConcurrentLinkedQueue<Message>();

        System.out.printf("CommChannel init - ConnectionName: %s \n", this.connectionName);
        System.out.printf("CommChannel init - Objectpath: %s \n", this.objectPath);
    }
    
    // Attempt to reserve a name in DBus daemon
    public boolean obtainName(String name) throws DBusException, IOException {
        // Send name request
        Message m = new MethodCall("org.freedesktop.DBus", "/",
                "org.freedesktop.DBus", "RequestName", (byte) 0,
                "su", name, 0);
        this.transport.mout.writeMessage(m);

        // Receive response
        while (true) {
            m = this.transport.min.readMessage();
            
            if (m instanceof MethodReturn) {
                UInt32 ret = (UInt32) m.getParameters()[0];

                if (ret.intValue() == 1) {
                    return true;
                }

                return false;
            }
        }
    }

    // Blocking: check input stream and add to queue
    public boolean checkInput() throws IOException, DBusException {
        Message m = transport.min.readMessage();

        if (m != null && !this.inputQueue.contains(m)) {
            System.out.println("New message received: " + m);
            this.inputQueue.add(m);
            return true;
        }

        return false;
    }
    
    // Send message: Calls and returns (OutputPort/InputPort)
    protected void sendImpl(CommMessage message) throws IOException {
        System.out.println("sendimpl - Called");
        System.out.printf("sendimpl - Operationname: %s\n", message.operationName());

        long id = message.id();
        Object[] values = DBusMarshalling.valueToObjectArray(message.value());
        String typeString = DBusMarshalling.valueToDBusString(message.value());

        DBusMarshalling.printArray(values);
        System.out.printf("sendimpl - Typestring: %s \n", typeString);
        
        Message m;
        try {
            if(messages.containsKey(id))
            {
                // Response to method call (InputPort)
                MethodCall imsg = (MethodCall)messages.get(id);
                if(!message.isFault())
                {
                    m = new MethodReturn(imsg, typeString, values);
                }
                else
                {
                    m = new Error(imsg, message.fault());
                }
               messages.remove(id);
            }
            else
            {
                // Outgoing method call (OutputPort)
                m = new MethodCall(
                    this.connectionName,
                    this.objectPath,
                    null,
                    message.operationName(),
                    (byte) 0,
                    typeString,
                    values);
                
                this.messages.put(message.id(), m);
            }
        } catch (DBusException e) {
            System.out.println("DBus Exception in sendimpl");
            System.out.println(e);
            throw new IOException(e);
        }
        
        this.transport.mout.writeMessage(m);
    }

    // Receive message: Incomming responses (TODO) and incomming calls (OutputPort/InputPort)
    protected CommMessage recvImpl() throws IOException {
        System.out.println("recvimpl - Called");
        
        try {
            if (!inputQueue.isEmpty()) {
                Message msg = this.inputQueue.poll();
                System.out.printf("recvimpl - Pulled D-Bus message from queue: %s\n", msg);
                
                if (msg instanceof MethodCall) {
                    System.out.printf("recvimpl - Marshalling to Jolie CommMessage: %s\n", msg);
                    
                    MethodCall call = (MethodCall) msg;
                    Value val = DBusMarshalling.ToJolieValue(call.getParameters(), call.getSig());
                    
                    CommMessage cmsg = CommMessage.createRequest(msg.getName(), "/", val);
                    this.messages.put(cmsg.id(), msg);
                    
                    System.out.printf("recvimpl - Marshalled returning CommMessage to CommCore: %s\n", cmsg);
                    return cmsg;
                } else {
                    throw new RuntimeException("recvimpl - Unsupported message type");
                }
            } else {
                throw new RuntimeException("recvimpl - Input message queue was empty");
            }
        } catch (DBusException ex) {
            throw new IOException(ex);
        }
    }

    // Receive message: Reponse from calls (TODO: change so that it utilizes checkInput and recvImpl instead)
    @Override
    public CommMessage recvResponseFor(CommMessage request) throws IOException {
        Long requestSerial = this.messages.get(request.id()).getSerial();

        // Read response
        System.out.println("recvResponsefor");
        System.out.printf("reqest.operationName %s \n", request.operationName());

        Message m;
        try {
            while (true) {
                m = this.transport.min.readMessage();
                if (m != null) {
                    if (m instanceof MethodReturn) {
                        MethodReturn resp = (MethodReturn) m;
                        if (requestSerial == resp.getReplySerial()) {
                            Value v = DBusMarshalling.ToJolieValue(resp.getParameters(), resp.getSig());
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
                                new FaultException(err.getName(), (parameters != null && parameters.length > 0) ? (String) parameters[0] : ""));
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

    protected void closeImpl() throws IOException {
        this.transport.disconnect();
    }

    public synchronized boolean isReady() throws IOException {
        // TODO: Implement?
        return true;
    }

    @Override
    public void disposeForInputImpl() throws IOException {
        // TODO: Implement?
        Interpreter.getInstance().commCore().registerForPolling(this);
    }
}
