package berlin.htw.hrz.kb

import grails.transaction.Transactional
import groovy.time.TimeCategory
import org.neo4j.graphdb.Result

import java.rmi.NoSuchObjectException

@Transactional
/**
 * Service which help you managing all kind of categories (main/sub) and also search for similar or 'user-relevant' documents
 */
class CategoryService {

    def springSecurityService

    /**
     * Number of documents which should be returned, so that not all found docs will be returned
     */
    def NumDocsToShow = 5

    /**
     * Adding the given doc to the given subcategories
     * @param doc
     * @param subCats
     * @return
     * @throws Exception
     */
    def addDoc(def doc, String[] subCats) throws Exception {
        def foundSubCats = []
        println('addDoc called')
        println(doc)

        if (!subCats) throw new IllegalArgumentException("Argument 'subCats' cant be null")

        //check if there is any not-existing subcategory given in the argument string-array, if so throw exception and cancel the method without associating doc to any subcategory
        for (def cat in subCats) {
            Subcategory subCat = Subcategory.findByName(cat)
            if (subCat) foundSubCats.add(subCat)
            else throw new Exception("Subcategory '${cat}' do not exist, operation 'addDoc' canceled.")
        }

        println(foundSubCats)
        //save all the changes, save can't be made earlier, because otherwise it can happened that the doc will be associated with cats before a non-existing cat occurs and exception is thrown
        for (Subcategory cat in foundSubCats) {
            cat.addToDocs(doc)
            cat.save(flush: true)
        }
        return true
    }

    /**
     * Change the name of the given category
     * @param oldName
     * @param newName
     * @return Errorcode as int, error-code if something went wrong
     */
    def changeCategoryName(def cat, String newName) {
        if (!newName || newName.empty) throw new IllegalArgumentException("Argument 'newName' can not be null or empty.")
        cat.name = newName

        if (!cat.validate()) {
            cat.errors?.allErrors?.each { log.error(it) }
            throw new Exception('Validation of data wasn\'t successfull')
        }
        return cat.save()
    }

    //todo: changeParent
    //def changeParent

    //todo: Rausfinden warum Änderung temporär funktioniert aber NIE in die Datenbank gelangt
    //Lösung 1: Zu ändernen Knoten löschen und mit neuen Beziehungn erstellen...meeeeh
    /**
     * Change the associated subcategories for the given category
     * @param catName
     * @param newCats
     * @return error-code as int, please have a look at the description of the class
     */
    def changeSubCats(def cat, String[] newCats) {
        def tempCat
        def parent = null

        if (!(cat instanceof Maincategory)) {
            parent = cat.mainCat ?: cat.parentCat ?: null

            if (parent instanceof Maincategory) {
                tempCat = new Subcategory(name: cat.name, mainCat: parent)
            } else {
                tempCat = new Subcategory(name: cat.name, parentCat: parent)
            }
        } else {
            tempCat = new Maincategory(name: cat.name)
        }

        for (cn in newCats) {
            def temp = getCategory(cn)
            tempCat.addToSubCats(temp)
        }
        cat.delete()
        tempCat.save(flush: true)
    }

    /**
     * Delete given category from the database
     * @param cat
     * @return
     */
    def deleteCategory(def cat) {
        if (!cat) throw new IllegalArgumentException('Argument can not be null')
        if (!(cat instanceof Subcategory) && !(cat instanceof Maincategory)) throw new IllegalArgumentException("Argument has wrong type, solution: 'berlin.htw.hrz.kb.Maincategory' or 'berlin.htw.hrz.kb.Subcategory'")

        cat.delete(flush: true)
    }

    /**
     * This method will search für similar documents (if forFaqs = false) for the given document, so you get a set of other relevant documents
     * or a set of Faqs (if forFaqs = true) which share the same associated subcategories
     * @param doc
     * @param forFaqs
     * @return
     * @throws Exception
     */
    def getAdditionalDocs(Document doc, Boolean forFaqs = false) throws Exception {
        def excludedMainCats = []
        excludedMainCats.add('group')
        excludedMainCats.add('author')
        def start, stop

        if (forFaqs) {
            start = new Date()
            println('FAQ')
            def relatedFaqs = getSameAssociatedDocs(doc, excludedMainCats as String[], true)
            if (!relatedFaqs) {
                excludedMainCats.add('lang')
                relatedFaqs = getSameAssociatedDocs(doc, excludedMainCats as String[], true)
            }
            stop = new Date()
            println('Benötigte Zeit: ' + TimeCategory.minus(stop, start))
            return relatedFaqs
        }

        println('DOCS')
        start = new Date()
        excludedMainCats.add('theme')
        def similarDocs = getSameAssociatedDocs(doc, excludedMainCats as String[])
        stop = new Date()
        println('Benötigte Zeit: ' + TimeCategory.minus(stop, start))
        return similarDocs
    }

