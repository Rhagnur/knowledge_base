import berlin.htw.hrz.kb.Document
import berlin.htw.hrz.kb.Category
import berlin.htw.hrz.kb.Subcategory
import grails.converters.JSON
import grails.converters.XML

class BootStrap {

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
                    temp.add([number: s.number, stepTitle: s.stepTitle, stepText: s.stepText, stepLink: s.stepLink])
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
                            }
                        }
                    }
                }
            }
        }
    }
    def destroy = {
    }
}
