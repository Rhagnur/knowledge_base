/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

class Maincategorie {

    static mapWith = "neo4j"

    static mapping = {
        subCats(cascade: 'save-update')
    }

    static constraints = {
        name nullable: false
    }
    static hasMany = [subCats: Subcategorie]

    String name;
}
