/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional
import groovy.json.JsonSlurper

@Transactional
class DocumentService {

    def categorieService

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
        def matchItems = docs.findAll{docs.count(it) > 1}.unique()
        return matchItems
    }

    //todo: vll einige Funktionen in eine extra Serviceklasse auslagern?
    def getDocsOfInterest(def userPrincipals) {
        def docMap = [:]

        println(userPrincipals.authorities)
        //1 Get the documents of the associated groups [ROLE_GP-STAFF, ROLE_GP-STUD]
        if (userPrincipals.authorities.any { it.authority == "ROLE_ANONYMOUS" }) {
            docMap.put('anonym', Subcategorie.findByName('anonym').docs.findAll())
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STUD" }) {
            docMap.put('student', Subcategorie.findByName('student').docs.findAll())
        }
        if (userPrincipals.authorities.any { it.authority == "ROLE_GP-STAFF" }) {
            docMap.put('stuff', Subcategorie.findByName('stuff').docs.findAll())
        }

        //2 Get docs from associated OS []
        String osName = ''
        if (System.properties['os.name'].toString().toLowerCase().contains('linux')) {
            osName = 'linux'
        }
        else if (System.properties['os.name'].toString().toLowerCase().contains('windows')) {
            osName = 'windows'
        }
        categorieService.getIterativeAllSubCats(Subcategorie.findByName(osName)).each {
            docMap.put(it.name, it.docs.findAll())
        }

        //3 Get the popularest docs
        def temp = Document.findAll()
        temp.sort { -it.viewCount }
        docMap.put('popular', temp)


        //println(docMap)
        return docMap
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
