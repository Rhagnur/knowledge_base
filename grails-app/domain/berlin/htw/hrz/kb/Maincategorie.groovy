/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

class Maincategorie {

    static constraints = {
        name nullable: false
    }
    static hasMany = [subCats: Subcategorie]

    String name;
}
