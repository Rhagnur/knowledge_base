/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

class Category {

    static mapWith = "neo4j"

    static constraints = {
        name nullable: false
    }
    static hasMany = [subCats: Subcategory]

    String name;
}
