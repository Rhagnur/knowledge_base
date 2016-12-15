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
        stepTitle nullable: true, matches: /[\w \t\-&.,:?!()'"äöüßÖÄÜ@\/]+/ //matches all word chars, space, tab and the given special chars
        images nullable: true
        doc nullable: true
    }
    /**
     * reference to the parent-document
     */
    static belongsTo = [doc: Document]

    /**
     * optional: reference to image-blob
     */
    //static hasOne = [image: Image]
    static hasMany = [images: Image]

    /**
     * Not optional
     */
    int number
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
    boolean showNumber
}
