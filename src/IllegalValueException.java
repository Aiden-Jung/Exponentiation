/** Aiden Hyunseok Jung & Hwi Jin Jang
 *  Department of Computer Science
 *  Grinnell College
 *  junghyun@grinnell.edu & janghwij@grinnell.edu
 *
 *  The IllegalValueException class is for illegal value errors.
 *  
 *  This is the given IllegalValueException class. We do not make any change.
 */

/**
 * Exception class for illegal decrease key
 * operations in pairing heaps.
 * @author Mark Allen Weiss
 */

public class IllegalValueException extends RuntimeException
{
    /**
     * Construct this exception object.
     * @param message the error message.
     */
    public IllegalValueException( String message )
    {
        super( message );
    }
}