    /**
     * This methods will return all documents which are associated with the given subcategories
     * That mean, that only documents will be returned which are associated with all of the subcategories
     * @param subs
     * @return
     * @throws Exception
     */
    def getAllDocsAssociatedToSubCategories(String[] subs) throws Exception {
        if (!subs) throw new IllegalArgumentException("Argument 'subs' can not be null.")
        def query = "MATCH (sub:Subcategory)-[:DOCS]->(doc:Document) WHERE (sub.name='${subs[0]}' "
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

        myDocs = myDocs.findAll { myDocs.count(it) == (subs.size() + 1) }.unique().sort { it.viewCount }

        return myDocs
    }

    /**
     * Getting all associated subcategories for the given category
     * @param catName
     * @return found main- or subcategory
     */
    def getAllSubCats(def cat) {
        if (!cat) throw new IllegalArgumentException('Argument can not be null')
        if (!(cat instanceof Subcategory) && !(cat instanceof Maincategory)) throw new IllegalArgumentException("Argument has wrong type, solution: 'berlin.htw.hrz.kb.Maincategory' or 'berlin.htw.hrz.kb.Subcategory'")

        return cat.subCats?.findAll()
    }

    /**
     * Getting a single category by the given name
     * @param catName
     * @return found main- or subcategory
     */
    def getCategory(String catName) {
        if (!catName || catName == '') throw new IllegalArgumentException()
        def cat = Maincategory.findByName(catName) ?: Subcategory.findByName(catName) ?: null
        if (!cat) throw new NoSuchObjectException("Can not find a category with the name: '${catName}'")
        return cat
    }

    /**
     * Getting the document count of the given category
     * @param catName
     * @return number of associated categories, error-code if something went wrong
     */
    def getDocCount(Subcategory cat) {
        if (!cat) throw new IllegalArgumentException('Argument can not be null')

        cat.docs.size()
    }

