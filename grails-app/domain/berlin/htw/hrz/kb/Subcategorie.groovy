/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb


class Subcategorie{

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
    static hasMany = [subCats: Subcategorie, docs: Document]
    Subcategorie parentCat
    Maincategorie mainCat

    String name;
}
