package berlin.htw.hrz.kb.rest

import berlin.htw.hrz.kb.Category
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class CategoryController extends RestfulController {
    static responseFormats = ['json', 'xml']

    CategoryController() {
        super(Category)
        println('Test')
    }
}
