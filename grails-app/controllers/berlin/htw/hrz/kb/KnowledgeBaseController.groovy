/*
  Created by IntelliJ IDEA.
  User: didschu
 */
package berlin.htw.hrz.kb

import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory


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
        //todo vll lieber weiterleitung auf index, damit user weiterarbeiten kann
        //redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
    }
    private void logException(final Exception ex) {
        log.error("Exception thrown: ${ex?.message}")
    }
    //End global exception handling


    def index() {
        if (Category.findAll().empty) {
            initService.initTestModell()
            flash.info = message(code: 'kb.info.testStructureAndDataCreated') as String
        }

        [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal];
    }

    //Nicht schön, soll aber auch nur zu Demonstrationszwecken sein, wie eine spätere Suche + Anzeige + Filterung funktionen könnte
    def search () {
        def docsFound = []
        def filter = []

        if (params.searchBar && params.searchBar.length() < 3) {
            flash.error = message(code: 'kb.error.searchTermTooShort') as String
            redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
        }
        else if (params.searchBar && params.searchBar.length() >= 3) {
            docsFound.addAll(Document.findAllByDocTitleIlike("%$params.searchBar%"))
            docsFound.addAll(Step.findAllByStepTitleIlike("%$params.searchBar%").doc)
            docsFound.addAll(Step.findAllByStepTextIlike("%$params.searchBar%").doc)
            docsFound.addAll(Faq.findAllByQuestionIlike("%$params.searchBar%"))
            docsFound.addAll(Faq.findAllByAnswerIlike("%$params.searchBar%"))
            docsFound.addAll(Article.findAllByDocContentIlike("%$params.searchBar%"))
            docsFound.sort { it.viewCount }.unique{ it.docTitle }
        } else {
            docsFound.addAll(Document.findAll().sort{it.steps})
        }

        //Filtere die Ergebnisse
        if (params.list('checkbox').size() > 0) {
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


        def all = [:]
        categoryService.getAllMainCats().each { mainCat ->
            def temp = []
            categoryService.getIterativeAllSubCats(mainCat.name).each { cat ->
                temp.add(cat.name as String)
            }
            all.put(mainCat.name, temp.sort{ it })
        }
        [searchBar: params.searchBar ,foundDocs: docsFound ,principal: springSecurityService.principal, allCatsByMainCats: all, filter: filter]
    }

    def deleteDoc() {
        if (params.docTitle) {
            Document doc = documentService.getDoc(params.docTitle)
            if (doc) {
                documentService.deleteDoc(doc)
                flash.info = "Dokument '${params.docTitle}' wurde gelöscht"
            } else {
                flash.error = message(code: 'kb.error.noSuchDocument') as String
            }


            redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
        } else {
            flash.error = message(code: 'kb.error.attrDocTitleNotFound') as String
        }

    }

    def showDoc() {
        String author
        def otherDocs = [:]
        Document myDoc = null

        //Falls ein anderes Dokument angezeigt werden soll, überschreibe das Default-Test-Dokument
        if (params.docTitle) {
            myDoc = documentService.getDoc(params.docTitle)
        }

        if (!myDoc) {
            flash.error = message(code: 'kb.error.noSuchDocument') as String
            forward(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
        }

        if (!(myDoc instanceof Faq)) {
            otherDocs = categoryService.getAdditionalDocs(myDoc)
        }

        //author = Subcategory.findAllByParentCat(Category.findByName('author')).find{it.docs.contains(myDoc)}?.name
        author = myDoc.linker.find{ it.subcat?.parentCat?.name == 'author' }?.subcat?.name
        if (!author) {
            author = 'Kein Autor gefunden'
        }

        myDoc = documentService.increaseCounter(myDoc)
        [document: myDoc, author: author, similarDocs: otherDocs, principal: springSecurityService.principal]
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
            try {
                Category cat = categoryService.getCategory(params.name)
                if (cat instanceof Subcategory) {
                    categoryService.deleteSubCategory(cat)
                    flash.info = message(code: 'kb.info.catDeleted') as String
                } else {
                    flash.error = message(code: 'kb.error.cantDeleteMainCat') as String
                }
            } catch (Exception e) {
                flash.error = message(code: 'kb.error.somethingWentWrong') as String
                e.printStackTrace()
            }
        }
        else {
            flash.error = message(code: 'kb.error.attrNameNotFound') as String
        }
        redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
    }

    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def createCat() {
        if (params.submit) {
            try {
                if (!params.catName || params.catName == '') flash.error = message(code: 'kb.error.attrNameCantBeNull') as String


                if (!(flash.error)) {
                    def docSubs = []

                    //Get subcats
                    if (params.list('checkbox').size() > 0) {
                        String[] cats = new String[params.list('checkbox').size()];
                        def temp = params.list('checkbox').toArray(cats) as String[]
                        temp.each {
                            docSubs.add(categoryService.getCategory(it))
                        }
                    }

                    //get parentCat
                    Category newParent = categoryService.getCategory(params.parentCat)

                    //create new subcat
                    if (categoryService.newSubCategory(params.catName as String, newParent, docSubs as Subcategory[])) {
                        flash.info = message(code: 'kb.info.catCreated') as String
                        redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
                    } else {
                        flash.error = message(code: 'kb.error.somethingWentWrong') as String
                    }

                }
            } catch (Exception e) {
                log.error(e.printStackTrace())
                flash.error = message (code: 'kb.error.somethingWentWrong') as String
            } finally {
                redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
            }

        }
        def all = [:]
        categoryService.getAllMainCats().each { mainCat ->
            def temp = []
            categoryService.getIterativeAllSubCats(mainCat.name).each { cat ->
                temp.add(cat.name as String)
            }
            all.put(mainCat.name, temp.sort{ it })
        }
        [cat: params.name?categoryService.getCategory(params.name):null, allCatsByMainCats: all, principal: springSecurityService.principal]
    }

    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def findUnlinkedSubCats() {
        def subCats = Subcategory.findAll()
        subCats.removeAll { it.parentCat != null }
        [subCats: subCats, principal: springSecurityService.principal]
    }

    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def changeCat() {
        if (!params.submit) {
            def all = [:]
            categoryService.getAllMainCats().each { mainCat ->
                def temp = []
                categoryService.getIterativeAllSubCats(mainCat.name).each { cat ->
                    temp.add(cat.name as String)
                }
                all.put(mainCat.name, temp.sort{ it })
            }
            [cat: params.name?categoryService.getCategory(params.name):null, allCatsByMainCats: all, principal: springSecurityService.principal]
        } else {
            if (!params.catName || params.catName == '') flash.error = message(code: 'kb.error.attrNameCantBeNull') as String

            if (!(flash.error)) {
                String[] cats = new String[params.list('checkbox').size()];
                def myCat = categoryService.getCategory(params.name)

                if (myCat instanceof Subcategory){

                    if(params.parentCat && myCat.parentCat.name != params.parentCat) {
                        Category newParent = categoryService.getCategory(params.parentCat)
                        myCat = categoryService.changeParent(myCat, newParent)
                    }

                    if (cats) {
                        def docSubs = params.list('checkbox').toArray(cats)
                        myCat = categoryService.changeSubCats(myCat, docSubs as String[])
                    }

                    if (params.catName && params.catName != myCat.name) {
                        myCat = categoryService.changeCategoryName(myCat, params.catName)
                    }


                    if (myCat) {
                        flash.info = message(code: 'kb.info.catChanged') as String
                    } else {
                        flash.error = message(code: 'kb.error.somethingWentWrong') as String
                    }

                }  else {
                    flash.error = message(code: 'kb.error.cantDeleteMainCat') as String
                }

                redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
            }
        }

    }

    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"]) //Für optinale Erweiterung "Autoren" später Abfrage, ob User als Autor eingetragen ist
    def createDoc() {
        if (params.submit) {
            try {
                def docSubs = null
                String[] docTags
                Document doc = null

                //Verarbeite Daten, welche alle Dokumente gemeinsam haben
                String tags = params.docTags
                docTags = tags.split(",")
                //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
                if (params.list('checkbox').empty) {
                    flash.error = message(code: 'kb.error.noSubCatGiven') as String
                } else {
                    String[] cats = new String[params.list('checkbox').size()];
                    docSubs = params.list('checkbox').toArray(cats);
                }

                //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
                if (params.tutorial == 'create') {
                    def allAttrs = params.findAll{it.key =~ /step[A-Za-z]+_[1-9]/}

                    if (allAttrs.containsValue('') || allAttrs.containsValue(null) || !params.docTitle || params.docTitle.empty) {
                        flash.error = message(code: 'kb.error.fillOutAllFields') as String
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
                            flash.error = message(code: 'kb.error.somethingWentWrong') as String
                            params.createTut = 'tutorial'
                        }

                        doc = documentService.newTutorial(params.docTitle as String, steps as Step[], docTags)
                    }
                }
                else if (params.faq == 'create') {
                    if (params.question && !params.question.empty && params.answer && !params.answer.empty) {
                        doc = documentService.newFaq(params.question as String, params.answer as String, docTags).save()
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
                redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
            }
            catch (Exception e) {
                log.error(e.printStackTrace())
                flash.error = message (code: 'kb.error.somethingWentWrong') as String
            }
            finally {
                redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
            }
        }

        def all = [:]
        categoryService.getAllMainCats().each { mainCat ->
            def temp = []
            categoryService.getIterativeAllSubCats(mainCat.name).each { cat ->
                temp.add(cat.name as String)
            }
            all.put(mainCat.name, temp.sort{ it })
        }
        [cats: all, docType: params.createFaq?'faq':params.createTut?'tutorial':'', principal: springSecurityService.principal]
    }

    /**
     * Method for exporting a document and its content as JSON or XML
     * @return rendered JSON or XML text
     */
    def exportDoc() {
        if (!params.docTitle && !params.exportAs) render("Error: Not enough arguments, 'docTitle' or 'exportAs' missing. Possible solutions for 'exportAs': 'json'/'xml'")
        if (params.exportAs != 'json' && params.exportAs != 'xml') render("Error: Wrong argument for 'exportAs', supported are 'exportAs=json' or 'exportAs=xml'")

        render (text: documentService.exportDoc(documentService.getDoc(params.docTitle), params.exportAs as String), encoding: 'UTF-8', contentType: "text/${params.exportAs}")
    }
}