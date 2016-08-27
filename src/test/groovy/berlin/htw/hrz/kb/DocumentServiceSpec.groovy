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
@Mock([Document, Tutorial, Step, Article, Faq])
class DocumentServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test newTutorial with steps"() {
        when:
            def doc = service.newTutorial('TestingServiceTutorialNullSteps',[new Step(number: 1, stepTitle: 'TestTitel1', stepText: 'TestTitel2', stepLink: 'TestLink1'), new Step(number: 2, stepTitle: 'TestTitel2', stepText: 'TestTitel2', stepLink: 'TestLink2')] as Step[], ['Test'] as String[])
        then:
            doc instanceof Document
            doc?.docTitle == 'TestingServiceTutorialNullSteps'
            doc?.tags?.contains('Test')
            doc?.steps != null
            doc?.steps?.find({it.stepTitle == 'TestTitel1'}) != null
            doc?.steps?.find({it.stepTitle == 'TestTitel2'}) != null
            notThrown ValidationErrorException
    }

    void "test newTutorial validation error"() {
        when:
            def doc = service.newTutorial(null, null, null)
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
            def doc = service.getDoc('Nonsens')
        then:
            thrown NoSuchObjectFoundException
    }

    void "test getDoc null"() {
        when:
            def doc = service.getDoc(null)
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
            doc = service.changeTags(doc, ['hallo','welt'] as String[])
        then:
            !doc.tags.contains('test1')
            !doc.tags.contains('test2')
            doc.tags.contains('hallo')
            doc.tags.contains('welt')
    }

    void "test changeTags IllArgEx"() {
        when:
            service.changeTags(null, null)
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

    void "test changeFaqAnswer"() {
        given:
            Faq doc = new Faq(docTitle: 'Test', viewCount: 0, question: 'Test?', answer: 'Test').save(flush:true)
        when:
            doc = service.changeFaqAnswer(doc, 'Hallo Welt')
        then:
            doc.answer == 'Hallo Welt'
    }

    void "test changeFaqQuestion"() {
        given:
            Faq doc = new Faq(docTitle: 'Test', viewCount: 0, question: 'Test?', answer: 'Test').save(flush:true)
        when:
            doc = service.changeFaqQuestion(doc, 'Hallo Welt?')
        then:
            doc.question == 'Hallo Welt?'
    }

    void "test changeLocked"() {
        given:
            Document doc = new Document(docTitle: 'Test', viewCount: 0, locked: false).save(flush:true)
        when:
            doc = service.changeLocked(doc, true)
        then:
            doc.locked
    }

    void "test changeTutorialSteps"() {
        given:
            Tutorial doc = new Tutorial(docTitle: 'test', viewCount: 0).addToSteps(new Step(stepTitle: 'Test', stepText: 'Hallo Welt', number: 1)).save(flush:true)
        when:
            doc = service.changeTutorialSteps(doc, [new Step(stepTitle: 'Test', stepText: 'Hallo Welt', number: 1), new Step(stepTitle: 'Test', stepText: 'Hallo Welt', number: 2)])
        then:
            doc.steps.size() == 2
    }
}
