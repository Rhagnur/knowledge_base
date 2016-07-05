/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.transaction.Transactional
import grails.web.JSONBuilder
import groovy.json.JsonOutput

import javax.print.Doc


@Transactional
class DocumentService {

    /**
     * These methods helps to save a new document into the database.
     * @param doctitle Title of the document
     * @param docContent Content of the document (most likely in JSON Format but stored as String)
     * @param docHiddenTags Hidden tags of the document for better searching and declaring synonymous meaning (e.g. WiFi, WLAN, Wireless Lan)
     * @param subCats Subcategories which should be associated with the new document
     * @return TRUE if no problems occurred while saving otherwise FALSE.
     */
    def addDoc(String docTitle, String docContent, String[] docHiddenTags, String[] subCats) {

        //println('title: ' + docTitle + ' class: ' + docTitle.getClass())
        //println('content: ' + docContent + ' class: ' + docContent.getClass())
        //println('tags: ' + docHiddenTags + ' class: ' + docHiddenTags.getClass())
        //println('cats: ' + subCats + ' class: ' + subCats.getClass())
        try {
            def doc = new Document(title: docTitle, content: docContent, hiddenTags: docHiddenTags)
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

    def exportDoc(String docTitle) {
        def myDoc = Document.findByTitle(docTitle)
        if (myDoc) {
            def slurper = new groovy.json.JsonSlurper()
            def temp = slurper.parseText(myDoc.content)
            temp << [lang: Maincategorie.findByName('lang').subCats.find{it.docs.contains(myDoc)}.name]
            temp << [type: Maincategorie.findByName('doctype').subCats.find{it.docs.contains(myDoc)}.name]
            temp << [title: myDoc.title]
            return JsonOutput.prettyPrint(JsonOutput.toJson(temp))
        }
    }
}
