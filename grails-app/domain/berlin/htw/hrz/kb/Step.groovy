/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domain class that represents a single step
 */
class Step extends StepDummy {
    static constraints = {
        number (unique: ['doc'])
    }
    int number
}
