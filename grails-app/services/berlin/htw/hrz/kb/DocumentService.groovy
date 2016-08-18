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
    Document changeDoc(Document oldDoc, Document newDoc) {
        return null
    }

    /**
     *
     * @param docTitle
     * @return
     * @throws Exception
     */
    void deleteDoc(Document doc) throws Exception {
        if (doc instanceof Tutorial) {
            doc.steps?.collect()?.each { step ->
                step.delete()
            }
        }

        //Vorsichtshalber Prüfung ob noch Linker gefunden werden, falls ja diese unlinken und löschen
        doc.linker.collect().each { linker ->
            Linker.unlink(linker.subcat, linker.doc)
        }

        doc.delete()
    }

    /**
     * This method exports a specific doc in a machine-friendly output
     * @param docTitle
     * @param exportAs Decides which format will be returned, use 'json' or 'xml' for either format
     * @return chosen document as JSON or XML Object
     * @throws Exception
     */
    def exportDoc(Document doc, String exportAs) throws Exception {
        if (!doc) { throw new IllegalArgumentException("Argument 'doc' can not be null") }
        def output
        if (exportAs == 'json') {
            output = doc as JSON
        } else if (exportAs == 'xml') {
            output = doc as XML
        } else {
            throw new IllegalArgumentException("No such 'exportAs' argument, please use 'json' or 'xml'.")
        }
        output
    }

    /**
     *
     * @param docTitle
     * @return
     * @throws Exception
     */
    // TODO [TR]: ist der Titel wirklich eine gute Wahl als PK ? Der kann durchaus redundant sein!
    //todo exception
    Document getDoc(String docTitle) throws Exception {
        if (!docTitle || docTitle == '') { throw new IllegalArgumentException() }
        Document myDoc = Document.findByDocTitle(docTitle)
        if (!myDoc) { throw new Exception('No such document found') }
        myDoc
    }

    /**
     *
     * @param doc
     * @return
     * @throws Exception
     */
    //todo exception
    Document increaseCounter(Document doc) throws Exception {
        if (!doc) { throw new IllegalArgumentException() }
        doc?.viewCount = doc?.viewCount + 1
        if (!doc?.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new Exception('ERROR: Validation of data wasn\'t successfull')
        }
        doc.save(flush: true)
    }

    /**
     * This method helps you to create a new article
     * @param docTitle
     * @param tags
     * @param docContent
     * @return new created document (article)
     * @throws Exception
     */
    //todo exception
    Article newArticle(String docTitle, String docContent, String[] tags) throws Exception {
        def temp = new Article(docTitle: docTitle, tags: tags, viewCount: 0, createDate: new Date(), docContent: docContent)
        if (!temp.validate()) { throw new Exception('ERROR: Validation of data wasn\'t successfull') }
        temp.save(flush: true)
    }

    /**
     * This method helps you to create a new faq
     * @param docTitle
     * @param tags
     * @param faq
     * @return new created document (faq)
     * @throws Exception
     */
    //todo exception
    Faq newFaq(String question, String answer, String[] tags) throws Exception {
        def temp = new Faq(docTitle: question, tags: tags, createDate: new Date(), viewCount: 0, question: question, answer: answer)
        if (!temp.validate()) { throw new Exception('ERROR: Validation of data wasn\'t successfull') }
        temp.save(flush: true)
    }

    /**
     * This method helps you to create a new tutorial
     * @param docTitle
     * @param tags
     * @param steps
     * @return new created document (tutorial)
     * @throws Exception
     */
    //todo exception
    Tutorial newTutorial(String docTitle, Step[] steps, String[] tags) throws Exception {
        def temp = new Tutorial(docTitle: docTitle, tags: tags, viewCount: 0, createDate: new Date())
        steps?.each { step ->
            temp.addToSteps(step)
        }
        if (!temp.validate()) { throw new Exception('ERROR: Validation of data wasn\'t successfull') }
        temp.save(flush: true)
    }
}
