/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domain class that represents a single Faq
 */
class Faq extends Document {

    static mapWith = "neo4j"

    static constraints = {
        question nullable: false, matches: /[\w \t\-&.,:?!()'"]+/ //matches all word chars, space, tab and the given special chars
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
