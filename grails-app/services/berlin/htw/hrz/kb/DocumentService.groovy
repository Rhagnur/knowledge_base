/*
 * Created by didschu
 */

package berlin.htw.hrz.kb

import grails.converters.JSON
import grails.converters.XML
import grails.transaction.Transactional
import groovy.xml.MarkupBuilder


/**
 * Service which help you to manage the different types of documents
 */
@Transactional
class DocumentService {

    //todo: changeTags, changeContent, ...Steps,Faq..bla

    Document changeArticleContent(Article doc, String newContent) throws IllegalArgumentException, ValidationErrorException {
        if (!doc) { throw new IllegalArgumentException("Atrribute 'doc' CAN NOT be null!") }
        doc.docContent = newContent
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     *
     * @param doc
     * @param newDocTitle
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeDocTitle(Document doc, String newDocTitle) throws IllegalArgumentException, ValidationErrorException {
        if (!doc || !newDocTitle) { throw new IllegalArgumentException("Atrribute 'doc' and 'newDocTitle' CAN NOT be null or empty!") }
        doc.docTitle = newDocTitle
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     *
     * @param doc
     * @param answer
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeFaqAnswer(Faq doc, String answer) throws IllegalArgumentException, ValidationErrorException {
        if (!doc) { throw new IllegalArgumentException("Atrribute 'doc' CAN NOT be null!") }
        doc.answer = answer
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     *
     * @param doc
     * @param question
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeFaqQuestion(Faq doc, String question) throws IllegalArgumentException, ValidationErrorException {
        if (!doc) { throw new IllegalArgumentException("Atrribute 'doc' CAN NOT be null!") }
        doc.question = question
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     *
     * @param doc
     * @param newLocked
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeLocked(Document doc, Boolean newLocked) throws IllegalArgumentException, ValidationErrorException {
        if (!doc || newLocked == null) { throw new IllegalArgumentException("Atrribute 'doc' and 'newLocked' CAN NOT be null!") }
        doc.locked = newLocked
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     *
     * @param doc
     * @param newTags
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeTags(Document doc, String[] newTags) throws IllegalArgumentException, ValidationErrorException {
        if (!doc) { throw new IllegalArgumentException("Atrribute 'doc' CAN NOT be null!") }
        doc.tags = newTags
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     *
     * @param doc
     * @param newSteps
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeTutorialSteps(Tutorial doc, List<Step> newSteps) throws IllegalArgumentException, ValidationErrorException {
        if (!doc && !newSteps) { throw new IllegalArgumentException("Atrribute 'doc' and 'newSteps' CAN NOT be null!") }
        doc.steps.clear()
        newSteps.each {Step step ->
            doc.addToSteps(step)
        }
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     *
     * @param docTitle
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
     * If no document is given or the parameter is null it will return a list of all unlocked documents in the chosen format
     * @param exportAs Decides which format will be returned, use 'json' or 'xml' for either format
     * @param doc document which you want to get exported, can be null
     * @return output as JSON or XML Object
     * @throws IllegalArgumentException
     */
    def exportDoc(String exportAs, Document doc = null) throws IllegalArgumentException {
        def output
        if (exportAs == 'json') {
            if (doc) { output = doc as JSON }
            else {
                def tempMap = [:]
                def tempList = []
                Document.findAllByLockedNotEqual(true).sort { it.docTitle }.each{ Document docIt ->
                    def tempElementData = [:]
                    tempElementData.put('docTitle', docIt.docTitle)
                    tempElementData.put('docType', docIt.class.simpleName)
                    tempElementData.put('author', getAuthor(docIt))
                    tempElementData.put('viewCount', docIt.viewCount)
                    tempList.add(tempElementData)
                }
                tempMap.put('info', 'This object represents a list of all documents that are not locked. You can get a json/xml object of a single document by using the subfix: /exportDoc?docTitle=(docTitle)&exportAs=(xml or json)')
                tempMap.put('documents', tempList)
                output = tempMap as JSON
            }
        } else if (exportAs == 'xml') {
            if (doc) { output = doc as XML }
            else {
                def writer = new StringWriter()
                new MarkupBuilder(writer).list {
                    info('This object represents a list of all documents that are not locked. You can get a json/xml object of a single document by using the subfix: /exportDoc?docTitle=(docTitle)&exportAs=(xml or json)')
                    documents {
                        Document.findAllByLockedNotEqual(true).sort { it.docTitle }.each{ Document docIt ->
                            document {
                                docTitle(docIt.docTitle)
                                docType(docIt.class.simpleName)
                                author(getAuthor(docIt))
                                viewCount(docIt.viewCount)
                            }
                        }
                    }
                }
                println(writer.toString())
                output = writer.toString()
            }
        } else {
            throw new IllegalArgumentException("No such 'exportAs' argument, please use 'json' or 'xml'!")
        }
        output
    }

    /**
     * Method for finding unlinked, unassociated documents.
     * @return list all found documents
     */
    List findUnlinkedDocs() {
        return Document.findAll().findAll { !it.linker } as List
    }

    /**
     * Method for getting the associated author of a document
     * @param doc
     * @return author if found returns a string which represents the author. if not found null
     * @throws IllegalArgumentException
     */
    String getAuthor(Document doc) throws IllegalArgumentException {
        if (!doc) { throw new IllegalArgumentException("Argument 'doc' CAN NOT be null!") }
        return (doc.linker.subcat.find{ it.parentCat.name == 'author' }.name as String)?:null
    }

    /**
     * Method for getting the associated language of a document
     * @param doc
     * @return lang if found returns a string which represents the language. if not found null
     * @throws IllegalArgumentException
     */
    String getLanguage(Document doc) throws IllegalArgumentException {
        if (!doc) { throw new IllegalArgumentException("Argument 'doc' CAN NOT be null!") }
        return (doc.linker.subcat.find{ it.parentCat.name == 'lang' }.name as String)?:null
    }

    /**
     *
     * @param docTitle
     * @return document
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
     * @return document
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
     * @return article
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
     * @return faq
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
     * @return tutorial
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
