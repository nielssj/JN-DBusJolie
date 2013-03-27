
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import jolie.Jolie;
import static org.junit.Assert.assertEquals;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author niels
 */
public class JolieTestProgram extends Thread
{
    private String outputfile, output;
    private String[] args;

    public JolieTestProgram(String[] args, String outputfile)
    {
        this.args = args;
        this.outputfile = outputfile;
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
        try
        {
           DataInputStream in = new DataInputStream(new FileInputStream(outputfile)); 
           BufferedReader br = new BufferedReader(new InputStreamReader(in));
           StringBuilder sb = new StringBuilder();
           String line;
           while((line = br.readLine()) != null)
           {
               sb.append(line);
           }
           br.close();
           output = sb.toString();
           
           new File(outputfile).delete();
        }
        catch (IOException ex)
        {
            output = "";
        }
    }

    public String getOutput()
    {
        return output;
    }
}
