package berlin.htw.hrz.kb

import grails.core.GrailsApplication
import grails.transaction.Transactional
import org.springframework.web.multipart.MultipartFile

import javax.imageio.ImageIO
import javax.servlet.ServletContext
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.AlphaComposite
import java.awt.RenderingHints

@Transactional
class ImportService {
    GrailsApplication grailsApplication
    ServletContext servletContext

    String preUrl = 'https://portal.rz.htw-berlin.de'
    String cookieCheckPage = 'https://portal.rz.htw-berlin.de/anleitungen.xml'

    void importOldFiles(List<String> oldDataFiles, String cookie) {
        URLConnection conn = null
        oldDataFiles.each { String dataFileLink ->
            try {
                conn = dataFileLink.toURL().openConnection()
                conn.setRequestProperty('Cookie', cookie)
                conn.connect()

                if (conn.responseCode == 200) {
                    println "[INFO] Process data-file '$dataFileLink' ..."

                    String file_path = dataFileLink.replace('https://portal.rz.htw-berlin.de', '')
                    String[] temp = file_path.split(/\/[\w-]+\.[a-z0-9]+/)
                    String parent_path = temp[0]
                    String file_name = dataFileLink.substring(dataFileLink.lastIndexOf('/') + 1, dataFileLink.size())
                    String file_name_hash = "${file_name.encodeAsMD5().toString()}.${file_name.substring(file_name.lastIndexOf('.') + 1, file_name.size())}"

                    println file_name
                    println file_name_hash
                    println file_path

                    File parentFolder = new File(grailsApplication.config.'kb.file.dir' as String, parent_path)
                    if (!parentFolder.exists()) { parentFolder.mkdirs() }

                    File tmpFile = new File(parentFolder, file_name)
                    tmpFile.bytes = conn.content.bytes
                    new File(grailsApplication.config.'kb.temp.dir' as String, file_name_hash).bytes = tmpFile.bytes

                    servletContext['file_mapper'].put(file_path, file_name_hash)

                }
            } catch (Exception e) {
                //todo was passiert hier? throw new Excepton später rausnehmen
                throw new Exception(e.message)
            } finally {
                conn.disconnect()
            }
        }
    }

    void importOldDocs(List<String> oldFiles, String cookie) {
        List<String> errorFiles = []
        URLConnection conn = null

        oldFiles.eachWithIndex{ String myUrl, int index ->
            if (index < 1000) {

                try {
                    println "\n[INFO] Verarbeite URL (${index+1}): '$myUrl' ..."
                    conn = myUrl.toURL().openConnection()
                    conn.setRequestProperty('Cookie', cookie)
                    conn.connect()
                    if (conn.responseCode == 200) {
                        Node result = new XmlParser().parseText(conn.content.text as String)
                        if (result.docType?.text() == 'Tutorial') {
                            println "[INFO] Tutorial mit ${result.steps.step.size()} Schritten gefunden"
                            if (!importTutorial(result, cookie)) {
                                errorFiles.add(myUrl)
                            }
                        } else {
                            println "[INFO] Faq mit ${result.steps.step.size()} Elementen gefunden"
                            if (!importFaq(result, cookie)) {
                                errorFiles.add(myUrl)
                            }
                        }
                    } else {
                        errorFiles.add(myUrl)
                    }

                } catch (Exception e) {
                    errorFiles.add(myUrl)
                    throw new Exception(e.message)
                }
                finally {
                    conn.disconnect()
                }

            }

        }
        println "\n\n[INFO] Es gab ${errorFiles.size()} Fehler beim Importierten. Diese Fehler betrafen:\n"
        errorFiles.each {
            println it
        }
    }

    String getIntro(Node xmlDoc) {
        String temp = ""
        if (xmlDoc.intro && xmlDoc.intro.text()) {
            xmlDoc.intro.section.each {
                temp += "<section>"
                if (it.title.text()) { temp += "<h2>${it.title.text()}</h2>" }
                if (it.text.text()) { temp += asString(it.text as NodeList) }
                temp += "</section>"
            }
        }
        temp
    }

