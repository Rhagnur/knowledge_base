/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

import grails.transaction.Transactional
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Result


/**
 * Service which help you managing all kind of categories (main/sub) and also search for similar or 'user-relevant' documents
 */
@Transactional
class CategoryService {

    /**
     * Injection for getting access to the graphDatabaseService object. Necessary to execute self build queries.
     */
    GraphDatabaseService graphDatabaseService

    /**
     * Number of documents which should be returned, so that not all found docs will be returned
     */
    // TODO[TR]: parametrisierung?
    //todo: Vll auch, dass später der User selbst die Einstellung setzen kann.
    def NumDocsToShow = 5

    /*
     * Adding the given doc to the given subcategories
     * @param doc single document to add to subcategories
     * @param subCats of subcategories where the doc should be added
     * @return true if operation was successful
     * @throws IllegalArgumentException
     * @throws ValidationErrorException

    //todo: Noch drin gelassen um zu verstehen, warum diese Methode alle Subkategorien zuordnet, die Methode docService.changeParents aber die letzte verliert?
    boolean addDoc(Document doc, List<Subcategory> subCats) throws IllegalArgumentException, ValidationErrorException {
        if (!subCats) { throw new IllegalArgumentException("Argument 'subCats' cant be null") }

        for (Subcategory cat in subCats) {
            if (cat.validate()) {
                Linker.link(cat, doc)
                doc.save()
                cat.save(flush:true)
            }
            else { throw new ValidationErrorException('Validation was not successful!') }
        }
        true
    }*/

    /**
     * Change the name of the given category
     * @param cat subcategory which should be changed
     * @param newName new name of the subcategory
     * @return subcategory if successful
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Category changeCategoryName(Subcategory cat, String newName) throws IllegalArgumentException, ValidationErrorException {
        if (!newName || newName.empty) { throw new IllegalArgumentException("Argument 'newName' can not be null or empty.") }
        cat.name = newName

        if (!cat.validate()) {
            cat.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for category-data was not successful!')
        }
        cat.save()
    }

    /**
     * Changes the parent of the given category
     * @param cat subcategory which should be changed
     * @param newParent new parent for the subcategory
     * @return subcategory if successful
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Subcategory changeParent(Subcategory cat, Category newParent) throws IllegalArgumentException, ValidationErrorException {
        if (!cat || !newParent) { throw new IllegalArgumentException('Argument can not be null.') }

        //newParent.removeFromSubCats(cat)
        cat.parentCat = null
        newParent.addToSubCats(cat)
        if (cat.validate() && newParent.validate() && newParent.save(flush:true)) { cat }
        else {
            cat.errors?.allErrors?.each { log.error(it) }
            newParent.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for category-data was not successful!')
        }
    }

    /**
     * Method will delete given category from the database
     * @param cat subcategory which should be deleted
     * @throws IllegalArgumentException
     */
    void deleteSubCategory(Subcategory cat) throws IllegalArgumentException {
        if (!cat) { throw new IllegalArgumentException('Argument can not be null') }
        cat.linker.collect().each { linker ->
            Linker.unlink(cat, linker.doc)
        }
        cat.delete()
    }

    /**
     * Method for getting all unassociated subcategories
     * @return List all found subcategories
     */
    List<Subcategory> findUnlinkedSubcats() {
        return Subcategory.findAll().findAll { !it.parentCat } as List<Subcategory>
    }

    /**
     * This method will search für additional documents (if forFaqs = false) for the given document, so you get a set of other relevant documents
     * or a set of Faqs (if forFaqs = true) which share the same associated subcategories
     * @param doc document which should be used to find related documents for
     * @return Map of found documents
     * @throws IllegalArgumentException
     */
    Map<String, List<Document>> getAdditionalDocs(Document doc) throws IllegalArgumentException {
        def myDocs = [:]

        def temp = getSameAssociatedDocs(doc, ['theme', 'os'] as String[])
        if (temp) {
            myDocs.faq = temp.findAll { it instanceof Faq }
            myDocs.article = temp.findAll { it instanceof Article }
        }

        myDocs.tutorial = getSameAssociatedDocs(doc, ['os', 'lang'] as String[], true)
        return myDocs
    }

