/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Faq)
@Mock([Document, Faq])
class FaqSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test faq all not null"() {
        when:
            Faq faq = new Faq(docTitle: 'TestingFaq', viewCount: 22, question: 'Testfrage?', answer: 'Testantwort').save()
        then:
            faq.validate() == true
            faq instanceof Document
            faq instanceof Faq
            faq.docTitle == 'TestingFaq'
            faq.viewCount == 22
            faq.question == 'Testfrage?'
            faq.answer == 'Testantwort'
    }

    void "test faq not nullable attrs = null"() {
        when:
            Faq faq = new Faq(docTitle: 'TestingFaqNullable', viewCount: 2, question: null, answer: null)
        then:
            faq.validate() == false
    }

    void "test inheritance"() {
        when:
            Faq faq = new Faq(docTitle: 'TestingFaqInheritance', viewCount: 2, question: null, answer: null)
        then:
            faq instanceof Faq
            faq instanceof Document
    }
}
