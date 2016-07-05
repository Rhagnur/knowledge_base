/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.transaction.Transactional

@Transactional
class InitService {

    def initTestModell() {
        def doc1 = new Document(docTitle: 'WLAN für Windows 7', docContent: '[{"id":1,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"},{"id":2,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"},{"id":3,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"}]', hiddenTags: ['wifi', 'wlan', 'windows 7'] as String[])
        def doc2 = new Document(docTitle: 'WiFi for Windows 7', docContent: '[{"id":1,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"},{"id":2,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"},{"id":3,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"}]', hiddenTags: ['wifi', 'wlan', 'windows 7'] as String[])
        def doc3 = new Document(docTitle: 'LAN für Linux', docContent: '[{"id":1,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"},{"id":2,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"},{"id":3,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"}]', hiddenTags: ['lan', 'linux'] as String[])
        def doc4 = new Document(docTitle: 'Cisco-Telefonie', docContent: '[{"id":1,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"},{"id":2,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"},{"id":3,"text":"Dies ist ein Absatz","link":"https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png"}]', hiddenTags: ['cisco', 'telefonie', 'telefon'] as String[])


        //println((doc1.validate() as String) + ' ' + (doc2.validate() as String) + ' ' + (doc3.validate() as String) + ' ' + (doc4.validate() as String))

        /* Versuch Document mit hasMany und belongsTo zum Laufen zu kriegen, funzte nicht....
        Maincategorie mainCat = new Maincategorie(name: 'os')
        Subcategorie win = new Subcategorie(name: 'windows')
        Subcategorie win7 = new Subcategorie(name: 'win_7')
        Subcategorie linux = new Subcategorie(name: 'linux')
        Subcategorie android = new Subcategorie(name: 'android')
        Subcategorie ios = new Subcategorie(name: 'ios')

        win7.addToDocs(doc1).addToDocs(doc2).save()
        win.addToSubCats(win7).save()
        mainCat.addToSubCats(win).addToSubCats(linux).addToSubCats(android).addToSubCats(ios).save()
        */


        new Maincategorie(name: 'group')
                .addToSubCats(new Subcategorie(name: 'anonym').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3))
                .addToSubCats(new Subcategorie(name: 'student').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3))
                .addToSubCats(new Subcategorie(name: 'stuff').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4))
                .addToSubCats(new Subcategorie(name: 'faculty').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4))
                .save()

        new Maincategorie(name: 'doctype')
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
                .addToSubCats(new Subcategorie(name: 'windows').addToSubCats(new Subcategorie(name: 'win_7').addToDocs(doc1).addToDocs(doc2)).addToSubCats(new Subcategorie(name: 'win_8').addToSubCats(new Subcategorie(name: 'win_8.1'))))
                .addToSubCats(new Subcategorie(name: 'linux').addToDocs(doc3))
                .addToSubCats(new Subcategorie(name: 'android'))
                .addToSubCats(new Subcategorie(name: 'ios'))
                .save()
    }
}
