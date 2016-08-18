package berlin.htw.hrz.kb

/**
 * Created by didschu on 18.08.16.
 */
class NoSuchObjectFoundException extends Exception {
    NoSuchObjectFoundException() {}
    NoSuchObjectFoundException(String message) { super(message) }
}
