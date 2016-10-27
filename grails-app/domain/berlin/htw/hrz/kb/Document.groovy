/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domain class that represents a single document
 */
class Document {

    static mapWith = "neo4j"

    static mapping = {
        docTitle index: true
    }

    static constraints = {
        videoLink nullable: true, url: true
        tags nullable: true
        changedBy nullable: true
        createDate nullable: true
        changeDate nullable: true
        locked nullable: true
        linker nullable: true
        docTitle unique: true, matches: /[\w \t\-&.,:?!()'"äöüßÖÄÜ]+/ //matches all word chars, space, tab and the given special chars
    }

    /**
     * Optional, list of associated linker-elements, can be null
     */
    static hasMany = [linker: Linker]

    /**
     * Not optional
     * need to match: all word chars, space, tab and the special chars (-&.,:?!()'")
     */
    String docTitle
    /**
     * Optional, can be null
     */
    String[] tags
    /**
     * Optional, array for storing a change history
     */
    String[] changedBy
    /**
     * Optional, can be null
     */
    Date createDate
    /**
     * Optional, can be null
     */
    Date changeDate
    /**
     * Optional, Todo: If sets, only user from specified groups have access to the document
     */
    Boolean locked
    /**
     * Not optional
     */
    int viewCount
    /**
     * optional
     */
    String videoLink
}