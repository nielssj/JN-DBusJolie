
import java.security.Permission;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author niels
 */

public class NoExitSecurityManager extends SecurityManager 
{
    @Override
    public void checkPermission(Permission perm) 
    {
        // allow anything.
    }
    @Override
    public void checkPermission(Permission perm, Object context) 
    {
        // allow anything.
    }
    @Override
    public void checkExit(int status) 
    {
        super.checkExit(status);
        throw new ExitException(status);
    }

    public static class ExitException extends SecurityException 
    {
        public final int status;
        public ExitException(int status) 
        {
            super("There is no escape!");
            this.status = status;
        }
    }
}