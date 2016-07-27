package berlin.htw.hrz.kb

import grails.transaction.Transactional
import groovy.time.TimeCategory
import org.neo4j.graphdb.NotFoundException

@Transactional
/**
 * Service which help you for managing all kind of categories (main/sub)
 */
class CategoryService {

    def documentService
    def springSecurityService

    /**
     * This method will return iterative all associated subcategories to the given categorie (either Maincategory or Subcategory)
     * @param cat Categorie you want to search through
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

    /**
     *
     * @param catName
     * @return found main- or subcategorie, error-code if something went wrong
     */
    def getAllSubCats(String catName) {
        return getCategory(catName).subCats
    }

    /**
     *
     * @param catName
     * @return number of associated categories, error-code if something went wrong
     */
    def getDocCount(String catName) {
        def cat = getCategory(catName)
        if (cat instanceof Maincategory) throw new NoSuchMethodException('ERROR: Maincategory do not have any documents')
        cat.docs.size()
    }

    def deleteCategorie(String catName) {
        def cat = getCategory(catName)
        cat.delete()
    }

    /**
     *
     * @param oldName
     * @param newName
     * @return Errorcode as int, error-code if something went wrong
     */
    def changeCategorieName(String oldName, String newName) {
        if (!newName || newName == '') throw new IllegalArgumentException()

        def cat = getCategory(oldName)
        cat.name = newName

        if (!cat.validate()) {
            cat.errors?.allErrors?.each { log.error(it) }
            throw new Exception('ERROR: Validation of data wasn\'t successfull')
        }
        return cat.save()
    }

    /**
     *
     * @param catName
     * @return found main- or subcategory
     */
    def getCategory(String catName) {
        if (!catName || catName == '') throw new IllegalArgumentException()
        def cat = Maincategory.findByName(catName) ?: Subcategory.findByName(catName) ?: null
        if (!cat) throw new NotFoundException()
        return cat
    }

    def getAllDocs(String catName) {
        def cat = getCategory(catName)
        if (cat instanceof Maincategory) throw new NoSuchMethodException('ERROR: Maincategory do not have any documents')
        return cat.docs?.findAll()
    }

    //todo: Finde Kategorie durch gegebene Maincat und Doc + changeParent
    //def getCategory
    //def changeParent

    //todo: Rausfinden warum Änderung temporär funktioniert aber NIE in die Datenbank gelangt
    //Lösung 1: Zu ändernen Knoten löschen und mit neuen Beziehungn erstellen...meeeeh

