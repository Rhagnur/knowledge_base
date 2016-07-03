/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.transaction.Transactional

@Transactional
class InitService {

    def initTestModell() {
        def doc1 = new Document(title: 'WLAN für Windows 7', content: 'Testcontent WLAN Win7', hiddenTags: ['wifi', 'wlan', 'windows 7'] as String[])
        def doc2 = new Document(title: 'WiFi for Windows 7', content: 'Testcontent WLAN Win7', hiddenTags: ['wifi', 'wlan', 'windows 7'] as String[])
        def doc3 = new Document(title: 'LAN für Linux', content: 'Testcontent LAN Linux', hiddenTags: ['lan', 'linux'] as String[])
        def doc4 = new Document(title: 'Cisco-Telefonie', content: 'Testcontent Cisco-Tele', hiddenTags: ['cisco', 'telefonie', 'telefon'] as String[])


        //println((doc1.validate() as String) + ' ' + (doc2.validate() as String) + ' ' + (doc3.validate() as String) + ' ' + (doc4.validate() as String))

        new Maincategorie(name: 'group')
                .addToSubCats(new Subcategorie(name: 'anonym').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3))
                .addToSubCats(new Subcategorie(name: 'student').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3))
                .addToSubCats(new Subcategorie(name: 'stuff').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4))
                .addToSubCats(new Subcategorie(name: 'faculty').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4))
                .save()

        new Maincategorie(name: 'doctyp')
                .addToSubCats(new Subcategorie(name: 'tutorial').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4))
                .addToSubCats(new Subcategorie(name: 'faq'))
                .addToSubCats(new Subcategorie(name: 'article'))
                .save()

        new Maincategorie(name: 'theme')
                .addToSubCats(new Subcategorie(name: 'tele').addToDocs(doc4))
                .addToSubCats(new Subcategorie(name: 'lan').addToDocs(doc3))
                .addToSubCats(new Subcategorie(name: 'wlan').addToDocs(doc1).addToDocs(doc2))
                .addToSubCats(new Subcategorie(name: 'lsf'))
                .save()

        new Maincategorie(name: 'lang')
                .addToSubCats(new Subcategorie(name: 'de').addToDocs(doc1).addToDocs(doc3).addToDocs(doc4))
                .addToSubCats(new Subcategorie(name: 'eng').addToDocs(doc2))
                .save()

        new Maincategorie(name: 'os')
                .addToSubCats(new Subcategorie(name: 'windows').addToSubCats(new Subcategorie(name: 'win_7').addToDocs(doc1).addToDocs(doc2)))
                .addToSubCats(new Subcategorie(name: 'linux').addToDocs(doc3))
                .addToSubCats(new Subcategorie(name: 'android'))
                .addToSubCats(new Subcategorie(name: 'ios'))
                .save()
    }
}
