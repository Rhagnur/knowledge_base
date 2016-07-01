package berlin.htw.hrz.kb

class KnowledgeBaseController {

    def documentService
    def initService

    def index() {
        def myDocs = []

        println('Maincats : ' + Maincategorie.findAll())
        if (Maincategorie.findAll().empty) {
            println('Keine Maincategorien angelegt')
            initService.initTestModell()
        } else {
            println('Maincategorien gefunden')
        }

        //println('##### MainCats!!!!!!!! ' + Maincategorie.findAllBySubCats())


        documentService.addDoc('LSF for faculty', 'Testcontent LSF Faculty', ['lsf', 'faculty'] as String[], ['anonym', 'faculty', 'lsf', 'tutorial', 'eng'] as String[])

        /*
        def request
        request = new Document(title: "Test", content: "Testcontent")
        request.save(flush:true)
        request = new Document(title: "Test2", content: "Testcontent")
        request.save(flush:true)
        request = Document.findAll()
        println (request.errors)
        println(request)
        documentService.createDocs()
        println('AUSGABE!!!!!!!!!!!!!!! ' + documentService.getAllDocs())
        def p = Document.cypherStatic("MATCH (n:tutorial) return n.title as title, n.content as content")
        while (p.hasNext()) {
            myDocs << p.next()
        }

        //println(myDocs)
        [myDocs]
        */
    }
}
