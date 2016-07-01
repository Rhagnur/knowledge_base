package berlin.htw.hrz.kb

class Subcategorie {

    static constraints = {
        name nullable: false
    }
    static hasMany = [subCats: Subcategorie, docs: Document]

    String name;
}
