import java.util.Arrays;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 *
 * @author niels
 */
public class ExclusiveMessagesFilter implements Filter {
    private List<String> allowedMessages;
    
    public ExclusiveMessagesFilter(String[] allowedMessages) {
        this.allowedMessages = Arrays.asList(allowedMessages);
    }
    
    @Override
    public boolean isLoggable(LogRecord lr) {
        return allowedMessages.contains(lr.getMessage().split(":")[0]);
    } 
}
