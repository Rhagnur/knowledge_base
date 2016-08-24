/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domainclass that represents a single category
 */
class Category {

    static mapWith = "neo4j"

    static constraints = {
        name nullable: false
        subCats nullable: true
        name unique: true
    }
    /**
     * Optional, list of associated subcategories which can be null
     */
    static hasMany = [subCats: Subcategory]

    /**
     * Not optional, the name of the category, which is unique
     */
    String name;
}