    /**
     * This methods searchs for document (if needed of specific type) which are associated to all given subcategories
     * @param subs string array of subcategory-names, CAN NOT be null
     * @param docTypes string array of doctype-names, can be null
     * @return list of found documents
     * @throws IllegalArgumentException
     */
    List<Document> getAllDocsAssociatedToSubCategories(String[] subs, String[] docTypes) throws IllegalArgumentException {
        if (!subs) throw new IllegalArgumentException("Argument 'subs' can not be null.")
        def query = ""
        def queryParams = [:]
        //Baue die Query, sieht etwas umständlich aus, beugt aber 'cartesian product' vor und verbessert damit Performance
        subs.eachWithIndex { String sub, i ->
            query += "MATCH (sub${i}:Subcategory) WHERE sub${i}.name={${i}}\n" +
                     "MATCH (sub${i})<-[:SUBCAT]-(:Linker)-[:DOC]->(doc:Document)\n"
            queryParams.put(i as String, sub)
        }
        docTypes.eachWithIndex{ String docType,  i ->
            //Muss leider so umständlich gemacht werden, da das Label nicht parametrisiert werden kann, execute() wirft sonst einen Fehler
            if (docType!= 'Tutorial' && docType!='Article' && docType!='Faq') { throw new IllegalArgumentException("Wrong argument given in 'docTypes[]'!") }
            if (i == 0) { query+='WHERE ' }
            query += "(doc:${docType})"
            if (i < docTypes.size() - 1) { query+=' OR ' }
        }

        query += "\nRETURN doc ORDER BY doc.viewCount DESC LIMIT ${NumDocsToShow}"
        //Feuer die Query, durch parametrisierung, sollte Injection vorgebeugt werden
        Result result = graphDatabaseService.execute(query, queryParams)
        result.toList(Document)
    }

    /**
     * Returns all existing categories that are not instance of subcategory
     * @return list of all found categories
     */
    List<Category> getAllMainCats() {
        Category.findAll()?.findAll { !(it instanceof Subcategory) }
    }

    /**
     * Gets all categories with all of its associated subcategories
     * @param excludedCats list of excluded categories, can be null
     * @return Map of all found entries, where key is the category and the value the associated subcategories
     */
    Map<String, List<String>> getAllMaincatsWithSubcats(List<Category> excludedCats = null) {
        def all = [:]
        getAllMainCats().each { Category mainCat ->
            def temp = []
            if (!excludedCats?.contains(mainCat)) {
                getIterativeAllSubCats(mainCat).each { Subcategory cat ->
                    temp.add(cat.name as String)
                }
                all.put(mainCat.name, temp.sort{ it } as List<String>)
            }
        }
        all
    }

    /**
     * Returns all existing subcategories
     * @return list of all found subcategories
     */
    List<Subcategory> getAllSubCats() {
        Subcategory.findAll()?.toList()
    }

    /**
     * Gets all associated subcategories for the given category
     * @param cat name of the category from which you want all associated subcategories (depth: 1)
     * @return list of all found subcategories
     * @throws IllegalArgumentException
     */
    List<Subcategory> getAllSubCats(Category cat) throws IllegalArgumentException {
        if (!cat) { throw new IllegalArgumentException('Argument can not be null') }
        cat.subCats?.findAll()?.toList()
    }



    /**
     * Gets a single category by the given name
     * @param catName name of the subcategory
     * @return found main- or subcategory
     * @throws IllegalArgumentException
     * @throws NoSuchObjectFoundException
     */
    Category getCategory(String catName) throws IllegalArgumentException, NoSuchObjectFoundException {
        if (!catName) { throw new IllegalArgumentException("Argument 'catName' CAN NOT be null!") }
        def cat = Category.findByName(catName)?:null
        if (!cat) { throw new NoSuchObjectFoundException("Can not find a category with the name: '${catName}'") }
        cat
    }

    /**
     * Gets all the associated docs from one subcategory
     * @param cat subcategory for looking up for documents
     * @return list of found documents
     * @throws IllegalArgumentException
     */
    List<Document> getDocs(Subcategory cat) throws IllegalArgumentException {
        if (!cat) { throw new IllegalArgumentException("Argument 'cat' CAN NOT be null!") }
        cat.linker.doc.toList()
    }

