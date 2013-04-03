package jolie.net;


import java.io.IOException;
import java.text.ParseException;

import jolie.Interpreter;
import jolie.net.ports.InputPort;
import org.freedesktop.dbus.*;
import org.freedesktop.dbus.exceptions.*;

public class DBusListener extends CommListener implements Runnable
{
    private String name;
    private Transport conn;
    private boolean stopped = false;

    public DBusListener(Interpreter interpreter, InputPort inputPort) throws IOException
    {
        super(interpreter, inputPort);
        name = inputPort.location().getSchemeSpecificPart(); // e.g. org.testname

        try
        {
            BusAddress address = new BusAddress(System.getenv("DBUS_SESSION_BUS_ADDRESS")); // TODO: Move to location (SESSION/SYSTEM)
            conn = new Transport(address);

            // Obtain DBus ID
            Message m = new MethodCall("org.freedesktop.DBus", "/",
            "org.freedesktop.DBus", "Hello", (byte) 0, null);
            conn.mout.writeMessage(m);
            m = conn.min.readMessage();
            System.out.println("Response to Hello is: "+m);

            // Reserve a name instead of just an ID
            m = new MethodCall("org.freedesktop.DBus", "/",
            "org.freedesktop.DBus", "RequestName", (byte) 0,
            "su", name, 0);
            conn.mout.writeMessage(m);
            m = conn.min.readMessage();
            System.out.println("Response to Requestname is: "+m);
        }
        catch (ParseException ex)
        {
            throw new RuntimeException("Failed to parse BusAddress", ex);
        }
        catch (DBusException ex)
        {
            throw new RuntimeException("Failed to register service in dbus", ex);
        }
    }

    @Override
    public void shutdown()
    {
        stopped = true;
    }

    @Override
    public void run()
    {
        stopped = false;
        
        // Listen for methods calls till shutdown is called
        while (!stopped) { 
            try
            {
                Message m = conn.min.readMessage();
                
                if(m != null && m instanceof MethodCall)
                {
                    MethodCall call = (MethodCall)m;
                    Message ret = null;
                    
                    if ("Introspect".equals(call.getName())) {
                        String data = "<node name=\"/\"><interface name=\"org.testname\">" +
                        "            <method name=\"Hello\">" +
                        "              <arg name=\"name\" type=\"s\" direction=\"in\"/>" +
                        "              <arg name=\"helloname\" type=\"s\" direction=\"out\"/></method></interface></node>";

                        ret = new MethodReturn(call, "s", data);

                        //ret = new org.freedesktop.dbus.Error(call, new NoSuchMethodException("This service is not introspectable"));
                    } else {
                        ret = new MethodReturn(call, "s", "Hello "+call.getParameters()[0]);
                    }
                    
                    conn.mout.writeMessage(ret);
                }
                
                Thread.sleep(100);
            }
            catch( Exception ex )
            {
                throw new RuntimeException("Unexpected failure upon message receive", ex);
            }
        }
    }        
}
