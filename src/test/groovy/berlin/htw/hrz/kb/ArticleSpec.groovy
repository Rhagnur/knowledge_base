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
@TestFor(Article)
@Mock([Document, Article])
class ArticleSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test new article not nullable attrs = null"() {
        when:
            Article article = new Article(docTitle: 'TestingNewArticleNullContent', docContent: null)
        then:
            article.validate() == false
    }

    void "test new Article all"() {
        when:
            Article article = new Article(docTitle: 'TestingNewArticleContent', viewCount: 2, docContent: 'Testing').save()
        then:
            article.validate() == true
            article.docTitle == 'TestingNewArticleContent'
            article.docContent == 'Testing'
    }

    void "test inheritance"() {
        when:
            Article article = new Article(docTitle: 'TestingNewArticleContent', docContent: null)
        then:
            article instanceof Article
            article instanceof Document
    }
}
