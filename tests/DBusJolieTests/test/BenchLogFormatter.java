
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author niels
 */
public class BenchLogFormatter extends Formatter {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private String lastMessage;
    
    public BenchLogFormatter(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    @Override
    public String format(LogRecord lr) {
        String[] parts = lr.getMessage().split(":");
        String msg = parts[0];
        String time = parts[1];
        if(lastMessage.equals(msg)) {
            return time + "\n";
        } else {
            return time + ", ";
        }
    }
}
