package berlin.htw.hrz.kb

import grails.transaction.Transactional

@Transactional
class CategorieService {

    def serviceMethod() {

    }

    /**
     * This method will count all found documents in a given Subcategorie, returns -1 if subcategorie with the given name doesn't exist
     * @param catName Name of the Categorie you found to search through, as String
     * @return Count of all found documents, as Integer
     */
    def getDocCountOfSubCategorie(String catName) {
        Subcategorie subCat = Subcategorie.findByName(catName)
        if (!subCat) {
            return -1
        } else {
            return subCat.docs.findAll().size()
        }
    }

    /**
     * This method will return iterative all associated subcategories to the given categorie (either Maincategorie or Subcategorie)
     * @param cat Categorie you want to search through
     * @return Array of all found categories
     */
    def getIterativeAllSubCats(def cat) {
        def subs = []
        if (cat) {
            if (!(cat instanceof Maincategorie)) {
                subs += cat
            }
            cat.subCats?.each { child ->
                subs += getIterativeAllSubCats(child)
            }
            subs.unique()
        }
    }

    def getAllSubCats(def cat) {
        return (cat? cat.subCats.findAll().toArray() : null)
    }
}
