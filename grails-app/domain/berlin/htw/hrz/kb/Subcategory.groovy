/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb


class Subcategory extends Category {

    static mapWith = "neo4j"

    static constraints = {
        name nullable: false
        parentCat nullable: false
        docs nullable: true
    }
    static hasMany = [docs: Document]
    Category parentCat
}
