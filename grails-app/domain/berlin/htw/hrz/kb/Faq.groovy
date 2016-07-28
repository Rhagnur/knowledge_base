package berlin.htw.hrz.kb

class Faq extends Document {

    static mapWith = "neo4j"

    static mapping = {
        answer type: "text"
    }

    String question, answer
}
