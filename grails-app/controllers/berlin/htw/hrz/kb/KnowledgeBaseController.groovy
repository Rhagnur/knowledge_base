/*
  Created by IntelliJ IDEA.
  User: didschu
 */
package berlin.htw.hrz.kb

import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class KnowledgeBaseController {

    def documentService
    def categorieService
    def initService
    def springSecurityService

    def loadTestDocs () {
        //return [Linux: Subcategorie.findByName('linux').docs.findAll(), WLAN: Subcategorie.findByName('wlan').docs.findAll(), Student: Subcategorie.findByName('student').docs.findAll(), Deutsch: Subcategorie.findByName('de').docs.findAll()]
        return documentService.getDocsOfInterest(springSecurityService.principal)
    }

    def index() {
        if (Maincategorie.findAll().empty) {
            initService.initTestModell()
            flash.info = "Neo4j war leer, Test-Domainklassen, Dokumente und Beziehungen angelegt"
        }
        println(System.properties['os.name'] + " # " + System.properties['os.arch'] + " # " + System.properties['os.version'])
        [otherDocs: loadTestDocs(), principal: springSecurityService.principal];
    }

    def search () {
        println(params)
        flash.info = "Suche noch nicht implementiert!"
        render(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])

    }

    def showDoc() {
        println('params: ' + params)
        Document myDoc

        //Falls ein anderes Dokument angezeigt werden soll, überschreibe das Default-Test-Dokument
        if (params.docTitle) {
            myDoc = Document.findByDocTitle(params.docTitle)
        } else {
            myDoc = Document.findByDocTitle('Cisco-Telefonie')
        }
        if (!myDoc) {
            flash.error = "Kein Dokument gefunden"
            render(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
        }

        //Erhöhe den Viewcount um dadurch eine ungefähre Beliebtheit der Dokumente zu erzeugen
        println('\n\n############################################### Start '+myDoc.viewCount+ ' Start ###############################################')
        myDoc.viewCount = myDoc.viewCount + 1
        //todo: Update funktioniert nicht, zeigt zwar in der GSP den ViewCount +1 an, im Document bleibt er aber unverändert
        myDoc.save()
        println('############################################### Ende '+myDoc.viewCount+ ' Ende ###############################################\n\n')

        [document: myDoc, principal: springSecurityService.principal]
    }




    def showCat() {
        def myCats = Maincategorie.findAll()
        //Default = hole alle Mainkategorien, ansonsten hole die Subkategorien der ausgewählten Kategorie
        if (params.cat) {
            myCats = categorieService.getAllSubCats(Subcategorie.findByName(params.cat)?Subcategorie.findByName(params.cat):Maincategorie.findByName(params.cat)?Maincategorie.findByName(params.cat):null)
            if (!myCats) {flash.error = "No such categorie!!!"}
        }
        [cats: myCats, principal: springSecurityService.principal]
    }

    @Secured("hasAuthority('ROLE_GP-STAFF')") //Für optinale Erweiterung "Autoren" später Abfrage, ob User als Autor eingetragen ist
    def createDoc() {
        println(params)
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

            if (!flash.error) {
                documentService.addDoc(docTitle, docContent, docTags, docSubs, docType)
                flash.info = 'Dokument erstellt'
                render(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
            }
        }

        String[] all = []
        Maincategorie.findAll().each { mainCat ->
            categorieService.getIterativeAllSubCats(mainCat).each { cat ->
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

        println('\n\n####### Render DOC as JSON...First try #######' )
        println(documentService.exportDoc('Cisco-Telefonie', 'json'))

        println('\n\n####### Render DOC as XML...First try #######' )
        println(documentService.exportDoc('Cisco-Telefonie', 'xml'))


        render(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
    }
}
