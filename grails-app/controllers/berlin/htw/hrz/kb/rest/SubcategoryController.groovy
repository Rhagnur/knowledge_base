package berlin.htw.hrz.kb.rest

import berlin.htw.hrz.kb.Subcategory
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class SubcategoryController extends RestfulController {
    SubcategoryController() {
        super(Subcategory)
        println('Test')
    }
}
