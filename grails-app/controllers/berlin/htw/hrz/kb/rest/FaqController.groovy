package berlin.htw.hrz.kb.rest

import berlin.htw.hrz.kb.Faq
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class FaqController extends RestfulController {
    static responseFormats = ['json', 'xml']

    FaqController() {
        super(Faq)
        println('Test')
    }
}