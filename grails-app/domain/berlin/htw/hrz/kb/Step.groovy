package berlin.htw.hrz.kb

class Step {

    static mapWith = "neo4j"

    static mapping = {
        // TODO [TR]: Was genau unterscheidet "text" von Standard-Strings in Neo4J ?
        stepText type: "text"
    }

    static constraints = {
        mediaLink nullable: true
    }
    static belongsTo = [doc: Document]

    int number;
    String stepTitle, stepText, mediaLink;
}
