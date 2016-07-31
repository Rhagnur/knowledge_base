package berlin.htw.hrz.kb

class Article extends Document {

    static mapping = {
        docContent type: "text"
    }

    static constraints = {
        docContent nullable: true
    }

    String docContent
}
