package berlin.htw.uss.fo

import berlin.htw.uss.utility.ExceptionHandler
import berlin.htw.uss.utility.Wizard
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured

import java.sql.Timestamp

@Secured('hasAuthority("X_HAS_PVZ_PERSON")')
class DoctoratesController implements Wizard, ExceptionHandler {
    SpringSecurityService springSecurityService
    DoctoratesService doctoratesService
    ResearchersService researchersService
    Map data = [:]

    int globalMax = 10

    /*
    TODO:
    - optimieren, aufräumen und säubern
    - /persons umsetzen
    - /Miniansicht Link auf /persons/id
     */

    /*########## Methoden zur Anzeige ##########*/

    /**
     *
     * @return
     */
    def index() {
        flash.info = flash.info
        println doctoratesService.getPersonStats()
        redirect(action: 'own')
    }

    /**
     *
     * @return
     */
    def own() {
        flash.info = flash.info
        String pvzID = springSecurityService.principal.data.person.pvzId.toString()
        int max = params.max?Integer.parseInt(params.max):globalMax
        int offset = params.offset?Integer.parseInt(params.offset):0

        withFormat {
            xml { render doctoratesService.getDoctoratesCompleteOrderByCreated(pvzID, max, offset) as XML }
            json { render doctoratesService.getDoctoratesCompleteOrderByCreated(pvzID, max, offset) as JSON }
            '*' {
                flash.js << 'Chart.bundle.min.js' << '/doctorates/chartJs?pvzID='+pvzID
                flash.js << 'jquery-ui.min.js' << 'dialog.js'
                doctoratesService.getDoctoratesCompleteOrderByCreated(pvzID, max, offset).each {
                    println "$it\n\n"
                }
                [all: doctoratesService.getDoctoratesCompleteOrderByCreated(pvzID, max, offset), count: doctoratesService.getDoctoratesCount(pvzID)]
            }
        }
    }


    //todo: sortieren wonach?
    /**
     *
     * @return
     */
    def all() {
        flash.info = flash.info
        int max = params.max?Integer.parseInt(params.max):globalMax
        int offset = params.offset?Integer.parseInt(params.offset):0

        withFormat {
            xml { render doctoratesService.getDoctoratesCompleteOrderById(max, offset) as XML }
            json { render doctoratesService.getDoctoratesCompleteOrderById(max, offset) as JSON }
            '*' {
                flash.js << 'Chart.bundle.min.js' << '/doctorates/chartJs'
                flash.js << 'jquery-ui.min.js' << 'dialog.js'
                [all: doctoratesService.getDoctoratesCompleteOrderById(max, offset), count: doctoratesService.getDoctoratesCount()]
            }
        }
    }

    //todo: wonach sortieren accepted/created/lastmodified?
    /**
     *
     * @return
     */
    def newest() {
        flash.info = flash.info
        int max = params.max?Integer.parseInt(params.max):globalMax
        int offset = params.offset?Integer.parseInt(params.offset):0

        withFormat {
            xml { render doctoratesService.getDoctoratesCompleteOrderByCreated(max, offset) as XML }
            json { render doctoratesService.getDoctoratesCompleteOrderByCreated(max, offset) as JSON }
            '*' {
                flash.js << 'Chart.bundle.min.js' << '/doctorates/chartJs'
                flash.js << 'jquery-ui.min.js' << 'dialog.js'
                [all: doctoratesService.getDoctoratesCompleteOrderByCreated(max, offset), count: doctoratesService.getDoctoratesCount()]
            }
        }
    }

    def categories() {
        flash.info = flash.info

        if (params.id) {
            int id = Integer.valueOf(params.id)
            int max = params.max?Integer.parseInt(params.max):globalMax
            int offset = params.offset?Integer.parseInt(params.offset):0

            withFormat {
                xml { render doctoratesService.getDoctoratesCompleteOrderByCreated(id, max, offset) as XML }
                json { render doctoratesService.getDoctoratesCompleteOrderByCreated(id, max, offset) as JSON }
                '*' {
                    flash.js << 'Chart.bundle.min.js' << '/doctorates/chartJs?catID='+id
                    flash.js << 'jquery-ui.min.js' << 'dialog.js'
                    [usedCats: doctoratesService.categoryStats, cat: doctoratesService.getCategoryLabel(id), catID: id, all: doctoratesService.getDoctoratesCompleteOrderByCreated(id, max, offset), count: doctoratesService.getDoctoratesCount(id)]
                }
            }
        } else {
            withFormat {
                xml { render doctoratesService.categoryStats as XML }
                json { render doctoratesService.categoryStats as JSON }
                '*' {
                    flash.js << 'Chart.bundle.min.js' << '/doctorates/pieChartJs?stype=categories&tlabel=Wissenschaftsgebiet'
                    flash.js << 'jquery-ui.min.js' << 'dialog.js'
                    [usedCats: doctoratesService.categoryStats, all: null, cat:null]
                }
            }
        }
    }

