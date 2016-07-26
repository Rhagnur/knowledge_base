/*
  Created by IntelliJ IDEA.
  User: didschu
 */
package berlin.htw.hrz.kb

import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class KnowledgeBaseController {

    def documentService
    def categoryService
    def initService
    def springSecurityService

    def loadTestDocs () {
        return categoryService.getDocsOfInterest(springSecurityService.principal, request)
    }

    def index() {
        def start, stop, otherDocs = null
        start = new Date()
        if (Maincategory.findAll().empty) {
            initService.initTestModell()
            flash.info = "Neo4j war leer, Test-Domainklassen, Dokumente und Beziehungen angelegt"
        }
        println(request.getHeader('User-Agent'))

        otherDocs = loadTestDocs()
        stop = new Date()
        println('\nSeitenladezeit: '+TimeCategory.minus(stop, start))

        [otherDocs: otherDocs, principal: springSecurityService.principal];
    }

    def search () {
        def docsFound = []

        if (params.searchBar && params.searchBar.length() < 3) {
            flash.error = "Suchbegriff zu kurz, mindestens 3 Zeichen!"
            redirect(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
        }
        else if (params.searchBar && params.searchBar.length() >= 3) {
            docsFound.addAll(Document.findAllByDocTitleIlike("%$params.searchBar%"))
            docsFound.addAll(Step.findAllByStepTitleIlike("%$params.searchBar%").doc)
            docsFound.addAll(Step.findAllByStepTextIlike("%$params.searchBar%").doc)
            docsFound.addAll(Faq.findAllByQuestionIlike("%$params.searchBar%").doc)
            docsFound.addAll(Faq.findAllByAnswerIlike("%$params.searchBar%").doc)
            docsFound.sort { it.viewCount }.unique{ it.docTitle }

        } else {
            docsFound.addAll(Document.findAll().sort{it.steps})
        }

        println(docsFound)
        [foundDocs: docsFound ,principal: springSecurityService.principal, searchTerm: params.searchBar]
    }

    def showDoc() {
        def start, stop, author
        def otherDocs = [:]
        start = new Date()
        Document myDoc

        //Falls ein anderes Dokument angezeigt werden soll, überschreibe das Default-Test-Dokument
        if (params.docTitle) {
            myDoc = documentService.getDoc(params.docTitle)
        } else {
            myDoc = Document.findByDocTitle('Cisco-Telefonie')
        }
        if (!myDoc) {
            flash.error = "Kein Dokument gefunden"
            forward(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
        }

        otherDocs.tutorials = myDoc.steps?categoryService.getSimilarDocs(myDoc, 'tutorial'):null
        otherDocs.faq = myDoc.steps?categoryService.getSimilarDocs(myDoc, 'faq'):null

        author = Subcategory.findAllByMainCat(Maincategory.findByName('author')).find{it.docs.contains(myDoc)}?.name
        if (!author) {
            author = 'Kein Autor gefunden'
        }

        stop = new Date()
        println('\nSeitenladezeit: '+TimeCategory.minus(stop, start))
        [document: myDoc, author: author, similarDocs: otherDocs, principal: springSecurityService.principal]
    }




    def showCat() {
        def myCats = Maincategory.findAll()
        //Default = hole alle Mainkategorien, ansonsten hole die Subkategorien der ausgewählten Kategorie
        if (params.cat) {
            myCats = Subcategory.findByName(params.cat)?Subcategory.findByName(params.cat).subCats:Maincategory.findByName(params.cat)?Maincategory.findByName(params.cat).subCats:null
            if (!myCats) {flash.error = "No such categorie!!!"}
        }
        [cats: myCats, principal: springSecurityService.principal]
    }

    //todo: Secured wieder setzen, auch in index.gsp...zu Testzwecken erstmal deaktiviert
    //@Secured("hasAuthority('ROLE_GP-STAFF')") //Für optinale Erweiterung "Autoren" später Abfrage, ob User als Autor eingetragen ist
    def createDoc() {
        if (params.submit) {
            def docTitle, docContent, docSubs, docType
            String[] docTags


            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            if (params.list('checkbox').empty) {
                flash.error = "Bitte mindestens eine dazugehörige Kategorie auswählen ausfüllen!"
            } else {
                String[] cats = new String[params.list('checkbox').size()];
                docSubs = params.list('checkbox').toArray(cats);
            }

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (params.tutorial == 'create') {
                docType = 'tutorial'

                def allAttrs = params.findAll{it.key =~ /step[A-Za-z]+_[1-9]/}

                if (allAttrs.containsValue('') || allAttrs.containsValue(null) || !params.docTitle || params.docTitle.empty) {
                    flash.error = "Bitte alle Felder ausfüllen!"
                } else {
                    docTitle = params.docTitle
                    def allTitles = allAttrs.findAll{it.key =~ /stepTitle_[1-9]/}
                    def allTexts = allAttrs.findAll{it.key =~ /stepText_[1-9]/}
                    def allLinks = allAttrs.findAll{it.key =~ /stepLink_[1-9]/}

                    //Verarbeite einzelne Steps
                    if (allTitles.size() == allTexts.size() && allTitles.size() == allLinks.size()) {
                        def contentTemp = []
                        for (int i = 1; i <= allTitles.size(); i++) {
                            println('title: ' + allTitles.get(/stepTitle_/+i))
                            contentTemp += [number: i, title: allTitles.get(/stepTitle_/+i), text: allTexts.get(/stepText_/+i), link: allLinks.get(/stepLink_/+i)]
                        }
                        println (contentTemp)
                        docContent = contentTemp
                    } else {
                        flash.error = "Fehler beim Verarbeiten!"
                    }
                }
            }
            else if (params.faq == 'create') {
                docType = 'faq'

                if (params.question && !params.question.empty || params.answer &&!params.answer.empty) {
                    docTitle = params.question
                    docContent = [question: params.question, answer: params.answer]

                } else {
                    flash.error = "Bitte alle Felder ausfüllen!"
                }

            }

            //Verarbeite Daten, welche alle Dokumente gemeinsam haben
            String tags = params.docTags
            docTags = tags.split(",")

            int viewCount = 0
            if (params.viewCount && params.viewCount =~/[0-9]/) {
                viewCount = params.viewCount as int
            }

            if (!flash.error) {
                categoryService.addDoc(docTitle, docContent, docTags, docSubs, docType, viewCount)
                flash.info = 'Dokument erstellt'
                redirect(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
            }
        }

        //todo: Anstatt eine gesamte 'Liste' lieber eine Hashmap mit Aufbaue [mainCatName1: [allSubcats], mainCatName2: [allSubcats],...]
        String[] all = []
        Maincategory.findAll().each { mainCat ->
            categoryService.getIterativeAllSubCats(mainCat.name).each { cat ->
                all += cat.name as String
            }
        }
        println('all' + all)
        [cats: all, docType: params.createFaq?'faq':params.createTut?'tutorial':'', principal: springSecurityService.principal]
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
        //todo: eigentliche methode in categoryService
        /*println('\n\n####### Get all docs from multiple Categories via DocService method #######' )
        categoryService.getAllDocsAssociatedToSubCategories(['win_7', 'student', 'null', 'article'] as String[]).each { doc ->
            println('Doc: ' + doc.title)
        }

        println('\n\n####### Get Subcategories from one specific document #######' )
        Subcategory.findAllByDocs(Document.findByDocTitle('WLAN für Windows 7')).each { sub ->
            println('Sub: ' + sub.name)
        }

        println('\n\n####### Get Subcats (iterativ) + Maincats from one specific document #######' )
        Subcategory.findAllByDocs(Document.findByDocTitle('WLAN für Windows 7')).each { sub ->
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
        println('Count: ' + Subcategory.findByName('win_7').docs.size())

        println('\n\n####### Render DOC as JSON...First try #######' )
        println(documentService.exportDoc('Cisco-Telefonie', 'json'))

        println('\n\n####### Render DOC as XML...First try #######' )
        println(documentService.exportDoc('Cisco-Telefonie', 'xml'))

        println('\n\n####### Change Categorie name #######' )
        def error = categoryService.changeCategorieName('rack', 'torsten')
        println('Errorcode: ' + error + ' Name: ' + Subcategory.findByName('torsten')?.name)
        error = categoryService.changeCategorieName('torsten', 'rack')
        println('Errorcode: ' + error + ' Name: ' + Subcategory.findByName('rack')?.name)

        println('\n\n####### Change Subcat of Categorie name #######' )
        def oldSubs = ['TestOld1', 'TestOld2'] as String[]
        def newSubs = ['TestNew1', 'TestNew2'] as String[]
        error = categoryService.changeSubCatRelations('Test', newSubs)
        println('Error: ' + error)

        println('\n\n####### Delete Categorie #######' )
        println('Errorcode: '+ categoryService.deleteCategorie('TestMain'))
        println('Errorcode: '+ categoryService.deleteCategorie('TestSubSub2'))

        println('\n\n####### get associated SubCats of... #######' )
        println('null')
        categoryService.getAllSubCats(null).each {
            println((!(it instanceof Integer))?it.name:it)
        }

        println('\na')
        categoryService.getAllSubCats('a').each {
            println((!(it instanceof Integer))?it.name:it)
        }

        println('\nos')
        categoryService.getAllSubCats('os').each {
            println((!(it instanceof Integer))?it.name:it)
        }

        println('\nwindows')
        categoryService.getAllSubCats('windows').each {
            println((!(it instanceof Integer))?it.name:it)
        }*/
        flash.info = "Tests werden gerade in Testklassen ausgelagert, bitte haben Sie etwas Geduld!"


        redirect(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
    }
}