    /**
     * This method will look up for documents which could be interesting for the user.
     * The category for the 'Docs of interest' are separated in 'operating system', 'group', 'popular', 'newest' and 'suggestion'.
     * The docs in 'suggestion' are found by the associated group of the user and his operating systems.
     * For every category there will be only the first five documents shown sorted by the view-count.
     * @param userPrincipals
     * @param request
     * @return hashmap of found results. Format can be like [<os_name>:list<docs>, <groud_name>:list<docs>,...]
     * @throws IllegalArgumentException
     */
    Map<String, List<Document>> getDocsOfInterest(def userPrincipals, def request) throws IllegalArgumentException {
        def subCatNames = []
        HashMap docMap = [:]
        def start, stop, temp

        //1 Get docs from associated OS []
        String osName = ''
        //process the os information from the request header
        // TODO [TR]: würde es vielleicht ein generischer Algorithmus auf Basis einer Map tun ? z.B. in application.groovy ?
        if (request.getHeader('User-Agent').toString().toLowerCase().contains('linux')) {
            osName = 'linux'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('android')) {
            osName = 'android'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('windows nt 6.1')) {
            osName = 'win_7'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('windows nt 6.2')) {
            osName = 'win_8'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('windows nt 10.0')) {
            osName = 'win_10'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('windows')) {
            osName = 'windows'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('iphone os 6')) {
            osName = 'ios_6'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('iphone os 7')) {
            osName = 'ios_7'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('iphone os 9')) {
            osName = 'ios_9'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('os x 10_8')) {
            osName = 'mac_108'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('os x 10_9')) {
            osName = 'mac_109'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('os x 10_10')) {
            osName = 'mac_1010'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('os x 10_11')) {
            osName = 'mac_1011'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('os x')) {
            osName = 'mac'
        }

        if (osName && osName != '') {
            temp = getDocs(getCategory(osName) as Subcategory).findAll { it instanceof Tutorial || it instanceof Article }.sort { -it.viewCount }
            if (temp.size() > NumDocsToShow) {
                temp = temp.subList(0, NumDocsToShow) as List<Document>
            }

            docMap.put(osName, temp)
            subCatNames.add(osName)
        }

        //2 Get the documents of the associated groups [ROLE_GP-STAFF, ROLE_GP-STUD]
        if (userPrincipals.authorities.any { it.authority == ("ROLE_GP-PROF" || "ROLE_GP-LBA") }) {
            temp = getDocs(getCategory('faculty') as Subcategory).findAll { it instanceof Tutorial || it instanceof Article }.sort { -it.viewCount }
            if (temp.size() > NumDocsToShow) {
                temp = temp.subList(0, NumDocsToShow) as List<Document>
            }

            docMap.put('faculty', temp)
            subCatNames.add('faculty')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STAFF" }) {
            temp = getDocs(getCategory('staff') as Subcategory).findAll { it instanceof Tutorial || it instanceof Article }.sort { -it.viewCount }
            if (temp.size() > NumDocsToShow) {
                temp = temp.subList(0, NumDocsToShow) as List<Document>
            }

            docMap.put('staff', temp)
            subCatNames.add('staff')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STUD" }) {
            temp = getDocs(getCategory('student') as Subcategory).findAll { it instanceof Tutorial || it instanceof Article }.sort { -it.viewCount }
            if (temp.size() > NumDocsToShow) {
                temp = temp.subList(0, NumDocsToShow) as List<Document>
            }

            docMap.put('student', temp)
            subCatNames.add('student')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_ANONYMOUS" }) {
            temp = getDocs(getCategory('anonym') as Subcategory).findAll { it instanceof Tutorial || it instanceof Article }.sort { -it.viewCount }
            if (temp.size() > NumDocsToShow) {
                temp = temp.subList(0, NumDocsToShow) as List<Document>
            }

            docMap.put('anonym', temp)
            subCatNames.add('anonym')
        }

        //3 Get the popularest docs
        temp = Document.findAll(max: NumDocsToShow, sort: 'viewCount', order: 'desc')
        docMap.put('popular', temp as List<Document>)

        //3 Get the popularest docs
        temp = Document.findAll(max: NumDocsToShow, sort: 'createDate', order: 'desc')
        docMap.put('newest', temp as List<Document>)

        //4 Get suggestions, sugg are associated to OS and the user-groups
        while (subCatNames && !subCatNames.empty) {
            def docs = getAllDocsAssociatedToSubCategories(subCatNames as String[], ['Tutorial', 'Article'] as String[])
            if (docs) {
                docMap.put('suggestion', docs as List<Document>)
                break
            } else {
                subCatNames.remove(subCatNames.last())
            }
        }

        docMap.sort { -(it.value.size()) }
    }

