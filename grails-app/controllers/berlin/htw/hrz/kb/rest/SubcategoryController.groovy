package berlin.htw.hrz.kb.rest

import berlin.htw.hrz.kb.Subcategory
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class SubcategoryController extends RestfulController {
    static responseFormats = ['json', 'xml']

    SubcategoryController() {
        super(Subcategory)
        println('Test')
    }
}
