package berlin.htw.hrz.kb

class Article extends Document {

    static constraints = {
        docContent nullable: true
    }

    String docContent
}
