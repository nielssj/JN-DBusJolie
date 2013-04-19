/**
 * *************************************************************************
 * Copyright (C) by Fabrizio Montesi * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU Library General Public License as * published by the Free
 * Software Foundation; either version 2 of the * License, or (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY;
 * without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU
 * Library General Public * License along with this program; if not, write to the * Free Software Foundation, Inc., * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. * * For details about the
 * authors of this software, see the AUTHORS file. * *************************************************************************
 */
package jolie.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import jolie.Interpreter;
import jolie.net.ports.OutputPort;
import jolie.runtime.FaultException;
import jolie.runtime.Value;

import org.freedesktop.dbus.Message;
import org.freedesktop.dbus.MethodCall;
import org.freedesktop.dbus.MethodReturn;
import org.freedesktop.dbus.Error;
import org.freedesktop.dbus.Transport;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.exceptions.DBusException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jolie.net.ports.Interface;
import jolie.runtime.typing.OneWayTypeDescription;
import jolie.runtime.typing.RequestResponseTypeDescription;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DBusCommChannel extends CommChannel {

  private static boolean TRACE = false;
  private Transport transport;
  String uniqueName;
  OutputPort port;
  private String connectionName;
  private String objectPath;
  private boolean isInputPort;
  // Inputports are always introspectable, outputports are determined by calling introspec
  private boolean isIntrospectable = true;
  // Detected messages waiting to be scheduled for execution by CommCore
  private ConcurrentLinkedQueue<Long> inputQueue;
  // Messages being executed or waiting to be, indexed by D-Bus serial
  private ConcurrentHashMap<Long, Message> messages;
  private ConcurrentHashMap<Long, Message> sentMessages;
  // The maps below are only updated at init, so there is no need to make them concurrent
  // Output port  - The signatures of method, aquired by calling IntrospectInput on the remote object
  // Input port   - The signatures of the return value of the method, aquired by calling setIntrospectOutput on the interface
  private Map<String, String> introspectedSignatures = new HashMap<String, String>();
  // Output port  - The names of the arguments given to remote method calls, in the order that they should appear when calling. 
  // Input port   - The names of expected arguments from remote callers
  private Map<String, String[]> requestArgs = new HashMap<String, String[]>();
  // Output port  - The names of the arguments that are expected in response to remote method calls.
  // Input port   - The names of the arguments in a method return that should be sent to remote callers
  private Map<String, String[]> responseArgs = new HashMap<String, String[]>();
  // Outputport interface as an introspection (XML) string
  Object[] introspectionOutput;

  public DBusCommChannel(Transport transport, String connectionName, String objectPath, URI location, boolean isInputPort)
          throws IOException, ParseException, DBusException, ParserConfigurationException, SAXException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    super();

    this.transport = transport;
    this.connectionName = connectionName;
    this.objectPath = objectPath;
    this.inputQueue = new ConcurrentLinkedQueue<Long>();
    this.messages = new ConcurrentHashMap<Long, Message>();
    this.sentMessages = new ConcurrentHashMap<Long, Message>();

    this.isInputPort = isInputPort;

    // Retreive introspection data (OutputPort only)
    if (!this.isInputPort) {
      this.IntrospectInput();
    }

    if (TRACE) {
      System.out.printf("CommChannel init - ConnectionName: %s \n", this.connectionName);
    }
    if (TRACE) {
      System.out.printf("CommChannel init - Objectpath: %s \n", this.objectPath);
    }
  }

  // OutputPort: Retreive and parse introspection data of the D-Bus object at the port location
  private void IntrospectInput() throws DBusException, IOException, ParserConfigurationException, SAXException {
    MethodCall m = new MethodCall(
            this.connectionName,
            this.objectPath,
            null,
            "Introspect",
            (byte) 0,
            "");

    this.transport.mout.writeMessage(m);
    Message retOrErr = this.listenSpecific(m.getSerial());
    if (retOrErr instanceof MethodReturn) {
      MethodReturn ret = (MethodReturn) retOrErr;
      String xml = (String) ret.getParameters()[0];

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder b = factory.newDocumentBuilder();

      InputStream is = new ByteArrayInputStream(xml.getBytes());
      Document d = b.parse(is);
      NodeList methods = d.getElementsByTagName("method");

      for (int i = 0; i < methods.getLength(); i++) {
        Node method = methods.item(i);

        String name = method.getAttributes().getNamedItem("name").getNodeValue();
        ArrayList<String> inputArgNames = new ArrayList<String>();
        ArrayList<String> outputArgNames = new ArrayList<String>();
        String signature = "";
        int inputArgCount = 0;
        int outputArgCount = 0;

        NodeList children = method.getChildNodes();
        boolean argsHaveNames = true;
        for (int j = 0; j < children.getLength(); j++) {
          Node child = children.item(j);

          if (child.getNodeName().equals("arg")) {
            NamedNodeMap attributes = child.getAttributes();

            Node argName = attributes.getNamedItem("name");
            if (attributes.getNamedItem("direction").getNodeValue().equals("in")) {
              signature += attributes.getNamedItem("type").getNodeValue();
              inputArgCount++;

              if (argName == null || argName.getNodeValue().equals("")) {
                argsHaveNames = false;
              } else {
                inputArgNames.add(argName.getNodeValue());
              }
            } else {
              outputArgCount++;
              if (argName == null || argName.getNodeValue().equals("")) {
                argsHaveNames = false;
              } else {
                outputArgNames.add(argName.getNodeValue());
              }
            }
          }
        }
        this.introspectedSignatures.put(name, signature);

        if (!argsHaveNames) {
          // In theory, some args may have names, and others not. In that case we default to ALL args having arg0, arg1 etc.
          inputArgNames.clear();
          outputArgNames.clear();

          for (int argNo = 0; argNo < inputArgCount; argNo++) {
            inputArgNames.add("arg" + argNo);
          }
          for (int argNo = 0; argNo < outputArgCount; argNo++) {
            outputArgNames.add("arg" + argNo);
          }
        }
        this.requestArgs.put(name, inputArgNames.toArray(new String[inputArgNames.size()]));
        this.responseArgs.put(name, outputArgNames.toArray(new String[outputArgNames.size()]));
      }
    } else {
      this.isIntrospectable = false;
    }
  }

  // InputPort: Prepare an introspection string to be returned upon incoming introspection requests.
  public void setIntrospectOutput(Interface iface) {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.newDocument();

      // Create root element with object path
      Element elmRoot = doc.createElement("node");
      elmRoot.setAttribute("name", this.objectPath);
      doc.appendChild(elmRoot);

      // Create interface element
      Element elmInterface = doc.createElement("interface");
      elmInterface.setAttribute("name", this.connectionName);
      elmRoot.appendChild(elmInterface);

      // Create req/res-method elements
      Map<String, RequestResponseTypeDescription> rros = iface.requestResponseOperations();
      for (String rroName : rros.keySet()) {
        RequestResponseTypeDescription rroDesc = rros.get(rroName);
        ArrayList<String> requestArgNames = new ArrayList<String>();
        ArrayList<String> responseArgNames = new ArrayList<String>();

        // Method root element
        Element elmMethod = doc.createElement("method");
        elmMethod.setAttribute("name", rroName);
        elmInterface.appendChild(elmMethod);

        // Request arg(s)
        Map<String, String> reqTypes = DBusMarshalling.jolieTypeToDBusString(rroDesc.requestType());
        for (String argName : reqTypes.keySet()) {
          Element elmArg = doc.createElement("arg");

          if (argName.length() > 0) {
            requestArgNames.add(argName);
            elmArg.setAttribute("name", argName); // Set name, if defined
          }

          elmArg.setAttribute("type", reqTypes.get(argName)); // Set type (D-Bus type string)
          elmArg.setAttribute("direction", "in");
          elmMethod.appendChild(elmArg);
        }

        // Response arg(s)
        Map<String, String> respTypes = DBusMarshalling.jolieTypeToDBusString(rroDesc.responseType());
        for (String argName : respTypes.keySet()) {
          Element elmArg = doc.createElement("arg");

          if (argName.length() > 0) {
            responseArgNames.add(argName);
            elmArg.setAttribute("name", argName); // Set name, if defined
          }

          elmArg.setAttribute("type", respTypes.get(argName)); // Set type (D-Bus type string)
          elmArg.setAttribute("direction", "out");
          elmMethod.appendChild(elmArg);
        }

        StringBuilder signature = new StringBuilder();
        for (String s : respTypes.values()) {
          signature.append(s);
        }

        this.introspectedSignatures.put(rroName, signature.toString());
        this.requestArgs.put(rroName, requestArgNames.toArray(new String[requestArgNames.size()]));
        this.responseArgs.put(rroName, responseArgNames.toArray(new String[responseArgNames.size()]));
      }

      // Create oneway-method elements
      Map<String, OneWayTypeDescription> owos = iface.oneWayOperations();
      for (String owoName : owos.keySet()) {
        RequestResponseTypeDescription owoDesc = rros.get(owoName);
        ArrayList<String> requestArgNames = new ArrayList<String>();

        // Method root element
        Element elmMethod = doc.createElement("method");
        elmMethod.setAttribute("name", owoName);
        elmInterface.appendChild(elmMethod);

        // Request arg(s)
        Map<String, String> reqTypes = DBusMarshalling.jolieTypeToDBusString(owoDesc.requestType());
        for (String argName : reqTypes.keySet()) {
          Element elmArg = doc.createElement("arg");

          if (argName.length() > 0) {
            requestArgNames.add(argName);
            elmArg.setAttribute("name", argName); // Set name, if defined
          }

          elmArg.setAttribute("type", reqTypes.get(argName)); // Set type (D-Bus type string)
          elmArg.setAttribute("direction", "in");
          elmMethod.appendChild(elmArg);
        }

        this.requestArgs.put(owoName, requestArgNames.toArray(new String[requestArgNames.size()]));
      }

      // Create introspectable interface
      Element elmIntroInterface = doc.createElement("interface");
      elmIntroInterface.setAttribute("name", "org.freedesktop.DBus.Introspectable");
      elmRoot.appendChild(elmIntroInterface);

      // Create introspect method
      Element elmIntroMethod = doc.createElement("method");
      elmIntroMethod.setAttribute("name", "Introspect");
      elmIntroInterface.appendChild(elmIntroMethod);

      Element elmIntroArg = doc.createElement("arg");
      elmIntroArg.setAttribute("type", "s");
      elmIntroArg.setAttribute("direction", "out");
      elmIntroMethod.appendChild(elmIntroArg);

      TransformerFactory tff = TransformerFactory.newInstance();
      Transformer tf = tff.newTransformer();
      StringWriter sw = new StringWriter();
      tf.transform(new DOMSource(doc), new StreamResult(sw));
      String introspectionString = sw.getBuffer().toString();

      if (TRACE) {
        System.out.println("Introspection string: " + introspectionString);
      }

      this.introspectionOutput = DBusMarshalling.valueToDBus(Value.create(introspectionString), new StringBuilder());
    } catch (Exception ex) {
      throw new RuntimeException("Failed to create introspection string", ex);
      // Log warning instead?
    }
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

  // Blocking: Listen untill a message is received
  public boolean listen() throws IOException, DBusException {
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
    if (TRACE) {
      System.out.println("respondToIntrospection - returning auto-generated introspection string");
    }
    Message mr = new MethodReturn((MethodCall) m, "s", this.introspectionOutput);
    this.transport.mout.writeMessage(mr);
  }

  // Blocking: Listen untill a message with a specific reply serial is received. If the serial was received earlier, return immediately
  private Message listenSpecific(Long serial) throws IOException, DBusException {
    while (true) {
      if (this.inputQueue.remove(serial)) {
        return this.messages.get(serial);
      } else {
        Message m = transport.min.readMessage();
        if (m instanceof MethodReturn || m instanceof Error) {
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

  // Send message: Calls and returns (OutputPort/InputPort)
  protected void sendImpl(CommMessage message) throws IOException {
    if (TRACE) {
      System.out.println("sendimpl - Called");
    }
    if (TRACE) {
      System.out.printf("sendimpl - Operationname: %s\n", message.operationName());
    }

    long id = message.id();
    StringBuilder builder = new StringBuilder();

    Message m;
    try {
      Object[] values;
      String typeString;

      if (isInputPort) {
        // Response to method call (InputPort)
        if (!message.isFault()) {
          typeString = this.introspectedSignatures.get(message.operationName());
          String[] argNames = this.requestArgs.get(message.operationName());
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
          typeString = this.introspectedSignatures.get(message.operationName());
          String[] argNames = this.requestArgs.get(message.operationName());
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
      if (TRACE) {
        System.out.println("DBus Exception in sendimpl");
      }
      if (TRACE) {
        System.out.println(e);
      }
      throw new IOException(e);
    }

    this.transport.mout.writeMessage(m);
  }

  // Receive message: Incomming calls (InputPort)
  protected CommMessage recvImpl() throws IOException {
    if (TRACE) {
      System.out.println("recvimpl - Called");
    }

    try {
      if (!inputQueue.isEmpty()) {
        Message msg = this.messages.get(this.inputQueue.poll());
        if (TRACE) {
          System.out.printf("recvimpl - Pulled D-Bus message from queue: %s\n", msg);
        }

        if (msg instanceof MethodCall) {
          if (TRACE) {
            System.out.printf("recvimpl - is MethodCall, marshalling to Jolie CommMessage: %s\n", msg);
          }

          Value val = DBusMarshalling.ToJolieValue(msg.getParameters(), msg.getSig(), this.responseArgs.get(msg.getName()));
          CommMessage cmsg = new CommMessage(msg.getSerial(), msg.getName(), "/", val, null);

          if (TRACE) {
            System.out.printf("recvimpl - Marshalled returning CommMessage to CommCore: %s\n", cmsg);
          }
          return cmsg;
        } else {
          throw new RuntimeException("recvimpl - Unsupported message type");
        }
      } else {
        throw new RuntimeException("recvimpl - Input message queue was empty"); // This should only happen if we have a bug
      }
    } catch (DBusException ex) {
      throw new IOException(ex);
    }
  }

  // Receive message: Incomming responses (OutputPort)
  @Override
  public CommMessage recvResponseFor(CommMessage request) throws IOException {
    if (TRACE) {
      System.out.println("recvResponsefor - Called");
    }
    if (TRACE) {
      System.out.printf("recvResponsefor - OperationName: %s \n", request.operationName());
    }

    MethodCall call = (MethodCall) sentMessages.remove(request.id());
    try {
      if (TRACE) {
        System.out.println("recvResponsefor - Looking for response in input transport");
      }
      Message msg = listenSpecific(call.getSerial());

      if (TRACE) {
        System.out.println("recvResponsefor - Input found, checking type..");
      }
      if (msg instanceof MethodReturn) {
        if (TRACE) {
          System.out.printf("recvResponsefor - Response appears to be successful, marshalling to Jolie CommMessage: %s\n", msg);
        }
        String[] tmp = this.responseArgs.get(request.operationName());
        String[] tmp2 = this.requestArgs.get(request.operationName());
        Value val = DBusMarshalling.ToJolieValue(msg.getParameters(), msg.getSig(), this.responseArgs.get(request.operationName()));
        return CommMessage.createResponse(request, val);
      } else if (msg instanceof Error) {
        if (TRACE) {
          System.out.printf("recvResponsefor - Response appears to be an error, marshalling to Jolie CommMessage: %s\n", msg);
        }
        Object[] parameters = msg.getParameters();
        return CommMessage.createFaultResponse(request,
                new FaultException(msg.getName(), (parameters != null && parameters.length > 0) ? (String) parameters[0] : ""));
      }

      if (TRACE) {
        System.out.println("recvResponsefor - Not a supported response type, continuing to look in input transport!");
      }
    } catch (DBusException e) {
      if (TRACE) {
        System.out.printf("recvResponsefor - DBusException while reading input transport: %s\n", e);
      }
      throw new IOException(e);
    }

    return null;
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
    // TODO: Implement?
    return true;
  }

  @Override
  public void disposeForInputImpl() throws IOException {
    // TODO: Implement?
    Interpreter.getInstance().commCore().registerForPolling(this);
  }
}
