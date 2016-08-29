/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domainclass that represents a single Faq
 */
class Faq extends Document {

    static mapWith = "neo4j"

    static constraints = {
        question nullable: false
        answer nullable: false
    }

    /**
     * Not optional
     */
    String question
    /**
     * Not optional
     */
    String answer
}
