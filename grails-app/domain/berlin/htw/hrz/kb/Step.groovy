package berlin.htw.hrz.kb

class Step {

    static mapWith = "neo4j"

    static mapping = {
        stepText type: "text"
    }

    static constraints = {
        mediaLink nullable: true
    }
    static belongsTo = [doc: Document]

    int number;
    String stepTitle, stepText, mediaLink;
}
