/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import org.junit.*;
import static org.junit.Assert.*;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import jolie.lang.NativeType;
import jolie.net.CommChannel;
import jolie.net.CommMessage;
import jolie.net.DBusCommChannel;
import jolie.net.DBusCommChannelFactory;
import jolie.net.DBusMarshalling;
import jolie.net.SoapProtocol;
import jolie.net.ports.InputPort;
import jolie.net.ports.Interface;
import jolie.net.ports.OutputPort;
import jolie.net.protocols.CommProtocol;
import jolie.process.TransformationReason;
import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import jolie.runtime.VariablePath;
import jolie.runtime.expression.Expression;
import jolie.runtime.typing.OneWayTypeDescription;
import jolie.runtime.typing.RequestResponseTypeDescription;
import jolie.runtime.typing.Type;
import jolie.util.Pair;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

/**
 *
 * @author niels
 */
public class BenchmarkingTests {

  private static String[] defaultArgs;
  private static String jpf;

  @BeforeClass
  public static void setUpClass() {
    jpf = "jolie-programs/concurrency/";
    defaultArgs = new String[]{
      "-i", "../../jolie-src/include",
      "-l", "../../jolie-src/javaServices/coreJavaServices/dist/coreJavaServices.jar",
      "-l", "../../jolie-src/javaServices/minitorJavaServices/dist/monitorJavaServices.jar",
      "-l", "../../jolie-src/lib/xsom/dist",
      "-l", "../../jolie-src/lib/jolie-xml/dist",
      "-l", "../../jolie-src/extensions/soap/dist/*",
      "-l", "../../jolie-src/extensions/http/dist/*",
      "-l", "../../jolie-src/extensions/dbus/dist/*",
      "-l", "../../jolie-src/lib/relaxngDatatype",
      "-l", "../../jolie-src/lib/wsdl4j",
      "-l", "../../jolie-src/lib/libmatthew",
      "-l", "../../jolie-src/lib/dbus-java"
    };
  }

