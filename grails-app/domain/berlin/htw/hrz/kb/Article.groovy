/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domain class that represents a single article-document
 */
class Article extends Document {

    static mapWith = "neo4j"

    static constraints = {
        docContent nullable: false
    }

    /**
     * Not optional
     */
    String docContent
}
