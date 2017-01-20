import berlin.htw.hrz.kb.Document
import berlin.htw.hrz.kb.Category
import berlin.htw.hrz.kb.Subcategory
import grails.converters.JSON
import grails.converters.XML
import grails.core.GrailsApplication

class BootStrap {
    GrailsApplication grailsApplication

    /*
    * Edited by didschu
    * Marshaller eingerichtet für JSON und XML, um Schwierigkeiten bei 'nested data' zu umgehen und zudem unnötige Informationen zu verhindern und nützliche einzupflegen
    */
    def init = { servletContext ->
        JSON.registerObjectMarshaller(Document) { doc ->
            def output = [:]
            output.title = doc.docTitle
            output.docType=doc.class.simpleName
            def author = doc.linker.find{ it.subcat?.parentCat?.name == 'author' }?.subcat
            if (author) output.author = author.name
            def lang = doc.linker.find{ it.subcat?.parentCat?.name == 'lang' }?.subcat
            if (lang) output.lang = lang.name
            if (doc.tags) output.tags = doc.tags
            if (doc.docContent) output.content = doc.docContent
            if (doc.question) output.question = doc.question
            if (doc.answer) output.answer =  doc.answer

            if (doc.steps) {

                def temp = []
                for (s in doc.steps.sort{ it.number }) {
                    temp.add([number: s.number, stepTitle: s.stepTitle, stepText: s.stepText, stepLink: s.stepLink, stepImage: s.stepImage])
                }
                output.steps = temp
            }

            return output

        }

        XML.registerObjectMarshaller(Document) { doc, xml ->
            xml.build{
                title(doc.docTitle)
                def temp = doc.linker.find{ it.subcat?.parentCat?.name == 'author' }?.subcat
                if (temp) author(temp.name)
                temp = doc.linker.find{ it.subcat?.parentCat?.name == 'lang' }?.subcat
                if (temp) lang(temp.name)
                if (doc.tags) tags(doc.tags)
                if (doc.docContent) content(doc.docContent)
                if (doc.question && doc.answer) {
                    question(doc.question)
                    answer(doc.answer)
                }

                if (doc.steps) {
                    steps([]) {
                        for (s in doc.steps.sort{ it.number }) {
                            step([]) {
                                number(s.number)
                                stepTitle(s.stepTitle)
                                stepText(s.stepText)
                                stepLink(s.stepLink)
                                stepImage(s.stepImage)
                            }
                        }
                    }
                }
            }
        }

        println "temp_path: ${grailsApplication.config.get('kb.temp.dir')}"

        println "Checking for kb.home and sub dirs..."
        File homeDir = new File(grailsApplication.config.getAt('kb.home.dir') as String)
        File tmpDir = new File(grailsApplication.config.getAt('kb.temp.dir') as String)
        File fileDir = new File(grailsApplication.config.getAt('kb.file.dir') as String)

        println homeDir.absolutePath
        println homeDir.exists()

        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
            println tmpDir.path
        }
        else {
            if (!tmpDir.directory && !tmpDir.canWrite() && !tmpDir.canRead()) {
                println "ATTENTION!!!"
            } else {
                println "DAS"
                println tmpDir.absolutePath
                println tmpDir.exists()
            }
        }
        if (!fileDir.exists()) {
            fileDir.mkdirs()
            println fileDir.path
        }
        else {
            if (!fileDir.directory && !fileDir.canWrite() && !fileDir.canRead()) {
                println "ATTENTION!!!"
            } else {
                println "DAS"
                println fileDir.absolutePath
                println fileDir.exists()
            }

        }
        servletContext['file_mapper'] = new HashMap<String, String>()




    }
    def destroy = {
    }
}
