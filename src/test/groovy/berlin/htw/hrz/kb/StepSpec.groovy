/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Step)
@Mock([Step, Document])
class StepSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test step valid"() {
        when:
            Step step = new Step(number: 20, stepTitle: 'TestingStep', stepText: 'Testing', stepLink: null, doc: new Document(docTitle: 'Test', viewCount: 1))
        then:
            step.validate() == true
            step.doc instanceof Document
            step.doc.docTitle == 'Test'
            step.number == 20
            step.stepTitle == 'TestingStep'
            step.stepText == 'Testing'
    }

    void "test step nullable attrs = null"() {
        when:
            Step step = new Step(number: 21, stepTitle: 'TestingStep', stepText: 'Testing', stepLink: null, doc: new Document(docTitle: 'Test', viewCount: 1))
        then:
            step.validate() == true
    }

    void "test step not-nullable attrs = null"() {
        when:
            Step step = new Step(number: null, stepTitle: null, stepText: null, doc: null)
        then:
            step.validate() == false
    }

    void "test step name not valid"() {
        when:
            Step step = new Step(number: 23, stepTitle: '$%TestingStep<img>', stepText: 'Testing', stepLink: null, doc: new Document(docTitle: 'Test', viewCount: 1))
        then:
            step.validate() == false
    }
}
