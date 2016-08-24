/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
* custom exception
*/
class ValidationErrorException extends Exception {
    ValidationErrorException() {}
    ValidationErrorException(String message) { super(message) }
}
