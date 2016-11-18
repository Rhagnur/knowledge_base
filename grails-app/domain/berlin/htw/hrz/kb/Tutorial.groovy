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
        videoLink nullable: true, url: true
    }

    /**
     * Optional
     */
    static hasMany = [steps: Step]

    /**
     * declares if step number will be displayed
     */
    boolean numbered

    /**
     * Intro text for tutorial
     */
    String intro

    /**
     * Optional: Link to video
     */
    String videoLink
}
