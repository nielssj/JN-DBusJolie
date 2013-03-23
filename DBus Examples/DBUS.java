/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbus;

import java.text.ParseException;
import org.freedesktop.dbus.BusAddress;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Message;
import org.freedesktop.dbus.MethodCall;
import org.freedesktop.dbus.MethodReturn;
import org.freedesktop.dbus.Transport;

/**
 *
 * @author jan
 */
public class DBUS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception  {
        testAndExpose();
    }
    
    public static void talkToOkular() throws Exception {
        BusAddress address = new BusAddress(
            System.getenv("DBUS_SESSION_BUS_ADDRESS"));
        Transport conn = new Transport(address);
        
        Message m = new MethodCall("org.freedesktop.DBus", "/",
        "org.freedesktop.DBus", "Hello", (byte) 0, null);
        conn.mout.writeMessage(m);
        MethodReturn response = (MethodReturn) conn.min.readMessage();
        System.out.println("My unique name is: "+response.getParameters()[0]);
       
       
        m = new MethodCall("org.kde.okular-3540", "/okular",
        "org.kde.okular", "openDocument", (byte) 0, "s", "~/Downloads/dbus-java.pdf");
        conn.mout.writeMessage(m);
    }
    
    public static void testAndExpose() throws Exception {
        BusAddress address = new BusAddress(
            System.getenv("DBUS_SESSION_BUS_ADDRESS"));
        
        //Transport conn = new Transport(address, true);
        Transport conn = new Transport(address);
        Message m = new MethodCall("org.freedesktop.DBus", "/",
        "org.freedesktop.DBus", "Hello", (byte) 0, null);
        conn.mout.writeMessage(m);
        m = conn.min.readMessage();
        System.out.println("Response to Hello is: "+m);
        m = new MethodCall("org.freedesktop.DBus", "/",
        "org.freedesktop.DBus", "RequestName", (byte) 0,
        "su", "org.testname", 0);
        conn.mout.writeMessage(m);
        m = conn.min.readMessage();
        System.out.println("Response to Requestname is: "+m);
        while (true) { 
            m = conn.min.readMessage();
            System.out.println("Some message: "+m);
            if (m != null && (m instanceof MethodReturn)) {
                System.out.println("Returns: "+m.getParameters()[0]);
            }
            if (m != null && (m instanceof DBusSignal)) {
                DBusSignal signal = (DBusSignal)m;
                System.out.println("Dat signal:"+signal);
            }
            if (m != null && (m instanceof MethodCall)) {
                MethodCall call = (MethodCall)m;
                System.out.println("I got something!: "+call);
                
                Message ret;
                if ("Introspect".equals(call.getName())) {
                    String data = "<node name=\"/\"><interface name=\"org.testname\">" +
"            <method name=\"Hello\">" +
"              <arg name=\"name\" type=\"s\" direction=\"in\"/>" +
"              <arg name=\"helloname\" type=\"s\" direction=\"out\"/></method></interface></node>";
                    
                    
                    ret = new MethodReturn(call, "s", data);
                } else {
                    ret = new MethodReturn(call, "s", "Hello "+call.getParameters()[0]);
                }
                conn.mout.writeMessage(ret);
            }
            Thread.sleep(100);
        }
       // conn.disconnect();
    }
}
