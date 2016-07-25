import berlin.htw.hrz.kb.Document
import berlin.htw.hrz.kb.Maincategorie
import berlin.htw.hrz.kb.Subcategorie
import grails.converters.JSON
import grails.converters.XML

class BootStrap {

    def init = { servletContext ->
        JSON.registerObjectMarshaller(Document) { doc ->
            def output = [:]
            output.docTitle = doc.docTitle
            def type = Subcategorie.findAllByMainCat(Maincategorie.findByName('doctype')).find{it.docs.contains(doc)}
            if (type) output.docType = type.name
            def author = Subcategorie.findAllByMainCat(Maincategorie.findByName('author')).find{it.docs.contains(doc)}
            if (author) output.author = author.name
            def lang = Subcategorie.findAllByMainCat(Maincategorie.findByName('lang')).find{it.docs.contains(doc)}
            if (lang) output.lang = lang.name
            output.hiddenTags = doc.hiddenTags
            if (doc.docContent) output.docContent = doc.docContent
            if (doc.faq) {
                output.faq = [question: doc.faq.question, answer: doc.faq.answer]
            }
            if (doc.steps) {

                def temp = []
                def temp2 = [:]
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
                def temp = Subcategorie.findAllByMainCat(Maincategorie.findByName('doctype')).find{it.docs.contains(doc)}
                if (temp) docType(temp.name)
                temp = Subcategorie.findAllByMainCat(Maincategorie.findByName('author')).find{it.docs.contains(doc)}
                if (temp) author(temp.name)
                temp = Subcategorie.findAllByMainCat(Maincategorie.findByName('lang')).find{it.docs.contains(doc)}
                if (temp) lang(temp.name)
                hiddenTags(doc.hiddenTags)
                if (doc.faq) {
                    faq([]) {
                        question(doc.faq.question)
                        answer(doc.faq.answer)
                    }
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