    /*
    def persons() {
        flash.info = flash.info

        if (params.id) {
            String pvzID = params.id as String
            int max = params.max?Integer.parseInt(params.max):globalMax
            int offset = params.offset?Integer.parseInt(params.offset):0

            List found = doctoratesService.getDoctoratesOrderByCreated(pvzID, max, offset)

            withForm {
                xml { render found as XML }
                json { render found as JSON }
                '*' {
                    [found: found, person: doctoratesService.getPerson(pvzID)]
                }
            }
        } else {
            List personStats = doctoratesService.getPersonStats()
            withForm {
                xml { render personStats as XML }
                json { render personStats as JSON }
                '*' {
                    [found: null, personStats: personStats, letters: personStats.groupBy({ k -> k?.sn?.charAt(0)?.toUpperCase()}).sort()]
                }
            }
        }
    }
    */

    /**
     *
     * @return
     */
    def show() {
        if (!params.id) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action:'index')
        } else {
            int id = Integer.valueOf(params.id)

            if (!doctoratesService.canRead(id)) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            Map doct = doctoratesService.getDoctorateComplete(id)

            if (!doct) {
                flash.errors << message(code: 'doctorate.error.noDoctFound')
                redirect(action: 'index')
                return
            }
            flash.info = flash.info
            [doct: doct, cats: doctoratesService.getAllAssociatedCategories(id), canWrite: doctoratesService.canWrite(id), attachments: doctoratesService.getAttachments(id), unusedSVTypes: doctoratesService.getUnusedSupervisionTypes(id), published: doctoratesService.getPublished(id)]
        }
    }


    /*########## Methoden zum Anlegen, bearbeiten und löschen ##########*/


    /**
     *
     * @return
     */
    def addPerson() {
        if (!params.id || !params.new_person_select) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        }else {
            if (!doctoratesService.canWrite(Integer.valueOf(params.id))) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            redirect(action: 'editSupervisor', params: [id: Integer.valueOf(params.id), svCat: params.new_person_select])
        }

    }

    /**
     *
     * @return
     */
    def addPublished() {
        if (!params.id) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        } else {
            if (!doctoratesService.canWrite(Integer.valueOf(params.id))) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            redirect(action: 'editPublished', id: Integer.valueOf(params.id))
        }
    }



    /**
     * Controller Method for creating a new doctorate entry
     * @return
     */
    //todo: Authority Prof
    def create() {
        wizard(data)
    }

    /**
     * Controller Method for deleting a doctorate entry
     * @return
     */
    def delete() {
        if (!params.id) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        } else {
            if (!doctoratesService.canWrite(Integer.valueOf(params.id))) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            if (doctoratesService.deleteDoctorateComplete(Integer.valueOf(params.id))) {
                flash.info = message(code: 'doctorate.info.doctorateDeleted')
            } else {
                flash.errors << message(code: 'error.general')
            }
            redirect(action: 'index')
        }
    }

    /**
     *
     * @return
     */
    def deleteAttachment() {
        if (!params.id || !params.attachCat) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        } else {
            if (!doctoratesService.canWrite(Integer.valueOf(params.id))) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            if (doctoratesService.deleteAttachment(Integer.valueOf(params.id), params.attachCat as String)) {
                flash.info = message(code: 'doctorate.info.attachmentDeleted')
            } else {
                flash.errors << message(code: 'error.general')
            }
            redirect(action: 'show', id: Integer.valueOf(params.id))
        }
    }

    /**
     *
     * @return
     */
    def deletePerson() {
        if (!params.id || !params.svCat) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        } else {
            int id = Integer.valueOf(params.id)
            String svCat = params.svCat as String

            if (!doctoratesService.canWrite(id)) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            if (!doctoratesService.getPerson(id, svCat)) {
                flash.errors << message(code: 'doctorate.error.noSuchPerson')
            } else if (svCat.endsWith('supervisor')) {
                flash.errors << message(code: 'doctorate.error.cantRemoveSupervisors')
            } else {
                if (doctoratesService.deletePerson(id, svCat)) {
                    flash.info = message(code: 'doctorate.info.partDeleted')
                } else {
                    flash.errors << message(code: 'error.general')
                }
            }

            redirect(action: 'show', id: id)
        }
    }

    def deletePublished() {
        if (!params.id) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        } else {
            int id = Integer.valueOf(params.id)

            if (!doctoratesService.canWrite(id)) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            if (doctoratesService.getPublished(id)) {
                doctoratesService.deleteAttachment(id, grailsApplication.config.'fo_doctorates'.'attach_for'.'dis' as String)

                if (doctoratesService.deletePublished(id)) {
                    flash.info = message(code: 'doctorate.info.partDeleted')
                } else {
                    flash.errors << message(code: 'error.general')
                }
            }

            redirect(action: 'show', id: id)
        }
    }

    /**
     *
     * @return
     */
    def downloadAttachment() {
        if (!params.id || !params.attachCat) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        } else {
            if (!doctoratesService.canRead(Integer.valueOf(params.id))) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            FileDataHolder res = doctoratesService.getAttachmentComplete(Integer.valueOf(params.id), params.attachCat as String)

            if (!res) {
                flash.errors << message(code: 'doctorate.error.noSuchAttachment')
                redirect(action: 'show', id: Integer.valueOf(params.id))
            } else {
                response.setContentType(res.mimeType)
                response.setHeader('Content-disposition', "attachment;filename=${res.fileName}")
                res.optOutputFile.withInputStream { stream ->
                    response.outputStream << stream
                }
                res.optOutputFile.delete()
                response
            }
        }
    }

    /**
     *
     * @return
     */
    def editGeneral() {
        if (!params.id) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        } else {
            int id = Integer.valueOf(params.id)

            if (!doctoratesService.canWrite(Integer.valueOf(id))) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            Map doct = doctoratesService.getDoctorate(id)

            if (!doct) {
                flash.errors << message(code: 'doctorate.error.noDoctFound')
                redirect(action: 'index')
            } else {
                if (params.submit) {
                    if (!params.doctorate_title || !params.doctorate_status) {
                        flash.errors << message(code: 'portal.error.allMandatory')
                    } else {
                        FileDataHolder tmpFileData = null
                        GeneralDataHolder tmpData = new GeneralDataHolder()

                        Timestamp start = params.'doctorate_acceptDate' ? new Date().parse('dd.MM.yyyy', params.doctorate_acceptDate as String).toTimestamp() : null
                        Timestamp end = params.'doctorate_endDate' ? new Date().parse('dd.MM.yyyy', params.doctorate_endDate as String).toTimestamp() : null

                        if (start && end && end - start <= 0) {
                            flash.errors << message(code: 'doctorate.error.endDateNotAfterStartDate')
                        } else {

                            def f = request.getFile('doctorate_ex_file')

                            tmpData.with {
                                title = params.'doctorate_title' as String
                                status = params.'doctorate_status' as String
                                categories = params.list('doctorate_categories').collect { Integer.valueOf(it) }
                                accepted = start
                                finished = end
                            }

                            if (!f.isEmpty()) {
                                tmpFileData = new FileDataHolder()
                                tmpFileData.with {
                                    fileName = f.getOriginalFilename()
                                    attachCat = grailsApplication.config.'fo_doctorates'.'attach_for'.'ex' as String
                                    created = new Date().toTimestamp()
                                    optInputStream = f.getInputStream()
                                    fileSize = f.getSize()
                                    mimeType = f.getContentType()
                                }
                            }

                            if (doctoratesService.updateGeneralComplete(id, tmpData, tmpFileData)) {
                                flash.info = message(code: 'portal.label.saved')
                                redirect(action: 'show', id: id)
                                return
                            } else {
                                flash.errors << message(code: 'error.general')
                            }
                        }
                    }
                }

                flash.js << 'doctorates/science_categories.js'
                [doct: doct, cats: doctoratesService.getAllAssociatedCategories(id), catsAll: doctoratesService.allCategories ,stati: doctoratesService.getAllStatusTypes() , attachment: doctoratesService.getAttachment(id, grailsApplication.config.'fo_doctorates'.'attach_for'.'ex' as String)]
            }
        }
    }

    /**
     *
     * @return
     */
    def editPromovend() {
        if (!params.id) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        } else {
            int id = Integer.valueOf(params.id)

            if (!doctoratesService.canWrite(Integer.valueOf(id))) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            Map doct = doctoratesService.getDoctorate(id)

            if (!doct) {
                flash.errors << message(code: 'doctorate.error.noDoctFound')
                redirect(action: 'index')
            } else {
                if (params.submit) {
                    if (!params.doctorand_sn || !params.doctorand_givenName || !params.doctorand_graduations || !params.doctorand_hightestGrad_uniName || !params.doctorand_hightestGrad_date) {
                        flash.errors << message(code: 'portal.error.allMandatory')
                    } else {
                        FileDataHolder tmpFileData = null
                        def f = request.getFile('doctorate_sv_agreement_file')
                        PromovendDataHolder promovendDataHolder = new PromovendDataHolder()
                        promovendDataHolder.with {
                            sn = params.doctorand_sn as String
                            givenName = params.doctorand_givenName as String
                            graduations = params.list('doctorand_graduations') as List<String>
                            hightestGradUniName = params.doctorand_hightestGrad_uniName as String
                            hightestGradDate = new Date().parse('dd.MM.yyyy', params.doctorand_hightestGrad_date as String).toTimestamp()
                        }

                        if (!f.isEmpty()) {
                            tmpFileData = new FileDataHolder()
                            tmpFileData.with {
                                fileName = f.getOriginalFilename()
                                attachCat = grailsApplication.config.'fo_doctorates'.'attach_for'.'sv_ag' as String
                                created = new Date().toTimestamp()
                                optInputStream = f.getInputStream()
                                fileSize = f.getSize()
                                mimeType = f.getContentType()
                            }
                        }

                        if (doctoratesService.updatePromovendComplete(id, promovendDataHolder, tmpFileData)) {
                            flash.info = message(code: 'portal.label.saved')
                            flash.test = message(code: 'portal.label.saved')
                            redirect(action: 'show', id: id)
                            return
                        } else {
                            flash.errors << message(code: 'error.general')
                        }
                    }
                }

                flash.js << 'doctorates/main.js'
                [doct: doct, grads: doctoratesService.getGraduations(id), gradTypes: doctoratesService.getAllGraduationTypes(), attachment: doctoratesService.getAttachment(id, grailsApplication.config.'fo_doctorates'.'attach_for'.'sv_ag' as String)]
            }
        }
    }

    def editPublished() {
        if (!params.id) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        } else {
            int id = Integer.valueOf(params.id)

            if (!doctoratesService.canWrite(Integer.valueOf(id))) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            Map doct = doctoratesService.getDoctorate(id)

            if (!doct) {
                flash.errors << message(code: 'doctorate.error.noDoctFound')
                redirect(action: 'index')
            } else {
                if (params.submit) {
                    if (!params.published_title || !params.published_author || !params.published_place || !params.published_isbn || !params.published_date) {
                        flash.errors << message(code: 'portal.error.allMandatory')
                    } else {
                        def f = request.getFile('doctorate_dis_file')
                        FileDataHolder tmpFile = null
                        PublishedDataHolder dataHolder = new PublishedDataHolder()
                        dataHolder.with {
                            doctID = id
                            title = params.published_title as String
                            author = params.published_author as String
                            place = params.published_place as String
                            isbn = params.published_isbn as String
                            date = params.published_date ? new Date().parse('dd.MM.yyyy', params.published_date as String).toTimestamp() : null
                            optLink = (params.published_link as String) ?: null
                        }
                        if (!f.isEmpty()) {
                            tmpFile = new FileDataHolder()
                            tmpFile.with {
                                fileName = f.getOriginalFilename()
                                attachCat = grailsApplication.config.'fo_doctorates'.'attach_for'.'dis' as String
                                created = new Date().toTimestamp()
                                optInputStream = f.getInputStream()
                                fileSize = f.getSize()
                                mimeType = f.getContentType()
                            }
                        }

                        if (doctoratesService.updatePublishedComplete(dataHolder, tmpFile)) {
                            flash.info = message(code: 'portal.label.saved')
                            redirect(action: 'show', id: id)
                            return
                        } else {
                            flash.errors << message(code: 'error.general')
                        }
                    }
                }

                [doct: doct, published: doctoratesService.getPublished(id), attachment: doctoratesService.getAttachment(id, grailsApplication.config.'fo_doctorates'.'attach_for'.'dis' as String)]
            }
        }
    }

    /**
     *
     * @return
     */
    def editSupervisor() {
        if (!params.id || !params.svCat) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        } else {
            int id = Integer.valueOf(params.id)

            if (!doctoratesService.canWrite(Integer.valueOf(id))) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            String svCat = params.svCat as String

            if (!doctoratesService.getDoctorate(id)) {
                flash.errors << message(code: 'doctorate.error.noDoctFound')
                redirect(action: 'index')
            } else {
                Map person = doctoratesService.getPerson(id, svCat)
                if (!person) {
                    person = [:]
                    person.'supervision_category' = svCat
                }

                if (params.submit) {
                    if (params.is_extern || svCat.startsWith('first')) {
                        if (!params.extern_sn && !params.extern_givenName) {
                            flash.errors << message(code: 'portal.error.allMandatory')
                            return
                        }
                        PersonDataHolder tmpData = new PersonDataHolder()
                        tmpData.with {
                            supervisionCategory = svCat
                            sn = params.extern_sn
                            givenName = params.extern_givenName
                            title = params.extern_title ?: null
                            field = params.extern_field ?: null
                            faculty = params.extern_faculty ?: null
                        }


                        if (doctoratesService.updateExtern(id, tmpData)) {
                            flash.info = message(code: 'portal.label.saved')
                            redirect(action: 'show', id: id)
                            return
                        } else {
                            flash.errors << message(code: 'error.general')
                        }
                    } else {
                        if (!params.intern_pvz_id) {
                            flash.errors << message(code: 'doctorate.error.noInternalPersonChosen')
                        } else {
                            if (doctoratesService.updateIntern(id, svCat, params.intern_pvz_id as String)) {
                                flash.info = message(code: 'portal.label.saved')
                                redirect(action: 'show', id: id)
                                return
                            } else {
                                flash.errors << message(code: 'error.general')
                            }
                        }
                    }
                }

                if (!svCat.startsWith('first')) {
                    flash.js << 'doctorates/intern_extern_switch.js'
                }
                [doctID: id, person: person]
            }
        }
    }

    /**
     *
     * @return
     */
    def editUniAndFinance() {
        if (!params.id) {
            flash.errors << message(code: 'doctorate.error.missingParameter')
            redirect(action: 'index')
        } else {
            int id = Integer.valueOf(params.id)

            if (!doctoratesService.canWrite(Integer.valueOf(id))) {
                flash.errors << message(code: 'error.general')
                redirect(action: 'index')
                return
            }

            Map doct = doctoratesService.getDoctorate(id)

            if (!doct) {
                flash.errors << message(code: 'doctorate.error.noDoctFound')
                redirect(action: 'index')
            } else {
                if (params.submit) {
                    if (!params.uni_name || !params.uni_category || !params.uni_location || !params.main_financing) {
                        flash.errors << message(code: 'portal.error.allMandatory')
                    } else {
                        FileDataHolder tmpFileData = null
                        UniFinanceDataHolder tmpData = new UniFinanceDataHolder()
                        def f = request.getFile('doctorate_coop_agreement_file')

                        tmpData.with {
                            uniName = params.uni_name as String
                            uniCategory = params.uni_category as String
                            uniLocation = params.uni_location as String
                            mainFinancing = params.main_financing as String
                            secondFinancing = (params.second_financing && params.second_financing != '0') ? params.second_financing as String : null
                        }

                        if (!f.isEmpty()) {
                            tmpFileData = new FileDataHolder()
                            tmpFileData.with {
                                fileName = f.getOriginalFilename()
                                attachCat = grailsApplication.config.'fo_doctorates'.'attach_for'.'coop' as String
                                created = new Date().toTimestamp()
                                optInputStream = f.getInputStream()
                                fileSize = f.getSize()
                                mimeType = f.getContentType()
                            }
                        }

                        if (doctoratesService.updateUniAndFinancingComplete(id, tmpData, tmpFileData)) {
                            flash.info = message(code: 'portal.label.saved')
                            redirect(action: 'show', id: id)
                            return
                        } else {
                            flash.errors << message(code: 'error.general')
                        }
                    }
                }

                [doct: doct, uniCategories: doctoratesService.getAllUniversityCategories(), financeTypes: doctoratesService.getAllFinanceTypes(), attachment: doctoratesService.getAttachment(id, grailsApplication.config.'fo_doctorates'.'attach_for'.'coop' as String)]
            }
        }
    }

    /*########## Servicemethoden für Statistiken und ähnliches ##########*/

    /**
     *
     * @return
     */
    def pieChartJs() {
        def js=this.class.getResourceAsStream('foDoctoratePieChart.js').text
        js=js.replaceAll('@STYPE@',params.stype?:'')
        js=js.replaceAll('@TLABEL@',params.tlabel?:'')
        render text: js, contentType: 'text/javascript'
    }

    /**
     *
     * @return
     */
    def chartJs() {
        def js=this.class.getResourceAsStream('foDoctorateChart.js').text
        js=js.replaceAll('@PID@',params.pvzID?:'')
        js=js.replaceAll('@CATID@',params.catID?:'')
        render text: js, contentType: 'text/javascript'
    }

    /**
     *
     * @return
     */
    def statsByYear() {
        def docts=doctoratesService.getYearStats(params?.pvzID?.toString()?:null, params.catID?Integer.valueOf(params.catID):null)
        def erg=[doctorates: docts]
        withFormat {
            xml { render erg as XML }
            json { render erg as JSON }
            '*' { [doctorates: docts] }
        }
    }

    /**
     * Needed for 'science_categories.js' so you don't need to 'hard code' the dictionary for translating category-ids into category-labels
     * @return
     */
    def category() {
        if (params.id) {
            render doctoratesService.getCategoryLabel(Integer.valueOf(params.id))?:''
        } else {
            render doctoratesService.getAllCategories() as JSON
        }
    }

    /**
     *
     * @return
     */
    def graduation() {
        Map gradMapping = grailsApplication.config.fo_doctorates.graduations
        if (params.grad) {
            render gradMapping.get(params.grad as String)?:''
        } else {
            render gradMapping as JSON
        }

    }




    /*########## Wizard und Methoden ##########*/


    private reset() {
        data = [:]
        _wizard = [
                [view: 'start', title: message(code: 'doctorate.create.title'), next: this.&start],
                [view: 'general', title: message(code: 'doctorate.general.title'), next: this.&general, beforeView: {
                    flash.js << 'doctorates/science_categories.js'
                }],
                [view: 'promovend', title: message(code: 'doctorate.promovend.title'), next: this.&promovend, beforeView: {
                    flash.js << 'doctorates/main.js'
                }],
                [view: 'uniAndFinancing', title: message(code: 'doctorate.uniAndFinancing.title'), next: this.&uniAndFinancing],
                [view: 'firstSupervisor', title: message(code: 'doctorate.first_supervisor.title'), next: this.&firstSupervisor],
                [view: 'secondSupervisor', title: message(code: 'doctorate.second_supervisor.title'), next: this.&secondSupervisor, beforeView: {
                    flash.js << 'doctorates/intern_extern_switch.js'
                }],
                [view: 'summary', title: message(code: 'activation.step.summary'), next: this.&summary, beforeView: {
                    flash.js << 'doctorates/intern_extern_switch.js'
                }],
                [view: 'finished', title: message(code: 'portal.label.finished'), next: { false }, prev: {
                    false
                }, beforeView: {
                    data = [:]
                    _wizard[0.._wizard.size() - 2]*.enabled = false
                }]
        ]
    }

    private start() {
        //benötigte Daten für spätere Selects in Formularen
        data.categories = doctoratesService.getAllCategories()
        data.graduations = doctoratesService.getAllGraduationTypes()
        data.financings = doctoratesService.getAllFinanceTypes()
        data.uni_categories = doctoratesService.getAllUniversityCategories()
        data.stati = doctoratesService.getAllStatusTypes()

        return true
    }

    private general() {
        if (params.doctorate_title) {
            data.doct_title = params.doctorate_title
            data.doct_status = params.doctorate_status
            data.doct_categories = params.list('doctorate_categories').collect { Integer.valueOf(it) }
            data.doct_acceptDate = params.doctorate_acceptDate ? new Date().parse('dd.MM.yyyy', params.doctorate_acceptDate as String).toTimestamp() : null
            data.doct_endDate = params.doctorate_endDate ? new Date().parse('dd.MM.yyyy', params.doctorate_endDate as String).toTimestamp() : null
            if (data.doct_acceptDate && data.doct_endDate && data.doct_acceptDate.compareTo(data.doct_endDate) >= 0) {
                flash.errors << message(code: 'doctorate.error.endDateNotAfterStartDate')
                return false
            }

            return true
        }
        flash.errors << message(code: 'portal.error.allMandatory')
        return false
    }

    private promovend() {
        if (params.doctorand_sn && params.doctorand_givenName && params.doctorand_graduations && params.doctorand_hightestGrad_uniName && params.doctorand_hightestGrad_date) {
            data.doctorand_sn = params.doctorand_sn as String
            data.doctorand_givenName = params.doctorand_givenName as String
            data.doctorand_graduations = params.list('doctorand_graduations')
            data.doctorand_hightestGrad_uniName = params.doctorand_hightestGrad_uniName
            data.doctorand_hightestGrad_date = new Date().parse("dd.MM.yyyy", params.doctorand_hightestGrad_date as String).toTimestamp()

            return true
        }
        flash.errors << message(code: 'portal.error.allMandatory')
        return false
    }

    private uniAndFinancing() {
        if (params.doctorate_uniName && params.doctorate_uniCategory && params.doctorate_uniPlace && params.doctorate_mainFinancing && params.doctorate_mainFinancing != '0') {
            data.doct_uniName = params.doctorate_uniName as String
            data.doct_uniCategory = params.doctorate_uniCategory as String
            data.doct_uniCategoryFullName = message(code: "doctorate.uniCategories.${data.doct_uniCategory}")
            data.doct_uniPlace = params.doctorate_uniPlace as String
            data.doct_mainFinancing = params.doctorate_mainFinancing as String

            if (params.doctorate_secondaryFinancing && params.doctorate_secondaryFinancing != '0') {
                data.doct_secFinancing = params.doctorate_secondaryFinancing as String
            }

            return true
        }
        flash.errors << message(code: 'portal.error.allMandatory')
        return false
    }

    private firstSupervisor() {
        if (params.doctorate_firstSupervisor_sn && params.doctorate_firstSupervisor_givenName) {
            data.doct_firstSV_sn = params.doctorate_firstSupervisor_sn as String
            data.doct_firstSV_givenName = params.doctorate_firstSupervisor_givenName as String

            data.doct_firstSV_title = params.doctorate_firstSupervisor_title ?: null
            data.doct_firstSV_faculty = params.doctorate_firstSupervisor_faculty ?: null
            data.doct_firstSV_field = params.doctorate_firstSupervisor_field ?: null

            return true
        }
        flash.errors << message(code: 'portal.error.allMandatory')
        return false
    }

    private secondSupervisor() {
        if (params.is_extern) {
            //extern
            if (params.doctorate_secondSupervisor_sn && params.doctorate_secondSupervisor_givenName) {
                data.doct_secSV_isExtern = true
                data.doct_secSV_sn = params.doctorate_secondSupervisor_sn as String
                data.doct_secSV_givenName = params.doctorate_secondSupervisor_givenName as String

                data.doct_secSV_title = params.doctorate_secondSupervisor_title ?: null
                data.doct_secSV_field = params.doctorate_secondSupervisor_field ?: null
                data.doct_secSV_faculty = params.doctorate_secondSupervisor_faculty ?: null

                data.doct_secSV_pvzID = null
                data.doct_secSV_person = null

                return true
            }
        } else {
            //intern
            if (params.intern_pvz_id) {
                data.doct_secSV_isExtern = false
                data.doct_secSV_pvzID = params.intern_pvz_id as String
                data.doct_secSV_person = researchersService.getById(data.doct_secSV_pvzID)

                data.doct_secSV_sn = null
                data.doct_secSV_givenName = null
                data.doct_secSV_title = null
                data.doct_secSV_field = null
                data.doct_secSV_faculty = null

                return true
            }
        }
        flash.errors << message(code: 'portal.error.allMandatory')
        return false
    }

    private summary() {
        int id = doctoratesService.createDoctorateComplete(data)
        if (id >= 0) {
            data.id = id
            return true
        } else {
            flash.errors << message(code: 'error.general')
            return false
        }
    }
}
