/*
 * Created by didschu
 */

package berlin.htw.hrz.kb

import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional



/**
 * Service which help you to manage the different types of documents
 */
@Transactional
class DocumentService {

    //todo: changeTitle, changeTags, changeContent, ...Steps,Faq..bla

    /**
     *
     * @param docTitle
     * @return
     */
    void deleteDoc(Document doc) {
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
     * @throws IllegalArgumentException
     */
    def exportDoc(Document doc, String exportAs) throws IllegalArgumentException {
        if (!doc) { throw new IllegalArgumentException("Argument 'doc' can not be null!") }
        def output
        if (exportAs == 'json') {
            output = doc as JSON
        } else if (exportAs == 'xml') {
            output = doc as XML
        } else {
            throw new IllegalArgumentException("No such 'exportAs' argument, please use 'json' or 'xml'!")
        }
        output
    }

    /**
     *
     * @param docTitle
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchObjectFoundException
     */
    Document getDoc(String docTitle) throws IllegalArgumentException, NoSuchObjectFoundException {
        if (!docTitle || docTitle == '') { throw new IllegalArgumentException() }
        Document myDoc = Document.findByDocTitle(docTitle)
        if (!myDoc) { throw new NoSuchObjectFoundException("No Object 'document' with docTitle '${docTitle}' found!") }
        myDoc
    }

    /**
     *
     * @param doc
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchObjectFoundException
     */
    Document increaseCounter(Document doc) throws IllegalArgumentException, ValidationErrorException {
        if (!doc) { throw new IllegalArgumentException("Argument 'doc' can not be null!") }
        doc?.viewCount = doc?.viewCount + 1
        if (!doc?.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush: true)
    }

    /**
     * This method helps you to create a new article
     * @param docTitle
     * @param tags
     * @param docContent
     * @return
     * @throws ValidationErrorException
     */
    Article newArticle(String docTitle, String docContent, String[] tags) throws ValidationErrorException {
        def temp = new Article(docTitle: docTitle, tags: tags, viewCount: 0, createDate: new Date(), docContent: docContent)
        if (!temp.validate()) {
            temp.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful')
        }
        temp.save(flush: true)
    }

    /**
     * This method helps you to create a new faq
     * @param docTitle
     * @param tags
     * @param faq
     * @return
     * @throws ValidationErrorException
     */
    Faq newFaq(String question, String answer, String[] tags) throws ValidationErrorException {
        def temp = new Faq(docTitle: question, tags: tags, createDate: new Date(), viewCount: 0, question: question, answer: answer)
        if (!temp.validate()) {
            temp.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful')
        }
        temp.save(flush: true)
    }

    /**
     * This method helps you to create a new tutorial
     * @param docTitle
     * @param tags
     * @param steps
     * @return
     * @throws ValidationErrorException
     */
    Tutorial newTutorial(String docTitle, Step[] steps, String[] tags) throws ValidationErrorException {
        def temp = new Tutorial(docTitle: docTitle, tags: tags, viewCount: 0, createDate: new Date())
        steps?.each { step ->
            temp.addToSteps(step)
        }
        if (!temp.validate()) {
            temp.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful')
        }
        temp.save(flush: true)
    }
}
