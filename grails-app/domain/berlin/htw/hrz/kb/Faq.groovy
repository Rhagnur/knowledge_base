package berlin.htw.hrz.kb

class Faq {

    static constraints = {
    }
    static belongsTo = [doc: Document]
    String question, answer
}
