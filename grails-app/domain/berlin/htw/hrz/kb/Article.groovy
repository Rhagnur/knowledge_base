/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domainclass that represents a single article-document
 */
class Article extends Document {

    static mapping = {
        docContent type: "text"
    }

    static constraints = {
        docContent nullable: false
    }

    /**
     * Not optional
     */
    String docContent
}
