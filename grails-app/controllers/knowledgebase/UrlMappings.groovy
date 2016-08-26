/*
 * Edited by didschu
 */
package knowledgebase

class UrlMappings {

    static mappings = {
        //URL Mappings für sinnvolle REST-Schnittstelle, um Dokumente als JSON/XML zu exportieren
        "/document.$format?"(controller: 'knowledgeBase') {
            action = [GET:'exportDoc']
        }
        "/document/$docTitle?.$format?"(controller: 'knowledgeBase') {
            action = [GET:'exportDoc']
        }

        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
        "/"(controller: 'knowledgeBase', action: 'index')
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
