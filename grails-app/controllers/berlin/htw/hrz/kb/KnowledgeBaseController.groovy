/*
  Created by IntelliJ IDEA.
  User: didschu
 */
package berlin.htw.hrz.kb

import grails.converters.JSON

class KnowledgeBaseController {

    def documentService
    def categorieService
    def initService

    def index() {
        if (Maincategorie.findAll().empty) {
            initService.initTestModell()
            flash.info = "Neo4j war leer, Test-Domainklassen, Dokumente und Beziehungen angelegt"
        }
        null
    }

    def showDoc() {
        def myDoc = documentService.exportDoc('Cisco-Telefonie', 'map')

        //Falls ein anderes Dokument angezeigt werden soll, überschreibe das Default-Test-Dokument
        if (params.docTitle) {
            myDoc = documentService.exportDoc('Cisco-Telefonie', 'map')
        }
        println('Doc: ' + myDoc)
        [document: myDoc]
    }




    def showCat() {
        def myCats = Maincategorie.findAll()
        //Default = hole alle Mainkategorien, ansonsten hole die Subkategorien der ausgewählten Kategorie
        if (params.cat) {
            myCats = categorieService.getAllSubCats(Subcategorie.findByName(params.cat)?Subcategorie.findByName(params.cat):Maincategorie.findByName(params.cat)?Maincategorie.findByName(params.cat):null)
            if (!myCats) {flash.error = "No such categorie!!!"}
        }
        [cats: myCats]
    }

    def createDoc() {
        println(params)
        if (params.submit) {
            if (!params.docTitle.empty && !params.docContent.empty && !params.docTags.empty) {

                def liste = params.list('checkbox')
                String[] cats = new String[liste.size()];
                cats = liste.toArray(cats);
                String tags = params.docTags
                String[] split = tags.split(",")
                documentService.addDoc(params.docTitle, params.docContent, split, cats)
                flash.info = "Doc angelegt"
                render(view: 'index')

            } else {
                flash.error = "Bitte alle Felder ausfüllen!"
            }
        }

        String[] all = []
        Maincategorie.findAll().each { mainCat ->
            categorieService.getIterativeAllSubCats(mainCat).each { cat ->
                all += cat.name as String
            }
        }
        println('all: ' + all)
        [cats: all]
    }

    def exportDoc() {
        def docTitle = 'Cisco-Telefonie'
        if (params.docTitle) {
            docTitle = params.docTitle
        }
        render (documentService.exportDoc(docTitle, params.exportAs?params.exportAs:'map'))
    }

    //Einfach nur zum Funktionalitäts testen
    def testingThings() {
        println('\n\n####### Get all Subcats from one Maincategorie, not iterativ #######' )
        //Get one main categorie and all subcategories
        def temp = Maincategorie.findByName('os')
        println('Main: ' + temp.name)
        for (def sub in temp.subCats.findAll()) {
            println('Subs: ' + sub.name)
        }

        println('\n\n####### Get all Subcats from one Maincategorie...iterativ #######' )
        temp= categorieService.getIterativeAllSubCats(Maincategorie.findByName('os'))
        temp.each {
            println('Cat: ' + it.name + ' belongsTo ' + (it.parentCat? it.parentCat.name : it.mainCat?.name))
        }

        println('\n\n####### Get all docs from one Categorie #######' )
        temp = Subcategorie.findByName('win_7').docs.findAll()
        for (def doc in temp) {
            println(doc.title)
        }

        println('\n\n####### Get all docs from multiple Categories #######' )
        temp = Subcategorie.findByName('win_7').docs.findAll().toArray()
        println(temp)
        temp += Subcategorie.findByName('de').docs.findAll().toArray()
        println(temp)
        println('Vor ...Filterung...')
        for (def doc in temp) {
            println(doc.id + ' # ' +doc.title)
        }
        println('\nNach ...Filterung...')
        def matchItems = temp.findAll{temp.count(it) > 1}.unique()
        for (def doc in matchItems) {
            println(doc.id + ' # ' +doc.title)
        }

        println('\n\n####### Get all docs from multiple Categories via DocService method #######' )
        documentService.getAllDocsAssociatedToSubCategories(['win_7', 'student', 'null', 'article'] as String[]).each { doc ->
            println('Doc: ' + doc.title)
        }

        println('\n\n####### Get Subcategories from one specific document #######' )
        Subcategorie.findAllByDocs(Document.findByDocTitle('WLAN für Windows 7')).each { sub ->
            println('Sub: ' + sub.name)
        }

        println('\n\n####### Get Subcats (iterativ) + Maincats from one specific document #######' )
        Subcategorie.findAllByDocs(Document.findByDocTitle('WLAN für Windows 7')).each { sub ->
            println('Sub: ' + sub.name)
            def tempMain = sub.mainCat
            while (tempMain == null) {
                sub = sub.parentCat
                tempMain = sub.mainCat
                println('Parent: ' + sub.name)
            }
            println('MainCat: ' +tempMain.name + '\n#########\n')
        }

        println('\n\n####### Get count of all docs from given subCat with CatServer method #######' )
        println('Count: ' + categorieService.getDocCountOfSubCategorie('win_7'))

        println('\n\n####### Export Doc as JSON #######' )
        exportDoc()

        println('\n\n####### Render DOC as XML...First try #######' )
        def tryFirst = Document.findByDocTitle('Cisco-Telefonie')
        println(tryFirst.docContent)
        println(JSON.parse(tryFirst.docContent))


        render(view: 'index')
    }
}