    /**
     * This method will return iterative all associated subcategories to the given category (either Category or Subcategory)
     * @param catName category you want to search through
     * @return ist of all found subcategories
     * @throws IllegalArgumentException
     * @throws NoSuchObjectFoundException
     */
    List<Subcategory> getIterativeAllSubCats(Category cat) throws IllegalArgumentException, NoSuchObjectFoundException {
        def subs = []
        if (cat) {
            if (cat instanceof Subcategory) {
                subs += cat
            }
            cat.subCats?.each { child ->
                subs += getIterativeAllSubCats(child)
            }
            subs.unique()
        }
        else { throw new IllegalArgumentException("Argument 'cat' CAN NOT be null!") }
    }

    /**
     * This method will search for similar docs by checking the connection to the maincategories
     * You can choose which maincategories are important for a results. That means, if your first lookup didn't find anything, exclude some less important maincategories and search again
     * @param givenDoc document to look up for
     * @param importantMainCats array-string for 'important' categories
     * @param forTutorial optional parameter
     * @return list of found documents
     * @throws IllegalArgumentException
     */
    List<Document> getSameAssociatedDocs(Document givenDoc, String[] importantMainCats, Boolean forTutorial=false) throws IllegalArgumentException {
        if (!importantMainCats) { throw new IllegalArgumentException("Argument 'importantMainCats' can not be null") }
        def queryParams = [:]

        def query = "MATCH (doc:Document) WHERE doc.docTitle='${givenDoc.docTitle}' WITH doc\n"
        importantMainCats.eachWithIndex { String catName, i ->
            query += "MATCH (doc)<-[:DOC]-(:Linker)-[:SUBCAT]->(sub${i}:Subcategory)-[*..5]->(main${i}:Category{name:{catName${i}}})\n" +
                     "MATCH (sub${i})<-[:SUBCAT]-(:Linker)-[:DOC]->(otherDoc:Document)\n"
            queryParams.put("catName${i}" as String, catName)
        }
        if (forTutorial) { query += "WHERE otherDoc.docTitle<>'${givenDoc.docTitle}' AND (otherDoc:Tutorial) \n"}
        else { query += "WHERE otherDoc.docTitle<>'${givenDoc.docTitle}' AND ((otherDoc:Article) OR (otherDoc:Faq))\n"}
        query += "RETURN distinct otherDoc ORDER BY otherDoc.docTitle"

        Result myResult = graphDatabaseService.execute(query, queryParams)
        myResult.toList(Document)
    }

    /**
     * Gets all subcategories from a array of names
     * @param catNames string-array of given names
     * @return list of found subcategories
     * @throws IllegalArgumentException
     * @throws NoSuchObjectFoundException
     */
    List<Subcategory> getSubcategories(String[] catNames) throws IllegalArgumentException, NoSuchObjectFoundException {
        if (!catNames) { throw new IllegalArgumentException("Argument 'catNames' can not be null") }
        List subCats = []
        catNames.each { catName ->
            subCats.add(getCategory(catName))
        }
        subCats
    }

    /**
     * Adds a new subcategory to the database
     * @param catName name of the new subcategory
     * @param parentCat parent of the new subcategory, can be a category or a subcategory
     * @param subCats optional, a list of subcategory which should be associated with
     * @return subcategory if successful
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Subcategory newSubCategory(String catName, Category parentCat) throws IllegalArgumentException, ValidationErrorException {
        if (!catName) { throw new IllegalArgumentException("Argument 'catName' can not be null or empty") }

        Subcategory newSub = new Subcategory(name: catName, parentCat: parentCat)

        if (!newSub.validate()) {
            newSub.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for category-data was not successful!')
        }
        newSub.save(flush: true)
    }
}
