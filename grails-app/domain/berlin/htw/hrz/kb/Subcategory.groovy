/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domainclass that represents a subcategory
 */
class Subcategory extends Category {

    static mapWith = "neo4j"

    static constraints = {
        parentCat nullable: true
        linker nullable: true
    }

    /**
     * Optional, can be null
     */
    static hasMany = [linker: Linker]
    /**
     * Optional, can be null
     */
    static belongsTo = [parentCat: Category]
}
