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

    /**
     * Changes the content of the article-document
     * @param doc can't be null
     * @param newContent can't be null
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeArticleContent(Article doc, String newContent) throws IllegalArgumentException, ValidationErrorException {
        if (!doc || !newContent) { throw new IllegalArgumentException("Attribute 'doc' and 'newContent' CAN NOT be null!") }
        doc.docContent = newContent
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     * Changes the locked attribute of a given document
     * @param doc can't be null
     * @param newLocked new status for locked, can't be null
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeDocLocked(Document doc, Boolean newLocked) throws IllegalArgumentException, ValidationErrorException {
        if (!doc || newLocked == null) { throw new IllegalArgumentException("Attribute 'doc' and 'newLocked' CAN NOT be null!") }
        doc.locked = newLocked
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     * Changes the parent of a given document
     * @param doc can't be null
     * @param newParents can't be null
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeDocParents(Document doc, List<Subcategory>newParents) throws IllegalArgumentException, ValidationErrorException {
        if (!doc || !newParents) { throw new IllegalArgumentException("Attribute 'doc' and 'newParents' CAN NOT be null or empty!") }
        else if ( !(newParents.find{ it.parentCat?.name == 'lang'}) || !(newParents.find{ it.parentCat?.name == 'author'}) ) {
            throw new IllegalArgumentException("Attribute 'newParents' must contain a child element from 'lang' and 'author'!")
        } else {
            if (doc.linker) {
                doc.linker.collect().each { Linker it ->
                    Linker.unlink(it.subcat, it.doc)
                }
            }

            for (Subcategory parent in newParents) {
                Linker.link(parent, doc)
                parent.save(flush:true)
            }
            if (!doc.validate()) {
                doc.errors?.allErrors?.each { log.error(it) }
                throw new ValidationErrorException('Validation for document-data was not successful!')
            }
            doc.save(flush:true)
        }
    }

    /**
     * Changes the tags for a given document
     * @param doc can't be null
     * @param newTags
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeDocTags(Document doc, String[] newTags) throws IllegalArgumentException, ValidationErrorException {
        if (!doc) { throw new IllegalArgumentException("Attribute 'doc' CAN NOT be null!") }
        doc.tags = newTags
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     * Changes the title for a given document
     * @param doc can't be null
     * @param newDocTitle can't be null and must be unique. You will get a ValidationErrorException if it isn't
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeDocTitle(Document doc, String newDocTitle) throws IllegalArgumentException, ValidationErrorException {
        if (!doc || !newDocTitle) { throw new IllegalArgumentException("Attribute 'doc' and 'newDocTitle' CAN NOT be null or empty!") }
        doc.docTitle = newDocTitle
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     * Changes the answer of a faq-document
     * @param doc can't be null
     * @param answer can't be null
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeFaqAnswer(Faq doc, String answer) throws IllegalArgumentException, ValidationErrorException {
        if (!doc || !answer) { throw new IllegalArgumentException("Attribute 'doc' and 'answer' CAN NOT be null!") }
        doc.answer = answer
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     * Changes the question of a faq-document
     * @param doc can't be null
     * @param question can't be null
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeFaqQuestion(Faq doc, String question) throws IllegalArgumentException, ValidationErrorException {
        if (!doc || !question) { throw new IllegalArgumentException("Attribute 'doc' and 'question' CAN NOT be null!") }
        doc.question = question
        doc.changeDate = new Date()
        if (!doc.validate()) {
            doc.errors?.allErrors?.each { log.error(it) }
            throw new ValidationErrorException('Validation for document-data was not successful!')
        }
        doc.save(flush:true)
    }

    /**
     * Changes the steps for a tutorial-document
     * @param doc can't be null
     * @param newSteps can't be null
     * @return document
     * @throws IllegalArgumentException
     * @throws ValidationErrorException
     */
    Document changeTutorialSteps(Tutorial doc, List<Step> newSteps) throws IllegalArgumentException, ValidationErrorException {
        if (!doc && !newSteps) { throw new IllegalArgumentException("Attribute 'doc' and 'newSteps' CAN NOT be null!") }
        doc.steps.collect().each { it.delete() }
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
     * Deletes a document from the database
     * @param docTitle can't be null
     * @throws IllegalArgumentException
     */
    void deleteDoc(Document doc) throws IllegalArgumentException {
        if (!doc) { throw new IllegalArgumentException("Argument 'doc' CAN NOT be null!") }
        //Falls das Dokument vom Typ Tutorial ist, lösche alle Steps
        if (doc instanceof Tutorial) {
            doc.steps?.collect()?.each { step ->
                step.delete()
            }
            doc.steps?.clear()
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
     * @param format Decides which format will be returned, use 'json' or 'xml' for either format
     * @param doc document which you want to get exported, can be null
     * @return output as JSON or XML Object
     * @throws IllegalArgumentException
     */
    def exportDoc(String format, Document doc = null) throws IllegalArgumentException {
        def output
        if (format == 'json') {
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
                tempMap.put('info', 'This object represents a list of all documents that are not locked. You can get a json/xml object of a single document by using: /document/:docTitle(.:format). The format-parameter is optional, if not given the accept header will be used.')
                tempMap.put('documents', tempList)
                output = tempMap as JSON
            }
        } else if (format == 'xml') {
            if (doc) { output = doc as XML }
            else {
                def writer = new StringWriter()
                new MarkupBuilder(writer).list {
                    info('This object represents a list of all documents that are not locked. You can get a json/xml object of a single document by using: /document/:docTitle(.format). The format-parameter is optional, if not given the accept header will be used.')
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
                output = writer.toString()
            }
        } else {
            throw new IllegalArgumentException("No such 'format' argument, please use 'json' or 'xml'!")
        }
        output
    }

    /**
     * Method for finding unlinked, unassociated documents.
     * @return list all found documents
     */
    List<Document> findUnlinkedDocs() {
        return Document.findAll().findAll { !it.linker } as List<Document>
    }

    /**
     * Method for getting the associated author of a document
     * @param doc
     * @return author if found returns a string which represents the author. if not found null
     * @throws IllegalArgumentException
     */
    String getAuthor(Document doc) throws IllegalArgumentException {
        if (!doc) { throw new IllegalArgumentException("Argument 'doc' CAN NOT be null!") }
        return (doc.linker?.subcat?.find{ it.parentCat.name == 'author' }?.name as String)?:null
    }

    /**
     * Method for getting the associated language of a document
     * @param doc
     * @return lang if found returns a string which represents the language. if not found null
     * @throws IllegalArgumentException
     */
    String getLanguage(Document doc) throws IllegalArgumentException {
        if (!doc) { throw new IllegalArgumentException("Argument 'doc' CAN NOT be null!") }
        return (doc.linker?.subcat?.find{ it.parentCat.name == 'lang' }?.name as String)?:null
    }

    /**
     * Gets a document by given title
     * @param docTitle can't be null
     * @return document
     * @throws IllegalArgumentException
     * @throws NoSuchObjectFoundException
     */
    Document getDoc(String docTitle) throws IllegalArgumentException, NoSuchObjectFoundException {
        if (!docTitle) { throw new IllegalArgumentException() }
        Document myDoc = Document.findByDocTitle(docTitle)
        if (!myDoc) { throw new NoSuchObjectFoundException("No Object 'document' with docTitle '${docTitle}' found!") }
        myDoc
    }

    /**
     * Increases the view-counter of a given document
     * @param doc can't be null
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
     * This methods helps to create a new lift of steps from a raw data input
     * IMPORTANT: For each number in the keyset you need a stepText AND stepTitle.
     * Example: If there is a key-element stepTitle_1 with non-null value then there also MUST be a key-element stepText_1 with non-null value. The stepLink is optional.
     * @param rawSteps raw data input. must have a format like [stepTitle_1:<String>, stepText_1:<String>, stepLink_1:<String>, stepTitle_2: ...]
     * @return steps a list of new created steps. null if rawSteps where empty or the amount stepTitle_X didn't match stepText_X
     */
    List<Step> newSteps(Map<String, String> rawSteps) {
        def steps = null

        if (rawSteps) {
            def allTitles = rawSteps.findAll { it.key =~ /stepTitle_[0-9]+/ && it.value } as Map
            def allTexts = rawSteps.findAll { it.key =~ /stepText_[0-9]+/  && it.value } as Map
            def allLinks = rawSteps.findAll { it.key =~ /stepLink_[0-9]+/ }
            boolean stepsError = false

            //Prüfe, ob es für jede StepTitelNummer auch eine StepTextNummer gibt, sprich ob jeder Step einen Titel und einen Text hat
            for (String stepTitle in allTitles.keySet()) {
                if (!allTexts.find { it.key =~ /stepText_${stepTitle.substring(stepTitle.indexOf('_') + 1)}/}) {
                    stepsError = true
                }
            }

            //Falls kein Fehler aufgetreten ist, verarbeite die Stepdaten und füge die Steps einer Liste hinzu
            if(!stepsError) {
                steps = []
                for (int i = 1; i <= allTitles.size(); i++) {
                    steps.add(new Step(number: i, stepTitle: allTitles.get(/stepTitle_/ + i), stepText: allTexts.get(/stepText_/ + i), stepLink: allLinks.get(/stepLink_/ + i)))
                }
            }
        }
        steps as List<Step>
    }

    /**
     * This method helps you to create a new tutorial
     * @param docTitle
     * @param tags
     * @param steps
     * @return tutorial
     * @throws ValidationErrorException
     */
    Tutorial newTutorial(String docTitle, List<Step> steps, String[] tags) throws ValidationErrorException {
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
