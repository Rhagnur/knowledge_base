/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domainclass that represents a tutorial
 */
class Tutorial extends Document {

    static constraints = {
        steps nullable: false
    }

    /**
     * Optional
     */
    static hasMany = [steps: Step]
}
