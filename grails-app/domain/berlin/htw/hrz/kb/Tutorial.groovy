/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domain class that represents a tutorial
 */
class Tutorial extends Document {

    static mapWith = "neo4j"

    static constraints = {
        steps nullable: false
        intro nullable: true
    }

    /**
     * Optional
     */
    static hasMany = [steps: Step]

    /**
     * Intro text for tutorial
     */
    String intro
}
