/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.converters.JSON
import grails.converters.XML
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(DocumentService)
@Mock([Document, Tutorial, Step, Article, Faq, Subcategory, Linker, Category])
class DocumentServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test newTutorial with steps"() {
        when:
            Tutorial doc = service.newTutorial('Testdoc',[new Step(number: 1, stepTitle: 'TestTitel1', stepText: 'TestTitel2'), new Step(number: 2, stepTitle: 'TestTitel2', stepText: 'TestTitel2')] as List, ['Test'] as String[]).save()
        then:
            doc.validate() == true
            doc instanceof Tutorial
            doc?.docTitle == 'Testdoc'
            doc?.tags?.contains('Test')
            doc?.steps != null
            doc?.steps?.find({it.stepTitle == 'TestTitel1'}) != null
            doc?.steps?.find({it.stepTitle == 'TestTitel2'}) != null
            notThrown ValidationErrorException
    }

    void "test newTutorial validation error"() {
        when:
            service.newTutorial(null, null, null)
        then:
            thrown ValidationErrorException
    }

    void "test getDoc"() {
        given:
            new Document(docTitle: 'TestingServiceGetDoc', viewCount: 0).save()
        when:
            def doc = service.getDoc('TestingServiceGetDoc')
        then:
            doc instanceof Document
            doc?.docTitle == 'TestingServiceGetDoc'
    }

    void "test getDoc not-existing"() {
        when:
            service.getDoc('Nonsens')
        then:
            thrown NoSuchObjectFoundException
    }

    void "test getDoc null"() {
        when:
            service.getDoc(null)
        then:
            thrown IllegalArgumentException
    }

    void "test increaseViewCount"() {
        given:
            def doc = new Document(docTitle: 'TestingIncreaseCount', viewCount: 42)
        expect:
            doc instanceof Document
            doc != null
            doc.viewCount == 42
        when:
            doc = service.increaseCounter(doc)
        then:
            doc instanceof Document
            doc != null
            doc.viewCount == 43
    }

    void "test increaseViewCount IllArgEx"() {
        when:
            service.increaseCounter(null)
        then:
            thrown IllegalArgumentException
    }


    void "test delete doc"() {
        setup:
            Document doc = new Document(docTitle: 'TestingServiceDeleteDoc', viewCount: 0).save(flush:true)
            String title = doc.docTitle
        and:
            service.deleteDoc(doc)
        when:
            service.getDoc(title)
        then:
            thrown NoSuchObjectFoundException
    }

    void "test export doc as JSON"() {
        given:
            Document doc = new Document(docTitle: 'TestingServiceExportDocJson', viewCount: 0).save()
        expect:
            doc instanceof Document
            doc.docTitle == 'TestingServiceExportDocJson'
        when:
            def myJson = service.exportDoc('json', doc)
        then:
            myJson instanceof JSON
    }

    //Nur ein Test, hat nicht funktioniert wie es sollte, daher nicht für andere Methoden übernommen
    //Hoffnung war, zu prüfen welche äußere UND welche innere Funktion aufgerufen wurde.
    void "test exportDoc as JSON functionCalls"() {
        setup:
            Document doc = new Document(docTitle: 'TestingServiceExportDocJson', viewCount: 0).save()
            DocumentService myMock = Mock(DocumentService)
        when:
            myMock.exportDoc('json', doc)
        then:
            1 * myMock.exportDoc('json', doc)
            0 * myMock._
    }

    void "test export doc as XML"() {
        given:
            Document doc = new Document(docTitle: 'TestingServiceExportDocXml', viewCount: 0).save()
        expect:
            doc instanceof Document
        when:
            def myJson = service.exportDoc('xml', doc)
        then:
            myJson instanceof XML
    }

    void "test export doc as Nonsense"() {
        given:
            Document doc = new Document(docTitle: 'TestingServiceExportDocXml', viewCount: 0).save()
        expect:
            doc instanceof Document
        when:
            service.exportDoc('nonsense', doc)
        then:
            thrown IllegalArgumentException
    }

    void "test export doc with doc=null"() {
        when:
            def myJson = service.exportDoc('json')
        then:
            myJson instanceof JSON
    }

    void "test export doc with exportAs=null"() {
        when:
            service.exportDoc(null)
        then:
            thrown IllegalArgumentException
    }

    void "test changeTitle valid"() {
        given:
            Document doc = new Document(docTitle: 'TestingChangeDoc', viewCount: 0).save(flush:true)
        when:
            doc = service.changeDocTitle(doc, 'Hallo Welt')
        then:
            doc.docTitle == 'Hallo Welt'
    }

    void "test changeTitle IllArgEx"() {
        when:
            service.changeDocTitle(null, null)
        then:
            thrown IllegalArgumentException
    }

    void "test changeTags valid"() {
        given:
            Document doc = new Document(docTitle: 'TestingChangeDoc', viewCount: 0, tags: ['test1','test2'] as String[]).save(flush:true)
        expect:
            doc.tags.contains('test1')
            doc.tags.contains('test2')
        when:
            doc = service.changeDocTags(doc, ['hallo', 'welt'] as String[])
        then:
            !doc.tags.contains('test1')
            !doc.tags.contains('test2')
            doc.tags.contains('hallo')
            doc.tags.contains('welt')
    }

    void "test changeTags IllArgEx"() {
        when:
            service.changeDocTags(null, null)
        then:
            thrown IllegalArgumentException
    }

    void "test changeArticleContent"() {
        given:
            Article doc = new Article(docTitle: 'Test', viewCount: 0, docContent: 'TestTest').save(flush:true)
        when:
            doc = service.changeArticleContent(doc, 'Hallo Welt')
        then:
            doc.docContent == 'Hallo Welt'
    }

    void "test changeArticleContent IllArgEx"() {
        when:
            service.changeArticleContent(null, null)
        then:
            thrown IllegalArgumentException
    }

    void "test changeFaqAnswer"() {
        given:
            Faq doc = new Faq(docTitle: 'Test', viewCount: 0, question: 'Test?', answer: 'Test').save(flush:true)
        when:
            doc = service.changeFaqAnswer(doc, 'Hallo Welt')
        then:
            doc.answer == 'Hallo Welt'
    }

    void "test changeFaqAnswer IllArgEx"() {
        when:
            service.changeFaqAnswer(null, null)
        then:
            thrown IllegalArgumentException
    }

    void "test changeFaqQuestion"() {
        given:
            Faq doc = new Faq(docTitle: 'Test', viewCount: 0, question: 'Test?', answer: 'Test').save(flush:true)
        when:
            doc = service.changeFaqQuestion(doc, 'Hallo Welt?')
        then:
            doc.question == 'Hallo Welt?'
    }

    void "test changeFaqQuestion IllArgEx"() {
        when:
            service.changeFaqQuestion(null, null)
        then:
            thrown IllegalArgumentException
    }

    void "test changeLocked"() {
        given:
            Document doc = new Document(docTitle: 'Test', viewCount: 0, locked: false).save(flush:true)
        when:
            doc = service.changeDocLocked(doc, true)
        then:
            doc.locked
    }

    void "test changeLocked IllArgEx"() {
        when:
            service.changeDocLocked(null, null)
        then:
            thrown IllegalArgumentException
    }

    void "test changeTutorialSteps"() {
        given:
            Tutorial doc = new Tutorial(docTitle: 'test', viewCount: 0).addToSteps(new Step(stepTitle: 'Test', stepText: 'Hallo Welt', number: 1)).save(flush:true)
        when:
            doc = service.changeTutorialSteps(doc, [new Step(stepTitle: 'Test', stepText: 'Hallo Welt', number: 1), new Step(stepTitle: 'Test', stepText: 'Hallo Welt', number: 2)])
        then:
            doc.steps.size() == 2
    }

    void "test changeTutorialSteps IllArgEx"() {
        when:
            service.changeTutorialSteps(null, null)
        then:
            thrown IllegalArgumentException
    }

    void "test changeDocParents"() {
        given:
            Document doc = new Document(docTitle: 'Test', viewCount: 0, locked: false).save(flush:true)
            new Category(name: 'lang').addToSubCats(new Subcategory(name: 'Hallo')).save()
            new Category(name: 'author').addToSubCats(new Subcategory(name: 'Welt')).save()
        expect:
            doc.linker == null
        when:
            doc = service.changeDocParents(doc, [Subcategory.findByName('Hallo'), Subcategory.findByName('Welt')] as List<Subcategory>)
        then:
            doc instanceof Document
            doc.linker.size() == 2
            doc.linker.subcat.name.containsAll(['Hallo', 'Welt'])
    }

    void "test changeDocParents IllArgEx null"() {
        when:
            service.changeDocParents(null,null)
        then:
            IllegalArgumentException ex = thrown()
            ex.getMessage() == "Attribute 'doc' and 'newParents' CAN NOT be null or empty!"
    }

    void "test changeDocParents IllArgEx missingCats"() {
        setup:
            Document doc = new Document(docTitle: 'Test', viewCount: 0, locked: false).save(flush:true)
        when:
            service.changeDocParents(doc, [new Subcategory(name: 'Hallo').save()] as List<Subcategory>)
        then:
            IllegalArgumentException ex = thrown()
            ex.getMessage() == "Attribute 'newParents' must contain a child element from 'lang' and 'author'!"

    }

    void "test findUnlinkedDocs"() {
        setup:
            new Document(docTitle: 'Test', viewCount: 0, locked: false).save(flush:true)
            new Document(docTitle: 'Test2', viewCount: 0, locked: false).save(flush:true)
        when:
            List<Document> docs = service.findUnlinkedDocs()
        then:
            docs.size() == 2
            docs.docTitle.containsAll(['Test','Test2'])

    }

    void "test newArticle"() {
        when:
            Article doc = service.newArticle('Test', 'Testing', null)
        then:
            notThrown Exception
            doc instanceof Article
            doc.docTitle == 'Test'
            doc.docContent == 'Testing'
    }

    void "test newArticle ValiErrEx"() {
        when:
            service.newArticle(null, null, null)
        then:
            thrown ValidationErrorException
    }

    void "test newFaq"() {
        when:
            Faq doc = service.newFaq('Test', 'Testing', null)
        then:
            notThrown Exception
            doc instanceof Faq
            doc.question == 'Test'
            doc.docTitle == doc.question
            doc.answer == 'Testing'
    }

    void "test newFaq ValiErrEx"() {
        when:
            service.newFaq(null, null, null)
        then:
            thrown ValidationErrorException
    }

    void "test newSteps"() {
        when:
            List<Step> steps = service.newSteps([stepTitle_1:'Test', stepText_1:'Testing', steptTitle_2:'Test2', stepText_2:'Testing2'])
        then:
            steps != null
            steps instanceof List<Step>
    }

    void "test newSteps null returns null"() {
        when:
            List<Step> steps = service.newSteps(null)
        then:
            steps == null
    }

    void "test newSteps stepText not match stepTitle"() {
        when:
            List<Step> steps = service.newSteps([stepTitle_1:'Test', stepText_2:'Testing2'])
        then:
            steps == null
    }
}
