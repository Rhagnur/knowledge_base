/*
  Created by IntelliJ IDEA.
  User: didschu
 */
package berlin.htw.hrz.kb

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured

/**
 * Controller class for handling the requests, redirects and processing data given from the views
 */
@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class KnowledgeBaseController {

    //todo: Rausfinden ob forward oder redirect
    //todo: Logik der Formularauswertung und grober Datenbearbeitung hier oder in Service???
    DocumentService documentService
    CategoryService categoryService
    InitService initService
    SpringSecurityService springSecurityService

    // Start global exception handling
    /**
     * Global exception handler method. Thanks to this methods you do not need a try/catch in every method which is using service methods that can throw exceptions
     * @param ex
     */
    void Exception(final Exception ex) {
        logException(ex)

        def temp = ""
        if (ex instanceof IllegalArgumentException) {
            temp = message(code:'kb.error.illegalArgument')
        } else if (ex instanceof NoSuchObjectFoundException) {
            temp = message(code:'kb.error.noSuchObject')
        } else if (ex instanceof ValidationErrorException) {
            temp = message(code:'kb.error.validationError')
        } else {
            temp = message(code:'kb.error.general')
        }

        temp += ex.message
        flash.error = temp
        forward(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
    }
    /**
     *
     * @param ex
     */
    private void logException(final Exception ex) {
        log.error("Exception thrown: ${ex?.message}")
    }
    //End global exception handling

    /**
     *
     * @return
     */
    def index() {
        //Falls keine Hauptkategorie angelegt ist, lege Struktur mit Testdokuemten an
        if (Category.findAll().empty) {
            initService.initTestModell()
            flash.info = message(code: 'kb.info.testStructureAndDataCreated') as String
        }
        println springSecurityService.principal

        [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal];
    }

    //Nicht schön, soll aber auch nur zu Demonstrationszwecken sein, wie eine spätere Suche + Anzeige + Filterung funktionen könnte
    //Ansonsten wäre es sinnvoller, die Logik in Service Methoden auszulagern
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

    def showDoc() {
        Document myDoc = null
        String author
        def otherDocs = [:]

        if (params.docTitle) {
            myDoc = documentService.getDoc(params.docTitle)
            if (myDoc) {
                myDoc = documentService.increaseCounter(myDoc)
                if (!(myDoc instanceof Faq)) {
                    otherDocs = categoryService.getAdditionalDocs(myDoc)
                }

                author = myDoc.linker.find{ it.subcat?.parentCat?.name == 'author' }?.subcat?.name
                if (!author) { author = 'Kein Autor gefunden' }
            }
            else { flash.error = message(code: 'kb.error.noSuchDocument') as String }
        }
        else { flash.error = message(code: 'kb.error.attrDocTitleNotFound') as String }

        if (flash.error) {
            forward(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
        } else {
            [document: myDoc, author: author, similarDocs: otherDocs, principal: springSecurityService.principal]
        }
    }

    def showCat() {
        def cat = null
        if (params.name) {
            cat = categoryService.getCategory(params.name)
        }
        [cat: cat, mainCats:(!cat)?categoryService.getAllMainCats():null, principal: springSecurityService.principal]
    }

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

    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    //todo: logik auslagern?
    def findUnlinkedSubCats() {
        def subCats = Subcategory.findAll()
        subCats.removeAll { it.parentCat != null }
        [subCats: subCats, principal: springSecurityService.principal]
    }

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

    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"]) //Für optionale Erweiterung "Autoren" später Abfrage, ob User als Autor eingetragen ist
    //todo: logik auslagern?
    //todo: autor = eingeloggter user, sprache = deutsch
    def createDoc() {
        println("params: ${params}")
        if (params.submit) {
            def docSubs = []
            String[] docTags
            Document doc = null

            //Verarbeite Daten, welche alle Dokumente gemeinsam haben
            //Hole und Verarbeite Tags
            if (params.docTags) {
                String tags = params.docTags
                docTags = tags.split(",")
            }
            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            if (params.list('checkbox').empty) {
                flash.error = message(code: 'kb.error.noSubCatGiven') as String
            } else {
                String[] cats = new String[params.list('checkbox').size()]
                docSubs.addAll(params.list('checkbox').toArray(cats))
                docSubs.add(springSecurityService.principal.username)
            }

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (!flash.error) {
                if (params.tutorial == 'create') {
                    //find all necessary steps data
                    def allTitles = params.findAll{it.key =~ /stepTitle_[0-9]+/}
                    def allTexts = params.findAll{it.key =~ /stepText_[0-9]+/}
                    def allLinks = params.findAll{it.key =~ /stepLink_[0-9]+/}

                    if (allTitles && allTitles.size() != allTexts?.size() || !params.docTitle) {
                        flash.error = message(code: 'kb.error.fillOutAllFields') as String
                        params.createTut = 'tutorial'
                    } else {
                        def steps = []
                        //Verarbeite einzelne Steps
                        if (allTitles.size() == allTexts.size()) {
                            for (int i = 1; i <= allTitles.size(); i++) {
                                steps.add(new Step(number: i, stepTitle: allTitles.get(/stepTitle_/+i), stepText: allTexts.get(/stepText_/+i), mediaLink: allLinks.get(/stepLink_/+i) ))
                            }
                        } else {
                            flash.error = message(code: 'kb.error.somethingWentWrong') as String
                            params.createTut = 'tutorial'
                        }

                        doc = documentService.newTutorial(params.docTitle as String, steps as Step[], docTags)
                    }
                }
                else if (params.faq == 'create') {
                    if (params.question && !params.question.empty && params.answer && !params.answer.empty) {
                        doc = documentService.newFaq(params.question as String, params.answer as String, docTags)
                    } else {
                        flash.error = message(code: 'kb.error.fillOutAllFields') as String
                        params.createFaq = 'faq'
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
        [cats: categoryService.getAllMaincatsWithSubcats([categoryService.getCategory('author')] as List), docType: params.createFaq?'faq':params.createTut?'tutorial':'', principal: springSecurityService.principal]
    }

    /**
     * Method for exporting a document and its content as JSON or XML
     * @return rendered JSON or XML text
     */
    def exportDoc() {
        if (!params.docTitle && !params.exportAs) { render("Error: Not enough arguments, 'docTitle' or 'exportAs' missing. Possible solutions for 'exportAs': 'json'/'xml'") }
        if (params.exportAs != 'json' && params.exportAs != 'xml') { render("Error: Wrong argument for 'exportAs', supported are 'exportAs=json' or 'exportAs=xml'") }

        render (text: documentService.exportDoc(documentService.getDoc(params.docTitle), params.exportAs as String), encoding: 'UTF-8', contentType: "application/${params.exportAs}")
    }
}