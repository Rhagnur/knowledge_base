package berlin.htw.hrz.kb

class Faq extends Document {

    static mapWith = "neo4j"

    static constraints = {
        question nullable: true
        answer nullable: true
    }

    static mapping = {
        answer type: "text"
    }

    String question, answer
}
