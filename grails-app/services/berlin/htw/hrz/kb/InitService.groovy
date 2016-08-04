/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.transaction.Transactional

@Transactional
class InitService {

    def initTestModell() {
        Random random = new Random()

        //init docs
        def myDocs = []
        for (int i = 0; i < 50; i++) {
            def magicNumber = random.nextInt(3)

            if (magicNumber == 0) {
                Document doc = new Tutorial(docTitle: "Testanleitung${i}", viewCount: random.nextInt(1000), tags: ["Test"], createDate: new Date())
                for (int j = 0; j < (random.nextInt(8) + 1); j++) {
                    doc.addToSteps(new Step(number: (j + 1), stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b>', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                }

                myDocs.add(doc.save(flush: true))
            } else if (magicNumber == 1) {
                myDocs.add(new Faq(docTitle: "Testfrage${i}?", viewCount: random.nextInt(1000), tags: ["Test"], createDate: new Date(), question: "Testfrage${i}?", answer: 'Eine mögliche Lösung wäre <a href="#">Testantwort</a>!').save(flush: true))
            } else if (magicNumber == 2) {
                myDocs.add(new Article(docTitle: "Testartikel${i}", viewCount: random.nextInt(1000), tags: ["Test"], createDate: new Date(), docContent: "Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b><img src='https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png' alt='Testbild' />").save(flush: true))
            }
        }

        def myMains = [:]

        def group = new Category(name: 'group')
                .addToSubCats(new Subcategory(name: 'anonym'))
                .addToSubCats(new Subcategory(name: 'student'))
                .addToSubCats(new Subcategory(name: 'staff'))
                .addToSubCats(new Subcategory(name: 'faculty'))
                .save()
        myMains.put('group', group)

        def theme = new Category(name: 'theme')
                .addToSubCats(new Subcategory(name: 'eForms'))
                .addToSubCats(new Subcategory(name: 'groupware'))
                .addToSubCats(new Subcategory(name: 'account'))
                .addToSubCats(new Subcategory(name: 'copy_print'))
                .addToSubCats(new Subcategory(name: 'mail'))
                .addToSubCats(new Subcategory(name: 'space'))
                .addToSubCats(new Subcategory(name: 'vpn'))
                .addToSubCats(new Subcategory(name: 'webservice'))
                .addToSubCats(new Subcategory(name: 'tele'))
                .addToSubCats(new Subcategory(name: 'lan'))
                .addToSubCats(new Subcategory(name: 'wlan'))
                .addToSubCats(new Subcategory(name: 'lsf'))
                .save()
        myMains.put('theme', theme)

        def lang = new Category(name: 'lang')
                .addToSubCats(new Subcategory(name: 'de'))
                .addToSubCats(new Subcategory(name: 'eng'))
                .save()
        myMains.put('lang', lang)

        def os = new Category(name: 'os')
                .addToSubCats(new Subcategory(name: 'windows')
                    .addToSubCats(new Subcategory(name: 'win_7'))
                    .addToSubCats(new Subcategory(name: 'win_8'))
                .addToSubCats(new Subcategory(name: 'win_10'))
                )
                .addToSubCats(new Subcategory(name: 'linux'))
                .addToSubCats(new Subcategory(name: 'android'))
                .addToSubCats(new Subcategory(name: 'ios')
                    .addToSubCats(new Subcategory(name: 'ios_6'))
                    .addToSubCats(new Subcategory(name: 'ios_7'))
                    .addToSubCats(new Subcategory(name: 'ios_9'))
                )
                .addToSubCats(new Subcategory(name: 'mac')
                    .addToSubCats(new Subcategory(name: 'mac_108'))
                    .addToSubCats(new Subcategory(name: 'mac_109'))
                    .addToSubCats(new Subcategory(name: 'mac_1010'))
                )
                .save()
        myMains.put('os', os)

        def author = new Category(name: 'author')
                .addToSubCats(new Subcategory(name: 'didschu'))
                .addToSubCats(new Subcategory(name: 'rack'))
                .save()
        myMains.put('author', author)

        myDocs.each { doc ->
            myMains.each {
                Category tempMain = it.value
                def tempSubs = tempMain.subCats.findAll() asList()
                def tempSubSub
                tempSubs.collect().each {
                    if (it.subCats) tempSubs.addAll(it.subCats)
                }

                Subcategory tempSub = tempSubs.get(random.nextInt(tempSubs.size()))
                //println(tempMain.name + " - " + tempSub.name + " # " + tempSub.name.getClass())
                if (!doc.tags) doc.tags = "${tempSub.name}"
                else doc.tags += "${tempSub.name}"
                tempSub.addToDocs(doc.save(flush:true))
                tempSub.save(flush: true)
            }
        }
    }
}
