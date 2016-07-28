import berlin.htw.hrz.kb.Document
import berlin.htw.hrz.kb.Maincategory
import berlin.htw.hrz.kb.Subcategory
import grails.converters.JSON
import grails.converters.XML

class BootStrap {

    def init = { servletContext ->
        JSON.registerObjectMarshaller(Document) { doc ->
            def output = [:]
            output.docTitle = doc.docTitle
            def type = Subcategory.findAllByMainCat(Maincategory.findByName('doctype')).find{it.docs.contains(doc)}
            if (type) output.docType = type.name
            def author = Subcategory.findAllByMainCat(Maincategory.findByName('author')).find{it.docs.contains(doc)}
            if (author) output.author = author.name
            def lang = Subcategory.findAllByMainCat(Maincategory.findByName('lang')).find{it.docs.contains(doc)}
            if (lang) output.lang = lang.name
            output.hiddenTags = doc.hiddenTags
            if (doc.docContent) output.docContent = doc.docContent
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
                docTitle(doc.docTitle)
                def temp = Subcategory.findAllByMainCat(Maincategory.findByName('doctype')).find{it.docs.contains(doc)}
                if (temp) docType(temp.name)
                temp = Subcategory.findAllByMainCat(Maincategory.findByName('author')).find{it.docs.contains(doc)}
                if (temp) author(temp.name)
                temp = Subcategory.findAllByMainCat(Maincategory.findByName('lang')).find{it.docs.contains(doc)}
                if (temp) lang(temp.name)
                hiddenTags(doc.hiddenTags)
                question(doc.question)
                answer(doc.answer)

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
