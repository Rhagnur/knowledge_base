package berlin.htw.hrz.kb

class Faq {

    static mapWith = "neo4j"

    static mapping = {
        answer type: "text"
    }

    static constraints = {
    }
    static belongsTo = [doc: Document]
    String question, answer
}
