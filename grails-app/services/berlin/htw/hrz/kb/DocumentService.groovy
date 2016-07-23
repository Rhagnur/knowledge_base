/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional
import groovy.json.JsonSlurper
import groovy.time.TimeCategory

@Transactional
class DocumentService {

    def categorieService
    def springSecurityService

    /**
     * These methods helps to save a new document into the database.
     * @param doctitle Title of the document
     * @param docContent Content of the document (most likely in JSON Format but stored as String)
     * @param docHiddenTags Hidden tags of the document for better searching and declaring synonymous meaning (e.g. WiFi, WLAN, Wireless Lan)
     * @param subCats Subcategories which should be associated with the new document
     * @return TRUE if no problems occurred while saving otherwise FALSE.
     */
    def addDoc(String docTitle, def docContent, String[] docHiddenTags, String[] subCats, String docType) {
        println('title: ' + docTitle)
        println('content: ' + docContent)
        println('tags: ' + docHiddenTags)
        println('subCats: ' + subCats)
        println('type: ' + docType)
        try {
            def doc

            if (docType == 'tutorial') {
                doc = new Document(docTitle: docTitle, hiddenTags: docHiddenTags)
                docContent.each { step ->
                    println(step)
                    doc.addToSteps(new Step(number: step.number, stepTitle: step.title, stepText: step.text, mediaLink: step.link))
                }
            }
            else if (docType == 'faq') {
                doc = new Document(docTitle: docTitle, hiddenTags: docHiddenTags, faq: new Faq(question: docContent.question, answer: docContent.answer))
            }
            for (def cat in subCats) {
                Subcategorie subCat = Subcategorie.findByName(cat)
                subCat.addToDocs(doc)
                subCat.save()
            }
            doc.save()
            true
        } catch (Exception e) {
            e.printStackTrace()
            false
        }
    }

    /**
     * These method finds all the associated documents for the given subcategorie[s]. Use only one Entity in the Array for getting all the documents of one subcategories.
     * If you use more only common documents will be returned. Documents which are'nt associated with all given Subcategories will be ignored.
     * @param subs subcategories for lookup as Array of String
     * @return all found documents as array
     */
    def getAllDocsAssociatedToSubCategories(String[] subs) {
        def docs = []
        subs.each { cat ->
            if (Subcategorie.findByName(cat) != null) {
                docs.addAll(Subcategorie.findByName(cat).docs?.findAll().toArray())
            }
        }
        //find only non unique docs, so you get all docs which are associated with the given categories
        docs = docs.findAll{docs.count(it) == subs.size()}.unique()
        return docs
    }

    //todo: vll einige Funktionen in eine extra Serviceklasse auslagern?
    def getDocsOfInterest(def userPrincipals) {
        def subCatNames = []
        def docMap = [:]
        def NumDocsToShow = 5
        def start, stop

        println(userPrincipals.authorities)

        println('1')
        start = new Date()
        //1 Get docs from associated OS []
        String osName = ''
        if (System.properties['os.name'].toString().toLowerCase().contains('linux')) {
            osName = 'linux'
        }
        else if (System.properties['os.name'].toString().toLowerCase().contains('windows')) {
            osName = 'windows'
        }
        categorieService.getIterativeAllSubCats(Subcategorie.findByName(osName)).each {
            docMap.put(it.name, it.docs.findAll())
            subCatNames.add(it.name)
        }
        stop = new Date()
        println(TimeCategory.minus(stop, start))

        println('2')
        start = new Date()
        //2 Get the documents of the associated groups [ROLE_GP-STAFF, ROLE_GP-STUD]
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STAFF" }) {
            docMap.put('stuff', Subcategorie.findByName('stuff').docs.findAll().sort{ -it.viewCount }.subList(0, NumDocsToShow))
            subCatNames.add('stuff')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STUD" }) {
            docMap.put('student', Subcategorie.findByName('student').docs.findAll().sort{ -it.viewCount }.subList(0, NumDocsToShow))
            subCatNames.add('student')
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_ANONYMOUS" }) {
            docMap.put('anonym', Subcategorie.findByName('anonym').docs.findAll().sort{ -it.viewCount }.subList(0, NumDocsToShow))
            subCatNames.add('anonym')
        }
        stop = new Date()
        println(TimeCategory.minus(stop, start))

