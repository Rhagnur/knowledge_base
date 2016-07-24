package berlin.htw.hrz.kb

class Faq {

    static mapWith = "neo4j"

    static mapping = {
        answer type: "text"
    }
    Document doc
    String question, answer
}
