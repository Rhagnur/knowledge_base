package berlin.htw.hrz.kb

class Document {

    static mapWith = "neo4j"

    static constraints = {
        hiddenTags nullable: true
    }
    String title, content
    String[] hiddenTags
}