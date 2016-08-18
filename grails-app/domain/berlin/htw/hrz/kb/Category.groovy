/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

class Category {

    static mapWith = "neo4j"

    static constraints = {
        name nullable: false
        subCats nullable: true
        name unique: true
    }
    static hasMany = [subCats: Subcategory]

    String name;
}
