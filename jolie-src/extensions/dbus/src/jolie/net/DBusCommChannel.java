/**
 * *************************************************************************
 * Copyright (C) by Fabrizio Montesi * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU Library General Public License as * published by the Free
 * Software Foundation; either version 2 of the * License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY;
 * without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU
 * Library General Public * License along with this program; if not, write to the * Free Software Foundation, Inc., * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. * * For details about the
 * authors of this software, see the AUTHORS file. * *************************************************************************
 */
package jolie.net;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.*;

import jolie.Interpreter;
import jolie.runtime.FaultException;
import jolie.runtime.Value;

import org.freedesktop.dbus.Message;
import org.freedesktop.dbus.MethodCall;
import org.freedesktop.dbus.MethodReturn;
import org.freedesktop.dbus.Error;
import org.freedesktop.dbus.Transport;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.exceptions.DBusException;

import javax.xml.parsers.ParserConfigurationException;
import jolie.net.ports.Port;
import org.xml.sax.SAXException;

public class DBusCommChannel extends CommChannel {

  private static final Logger log = Logger.getLogger("jolie.net.dbus");
  private static boolean TRACE = false;
  protected final Transport transport;
  private URI location;
  private String uniqueName;
  private final String connectionName;
  private final String objectPath;
  private final boolean isInputPort;
  // Inputports are always introspectable, outputports are determined by calling introspec
  private boolean isIntrospectable = true;
  // Detected messages waiting to be scheduled for execution by CommCore
  private ConcurrentLinkedQueue<Long> inputQueue;
  // Messages being executed or waiting to be, indexed by D-Bus serial
  private ConcurrentHashMap<Long, Message> messages;
  private ConcurrentHashMap<Long, Message> sentMessages;
  // Outputport interface as an introspection (XML) string
  protected Object[] introspectionOutput;
  private final DBusIntrospector introspector;

  public DBusCommChannel(Transport transport, String connectionName, String objectPath, URI location, boolean isInputPort, Port port)
          throws IOException, ParseException, DBusException, ParserConfigurationException, SAXException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    super();

    log.info(String.format("DBusCommChannel - Constructing channel:%s", System.nanoTime()));

    this.transport = transport;
    this.location = location;
    this.connectionName = connectionName;
    this.objectPath = objectPath;
    this.inputQueue = new ConcurrentLinkedQueue<Long>();
    this.messages = new ConcurrentHashMap<Long, Message>();
    this.sentMessages = new ConcurrentHashMap<Long, Message>();

    this.isInputPort = isInputPort;

    // Prepare/Retreive introspection data (InputPort/OutputPort)
    this.introspector = new DBusIntrospector(objectPath, connectionName, this);
    if (this.isInputPort) {
      log.info(String.format("DBusCommChannel - Preparing introspection data:%s", System.nanoTime()));
      this.introspectionOutput = this.introspector.setIntrospectOutput(port.getInterface());
    } else {
      log.info(String.format("DBusCommChannel - Retreiving introspection data:%s", System.nanoTime()));
      this.isIntrospectable = this.introspector.IntrospectInput();
    }

    this.setToBeClosed(false); // Enable channel persistance

