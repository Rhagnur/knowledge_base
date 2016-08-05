/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb


class Subcategory extends Category {

    static mapWith = "neo4j"

    static constraints = {
        parentCat nullable: false
        docs nullable: true
    }
    static hasMany = [docs: Document]
    Category parentCat
}
