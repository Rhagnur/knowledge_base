package berlin.htw.hrz.kb

import grails.transaction.Transactional
import org.springframework.web.multipart.MultipartFile
import sun.awt.image.URLImageSource

import javax.imageio.ImageIO
import java.awt.Graphics2D
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.awt.AlphaComposite
import java.awt.RenderingHints

@Transactional
class ImportService {
    //todo Verlinkung der Anleitungsdokumente untereinander
    //todo videos?
    DocumentService documentService
    String preUrl = 'https://portal.rz.htw-berlin.de'
    String cookieCheckPage = 'https://portal.rz.htw-berlin.de/anleitungen.xml'

    void importOldDocs(List<String> oldFiles, String cookie) {
        //println "importOldDocs: ${oldFiles}"
        List<String> errorFiles = []
        URLConnection conn = null

        oldFiles.eachWithIndex{ String myUrl, int index ->
            if (index < 1000) {

                try {
                    println "\n${index+1} [INFO] Verarbeite URL: '$myUrl' ..."
                    conn = myUrl.toURL().openConnection()
                    conn.setRequestProperty('Cookie', cookie)
                    conn.connect()
                    if (conn.responseCode == 200) {
                        Node result = new XmlParser().parseText(conn.content.text as String)
                        println "[INFO] Es wurden ${result.steps.step.size()} Schritte gefunden"
                        if (result.docType?.text() == 'Tutorial') {
                            if (!importTutorial(result, cookie)) {
                                errorFiles.add(myUrl)
                            }
                        } else {
                            println "keine steps gefunden"
                            //importArticle(result)
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
        println "[INFO] Es gab ${errorFiles.size()} Fehler beim Importierten. Diese Fehler betrafen:\n"
        errorFiles.each {
            println it
        }
    }

    static String cookieGetter(String user, String pass) {
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

    String getMimeType(URLConnection conn) {
        conn.getHeaderField('Content-Type')
    }

    byte[] imageToBytes(URLConnection conn) {
        conn.inputStream.bytes
    }

    byte[] resizeImage(byte[] stepMedia, String mimeType) {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(stepMedia))
        double scale = 200 / img.width
        int newWidth = 200
        int newHeight = img.height * scale

        java.awt.Image resizedImage = img.getScaledInstance(newWidth, newHeight, img.SCALE_SMOOTH)
        BufferedImage bimage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

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
        nodes.each { Node node ->
            temp += asString(node)
        }
        temp
    }

    Step[] processSteps(NodeList rawSteps, String cookie) {
        println '[INFO] Verarbeite Schritte...'
        Step[] mySteps = []
        rawSteps?.step?.eachWithIndex { step, int index ->
            int stepNumber
            byte[] stepMedia = null, previewMedia = null
            String stepContent ="", stepTitle = "", altText = "", mimeType = ""
            ImageCached imgc = null
            Image img = null

            //println "debug: number"
            if (step.number) {
                stepNumber = (step.number?.text())?.toInteger()
            } else {
                stepNumber = index + 1
            }

            //println "debug: media alt"
            if (step.image?.alt?.text()) {
                altText = step.image?.alt?.text() as String
            }

            //println "debug: media"
            if (step.image?.link?.text()) {
                URL imgUrl = ("$preUrl${step.image?.link?.text()}" as String).toURL()


                println 'getMime and imageRaw'
                URLConnection conn = imgUrl.openConnection()
                conn.setRequestProperty('Cookie', cookie)
                conn.connect()

                if (conn.responseCode == 200) {
                    mimeType = getMimeType(conn)

                    stepMedia = imageToBytes(conn)
                    img = new Image(blob: stepMedia, altText: altText, mimeType: mimeType, preview: null)
                }

                previewMedia = resizeImage(stepMedia, mimeType)

                println 'resize'
                imgc = new ImageCached(blob: previewMedia, altText: altText, mimeType: mimeType)
                if (imgc.validate()) {
                    imgc = imgc.save(flush:true)
                } else {
                    imgc.errors?.allErrors?.each { println it }
                    throw new Exception("Kaputt")
                }

                img.preview = imgc
                if (img.validate()) {
                    img = img.save(flush: true)
                } else {
                    img.errors?.allErrors?.each { println it }
                    throw new Exception("Kaputt")
                }

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
                stepTitle = stepTitle.replaceFirst(/Schritt [0-9]+: /, '')
            }


            //println "debug: content"
             stepContent = ""

            //println "debug: steps"
            step.section?.each { section ->
                String content = asString(section.content[0].children())
                String myCss = ''

                if (section.classes?.css) {
                    //println "Gibt css"
                    section.classes.css.each {
                        //println "css $it.text()"
                        myCss += (it.text()=='Hinweis'?'infobox ':'')
                    }
                    //println "myCss $myCss"
                }

                if (section.'@number' && (section.'@number' as int) > 1) {
                    String title = section.title?.text()
                    stepContent += "<section class='$myCss'><h2>$title</h2>$content</section>\n" as String
                } else {
                    if (content.startsWith(/[0-9]+. /)) {
                        //println "Lösche content prefix"
                        content = content.replaceFirst(/[0-9]+. /, '')
                    }
                    stepContent += "<section class='$myCss'>$content</section>\n" as String
                }

            }

            //println "\nNumber $stepNumber\nTitel $stepTitle\nContent $stepContent\n" +
                    "\nMediaType $mimeType\nAltText $altText"
            Step myStep = new Step(number: stepNumber, stepTitle: stepTitle, stepText: stepContent, image: img)
            if (myStep.validate()) {
                mySteps += myStep
            } else {
                myStep.errors?.allErrors?.each { println it }
                throw new Exception("Kaputt")
            }


        }
        mySteps
    }

    boolean importTutorial(Node xmlDoc, String cookie) {
        println '[INFO] Importiere Dokument...'
        Step[] mySteps = []

        //steps verarbeiten
        //println "debug: steps verarbeiten"
        mySteps = processSteps(xmlDoc.steps, cookie)





        //tut erstellen
        Tutorial myTut = new Tutorial(docTitle: xmlDoc.title.text() as String, locked: false, numbered: Boolean.valueOf(xmlDoc.numbered.text()), viewCount: 0, createDate: getDate(xmlDoc), tags: getTags(xmlDoc), mirUrl: xmlDoc.mirurl.text() as String)
        //steps hinzufügen
        mySteps.each { step ->
            myTut.addToSteps(step)
        }
        if (!myTut.validate()) {
            println myTut.errors
            //myTut.delete()
            false
        } else {
            //wenn alles schick, speichern und an author/lang unterkategorien hängen
            println "[INFO] Everything seems fine. Save doc..."
            myTut = myTut.save(flush:true)
            new Linker(subcat: getAuthor(xmlDoc), doc: myTut).save(flush:true)
            new Linker(subcat: Subcategory.findByName('de'), doc: myTut).save(flush:true)
            //println getAuthor(xmlDoc)
            //println myTut.linker.subcat.name
            true
        }
    }
}
