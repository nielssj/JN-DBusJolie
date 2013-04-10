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
    private boolean isInputPort;
    
    // Detected messages waiting to be scheduled for execution by CommCore
    ConcurrentLinkedQueue<Long> inputQueue;  
    // Messages being executed or waiting to be, indexed by D-Bus serial
    ConcurrentHashMap<Long, Message> messages;
    
    
    // Constructor: Save details and instantiate collections
    public DBusCommChannel(Transport transport, String connectionName, String objectPath, URI location, boolean isInputPort)
            throws IOException, ParseException, DBusException {
        super();

        this.transport = transport;
        this.connectionName = connectionName;
        this.objectPath = objectPath;
        this.inputQueue = new ConcurrentLinkedQueue<Long>();
        this.messages = new ConcurrentHashMap<Long, Message>();
        this.isInputPort = isInputPort;

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
        Long s = m.getSerial();
        
        if (!this.inputQueue.contains(s)) {
            this.messages.put(s, m);
            this.inputQueue.add(s);
            return true;
        } 

        return false;
    }
    
    private Message checkInputSpecific(Long serial) throws IOException, DBusException {
        if(this.inputQueue.remove(serial)) {
            return this.messages.get(serial);
        } else {
            while(true) {
                Message m = transport.min.readMessage();
                String so = m.getSource();
                Long s = m.getReplySerial();
                if(m instanceof MethodReturn) {
                    return m;
                } else {
                    this.messages.put(s, m);
                    this.inputQueue.add(s);
                }
            } 
        }
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
            if(isInputPort)
            {
                // Response to method call (InputPort)
                if(!message.isFault())
                {
                    m = new MethodReturn(
                        (MethodCall)messages.remove(message.id()), 
                        typeString, 
                        values);
                }
                else
                {
                    m = new Error(
                        messages.remove(message.id()), 
                        message.fault());
                }
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
                Message msg = this.messages.get(this.inputQueue.poll());
                System.out.printf("recvimpl - Pulled D-Bus message from queue: %s\n", msg);
                
                if (msg instanceof MethodCall) {
                    System.out.printf("recvimpl - is MethodCall, marshalling to Jolie CommMessage: %s\n", msg);
                    
                    Value val = DBusMarshalling.ToJolieValue(msg.getParameters(), msg.getSig());
                    CommMessage cmsg = new CommMessage(msg.getSerial(), msg.getName(), "/", val, null);
                    
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

    @Override
    public CommMessage recvResponseFor(CommMessage request) throws IOException {
        System.out.println("recvResponsefor - Called");
        System.out.printf("recvResponsefor - OperationName: %s \n", request.operationName());
        
        Message msg;
        CommMessage cmsg;     
        try {
            System.out.println("recvResponsefor - Looking for response in input transport");
            while (true) 
            { 
                msg = transport.min.readMessage(); // Blocking
                
                System.out.println("recvResponsefor - Input found, checking type..");
                if (msg instanceof MethodReturn) {
                    System.out.printf("recvResponsefor - Response appears to be successful, marshalling to Jolie CommMessage: %s\n", msg);
                    Value val = DBusMarshalling.ToJolieValue(msg.getParameters(), msg.getSig());
                    return new CommMessage(msg.getSerial(), msg.getName(), "/", val, null); 
                } else if(msg instanceof Error) {
                    System.out.printf("recvResponsefor - Response appears to be an error, marshalling to Jolie CommMessage: %s\n", msg);
                    Object[] parameters = msg.getParameters();
                    return new CommMessage(msg.getSerial(), msg.getName(), "/", null, 
                            new FaultException(msg.getName(), (parameters != null && parameters.length > 0) ? (String) parameters[0] : "")); 
                }
                
                System.out.println("recvResponsefor - Not a supported response type, continuing to look in input transport!");
            }
        } catch (DBusException e) {
            System.out.printf("recvResponsefor - DBusException while reading input transport: %s\n", e);
            throw new IOException(e);
        }
    }

    protected void closeImpl() throws IOException {
        // TODO: Implement?
    }
    
    public void disconnect() throws IOException {
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
