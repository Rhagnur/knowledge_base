package berlin.htw.hrz.kb

import grails.converters.JSON
import grails.converters.XML
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.neo4j.graphdb.NotFoundException
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
        when:
            def doc = service.newTutorial('TestingServiceTutorialNullSteps', ['Test'] as String[], null)
        then:
            doc != null
            doc?.docTitle == 'TestingServiceTutorialNullSteps'
            doc?.hiddenTags?.contains('Test')
            doc?.steps == null
            notThrown Exception
    }

    void "test newTutorial with steps"() {
        when:
            def doc = service.newTutorial('TestingServiceTutorialNullSteps', ['Test'] as String[], [new Step(number: 1, stepTitle: 'TestTitel1', stepText: 'TestTitel2', mediaLink: 'TestLink1'), new Step(number: 2, stepTitle: 'TestTitel2', stepText: 'TestTitel2', mediaLink: 'TestLink2')] as Step[])
        then:
            doc instanceof Document
            doc?.docTitle == 'TestingServiceTutorialNullSteps'
            doc?.hiddenTags?.contains('Test')
            doc?.steps != null
            doc?.steps?.find({it.stepTitle == 'TestTitel1'}) != null
            doc?.steps?.find({it.stepTitle == 'TestTitel2'}) != null
            notThrown Exception
    }

    void "test newTutorial validation error"() {
        when:
            def doc = service.newTutorial(null, null, null)
        then:
            Exception ex = thrown()
            ex.message == 'ERROR: Validation of data wasn\'t successfull'
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
            thrown NotFoundException
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
        given:
            def doc = new Document(docTitle: 'TestingServiceDeleteDoc', viewCount: 0).save()
        expect:
            doc instanceof Document
        when:
            service.deleteDoc(doc.docTitle)
        then:
            notThrown Exception
    }

    void "test export doc as JSON"() {
        given:
            def doc = new Document(docTitle: 'TestingServiceExportDocJson', viewCount: 0).save()
        expect:
            doc instanceof Document
        when:
            def myJson = service.exportDoc('TestingServiceExportDocJson', 'json')
        then:
            myJson instanceof JSON
            //todo 1 * service.exportDoc('TestingServiceExportDocJson', 'json')
            //1 * service.getDoc('TestingServiceExportDocJson')
            //0 * service._
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

    void "test export doc as Nonsense"() {
        given:
            def doc = new Document(docTitle: 'TestingServiceExportDocXml', viewCount: 0).save()
        expect:
            doc instanceof Document
        when:
            def myNonsense = service.exportDoc('TestingServiceExportDocXml', 'nonsense')
        then:
            thrown IllegalArgumentException
    }
}
