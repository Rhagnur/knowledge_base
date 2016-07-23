/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

class Document {

    static mapWith = "neo4j"

    static constraints = {
        hiddenTags nullable: true
        faq nullable: true
        faq unique: true
        steps nullable: true
    }

    static hasMany = [steps: Step]
    Faq faq

    String docTitle
    String[] hiddenTags
    int viewCount
}