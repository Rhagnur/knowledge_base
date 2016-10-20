package berlin.htw.hrz.kb.rest

import berlin.htw.hrz.kb.Article
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ArticleController extends RestfulController {
    static responseFormats = ['json', 'xml']

    ArticleController() {
        super(Article)
        println('Test')
    }
}