        println('3')
        start = new Date()
        //3 Get the popularest docs
        def temp = Document.findAll(max : NumDocsToShow, sort: 'viewCount', order: 'desc')
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
        //prio reihenfolge thema, sprache, betriebssystem, gruppe
        def catNames = []
        def temp
        def start, stop

        println('\n1 subCat doctype')
        start = new Date()
        if (typeOf == 'tutorial') {
            catNames.add('tutorial')
        }
        else if (typeOf == 'faq') {
            catNames.add('faq')
        }
        stop = new Date()
        println(TimeCategory.minus(stop, start))

        println('\n2 subCats theme and lang')
        start = new Date()
        //1 add theme and lang subCatName
        temp = Subcategorie.findByMainCatAndDocs(Maincategorie.findByName('theme'), doc)
        if (temp) { catNames.add(temp.name) }
        temp = Subcategorie.findByMainCatAndDocs(Maincategorie.findByName('lang'), doc)
        if (temp) { catNames.add(temp.name) }
        stop = new Date()
        println(TimeCategory.minus(stop, start))


        println('\n3 subCat os')
        start = new Date()
        //2 add os subCatName
        temp = categorieService.getIterativeAllSubCats(Maincategorie.findByName('os'))
        temp = temp.find { it.docs.contains(doc)}
        if (temp) {
            catNames.add(temp.name)
        }
        stop = new Date()
        println(TimeCategory.minus(stop, start))

        println('\n4 subcat group')
        start = new Date()
        //3 add groupSubCatName
        if (springSecurityService.principal.authorities.any { it.authority == "ROLE_ANONYMOUS" }) {
            catNames.add('anonym')
        }
        else if (springSecurityService.principal.authorities.any { it.authority == "ROLE_GP-STAFF" }) {
            catNames.add('stuff')
        }
        else if (springSecurityService.principal.authorities.any { it.authority == "ROLE_GP-STUD" }) {
            catNames.add('student')
        }

        while (catNames && catNames.size() > 1) {
            docs = getAllDocsAssociatedToSubCategories(catNames as String[])
            docs.remove(doc)
            if (docs && !docs.empty) {
                stop = new Date()
                println(TimeCategory.minus(stop, start))
                return docs
            } else {
                catNames.remove(catNames.last())
            }
        }

        return null
    }

    def exportDoc(String docTitle, String exportAs) {
        def myDoc = Document.findByDocTitle(docTitle)
        if (myDoc) {
            def output
            if (exportAs == 'json') {
                JSON.use('deep') {
                    output = myDoc as JSON
                    if (myDoc.faq) {
                        HashMap resultFaq = new JsonSlurper().parseText((myDoc.faq as JSON).toString())
                        resultFaq.remove('doc')
                        HashMap resultDoc = new JsonSlurper().parseText((myDoc as JSON).toString())
                        resultDoc.remove('faq')
                        resultDoc.remove('steps')
                        resultDoc.put('faq', resultFaq)
                        println(resultDoc as JSON)
                        output = resultDoc as JSON
                    }

                }
            }
            //todo: XML Export funktioniert bei FAQ noch nicht wirklich, vielleicht einfach Export für FAQ untersagen?
            else if(exportAs == 'xml') {
                XML.use('deep') {
                    output = myDoc as XML

                }
                /*
                if (myDoc.faq) {
                    String faq = (myDoc.faq as XML).toString()
                    String doc = (myDoc as XML).toString()
                    NodeChild resultFaq = new XmlSlurper().parseText(faq)
                    GPathResult resultDoc = new XmlSlurper().parseText(doc)
                    resultFaq.childNodes().each {
                        println(it.toString())
                    }
                    println(XmlUtil.serialize(resultFaq))

                    HashMap resultFaq = new XmlSlurper().parseText(faq)
                    resultFaq.remove('doc')
                    HashMap resultDoc = new XmlSlurper().parseText(doc)
                    resultDoc.remove('faq')
                    resultDoc.remove('steps')
                    resultDoc.put('faq', resultFaq)
                    println(resultDoc as XML)
                    output = resultDoc as XML
                }*/
            }
            return (output)
        }
    }
}
