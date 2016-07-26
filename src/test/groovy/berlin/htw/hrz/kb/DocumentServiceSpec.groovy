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
@Mock([Document, Step])
class DocumentServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test newTutorial with null steps"() {
        given:
            def doc = service.newTutorial('TestingServiceTutorialNullSteps', ['Test'] as String[], null)
        expect:
            doc != null
            doc?.docTitle == 'TestingServiceTutorialNullSteps'
            doc?.hiddenTags?.contains('Test')
            doc?.steps == null
    }

    void "test newTutorial with steps"() {
        given:
            def doc = service.newTutorial('TestingServiceTutorialNullSteps', ['Test'] as String[], [new Step(number: 1, stepTitle: 'TestTitel1', stepText: 'TestTitel2', mediaLink: 'TestLink1'), new Step(number: 2, stepTitle: 'TestTitel2', stepText: 'TestTitel2', mediaLink: 'TestLink2')] as Step[])
        expect:
            doc instanceof Document
            doc?.docTitle == 'TestingServiceTutorialNullSteps'
            doc?.hiddenTags?.contains('Test')
            doc?.steps != null
            doc?.steps?.find({it.stepTitle == 'TestTitel1'}) != null
            doc?.steps?.find({it.stepTitle == 'TestTitel2'}) != null
    }

    void "test newTutorial validation error"() {
        given:
            def doc = service.newTutorial(null, null, null)
        expect:
            doc instanceof Integer
            doc == -2
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


    void "test delete doc"() {
        given:
            def doc = new Document(docTitle: 'TestingServiceDeleteDoc', viewCount: 0).save()
        expect:
            doc instanceof Document
        when:
            def code = service.deleteDoc(doc.docTitle)
            doc = service.getDoc('TestingServiceDeleteDoc')
        then:
            code instanceof Integer
            code == 0
            doc == null
    }

    void "test export doc as JSON"() {
        given:
            service.newTutorial('TestingServiceExportDocJson', null, null)
            def doc = new Document(docTitle: 'TestingServiceExportDocJson', viewCount: 0).save()
        expect:
            doc instanceof Document
        when:
            def myJson = service.exportDoc('TestingServiceExportDocJson', 'json')
        then:
            myJson instanceof JSON
    }

    void "test export doc as XML"() {
        given:
            def doc = new Document(docTitle: 'TestingServiceExportDocXml', viewCount: 0).save()
        expect:
            doc instanceof Document
        when:
            def myJson = service.exportDoc('TestingServiceExportDocXml', 'xml')
        then:
            myJson instanceof XML
    }
}
