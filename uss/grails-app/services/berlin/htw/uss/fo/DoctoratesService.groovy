package berlin.htw.uss.fo

import grails.plugin.springsecurity.SpringSecurityService
import grails.transaction.Transactional
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

@Transactional
class DoctoratesService {
    def grailsApplication
    def foDataSource
    SpringSecurityService springSecurityService
    ResearchersService researchersService

    /**
     *
     * @return
     */
    boolean canRead(int doctID) {
        true
    }

    /**
     *
     * @return
     */
    boolean canWrite(int doctID) {
        String pvzID = username
        admin || pvzID in getAllIntern(doctID).'pvz_id'
    }

    /* ######### CREATE ########## */

    /**
     *
     * @param doctID
     * @param catID
     * @return
     */
    boolean createAssociatedCategory(int doctID, int catID) {
        withConnection { Sql sql ->
            createAssociatedCategory(sql, doctID, catID)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @param catID
     * @return
     */
    boolean createAssociatedCategory(Sql sql, int doctID, int catID) {
        sql.executeUpdate("INSERT INTO fo.doctorate_categories (doct_id, cat_id) VALUES ($doctID, $catID)")
    }

    /**
     *
     * @param doctID
     * @param data
     * @return
     */
    boolean createAttachment(int doctID, FileDataHolder data) {
        Connection conn = null
        PreparedStatement ps = null
        boolean done = false

        try {
            conn = oldConnection
            if (conn) {
                ps = conn.prepareStatement('INSERT INTO fo.doctorate_attachments (doct_id, attach_category, date, rawdata, size, mime_type, filename) VALUES (?, CAST(? AS fo.attach_types), ?, ?, ?, ?, ?)')
                ps.setInt(1, doctID)
                ps.setString(2, data.attachCat)
                ps.setDate(3, new Date(data.created.getTime()))
                ps.setBinaryStream(4, data.optInputStream, data.fileSize as int)
                ps.setLong(5, data.fileSize)
                ps.setString(6, data.mimeType)
                ps.setString(7, data.fileName)
                ps.execute()
                done = true
            }
        } catch (all) {
            all.printStackTrace()
        } finally {
            data?.optInputStream?.close()
            ps?.close()
            conn?.close()
        }
        done
    }

    /**
     *
     * @param data
     * @return
     */
    int createDoctorate(DoctorateDataHolder data) {
        withConnection { Sql sql ->
            createDoctorate(sql, data)
        } as int
    }

    /**
     *
     * @param sql
     * @param data
     * @return
     */
    int createDoctorate(Sql sql, DoctorateDataHolder data) {
        List params = [
                data.created,
                data.lastmodified,
                data.accepted,
                data.finished,
                data.title,
                data.status,
                data.doctorandSN,
                data.doctorandGivenName,
                data.doctorandUniName,
                data.doctorandGradDate,
                data.universityName,
                data.universityCategory,
                data.universityLocation,
                data.mainFinancing,
                data.secondFinancing
        ]
        sql.firstRow("INSERT INTO fo.doctorate (created, lastmodified, accepted, finished, title, status, doctorand_sn, doctorand_givenname, doctorand_university_name, doctorand_graduation_date, university_name, university_category, university_place, main_financing, second_financing)" +
                " VALUES (?, ?, ?, ?, ?, CAST(? AS fo.status_types), ?, ?, ?, ?, ?, CAST(? AS fo.uni_categories), ?, CAST(? AS fo.financing_types), CAST(? AS fo.financing_types)) RETURNING id", params).id
    }

    /**
     *
     * @param data
     * @return
     */
    int createDoctorateComplete(data) {
        def params = []
        int id = -99

        withTransaction { Sql sql ->
            //Promotionen
            DoctorateDataHolder doctData = new DoctorateDataHolder()
            doctData.with {
                created = new java.util.Date().toTimestamp()
                lastmodified = new java.util.Date().toTimestamp()
                accepted = data.doct_acceptDate ?: null
                finished = data.doct_endDate ?: null
                title = data.doct_title
                status = data.doct_status
                doctorandSN = data.doctorand_sn
                doctorandGivenName = data.doctorand_givenName
                doctorandUniName = data.doctorand_hightestGrad_uniName
                doctorandGradDate = data.doctorand_hightestGrad_date
                universityName = data.doct_uniName
                universityCategory = data.doct_uniCategory
                universityLocation = data.doct_uniPlace
                mainFinancing = data.doct_mainFinancing
                secondFinancing = data.doct_secFinancing ?: null
            }
            id = createDoctorate(sql, doctData)

            //Wissenschaftskategorien
            data.doct_categories.each { int cat ->
                createAssociatedCategory(sql, id, cat)
            }

            //Abschlüsse
            data.doctorand_graduations.each { String grad ->
                createGraduation(sql, id, grad)
            }

            //Erstbetreuer
            PersonDataHolder personDataFirstSV = new PersonDataHolder()
            personDataFirstSV.with {
                supervisionCategory = grailsApplication.config.'fo_doctorates'.'extern_categories'.'sv1' as String
                sn = data.doct_firstSV_sn
                givenName = data.doct_firstSV_givenName
                title = data.doct_firstSV_title ?: null
                faculty = data.doct_firstSV_faculty ?: null
                field = data.doct_firstSV_field ?: null
            }
            createExtern(sql, id, personDataFirstSV)

            //Zweitbetreuer
            if (data.doct_secSV_isExtern) {
                PersonDataHolder personDataSecSV = new PersonDataHolder()
                personDataSecSV.with {
                    supervisionCategory = grailsApplication.config.'fo_doctorates'.'extern_categories'.'sv2' as String
                    sn = data.doct_secSV_sn
                    givenName = data.doct_secSV_givenName
                    title = data.doct_secSV_title ?: null
                    faculty = data.doct_secSV_faculty ?: null
                    field = data.doct_secSV_field ?: null
                }
                createExtern(sql, id, personDataSecSV)
            } else {
                createIntern(sql, id, grailsApplication.config.'fo_doctorates'.'extern_categories'.'sv2' as String, data.doct_secSV_pvzID as String)
            }
        }
        id
    }

    /**
     *
     * @param doctID
     * @param data
     * @return
     */
    boolean createExtern(int doctID, PersonDataHolder data) {
        withConnection { Sql sql ->
            createExtern(sql, doctID, data)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @param data
     * @return
     */
    boolean createExtern(Sql sql, int doctID, PersonDataHolder data) {
        sql.executeUpdate("INSERT INTO fo.doctorate_people_extern (doct_id, supervision_category, sn, givenname, title, faculty, field) VALUES ($doctID, CAST(${data.supervisionCategory} AS supervision_types), ${data.sn}, ${data.givenName}, ${data.title}, ${data.faculty}, ${data.field})") > 0
    }

    /**
     *
     * @param doctID
     * @param gradType
     * @return
     */
    boolean createGraduation(int doctID, String gradType) {
        withConnection { Sql sql ->
            createGraduation(sql, doctID, gradType)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @param gradType
     * @return
     */
    boolean createGraduation(Sql sql, int doctID, String gradType) {
        sql.executeUpdate("INSERT INTO fo.doctorate_graduations (doct_id, type) VALUES ($doctID, CAST($gradType AS fo.graduation_types))") > 0
    }

    /**
     *
     * @param doctID
     * @param svCat
     * @param pvzID
     * @return
     */
    boolean createIntern(int doctID, String svCat, String pvzID) {
        withConnection { Sql sql ->
            createIntern(sql, doctID, svCat, pvzID)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @param svCat
     * @param pvzID
     * @return
     */
    boolean createIntern(Sql sql, int doctID, String svCat, String pvzID) {
        sql.executeUpdate("INSERT INTO fo.doctorate_people_intern (doct_id, supervision_category, pvz_id) VALUES ($doctID, CAST($svCat AS supervision_types), $pvzID)") > 0
    }

    /**
     *
     * @param data
     * @return
     */
    boolean createPublished(PublishedDataHolder data) {
        withConnection { Sql sql ->
            createPublished(sql, data)
        }
    }

    /**
     *
     * @param sql
     * @param data
     * @return
     */
    boolean createPublished(Sql sql, PublishedDataHolder data) {
        List params = [data.doctID, data.author, data.title, data.place, data.isbn, data.date, data.optLink]
        sql.executeUpdate("INSERT INTO fo.doctorate_published (doct_id, author, title, place, isbn, published_date, link) VALUES (?, ?, ?, ?, ?, ?, ?)", params) > 0
    }

    /**
     *
     * @param res
     * @param attCat
     * @return
     */
    FileDataHolder resultToFileDataHolder(res, String attCat) {
        FileDataHolder tmpFileData = null

        if (res) {
            tmpFileData = new FileDataHolder()
            tmpFileData.with {
                fileName = res.filename
                attachCat = attCat
                created = res.date
                fileSize = res.size
                prettySize = sizeToPrettySize(fileSize)
                mimeType = res.mime_type
            }
        }

        tmpFileData
    }


    /* ######### DELETE ########## */

    /**
     *
     * @param doctID
     * @param attachCat
     * @return
     */
    boolean deleteAttachment(int doctID, String attachCat) {
        withConnection { Sql sql ->
            deleteAttachment(sql, doctID, attachCat)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @param attachCat
     * @return
     */
    boolean deleteAttachment(Sql sql, int doctID, String attachCat) {
        sql.executeUpdate("DELETE FROM fo.doctorate_attachments WHERE doct_id = $doctID AND attach_category = CAST($attachCat as fo.attach_types)") > 0
    }

    /**
     *
     * @param doctID
     * @return
     */
    boolean deleteCategories(int doctID) {
        withConnection { Sql sql ->
            deleteCategories(sql, doctID)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @return
     */
    boolean deleteCategories(Sql sql, int doctID) {
        sql.executeUpdate("DELETE FROM fo.doctorate_categories WHERE doct_id = $doctID") > 0
    }

    /**
     *
     * @param doctID
     * @return
     */
    boolean deleteDoctorate(int doctID) {
        withConnection { Sql sql ->
            deleteDoctorate(sql, doctID)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @return
     */
    boolean deleteDoctorate(Sql sql, int doctID) {
        sql.executeUpdate("DELETE FROM fo.doctorate WHERE id = $doctID") > 0
    }

    /**
     * Method for deleting all data associated with given doctorate ignoring the 'inheritance prevents ON DELETE CASCADE' bug
     * @param doctID
     * @return
     */
    boolean deleteDoctorateComplete(int doctID) {
        withTransaction { Sql sql ->
            if (getPersons(sql, doctID)) {
                deletePersons(sql, doctID)
            }

            deleteDoctorate(sql, doctID)
        }

    }

    /**
     *
     * @param doctID
     * @return
     */
    boolean deleteGraduations(int doctID) {
        withConnection { Sql sql ->
            deleteGraduations(sql, doctID)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @return
     */
    boolean deleteGraduations(Sql sql, int doctID) {
        sql.executeUpdate("DELETE FROM fo.doctorate_graduations WHERE doct_id = $doctID") > 0
    }

    /**
     *
     * @param doctID
     * @param svCat
     * @return
     */
    boolean deletePerson(int doctID, String svCat) {
        withConnection { Sql sql ->
            deletePerson(sql, doctID, svCat)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @param svCat
     * @return
     */
    boolean deletePerson(Sql sql, int doctID, String svCat) {
        sql.executeUpdate("DELETE FROM fo.doctorate_people WHERE doct_id = $doctID AND supervision_category = CAST($svCat AS fo.supervision_types);") > 0
    }

    /**
     * Method for deleting all persons associated with a given doctorate
     * Necessary because inheritance between the people tables prevents ON DELETE CASCADE
     * @param doctID
     * @return
     */
    boolean deletePersons(int doctID) {
        withConnection { Sql sql ->
            deletePersons(sql, doctID)
        }
    }

    /**
     * Method for deleting all persons associated with a given doctorate
     * Necessary because inheritance between the people tables prevents ON DELETE CASCADE
     * @param sql
     * @param doctID
     * @return
     */
    boolean deletePersons(Sql sql, int doctID) {
        sql.executeUpdate("DELETE FROM fo.doctorate_people WHERE doct_id = $doctID") > 0
    }

    /**
     *
     * @param doctID
     * @return
     */
    boolean deletePublished(int doctID) {
        withConnection { Sql sql ->
            deletePublished(sql, doctID)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @return
     */
    boolean deletePublished(Sql sql, int doctID) {
        sql.executeUpdate("DELETE FROM fo.doctorate_published WHERE doct_id = $doctID") > 0
    }


    /* ######### GET ########## */

    /**
     *
     * @param doctID
     * @return
     */
    List getAllAssociatedCategories(int doctID) {
        List res = []
        withConnection { Sql sql ->
            sql.eachRow("SELECT * FROM fo.doctorate_categories dc, public.categories c WHERE dc.cat_id = c.category_id AND dc.doct_id = $doctID") {
                Map tmp = [:]
                tmp.put('catID', it.cat_id)
                tmp.put('cat', it.category)
                res.add(tmp)
            }
        }
        res
    }

    /**
     *
     * @return
     */
    boolean getAdmin() {
        springSecurityService.principal.username in ['rack', 'keller']
    }

    /**
     * ATTENTION: use allCategories instead of getAllCategories to prevent MethodNotFound Exception
     * @return
     */
    List getAllCategories() {
        List res = []
        withConnection { Sql sql ->
            sql.eachRow("SELECT * FROM categories") {
                Map tmp = [:]
                tmp.put('catID', it.category_id)
                tmp.put('cat', it.category)
                res.add(tmp)
            }
        }
        res
    }

    /**
     * Method for getting all associated external persons for given doctorate id
     * @param id
     * @return
     */
    List<GroovyRowResult> getAllExtern(int id) {
        withConnection { Sql sql ->
            sql.rows("SELECT * from fo.doctorate_people_extern WHERE doct_id = $id")
        } as List
    }

    /**
     * Method for getting all associated internal persons for given doctorate id
     * @param id
     * @return
     */
    List<GroovyRowResult> getAllIntern(int id) {
        withConnection { Sql sql ->
            sql.rows("SELECT * from fo.doctorate_people_intern WHERE doct_id = $id")
        } as List
    }

    /**
     *
     * @return
     */
    List<String> getAllAttachmentTypes() {
        withConnection { Sql sql ->
            sql.rows("SELECT unnest(enum_range(NULL::fo.attach_types))")
        }.'unnest' as List<String>
    }

    /**
     *
     * @return
     */
    List<String> getAllFinanceTypes() {
        withConnection { Sql sql ->
            sql.rows("SELECT unnest(enum_range(NULL::fo.financing_types))")
        }.'unnest' as List<String>
    }

    /**
     *
     * @return
     */
    List<String> getAllGraduationTypes() {
        withConnection { Sql sql ->
            sql.rows("SELECT unnest(enum_range(NULL::fo.graduation_types))")
        }.'unnest' as List<String>
    }

    /**
     *
     * @return
     */
    List<String> getAllStatusTypes() {
        withConnection { Sql sql ->
            sql.rows("SELECT unnest(enum_range(NULL::fo.status_types))")
        }.'unnest' as List<String>
    }

    /**
     *
     * @return
     */
    List<String> getAllSupervisionTypes() {
        withConnection { Sql sql ->
            sql.rows("SELECT unnest(enum_range(NULL::fo.supervision_types))")
        }.'unnest' as List<String>
    }

    /**
     *
     * @return
     */
    List<String> getAllUniversityCategories() {
        withConnection { Sql sql ->
            sql.rows("SELECT unnest(enum_range(NULL::fo.uni_categories))")
        }.'unnest' as List<String>
    }

    /**
     *
     * @param doctID
     * @return
     */
    List<String> getAssociatedSupervisionTypes(int doctID) {
        withConnection { Sql sql ->
            sql.rows("SELECT supervision_category FROM fo.doctorate_people WHERE doct_id = $doctID")
        }.'supervision_category' as List<String>
    }

    /**
     * Method for getting a specific attachment
     * Caution: Just for the purpose of getting all information except the file itself, use 'getAttachmentComplete' for this.
     * @param doctID ID of the associated doctorate
     * @param attCat Identifier for the wanted attachment category
     * @return
     */
    FileDataHolder getAttachment(int doctID, String attCat) {
        FileDataHolder tmpFile = null

        withConnection { Sql sql ->
            def res = sql.firstRow("SELECT doct_id, attach_category, date, size, mime_type, filename FROM fo.doctorate_attachments WHERE doct_id = $doctID AND attach_category = CAST($attCat as fo.attach_types)")
            if (res) {
                tmpFile = resultToFileDataHolder(res, attCat)

            }
        }
        tmpFile
    }

    /**
     * Method for getting an attachment with preparedStatement
     * Necessary for using Streams while reading the file data from database
     * @param doctID
     * @param attCat
     * @return
     */
    FileDataHolder getAttachmentComplete(int doctID, String attCat) {
        FileDataHolder tmp = null
        Connection conn = null
        PreparedStatement ps = null

        try {
            conn = oldConnection
            if (conn) {
                ps = conn.prepareStatement('SELECT * FROM fo.doctorate_attachments WHERE doct_id = ? AND attach_category = CAST(? AS fo.attach_types)')
                ps.setInt(1, doctID)
                ps.setString(2, attCat)

                ResultSet resultSet = ps.executeQuery()

                if (resultSet.next()) {
                    tmp = new FileDataHolder()
                    String name = resultSet.getString('filename')
                    String extension = null

                    if (name.contains('.')) {
                        extension = name.substring(name.lastIndexOf('.'), name.length())
                        name = name.substring(0, name.lastIndexOf('.'))
                    }

                    File tmpFile = File.createTempFile(name, extension)
                    tmpFile.deleteOnExit()
                    tmpFile.withOutputStream { stream ->
                        stream << resultSet.getBinaryStream('rawdata')
                    }

                    tmp.with {
                        fileName = resultSet.getString('filename')
                        attachCat = attCat
                        created = resultSet.getTimestamp('date')
                        optOutputFile = tmpFile
                        fileSize = resultSet.getLong('size')
                        prettySize = sizeToPrettySize(fileSize)
                        mimeType = resultSet.getString('mime_type')
                    }
                }
            }
        } catch (all) {
            all.printStackTrace()
        } finally {
            ps?.close()
            conn?.close()
        }
        tmp
    }

    /**
     * Method for getting all attachments associated to a given doctorate id
     * Caution: You will only get all meta-information for the files but not the byte data itself
     * @param id
     * @return
     */
    List<FileDataHolder> getAttachments(int id) {
        List tmp = []

        withConnection { Sql sql ->
            sql.eachRow("SELECT * FROM fo.doctorate_attachments WHERE doct_id = $id") {
                tmp.add(resultToFileDataHolder(it, it.'attach_category' as String))
            }
        }
        tmp
    }

    String getCategoryLabel(int catID) {
        withConnection { Sql sql ->
            sql.firstRow("SELECT c.category AS label FROM public.categories c WHERE c.category_id = $catID")?.label
        }
    }

    /**
     *
     * @return
     */
    List<Map> getCategoryStats() {
        List res = []

        withConnection { Sql sql ->
            sql.eachRow("SELECT count(dc.doct_id) as count, dc.cat_id as catID, c.category as catLabel FROM fo.doctorate_categories dc, public.categories c WHERE dc.cat_id = c.category_id GROUP BY dc.cat_id, c.category ORDER BY c.category") {
                Map tmp = [:]
                tmp.'catID' = it.catID
                tmp.'catLabel' = it.catLabel
                tmp.'count' = it.count
                res.add(tmp)
            }
        }
        res
    }

    /**
     *
     * @param id
     * @return
     */
    Map getDoctorate(int id) {
        withConnection { Sql sql ->
            sql.firstRow("SELECT * FROM fo.doctorate WHERE id = $id")
        } as Map
    }

    /**
     *
     * @param id
     * @return
     */
    Map getDoctorateComplete(int id) {
        Map result = getDoctorate(id)
        if (result) {
            List tmp = []

            result.graduations = getGraduations(id)
            result.categories = getAllAssociatedCategories(id)

            if (isPublished(id)) {
                result.published = getPublished(id)
            }

            result.persons = []
            tmp = getAllExtern(id)

            if (tmp != []) {
                result.persons = tmp
            }
            tmp = []

            getAllIntern(id).each {
                Map personTmp = researchersService.getById(it.'pvz_id' as String) as Map
                personTmp.'supervision_category' = it.'supervision_category'
                personTmp.'doct_id' = it.'doct_id'
                tmp += personTmp
            }
            if (tmp != []) {
                result.persons += tmp
            }
        }

        result
    }

    /**
     * Method for getting all stored doctorates no matter the associated persons or authorities
     * @return List < GroovyRowResult >
     */
    List<Map> getDoctoratesOrderByCreated(int max, int offset) {
        List res = []
        withConnection { Sql sql ->
            //Mit rows + each zu arbeiten ist hier einfach, da eachRow ein Proxy Objekt zurück gibt, wo man das Mapping komplett von Hand machen müsste
            sql.rows("SELECT * FROM fo.doctorate ORDER BY created DESC LIMIT $max OFFSET $offset").each {
                Map tmp = it as Map
                tmp.canWrite = canWrite(it.id)
                res.add(tmp)
            }
        }
        println res
        res
    }

    List<Map> getDoctoratesOrderById(int max, int offset) {
        List res = []
        withConnection { Sql sql ->
            //Mit rows + each zu arbeiten ist hier einfach, da eachRow ein Proxy Objekt zurück gibt, wo man das Mapping komplett von Hand machen müsste
            sql.rows("SELECT * FROM fo.doctorate ORDER BY id LIMIT $max OFFSET $offset").each {
                Map tmp = it as Map
                tmp.canWrite = canWrite(it.id)
                res.add(tmp)
            }
        }
        println res
        res
    }



    /**
     *
     * @param pvzID
     * @return
     */
    List<GroovyRowResult> getDoctoratesOrderByCreated(String pvzID, int max, int offset) {
        withConnection { Sql sql ->
            sql.rows("SELECT * FROM fo.doctorate d, fo.doctorate_people_intern dpi WHERE d.id = dpi.doct_id AND dpi.pvz_id = $pvzID ORDER BY d.created DESC LIMIT $max OFFSET $offset")
        } as List
    }

    /**
     *
     * @param catID
     * @return
     */
    ArrayList<Map> getDoctoratesOrderByCreated(int catID, int max, int offset) {
        List res = []
        withConnection { Sql sql ->
           sql.rows("SELECT * FROM fo.doctorate d, fo.doctorate_categories dc, public.categories c WHERE d.id = dc.doct_id AND dc.cat_id = c.category_id AND dc.cat_id = $catID  ORDER BY d.created DESC LIMIT $max OFFSET $offset").each {
               Map tmp = it as Map
               tmp.canWrite = canWrite(it.id)
               res.add(tmp)
           }
        }
        res
    }

    List<Map> getDoctoratesCompleteOrderByCreated(int max, int offset) {
        List res = []
        withConnection { Sql sql ->
            sql.eachRow("SELECT id FROM fo.doctorate ORDER BY created DESC LIMIT $max OFFSET $offset") {
                res.add(getDoctorateComplete(it.id))
            }
        }
        res
    }

    List<Map> getDoctoratesCompleteOrderById(int max, int offset) {
        List res = []
        withConnection { Sql sql ->
            sql.eachRow("SELECT id FROM fo.doctorate ORDER BY id LIMIT $max OFFSET $offset") {
                res.add(getDoctorateComplete(it.id))
            }
        }
        res
    }

    /**
     *
     * @param pvzID
     * @return
     */
    List<Map> getDoctoratesCompleteOrderByCreated(String pvzID, int max, int offset) {
        List res = []
        withConnection { Sql sql ->
            sql.eachRow("SELECT d.id FROM fo.doctorate d, fo.doctorate_people_intern dpi WHERE d.id = dpi.doct_id AND dpi.pvz_id = $pvzID ORDER BY d.created DESC LIMIT $max OFFSET $offset") {
                res.add(getDoctorateComplete(it.id))
            }
        }
        res
    }

    /**
     *
     * @param pvzID
     * @return
     */
    List<Map> getDoctoratesCompleteOrderByCreated(int catID, int max, int offset) {
        List res = []
        withConnection { Sql sql ->
            sql.eachRow("SELECT d.id FROM fo.doctorate d, fo.doctorate_categories dc WHERE d.id = dc.doct_id AND dc.cat_id = $catID ORDER BY d.created DESC LIMIT $max OFFSET $offset") {
                res.add(getDoctorateComplete(it.id))
            }
        }
        res
    }

    /**
     *
     * @param pvzID
     * @return
     */
    Integer getDoctoratesCount() {
        withConnection { Sql sql ->
            sql.firstRow("SELECT count(id) AS count FROM fo.doctorate").count
        } as Integer
    }

    /**
     *
     * @param pvzID
     * @return
     */
    Integer getDoctoratesCount(Integer catID) {
        withConnection { Sql sql ->
            sql.firstRow("SELECT count(doct_id) AS count FROM fo.doctorate_categories WHERE cat_id = $catID").count
        } as Integer
    }

    /**
     *
     * @param pvzID
     * @return
     */
    Integer getDoctoratesCount(String pvzID) {
        withConnection { Sql sql ->
            sql.firstRow("SELECT count(doct_id) AS count FROM fo.doctorate_people_intern WHERE pvz_id = $pvzID").count
        } as Integer
    }

    /**
     *
     * @param id
     * @return
     */
    List<String> getGraduations(int id) {
        withConnection { Sql sql ->
            sql.rows("SELECT type FROM fo.doctorate_graduations WHERE doct_id = $id")
        }.type as List<String>
    }

    /**
     *
     * @param doctID
     * @param svCat
     * @return
     */
    Map getPerson(int doctID, String svCat) {
        def person = withConnection { Sql sql ->
            sql.firstRow("SELECT * FROM fo.doctorate_people_intern WHERE doct_id = $doctID AND supervision_category = CAST($svCat AS fo.supervision_types)")
        }

        if (!person) {
            person = withConnection { Sql sql ->
                sql.firstRow("SELECT * FROM fo.doctorate_people_extern WHERE doct_id = $doctID AND supervision_category = CAST($svCat AS fo.supervision_types)")
            }
        } else {
            person += researchersService.getById(person.'pvz_id' as String)
        }
        person as Map
    }

    /**
     *
     * @param doctID
     * @return
     */
    List<GroovyRowResult> getPersons(int doctID) {
        withConnection { Sql sql ->
            getPersons(sql, doctID)
        } as List
    }

    /**
     *
     * @param sql
     * @param doctID
     * @return
     */
    List<GroovyRowResult> getPersons(Sql sql, int doctID) {
        sql.rows("SELECT * FROM fo.doctorate_people WHERE doct_id = $doctID")
    }

    List<Map> getPersonStats() {
        withConnection { Sql sql ->
            sql.rows("SELECT p.sn, p.givenname, p.active, count(dpi.doct_id) as count FROM fo.doctorate_people_intern dpi, pvz.persons p WHERE p.person_id = dpi.pvz_id GROUP BY p.sn, p.givenname, p.active")
        }.collect { it as Map }.toList()
    }

    /**
     *
     * @param doctID
     * @return
     */
    GroovyRowResult getPublished(int doctID) {
        withConnection { Sql sql ->
            getPublished(sql, doctID)
        } as GroovyRowResult
    }

    /**
     *
     * @param sql
     * @param doctID
     * @return
     */
    GroovyRowResult getPublished(Sql sql, int doctID) {
        sql.firstRow("SELECT * FROM fo.doctorate_published WHERE doct_id = $doctID")
    }

    /**
     *
     * @return
     */
    private String getUsername() {
        springSecurityService.principal.data.person.pvzId.toString()
    }

    /**
     *
     * @param doctID
     * @return
     */
    List<String> getUnusedSupervisionTypes(int doctID) {
        List<String> allTypes = getAllSupervisionTypes()
        List<String> usedTypes = getAssociatedSupervisionTypes(doctID)

        (allTypes - usedTypes)
    }

    /**
     *
     * @param sql
     * @return
     */
    List<Map<String, Integer>> getYearStats(String pvzID = null, Integer catID = null) {
        withConnection { Sql sql ->
            if (!pvzID && !catID) {
                sql.rows("""
select y jahr,
(select count(doct.id) FROM fo.doctorate doct WHERE extract(year from doct.accepted) <= y AND extract(year from doct.finished) >= y) zaehler
from generate_series((SELECT min(extract(year from accepted))::int from fo.doctorate), (SELECT max(extract(year from finished))::int from fo.doctorate)) y
""")
            } else if (!pvzID && catID) {
                sql.rows("""
WITH joined_doct AS (SELECT * FROM fo.doctorate doct, fo.doctorate_categories dc WHERE doct.id = dc.doct_id AND dc.cat_id = $catID)
select y jahr,
(select count(joined_doct.id) FROM joined_doct WHERE extract(year from joined_doct.accepted) <= y AND extract(year from joined_doct.finished) >= y) zaehler
from generate_series((SELECT min(extract(year from accepted))::int from fo.doctorate), (SELECT max(extract(year from finished))::int from fo.doctorate)) y
""")
            } else if (pvzID && !catID) {
                sql.rows("""
WITH joined_doct AS (SELECT * FROM fo.doctorate doct, fo.doctorate_people_intern p WHERE doct.id = p.doct_id AND p.pvz_id = $pvzID)
select y jahr,
(select count(doct.id) FROM joined_doct doct WHERE extract(year from doct.accepted) <= y AND extract(year from doct.finished) >= y) zaehler
from generate_series((SELECT min(extract(year from accepted))::int from fo.doctorate), (SELECT max(extract(year from finished))::int from fo.doctorate)) y
""")

            } else if (pvzID && catID) {
                //bis jetzt noch nicht vorgesehen
                []
            }
        } as List
    }

    /**
     *
     * @param doctID
     * @return
     */
    boolean isPublished(int doctID) {
        withConnection { Sql sql ->
            sql.firstRow("SELECT doct_id FROM fo.doctorate_published WHERE doct_id = $doctID") != null
        }
    }


    /* ######### UPDATE ########## */

    /**
     *
     * @param sql
     * @param doctID
     * @param categories
     * @return
     */
    boolean updateCategories(int doctID, List<Integer> categories) {
        withConnection { Sql sql ->
            deleteCategories(sql, doctID)
            categories.each {
                createAssociatedCategory(sql, doctID, it)
            }
            true
        }

    }

    /**
     *
     * @param sql
     * @param doctID
     * @param categories
     * @return
     */
    boolean updateCategories(Sql sql, int doctID, List<Integer> categories) {
        deleteCategories(sql, doctID)
        categories.each {
            createAssociatedCategory(sql, doctID, it)
        }
        true
    }

    /**
     *
     * @param doctID
     * @param data
     * @return
     */
    boolean updateExtern(int doctID, PersonDataHolder data) {
        withTransaction { Sql sql ->
            updateExtern(sql, doctID, data)
        }
    }

    /**
     *
     * @param doctID
     * @param data
     * @return
     */
    boolean updateExtern(Sql sql, int doctID, PersonDataHolder data) {
            deletePerson(sql, doctID, data.supervisionCategory)
            createExtern(sql, doctID, data)
    }

    /**
     *
     * @param doctID
     * @param data
     * @return
     */
    boolean updateGeneral(int doctID, GeneralDataHolder data) {
        withConnection { Sql sql ->
            updateGeneral(sql, doctID, data)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @param data
     * @return
     */
    boolean updateGeneral(Sql sql, int doctID, GeneralDataHolder data) {
        sql.executeUpdate("UPDATE fo.doctorate SET title = $data.title, status = CAST($data.status AS fo.status_types), accepted = $data.accepted, finished = $data.finished WHERE id = $doctID") > 0
    }

    /**
     *
     * @param doctID
     * @param data
     * @param fileData
     * @return
     */
    boolean updateGeneralComplete(int doctID, GeneralDataHolder data, FileDataHolder fileData) {
        try {
            boolean attachDone = false

            if (fileData) {
                if (getAttachment(doctID, fileData.attachCat)) {
                    deleteAttachment(doctID, fileData.attachCat)
                }
                attachDone = createAttachment(doctID, fileData)
            } else {
                attachDone = true
            }

            if (attachDone) {
                withTransaction { Sql sql ->
                    updateGeneral(sql, doctID, data)
                    updateCategories(sql, doctID, data.categories)
                } && attachDone
            } else {
                false
            }
        } catch (all) {
            all.printStackTrace()
            false
        }
    }

    /**
     *
     * @param doctID
     * @param svCat
     * @param pvzID
     * @return
     */
    boolean updateIntern(int doctID, String svCat, String pvzID) {
        withTransaction { Sql sql ->
            updateIntern(sql, doctID, svCat, pvzID)
        }
    }

    /**
     *
     * @param doctID
     * @param svCat
     * @param pvzID
     * @return
     */
    boolean updateIntern(Sql sql, int doctID, String svCat, String pvzID) {
        deletePerson(sql, doctID, svCat)
        createIntern(sql, doctID, svCat, pvzID)
    }

    /**
     *
     * @param doctID
     * @param data
     * @return
     */
    boolean updatePromovend(int doctID, PromovendDataHolder data) {
        withConnection { Sql sql ->
            updatePromovend(sql, doctID, data)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @param data
     * @return
     */
    boolean updatePromovend(Sql sql, int doctID, PromovendDataHolder data) {
        sql.executeUpdate("UPDATE fo.doctorate SET doctorand_sn = $data.sn, doctorand_givenname = $data.givenName, doctorand_university_name = $data.hightestGradUniName, doctorand_graduation_date = $data.hightestGradDate WHERE id = $doctID") > 0
    }

    /**
     *
     * @param doctID
     * @param data
     * @param fileData
     * @return
     */
    boolean updatePromovendComplete(int doctID, PromovendDataHolder data, FileDataHolder fileData) {
        try {
            boolean attachDone = false

            if (fileData) {
                if (getAttachment(doctID, fileData.attachCat)) {
                    deleteAttachment(doctID, fileData.attachCat)
                }
                attachDone = createAttachment(doctID, fileData)
            } else {
                attachDone = true
            }

            if (attachDone) {
                withTransaction { Sql sql ->
                    deleteGraduations(sql, doctID)
                    data.graduations.each { String graduation ->
                        createGraduation(sql, doctID, graduation)
                    }
                    updatePromovend(sql, doctID, data)
                } && attachDone
            } else {
                false
            }
        } catch (all) {
            all.printStackTrace()
            false
        }

    }

    /**
     *
     * @param data
     * @param fileData
     * @return
     */
    boolean updatePublishedComplete(PublishedDataHolder data, FileDataHolder fileData) {
        try {
            boolean attachDone = false

            if (fileData) {
                if (getAttachment(data.doctID, fileData.attachCat)) {
                    deleteAttachment(data.doctID, fileData.attachCat)
                }
                attachDone = createAttachment(data.doctID, fileData)
            } else {
                attachDone = true
            }

            if (attachDone) {
                withTransaction { Sql sql ->
                    if (getPublished(sql, data.doctID)) {
                        deletePublished(sql, data.doctID)
                    }
                    createPublished(sql, data)
                } && attachDone
            } else {
                false
            }
        } catch (all) {
            all.printStackTrace()
            false
        }


    }

    /**
     *
     * @param doctID
     * @param data
     * @return
     */
    boolean updateUniAndFinancing(int doctID, UniFinanceDataHolder data) {
        withConnection { Sql sql ->
            updateUniAndFinancing(sql, doctID, data)
        }
    }

    /**
     *
     * @param sql
     * @param doctID
     * @param data
     * @return
     */
    boolean updateUniAndFinancing(Sql sql, int doctID, UniFinanceDataHolder data) {
        sql.executeUpdate("UPDATE fo.doctorate SET university_name = $data.uniName, university_category = CAST($data.uniCategory AS fo.uni_categories), university_place = $data.uniLocation, main_financing = CAST($data.mainFinancing AS fo.financing_types), second_financing = CAST($data.secondFinancing AS fo.financing_types) WHERE id = $doctID") > 0
    }

    /**
     *
     * @param doctID
     * @param data
     * @param fileData
     * @return
     */
    boolean updateUniAndFinancingComplete(int doctID, UniFinanceDataHolder data, FileDataHolder fileData) {
        try {
            boolean attachDone = false

            if (fileData) {
                if (getAttachment(doctID, fileData.attachCat)) {
                    deleteAttachment(doctID, fileData.attachCat)
                }
                attachDone = createAttachment(doctID, fileData)
            } else {
                attachDone = true
            }

            if (attachDone) {
                withTransaction { Sql sql ->
                    updateUniAndFinancing(sql, doctID, data)
                } && attachDone
            } else {
                false
            }
        } catch (all) {
            all.printStackTrace()
            false
        }
    }


    /* important private methods for setting up a proper connection to the database */


    /**
     * Important: To be able to use this method you need to change the name of the database in the grails config
     * @return
     */
    private Connection getOldConnection() {
        Class.forName('org.postgresql.Driver')
        String url = "${foDataSource.url}/${grailsApplication.config.'fo_doctorates'.'db_name'}"
        Properties connProps = new Properties()
        connProps.put('user', foDataSource.user)
        connProps.put('password', foDataSource.password)
        DriverManager.getConnection(url, connProps)
    }

    /**
     *
     * @return
     */
    private Sql getConnection() {
        return Sql.newInstance(foDataSource)
    }

    /**
     *
     * @param action
     * @return
     */
    private withConnection(Closure action) {
        Sql sql = connection

        try {
            def res = action(sql)
            sql.close()
            return res
        } catch (all) {
            sql?.close()
            throw new Exception('Database operation failed.', all)
        }
    }

    /**
     *
     * @param action
     * @return
     */
    private withTransaction(Closure action) {
        Sql sql = connection

        try {
            sql.execute("BEGIN TRANSACTION")
            def res = action(sql)
            sql.execute("COMMIT")
            sql.close()
            return res
        } catch (all) {
            sql?.execute("ROLLBACK")
            sql?.close()
            throw new Exception('Database operation failed.', all)
        }
    }
}
