package berlin.htw.hrz.kb

/**
 * Created by didschu on 18.08.16.
 */
class ValidationErrorException extends Exception {
    ValidationErrorException() {}
    ValidationErrorException(String message) { super(message) }
}
