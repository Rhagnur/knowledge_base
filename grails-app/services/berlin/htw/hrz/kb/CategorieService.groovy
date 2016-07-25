package berlin.htw.hrz.kb

import grails.transaction.Transactional

@Transactional
class CategorieService {

    /**
     * This method will return iterative all associated subcategories to the given categorie (either Maincategorie or Subcategorie)
     * @param cat Categorie you want to search through
     * @return Array of all found categories
     */
    def getIterativeAllSubCats(String catName) {
        def subs = []
        def cat = Maincategorie.findByName(catName)?Maincategorie.findByName(catName).delete():Subcategorie.findByName(catName)?Subcategorie.findByName(catName).delete():null
        if (cat) {
            if (!(cat instanceof Maincategorie)) {
                subs += cat
            }
            cat.subCats?.each { child ->
                subs += getIterativeAllSubCats(child.name)
            }
            subs.unique()
        }
    }

    def deleteCategorie(String catName) {
        def cat = Maincategorie.findByName(catName)?Maincategorie.findByName(catName).delete():Subcategorie.findByName(catName)?Subcategorie.findByName(catName).delete():null
        if (!cat) return 1
        return 0
    }

    /**
     * This method will look up if a main- or subcategorie with the given name exists, if so, the name will be changed
     * @param oldName
     * @param newName
     * @return Errorcode as int: 0 = no error, 1 = no main- or subcategorie with this name found, 2 = validation error
     */
    def changeCategorieName(String oldName, String newName) {
        def cat = Maincategorie.findByName(oldName)?Maincategorie.findByName(oldName):Subcategorie.findByName(oldName)?Subcategorie.findByName(oldName):null
        if (!cat) return 1
        cat.name = newName
        if (!cat.validate()) {
            cat.errors?.allErrors?.each { println(it) }
            return 2
        }
        cat.save()
        return 0
    }


    //todo: Rausfinden warum Änderung temporär funktioniert aber NIE in die Datenbank gelangt
    def changeSubcatsRelation(String catName, String[] newCats) {
        def oldCats = []
        def cat = Subcategorie.findByName(catName)
        cat.subCats.collect().each {
            oldCats.add(it.name)
        }

        for (cn in oldCats) {
            def temp = Subcategorie.findByName(cn)
            cat.removeFromSubCats(temp)
            temp.parentCat = null
        }

        for (cn in newCats) {
            def temp = Subcategorie.findByName(cn)
            if (temp) cat.addToSubCats(temp)
        }
        cat.subCats.each {
            println(it.name)
            println(it.parentCat?.name)
        }
        return cat.save(flash: true)
    }
}