    /**
     * Getting all the associated docs to
     * @param cat
     * @return
     */
    def getDocs(Subcategory cat) {
        return cat.docs?.findAll()
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
     */
    def getDocsOfInterest(def userPrincipals, def request) {
        def subCatNames = []
        def docMap = [:]
        def start, stop, temp

        println(userPrincipals.authorities)

        println('1 Os')
        start = new Date()
        //1 Get docs from associated OS []
        String osName = ''
        //process the os information from the request header

//        Windows 3.11 => Win16,
//        Windows 95 => (Windows 95)|(Win95)|(Windows_95),
//        Windows 98 => (Windows 98)|(Win98),
//        Windows 2000 => (Windows NT 5.0)|(Windows 2000),
//        Windows XP => (Windows NT 5.1)|(Windows XP),
//        Windows Server 2003 => (Windows NT 5.2),
//        Windows Vista => (Windows NT 6.0),
//        Windows 7 => (Windows NT 6.1),
//        Windows 8 => (Windows NT 6.2),
//        Windows 10 => (Windows NT 10.0),
//        Windows NT 4.0 => (Windows NT 4.0)|(WinNT4.0)|(WinNT)|(Windows NT),
//        Windows ME => Windows ME,
//                              Open BSD => OpenBSD,
//        Sun OS => SunOS,
//        Linux => (Linux)|(X11),
//        Mac OS => (Mac_PowerPC)|(Macintosh),
//        QNX => QNX,
//        BeOS => BeOS,
//        OS/2 => OS/2,
        if (request.getHeader('User-Agent').toString().toLowerCase().contains('linux')) {
            osName = 'linux'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('windows nt 6.1')) {
            osName = 'win_7'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('windows nt 6.2')) {
            osName = 'win_8'
        } else if (request.getHeader('User-Agent').toString().toLowerCase().contains('mac_powerpc') || request.getHeader('User-Agent').toString().toLowerCase().contains('macintosh')) {
            osName = 'mac'
        }

        temp = getDocs(getCategory(osName) as Subcategory)
        if (temp.size() > NumDocsToShow) {
            temp = temp.sort {
                -it.viewCount
            }.subList(0, NumDocsToShow)
        }

        docMap.put(osName, temp)
        subCatNames.add(osName)

        stop = new Date()
        println('Benötigte Zeit: ' + TimeCategory.minus(stop, start))

        println('2 Group')
        start = new Date()
        //2 Get the documents of the associated groups [ROLE_GP-STAFF, ROLE_GP-STUD]
        if (userPrincipals.authorities.any { it.authority == ("ROLE_GP-PROF" || "ROLE_GP-LBA") }) {
            docMap.put('faculty', getDocs(getCategory('faculty') as Subcategory).sort {
                -it.viewCount
            }.subList(0, NumDocsToShow))
            subCatNames.add('anonym')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STAFF" }) {
            docMap.put('staff', getDocs(getCategory('staff') as Subcategory).sort {
                -it.viewCount
            }.subList(0, NumDocsToShow))
            subCatNames.add('staff')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STUD" }) {
            docMap.put('student', getDocs(getCategory('student') as Subcategory).sort {
                -it.viewCount
            }.subList(0, NumDocsToShow))
            subCatNames.add('student')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_ANONYMOUS" }) {
            docMap.put('anonym', getDocs(getCategory('anonym') as Subcategory).sort {
                -it.viewCount
            }.subList(0, NumDocsToShow))
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
            def docs = getAllDocsAssociatedToSubCategories(subCatNames as String[])
            if (docs && !docs.empty) {
                if (docs.size() > NumDocsToShow) {
                    docs = docs.sort {
                        -it.viewCount
                    }.subList(0, NumDocsToShow)
                }
                docMap.put('suggestion', docs)
                break
            } else {
                subCatNames.remove(subCatNames.last())
            }
        }
        stop = new Date()
        println('Benötigte Zeit: ' + TimeCategory.minus(stop, start))

        docMap = docMap.sort { -(it.value.size()) }
        return docMap
    }

    /**
     * This method will return iterative all associated subcategories to the given category (either Maincategory or Subcategory)
     * @param cat Category you want to search through
     * @return Array of all found categories
     */
    def getIterativeAllSubCats(String catName) {
        def subs = []
        def cat = getCategory(catName)
        if (cat) {
            if (!(cat instanceof Maincategory)) {
                subs += cat
            }
            cat.subCats?.each { child ->
                subs += getIterativeAllSubCats(child.name)
            }
            subs.unique()
        }
    }

    def getSameAssociatedDocs(Document givenDoc, String[] excludedMainCats, Boolean forFaqs = false) {
        //prepare query
        def query = "MATCH (main:Maincategory)<-[*]-(sub:Subcategory)-[:DOCS]->(doc:Document), " +
                "(sub)-[:DOCS]->(otherDoc:Document)" +
                "WHERE doc.docTitle = '${givenDoc.docTitle}' AND main.name IS NOT NULL "
        excludedMainCats.each { catName ->
            query = query + " AND main.name <> '${catName}' "
        }
        query = query + "RETURN main.name AS mainName, sub.name as subName, otherDoc ORDER BY otherDoc.viewCount"

        //fire query
        Result myResult = Subcategory.cypherStatic(query)

        //process query
        def cats = []
        def docs = []
        myResult.each {
            cats.add(it.subName)
            docs.add(it.otherDoc as Document)
        }

        cats = cats.unique()

        //find only docs which are associated to all found subcats
        docs.each {
            //println(it.docTitle)
        }
        docs = docs.findAll { docs.count(it) == cats.size() }.unique()
        docs.each { doc ->
            println 'Gefunden: ' + doc.docTitle
        }

        if (forFaqs) {
            return docs.findAll { it instanceof Faq }
        } else {
            return docs.findAll { it instanceof Tutorial || it instanceof Article }
        }
    }


    def newSubCategory(String catName, Maincategory mainCat, Subcategory[] subCats = null) {
        if (!catName || catName.empty) throw new IllegalArgumentException("Argument 'catName' can not be null or empty")

        Subcategory newSub = new Subcategory(name: catName)

        if (!mainCat) throw new IllegalArgumentException("Argument 'mainCat' can not be null")
        newSub.mainCat = mainCat

        for (Subcategory sub in subCats) {
            newSub.addToSubCats(sub)
        }

        return newSub.save(flush: true)
    }

    def newSubCategory(String catName, Subcategory parenCat, Subcategory[] subCats = null) {
        if (!catName || catName.empty) throw new IllegalArgumentException("Argument 'catName' can not be null or empty")

        Subcategory newSub = new Subcategory(name: catName)

        if (!parenCat) throw new IllegalArgumentException("Argument 'parentCat' can not be null")
        newSub.parentCat = parenCat

        for (Subcategory sub in subCats) {
            newSub.addToSubCats(sub)
        }

        return newSub.save(flush: true)
    }
}
