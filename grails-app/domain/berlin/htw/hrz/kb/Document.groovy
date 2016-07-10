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



    //geht irgendwie nicht, da sonst der gesamte Graph zerstört ist????? Braucht man aber auch vermutlich nicht...man kann sich auch über Subcat.findAllByDocs all SubCats zu einem Doc holen
    //static hasMany = [parentCats: Subcategorie]
    //static belongsTo = [Subcategorie]

    String docTitle
    String[] hiddenTags
}