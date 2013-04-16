package dbusokular;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import org.freedesktop.DBus;
import org.freedesktop.dbus.BusAddress;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.Message;
import org.freedesktop.dbus.MethodCall;
import org.freedesktop.dbus.MethodReturn;
import org.freedesktop.dbus.Transport;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.exceptions.DBusExecutionException;

/**
 *
 * @author jan
 */
public class DBusOkular {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception  {
        //testAndExpose();
        talkToOkular();
        //talkToTwiceService();
    }
    
    public static void talkToOkular() throws Exception {
        // Initialize D-Bus connection
        BusAddress address = new BusAddress(
            System.getenv("DBUS_SESSION_BUS_ADDRESS"));
        Transport conn = new Transport(address);
        
        Date before = new Date();
        Message m = new MethodCall("org.freedesktop.DBus", "/",
        "org.freedesktop.DBus", "Hello", (byte) 0, null);
        conn.mout.writeMessage(m);
        Date afterSend = new Date();
        MethodReturn response = (MethodReturn) conn.min.readMessage();
        Date afterResp = new Date();
        System.out.println("My unique name is: "+response.getParameters()[0]);
        
        System.out.printf("Compose and send:\t %d ms\n", (afterSend.getTime() - before.getTime()));
        System.out.printf("Receive response:\t %d ms\n", (afterResp.getTime() - afterSend.getTime()));
        System.out.printf("Total:\t\t\t %d ms\n", (afterResp.getTime() - before.getTime()));
        
        // Talk to Okular
        String okularInstance = "org.kde.okular-4672";
        
        // Open document in Okular
        m = new MethodCall(okularInstance, "/okular",
        "org.kde.okular", "openDocument", (byte) 0, "s", "~/Downloads/jolie_wshandbook2012.pdf");
        conn.mout.writeMessage(m);
        
        // Go to next page (No parameters, one-way)
        m = new MethodCall(
            okularInstance, 
            "/okular",
            "org.kde.okular", 
            "slotNextPage", (byte) 0, null);
        conn.mout.writeMessage(m);
        
        // Go to specific page (Single uint parameter, one-way)
        m = new MethodCall(
            okularInstance, 
            "/okular",
            "org.kde.okular", 
            "goToPage", (byte) 0, "u", 10);
        conn.mout.writeMessage(m);
        
        // Get number of pages (No parameters, uint32 return value)
        UInt32 numPages = NumberOfPages(conn, okularInstance);
        System.out.println("Number of pages: " + numPages); 
        
    }
    
    public static void talkToTwiceService() throws Exception {
        // Initialize D-Bus connection
        BusAddress address = new BusAddress(System.getenv("DBUS_SESSION_BUS_ADDRESS"));
        Transport conn = new Transport(address);
        
        Message m = new MethodCall("org.freedesktop.DBus", "/",
        "org.freedesktop.DBus", "Hello", (byte) 0, null);
        conn.mout.writeMessage(m);
        MethodReturn response = (MethodReturn) conn.min.readMessage();
        System.out.println("My unique name is: "+response.getParameters()[0]);
        
        int val = 10;
        m = new MethodCall("org.testname", "/object",
        "org.testname", "twice", (byte) 0, "u", val);
        System.out.printf("Calling twice (val = %d) service..\n", val);
        conn.mout.writeMessage(m);
        
        System.out.println("Receiving response..");
        while (true) {
            m = conn.min.readMessage();
            if(m != null && m instanceof MethodReturn)
            {
                System.out.println("Received response: " + m.getParameters()[0]);
                return;
            }
        }
    }
    
    public static UInt32 NumberOfPages(Transport conn, String okularIntance) throws Exception
    {
        UInt32 result = new UInt32(0L);
        
        // Make request
        Message m = new MethodCall(
                okularIntance, 
                "/okular",
                "org.kde.okular", 
                "pages", (byte) 0, null);
        long callId = m.getSerial();
        conn.mout.writeMessage(m);
        
        // Read response
        while(true)
        {
            m = conn.min.readMessage();
            if(m != null && (m instanceof MethodReturn))
            {
                Message resp = (MethodReturn)m;
                Long respId = new Long(resp.getReplySerial());
                if(respId.equals(callId))
                {
                    result = (UInt32)resp.getParameters()[0];
                    break;
                }
            }
        }
        return result;
    }
    
    public static void testAndExpose() throws Exception {
        BusAddress address = new BusAddress(
            System.getenv("DBUS_SESSION_BUS_ADDRESS"));
        
        // Say hello to D-Bus
        //Transport conn = new Transport(address, true);
        Transport conn = new Transport(address);
        Message m = new MethodCall("org.freedesktop.DBus", "/",
        "org.freedesktop.DBus", "Hello", (byte) 0, null);
        conn.mout.writeMessage(m);
        m = conn.min.readMessage();
        System.out.println("Response to Hello is: "+m);
        
        // Request a name
        m = new MethodCall("org.freedesktop.DBus", "/",
        "org.freedesktop.DBus", "RequestName", (byte) 0,
        "su", "org.testname", 0);
        conn.mout.writeMessage(m);
        m = conn.min.readMessage();
        System.out.println("Response to Requestname is: "+m);
        
        // Introsepctable "Hello service"
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
                
                Message ret = null;
                System.out.println(Arrays.deepToString(call.getParameters()));
                if ("Introspect".equals(call.getName())) {
                    /*String data = "<node name=\"/\"><interface name=\"org.testname\">" +
"            <method name=\"Hello\">" +
"              <arg name=\"name\" type=\"s\" direction=\"in\"/>" +
"              <arg name=\"helloname\" type=\"s\" direction=\"out\"/></method></interface></node>";
                    
                    
                    ret = new MethodReturn(call, "s", data);*/
                    ret = new org.freedesktop.dbus.Error(call, new DBus.Error.UnknownMethod(""));
                } else if ("".equals(call.getName())) {
                   //ret = new MethodReturn(call, "u", call.getParameters()[0]);
                } else {
                  Arrays.deepToString(call.getParameters());
                  ret = new MethodReturn(call, "ai", new Object[] {new Integer[] { 42, 4}});
                }
                conn.mout.writeMessage(ret);
            }
            Thread.sleep(100);
        }
       // conn.disconnect();
    }
}
