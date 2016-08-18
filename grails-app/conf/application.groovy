/**
 * Created by didschu on 30.06.16.
 */

grails.resources.debug = true

//grails.plugin.springsecurity.useBasicAuth = true
//grails.plugin.springsecurity.basic.realmName = "HRZ Knowledge Base"
grails.plugin.springsecurity.logout.postOnly = false
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        //[pattern: '/login/impersonate', access:['hasAuthority("OU_ZE_HRZ")', 'IS_AUTHENTICATED_FULLY' ]],
        //[pattern: '/logout/impersonate', access: ['permitAll']],
        [pattern: '/',               access: ['permitAll']],
        [pattern: '/error',          access: ['permitAll']],
        [pattern: '/index',          access: ['permitAll']],
        //[pattern: '/account',        access: ['isFullyAuthenticated()']],
        [pattern: '/index.gsp',      access: ['permitAll']],
        [pattern: '/shutdown',       access: ['permitAll']],
        [pattern: '/assets/**',      access: ['permitAll']],
        [pattern: '/**/js/**',       access: ['permitAll']],
        [pattern: '/**/css/**',      access: ['permitAll']],
        [pattern: '/**/images/**',   access: ['permitAll']],
        [pattern: '/**/favicon.ico', access: ['permitAll']]
        ]

grails {
    neo4j {
        type = "rest"
        //location = "http://kbjan.rz.htw-berlin.de:7474/db/data"
        location = "http://localhost:7474/db/data"
        username = "xxx"
        password = "xxx"
    }
}