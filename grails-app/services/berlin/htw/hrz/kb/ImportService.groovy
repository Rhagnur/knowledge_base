package berlin.htw.hrz.kb

import grails.transaction.Transactional
import groovy.xml.DOMBuilder
import groovy.xml.XmlUtil

@Transactional
class ImportService {
    DocumentService documentService

    void importOldDocs(List<String> oldFiles) {
        oldFiles.each { String myUrl ->
            def test = new XmlSlurper().parse(myUrl)
            println "1 ${test.'content'.'content-area'.'@main'}"
            println "2${test.depthFirst().findAll { it.name() == 'keyword' }.toList()} "
            test.depthFirst().findAll { it.name() == 'step'}.each {
                println it.'@id'
                println it.box.section
                //println XmlUtil.serialize(it.box.paragraph)
                it.box.paragraph.each {
                    println it
                }
                println " "
                //documentService.newTutorial(it.box?.section, null, null)
            }

            //println XmlUtil.serialize(test)



            //def parser = new XmlSlurper()
            //parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
            //parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            //def test = parser.parse(myUrl)
            //println XmlUtil.serialize(test)

        }
    }
}
