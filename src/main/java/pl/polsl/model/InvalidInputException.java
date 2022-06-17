package pl.polsl.model;

/**
 * Custom exeption for handling invalid input
 * @author Jacek
 * @version 1.5
 */
public class InvalidInputException extends Exception  
{  
    /**
     * Calls the constructor of parent Exception.
     * @param errorMessage contents of the exception
     */
    public InvalidInputException (String errorMessage)  
    {  
        super(errorMessage);  
    }
}
