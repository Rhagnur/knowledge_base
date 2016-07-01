package berlin.htw.hrz.kb

import grails.transaction.Transactional

@Transactional
class DocumentService {

    def createDocs() {
        //Document.saveAll([
        //        new Document(title: "Test1", content: "Testcontent1"),
        //        new Document(title: "Test2", content: "Testcontent2")
        //])
        def p = new Document(title: "Test1", content: "Testcontent1", hiddenTags: "Test")
        println('VALIDATEEEEEEEEE!"!!!!!!!!! ' + p.validate())
        p.save(flush:true)
    }

    def getAllDocs() {
        [Document.findAll()]
    }

    def addDoc(String doctitle, String docContent, String[] docHiddenTags, String[] subCats) {

        def doc = new Document(title: doctitle, content: docContent, hiddenTags: docHiddenTags)
        for (def cat in subCats) {
            println(cat)
            Subcategorie subCat = Subcategorie.findByName(cat)
            subCat.addToDocs(doc)
            subCat.save()
        }
        doc.save()
    }
}