    String cookieGetter(String user, String pass) {
        String temp = ''
        HttpURLConnection conn = 'https://portal.rz.htw-berlin.de/anleitungen.html'.toURL().openConnection() as HttpURLConnection
        conn.requestMethod = 'GET'
        conn.connect()
        if (conn.responseCode.toInteger() == 200) {
            temp = conn.getHeaderField('Set-Cookie')
            temp = temp.substring(0, temp.lastIndexOf(';'))
        }
        conn.disconnect()


        conn = "https://portal.rz.htw-berlin.de/cms/login?username=$user&password=$pass".toString().toURL().openConnection() as HttpURLConnection
        conn.requestMethod = 'GET'
        conn.setRequestProperty('Cookie', temp)
        conn.connect()
        if (conn.responseCode.toInteger() == 200) {
            conn.disconnect()
            temp
        } else {
            conn.disconnect()
            null
        }
    }

    boolean testingCookie (String cookie) {
        URLConnection conn = null
        try {
            conn = cookieCheckPage.toURL().openConnection()
            conn.setRequestProperty('Cookie', cookie)
            conn.connect()
            if (!conn.responseCode != 200) { false }

            def result = new XmlSlurper().parseText(conn.content.text as String)
            if (!(result?.'meta-inf'?.user?.username as String)) { false }
            else { true }
        } catch (Exception e) {
            e.printStackTrace()
            false
        }
    }

    boolean importOldDocs(MultipartFile linkFile, String user, String pass) {
        String cookie = cookieGetter(user, pass)
        boolean cookieGood = testingCookie(cookie)
        if ( cookieGood ) {
            importOldDocs(linkFile.inputStream.readLines().collect {
                it.toString().replace('.xml', '.export')
            }, cookie)
            true
        } else {
            false
        }
    }

    boolean importOldFiles(MultipartFile linkDataFile, String user, String pass) {
        String cookie = cookieGetter(user, pass)
        boolean cookieGood = testingCookie(cookie)
        if ( cookieGood ) {
            importOldFiles(linkDataFile.inputStream.readLines().collect {
                it.toString().replace('.xml', '.export')
            }, cookie)
            true
        } else {
            false
        }
    }

    String getMimeType(URLConnection conn) {
        conn.getHeaderField('Content-Type')
    }

    byte[] imageToBytes(URLConnection conn) {
        conn.inputStream.bytes
    }

    byte[] resizeImage(byte[] stepMedia, String mimeType) {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(stepMedia))
        double scale = grailsApplication.config.getAt('kb.preview.image.width') / img.width
        int newWidth = grailsApplication.config.getAt('kb.preview.image.width')
        int newHeight = img.height * scale

