/*
  Created by IntelliJ IDEA.
  User: didschu
 */
package berlin.htw.hrz.kb


class KnowledgeBaseController {

    def documentService
    def initService

    def index() {
        if (Maincategorie.findAll().empty) {
            initService.initTestModell()
        }

        //println('##### MainCats!!!!!!!! ' + Maincategorie.findAllBySubCats())


        if (documentService.addDoc('LSF für Lehrende', 'Testcontent LSF Faculty', ['lsf', 'faculty'] as String[], ['faculty', 'lsf', 'tutorial', 'de'] as String[])) {
            println('Alles gut')
        }
        else {
            println('Bäh')
        }

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

    def getSubCats(def cat) {
        def subs = []
        if (cat) {
            if (!(cat instanceof Maincategorie)) {
                subs += cat
            }
            cat.subCats?.each { child ->
                subs += getSubCats(child)
            }
            subs.unique()
        }
    }

    def testingThings() {
        println('\n\n####### Get all Subcats from one Maincategorie, not iterativ #######' )
        //Get one main categorie and all subcategories
        def catGroup = Maincategorie.findByName('os')
        println('Main: ' + catGroup.name)
        for (def sub in catGroup.subCats.findAll()) {
            println('Subs: ' + sub.name)
        }

        println('\n\n####### Get all Subcats from one Maincategorie...iterativ #######' )
        def all= getSubCats(Maincategorie.findByName('os'))
        all.each {
            println(it.name)
        }

        println('\n\n####### Get all docs from one Categorie #######' )
        def docs = Subcategorie.findByName('win_7').docs.findAll()
        for (def doc in docs) {
            println(doc.title)
        }

        println('\n\n####### Get all docs from multiple Categories #######' )
        docs = Subcategorie.findByName('win_7').docs.findAll().toArray()
        println(docs)
        docs += Subcategorie.findByName('de').docs.findAll().toArray()
        println(docs)
        println('Vor ...Filterung...')
        for (def doc in docs) {
            println(doc.id + ' # ' +doc.title)
        }
        println('\nNach ...Filterung...')
        def matchItems = docs.findAll{docs.count(it) > 1}.unique()
        for (def doc in matchItems) {
            println(doc.id + ' # ' +doc.title)
        }

        println('\n\n####### Get all docs from multiple Categories via DocService method #######' )
        documentService.getAllDocsAssociatedToSubCategories(['win_7', 'student'] as String[]).each { doc ->
            println('Doc: ' + doc.title)
        }


        render(view: 'index')
    }

    def createDoc() {
        println(params)
        if (params.submit) {
            println('Anlegen')
            if (!params.docTitle.empty && !params.docContent.empty && !params.docTags.empty) {

                def liste = params.list('checkbox')
                println(liste.size())
                String[] cats = new String[liste.size()];
                cats = liste.toArray(cats);
                println(cats)
                String tags = params.docTags
                println('tags: ' + tags)
                String[] split = tags.split(",")
                println(split)
                documentService.addDoc(params.docTitle, params.docContent, split, cats)
                flash.info = "Doc angelegt"
                render(view: 'index')

            } else {
                flash.error = "Bitte alle Felder ausfüllen!"
            }
        }

        String[] all = []
        Maincategorie.findAll().each { mainCat ->
            getSubCats(mainCat).each { cat ->
                all += cat.name as String
            }
        }
        println('all: ' + all)
        [cats: all]
    }
}
