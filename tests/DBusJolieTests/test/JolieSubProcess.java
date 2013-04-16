
import java.io.*;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author niels
 */
public class JolieSubProcess
{
    private static String[] env = new String[] { 
        "/usr/bin/java", 
        "-jar",
        "-Xdebug",
        "-Djava.library.path=/usr/local/lib/jni",
        "../../jolie-src/jolie/dist/jolie.jar"
    };
    
    private String[] args;
    private Process process;

    public JolieSubProcess(String sourcefile, String[] args)
    {
        args = ArrayUtils.add(args, sourcefile);
        this.args = ArrayUtils.addAll(env, args);
    }

    public void start() {
        Runtime r = Runtime.getRuntime();
        
        try 
        {
            process = r.exec(args);
        }
        catch (IOException ex) 
        {
            throw new RuntimeException(String.format("jolie.jar could not be found at %s", env[3]), ex);
        }             
    }
    
    public void stop() {
        process.destroy();
    }
    
    public int join()
    {
        int exitValue = -1;
        
        // Wait for process to exit
        try 
        {
            exitValue = process.waitFor();
        } 
        catch (InterruptedException ex) 
        { 
            throw new RuntimeException("Join was interrupted", ex);
        }
        
        return exitValue;
    }

    public String getOutput()
    {
        DataInputStream in = new DataInputStream(process.getInputStream()); 
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        
        try 
        {
            while((line = br.readLine()) != null)
            {
              System.out.println(line);
                sb.append(line);
            }
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Failed to read output of process", ex);
        }
        
        return sb.toString();
    }
    
    public String getErrorStream()
    {
        DataInputStream in = new DataInputStream(process.getErrorStream()); 
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;

        try
        {
            while((line = br.readLine()) != null)
            {
              System.out.println(line);
                sb.append(line);
            }
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Process failed and wrapper failed to read ErrorStream", ex);
        }
        
        return sb.toString();
    }
}
