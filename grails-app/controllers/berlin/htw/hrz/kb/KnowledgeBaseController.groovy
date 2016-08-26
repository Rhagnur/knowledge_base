/*
  Created by IntelliJ IDEA.
  User: didschu
 */
package berlin.htw.hrz.kb

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured

import javax.ws.rs.GET
import javax.ws.rs.Path

/**
 * Controller class for handling the requests, redirects and processing data given from the views
 */
@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class KnowledgeBaseController {

    //todo: Logik der Formularauswertung und grober Datenbearbeitung hier oder in Service???
    DocumentService documentService
    CategoryService categoryService
    InitService initService
    SpringSecurityService springSecurityService

    //###### Start global exception handling ######
    /**
     * Global exception handler method. Thanks to this methods you do not need a try/catch in every method which is using service methods that can throw exceptions
     * @param ex
     */
    void Exception(final Exception ex) {
        logException(ex)

        def temp = ""
        //Prüfe ob eine der benutzten Exceptions vorliegt. Wenn ja erzeuge spezifische Fehlernachricht, ansonsten eine allgemein
        if (ex instanceof IllegalArgumentException) {
            temp = message(code:'kb.error.illegalArgument')
        } else if (ex instanceof NoSuchObjectFoundException) {
            temp = message(code:'kb.error.noSuchObject')
        } else if (ex instanceof ValidationErrorException) {
            temp = message(code:'kb.error.validationError')
        } else {
            temp = message(code:'kb.error.general')
        }

        //Hänge die eigentliche Exception.message an die Fehlernachricht für mehr Klarheit.
        //Benutzer wird dann mit Fehlernachricht in der Flashvariablen auf Indexseite geschickt und diese ausgegeben
        temp += "\n${ex.message}"
        flash.error = temp
        redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
    }
    /**
     * Logs the exception message to the system log file
     * @param ex
     */
    private void logException(final Exception ex) {
        log.error("Exception thrown: ${ex?.message}\n")
        ex.printStackTrace()
    }
    //###### End global exception handling ######

    /**
     * Controller method for changing a category.
     * In the given states it is only allowed the change a subcategory.
     * @return
     */
    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def changeCat() {
        if (params.submit) {
            if (!params.catName) { flash.error = message(code: 'kb.error.attrNameCantBeNull') as String }
            else {
                Category myCat = categoryService.getCategory(params.name)

                if (myCat instanceof Subcategory){
                    if(params.parentCat && myCat.parentCat.name != params.parentCat) {
                        Category newParent = categoryService.getCategory(params.parentCat)
                        myCat = categoryService.changeParent(myCat, newParent)
                    }
                    if (params.catName && params.catName != myCat.name) {
                        myCat = categoryService.changeCategoryName(myCat, params.catName)
                    }

                    if (myCat) {
                        flash.info = message(code: 'kb.info.catChanged') as String
                    }
                    else { flash.error = message(code: 'kb.error.somethingWentWrong') as String }
                }
                else { flash.error = message(code: 'kb.error.cantDeleteOrChangeMainCat') as String }
                redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
            }
        }
        [cat: params.name?categoryService.getCategory(params.name):null, allCatsByMainCats: categoryService.getAllMaincatsWithSubcats(), principal: springSecurityService.principal]
    }

    /**
     * Controller method for creating a subcategory
     * @return
     */
    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def createCat() {
        //Falls createCat Form abgeschickt wurde, bearbeite Daten und erstelle Subkategorie
        if (params.submit) {
            if (!params.catName) { flash.error = message(code: 'kb.error.attrNameCantBeNull') as String }
            else {
                //Hole die Elternkategorie
                Category newParent = categoryService.getCategory(params.parentCat)

                //Erstelle neue Subkategorie
                if (categoryService.newSubCategory(params.catName as String, newParent)) {
                    flash.info = message(code: 'kb.info.catCreated') as String
                }
                else { flash.error = message(code: 'kb.error.somethingWentWrong') as String }

            }
            redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
        }
        [cat: params.name?categoryService.getCategory(params.name):null, allCatsByMainCats: categoryService.getAllMaincatsWithSubcats(), principal: springSecurityService.principal, origin: params.originName]
    }

    def createArticle() {
        println(params)
        if (params.submit) {
            def docSubs = []
            String[] docTags
            Article doc = null

            //Hole und Verarbeite Tags
            if (params.docTags) {
                //Hole dir den tag-input und entferne alle 'space' Elemente (Leerzeichen, Tab, \n etc)
                String tags = (params.docTags as String).replaceAll('\\s', '')
                docTags = tags.split(',')
            }

            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            def clickedSubcats = params.list('checkbox') as List
            if (!clickedSubcats) {
                flash.error = message(code: 'kb.error.noSubCatGiven') as String
            } else {
                //Füge den Autor (eingeloggter User) des Dokuments an
                docSubs.add(springSecurityService.principal.username)
                //Füge Sprache hinzu
                docSubs.add(params.language as String)
                String[] cats = new String[params.list('checkbox').size()]
                //Füge alle angeklickten Subkategorien an
                docSubs.addAll(params.list('checkbox').toArray(cats))
            }

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (!flash.error) {
                if (params.docTitle && !params.docTitle.empty && params.docContent && !params.docContent.empty) {
                    doc = documentService.newArticle(params.docTitle as String, params.docContent as String, docTags)
                } else {
                    flash.error = message(code: 'kb.error.fillOutAllFields') as String
                }

                if (!flash.error) {
                    categoryService.addDoc(doc, categoryService.getSubcategories(docSubs as String[]))
                    flash.info = message(code: 'kb.info.docCreated') as String
                    redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
                }
            }
        }
        [cats: categoryService.getAllMaincatsWithSubcats([categoryService.getCategory('author'), categoryService.getCategory('lang')] as List), lang:categoryService.getAllSubCats(categoryService.getCategory('lang')).sort{it.name}.name, principal: springSecurityService.principal]
    }

    def createFaq() {
        println(params)
        if (params.submit) {
            def docSubs = []
            String[] docTags
            Faq doc = null

            //Hole und Verarbeite Tags
            if (params.docTags) {
                //Hole dir den tag-input und entferne alle 'space' Elemente (Leerzeichen, Tab, \n etc)
                String tags = (params.docTags as String).replaceAll('\\s', '')
                docTags = tags.split(',')
            }

            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            def clickedSubcats = params.list('checkbox') as List
            if (!clickedSubcats) {
                flash.error = message(code: 'kb.error.noSubCatGiven') as String
            } else {
                //Füge den Autor (eingeloggter User) des Dokuments an
                docSubs.add(springSecurityService.principal.username)
                //Füge Sprache hinzu
                docSubs.add(params.language as String)
                String[] cats = new String[params.list('checkbox').size()]
                //Füge alle angeklickten Subkategorien an
                docSubs.addAll(params.list('checkbox').toArray(cats))
            }

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (!flash.error) {
                if (params.question && !params.question.empty && params.answer && !params.answer.empty) {
                    doc = documentService.newFaq(params.question as String, params.answer as String, docTags)
                } else {
                    flash.error = message(code: 'kb.error.fillOutAllFields') as String
                }

                if (!flash.error) {
                    categoryService.addDoc(doc, categoryService.getSubcategories(docSubs as String[]))
                    flash.info = message(code: 'kb.info.docCreated') as String
                    redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
                }
            }
        }
        [cats: categoryService.getAllMaincatsWithSubcats([categoryService.getCategory('author'), categoryService.getCategory('lang')] as List), lang:categoryService.getAllSubCats(categoryService.getCategory('lang')).sort{it.name}.name, principal: springSecurityService.principal]
    }

    /**
     * Controller method for creating a new tutorial
     */
    def createTutorial() {
        println(params)
        if (params.submit) {
            def docSubs = []
            String[] docTags
            Tutorial doc = null
            boolean stepsError = false

            //Hole und Verarbeite Tags
            if (params.docTags) {
                //Hole dir den tag-input und entferne alle 'space' Elemente (Leerzeichen, Tab, \n etc)
                String tags = (params.docTags as String).replaceAll('\\s','')
                docTags = tags.split(',')
            }
            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            def clickedSubcats = params.list('checkbox') as List
            if (!clickedSubcats) {
                flash.error = message(code: 'kb.error.noSubCatGiven') as String
            } else {
                //Füge den Autor (eingeloggter User) des Dokuments an
                docSubs.add(springSecurityService.principal.username)
                //Füge Sprache hinzu
                docSubs.add(params.language as String)
                String[] cats = new String[params.list('checkbox').size()]
                //Füge alle angeklickten Subkategorien an
                docSubs.addAll(params.list('checkbox').toArray(cats))
            }
            println('docSubs: ' +docSubs)

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (!flash.error) {

                //Finde alle Daten für die einzelnen Schritte
                def allTitles = params.findAll { it.key =~ /stepTitle_[0-9]+/ && it.value } as Map
                def allTexts = params.findAll { it.key =~ /stepText_[0-9]+/  && it.value } as Map
                def allLinks = params.findAll { it.key =~ /stepLink_[0-9]+/ }
                println('titles: '+allTitles)
                println('texts: '+allTexts)

                //Prüfe, ob es für jede StepTitelNummer auch eine StepTextNummer gibt, sprich ob jeder Step einen Titel und einen Text hat
                for (String stepTitle in allTitles.keySet()) {
                    if (!allTexts.find { it.key =~ /stepText_${stepTitle.substring(stepTitle.indexOf('_') + 1)}/}) {
                        stepsError = true
                    }
                }

                if (stepsError || !params.docTitle) {
                    flash.error = message(code: 'kb.error.fillOutAllFields') as String
                    params.createDoc = 'tutorial'
                } else {
                    def steps = []
                    //Verarbeite einzelne Steps
                    for (int i = 1; i <= allTitles.size(); i++) {
                            steps.add(new Step(number: i, stepTitle: allTitles.get(/stepTitle_/ + i), stepText: allTexts.get(/stepText_/ + i), mediaLink: allLinks.get(/stepLink_/ + i)))
                    }

                    doc = documentService.newTutorial(params.docTitle as String, steps as Step[], docTags)
                }
                if (!flash.error) {
                    categoryService.addDoc(doc, categoryService.getSubcategories(docSubs as String[]))
                    flash.info = message(code: 'kb.info.docCreated') as String

                    redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
                }
            }
        }
        [cats: categoryService.getAllMaincatsWithSubcats([categoryService.getCategory('author'), categoryService.getCategory('lang')] as List), lang:categoryService.getAllSubCats(categoryService.getCategory('lang')).sort{it.name}.name, principal: springSecurityService.principal]
    }

    /**
     * Controller method for creating a new document
     * @return
     */
    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"]) //Für optionale Erweiterung "Autoren" später Abfrage, ob User als Autor eingetragen ist
    //todo: logik auslagern?
    //todo: Anstatt createDoc lieber createFaq, createTutorial, createArticle...macht Abfragen und Views einfacher
    def createDoc() {
        println("params: ${params}")
        if (params.submit) {
            def docSubs = []
            String[] docTags
            Document doc = null

            //Verarbeite Daten, welche alle Dokumente gemeinsam haben
            //Hole und Verarbeite Tags
            if (params.docTags) {
                //Hole dir den tag-input und entferne alle 'space' Elemente (Leerzeichen, Tab, \n etc)
                String tags = (params.docTags as String).replaceAll('\\s','')
                docTags = tags.split(',')
            }
            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            def clickedSubcats = params.list('checkbox') as List
            if (!clickedSubcats) {
                flash.error = message(code: 'kb.error.noSubCatGiven') as String
            } else if (!clickedSubcats.any { categoryService.getCategory('lang').subCats.name.toList().contains( it ) }) {
                flash.error = message(code: 'kb.error.noLanguageChosen') as String
            } else {
                String[] cats = new String[params.list('checkbox').size()]
                //Füge alle angeklickten Subkategorien an
                docSubs.addAll(params.list('checkbox').toArray(cats))
                //Füge den Autor (eingeloggter User) des Dokuments an
                docSubs.add(springSecurityService.principal.username)
            }

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (!flash.error) {
                if (params.tutorial == 'create') {
                    boolean stepsError = false
                    //Finde alle Daten für die einzelnen Schritte
                    def allTitles = params.findAll { it.key =~ /stepTitle_[0-9]+/ && it.value } as Map
                    def allTexts = params.findAll { it.key =~ /stepText_[0-9]+/  && it.value } as Map
                    def allLinks = params.findAll { it.key =~ /stepLink_[0-9]+/ }

                    //Prüfe, ob es für jede StepTitelNummer auch eine StepTextNummer gibt, sprich ob jeder Step einen Titel und einen Text hat
                    for (String stepTitle in allTitles.keySet()) {
                        if (!allTexts.find { it.key =~ /stepText_${stepTitle.substring(stepTitle.indexOf('_') + 1)}/}) {
                            stepsError = true
                        }
                    }

                    //Prüfe ob die Anzahl der Titel-Daten der Anzahl der Text-Daten entspricht und ob ein Dokumententitel gesetzt wurde
                    //todo: Prüfe ob NICHT nur die Anzahl der Daten gleich ist, sondern ob es zu jeder TitelNummer auch eine TextNummer gibt
                    if (stepsError || !params.docTitle) {
                        flash.error = message(code: 'kb.error.fillOutAllFields') as String
                        params.createDoc = 'tutorial'
                    } else {
                        def steps = []
                        //Verarbeite einzelne Steps
                        if (allTitles.size() == allTexts.size()) {
                            for (int i = 1; i <= allTitles.size(); i++) {
                                steps.add(new Step(number: i, stepTitle: allTitles.get(/stepTitle_/+i), stepText: allTexts.get(/stepText_/+i), mediaLink: allLinks.get(/stepLink_/+i) ))
                            }
                        } else {
                            flash.error = message(code: 'kb.error.somethingWentWrong') as String
                            params.createDoc = 'tutorial'
                        }

                        doc = documentService.newTutorial(params.docTitle as String, steps as Step[], docTags)
                    }
                }
                else if (params.faq == 'create') {
                    if (params.question && !params.question.empty && params.answer && !params.answer.empty) {
                        doc = documentService.newFaq(params.question as String, params.answer as String, docTags)
                    } else {
                        flash.error = message(code: 'kb.error.fillOutAllFields') as String
                        params.createDoc = 'faq'
                    }

                }
                //todo
                //else if (params.article == 'create') {

                //}

                if (!flash.error) {
                    categoryService.addDoc(doc, categoryService.getSubcategories(docSubs as String[]))
                    flash.info = message(code: 'kb.info.docCreated') as String
                    redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
                }
            }
        }

        if (flash.error) {
            if (params.tutorial == 'create') { params.createDoc = 'tutorial' }
            else if (params.faq == 'create') { params.createDoc = 'faq' }
            else if (params.article == 'create') { params.createDoc = 'article' }
        }
        [cats: categoryService.getAllMaincatsWithSubcats([categoryService.getCategory('author')] as List), docType: params.createFaq?'faq':params.createTut?'tutorial':'', principal: springSecurityService.principal]
    }

    /**
     * Controller method for deleting an existing subcategory
     * It is not possible to delete a 'main'-category!
     * @return
     */
    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def deleteCat() {
        if (params.name) {
            Category cat = categoryService.getCategory(params.name)
            if (cat) {
                categoryService.deleteSubCategory(cat)
                flash.info = message(code: 'kb.info.catDeleted') as String
            }
            else { flash.error = message(code: 'kb.error.noSuchCategorie') as String }
        }
        else { flash.error = message(code: 'kb.error.attrNameNotFound') as String }
        redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
    }

    /**
     * Controller method for deleting a document
     * @return
     */
    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def deleteDoc() {
        if (params.docTitle) {
            Document doc = documentService.getDoc(params.docTitle)
            if (doc) {
                documentService.deleteDoc(doc)
                flash.info = "Dokument '${params.docTitle}' wurde gelöscht"
            }
            else { flash.error = message(code: 'kb.error.noSuchDocument') as String }
        }
        else { flash.error = message(code: 'kb.error.attrDocTitleNotFound') as String }
        redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
    }

    /**
     * Controller method for either exporting a single document or a list of all unlocked documents
     * Usage: use '/document(.:format)' for getting the list or '/document/:docTitle(.:format)' for getting a single document
     * if you don't use the format parameter this method will look for the accepted formats in the accept-header
     * @return object can be either a json or xml object
     */
    def exportDoc() {
        println(params)
        String format = ''
        if (!params.format) {
            String[] acceptFormats = request.getHeader('Accept').toString().split(',')
            for (String acceptFormat in acceptFormats) {
                if (acceptFormat.contains('xml')) {
                    format = 'xml'
                    break
                }
                else if (acceptFormat.contains('json')) {
                    format = 'json'
                    break
                }
            }
        }
        else if (params.format != 'json' && params.format != 'xml') {
            response.status = 501
            render([error: "Given format is unrecognized for this method. Please use 'json' or 'xml'!"] as JSON)
        }
        else { format = params.format }

        render (text: documentService.exportDoc(format, params.docTitle?documentService.getDoc(params.docTitle):null), encoding: 'UTF-8', contentType: "application/${format}")
    }

    /**
     * Controller method for finding unlinked elements (subcategories and documents)
     * @return
     */
    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def findUnlinkedObjs() {
        [subCats: categoryService.findUnlinkedSubcats(), docs:documentService.findUnlinkedDocs(), principal: springSecurityService.principal]
    }

    /**
     * Controller method for the index (main) page
     * @return
     */
    def index() {
        //Falls keine Hauptkategorie angelegt ist, lege Struktur mit Testdokumente an
        if (Category.findAll().empty) {
            initService.initTestModell()
            flash.info = message(code: 'kb.info.testStructureAndDataCreated') as String
        }
/*        Document doc = Document.findByDocTitle('Testartikel5')
        doc.linker.collect().each { Linker linker ->
            Linker.unlink(linker.subcat, linker.doc)
        }
        doc = doc.save(flush:true)
        doc.linker.each {
            println('Noch da')
        }*/

        println(documentService.findUnlinkedDocs())
        println(categoryService.findUnlinkedSubcats())

        [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal];
    }

    /**
     * Controller method for navigation through the categories or displaying a single one
     * Usage: use without parameter to get a list of all 'main'-categories or use it with '?name=(subcat.name)' to get view of the details of a single subcategory
     * @return
     */
    def showCat() {
        def cat = null
        if (params.name) {
            cat = categoryService.getCategory(params.name)
        }
        //Falls ein Name einer Kategorie angegeben wurde, wird diese an die View zurück gegeben, ansonsten werden alle Mainkategorien (Categories) ausgegeben
        [cat: cat, mainCats:(!cat)?categoryService.getAllMainCats():null, principal: springSecurityService.principal]
    }

    /**
     * Controller method for showing a single document
     * @return
     */
    def showDoc() {
        Document myDoc = null
        def otherDocs = [:]

        if (params.docTitle) {
            myDoc = documentService.getDoc(params.docTitle)
            if (myDoc) {
                //Erhöhe Viewcount
                myDoc = documentService.increaseCounter(myDoc)
                if (!(myDoc instanceof Faq)) {
                    //Falls Dokument nicht vom Typ Faq ist, hole verwandte Dokumente bzw andere interessante Dokumente
                    otherDocs = categoryService.getAdditionalDocs(myDoc)
                }
            }
            else { flash.error = message(code: 'kb.error.noSuchDocument') as String }
        }
        else { flash.error = message(code: 'kb.error.attrDocTitleNotFound') as String }

        if (flash.error) {
            redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
        } else {
            [document: myDoc, author: documentService.getAuthor(myDoc), lang: documentService.getLanguage(myDoc), similarDocs: otherDocs, principal: springSecurityService.principal]
        }
    }

    //###### Anfang der Debugmethoden, welche nicht direkt wichtig für die eigentlichen Funktionalitäten der Anwendung sind ######

    //Nicht schön, soll aber auch nur zu Demonstrationszwecken sein, wie eine spätere Suche + Anzeige + Filterung funktionen könnte
    //Ansonsten wäre es sinnvoller, die Logik in Service Methoden auszulagern
    /**
     * Controller method for searching through documents
     * Attention: Only for debug and testing purpose, not optimized!
     * @return
     */
    def search () {
        def docsFound = []
        def filter = []

        if (params.searchBar && params.searchBar.length() < 3) {
            flash.error = message(code: 'kb.error.searchTermTooShort') as String
            redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
        }
        else if (params.searchBar && params.searchBar.length() >= 3) {
            //Suche über die Eigenschaften der einzelnen Dokumente
            //Dieser Entwurf ist recht langsam, wird aber später, durch die Volltextsuche der HTW Berlin ersetzt
            docsFound.addAll(Document.findAllByDocTitleIlike("%$params.searchBar%"))
            docsFound.addAll(Step.findAllByStepTitleIlike("%$params.searchBar%").doc)
            docsFound.addAll(Step.findAllByStepTextIlike("%$params.searchBar%").doc)
            docsFound.addAll(Faq.findAllByQuestionIlike("%$params.searchBar%"))
            docsFound.addAll(Faq.findAllByAnswerIlike("%$params.searchBar%"))
            docsFound.addAll(Article.findAllByDocContentIlike("%$params.searchBar%"))
            docsFound.sort { it.viewCount }.unique{ it.docTitle }
        }
        else { docsFound.addAll(Document.findAll().sort{it.steps}) }

        //Filtere die Ergebnisse
        if (!params.list('checkbox').empty) {
            def tempDocs = []

            String[] cats = new String[params.list('checkbox').size()];
            def temp = params.list('checkbox').toArray(cats) as String[]

            temp.each { catName ->
                filter.add(catName)
                tempDocs += docsFound.findAll { it.linker?.subcat?.name?.contains(catName) }
            }

            docsFound.removeAll { it == null }
            docsFound = tempDocs.findAll { tempDocs.count(it) == (temp.size()) }.unique().sort { it.viewCount }
        }
        [searchBar: params.searchBar ,foundDocs: docsFound ,principal: springSecurityService.principal, allCatsByMainCats: categoryService.getAllMaincatsWithSubcats(), filter: filter]
    }
}