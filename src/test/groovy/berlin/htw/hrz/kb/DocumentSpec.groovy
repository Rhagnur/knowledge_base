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
@TestFor(Document)
@Mock(Document)
class DocumentSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test document valide"() {
        when:
            Document doc = new Document(docTitle: 'TestingDoc', createDate: new Date(), changeDate: new Date(), changedBy: ['didschu - 11.11.1111'], tags: ['test', 'testing'], viewCount: 42, locked: true).save()
        then:
            notThrown Exception
            doc != null
            doc instanceof Document
            doc.docTitle == 'TestingDoc'
            doc.createDate instanceof Date
            doc.changeDate instanceof Date
            doc.viewCount == 42
            doc.changedBy == ['didschu - 11.11.1111'] as String[]
            doc.tags == ['test', 'testing'] as String[]
            doc.locked
    }

    void "test document nullable attrs = null"() {
        when:
            Document doc = new Document(docTitle: 'TestingNullableDoc', viewCount: 812)
        then:
            doc.validate() == true
    }

    void "test document not-nullable attrs = null"() {
        when:
            Document doc = new Document(docTitle: null, viewCount: null)
        then:
            doc.validate() == false
    }

    void "test document name not valid"() {
        when:
            Document doc = new Document(docTitle: '<not working>Test$', viewCount: 0)
        then:
            doc.validate() == false
    }
}
