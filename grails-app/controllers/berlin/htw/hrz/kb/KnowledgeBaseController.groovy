/*
  Created by IntelliJ IDEA.
  User: didschu
 */
package berlin.htw.hrz.kb

import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory


// TODO [TR]: Etliche "gefährliche" Methoden sind ja gar nicht abgesichert! Beispiel: deleteCat()
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


    def index() {
        def start, stop
        start = new Date()
        if (Category.findAll().empty) {
            initService.initTestModell()
            flash.info = message(code: 'kb.info.testStructureAndDataCreated') as String
        }

        println(request.getHeader('User-Agent'))

        stop = new Date()
        println('\nSeitenladezeit: '+TimeCategory.minus(stop, start))

        [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal];
    }

    //Nicht schön, soll aber auch nur zu Demonstrationszwecken sein, wie eine spätere Suche + Anzeige + Filterung funktionen könnte
    def search () {
        println(params)
        def docsFound = []
        def filter = []

        if (params.searchBar && params.searchBar.length() < 3) {
            flash.error = message(code: 'kb.error.searchTermTooShort') as String
            redirect(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
        }
        else if (params.searchBar && params.searchBar.length() >= 3) {
            println('bla')
            // TODO [TR]: ILIKE geht auch mit Neo4J ?
            docsFound.addAll(Document.findAllByDocTitleIlike("%$params.searchBar%"))
            docsFound.addAll(Step.findAllByStepTitleIlike("%$params.searchBar%").doc)
            docsFound.addAll(Step.findAllByStepTextIlike("%$params.searchBar%").doc)
            docsFound.addAll(Faq.findAllByQuestionIlike("%$params.searchBar%"))
            docsFound.addAll(Faq.findAllByAnswerIlike("%$params.searchBar%"))
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
                docsFound.each {doc ->
                    tempDocs.add(categoryService.getCategory(catName).docs?.find { it == doc })
                }
            }
            println(tempDocs)
            docsFound = tempDocs.findAll { tempDocs.count(it) == (temp.size()) }.unique().sort { it.viewCount }
            println(docsFound)
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

    def showDoc() {
        def start, stop, author
        def otherDocs = [:]
        start = new Date()
        Document myDoc = null

        //Falls ein anderes Dokument angezeigt werden soll, überschreibe das Default-Test-Dokument
        if (params.docTitle) {
            myDoc = documentService.getDoc(params.docTitle)
            println('parents ' + myDoc.parentCats)
        }

        if (!myDoc) {
            flash.error = message(code: 'kb.error.noSuchDocument') as String
            forward(view: 'index', model: [otherDocs: loadTestDocs(), principal: springSecurityService.principal])
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
        stop = new Date()
        println('\nSeitenladezeit: '+TimeCategory.minus(stop, start))
        println(otherDocs)
        [document: myDoc, author: author, similarDocs: otherDocs, principal: springSecurityService.principal]
    }

    def showCat() {
        def cat = null
        if (params.name) {
            cat = categoryService.getCategory(params.name)
        }
        [cat: cat, mainCats:(!cat)?categoryService.getAllMainCats():null]
    }

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

    def createCat() {
        println(params)
        if (params.submit) {
            if (!params.catName || params.catName == '') flash.error = message(code: 'kb.error.attrNameCantBeNull') as String


            if (!(flash.error)) {
                println('Cat anlegen')

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

        }
        def all = [:]
        categoryService.getAllMainCats().each { mainCat ->
            def temp = []
            categoryService.getIterativeAllSubCats(mainCat.name).each { cat ->
                temp.add(cat.name as String)
            }
            all.put(mainCat.name, temp.sort{ it })
        }
        [cat: params.name?categoryService.getCategory(params.name):null, allCatsByMainCats: all]
    }

    def findUnlinkedSubCats() {
        def subCats = Subcategory.findAll()
        subCats.removeAll { it.parentCat != null }
        println(subCats)
        [subCats: subCats]
    }

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
            [cat: params.name?categoryService.getCategory(params.name):null, allCatsByMainCats: all]
        } else {
            println(params)

            if (!params.catName || params.catName == '') flash.error = message(code: 'kb.error.attrNameCantBeNull') as String


            if (!(flash.error)) {
                println('Cat ändern')
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


    @Secured("hasAuthority('ROLE_GP-STAFF')") //Für optinale Erweiterung "Autoren" später Abfrage, ob User als Autor eingetragen ist
    def createDoc() {
        if (params.submit) {
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
                println('Faq')
                if (params.question && !params.question.empty && params.answer && !params.answer.empty) {
                    println('create Faq')
                    doc = documentService.newFaq(params.question as String, params.answer as String, docTags).save()
                    println('Faq erstellt')

                } else {
                    flash.error = message(code: 'kb.error.fillOutAllFields') as String
                    params.createFaq = 'faq'
                }

            }
            //todo
            //else if (params.article == 'create') {

            //}


            if (!flash.error) {
                println(doc)
                categoryService.addDoc(doc, docSubs as String[])
                flash.info = message(code: 'kb.info.docCreated') as String
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
        println('all' + all)
        [cats: all, docType: params.createFaq?'faq':params.createTut?'tutorial':'', principal: springSecurityService.principal]
    }

    // TODO [TR]: und der Content-Type ? Siehe RFC 1945
    def exportDoc() {
        Document doc
        if (!params.docTitle && !params.exportAs) render("Error: Not enought arguments, 'docTitle' or 'exportAs' missing. Possible solutions for 'exportAs': 'json'/'xml'")
        doc = documentService.getDoc(params.docTitle)
        render (documentService.exportDoc(doc, params.exportAs as String))
    }
}
