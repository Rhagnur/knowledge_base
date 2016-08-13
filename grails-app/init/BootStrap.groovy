import berlin.htw.hrz.kb.Document
import berlin.htw.hrz.kb.Category
import berlin.htw.hrz.kb.Subcategory
import grails.converters.JSON
import grails.converters.XML

class BootStrap {

    def init = { servletContext ->
        JSON.registerObjectMarshaller(Document) { doc ->
            def output = [:]
            output.title = doc.docTitle
            output.docType=doc.class.simpleName
            def author = Subcategory.findAllByParentCat(Category.findByName('author')).find{it.docs.contains(doc)}
            if (author) output.author = author.name
            def lang = Subcategory.findAllByParentCat(Category.findByName('lang')).find{it.docs.contains(doc)}
            if (lang) output.lang = lang.name
            if (doc.tags) output.tags = doc.tags
            if (doc.docContent) output.content = doc.docContent
            if (doc.question) output.question = doc.question
            if (doc.answer) output.answer =  doc.answer

            if (doc.steps) {

                def temp = []
                for (s in doc.steps.sort{ it.number }) {
                    temp.add([number: s.number, stepTitle: s.stepTitle, stepText: s.stepText, mediaLink: s.mediaLink])
                }
                output.steps = temp
            }

            return output

        }



        XML.registerObjectMarshaller(Document) { doc, xml ->
            xml.build{
                title(doc.docTitle)
                def temp = Subcategory.findAllByParentCat(Category.findByName('author')).find{it.docs.contains(doc)}
                if (temp) author(temp.name)
                temp = Subcategory.findAllByParentCat(Category.findByName('lang')).find{it.docs.contains(doc)}
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
                                if (s.mediaLink) mediaLink(s.mediaLink)
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