    /**
     *
     * @param catName
     * @param newCats
     * @return error-code as int, please have a look at the description of the class
     */
    def changeSubCats(String catName, String[] newCats) {
        def tempCat, cat = getCategory(catName)
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

    //todo: Anstatt alle Attribute zu übergeben wäre es sinnvoller nur ein Document und die Liste der Kategorien zu übergeben, an welche es angeheftet werden soll
    def addDoc(String docTitle, def docContent, String[] hiddenTags, String[] subCats, String docType, int viewCount) {
        println('title: ' + docTitle)
        println('content: ' + docContent)
        println('tags: ' + hiddenTags)
        println('subCats: ' + subCats)
        println('type: ' + docType)
        println('viewCount: ' + viewCount)
        def doc = null

        if (docType == 'tutorial') {
            def steps = []
            docContent.each { step ->
                steps.add(new Step(number: step.number, stepTitle: step.title, stepText: step.text, mediaLink: step.link))
            }
            doc = documentService.newTutorial(docTitle, hiddenTags, steps as Step[])
        } else if (docType == 'faq') {
            doc = documentService.newFaq(docTitle, hiddenTags, new Faq(question: docContent.question, answer: docContent.answer))
        } else {
            throw new IllegalArgumentException("ERROR: Doctype '${docType}' is not known")
        }

        for (def cat in subCats) {
            Subcategory subCat = Subcategory.findByName(cat)
            subCat.addToDocs(doc)
            subCat.save(flush: true)
        }
        return doc.save(flush:true)
    }

    def getAllDocsAssociatedToSubCategories(String[] subs) {
        def docs = []
        subs.each { cat ->
            if (Subcategory.findByName(cat) != null) {
                docs.addAll(Subcategory.findByName(cat).docs?.findAll().toArray())
            }
        }
        //find only non unique docs, so you get all docs which are associated with the given categories
        docs = docs.findAll { docs.count(it) == subs.size() }.unique()
        return docs
    }

    //todo: vll einige Funktionen in eine extra Serviceklasse auslagern?
    def getDocsOfInterest(def userPrincipals, def request) {
        def subCatNames = []
        def docMap = [:]
        def NumDocsToShow = 5
        def start, stop, temp

        println(userPrincipals.authorities)

        println('1')
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

        temp = getAllDocs(osName)
        if (temp.size() > NumDocsToShow){
            temp = temp.sort {
                -it.viewCount
            }.subList(0, NumDocsToShow)
        }

        docMap.put(osName, temp)
        subCatNames.add(osName)

        stop = new Date()
        println(TimeCategory.minus(stop, start))

        println('2')
        start = new Date()
        //2 Get the documents of the associated groups [ROLE_GP-STAFF, ROLE_GP-STUD]
        if (userPrincipals.authorities.any { it.authority == ("ROLE_GP-PROF"||"ROLE_GP-LBA") }) {
            docMap.put('faculty', getAllDocs('faculty').sort {
                -it.viewCount
            }.subList(0, NumDocsToShow))
            subCatNames.add('anonym')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STAFF" }) {
            docMap.put('staff', getAllDocs('staff').sort {
                -it.viewCount
            }.subList(0, NumDocsToShow))
            subCatNames.add('staff')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STUD" }) {
            docMap.put('student', getAllDocs('student').sort {
                -it.viewCount
            }.subList(0, NumDocsToShow))
            subCatNames.add('student')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_ANONYMOUS" }) {
            docMap.put('anonym', getAllDocs('anonym').sort {
                -it.viewCount
            }.subList(0, NumDocsToShow))
            subCatNames.add('anonym')
        }
        stop = new Date()
        println(TimeCategory.minus(stop, start))

        println('3')
        start = new Date()
        //3 Get the popularest docs
        temp = Document.findAll(max: NumDocsToShow, sort: 'viewCount', order: 'desc')
        docMap.put('popular', temp)
        stop = new Date()
        println(TimeCategory.minus(stop, start))

        println('4')
        start = new Date()
        //4 Get suggestions, sugg are associated to OS and the user-groups
        while (subCatNames && !subCatNames.empty) {
            def docs = getAllDocsAssociatedToSubCategories(subCatNames as String[])
            if (docs && !docs.empty) {
                docMap.put('suggestion', docs)
                break
            } else {
                subCatNames.remove(subCatNames.last())
            }
        }
        stop = new Date()
        println(TimeCategory.minus(stop, start))

        docMap = docMap.sort { -(it.value.size()) }
        return docMap
    }

    def getSimilarDocs(Document doc, String typeOf) {
        def docs
        //prio reihenfolge thema, betriebssystem, sprache, gruppe
        def catNames = []
        def temp
        def start, stop

        println('\n1 subCat doctype')
        start = new Date()
        if (typeOf == 'tutorial') {
            catNames.add('tutorial')
        } else if (typeOf == 'faq') {
            catNames.add('faq')
        }
        stop = new Date()
        println(TimeCategory.minus(stop, start))

        println('\n2 subCats theme')
        start = new Date()
        temp = Subcategory.findByMainCatAndDocs(Maincategory.findByName('theme'), doc)
        if (temp) {
            catNames.add(temp.name)
        }
        stop = new Date()
        println(TimeCategory.minus(stop, start))


        println('\n3 subCat os')
        start = new Date()
        temp = getIterativeAllSubCats('os')
        temp = temp.find { it.docs.contains(doc) }
        if (temp) {
            catNames.add(temp.name)
        }
        stop = new Date()
        println(TimeCategory.minus(stop, start))

        println('\n4 subCat lang')
        start = new Date()
        temp = Subcategory.findByMainCatAndDocs(Maincategory.findByName('lang'), doc)
        if (temp) {
            catNames.add(temp.name)
        }
        stop = new Date()
        println(TimeCategory.minus(stop, start))

        println('\n5 subcat group')
        start = new Date()
        if (springSecurityService.principal.authorities.any { it.authority == "ROLE_ANONYMOUS" }) {
            catNames.add('anonym')
        } else if (springSecurityService.principal.authorities.any { it.authority == "ROLE_GP-STAFF" }) {
            catNames.add('staff')
        } else if (springSecurityService.principal.authorities.any { it.authority == "ROLE_GP-STUD" }) {
            catNames.add('student')
        } else if (springSecurityService.principal.authorities.any { it.authority == ("ROLE_GP-PROF"||"ROLE_GP-LBA") }) {
            catNames.add('faculty')
        }


        stop = new Date()
        println(TimeCategory.minus(stop, start))

        println('\n5 search for docs')
        start = new Date()
        while (catNames && catNames.size() > 1 ) {
            println(catNames)
            docs = getAllDocsAssociatedToSubCategories(catNames as String[])
            println(docs)
            docs.remove(doc)
            if (docs && !docs.empty) {
                stop = new Date()
                println(TimeCategory.minus(stop, start))

                return docs
            } else {
                catNames.remove(catNames.last())
            }
        }
        stop = new Date()
        println(TimeCategory.minus(stop, start))

        return null
    }

}
