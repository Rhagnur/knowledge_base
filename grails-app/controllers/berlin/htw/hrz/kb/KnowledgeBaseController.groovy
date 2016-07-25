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
    def categorieService
    def initService
    def springSecurityService

    def loadTestDocs () {
        return documentService.getDocsOfInterest(springSecurityService.principal, request)
    }

    def index() {
        def start, stop, otherDocs
        start = new Date()
        if (Maincategorie.findAll().empty) {
            initService.initTestModell()
            flash.info = "Neo4j war leer, Test-Domainklassen, Dokumente und Beziehungen angelegt"
        }
        println(request.getHeader('User-Agent'))

        otherDocs = loadTestDocs()
        stop = new Date()
        println('\nSeitenladezeit: '+TimeCategory.minus(stop, start))
        println(Document)
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

        otherDocs.tutorials = myDoc.steps?documentService.getSimilarDocs(myDoc, 'tutorial'):null
        otherDocs.faq = myDoc.steps?documentService.getSimilarDocs(myDoc, 'faq'):null

        author = Subcategorie.findAllByMainCat(Maincategorie.findByName('author')).find{it.docs.contains(myDoc)}?.name
        if (!author) {
            author = 'Kein Autor gefunden'
        }

        stop = new Date()
        println('\nSeitenladezeit: '+TimeCategory.minus(stop, start))
        [document: myDoc, author: author, similarDocs: otherDocs, principal: springSecurityService.principal]
    }




    def showCat() {
        def myCats = Maincategorie.findAll()
        //Default = hole alle Mainkategorien, ansonsten hole die Subkategorien der ausgewählten Kategorie
        if (params.cat) {
            myCats = Subcategorie.findByName(params.cat)?Subcategorie.findByName(params.cat).subCats:Maincategorie.findByName(params.cat)?Maincategorie.findByName(params.cat).subCats:null
            if (!myCats) {flash.error = "No such categorie!!!"}
        }
        [cats: myCats, principal: springSecurityService.principal]
    }

    @Secured("hasAuthority('ROLE_GP-STAFF')") //Für optinale Erweiterung "Autoren" später Abfrage, ob User als Autor eingetragen ist
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
                documentService.addDoc(docTitle, docContent, docTags, docSubs, docType, viewCount)
                flash.info = 'Dokument erstellt'
                redirect(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
            }
        }

        String[] all = []
        Maincategorie.findAll().each { mainCat ->
            categorieService.getIterativeAllSubCats(mainCat.name).each { cat ->
                all += cat.name as String
            }
        }
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
        println('\n\n####### Get all Subcats from one Maincategorie, not iterativ #######' )
        //Get one main categorie and all subcategories
        def temp = Maincategorie.findByName('os')
        println('Main: ' + temp.name)
        for (def sub in temp.subCats.findAll()) {
            println('Subs: ' + sub.name)
        }

        println('\n\n####### Get all Subcats from one Maincategorie...iterativ #######' )
        temp= categorieService.getIterativeAllSubCats('os')
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
        println('Count: ' + Subcategorie.findByName('win_7').docs.size())

        println('\n\n####### Render DOC as JSON...First try #######' )
        println(documentService.exportDoc('Cisco-Telefonie', 'json'))

        println('\n\n####### Render DOC as XML...First try #######' )
        println(documentService.exportDoc('Cisco-Telefonie', 'xml'))

        println('\n\n####### Change Categorie name #######' )
        def error = categorieService.changeCategorieName('rack', 'torsten')
        println('Errorcode: ' + error + ' Name: ' + Subcategorie.findByName('torsten')?.name)
        error = categorieService.changeCategorieName('torsten', 'rack')
        println('Errorcode: ' + error + ' Name: ' + Subcategorie.findByName('rack')?.name)

        println('\n\n####### Change Subcat of Categorie name #######' )
        def oldSubs = ['TestOld1', 'TestOld2'] as String[]
        def newSubs = ['TestNew1', 'TestNew2'] as String[]
        error = categorieService.changeSubcatsRelation('Test', newSubs)
        println('Error: ' + error)
        println(error.subCats?.size())

        println('\n\n####### Delete Categorie #######' )
        println('Errorcode: '+categorieService.deleteCategorie('TestMain'))
        println('Errorcode: '+categorieService.deleteCategorie('TestSubSub2'))


        redirect(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
    }
}
