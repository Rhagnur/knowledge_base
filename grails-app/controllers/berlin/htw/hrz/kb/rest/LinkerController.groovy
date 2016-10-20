package berlin.htw.hrz.kb.rest

import berlin.htw.hrz.kb.Linker
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class LinkerController extends RestfulController {
    static responseFormats = ['json', 'xml']

    LinkerController() {
        super(Linker)
        println('Test')
    }
}