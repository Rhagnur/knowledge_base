package berlin.htw.hrz.kb

import grails.transaction.Transactional
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil

@Transactional
class ImportService {
    //todo Verlinkung der Anleitungsdokumente untereinander
    //todo videos?
    DocumentService documentService
    String preUrl = 'http://portal.rz.htw-berlin.de'

    void importOldDocs(List<String> oldFiles) {
        println "importOldDocs: ${oldFiles}"
        oldFiles.each { String myUrl ->
            println "url $myUrl"
            GPathResult result = new XmlSlurper().parse(myUrl)
            println "steps ${result.steps.size()}"
            if (result.steps.size() != 0) {
                println "steps gefunden"
                importTutorial(result)
            }
            else {
                println "keine steps gefunden"
                importArticle(result)
            }
        }
    }

    byte[] imageToBytes(URL path) {
        path.bytes
    }

    Date getDate(GPathResult xmlDoc) {
        String myDate = "${xmlDoc.meta.date.day}/${xmlDoc.meta.date.month}/${xmlDoc.meta.date.year} ${xmlDoc.meta.date.hour}:${xmlDoc.meta.date.minute}:${xmlDoc.meta.date.second}"
        Date.parse('dd/MM/yyyy hh:mm:ss', myDate)
    }

    String[] getTags(GPathResult xmlDoc) {
        String[] myTags
        //tags verarbeiten
        xmlDoc.meta.tags.tag.each { tag ->
            myTags += tag
        }
        myTags
    }

    Subcategory getAuthor(GPathResult xmlDoc) {
        String author = xmlDoc.author as String
        Subcategory authorNode = Subcategory.findByName(author)
        if (!authorNode) {
            authorNode = new Subcategory(name: author)
            Category.findByName('author')?.addToSubCats(authorNode)?.save(flush:true)
        }
        authorNode.save(flush: true)
    }

    void importArticle(GPathResult xmlDoc) {
        println "importArticle"
        String myContent = ""
        //content
        println xmlDoc.content?true:false
        xmlDoc.docContent.content.each {
            println "\n\nDebug ${it.section}"
            //todo: problem mit serialize <?xml version="1.0" encoding="UTF-8"?>
            myContent += XmlUtil.serialize(it.section)
        }
        myContent.replaceAll(/version/, '')
        println "\n\n\n$myContent"
    }

    void importTutorial(GPathResult xmlDoc) {
        Step[] mySteps = []

        //steps verarbeiten
        xmlDoc.steps.step.each { step ->
            int stepNumber = (step.number as String).toInteger()
            byte[] stepMedia = imageToBytes(("$preUrl${step.image.link}" as String).toURL())
            String stepLink = "$preUrl${step.image.link}" as String
            String stepTitle = step.depthFirst().find { it.name() == 'section' && it.@number == 1 }?.title as String
            String stepContent = ""
            step.section.each { section ->
                if (section.@number != 1) {
                    stepContent += "<section><h2>$section.title</h2>${XmlUtil.serialize(section.content)}</section>" as String
                } else {
                    stepContent += "<section>${XmlUtil.serialize(section.content)}</section>" as String
                }
            }

            //println "$stepNumber $stepTitle $stepLink $stepContent"
            mySteps += new Step(number: stepNumber, stepTitle: stepTitle, stepText: stepContent, stepLink: stepLink, stepImage: stepMedia)
        }

        //tut erstellen
        Tutorial myTut = new Tutorial(docTitle: xmlDoc.title as String, locked: false, viewCount: 0, createDate: getDate(xmlDoc), tags: getTags(xmlDoc))
        //steps hinzufügen
        mySteps.each { step ->
            myTut.addToSteps(step)
        }
        if (!myTut.validate()) {
            println myTut.errors
        } else {
            //wenn alles schick, speichern und an author/lang unterkategorien hängen
            myTut = myTut.save(flush:true)
            new Linker(subcat: getAuthor(xmlDoc), doc: myTut).save(flush:true)
            new Linker(subcat: Subcategory.findByName('de'), doc: myTut).save(flush:true)
            myTut.steps.stepImage.each {
                println "######\n$it\n#######"
            }
        }
    }
}
