/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional

@Transactional
class DocumentService {

    //todo: ChangeDocument

    /**
     * This method helps you to create a new tutorial
     * Attention: Method itself won't save the doc via save() method
     * @param docTitle
     * @param hiddenTags
     * @param steps
     * @return new created document (tutorial)
     */
    def newTutorial(String docTitle, String[] hiddenTags, Step[] steps) {
        def temp = new Document(docTitle: docTitle, hiddenTags: hiddenTags, viewCount: 0)
        steps?.each { step ->
            temp.addToSteps(step)
        }
        if (!temp.validate()) return -2
        return temp.save(flush: true)
    }

    /**
     * This method helps you to create a new faq
     * Attention: Method itself won't save the doc via save() method
     * @param docTitle
     * @param hiddenTags
     * @param faq
     * @return new created document (faq)
     */
    def newFaq(String docTitle, String[] hiddenTags, Faq faq) {
        def temp = new Document(docTitle: docTitle, hiddenTags: hiddenTags, viewCount: 0, faq: faq)
        if (!temp.validate()) return -2
        return temp.save(flush: true)
    }

    /**
     * This method helps you to create a new article
     * Attention: Method itself won't save the doc via save() method
     * @param docTitle
     * @param hiddenTags
     * @param docContent
     * @return new created document (article)
     */
    def newArticle(String docTitle, String[] hiddenTags, String docContent) {
        def temp = new Document(docTitle: docTitle, hiddenTags: hiddenTags, viewCount: 0, docContent: docContent)
        if (!temp.validate()) return -2
        return temp.save(flush: true)
    }

    /**
     *
     * @param docTitle
     * @return
     */
    def deleteDoc(String docTitle) {
        Document doc = getDoc(docTitle)
        if (!doc) return -1
        if (doc.faq) doc.faq.delete()
        if (doc.steps) {
            doc.steps.collect().each { step ->
                step.delete()
            }
        }
        doc.delete()
        return 0
    }

    /**
     *
     * @param docTitle
     * @return
     */
    def getDoc(String docTitle) {
        def myDoc = Document.findByDocTitle(docTitle)
        if (myDoc) {
            myDoc.viewCount = myDoc.viewCount + 1
            if (myDoc.validate()) {
                return myDoc.save()
            } else {
                myDoc.errors.allErrors.each {
                    println(it)
                }
                return null
            }

        }
        return null
    }

    /**
     * This method exports a specific doc in a machine-friendly output
     * @param docTitle
     * @param exportAs Decides which format will be returned, use 'json' or 'xml' for either format
     * @return
     */
    def exportDoc(String docTitle, String exportAs) {
        def myDoc = Document.findByDocTitle(docTitle)
        if (myDoc) {
            def output
            if (exportAs == 'json') {
                output = myDoc as JSON
            }
            else if(exportAs == 'xml') {
                output = myDoc as XML
            }
            return (output)
        }
    }
}
