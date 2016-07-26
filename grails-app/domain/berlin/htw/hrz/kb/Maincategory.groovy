/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

class Maincategory {

    static mapWith = "neo4j"

    static mapping = {
        subCats(cascade: 'save-update')
    }

    static constraints = {
        name nullable: false
    }
    static hasMany = [subCats: Subcategory]

    String name;
}
