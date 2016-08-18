package berlin.htw.hrz.kb

import grails.transaction.Transactional
import groovy.time.TimeCategory
import org.neo4j.graphdb.Result

import java.rmi.NoSuchObjectException

// TODO [TR]: "public": Access Scopes in Groovy ?
// TODO [TR]: bessere Typisierung insbesondere in Services ?


@Transactional
/**
 * Service which help you managing all kind of categories (main/sub) and also search for similar or 'user-relevant' documents
 */
class CategoryService {

    //def springSecurityService

    /**
     * Number of documents which should be returned, so that not all found docs will be returned
     */
    // TODO[TR]: parametrisierung ?
    def NumDocsToShow = 5

    /**
     * Adding the given doc to the given subcategories
     * @param doc
     * @param subCats
     * @return
     * @throws Exception
     */
    // TODO [TR]: wrapper für signature Document, Subcategory[]
    //todo ANPASSSEN AN NEUEN SIGNATUR!!!!!!!!!!!!!!
    boolean addDoc(Document doc, List<Subcategory> subCats) throws Exception {
        if (!subCats) { throw new IllegalArgumentException("Argument 'subCats' cant be null") }

        //save all the changes, save can't be made earlier, because otherwise it can happened that the doc will be associated with cats before a non-existing cat occurs and exception is thrown
        for (Subcategory cat in subCats) {
            if (Linker.link(cat, doc) && cat.validate()) { cat.save(flush: true) }
            else { throw new Exception('Somethinge went wrong') }
        }
        true
    }

    /**
     * Change the name of the given category
     * @param cat
     * @param newName
     * @return
     * @throws Exception
     */
    Category changeCategoryName(Category cat, String newName) throws Exception {
        if (!newName || newName.empty) { throw new IllegalArgumentException("Argument 'newName' can not be null or empty.") }
        cat.name = newName

        if (!cat.validate()) {
            cat.errors?.allErrors?.each { log.error(it) }
            throw new Exception('Validation of data wasn\'t successfull')
        }
        cat.save()
    }

    /**
     * Changes the parent of the given category
     * @param cat
     * @param newParent
     * @return
     * @throws Exception
     */
    Subcategory changeParent(Subcategory cat, Category newParent) throws Exception {
        if (!cat || !newParent) { throw new IllegalArgumentException('Argument can not be null.') }

        newParent.removeFromSubCats(cat)
        cat.parentCat = null
        newParent.addToSubCats(cat)
        if (cat.validate && newParent.validate && newParent.save(flush:true)) { cat }
        else { null }
    }

    /**
     * Change the associated subcategories for the given category
     * @param cat
     * @param newCats
     * @return
     * @throws Exception
     */
    Category changeSubCats(Category cat, String[] newCats) throws Exception {
        def tempCats = []

        //Hole benötigte Subcats
        for (cn in newCats) {
            Subcategory temp = getCategory(cn)
            tempCats.add(temp)
        }

        //räume alte Verweise auf
        // TODO [TR]: ist collect() hier nicht überflüssig ?
        cat.subCats.collect().each {
            it.parentCat = null
            it.save(flush: true)
        }
        cat.subCats.clear()

        //setze neue Beziehungen
        tempCats.each {
            cat.addToSubCats(it)
        }

        if (cat.validate()) { cat.save(flush: true) }
        else { null }
    }

    /**
     * Delete given category from the database
     * @param cat
     * @throws Exception
     */
    void deleteSubCategory(Subcategory cat) throws Exception {
        if (!cat) { throw new IllegalArgumentException('Argument can not be null') }
        cat.linker.collect().each { linker ->
            Linker.unlink(cat, linker.doc)
        }
        cat.delete()
    }

