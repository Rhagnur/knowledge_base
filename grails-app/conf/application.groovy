grails.resources.debug = true

//grails.plugin.springsecurity.useBasicAuth = true
//grails.plugin.springsecurity.basic.realmName = "HRZ Knowledge Base"
grails.plugin.springsecurity.ipRestrictions = [
        [pattern: '/**', access: '141.45.0.0/16'],
        [pattern: '/login/impersonate', access: '141.45.0.0/16'],
        [pattern: '/logout/impersonate', access: '141.45.0.0/16']
]
grails.plugin.springsecurity.logout.postOnly = false
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        [pattern: '/',               access: ['permitAll']],
        [pattern: '/error',          access: ['permitAll']],
        [pattern: '/index',          access: ['permitAll']],
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