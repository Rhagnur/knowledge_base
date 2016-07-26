/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb


class Subcategory {

    static mapWith = "neo4j"

    static mapping = {
        subCats(cascade: 'save-update')
        docs(cascade: 'save-update')
    }

    static constraints = {
        name nullable: false
        parentCat nullable: true
        mainCat nullable: true
    }
    static hasMany = [subCats: Subcategory, docs: Document]
    Subcategory parentCat
    Maincategory mainCat

    String name;
}
