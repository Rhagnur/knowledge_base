package berlin.htw.hrz.kb

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Step)
@Mock(Step)
class StepSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test step all"() {
        when:
            Step step = new Step(number: 22, stepTitle: 'TestingStep', stepText: 'Testing', mediaLink: 'http://testing.test/testimg.jpg', doc: new Document(docTitle: 'Test', viewCount: 1)).save()
        then:
            step.validate() == true
            step.doc instanceof Document
            step.doc.docTitle == 'Test'
            step.number == 22
            step.stepTitle == 'TestingStep'
            step.stepText == 'Testing'
            step.mediaLink == 'http://testing.test/testimg.jpg'
    }

    void "test step nullable attrs = null"() {
        when:
            Step step = new Step(number: 22, stepTitle: 'TestingStep', stepText: 'Testing', mediaLink: null, doc: new Document(docTitle: 'Test', viewCount: 1))
        then:
            step.validate() == true
    }

    void "test step not-nullable attrs = null"() {
        when:
            Step step = new Step(number: null, stepTitle: null, stepText: null, mediaLink: null, doc: null)
        then:
            step.validate() == false
    }
}