    /**
     * This method will search für additional documents (if forFaqs = false) for the given document, so you get a set of other relevant documents
     * or a set of Faqs (if forFaqs = true) which share the same associated subcategories
     * @param doc
     * @param forFaqs
     * @return hashmap of found documents in format [ faq:[...], article:[...], tutorial:[...] ]
     * @throws Exception
     */
    HashMap getAdditionalDocs(Document doc) throws Exception {
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
     * This methods will return all documents which are associated with the given subcategories
     * That mean, that only documents will be returned which are associated with all of the subcategories
     * @param subs
     * @return list of found documents
     * @throws Exception
     */
    // TODO [TR]: cipher injection ?
    // todo optimuerung anstatt ketzige form, lieber sub0->doc, sub1->doc where sub0.name AND sub1.name, dann fällt each, unique und sort weg
    List getAllDocsAssociatedToSubCategories(String[] subs) throws Exception {
        if (!subs) throw new IllegalArgumentException("Argument 'subs' can not be null.")
        def query = "MATCH (sub:Subcategory)-[r*..2]-(doc:Document) WHERE (sub.name='${subs[0]}' "
        subs = subs.drop(1)
        subs.each {
            query += "OR sub.name='${it}'"
        }
        query += ") RETURN sub.name AS subName, doc"
        Result result = Subcategory.cypherStatic(query)
        def myDocs = []
        result.each {
            myDocs.add(it.doc as Document)
        }

        myDocs.findAll { myDocs.count(it) == (subs.size() + 1) }.unique().sort { it.viewCount }
    }

    /**
     * Return all existing maincategories
     * @return
     */
    List getAllMainCats() {
        Category.findAll()?.findAll { !(it instanceof Subcategory) }
    }

    /**
     * Return all existing subcategories
     * @return
     */
    List getAllSubCats() {
        Subcategory.findAll()?.toList()
    }

    /**
     * Getting all associated subcategories for the given category
     * @param catName
     * @return found main- or subcategory
     * @throws Exception
     */
    List getAllSubCats(Category cat) throws Exception {
        if (!cat) { throw new IllegalArgumentException('Argument can not be null') }
        cat.subCats?.findAll()?.toList()
    }

    /**
     * Getting a single category by the given name
     * @param catName
     * @return found main- or subcategory
     * @throws Exception
     */
    //todo eigene Exception
    Category getCategory(String catName) throws Exception {
        if (!catName || catName == '') { throw new IllegalArgumentException("Argument can not be null or empty") }
        def cat = Category.findByName(catName)?:null
        if (!cat) { throw new NoSuchObjectException("Can not find a category with the name: '${catName}'") }
        cat
    }

    /**
     * Getting the document count of the given category
     * @param catName
     * @return number of associated categories, error-code if something went wrong
     * @throws Exception
     */
    Integer getDocCount(Subcategory cat) throws Exception {
        if (!cat) { throw new IllegalArgumentException('Argument can not be null') }
        cat.linker?.doc?.size()
    }

    /**
     * Getting all the associated docs from one subcategoy
     * @param cat
     * @return
     * @throws Exception
     */
    List getDocs(Subcategory cat) throws Exception {
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
     * @return
     * @throws Exception
     */
    HashMap getDocsOfInterest(def userPrincipals, def request) throws Exception {
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
            def docs = getAllDocsAssociatedToSubCategories(subCatNames as String[]).findAll { it instanceof Tutorial || it instanceof Article }.sort{ -it.viewCount }
            if (docs && !docs.empty) {
                if (docs.size() > NumDocsToShow) {
                    docs = docs.subList(0, NumDocsToShow)
                }
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
     * @param cat Category you want to search through
     * @return
     * @throws Exception
     */
    // TODO [TR]: könnte das nicht auch die getAllSubCats()-Methode tun, z.B. mit einem Parameter "boolean recurse=false"
    List getIterativeAllSubCats(String catName) throws Exception {
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
     * @param givenDoc
     * @param excludedMainCats
     * @param forFaqs
     * @return
     * @throws Exception
     */
    List getSameAssociatedDocs(Document givenDoc, String[] excludedMainCats, Boolean forTutorial=false) throws Exception {
        //prepare query
        def query = "MATCH (doc:Document) WHERE doc.docTitle='${givenDoc.docTitle}' WITH doc\n"
        excludedMainCats.eachWithIndex { catName, i ->
            query += "MATCH (doc)-[*..2]-(sub${i}:Subcategory)\n" +
                     "MATCH (sub${i})-[*]->(main${i}:Category{name:'${catName}'})\n" +
                     "MATCH (sub${i})-[*..2]-(otherDoc:Document)\n"
        }
        query += "RETURN distinct otherDoc ORDER BY otherDoc.viewCount"

        //fire query
        Result myResult = Subcategory.cypherStatic(query)

        if (forTutorial) { return myResult.toList(Document).findAll { it instanceof Tutorial && it != givenDoc } }
        else { return myResult.toList(Document).findAll { it instanceof Faq || (it instanceof Article && it != givenDoc) } }
    }

    /**
     *
     * @param catNames
     * @return
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
     * @param catName
     * @param mainCat
     * @param subCats default null, or a list of subcategory which should be associated with
     * @return
     * @throws Exception
     */
    Subcategory newSubCategory(String catName, Category parentCat, Subcategory[] subCats = null) throws IllegalArgumentException {
        if (!catName || catName.empty) { throw new IllegalArgumentException("Argument 'catName' can not be null or empty") }
        if (!parentCat) { throw new IllegalArgumentException("Argument 'mainCat' can not be null") }

        Subcategory newSub = new Subcategory(name: catName, parentCat: parentCat)

        for (Subcategory sub in subCats) {
            newSub.addToSubCats(sub)
        }

        newSub.save(flush: true)
    }
}
