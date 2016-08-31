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
@TestFor(Tutorial)
@Mock([Tutorial, Document, Step])
class TutorialSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test tutorial all"() {
        when:
            Tutorial tut = new Tutorial(docTitle: 'TestingTutorial', viewCount: 23).addToSteps(new Step(number: 1, stepTitle: 'Test', stepText: 'Test')).addToSteps(new Step(number: 2, stepTitle: 'Test', stepText: 'Test')).save()
        then:
            tut.validate() == true
            tut instanceof Tutorial
            tut instanceof Document
            tut.docTitle == 'TestingTutorial'
            tut.viewCount == 23
            tut.steps.size() == 2
    }

    void "test tutorial not nullable = null"() {
        when:
            Tutorial tut = new Tutorial(docTitle: 'TestingTutNullable', viewCount: 2, steps: null)
        then:
            tut.validate() == false
    }

    void "test inheritance"() {
        when:
            Tutorial tut = new Tutorial(docTitle: 'TestingTutNullable', viewCount: 2, steps: null)
        then:
            tut instanceof Tutorial
            tut instanceof Document
    }
}
