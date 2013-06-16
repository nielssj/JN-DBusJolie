package jolie.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jolie.net.ports.Interface;
import jolie.runtime.Value;
import jolie.runtime.typing.OneWayTypeDescription;
import jolie.runtime.typing.RequestResponseTypeDescription;

import org.freedesktop.dbus.Message;
import org.freedesktop.dbus.MethodCall;
import org.freedesktop.dbus.MethodReturn;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 *
 * @author jan
 */
public class DBusIntrospector {

  private final String objectPath;
  private final String connectionName;
  // Output port  - The signatures of remote methods, aquired by calling IntrospectInput on the remote object
  // Input port   - The signatures of the return value of the method, aquired by calling setIntrospectOutput on the interface
  protected final Map<String, String> signatures = new HashMap<String, String>();
  // Output port  - The names of the arguments given to remote method calls, in the order that they should appear when calling. 
  // Input port   - The names of expected arguments from remote callers
  protected final Map<String, String[]> requestArgs = new HashMap<String, String[]>();
  // Output port  - The names of the arguments that are expected in response to remote method calls.
  // Input port   - The names of the arguments in a method return that should be sent to remote callers
  protected final Map<String, String[]> responseArgs = new HashMap<String, String[]>();
  private final DBusCommChannel channel;

  public DBusIntrospector(String objectPath, String connectionName, DBusCommChannel channel) {
    this.objectPath = objectPath;
    this.connectionName = connectionName;
    this.channel = channel;
  }

  /*
   * InputPort: Prepare an introspection string to be returned upon incoming introspection requests.
   */
  public Object[] setIntrospectOutput(Interface iface) {
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
      elmInterface.setAttribute("name", this.connectionName.replace("-", "")); // Bus names can hyphens, but interface names cannot.  
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
        
        this.signatures.put(rroName, signature.toString());
        this.requestArgs.put(rroName, requestArgNames.toArray(new String[requestArgNames.size()]));
        this.responseArgs.put(rroName, responseArgNames.toArray(new String[responseArgNames.size()]));
      }

      // Create oneway-method elements
      Map<String, OneWayTypeDescription> owos = iface.oneWayOperations();
      for (String owoName : owos.keySet()) {
        OneWayTypeDescription owoDesc = owos.get(owoName);
        ArrayList<String> requestArgNames = new ArrayList<String>();

        // Method root element
        Element elmMethod = doc.createElement("method");
        elmMethod.setAttribute("name", owoName);
        elmInterface.appendChild(elmMethod);

        Element noReplyAnnotation = doc.createElement("annotation");
        noReplyAnnotation.setAttribute("name", "org.freedesktop.DBus.Method.NoReply");
        noReplyAnnotation.setAttribute("value", "true");
        elmMethod.appendChild(noReplyAnnotation);

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


      return DBusMarshalling.valueToDBus(Value.create(introspectionString), new StringBuilder());
    } catch (Exception ex) {
      throw new RuntimeException("Failed to create introspection string", ex);
      // Log warning instead?
    }
  }

  /*
   * OutputPort: Retrieve and parse introspection data of the D-Bus object at the port location
   */
  protected boolean IntrospectInput() throws DBusException, IOException, ParserConfigurationException, SAXException {
    MethodCall m = new MethodCall(
            this.connectionName,
            this.objectPath,
            null,
            "Introspect",
            (byte) 0,
            "");

    this.channel.transport.mout.writeMessage(m);
    Message retOrErr = this.channel.listenFor(m.getSerial());
    if (retOrErr instanceof MethodReturn) {
      MethodReturn ret = (MethodReturn) retOrErr;
      String xml = (String) ret.getParameters()[0];

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      
      DocumentBuilder b = factory.newDocumentBuilder();

      Document d = b.parse(new ByteArrayInputStream(xml.getBytes()));

      Node node = d.getElementsByTagName("node").item(0);
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
        this.signatures.put(name, signature);

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
      return true;
    } else {
      return false;
    }
  }
}
