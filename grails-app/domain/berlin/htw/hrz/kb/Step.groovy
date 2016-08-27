/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domainclass that represents a single step
 */
class Step {

    static mapWith = "neo4j"

    static mapping = {
        // TODO [TR]: Was genau unterscheidet "text" von Standard-Strings in Neo4J ?
        stepText type: "text"
    }

    static constraints = {
        stepLink nullable: true
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
