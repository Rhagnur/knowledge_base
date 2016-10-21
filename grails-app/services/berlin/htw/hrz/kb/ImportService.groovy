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
        oldFiles.each { String myUrl ->
            GPathResult result = new XmlSlurper().parse(myUrl)
            if (result.steps) {
                importTutorial(result)
            }
        }
    }

    void importTutorial(GPathResult xmlDoc) {
        String[] myTags = []
        Step[] mySteps = []

        //tags verarbeiten
        xmlDoc.meta.tags.tag.each { tag ->
            myTags += tag
        }

        //steps verarbeiten
        xmlDoc.steps.step.each { step ->
            int stepNumber = (step.number as String).toInteger()
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
            mySteps += new Step(number: stepNumber, stepTitle: stepTitle, stepText: stepContent, stepLink: stepLink)
        }

        //date holen
        String myDate = "${xmlDoc.meta.date.day}/${xmlDoc.meta.date.month}/${xmlDoc.meta.date.year} ${xmlDoc.meta.date.hour}:${xmlDoc.meta.date.minute}:${xmlDoc.meta.date.second}"

        //author holen
        String author = xmlDoc.author as String
        Subcategory authorNode = Subcategory.findByName(author)
        if (!authorNode) {
            Category.findByName('author')?.addToSubCats(new Subcategory(name: author))?.save(flush:true)
        }

        //tut erstellen
        Tutorial myTut = new Tutorial(docTitle: xmlDoc.title as String, locked: false, viewCount: 0, createDate: Date.parse('dd/MM/yyyy hh:mm:ss', myDate))
        //steps hinzufügen
        mySteps.each { step ->
            myTut.addToSteps(step)
        }
        if (!myTut.validate()) {
            println myTut.errors
        } else {
            //wenn alles schick, speichern und an author/lang unterkategorien hängen
            myTut = myTut.save(flush:true)
            new Linker(subcat: Subcategory.findByName(author), doc: myTut).save(flush:true)
            new Linker(subcat: Subcategory.findByName('de'), doc: myTut).save(flush:true)
        }
    }
}
