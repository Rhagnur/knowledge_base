/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * custom exception
 */
class NoSuchObjectFoundException extends Exception {
    NoSuchObjectFoundException() {}
    NoSuchObjectFoundException(String message) { super(message) }
}
