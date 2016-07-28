/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional
import org.neo4j.graphdb.NotFoundException

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
    def newTutorial(String docTitle, String[] hiddenTags, Step[] steps) throws Exception {
        def temp = new Document(docTitle: docTitle, hiddenTags: hiddenTags, viewCount: 0)
        steps?.each { step ->
            temp.addToSteps(step)
        }
        if (!temp.validate()) throw new Exception('ERROR: Validation of data wasn\'t successfull')
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
    def newFaq(String docTitle, String[] hiddenTags, Faq faq) throws Exception {
        def temp = new Document(docTitle: docTitle, hiddenTags: hiddenTags, viewCount: 0, faq: faq)
        if (!temp.validate()) throw new Exception('ERROR: Validation of data wasn\'t successfull')
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
    def newArticle(String docTitle, String[] hiddenTags, String docContent) throws Exception {
        def temp = new Document(docTitle: docTitle, hiddenTags: hiddenTags, viewCount: 0, docContent: docContent)
        if (!temp.validate()) throw new Exception('ERROR: Validation of data wasn\'t successfull')
        return temp.save(flush: true)
    }

    /**
     *
     * @param docTitle
     * @return
     */
    def deleteDoc(String docTitle) throws Exception {
        Document doc = getDoc(docTitle)
        if (doc.faq) doc.faq.delete()
        if (doc.steps) {
            doc.steps.collect().each { step ->
                step.delete()
            }
        }
        doc.delete()
    }

    def increaseCounter(Document doc) {
        if (!doc) throw new IllegalArgumentException()
        doc?.viewCount = doc?.viewCount + 1
        if (!doc?.validate()) throw new Exception('ERROR: Validation of data wasn\'t successfull')
        return doc.save(flush:true)
    }

    /**
     *
     * @param docTitle
     * @return
     */
    def getDoc(String docTitle) throws Exception {
        if (!docTitle || docTitle == '') throw new IllegalArgumentException()
        def myDoc = Document.findByDocTitle(docTitle)
        if (myDoc) return myDoc
        throw new NotFoundException()
    }

    /**
     * This method exports a specific doc in a machine-friendly output
     * @param docTitle
     * @param exportAs Decides which format will be returned, use 'json' or 'xml' for either format
     * @return
     */
    def exportDoc(String docTitle, String exportAs) throws Exception {
        def myDoc = getDoc(docTitle)
        def output = null
        if (exportAs == 'json') {
            output = myDoc as JSON
        } else if (exportAs == 'xml') {
            output = myDoc as XML
        } else {
            throw new IllegalArgumentException()
        }
        return output
    }
}