    log.info(String.format("DBusCommChannel - Channel constructed:%s", System.nanoTime()));
  }

  // Attempt to reserve a name in DBus daemon
  public boolean obtainName(String name) throws DBusException, IOException {
    log.info(String.format("obtainName - Obtaining unique name:%s", System.nanoTime()));

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
          log.info(String.format("obtainName - Obtaining unique name:%s", System.nanoTime()));
          return true;
        }

        // Failed to obtain name, the requested name was probably already reserved
        return false;
      } else if (m instanceof Error) {
        // Failed to obtain name, the requested name was probably malformed
        return false;
      }
    }
  }

  // Blocking: Listen untill a message is received
  public boolean listen() throws IOException, DBusException {
    log.fine("listen - Called");

    Message m = transport.min.readMessage();

    if (m.getName().equals("Introspect")) {
      this.respondToIntrospection(m);
      return false;
    }

    Long s = m.getSerial();
    if (m instanceof MethodCall && !this.inputQueue.contains(s)) {
      this.messages.put(s, m);
      this.inputQueue.add(s);
      return true;
    }

    return false;
  }

  private void respondToIntrospection(Message m) throws IOException, DBusException {
    log.fine("respondToIntrospection - Returning auto-generated introspection string");
    Message mr = new MethodReturn((MethodCall) m, "s", this.introspectionOutput);
    this.transport.mout.writeMessage(mr);
  }

  // Blocking: Listen untill a message with a specific reply serial is received.
  protected Message listenFor(Long serial) throws IOException, DBusException {
    while (true) {
      if (this.inputQueue.remove(serial)) {
        return this.messages.get(serial);
      } else {
        Message m;
        synchronized (this.transport.min) {
          log.info("listenFor - Thread is starting to listen");
          m = this.transport.min.readMessage();
        }
        log.info("listenFor - readMessage terminated");

        if (m != null && (m instanceof MethodReturn || m instanceof Error)) {
          log.info("listenFor - A message was found");
          Long s = m.getReplySerial();
          if (serial.equals(s)) {
            return m;
          } else {
            this.messages.put(s, m);
            this.inputQueue.add(s);
          }
        }
      }
    }
  }

  // Send message: Sends message and then returns (OutputPort/InputPort)
  protected void sendImpl(CommMessage message) throws IOException {
    log.info(String.format("sendImpl - Called:%s", System.nanoTime()));
    log.fine(String.format("sendImpl - Operation name: %s", message.operationName()));

    long id = message.id();
    StringBuilder builder = new StringBuilder();

    Message m;
    try {
      Object[] values;
      String typeString;

      if (isInputPort) {
        // Response to method call (InputPort)
        if (!message.isFault()) {
          typeString = this.introspector.signatures.get(message.operationName());
          String[] argNames = this.introspector.responseArgs.get(message.operationName());
          values = DBusMarshalling.valueToDBus(message.value(), argNames);

          m = new MethodReturn(
                  (MethodCall) messages.remove(message.id()),
                  typeString,
                  values);
        } else {
          m = new Error(
                  messages.remove(message.id()),
                  message.fault());
        }
      } else {
        // Outgoing method call (OutputPort)
        if (this.isIntrospectable) {
          typeString = this.introspector.signatures.get(message.operationName());
          String[] argNames = this.introspector.requestArgs.get(message.operationName());
          values = DBusMarshalling.valueToDBus(message.value(), argNames);
        } else {
          values = DBusMarshalling.valueToDBus(message.value(), builder);
          typeString = builder.toString();
        }

        m = new MethodCall(
                this.connectionName,
                this.objectPath,
                null,
                message.operationName(),
                (byte) 0,
                typeString,
                values);
        sentMessages.put(message.id(), m);
      }
    } catch (DBusException e) {
      log.log(Level.SEVERE, "sendimpl - Exception from dbus-java binding", e);
      throw new IOException(e);
    }

    log.fine(String.format("sendImpl - Sending:%s", System.nanoTime()));
    synchronized (this.transport.mout) {
      this.transport.mout.writeMessage(m);
    }
    log.fine(String.format("sendImpl - Sent:%s", System.nanoTime()));

    log.info("sendImpl - Returned succesfully");
  }

  // Receive message: Incomming calls (InputPort)
  protected CommMessage recvImpl() throws IOException {
    log.info("recvImpl - Called");

    try {
      if (!inputQueue.isEmpty()) {
        // Pulling a D-Bus message from queue
        Message msg = this.messages.get(this.inputQueue.poll());

        if (msg instanceof MethodCall) {
          // Method call found, marshalling to Jolie CommMessage
          Value val = DBusMarshalling.ToJolieValue(msg.getParameters(), msg.getSig(), this.introspector.requestArgs.get(msg.getName()));
          CommMessage cmsg = new CommMessage(msg.getSerial(), msg.getName(), "/", val, null);

          log.info("recvImpl - Returned succesfully");
          return cmsg;
        } else {
          log.severe("recvImpl - Unsupported message type");
          throw new RuntimeException("recvImpl - Unsupported message type");
        }
      } else {
        log.severe("recvImpl - Input message queue was empty");
        throw new RuntimeException("recvImpl - Input message queue was empty"); // This should only happen if we have a bug
      }
    } catch (DBusException ex) {
      throw new IOException(ex);
    }
  }

  // Receive message: Incomming responses (OutputPort)
  @Override
  public CommMessage recvResponseFor(CommMessage request) throws IOException {
    log.info(String.format("recvResponseFor - Called:%s", System.nanoTime()));
    log.fine(String.format("recvResponseFor - Operation name: %s", request.operationName()));

    // Fetch matching call to get D-Bus serial
    MethodCall call = (MethodCall) sentMessages.remove(request.id());
    try {
      // Looking for response in input transport
      Message msg = listenFor(call.getSerial());
      log.info(String.format("recvResponseFor - Found matching response:%s", System.nanoTime()));

      if (msg instanceof MethodReturn) {
        // Success response found, marshalling to Jolie Reqsponses
        Value val = DBusMarshalling.ToJolieValue(msg.getParameters(), msg.getSig(), this.introspector.responseArgs.get(request.operationName()));

        log.info(String.format("recvResponseFor - Returned succesfully:%s", System.nanoTime()));
        return CommMessage.createResponse(request, val);
      } else if (msg instanceof Error) {
        // Error response found, marshalling to Jolie FaultResponse
        Object[] parameters = msg.getParameters();

        log.info("recvResponseFor - Returned fault response");
        return CommMessage.createFaultResponse(request,
                new FaultException(msg.getName(), (parameters != null && parameters.length > 0) ? (String) parameters[0] : ""));
      }

      log.warning(String.format("recvResponseFor - Not a supported response type, continuing to look in input transport: %s", msg));
      return null;

    } catch (DBusException e) {
      log.log(Level.SEVERE, "recvResponseFor - DBusException while reading input transport", e);
      throw new IOException(e);
    }
  }

  protected void closeImpl() throws IOException {
    if (!isInputPort) {
      this.disconnect();
    }
  }

  public void disconnect() throws IOException {
    this.transport.disconnect();
  }

  public synchronized boolean isReady() throws IOException {
    return true;
  }

  private void _releaseImpl() throws IOException {
    Interpreter.getInstance().commCore().putPersistentChannel(this.location, "", this);
  }

  @Override
  protected void releaseImpl()
          throws IOException {
    if (lock.isHeldByCurrentThread()) {
      _releaseImpl();
    } else {
      lock.lock();
      try {
        _releaseImpl();
      } finally {
        lock.unlock();
      }
    }
  }
}
