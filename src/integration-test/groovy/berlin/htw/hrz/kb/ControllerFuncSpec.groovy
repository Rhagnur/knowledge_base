/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.test.mixin.integration.Integration
import grails.transaction.*

import spock.lang.*
import geb.spock.*

/**
 * See http://www.gebish.org/manual/current/ for more instructions
 */
@Integration
@Rollback
class ControllerFuncSpec extends GebSpec {

    def setup() {
    }

    def cleanup() {
    }

    void "test REST get doc !title !format"() {
        given:
            RestBuilder rest = new RestBuilder()
        when:
            RestResponse response = rest.get("http://localhost:8080/document")
        then:
        	response.status == 200
            response.xml.getProperty('info').toString() == 'This object represents a list of all documents that are not locked. You can get a json/xml object of a single document by using: /document/:docTitle(.:format). The format-parameter is optional, if not given the accept header will be used.'
            response.xml.getProperty('documents') != null
    }

    void "test REST get doc !title format=xml"() {
        given:
        RestBuilder rest = new RestBuilder()
        when:
        RestResponse response = rest.get("http://localhost:8080/document.xml")
        then:
        response.status == 200
        response.xml.getProperty('info').toString() == 'This object represents a list of all documents that are not locked. You can get a json/xml object of a single document by using: /document/:docTitle(.:format). The format-parameter is optional, if not given the accept header will be used.'
        response.xml.getProperty('documents') != null
    }

    void "test REST get doc !title format=json"() {
        given:
            RestBuilder rest = new RestBuilder()
        when:
            RestResponse response = rest.get("http://localhost:8080/document.json")
        then:
            response.status == 200
            response.json['info'] instanceof String
            response.json['info'] == 'This object represents a list of all documents that are not locked. You can get a json/xml object of a single document by using: /document/:docTitle(.:format). The format-parameter is optional, if not given the accept header will be used.'
            response.json['documents'].each { doc ->
                doc.docType instanceof String
                doc.docType =~ /(Article)|(Faq)|(Tutorial)/
                doc.viewCount instanceof Integer
                doc.author instanceof String
                doc.author =~ /[a-z]+/
                doc.docTitle instanceof String
                doc.docTitle =~ /[A-Za-z0-9? ]+/
            }
    }

    void "test REST get doc title !format"() {
        given:
            RestBuilder rest = new RestBuilder()
        when:
            RestResponse response = rest.get("http://localhost:8080/document/Did+integration-test+work%3F")
        then:
            response.status == 200
            response.xml.getProperty('question') == 'Did integration-test work?'
    }

    void "test REST get doc title format=xml"() {
        given:
        RestBuilder rest = new RestBuilder()
        when:
        RestResponse response = rest.get("http://localhost:8080/document/Did+integration-test+work%3F.xml")
        then:
        response.status == 200
        response.xml.getProperty('question') == 'Did integration-test work?'
    }

    void "test REST get doc title format=json"() {
        given:
            RestBuilder rest = new RestBuilder()
        when:
            RestResponse response = rest.get("http://localhost:8080/document/Did+integration-test+work%3F.json")
        then:
            response.status == 200
            response.json['question'] == 'Did integration-test work?'
    }

    void "test REST get doc wrong format"() {
        given:
            RestBuilder rest = new RestBuilder()
        when:
            RestResponse response = rest.get("http://localhost:8080/document/Did+integration-test+work%3F.nonsense")
        then:
            response.status == 200
            response.xml != null
    }

    void "test REST get doc wrong title"() {
        given:
            RestBuilder rest = new RestBuilder()
        when:
            RestResponse response = rest.get("http://localhost:8080/document/NonsenseHalloWeltUndSo")
        then:
            response.status == 500
            response.xml =~ /Short:No Object 'document' with docTitle 'NonsenseHalloWeltUndSo' found!; Long:./
    }

}