        java.awt.Image resizedImage = img.getScaledInstance(newWidth, newHeight, img.SCALE_SMOOTH)
        BufferedImage bimage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)

        Graphics2D g = bimage.createGraphics()
        g.drawImage(resizedImage, 0, 0, null)
        g.dispose()
        g.setComposite(AlphaComposite.Src)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON)

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ImageIO.write(bimage, mimeType.substring(mimeType.indexOf('/') + 1, mimeType.length()), baos)
        baos.toByteArray()
    }

    Date getDate(Node xmlDoc) {
        String myDate = "${xmlDoc.meta.date.day.text()}/${xmlDoc.meta.date.month.text()}/${xmlDoc.meta.date.year.text()} ${xmlDoc.meta.date.hour.text()}:${xmlDoc.meta.date.minute.text()}:${xmlDoc.meta.date.second.text()}"
        Date.parse('dd/MM/yyyy hh:mm:ss', myDate)
    }

    String[] getTags(Node xmlDoc) {
        String[] myTags = []
        //tags verarbeiten
        xmlDoc.meta.tags.tag.each { tag ->
            myTags += tag.text()
        }
        myTags
    }

    Subcategory getAuthor(Node xmlDoc) {
        String author = xmlDoc.author.text() as String
        //println "author $author"
        if (!author) { author = 'IT-Helpcenter' }
        Subcategory authorNode = Subcategory.findByName(author)
        if (!authorNode) {
            authorNode = new Subcategory(name: author)
            Category.findByName('author')?.addToSubCats(authorNode)?.save(flush:true)
        }
        //println authorNode
        //println authorNode.name
        authorNode.save(flush: true)
    }

    void importArticle(Node xmlDoc) {
        println "importArticle"
        String myContent = ""

        //content

        xmlDoc.docContent.section.each { section ->
            myContent += "\n<section>\n"

            if (section.title && section.title != "" && section.title.size() > 0) { myContent += "<h2>${section.title.text()}</h2>" }
            section.content.each { Node content ->
                myContent += asString(content.children())
            }
            myContent += "\n</section>\n"
            //println XmlUtil.serialize(section.content)
        }

        //println "\n\n\nContent\n$myContent"


    }

    String asString(Node node) {
        StringWriter sw = new StringWriter()
        PrintWriter pw = new PrintWriter(sw)
        XmlNodePrinter np = new XmlNodePrinter(pw)
        np.setPreserveWhitespace(true)
        np.print(node)
        return sw.toString()
    }
    String asString(NodeList nodes) {
        String temp = ""
        nodes.each {
            temp += asString(it)
        }
        temp
    }

    List processSteps(NodeList rawSteps, String cookie, asSidebox = false) {
        println '[INFO] Verarbeite Schritte...'
        List mySteps = []
        rawSteps?.step?.findAll{ it.section.content.text() || it.images }?.eachWithIndex { step, int index ->
            boolean showNumber = false
            int stepNumber
            byte[] stepMedia = null, previewMedia = null
            String stepContent ="", stepTitle = "", altText = "", mimeType = "", css = "", embLink = "", embLinkType = ""
            List<Image> imgsTemp = []

            //println "debug: number"
            if (step.number) {
                stepNumber = (step.number?.text())?.toInteger()
            } else {
                stepNumber = index + 1
            }

            //println "debug: media alt"


            //println "debug: media"
            step.images.image.each {
                //println 'image verarbeiten'
                //println it
                if (it.alt?.text()) {
                    altText = it.alt?.text() as String
                }

                if (it.embLink?.a) {
                    embLink = it.embLink.a.'@href'.text()
                    embLinkType = it.embLink.a.'@type'.text()
                }

                Image img = null
                URL imgUrl = ("$preUrl${it?.imgLink?.text()}" as String).toURL()


                //println 'getMime and imageRaw'
                URLConnection conn = imgUrl.openConnection()
                conn.setRequestProperty('Cookie', cookie)
                conn.connect()

                if (conn.responseCode == 200) {
                    mimeType = getMimeType(conn)
                    stepMedia = imageToBytes(conn)
                    int imgNumber = it.number.text()?.toInteger()

                    //println "number ${it.number.text()?.toInteger()}"
                    //println "numberInt $imgNumber"
                    img = new Image(blob: stepMedia, altText: altText, mimeType: mimeType, preview: null, number: imgNumber, link: embLink, linkType: embLinkType)
                }

                previewMedia = resizeImage(stepMedia, mimeType)

                //println 'resize'
                ImageCached imgc = new ImageCached(blob: previewMedia, altText: altText, mimeType: mimeType)
                if (imgc.validate()) {
                    imgc = imgc.save(flush:true)
                } else {
                    println '[WARNING] ImageCached Element ist nicht valide, Fehlermeldung folgt...'
                    imgc.errors?.allErrors?.each { println it }
                    throw new Exception("Kaputt")
                }

                img.preview = imgc
                if (img.validate()) {
                    img = img.save(flush: true)
                } else {
                    println '[WARNING] Image Element ist nicht valide, Fehlermeldung folgt...'
                    img.errors?.allErrors?.each { println it }
                    throw new Exception("Kaputt")
                }

                //println "number bla $img.number"

                imgsTemp.add(img)

                conn.disconnect()
            }

            //println "debug: title"
            if (step.section.size() > 1) {
                stepTitle = step.section?.find { it.'@number' == '1' }?.title?.text() as String
            } else {
                stepTitle = step.section?.title?.text() as String
            }
            if (stepTitle.startsWith('Schritt')) {
                //println "Lösche Titel prefix"
                showNumber = true
                stepTitle = stepTitle.replaceFirst(/Schritt [0-9]+: /, '')
            }


            //println "debug: content"
            stepContent = ""

            //println "debug: css"
            if (step.classes.css) {
                step.classes.css.each {
                    css += (it.text() == 'Hinweis'?'infobox ':(it.text()=='Graue Box'?'infobox-2 ':''))
                }
                //println "style $css"
            }

            //println "debug: steps"
            step.section?.each { section ->
                String content = asString(section.content[0].children())


                if (section.'@number' && (section.'@number' as int) > 1) {
                    String title = section.title?.text()
                    stepContent += "<section><h2>$title</h2>$content</section>\n" as String
                } else {
                    if (content.startsWith(/[0-9]+. /)) {
                        //println "Lösche content prefix"
                        content = content.replaceFirst(/[0-9]+. /, '')
                    }
                    stepContent += "<section>$content</section>\n" as String
                }

            }

            //println "\nNumber $stepNumber\nTitel $stepTitle\nContent $stepContent\n" +
            //       "\nMediaType $mimeType\nAltText $altText"

            Step myStep = null
            if (asSidebox) {
                myStep = new Sidebox(number: stepNumber, stepTitle: stepTitle, stepText: stepContent, showNumber: showNumber, style: css)
            } else {
                myStep = new Step(number: stepNumber, stepTitle: stepTitle, stepText: stepContent, showNumber: showNumber, style: css)
            }

            imgsTemp.each {
                myStep.addToImages(it)
            }
            myStep

            if (myStep.validate()) {
                mySteps += myStep.save(flush: true)
            } else {
                println '[WARNING] Step Element ist nicht valide, Fehlermeldung folgt...'
                println myStep.errors
                myStep.errors?.allErrors?.each { println it }
                throw new Exception("Kaputt")
            }


        }
        mySteps
    }

    Document validateAndSaveDoc(Document doc) {
        if (!doc.validate()) {

            if ( Document.findByDocTitle(doc.docTitle) ) {

                println "[WARNING] Dokument mit dem Titel '$doc.docTitle' existiert bereits!"
                doc.docTitle = changeDuplicatedTitle(doc.docTitle)
                println "[INFO] Ändere Dokumententitel zu '${doc.docTitle}'..."

                if (doc.validate()) {
                    println "[INFO] Alles ok, Dokument wird gespeichert..."
                    doc.save(flush: true)
                } else {
                    println '[WARNING] Dokument ist nicht valide, Fehlermeldung folgt...'
                    println doc.errors
                    throw new Exception('Testaustieg2, später rausnehmen')
                    null
                }
            }

            if (doc.errors.errorCount > 0 ) {
                println '[WARNING] Dokument ist nicht valide, Fehlermeldung folgt...'
                println doc.errors
                throw new Exception('Testaustieg, später rausnehmen')
                null
            }
        }
        doc.save(flush: true)
    }

    boolean importFaq(Node xmlDoc, String cookie) {
        boolean noError = true
        println '[INFO] Importierte Faq...'
        xmlDoc.steps.step.each { def faq ->
            if (faq.section.title.text()) {
                Document temp = new Faq(docTitle: faq.section.title.text(), question: faq.section.title.text(), answer: asString(faq.section.content), tags: getTags(xmlDoc), createDate: getDate(xmlDoc), changeDate: new Date(), locked: false, viewCount: 0, )
                temp = addSideInfo(xmlDoc, temp, cookie)
                temp = validateAndSaveDoc(temp)
                if (!temp) { noError = false }
                else { linkDocument(xmlDoc, temp) }
            }
        }
        noError
    }

    Document addSideInfo(Node xmlDoc, Document doc, String cookie) {
        if (xmlDoc.aside && (xmlDoc.aside.step.section.content.text() || xmlDoc.aside.step.images)) {
            processSteps(xmlDoc.aside, cookie, true).each {
                doc.addToSideboxes(it)
            }
        }
        doc
    }

    String changeDuplicatedTitle(String oldTitle) {
        int count = 2
        String newTitle = ""
        while (true) {
            newTitle = "$oldTitle ($count)"
            if (!Document.findByDocTitle(newTitle)) {
                break
            }
            count += 1
        }
        newTitle
    }

    boolean importTutorial(Node xmlDoc, String cookie) {
        println '[INFO] Importiere Tutorial...'
        Step[] mySteps = []

        //steps verarbeiten
        //println "debug: steps verarbeiten"
        mySteps = processSteps(xmlDoc.steps, cookie)

        //tut erstellen
        Document myTut = new Tutorial(docTitle: xmlDoc.title.text() as String, locked: false, viewCount: 0, createDate: getDate(xmlDoc), tags: getTags(xmlDoc), mirUrl: xmlDoc.mirurl.text() as String, intro: getIntro(xmlDoc), videoLink: (xmlDoc.video.'@link'.text() as String)?:null)
        //steps hinzufügen
        mySteps.each { step ->
            myTut.addToSteps(step)
        }
        myTut = addSideInfo(xmlDoc, myTut, cookie)
        myTut = validateAndSaveDoc(myTut)
        if (!myTut) { false }
        else {
            linkDocument(xmlDoc, myTut)
            true
        }
    }

    void linkDocument (Node xmlDoc, Document myDoc) {
        new Linker(subcat: getAuthor(xmlDoc), doc: myDoc).save(flush:true)
        new Linker(subcat: Subcategory.findByName('de'), doc: myDoc).save(flush:true)
        
        String mirLink = xmlDoc.mirurl.text()
        List<String> mirLinkParts = mirLink.split('/').toList()

        //os
        //todo eleganter machen
        if (mirLink.contains('windows')) {
            if (mirLinkParts.contains('windowsxp') || mirLinkParts.contains('windows_xp')) {
                println "[INFO] Anleitung für Win xp erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('win_xp'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.contains('windowsvista') || mirLinkParts.contains('windows_vista')) {
                println "[INFO] Anleitung für Win vista erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('win_vista'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.contains('windows7') || mirLinkParts.contains('windows_7')) {
                println "[INFO] Anleitung für Win 7 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('win_7'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.contains('windows8') || mirLinkParts.contains('windows_8')) {
                println "[INFO] Anleitung für Win 8 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('win_8'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.contains('windows10') || mirLinkParts.contains('windows_10')) {
                println "[INFO] Anleitung für Win 10 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('win_10'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.contains('windowsphone') || mirLinkParts.contains('windows_phone')) {
                println "[INFO] Anleitung für Win Phone erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('win_phone'), doc: myDoc).save(flush:true)
            } else {
                new Linker(subcat: Subcategory.findByName('windows'), doc: myDoc).save(flush:true)
            }
        } else if (mirLink.contains('mac_os_x') || mirLink.contains('macosx')) {
            if (mirLinkParts.find { it ==~ /(mac(_)?os?(_)?x?(_)?)?10(_)?4(_[a-z]+)?/}) {
                println "[INFO] Anleitung für OS X 10_4 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('osx_10_4'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.find { it ==~ /(mac(_)?os?(_)?x?(_)?)?10(_)?5(_[a-z]+)?/}) {
                println "[INFO] Anleitung für OS X 10_5 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('osx_10_5'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.find { it ==~ /(mac(_)?os?(_)?x?(_)?)?10(_)?6(_[a-z]+)?/}) {
                println "[INFO] Anleitung für OS X 10_6 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('osx_10_6'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.find { it ==~ /(mac(_)?os?(_)?x?(_)?)?10(_)?7(_[a-z]+)?/}) {
                println "[INFO] Anleitung für OS X 10_7 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('osx_10_7'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.find { it ==~ /(mac(_)?os?(_)?x?(_)?)?10(_)?8(_[a-z]+)?/}) {
                println "[INFO] Anleitung für OS X 10_8 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('osx_10_8'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.find { it ==~ /(mac(_)?os?(_)?x?(_)?)?10(_)?9(_[a-z]+)?/}) {
                println "[INFO] Anleitung für OS X 10_9 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('osx_10_9'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.find { it ==~ /(mac(_)?os?(_)?x?(_)?)?10(_)?10(_[a-z]+)?/}) {
                println "[INFO] Anleitung für OS X 10_10 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('osx_10_10'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.find { it ==~ /(mac(_)?os?(_)?x?(_)?)?10(_)?11(_[a-z]+)?/}) {
                println "[INFO] Anleitung für OS X 10_11 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('osx_10_11'), doc: myDoc).save(flush:true)
            } else {
                new Linker(subcat: Subcategory.findByName('mac'), doc: myDoc).save(flush:true)
            }
        } else if (mirLink.contains('linux')) {
            println "[INFO] Anleitung für Linux erkannt, verlinke Dokument mit der Subkategorie..."
            new Linker(subcat: Subcategory.findByName('linux'), doc: myDoc).save(flush:true)
        } else if (mirLink.contains('ubuntu')) {
            println "[INFO] Anleitung für Ubuntu erkannt, verlinke Dokument mit der Subkategorie..."
            new Linker(subcat: Subcategory.findByName('ubuntu'), doc: myDoc).save(flush:true)
        } else if (mirLink.contains('symbian')) {
            println "[INFO] Anleitung für Symbian erkannt, verlinke Dokument mit der Subkategorie..."
            new Linker(subcat: Subcategory.findByName('symbian'), doc: myDoc).save(flush:true)
        } else if (mirLink.contains('kindle_touch')) {
            println "[INFO] Anleitung für kindle_touch erkannt, verlinke Dokument mit der Subkategorie..."
            new Linker(subcat: Subcategory.findByName('kindle_touch'), doc: myDoc).save(flush:true)
        } else if (mirLink.contains('android')) {
            if (mirLinkParts.contains('android_5_1')) {
                println "[INFO] Anleitung für android 5.1 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('an_5_1'), doc: myDoc).save(flush:true)
            } else {
                println "[INFO] Anleitung für android erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('android'), doc: myDoc).save(flush:true)
            }
        } else if (mirLink.contains('ios') || mirLink.contains('ipod')) {
            if (mirLinkParts.contains('ios7') || mirLinkParts.contains('ios_7')) {
                println "[INFO] Anleitung für ios7 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('ios_7'), doc: myDoc).save(flush:true)
            } else if (mirLinkParts.contains('ios8p')) {
                println "[INFO] Anleitung für ios8 erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('ios_8'), doc: myDoc).save(flush:true)
            } else {
                println "[INFO] Anleitung für ios erkannt, verlinke Dokument mit der Subkategorie..."
                new Linker(subcat: Subcategory.findByName('ios'), doc: myDoc).save(flush:true)
            }
        } else {
            println '[INFO] Es wurde kein Betriebssystem erkannt, überspringe Schritt...'
        }

        //theme
        String subTemp = mirLink.split(/\/anleitungen\//)[1]
        String foundTheme = subTemp.substring(0, (subTemp.indexOf('/') > 0)?subTemp.indexOf('/'):subTemp.size())
        Subcategory cat = Subcategory.findByName(foundTheme)
        if (!cat) {
            println "[INFO] Keine Themen-Subkategorie mit dem Namen '$foundTheme' gefunden, erstelle eine..."
            cat = new Subcategory(name: foundTheme)
            Category.findByName('theme').addToSubCats(cat).save(flush: true)
            cat = cat.save(flush: true)
        }
        println "[INFO] Verlinke Dokument mit der Themen-Subkategorie '$cat.name'..."
        new Linker(subcat: cat, doc: myDoc).save(flush:true)

        //groups
        if (mirLinkParts.contains('lehrende')) {
            println "[INFO] Gruppenzuordnung für 'faculty' gefunden, verlinke..."
            new Linker(subcat: Subcategory.findByName('faculty'), doc: myDoc).save(flush:true)
        } else if (mirLinkParts.contains('studierende')) {
            println "[INFO] Gruppenzuordnung für 'student' gefunden, verlinke..."
            new Linker(subcat: Subcategory.findByName('student'), doc: myDoc).save(flush:true)
        } else {
            println '[INFO] Keine spezifische Gruppenzuordnung gefunden, benutze festgelegte...'

            //todo eleganter machen
            HashMap<String, List<String>> tempAsso = new HashMap<>()
            tempAsso.put('eFormulare', ['anonym', 'student', 'staff', 'faculty'] as List<String>)
            tempAsso.put('groupware', ['staff', 'faculty'] as List<String>)
            tempAsso.put('account', ['anonym', 'student', 'staff', 'faculty'] as List<String>)
            tempAsso.put('kopieren_drucken', ['anonym', 'student', 'staff', 'faculty'] as List<String>)
            tempAsso.put('email', ['anonym', 'student', 'staff', 'faculty'] as List<String>)
            tempAsso.put('speicherplatz', ['student', 'staff', 'faculty'] as List<String>)
            tempAsso.put('vpn', ['anonym', 'student', 'staff', 'faculty'] as List<String>)
            tempAsso.put('web', ['student', 'staff', 'faculty'] as List<String>)
            tempAsso.put('telefonie', ['staff', 'faculty'] as List<String>)
            tempAsso.put('lan', ['anonym', 'student', 'staff', 'faculty'] as List<String>)
            tempAsso.put('wlan', ['anonym', 'student', 'staff', 'faculty'] as List<String>)
            tempAsso.put('cm', ['anonym', 'student', 'staff', 'faculty'] as List<String>)

            println "[INFO] Für Thema '$foundTheme' Gruppenzuordnung '${tempAsso.get(foundTheme).toString()}' gefunden, verlinke..."
            tempAsso.get(foundTheme).each { String group ->
                new Linker(subcat: Subcategory.findByName(group), doc: myDoc).save(flush: true)
            }
        }
    }
}
