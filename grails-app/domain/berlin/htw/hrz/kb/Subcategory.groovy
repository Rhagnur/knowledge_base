/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb


class Subcategory extends Category {

    static mapWith = "neo4j"

    static constraints = {
        parentCat nullable: true
        linker nullable: true
    }

    static hasMany = [linker: Linker]
    static belongsTo = [parentCat: Category]
}
