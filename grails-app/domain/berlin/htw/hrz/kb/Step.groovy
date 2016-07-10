package berlin.htw.hrz.kb

class Step {

    static mapWith = "neo4j"

    static constraints = {
        mediaLink nullable: true
    }

    int number;
    String stepTitle, stepText, mediaLink;
}
