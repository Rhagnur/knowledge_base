/*
  Created by IntelliJ IDEA.
  User: didschu
 */
package berlin.htw.hrz.kb

import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import org.neo4j.graphdb.Result

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class KnowledgeBaseController {

    DocumentService documentService
    CategoryService categoryService
    InitService initService
    def springSecurityService

    // Start global exception handling
    def Exception(final Exception ex) {
        logException(ex)
        render (view: '/error', model: [exception : ex])
    }
    private void logException(final Exception ex) {
        log.error("Exception thrown: ${ex?.message}")
    }
    //End global exception handling

    def loadTestDocs () {
        return categoryService.getDocsOfInterest(springSecurityService.principal, request)
        //return null
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
            docsFound.addAll(Faq.findAllByQuestionIlike("%$params.searchBar%"))
            docsFound.addAll(Faq.findAllByAnswerIlike("%$params.searchBar%"))
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

        if (!(myDoc instanceof Faq)) {
            otherDocs = categoryService.getAdditionalDocs(myDoc)
        }

        author = Subcategory.findAllByMainCat(Maincategory.findByName('author')).find{it.docs.contains(myDoc)}?.name
        if (!author) {
            author = 'Kein Autor gefunden'
        }

        myDoc = documentService.increaseCounter(myDoc)
        stop = new Date()
        println('\nSeitenladezeit: '+TimeCategory.minus(stop, start))
        println(otherDocs)
        [document: myDoc, author: author, similarDocs: otherDocs, principal: springSecurityService.principal]
    }




    def showCat() {
        def myCats = Maincategory.findAll()
        //Default = hole alle Mainkategorien, ansonsten hole die Subkategorien der ausgewählten Kategorie
        if (params.cat) {
            myCats = categoryService.getAllSubCats(categoryService.getCategory(params.cat as String))
        }
        [cats: myCats, principal: springSecurityService.principal]
    }

    //todo: Secured wieder setzen, auch in index.gsp...zu Testzwecken erstmal deaktiviert
    //@Secured("hasAuthority('ROLE_GP-STAFF')") //Für optinale Erweiterung "Autoren" später Abfrage, ob User als Autor eingetragen ist
    def createDoc() {
        if (params.submit) {
            def docTitle, docContent, docSubs, docType
            String[] docTags
            def doc

            //Verarbeite Daten, welche alle Dokumente gemeinsam haben
            String tags = params.docTags
            docTags = tags.split(",")
            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            if (params.list('checkbox').empty) {
                flash.error = "Bitte mindestens eine dazugehörige Kategorie auswählen ausfüllen!"
            } else {
                String[] cats = new String[params.list('checkbox').size()];
                docSubs = params.list('checkbox').toArray(cats);
            }

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (params.tutorial == 'create') {
                def allAttrs = params.findAll{it.key =~ /step[A-Za-z]+_[1-9]/}

                if (allAttrs.containsValue('') || allAttrs.containsValue(null) || !params.docTitle || params.docTitle.empty) {
                    flash.error = "Bitte alle Felder ausfüllen!"
                    params.createTut = 'tutorial'
                } else {
                    def steps = []

                    //find all necessary steps data
                    def allTitles = allAttrs.findAll{it.key =~ /stepTitle_[1-9]/}
                    def allTexts = allAttrs.findAll{it.key =~ /stepText_[1-9]/}
                    def allLinks = allAttrs.findAll{it.key =~ /stepLink_[1-9]/}

                    //Verarbeite einzelne Steps
                    if (allTitles.size() == allTexts.size() && allTitles.size() == allLinks.size()) {
                        for (int i = 1; i <= allTitles.size(); i++) {
                            steps.add(new Step(number: i, stepTitle: allTitles.get(/stepTitle_/+i), stepText: allTexts.get(/stepText_/+i), mediaLink: allLinks.get(/stepLink_/+i) ))
                        }
                    } else {
                        flash.error = "Fehler beim Verarbeiten!"
                        params.createTut = 'tutorial'
                    }

                    doc = documentService.newTutorial(params.docTitle as String, steps as Step[], docTags)
                }
            }
            else if (params.faq == 'create') {
                println('Faq')
                if (params.question && !params.question.empty && params.answer && !params.answer.empty) {
                    println('create Faq')
                    doc = documentService.newFaq(params.question as String, params.answer as String, docTags).save()
                    println('Faq erstellt')

                } else {
                    flash.error = "Bitte alle Felder ausfüllen!"
                    params.createFaq = 'faq'
                }

            }
            //todo
            //else if (params.article == 'create') {

            //}


            if (!flash.error) {
                println(doc)
                categoryService.addDoc(doc, docSubs)
                flash.info = 'Dokument erstellt'
                redirect(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
            }
        }

        //todo: Anstatt eine gesamte 'Liste' lieber eine Hashmap mit Aufbaue [mainCatName1: [allSubcats], mainCatName2: [allSubcats],...]
        def all = [:]
        Maincategory.findAll().each { mainCat ->
            def temp = []
            categoryService.getIterativeAllSubCats(mainCat.name).each { cat ->
                temp.add(cat.name as String)
            }
            all.put(mainCat.name, temp.sort{ it })
        }
        println('all' + all)
        [cats: all, docType: params.createFaq?'faq':params.createTut?'tutorial':'', principal: springSecurityService.principal]
    }

    def exportDoc() {
        Document doc
        if (!params.docTitle && !params.exportAs) render("Error: Not enought arguments, 'docTitle' or 'exportAs' missing. Possible solutions for 'exportAs': 'json'/'xml'")
        doc = documentService.getDoc(params.docTitle)
        render (documentService.exportDoc(doc, params.exportAs as String))
    }
}
