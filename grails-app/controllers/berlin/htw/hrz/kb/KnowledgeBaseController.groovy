/*
  Created by IntelliJ IDEA.
  User: didschu
 */
package berlin.htw.hrz.kb

import grails.converters.XML
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

import java.nio.file.Files
import java.nio.file.Paths

/**
 * Controller class for handling the requests, redirects and processing data given from the views
 */
@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class KnowledgeBaseController {

    //Injection der benötigten Serviceklassen
    DocumentService documentService
    CategoryService categoryService
    ImportService importService
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
        flash.error << temp
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
     * Controller method for changing an article
     * @return
     */
    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def changeArticle(){
        if (params.submit) {
            def docSubs = []
            String[] docTags = null
            def doc = documentService.getDoc(params.docTitle)

            //Hole und Verarbeite Tags
            if (params.docTags) {
                //Hole dir den tag-input und entferne alle 'space' Elemente (Leerzeichen, Tab, \n etc)
                docTags = (params.docTags as String)?.replaceAll('\\s', '')?.split(',')
            }

            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            def clickedSubcats = params.list('checkbox') as String[]
            if (!clickedSubcats) {
                flash.error << message(code: 'kb.error.noSubCatGiven') as String
            } else {
                //Füge den Autor (eingeloggter User) des Dokuments an
                docSubs.add(params.authorNew)
                //Füge Sprache hinzu
                docSubs.add(params.languageNew)
                //Füge alle angeklickten Subkategorien an
                docSubs.addAll(clickedSubcats)
            }

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (!flash.error && doc) {
                if (params.docTitleNew && !params.docTitleNew.empty && params.docContent && !params.docContent.empty) {
                    //Titel ändern
                    if (params.docTitle != params.docTitleNew) {
                        println('Title ändern')
                        doc = documentService.changeDocTitle(doc, params.docTitleNew)
                        println('Titel geändert')
                    }
                    //Content ändern
                    println("title: $doc.docTitle")
                    doc = documentService.changeArticleContent(doc, params.docContent)
                    //Tags ändern
                    println('Tags ändern')
                    doc = documentService.changeDocTags(doc, docTags)
                    //Parents ändern, aber nur, wenn sich etwas geändert hat
                    List newParents = categoryService.getSubcategories(docSubs as String[])
                    List oldParents = doc.linker.subcat as List
                    if (oldParents.size() != newParents.size() || !newParents.containsAll(oldParents)) {
                        println('Parents ändern')
                        doc = documentService.changeDocParents(doc, categoryService.getSubcategories(docSubs as String[]))
                    }
                } else {
                    flash.error << message(code: 'kb.error.fillOutAllFields') as String
                }

                if (!flash.error) { flash.info << message(code: 'kb.info.docChanged') as String }
                else { flash.error << message(code: 'kb.error.somethingWentWrong') as String }
                redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
            }
        } else {
            [cats: categoryService.getAllMaincatsWithSubcats([categoryService.getCategory('author'), categoryService.getCategory('lang')] as List), lang:categoryService.getAllSubCats(categoryService.getCategory('lang')).sort{it.name}.name, author:categoryService.getAllSubCats(categoryService.getCategory('author')).sort{it.name}.name, principal: springSecurityService.principal, doc:documentService.getDoc(params.docTitle)]
        }
    }

    /**
     * Controller method for changing a category.
     * In the given states it is only allowed the change a subcategory.
     * @return
     */
    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def changeCat() {
        if (params.submit) {
            if (!params.catName) { flash.error << message(code: 'kb.error.attrNameCantBeNull') as String }
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
                        flash.info << message(code: 'kb.info.catChanged') as String
                    }
                    else { flash.error << message(code: 'kb.error.somethingWentWrong') as String }
                }
                else { flash.error << message(code: 'kb.error.cantDeleteOrChangeMainCat') as String }
                redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
            }
        }
        [cat: params.name?categoryService.getCategory(params.name):null, allCatsByMainCats: categoryService.getAllMaincatsWithSubcats(), principal: springSecurityService.principal]
    }

    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def changeFaq() {
        if (params.submit) {
            def docSubs = []
            String[] docTags = null
            def doc = documentService.getDoc(params.docTitle)

            //Hole und Verarbeite Tags
            if (params.docTags) {
                //Hole dir den tag-input und entferne alle 'space' Elemente (Leerzeichen, Tab, \n etc)
                docTags = (params.docTags as String)?.replaceAll('\\s', '')?.split(',')
            }

            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            def clickedSubcats = params.list('checkbox') as String[]
            if (!clickedSubcats) {
                flash.error << message(code: 'kb.error.noSubCatGiven') as String
            } else {
                //Füge den Autor (eingeloggter User) des Dokuments an
                docSubs.add(params.authorNew)
                //Füge Sprache hinzu
                docSubs.add(params.languageNew)
                //Füge alle angeklickten Subkategorien an
                docSubs.addAll(clickedSubcats)
            }

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (!flash.error && doc) {
                if (params.question && params.question.replaceAll('\\s', '') != '' && params.answer && params.answer.replaceAll('\\s', '') != '' ) {
                    //Title und Frage ändern
                    if (params.docTitle != params.question) {
                        doc = documentService.changeDocTitle(doc, params.question)
                        doc = documentService.changeFaqQuestion(doc, params.question)
                    }
                    //Antwort ändern
                    doc = documentService.changeFaqAnswer(doc, params.answer)
                    //Tags ändern
                    doc = documentService.changeDocTags(doc, docTags)
                    //Parents ändern, aber nur, wenn sich etwas geändert hat
                    List newParents = categoryService.getSubcategories(docSubs as String[])
                    List oldParents = doc.linker.subcat as List
                    if (oldParents.size() != newParents.size() || !newParents.containsAll(oldParents)) {
                        doc = documentService.changeDocParents(doc, categoryService.getSubcategories(docSubs as String[]))
                    }
                } else {
                    flash.error << message(code: 'kb.error.fillOutAllFields') as String
                }

                if (!flash.error) { flash.info << message(code: 'kb.info.docChanged') as String }
                else { flash.error << message(code: 'kb.error.somethingWentWrong') as String }
                redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
            }
        } else {
            [cats: categoryService.getAllMaincatsWithSubcats([categoryService.getCategory('author'), categoryService.getCategory('lang')] as List), lang:categoryService.getAllSubCats(categoryService.getCategory('lang')).sort{it.name}.name, author:categoryService.getAllSubCats(categoryService.getCategory('author')).sort{it.name}.name, principal: springSecurityService.principal, doc:documentService.getDoc(params.docTitle)]
        }
    }

    def changeTutorial() {
        if (params.submit) {
            def docSubs = []
            String[] docTags = null
            def doc = documentService.getDoc(params.docTitle)

            //Hole und Verarbeite Tags
            if (params.docTags) {
                //Hole dir den tag-input und entferne alle 'space' Elemente (Leerzeichen, Tab, \n etc)
                docTags = (params.docTags as String)?.replaceAll('\\s', '')?.split(',')
            }

            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            def clickedSubcats = params.list('checkbox') as String[]
            if (!clickedSubcats) {
                flash.error << message(code: 'kb.error.noSubCatGiven') as String
            } else {
                //Füge den Autor (eingeloggter User) des Dokuments an
                docSubs.add(params.authorNew)
                //Füge Sprache hinzu
                docSubs.add(params.languageNew)
                //Füge alle angeklickten Subkategorien an
                docSubs.addAll(clickedSubcats)
            }

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (!flash.error && doc) {
                //Finde alle Daten für die einzelnen Schritte und verarbeite sie
                def stepData = [:]
                List steps
                stepData << params.findAll { it.key =~ /stepTitle_[0-9]+/ && it.value } << params.findAll { it.key =~ /stepText_[0-9]+/  && it.value } << params.findAll { it.key =~ /stepLink_[0-9]+/ }
                steps = documentService.newSteps(stepData as Map)

                if (steps && params.docTitleNew) {
                    //Title und Frage ändern
                    if (params.docTitle != params.docTitleNew) {
                        doc = documentService.changeDocTitle(doc, params.docTitleNew)
                    }
                    //Steps ändern
                    doc = documentService.changeTutorialSteps(doc, steps)
                    //Tags ändern
                    doc = documentService.changeDocTags(doc, docTags)
                    //Parents ändern, aber nur, wenn sich etwas geändert hat
                    List newParents = categoryService.getSubcategories(docSubs as String[])
                    List oldParents = doc.linker.subcat as List
                    if (oldParents.size() != newParents.size() || !newParents.containsAll(oldParents)) {
                        doc = documentService.changeDocParents(doc, categoryService.getSubcategories(docSubs as String[]))
                    }
                } else {
                    flash.error << message(code: 'kb.error.fillOutAllFields') as String
                }

                if (!flash.error) { flash.info << message(code: 'kb.info.docChanged') as String }
                else { flash.error << message(code: 'kb.error.somethingWentWrong') as String }
                redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
            }
        } else {
            [cats: categoryService.getAllMaincatsWithSubcats([categoryService.getCategory('author'), categoryService.getCategory('lang')] as List), lang:categoryService.getAllSubCats(categoryService.getCategory('lang')).sort{it.name}.name, author:categoryService.getAllSubCats(categoryService.getCategory('author')).sort{it.name}.name, principal: springSecurityService.principal, doc:documentService.getDoc(params.docTitle)]
        }
    }

    /**
     * Controller method for creating a subcategory
     * @return
     */
    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def createCat() {
        //Falls createCat Form abgeschickt wurde, bearbeite Daten und erstelle Subkategorie
        if (params.submit) {
            if (!params.catName) { flash.error << message(code: 'kb.error.attrNameCantBeNull') as String }
            else {
                //Hole die Elternkategorie
                Category newParent = categoryService.getCategory(params.parentCat)

                //Erstelle neue Subkategorie
                if (categoryService.newSubCategory(params.catName as String, newParent)) {
                    flash.info << message(code: 'kb.info.catCreated') as String
                }
                else { flash.error << message(code: 'kb.error.somethingWentWrong') as String }

            }
            redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request),principal: springSecurityService.principal])
        }
        [cat: params.name?categoryService.getCategory(params.name):null, allCatsByMainCats: categoryService.getAllMaincatsWithSubcats(), principal: springSecurityService.principal, origin: params.originName]
    }

    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def createArticle() {
        if (params.submit) {
            def docSubs = []
            String[] docTags = null
            Article doc = null

            //Hole und Verarbeite Tags
            if (params.docTags) {
                //Hole dir den tag-input und entferne alle 'space' Elemente (Leerzeichen, Tab, \n etc)
                String tags = (params.docTags as String).replaceAll('\\s', '')
                docTags = tags.split(',')
            }

            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            def clickedSubcats = params.list('checkbox') as String[]
            if (!clickedSubcats) {
                flash.error << message(code: 'kb.error.noSubCatGiven') as String
            } else {
                //Füge den Autor (eingeloggter User) des Dokuments an
                docSubs.add(springSecurityService.principal.username)
                //Füge Sprache hinzu
                docSubs.add(params.language as String)
                //Füge alle angeklickten Subkategorien an
                docSubs.addAll(clickedSubcats)
            }

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (!flash.error) {
                if (params.docTitle && !params.docTitle.empty && params.docContent && !params.docContent.empty) {
                    doc = documentService.newArticle(params.docTitle as String, params.docContent as String, docTags)
                } else {
                    flash.error << message(code: 'kb.error.fillOutAllFields') as String
                }

                if (!flash.error) {
                    if (documentService.changeDocParents(doc, categoryService.getSubcategories(docSubs as String[]))) {
                        flash.info << message(code: 'kb.info.docCreated') as String
                        redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
                    }
                }
            }
        }
        [cats: categoryService.getAllMaincatsWithSubcats([categoryService.getCategory('author'), categoryService.getCategory('lang')] as List), lang:categoryService.getAllSubCats(categoryService.getCategory('lang')).sort{it.name}.name, principal: springSecurityService.principal]
    }

    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def createFaq() {
        if (params.submit) {
            def docSubs = []
            String[] docTags = null
            Faq doc = null

            //Hole und Verarbeite Tags
            if (params.docTags) {
                //Hole dir den tag-input und entferne alle 'space' Elemente (Leerzeichen, Tab, \n etc)
                String tags = (params.docTags as String).replaceAll('\\s', '')
                docTags = tags.split(',')
            }

            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            def clickedSubcats = params.list('checkbox') as String[]
            if (!clickedSubcats) {
                flash.error << message(code: 'kb.error.noSubCatGiven') as String
            } else {
                //Füge den Autor (eingeloggter User) des Dokuments an
                docSubs.add(springSecurityService.principal.username)
                //Füge Sprache hinzu
                docSubs.add(params.language as String)
                //Füge alle angeklickten Subkategorien an
                docSubs.addAll(clickedSubcats)
            }

            //Verarbeite dokumentspezifische Daten (Tutorial: verarbeite einzelne Steps, FAQ: verarbeite Frage-Antwort)
            if (!flash.error) {
                if (params.question && !params.question.empty && params.answer && !params.answer.empty) {
                    doc = documentService.newFaq(params.question as String, params.answer as String, docTags)
                } else {
                    flash.error << message(code: 'kb.error.fillOutAllFields') as String
                }

                if (!flash.error) {
                    if (documentService.changeDocParents(doc, categoryService.getSubcategories(docSubs as String[]))) {
                        flash.info << message(code: 'kb.info.docCreated') as String
                        redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
                    }
                }
            }
        }
        [cats: categoryService.getAllMaincatsWithSubcats([categoryService.getCategory('author'), categoryService.getCategory('lang')] as List), lang:categoryService.getAllSubCats(categoryService.getCategory('lang')).sort{it.name}.name, principal: springSecurityService.principal]
    }

    /**
     * Controller method for creating a new tutorial
     */
    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def createTutorial() {
        if (params.submit) {
            def docSubs = []
            String[] docTags = null
            Tutorial doc = null

            //Hole und Verarbeite Tags
            if (params.docTags) {
                //Hole dir den tag-input und entferne alle 'space' Elemente (Leerzeichen, Tab, \n etc). Splitte dann die gefundenen Schlagworte.
                docTags = (params.docTags as String).replaceAll('\\s','').split(',')
            }
            //Hole Subkategorien, repräsentiert durch Checkboxen und erzeuge eine Liste aus den ausgewählten
            def clickedSubcats = params.list('checkbox') as String[]
            if (!clickedSubcats) {
                flash.error << message(code: 'kb.error.noSubCatGiven') as String
            } else {
                //Füge den Autor (eingeloggter User) des Dokuments an
                docSubs.add(springSecurityService.principal.username)
                //Füge Sprache hinzu
                docSubs.add(params.language as String)
                //Füge alle angeklickten Subkategorien an
                docSubs.addAll(clickedSubcats)
            }

            if (!flash.error) {
                //Finde alle Daten für die einzelnen Schritte und verarbeite sie
                def stepData = [:]
                List steps
                stepData << params.findAll { it.key =~ /stepTitle_[0-9]+/ && it.value } << params.findAll { it.key =~ /stepText_[0-9]+/  && it.value } << params.findAll { it.key =~ /stepLink_[0-9]+/ }
                steps = documentService.newSteps(stepData as Map)

                if (!steps || !params.docTitle) {
                    flash.error << message(code: 'kb.error.fillOutAllFields') as String
                    params.createDoc = 'tutorial'
                } else {
                    doc = documentService.newTutorial(params.docTitle as String, steps, docTags)
                }
                if (!flash.error) {
                    if (documentService.changeDocParents(doc, categoryService.getSubcategories(docSubs as String[]))) {
                        flash.info << message(code: 'kb.info.docCreated') as String
                        redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
                    }
                }
            }
        }
        [cats: categoryService.getAllMaincatsWithSubcats([categoryService.getCategory('author'), categoryService.getCategory('lang')] as List), lang:categoryService.getAllSubCats(categoryService.getCategory('lang')).sort{it.name}.name, principal: springSecurityService.principal]
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
                flash.info << message(code: 'kb.info.catDeleted') as String
            }
            else { flash.error << message(code: 'kb.error.noSuchCategorie') as String }
        }
        else { flash.error << message(code: 'kb.error.attrNameNotFound') as String }
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
                flash.info << "Dokument '${params.docTitle}' wurde gelöscht"
            }
            else { flash.error << message(code: 'kb.error.noSuchDocument') as String }
        }
        else { flash.error << message(code: 'kb.error.attrDocTitleNotFound') as String }
        redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
    }

    /**
     * Controller method for downloading files
     */
    def download() {
        println params
        if (params.file) {
            String file_path = params.file as String
            String file_name = file_path.substring(file_path.lastIndexOf('/') + 1, file_path.size())
            String parent_path = file_path.split(/\/[\w-]+\.[a-z0-9]+/)[0]
            File file = new File("${grailsApplication.config.'kb.file.dir' as String}$parent_path", file_name)

            if (!file.exists()) {
                flash.error <<  message(code: 'kb.error.noSuchDownloadableFileFound') as String
            } else if(!file.canRead()) {
                flash.error <<  message(code: 'kb.error.noRightsToReadFile') as String
            } else {
                response.setHeader("Content-disposition", "attachment;filename=\"${file_name}\"")
                response.setHeader("Content-Type", Files.probeContentType(Paths.get(file.toURI())))
                response.outputStream << file.bytes
            }
        }
        else {
            flash.error <<  message(code: 'kb.error.wrongParameter') as String
        }

        if (flash.error) {
            redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
        }
    }

    /**
     *
     * Controller method for either exporting a single document or a list of all unlocked documents.
     * Usage: use '/document(.:format)' for getting the list or '/document/:docTitle(.:format)' for getting a single document.
     * If you don't use the format parameter this method will look for the accepted formats in the accept-header if no fitting found xml will be used as a standard.
     * Only JSON and XML format are declared, if you try this method with another format XML will be used!
     * INFORMATION: Only method which is configured and optimized as a REST method
     * @return object can be either a json or xml object. The Object represents either a list of documents or a single document
     */
    def exportDoc() {
        String myErrorMessage = null
        Document doc = null

        if (params.docTitle) {
            try {
                doc = documentService.getDoc(params.docTitle)
            }
            catch (Exception ex) {
                response.status = 500
                myErrorMessage =   "Short:$ex.message; Long:$ex.stackTrace"
            }
        }

        if (!myErrorMessage) {
            withFormat {
                json {render (text: documentService.exportDoc('json',doc?doc:null), contentType: "application/json")}
                '*' {render (text: documentService.exportDoc('xml', doc?doc:null), contentType: "application/xml")}
            }
        } else {
            render([error: myErrorMessage] as XML)
        }
    }

    /**
     * Controller method for finding unlinked elements (subcategories and documents)
     * @return
     */
    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def findUnlinkedObjs() {
        [subCats: categoryService.findUnlinkedSubcats(), docs:documentService.findUnlinkedDocs(), principal: springSecurityService.principal]
    }

    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    //Debug
    //todo: verfeinern
    def importDocs() {
        //println params
        if (params.submit) {
            if (!params.username || !params.password || (params.infoFile as MultipartFile).isEmpty()) {
                flash.error << message(code: 'kb.error.fillOutAllFields') as String
            } else {
                /*
            MultipartFile tempFile = request.getFile('infoFile')
            println tempFile.getContentType()
            File myFile = new File(tempFile.getOriginalFilename());
            myFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(myFile);
            fos.write(tempFile.getBytes());
            fos.close();
            myFile.readLines().each {
                println it
            }
            */
                if (importService.importOldDocs(request.getFile('infoFile') as MultipartFile, params.username as String, params.password as String)) {
                    flash.info << message(code: 'kb.info.documentsImported') as String
                    redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
                } else {
                    flash.error << message(code: 'kb.error.credentialsWrong') as String
                }
                //importService.importOldDocs(temp.split('\n').toList())
            }
        }

        [principal: springSecurityService.principal]
    }

    @Secured(["hasAuthority('ROLE_GP-STAFF')", "hasAuthority('ROLE_GP-PROF')"])
    def importFiles() {
        println params.submit
        if (!params.username || !params.password || (params.infoFile as MultipartFile).isEmpty()) {
            flash.error << message(code: 'kb.error.fillOutAllFields') as String
        } else {
            /*
        MultipartFile tempFile = request.getFile('infoFile')
        println tempFile.getContentType()
        File myFile = new File(tempFile.getOriginalFilename());
        myFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(myFile);
        fos.write(tempFile.getBytes());
        fos.close();
        myFile.readLines().each {
            println it
        }
        */
            if (importService.importOldFiles(request.getFile('infoFile') as MultipartFile, params.username as String, params.password as String)) {
                flash.info << message(code: 'kb.info.documentsImported') as String
                redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
            } else {
                flash.error << message(code: 'kb.error.credentialsWrong') as String
            }
            //importService.importOldDocs(temp.split('\n').toList())
        }

    [principal: springSecurityService.principal]
    }

    /**
     * Controller method for the index (main) page
     * @return
     */
    def index() {
        //Falls keine Hauptkategorie angelegt ist, lege Struktur mit Testdokumente an
        if (Category.findAll().empty) {
            initService.initTestModell()
            flash.info << message(code: 'kb.info.testStructureAndDataCreated') as String
        }

        println "lookup: $request.remoteAddr"
        println "principal: $springSecurityService.principal"
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

        if (params.docTitle || params.mirUrl) {
            if (params.docTitle) { myDoc = documentService.getDoc(params.docTitle) }
            if (params.mirUrl) { myDoc = documentService.getDocByMirURL(params.mirUrl)}

            if (myDoc) {
                //Erhöhe Viewcount
                myDoc = documentService.increaseCounter(myDoc)
                if (!(myDoc instanceof Faq)) {
                    //Falls Dokument nicht vom Typ Faq ist, hole verwandte Dokumente bzw andere interessante Dokumente
                    otherDocs = categoryService.getAdditionalDocs(myDoc)
                }
            }
            else { flash.error << message(code: 'kb.error.noSuchDocument') as String }
        }
        else { flash.error << message(code: 'kb.error.attrDocTitleNotFound') as String }

        if (flash.error) {
            redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
        } else {
            [document: myDoc, author: documentService.getAuthor(myDoc), lang: documentService.getLanguage(myDoc), similarDocs: otherDocs, principal: springSecurityService.principal]
        }
    }

    def showImage() {
        //Step myStep = Step.findById(params.id)
        def myImage = Image.findById(params.id)?:ImageCached.findById(params.id)

        render file: myImage.blob, contentType: myImage.mimeType
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
            flash.error << message(code: 'kb.error.searchTermTooShort') as String
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
        [searchBar: params.searchBar ,foundDocs: docsFound ,principal: springSecurityService.principal, allCatsByMainCats: categoryService.getAllMaincatsWithSubcats(), filter: filter, hasAside: true]
    }

    /**
     * Controller method for uploading a media file
     * @return
     */
    def uploadFile() {
        println params
        println "Test"
        if (params.submit) {
            List<String> corruptedFiles = []

            if (!params.parentPath || (params.uploadFile as MultipartFile).isEmpty()) {
                flash.error << message(code: 'kb.error.fillOutAllFields') as String
            } else if (!(params.parentPath ==~ /\/([a-z0-9]+\/)?([a-z0-9]+\/?)/)) {
                flash.error << message(code: 'kb.error.wrongParentPathInput') as String
            } else {
                String parentPath = params.parentPath as String
                println parentPath

                request.getMultiFileMap().uploadFile.each { MultipartFile file ->
                    println file.originalFilename

                    if (!importService.importFile(file, parentPath)) {
                        corruptedFiles.add(file.originalFilename)
                    }
                }
            }

            if (!flash.error) {
                flash.info << "${message(code: 'kb.info.fileUploaded')}"
                if (corruptedFiles) {
                    flash.error << "${message(code: 'kb.error.uploadFiles')}:${corruptedFiles.collect {" $it"}}"
                }
                redirect(view: 'index', model: [otherDocs: categoryService.getDocsOfInterest(springSecurityService.principal, request), principal: springSecurityService.principal])
            }
        }
        [principal: springSecurityService.principal]
    }
}
