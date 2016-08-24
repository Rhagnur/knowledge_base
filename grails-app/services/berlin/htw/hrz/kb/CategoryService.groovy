/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

import grails.transaction.Transactional
import groovy.time.TimeCategory
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
    // TODO[TR]: parametrisierung ?
    def NumDocsToShow = 5

    /**
     * Adding the given doc to the given subcategories
     * @param a single document to add to subcategories
     * @param list of subcategories where the doc should be added
     * @return true if operation was successful
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    boolean addDoc(Document doc, List<Subcategory> subCats) throws IllegalArgumentException, ValidationErrorException {
        if (!subCats) { throw new IllegalArgumentException("Argument 'subCats' cant be null") }

        //save all the changes, save can't be made earlier, because otherwise it can happened that the doc will be associated with cats before a non-existing cat occurs and exception is thrown
        for (Subcategory cat in subCats) {
            if (cat.validate()) {
                Linker.link(cat, doc)
                cat.save(flush: true)
            }
            else { throw new ValidationErrorException('Validation was not successful!') }
        }
        true
    }

    /**
     * Change the name of the given category
     * @param subcategory which should be changed
     * @param new name of the subcategory
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
     * @param subcategory which should be changed
     * @param new parent for the subcategory
     * @return subcategory if successful
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Subcategory changeParent(Subcategory cat, Category newParent) throws IllegalArgumentException, ValidationErrorException {
        if (!cat || !newParent) { throw new IllegalArgumentException('Argument can not be null.') }

        newParent.removeFromSubCats(cat)
        cat.parentCat = null
        newParent.addToSubCats(cat)
        if (cat.validate && newParent.validate && newParent.save(flush:true)) { cat }
        else { throw new ValidationErrorException('Validation for category-data was not successful!') }
    }

    /**
     * Change the associated subcategories for the given category
     * @param subcategory which should be changed
     * @param list of new subcategories which should be associated
     * @return subcategory if successful
     * @throws ValidationErrorException
     */
    Category changeSubCats(Category cat, List<Subcategory> newSubcats) throws ValidationErrorException {
        //räume alte Verweise auf
        cat.subCats.each {
            it.parentCat = null
            it.save(flush: true)
        }
        cat.subCats.clear()

        //setze neue Beziehungen
        newSubcats.each {
            cat.addToSubCats(it)
        }

        if (cat.validate()) { cat.save(flush: true) }
        else { throw new ValidationErrorException('Validation for category-data was not successful!') }
    }

    /**
     * Delete given category from the database
     * @param subcategory which should be deleted
     * @throws IllegalArgumentException
     */
    void deleteSubCategory(Subcategory cat) throws IllegalArgumentException {
        if (!cat) { throw new IllegalArgumentException('Argument can not be null') }
        cat.linker.collect().each { linker ->
            Linker.unlink(cat, linker.doc)
        }
        cat.delete(flush: true)
    }

    /**
     * This method will search für additional documents (if forFaqs = false) for the given document, so you get a set of other relevant documents
     * or a set of Faqs (if forFaqs = true) which share the same associated subcategories
     * @param document which should be used to find related documents for
     * @return hashmap of found documents in format [ faq:[...], article:[...], tutorial:[...] ]
     * @throws IllegalArgumentException
     */
    HashMap getAdditionalDocs(Document doc) throws IllegalArgumentException {
        def myDocs = [:]
        def start, stop

        println('FAQ und Artikel')
        start = new Date()
        def temp = getSameAssociatedDocs(doc, ['theme', 'os'] as String[])
        if (temp) {
            myDocs.faq = temp.findAll { it instanceof Faq }
            myDocs.article = temp.findAll { it instanceof Article }
        }
        stop = new Date()
        println('Benötigte Zeit: ' + TimeCategory.minus(stop, start))


        println('Anleitungen')
        start = new Date()
        myDocs.tutorial = getSameAssociatedDocs(doc, ['os', 'lang'] as String[], true)
        stop = new Date()
        println('Benötigte Zeit: ' + TimeCategory.minus(stop, start))
        return myDocs
    }

    /**
     * This methods searchs for document (if needed of specific type) which are associated to all given subcategories
     * @param string array of subcategory-names, CAN NOT be null
     * @param string array of doctype-names, can be null
     * @return list of found documents
     * @throws IllegalArgumentException
     */
    List getAllDocsAssociatedToSubCategories(String[] subs, String[] docTypes) throws IllegalArgumentException {
        if (!subs) throw new IllegalArgumentException("Argument 'subs' can not be null.")
        def query = ""
        def queryParams = [:]
        subs.eachWithIndex { String sub, i ->
            query += "MATCH (sub${i}:Subcategory) WHERE sub${i}.name='{sub${i}}'\n" +
                    "MATCH (sub${i})-[*..2]-(doc:Document)\n"
            queryParams.put("sub${i}" as String, sub)
        }
        docTypes.eachWithIndex{ String docType,  i ->
            //Muss leider so umständlich gemacht werden, da das Label nicht parametrisiert werden kann, execute() wirft sonst einen Fehler
            if (docType!= 'Tutorial' && docType!='Article' && docType!='Faq') { throw new IllegalArgumentException("Wrong argument given in 'docTypes[]'!") }
            if (i == 0) { query+='WHERE ' }
            query += "(doc:${docType})"
            if (i < docTypes.size() - 1) { query+=' OR ' }
        }

        query += "\nRETURN doc ORDER BY doc.viewCount DESC LIMIT ${NumDocsToShow}"
        //println(query)
        //println(queryParams)
        Result result = graphDatabaseService.execute(query, queryParams)
        result.toList(Document)
    }

    /**
     * Return all existing categories that are not instance of subcategory
     * @return list of all found categories
     */
    List getAllMainCats() {
        Category.findAll()?.findAll { !(it instanceof Subcategory) }
    }

    /**
     * Getting all categories with all of its associated subcategories
     * @param list of excluded categories, can be null
     * @return Map of all found entries, where key is the category and the value the associated subcategories
     */
    HashMap getAllMaincatsWithSubcats(List<Category> excludedCat = null) {
        def all = [:]
        getAllMainCats().each { Category mainCat ->
            def temp = []
            if (!excludedCat?.contains(mainCat)) {
                getIterativeAllSubCats(mainCat.name).each { cat ->
                    temp.add(cat.name as String)
                }
                all.put(mainCat.name, temp.sort{ it })
            }
        }
        all
    }

    /**
     * Returns all existing subcategories
     * @return list of all found subcategories
     */
    List getAllSubCats() {
        Subcategory.findAll()?.toList()
    }

    /**
     * Getting all associated subcategories for the given category
     * @param name of the category from which you want all associated subcategories (depth: 1)
     * @return list of all found subcategories
     * @throws IllegalArgumentException
     */
    List getAllSubCats(Category cat) throws IllegalArgumentException {
        if (!cat) { throw new IllegalArgumentException('Argument can not be null') }
        cat.subCats?.findAll()?.toList()
    }



    /**
     * Getting a single category by the given name
     * @param name of the subcategory
     * @return found main- or subcategory
     * @throws IllegalArgumentException
     * @throws NoSuchObjectFoundException
     */
    Category getCategory(String catName) throws IllegalArgumentException, NoSuchObjectFoundException {
        if (!catName || catName == '') { throw new IllegalArgumentException("Argument can not be null or empty") }
        def cat = Category.findByName(catName)?:null
        if (!cat) { throw new NoSuchObjectFoundException("Can not find a category with the name: '${catName}'") }
        cat
    }

    /**
     * Getting the document count of the given category
     * @param name of the subcategory
     * @return number of associated categories, error-code if something went wrong
     * @throws IllegalArgumentException
     */
    Integer getDocCount(Subcategory cat) throws IllegalArgumentException {
        if (!cat) { throw new IllegalArgumentException('Argument can not be null') }
        cat.linker?.doc?.size()
    }

    /**
     * Getting all the associated docs from one subcategoy
     * @param cat
     * @return
     * @throws IllegalArgumentException
     */
    List getDocs(Subcategory cat) throws IllegalArgumentException {
        if (!cat) { throw new IllegalArgumentException('Argument can not be null') }
        cat.linker.doc.toList()
    }

    //todo: anstatt Pincipal zu übergeben vll direkt hier im Service injecten und nutzen
    //todo: optimieren
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
    HashMap getDocsOfInterest(def userPrincipals, def request) throws IllegalArgumentException {
        def subCatNames = []
        HashMap docMap = [:]
        def start, stop, temp

        println(userPrincipals.authorities)

        println('1 Os')
        start = new Date()
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
                temp = temp.subList(0, NumDocsToShow)
            }

            docMap.put(osName, temp)
            subCatNames.add(osName)
        }
        stop = new Date()
        println('Benötigte Zeit: ' + TimeCategory.minus(stop, start))

        println('2 Group')
        start = new Date()
        //2 Get the documents of the associated groups [ROLE_GP-STAFF, ROLE_GP-STUD]
        if (userPrincipals.authorities.any { it.authority == ("ROLE_GP-PROF" || "ROLE_GP-LBA") }) {
            temp = getDocs(getCategory('faculty') as Subcategory).findAll { it instanceof Tutorial || it instanceof Article }.sort { -it.viewCount }
            if (temp.size() > NumDocsToShow) {
                temp = temp.subList(0, NumDocsToShow)
            }

            docMap.put('faculty', temp)
            subCatNames.add('faculty')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STAFF" }) {
            temp = getDocs(getCategory('staff') as Subcategory).findAll { it instanceof Tutorial || it instanceof Article }.sort { -it.viewCount }
            if (temp.size() > NumDocsToShow) {
                temp = temp.subList(0, NumDocsToShow)
            }

            docMap.put('staff', temp)
            subCatNames.add('staff')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STUD" }) {
            temp = getDocs(getCategory('student') as Subcategory).findAll { it instanceof Tutorial || it instanceof Article }.sort { -it.viewCount }
            if (temp.size() > NumDocsToShow) {
                temp = temp.subList(0, NumDocsToShow)
            }

            docMap.put('student', temp)
            subCatNames.add('student')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_ANONYMOUS" }) {
            temp = getDocs(getCategory('anonym') as Subcategory).findAll { it instanceof Tutorial || it instanceof Article }.sort { -it.viewCount }
            if (temp.size() > NumDocsToShow) {
                temp = temp.subList(0, NumDocsToShow)
            }

            docMap.put('anonym', temp)
            subCatNames.add('anonym')
        }
        stop = new Date()
        println('Benötigte Zeit: ' + TimeCategory.minus(stop, start))

        println('3 Popular')
        start = new Date()
        //3 Get the popularest docs
        temp = Document.findAll(max: NumDocsToShow, sort: 'viewCount', order: 'desc')
        docMap.put('popular', temp)
        stop = new Date()
        println('Benötigte Zeit: ' + TimeCategory.minus(stop, start))

        println('4 Neuste')
        start = new Date()
        //3 Get the popularest docs
        temp = Document.findAll(max: NumDocsToShow, sort: 'createDate', order: 'desc')
        docMap.put('newest', temp)
        stop = new Date()
        println('Benötigte Zeit: ' + TimeCategory.minus(stop, start))

        println('5 Suggestion')
        start = new Date()
        //4 Get suggestions, sugg are associated to OS and the user-groups
        while (subCatNames && !subCatNames.empty) {
            def docs = getAllDocsAssociatedToSubCategories(subCatNames as String[], ['Tutorial', 'Article'] as String[])
            if (docs && !docs.empty) {
                docMap.put('suggestion', docs)
                break
            } else {
                subCatNames.remove(subCatNames.last())
            }
        }
        stop = new Date()
        println('Benötigte Zeit: ' + TimeCategory.minus(stop, start))

        docMap.sort { -(it.value.size()) } as HashMap
    }

    /**
     * This method will return iterative all associated subcategories to the given category (either Category or Subcategory)
     * @param category you want to search through
     * @return ist of all found subcategories
     * @throws IllegalArgumentException
     * @throws NoSuchObjectFoundException
     */
    List getIterativeAllSubCats(String catName) throws IllegalArgumentException, NoSuchObjectFoundException {
        def subs = []
        Category cat = getCategory(catName)
        if (cat) {
            if (cat instanceof Subcategory) {
                subs += cat
            }
            cat.subCats?.each { child ->
                subs += getIterativeAllSubCats(child.name)
            }
            subs.unique()
        }
    }

    /**
     * This method will search for similar docs by checking the connection to the maincategories
     * You can exclude maincategories for a results. That means, if your first lookup didn't find anything, exclude less important maincategories and search again
     * @param document to look up for
     * @param array-string for categories which should be excluded
     * @param optional parameter
     * @return list of found documents
     * @throws IllegalArgumentException
     */
    List getSameAssociatedDocs(Document givenDoc, String[] excludedMainCats, Boolean forTutorial=false) throws IllegalArgumentException {
        if (!excludedMainCats) { throw new IllegalArgumentException("Argument 'excludedMainCats' can not be null") }
        //prepare query
        def start, end
        def queryParams = [:]
        start = new Date()
        def query = "MATCH (doc:Document) WHERE doc.docTitle='${givenDoc.docTitle}' WITH doc\n"
        excludedMainCats.eachWithIndex { String catName, i ->
            query += "MATCH (doc)-[*..2]-(sub${i}:Subcategory)\n" +
                     "MATCH (sub${i})-[*]->(main${i}:Category{name:{catName${i}}})\n" +
                     "MATCH (sub${i})-[*..2]-(otherDoc:Document)\n"
            queryParams.put("catName${i}" as String, catName)
        }
        if (forTutorial) { query += "WHERE (otherDoc:Tutorial) AND otherDoc.docTitle<>'${givenDoc.docTitle}'\n"}
        else { query += "WHERE ((otherDoc:Article) OR (otherDoc:Faq)) AND otherDoc.docTitle<>'${givenDoc.docTitle}'\n"}
        query += "RETURN distinct otherDoc ORDER BY otherDoc.docTitle"

        //fire query
        //println(query)
        //println(queryParams)
        //Result myResult = Subcategory.cypherStatic(query)
        Result myResult = graphDatabaseService.execute(query, queryParams)
        end = new Date()
        println('Queryzeit: ' + TimeCategory.minus(end, start))
        myResult.toList(Document)
    }

    /**
     * Getting all subcategories from a array of names
     * @param string-array of given names
     * @return list of found subcategories
     * @throws IllegalArgumentException
     */
    List<Subcategory> getSubcategories(String[] catNames) throws IllegalArgumentException {
        if (!catNames) { throw new IllegalArgumentException("Argument 'catNames' can not be null") }
        List subCats = []
        catNames.each { catName ->
            subCats.add(getCategory(catName))
        }
        subCats
    }

    /**
     * Adding a new subcategory to the database
     * @param name of the new subcategory
     * @param parent of the new subcategory, can be a category or a subcategory
     * @param default null, or a list of subcategory which should be associated with
     * @return subcategory if successful
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Subcategory newSubCategory(String catName, Category parentCat, Subcategory[] subCats = null) throws IllegalArgumentException, ValidationErrorException {
        if (!catName || catName.empty) { throw new IllegalArgumentException("Argument 'catName' can not be null or empty") }
        if (!parentCat) { throw new IllegalArgumentException("Argument 'mainCat' can not be null") }

        Subcategory newSub = new Subcategory(name: catName, parentCat: parentCat)

        for (Subcategory sub in subCats) {
            newSub.addToSubCats(sub)
        }

        if (newSub.validate()) { newSub.save(flush: true) }
        else { throw new ValidationErrorException('Validation for category-data was not successful!') }
    }
}
