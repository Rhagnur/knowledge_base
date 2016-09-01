/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domain class that represents a single step
 */
class Step {

    static mapWith = "neo4j"

    static constraints = {
        stepLink nullable: true, url: true
        stepTitle matches: /[\w \t\-&.,:?!()'"]+/ //matches all word chars, space, tab and the given special chars
    }
    /**
     * reference to the parent-document
     */
    static belongsTo = [doc: Document]

    /**
     * Not optional
     */
    int number;
    /**
     * Not optional
     * need to match: all word chars, space, tab and the special chars (-&.,:?!()'")
     */
    String stepTitle
    /**
     * Not optional
     */
    String stepText
    /**
     * optional, can be null
     */
    String stepLink;
}
