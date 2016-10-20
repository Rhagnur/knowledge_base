package berlin.htw.hrz.kb.rest

import berlin.htw.hrz.kb.Tutorial
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class TutorialController extends RestfulController {
    static responseFormats = ['json', 'xml']

    TutorialController() {
        super(Tutorial)
        println('Test')
    }
}
