
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import jolie.Jolie;
import org.apache.commons.lang3.ArrayUtils;
import static org.junit.Assert.assertEquals;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author niels
 */
public class JolieThread extends Thread
{
    private String output;
    private String[] args;

    public JolieThread(String sourcefile, String[] args)
    {
        String[] sargs;
        
        String[] pargs = sourcefile.split(" ");
        if(pargs.length > 1) {
            sargs = pargs;
        } else {
            sargs = new String[] { sourcefile };
        }
        
        this.args = ArrayUtils.addAll(args, sargs);
    }

    @Override
    public void run() {
        try 
        {
            Jolie.main(args);
        } 
        // Assert
        catch (NoExitSecurityManager.ExitException e) 
        {
            assertEquals("Exit status", 0, e.status);
        }

        // Read output
        /*try
        {
           BufferedReader br = new BufferedReader(new InputStreamReader(in));
           StringBuilder sb = new StringBuilder();
           String line;
           while((line = br.readLine()) != null)
           {
               sb.append(line);
           }
           br.close();
           output = sb.toString();
        }
        catch (IOException ex)
        {
            output = "";
        }*/
    }

    public String getOutput()
    {
        return output;
    }
}
