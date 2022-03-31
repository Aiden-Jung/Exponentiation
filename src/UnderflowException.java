/** Aiden Hyunseok Jung & Hwi Jin Jang
 *  Department of Computer Science
 *  Grinnell College
 *  junghyun@grinnell.edu & janghwij@grinnell.edu
 *
 *  The UnderflowException class is for underflow errors.
 *  
 *  This is the given UnderflowException class. We do not make any change.
 */

/**
 * Exception class for access in empty containers
 * such as stacks, queues, and priority queues.
 * @author Mark Allen Weiss
 */
public class UnderflowException extends RuntimeException
{
    /**
     * Construct this exception object.
     * @param message the error message.
     */
    public UnderflowException( String message )
    {
        super( message );
    }
}