  @Before
  public void setUp() {
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @After
  public void tearDown() {
    System.setSecurityManager(null);
  }

  @Test
  public void sendTiny() throws Exception {
    // Configure client and server
    JolieSubProcess server = new JolieSubProcess(jpf + "server_concurrent.ol", defaultArgs);
    JolieThread client = new JolieThread(jpf + "client_benchmark.ol", defaultArgs);

    // Set up benchmark logging
    Logger logger = setUpLogger(
            new String[]{
      "sendImpl - Called",
      "sendImpl - Returned succesfully"
    },
            "sendTiny.csv", "jolie.net.dbus");

    // Execute
    server.start();
    String firstLine = server.getOutputLine(); // (Blocking) Wait for first output from server
    client.start();
    client.join();
    server.stop();

    // Push memory log to file
    MemoryHandler mh = (MemoryHandler) logger.getHandlers()[0];
    mh.push();
  }

  @Test
  public void recvTiny() throws Exception {
    // Configure client and server
    JolieSubProcess server = new JolieSubProcess(jpf + "server_concurrent.ol", defaultArgs);
    JolieThread client = new JolieThread(jpf + "client_benchmark.ol", defaultArgs);

    // Set up benchmark logging
    Logger logger = setUpLogger(
            new String[]{
      "recvResponseFor - Found matching response",
      "recvResponseFor - Returned succesfully"
    },
            "recvTiny.csv", "jolie.net.dbus");

    // Execute
    server.start();
    String firstLine = server.getOutputLine(); // (Blocking) Wait for first output from server
    client.start();
    client.join();
    server.stop();

    // Push memory log to file
    MemoryHandler mh = (MemoryHandler) logger.getHandlers()[0];
    mh.push();
  }

  @Test
  public void fullTiny() throws Exception {
    // Configure client and server
    JolieSubProcess server = new JolieSubProcess(jpf + "server_concurrent.ol", defaultArgs);
    JolieThread client = new JolieThread(jpf + "client_benchmark.ol", defaultArgs);

    // Set up benchmark logging
    Logger logger = setUpLogger(
            new String[]{
      "sendImpl - Called",
      "sendImpl - Sending",
      "sendImpl - Sent",
      "recvResponseFor - Found matching response",
      "recvResponseFor - Returned succesfully"
    },
            "fullTiny.csv", "jolie.net.dbus");

    // Execute
    server.start();
    String firstLine = server.getOutputLine(); // (Blocking) Wait for first output from server
    client.start();
    client.join();
    server.stop();

    // Push memory log to file
    MemoryHandler mh = (MemoryHandler) logger.getHandlers()[0];
    mh.push();
  }

  @Test
  public void fullTinySOAPSocket() throws Exception {
    // Configure client and server
    JolieSubProcess server = new JolieSubProcess("jolie-programs/benchmark/server_soap_concurrent.ol", defaultArgs);
    JolieThread client = new JolieThread("jolie-programs/benchmark/client_soap_benchmark.ol", defaultArgs);

    // Set up benchmark logging
    Logger logger = setUpLogger(
            new String[]{
      "sendImpl - Called",
      "sendImpl - Sending",
      "sendImpl - Sent",
      "recvImpl - Called",
      "recvImpl - Returned succesfully"
    },
            "fullTinySOAP.csv", "jolie.net.soket");

    // Execute
    server.start();
    String firlstLine = server.getOutputLine(); // (Blocking) Wait for first output from server
    client.start();
    client.join();
    server.stop();

    // Push memory log to file
    MemoryHandler mh = (MemoryHandler) logger.getHandlers()[0];
    mh.push();
  }

  @Test
  public void fullLargeSOAPSocket() throws Exception {
    // Configure client and server
    JolieSubProcess server = new JolieSubProcess("jolie-programs/benchmark/server_soap2_concurrent.ol", defaultArgs);
    JolieThread client = new JolieThread("jolie-programs/benchmark/large_soap_benchmark.ol", defaultArgs);

    // Set up benchmark logging
    Logger logger = setUpLogger(
            new String[]{
      "sendImpl - Called",
      "sendImpl - Sending",
      "sendImpl - Sent",
      "recvImpl - Called",
      "recvImpl - Returned succesfully"
    },
            "fullLargeSOAP.csv", "jolie.net.socket");

    // Execute
    server.start();
    String firstLine = server.getOutputLine(); // (Blocking) Wait for first output from server
    client.start();
    client.join();
    server.stop();

    // Push memory log to file
    MemoryHandler mh = (MemoryHandler) logger.getHandlers()[0];
    mh.push();
  }

  private Logger setUpLogger(String[] allowedMessages, String filename, String logDomain) throws IOException {
    // Filter - Only allow entries with specific messages
    ExclusiveMessagesFilter emf = new ExclusiveMessagesFilter(allowedMessages);

    // File handler - Save to time stamps to file
    FileHandler fh = new FileHandler(filename);
    fh.setFormatter(new BenchLogFormatter(allowedMessages[allowedMessages.length - 1]));
    fh.setFilter(emf);

    // Memory buffer - Store in memory during benchmark
    MemoryHandler mh = new MemoryHandler(fh, 1000000, Level.OFF);

    // Logger - Configure logger with handlers, filter and level
    Logger logger = Logger.getLogger(logDomain);
    logger.setUseParentHandlers(false);
    logger.addHandler(mh);
    logger.setLevel(Level.FINE);

    return logger;
  }

  @Test
  public void soapSend() throws Exception {
    // Set up benchmark logging

    Logger logger = setUpLogger(
            new String[]{
      "send - start",
      "sendImpl - Sending",
      "sendImpl - After send",
      "recv - before parse",
      "recv - after parse",
      "recv - before return"
    }, "soapsend.csv", "jolie.net.socket");

    int size = 200;

    Value root = Value.create();
    Map<String, ValueVector> children = root.children();
    int argNo = 0;
    int currentArgNo;

    currentArgNo = argNo;
    for (; argNo < currentArgNo + size; argNo++) {
      ValueVector _int = ValueVector.create();
      _int.add(Value.create(42));
      children.put("arg" + argNo, _int);
    }

    CommMessage m = CommMessage.createRequest("operation", null, root);

    Pair<Expression, Expression>[] exprs = (Pair<Expression, Expression>[]) Array.newInstance(Pair.class, 1);
    exprs[0] = new Pair<Expression, Expression>(new Expression() {
      @Override
      public Value evaluate() {
        return Value.UNDEFINED_VALUE;
      }

      @Override
      public Expression cloneExpression(TransformationReason tr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
      }
    }, null);

    VariablePath vp = new VariablePath(exprs) {
      @Override
      public Value getRootValue() {
        return Value.UNDEFINED_VALUE;
      }
    };

    CommProtocol soap = new SoapProtocol(vp, new URI("www.test.com"), null);

    ByteArrayOutputStream temp = new ByteArrayOutputStream(100000);
    OutputStream out;
    InputStream in;

    soap.send(temp, m, null);
    temp.flush();
    temp.close();

    String XMLstring = new String(temp.toByteArray());

    int wc = 10000;
    int bc = 100000;

    // Warm up
    System.out.println("Starting warm up..");
    for (int i = 0; i < wc; i++) {
      out = new FileOutputStream("/dev/null");
      in = new ByteArrayInputStream(XMLstring.getBytes());

      soap.send(out, m, in);
      out.flush();
      out.close();

      soap.recv(in, out);
      in.close();
    }

    // Benchmark
    System.out.println("Starting benchmark..");

    for (int i = 0; i < bc; i++) {
      out = new FileOutputStream("/dev/null");
      in = new ByteArrayInputStream(XMLstring.getBytes());

      soap.send(out, m, null);
      out.flush();
      out.close();

      soap.recv(in, out);
      in.close();
    }

    // Push memory log to file
    MemoryHandler mh = (MemoryHandler) logger.getHandlers()[0];
    mh.push();
  }

  // *1   Initialization of CommChannel ("Hello cost")
  // *2   Send request (marshal and send off)
  // *3   Bus trip
  // *4   Receive response (read and demarshal)
  // *5   Introspect
  //      - Call
  //      - Parsing
  // *6   Make introspectable
  //      - Build and serialize
  @Test
  public void test0() throws Exception {

    DBusCommChannelFactory dcf = new DBusCommChannelFactory(null);
    URI loc = new URI("dbus:/org.testname7:/object");
    Type t = Type.create(NativeType.INT, null, true, null);
    RequestResponseTypeDescription rtd = new RequestResponseTypeDescription(t, t, null);
    Map<String, OneWayTypeDescription> owtds = new HashMap<String, OneWayTypeDescription>();
    Map<String, RequestResponseTypeDescription> rtds = new HashMap<String, RequestResponseTypeDescription>();
    rtds.put("Twice", rtd);
    Interface iface = new Interface(owtds, rtds);

    // Make output channel
    OutputPort port = new OutputPort(null, "1", null, null, iface, true);
    long oistart = System.nanoTime();
    CommChannel channel = dcf.createChannel(loc, port);
    long oiend = System.nanoTime();

    // Make input channel
    InputPort iport = new InputPort("john", loc, null, iface, null, null);
    long iistart = System.nanoTime();
    DBusCommChannel ichannel = DBusCommChannelFactory.createChannel(loc, iport);
    long iiend = System.nanoTime();

    double oinit = new Long(oiend - oistart).doubleValue() / (1000000.0);
    double iinit = new Long(iiend - iistart).doubleValue() / (1000000.0);
    System.out.println("oinit\t\tiinit");
    System.out.printf("%fms\t%fms\n\n", oinit, iinit);


    // Make message
    CommMessage msg = new CommMessage(1, "twice", null, Value.create(10), null);
    long start, end, recvd, marshld;
    double send, trip, recv;

    int wc = 1000;
    int bc = 10000;

    // Warm up
    System.out.println("Starting warm up..");
    for (int i = 0; i < wc; i++) {
      // Send message
      channel.send(msg);
      if (ichannel.listen()) {
        CommMessage resp = ichannel.recv();
      }
    }

    // Benchmark
    System.out.println("Starting benchmark..");
    double[] sends = new double[bc];
    double[] trips = new double[bc];
    double[] recvs = new double[bc];
    for (int i = 0; i < bc; i++) {
      // Send message
      start = System.nanoTime();
      channel.send(msg);
      end = System.nanoTime();

      // Listen for message
      recvd = -1;
      marshld = -1;
      if (ichannel.listen()) {
        recvd = System.nanoTime();
        CommMessage resp = ichannel.recv();
        marshld = System.nanoTime();
      }

      // Save result to memory
      sends[i] = new Long(end - start).doubleValue() / (1000000.0);
      trips[i] = new Long(recvd - end).doubleValue() / (1000000.0);
      recvs[i] = new Long(marshld - recvd).doubleValue() / (1000000.0);
    }

    // Mean
    double sendMean = new Mean().evaluate(sends);
    double tripMean = new Mean().evaluate(trips);
    double recvMean = new Mean().evaluate(recvs);
    System.out.println("\tsend\t\ttrip\t\trecv");
    System.out.printf("mean:\t%fms\t%fms\t%fms\n", sendMean, tripMean, recvMean);

    // Standard Deviation
    double sendSD = new StandardDeviation().evaluate(sends);
    double tripSD = new StandardDeviation().evaluate(trips);
    double recvSD = new StandardDeviation().evaluate(recvs);
    System.out.printf("sd:\t%fms\t%fms\t%fms\n", sendSD, tripSD, recvSD);

    writeToCSV(sends, trips, recvs);

    assertTrue(true);

  }

  @Test
  public void test1() throws Exception {
    Value root = Value.create();
    Map<String, ValueVector> children = root.children();

    ValueVector string = ValueVector.create();
    string.add(Value.create("John"));
    children.put("arg0", string);

    ValueVector ints = ValueVector.create();
    ints.add(Value.create(12));
    ints.add(Value.create(42));
    ints.add(Value.create(1257));
    children.put("arg1", ints);

    ValueVector nested = ValueVector.create();

    Value nestedChild = Value.create();
    ValueVector truevalue = ValueVector.create();
    truevalue.add(Value.create(true));
    nestedChild.children().put("truevalue", truevalue);

    ValueVector falsevalue = ValueVector.create();
    falsevalue.add(Value.create(false));
    nestedChild.children().put("falsevalue", falsevalue);

    nested.add(nestedChild);
    children.put("arg2", nested);

    ValueVector doubles = ValueVector.create();
    doubles.add(Value.create(12.0));
    doubles.add(Value.create(42.4));
    doubles.add(Value.create(1257.1));
    doubles.add(Value.create(12.0));
    doubles.add(Value.create(42.4));
    doubles.add(Value.create(1257.1));
    doubles.add(Value.create(12.0));
    doubles.add(Value.create(42.4));
    doubles.add(Value.create(1257.1));
    doubles.add(Value.create(12.0));
    doubles.add(Value.create(42.4));
    doubles.add(Value.create(1257.1));
    doubles.add(Value.create(12.0));
    doubles.add(Value.create(42.4));
    doubles.add(Value.create(1257.1));
    children.put("arg3", doubles);

    ValueVector nested2 = ValueVector.create();

    Value nested2Child = Value.create();
    ValueVector string1 = ValueVector.create();
    string1.add(Value.create("string1"));
    nested2Child.children().put("string1", string1);

    ValueVector string2 = ValueVector.create();
    string2.add(Value.create("string2"));
    nested2Child.children().put("string2", string2);

    nested2.add(nested2Child);

    nested2Child = Value.create();
    string1 = ValueVector.create();
    string1.add(Value.create("string1"));
    nested2Child.children().put("string1", string1);

    string2 = ValueVector.create();
    string2.add(Value.create("string2"));
    nested2Child.children().put("string2", string2);

    nested2.add(nested2Child);

    children.put("arg4", nested2);


    int wc = 10000;
    int bc = 100000;

    // Warm up
    System.out.println("Starting warm up..");
    String[] argNames = new String[]{"arg0", "arg1", "arg2", "arg3", "arg4"};
    StringBuilder sb;
    for (int i = 0; i < wc; i++) {
      Object[] res = DBusMarshalling.valueToDBus(root, argNames);
      sb = new StringBuilder();
      res = DBusMarshalling.valueToDBus(root, sb);
      DBusMarshalling.ToJolieValue(res, "saia{sb}adaa{ss}", argNames);
    }

    // Benchmark
    System.out.println("Starting benchmark..");
    long startMarshal, endMarshal, startDeMarshal, endDeMarshal, startMarshalUnknown, endMarshalUnknown;
    double[] marshals = new double[bc];
    double[] marshalsUnknown = new double[bc];
    double[] demarshals = new double[bc];

    for (int i = 0; i < bc; i++) {
      // Marshalling with known names
      startMarshal = System.nanoTime();
      Object[] res = DBusMarshalling.valueToDBus(root, argNames);
      endMarshal = System.nanoTime();

      // Marshalling with unknownknown names
      sb = new StringBuilder();
      startMarshalUnknown = System.nanoTime();
      Object[] res2 = DBusMarshalling.valueToDBus(root, sb);
      endMarshalUnknown = System.nanoTime();

      // Listen for message
      startDeMarshal = System.nanoTime();
      DBusMarshalling.ToJolieValue(res, "saia{sb}adaa{ss}", argNames);
      endDeMarshal = System.nanoTime();

      // Save result to memory
      marshals[i] = new Long(endMarshal - startMarshal).doubleValue() / (1000000.0);
      marshalsUnknown[i] = new Long(endMarshalUnknown - startMarshalUnknown).doubleValue() / (1000000.0);
      demarshals[i] = new Long(endDeMarshal - startDeMarshal).doubleValue() / (1000000.0);
    }

    // Mean
    double marshalMean = new Mean().evaluate(marshals);
    double marshalUnknownMean = new Mean().evaluate(marshalsUnknown);
    double demarshalMean = new Mean().evaluate(demarshals);
    System.out.println("\tmarshal\t\tunknown marshal\t\tdemarshal");
    System.out.printf("mean:\t%fms\t%fms\t\t%fms\n", marshalMean, marshalUnknownMean, demarshalMean);

    // Standard Deviation
    double marshalSD = new StandardDeviation().evaluate(marshals);
    double marshalUnknownSD = new StandardDeviation().evaluate(marshalsUnknown);
    double demarshalSD = new StandardDeviation().evaluate(demarshals);
    System.out.printf("sd:\t%fms\t%fms\t\t%fms\n", marshalSD, marshalUnknownSD, demarshalSD);
  }

  @Test
  public void test2() throws Exception {
    int size = 2000;

    Value root = Value.create();
    Map<String, ValueVector> children = root.children();
    String[] argNames = new String[1 * size];
    StringBuilder sb;
    String signature;
    int argNo = 0;
    int currentArgNo;

    currentArgNo = argNo;
    for (; argNo < currentArgNo + size; argNo++) {
      ValueVector _int = ValueVector.create();
      _int.add(Value.create(42));
      children.put("arg" + argNo, _int);
      argNames[argNo] = "arg" + argNo;
    }

    sb = new StringBuilder();
    DBusMarshalling.valueToDBus(root, sb);
    signature = sb.toString();

    int wc = 10000;
    int bc = 100000;

    // Warm up
    System.out.println("Starting warm up..");

    for (int i = 0; i < wc; i++) {
      Object[] res = DBusMarshalling.valueToDBus(root, argNames);
      sb = new StringBuilder();
      res = DBusMarshalling.valueToDBus(root, sb);
      DBusMarshalling.ToJolieValue(res, signature, argNames);
    }

    // Benchmark
    System.out.println("Starting benchmark..");
    long startMarshal, endMarshal, startDeMarshal, endDeMarshal, startMarshalUnknown, endMarshalUnknown;
    double[] marshals = new double[bc];
    double[] marshalsUnknown = new double[bc];
    double[] demarshals = new double[bc];

    for (int i = 0; i < bc; i++) {
      // Marshalling with known names
      startMarshal = System.nanoTime();
      Object[] res = DBusMarshalling.valueToDBus(root, argNames);
      endMarshal = System.nanoTime();

      // Marshalling with unknown names
      sb = new StringBuilder();
      startMarshalUnknown = System.nanoTime();
      Object[] res2 = DBusMarshalling.valueToDBus(root, sb);
      endMarshalUnknown = System.nanoTime();

      // Demarshalling with known names
      startDeMarshal = System.nanoTime();
      DBusMarshalling.ToJolieValue(res2, signature, argNames);
      endDeMarshal = System.nanoTime();

      // Save result to memory
      marshals[i] = new Long(endMarshal - startMarshal).doubleValue() / (1000000.0);
      marshalsUnknown[i] = new Long(endMarshalUnknown - startMarshalUnknown).doubleValue() / (1000000.0);
      demarshals[i] = new Long(endDeMarshal - startDeMarshal).doubleValue() / (1000000.0);
    }

    // Mean
    double marshalMean = new Mean().evaluate(marshals);
    double marshalUnknownMean = new Mean().evaluate(marshalsUnknown);
    double demarshalMean = new Mean().evaluate(demarshals);
    System.out.println("\tmarshal\t\tunknown marshal\t\tdemarshal");
    System.out.printf("mean:\t%fms\t%fms\t\t%fms\n", marshalMean, marshalUnknownMean, demarshalMean);



    // Standard Deviation
    double marshalSD = new StandardDeviation().evaluate(marshals);
    double marshalUnknownSD = new StandardDeviation().evaluate(marshalsUnknown);
    double demarshalSD = new StandardDeviation().evaluate(demarshals);
    System.out.printf("sd:\t%fms\t%fms\t\t%fms\n", marshalSD, marshalUnknownSD, demarshalSD);

    writeToCSV(marshals, marshalsUnknown, demarshals);
    assertTrue(true);
  }

  @Test
  public void test3() throws Exception {
    int size = 2000;

    Value root = Value.create();
    Map<String, ValueVector> children = root.children();
    int argNo = 0;
    int currentArgNo;

    currentArgNo = argNo;
    for (; argNo < currentArgNo + size; argNo++) {
      ValueVector _int = ValueVector.create();
      _int.add(Value.create(42));
      children.put("arg" + argNo, _int);
    }

    CommMessage m = CommMessage.createRequest("operation", null, root);

    Pair<Expression, Expression>[] exprs = (Pair<Expression, Expression>[]) Array.newInstance(Pair.class, 1);
    exprs[0] = new Pair<Expression, Expression>(new Expression() {
      @Override
      public Value evaluate() {
        return Value.UNDEFINED_VALUE;
      }

      @Override
      public Expression cloneExpression(TransformationReason tr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
      }
    }, null);

    VariablePath vp = new VariablePath(exprs) {
      @Override
      public Value getRootValue() {
        return Value.UNDEFINED_VALUE;
      }
    };

    CommProtocol soap = new SoapProtocol(vp, new URI("www.test.com"), null);

    ByteArrayOutputStream temp = new ByteArrayOutputStream(100000);
    OutputStream out;
    InputStream in;

    soap.send(temp, m, null);
    temp.flush();
    temp.close();

    String XMLstring = new String(temp.toByteArray());

    int wc = 10000;
    int bc = 100000;

    // Warm up
    System.out.println("Starting warm up..");
    for (int i = 0; i < wc; i++) {
      if (i % 1000 == 0) {
        System.out.printf("i: %d\n", i);
      }
      out = new FileOutputStream("/dev/null");
      in = new ByteArrayInputStream(XMLstring.getBytes());

      soap.send(out, m, in);
      out.flush();
      out.close();

      soap.recv(in, out);
      in.close();
    }

    // Benchmark
    System.out.println("Starting benchmark..");
    long startSend, endSend, startRecv, endRecv;
    double[] sends = new double[bc];
    double[] recvs = new double[bc];

    for (int i = 0; i < bc; i++) {
      if (i % 10000 == 0) {
        System.out.printf("i: %d\n", i);
      }
      out = new FileOutputStream("/dev/null");
      in = new ByteArrayInputStream(XMLstring.getBytes());

      startSend = System.nanoTime();
      soap.send(out, m, null);
      endSend = System.nanoTime();
      out.flush();
      out.close();

      startRecv = System.nanoTime();
      soap.recv(in, out);
      endRecv = System.nanoTime();
      in.close();

      sends[i] = new Long(endSend - startSend).doubleValue() / (1000000.0);
      recvs[i] = new Long(endRecv - startRecv).doubleValue() / (1000000.0);
    }

    // Mean
    double sendMean = new Mean().evaluate(sends);
    double recvMean = new Mean().evaluate(sends);
    System.out.println("send\t\t\trecv");
    System.out.printf("mean:\t%fms\t%fms\n", sendMean, recvMean);


    // Standard Deviation
    double sendSD = new StandardDeviation().evaluate(sends);
    double recvSD = new StandardDeviation().evaluate(recvs);
    System.out.printf("sd:\t%fms\t%fms\n", sendSD, recvSD);


    PrintWriter pw = new PrintWriter("SOAPprotocolBench.csv", "UTF-8");

    for (int i = 0; i < sends.length; i++) {
      pw.printf("%f,%f\n", sends[i], recvs[i]);
    }

    pw.flush();
    pw.close();
  }

  private void writeToCSV(double[] sends, double[] trips, double[] recvs) throws Exception {
    PrintWriter pw = new PrintWriter("bench.csv", "UTF-8");

    for (int i = 0; i < sends.length; i++) {
      pw.printf("%f,%f,%f\n", sends[i], trips[i], recvs[i]);
    }

    pw.flush();
    pw.close();

  }
}
