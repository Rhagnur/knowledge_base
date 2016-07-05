/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb


class Subcategorie{

    static mapWith = "neo4j"

    static constraints = {
        name nullable: false
        parentCat nullable: true
        mainCat nullable: true
    }
    static hasMany = [subCats: Subcategorie, docs: Document]
    static belongsTo = [parentCat: Subcategorie, mainCat: Maincategorie]

    String name;
}
