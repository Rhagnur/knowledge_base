/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional


@Transactional
/**
 * Service which help you to manage the different types of documents
 */
class DocumentService {

    //todo: ChangeDocument

    /**
     *
     * @param docTitle
     * @return
     * @throws Exception
     */
    def deleteDoc(Document doc) throws Exception {
        if (doc instanceof Tutorial) {
            doc.steps?.collect()?.each { step ->
                step.delete()
            }
        }
        doc.delete()
    }

    /**
     * This method exports a specific doc in a machine-friendly output
     * @param docTitle
     * @param exportAs Decides which format will be returned, use 'json' or 'xml' for either format
     * @return
     * @throws Exception
     */
    def exportDoc(Document doc, String exportAs) throws Exception {
        if (!doc) throw new IllegalArgumentException("Argument 'doc' can not be null")
        def output
        if (exportAs == 'json') {
            output = doc as JSON
        } else if (exportAs == 'xml') {
            output = doc as XML
        } else {
            throw new IllegalArgumentException("No such 'exportAs' argument, please use 'json' or 'xml'.")
        }
        return output
    }

    /**
     *
     * @param docTitle
     * @return
     * @throws Exception
     */
    def getDoc(String docTitle) throws Exception {
        if (!docTitle || docTitle == '') throw new IllegalArgumentException()
        def myDoc = Document.findByDocTitle(docTitle)
        if (myDoc) return myDoc
        throw new Exception('No such document found')
    }

    /**
     *
     * @param doc
     * @return
     * @throws Exception
     */
    def increaseCounter(Document doc) throws Exception {
        if (!doc) throw new IllegalArgumentException()
        doc?.viewCount = doc?.viewCount + 1
        if (!doc?.validate()) throw new Exception('ERROR: Validation of data wasn\'t successfull')
        return doc.save(flush: true)
    }

    /**
     * This method helps you to create a new article
     * Attention: Method itself won't save the doc via save() method
     * @param docTitle
     * @param tags
     * @param docContent
     * @return new created document (article)
     * @throws Exception
     */
    def newArticle(String docTitle, String docContent, String[] tags) throws Exception {
        def temp = new Article(docTitle: docTitle, tags: tags, viewCount: 0, createDate: new Date(), docContent: docContent)
        if (!temp.validate()) throw new Exception('ERROR: Validation of data wasn\'t successfull')
        return temp.save(flush: true)
    }

    /**
     * This method helps you to create a new faq
     * Attention: Method itself won't save the doc via save() method
     * @param docTitle
     * @param tags
     * @param faq
     * @return new created document (faq)
     * @throws Exception
     */
    def newFaq(String question, String answer, String[] tags) throws Exception {
        def temp = new Faq(docTitle: question, tags: tags, createDate: new Date(), viewCount: 0, question: question, answer: answer)
        if (!temp.validate()) throw new Exception('ERROR: Validation of data wasn\'t successfull')
        return temp.save(flush: true)
    }

    /**
     * This method helps you to create a new tutorial
     * Attention: Method itself won't save the doc via save() method
     * @param docTitle
     * @param tags
     * @param steps
     * @return new created document (tutorial)
     * @throws Exception
     */
    def newTutorial(String docTitle, Step[] steps, String[] tags) throws Exception {
        def temp = new Tutorial(docTitle: docTitle, tags: tags, viewCount: 0, createDate: new Date())
        steps?.each { step ->
            temp.addToSteps(step)
        }
        if (!temp.validate()) throw new Exception('ERROR: Validation of data wasn\'t successfull')
        return temp.save(flush: true)
    }
}
