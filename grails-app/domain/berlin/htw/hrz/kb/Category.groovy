/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

import grails.rest.Resource

/**
 * Domain class that represents a single category
 */
//@Resource(readOnly = false, formats = ['json', 'xml'], uri = '/category')
class Category {

    static mapWith = "neo4j"

    static mapping = {
        name index: true
    }

    static constraints = {
        subCats nullable: true
        name unique: true, matches: /[\w]+/ //matches 'a-zA-Z0-9' and the char '_' (word chars)
    }
    /**
     * Optional, list of associated subcategories which can be null
     */
    static hasMany = [subCats: Subcategory]

    /**
     * Not optional, the name of the category, which is unique
     * Can only be characters from a-Z0-9 and the char '_'
     */
    String name;
}
