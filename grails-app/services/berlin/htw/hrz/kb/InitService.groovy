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
        int numberOfDocs = 200


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

        for (int i = 0; i < numberOfDocs; i++) {
            Document doc = null
            def magicNumber = random.nextInt(6)
            //println(i +' ' + magicNumber)

            //Abfrage mag seltsam anmuten, soll aber dabei helfen die Menge an Dokumenten wie folgt zu halten Anleitungen > FAQ > Artikel
            if (magicNumber == 0 || magicNumber == 1 || magicNumber == 2) {
                doc = new Tutorial(docTitle: "Testanleitung${i}", viewCount: random.nextInt(3000), tags: ["Test"], createDate: new Date())
                for (int j = 0; j < (random.nextInt(10) + 1); j++) {
                    doc.addToSteps(new Step(number: (j + 1), stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b>', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                }
            } else if (magicNumber == 3 || magicNumber == 4) {
                doc = new Faq(docTitle: "Testfrage${i}?", viewCount: random.nextInt(3000), tags: ["Test"], createDate: new Date(), question: "Testfrage${i}?", answer: 'Eine mögliche Lösung wäre <a href="#">Testantwort</a>!')
            } else {
                doc = new Article(docTitle: "Testartikel${i}", viewCount: random.nextInt(3000), tags: ["Test"], createDate: new Date(), docContent: "Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b><img src='https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png' alt='Testbild' />")
            }
            doc.save()

            //Aus jeder Hauptkategorie wird zufällig eine Unterkategorie gewählt und das Dokument daran gehangen.
            myMains.each {
                Category tempMain = it.value
                def tempSubs = tempMain.subCats.findAll() asList()
                tempSubs.collect().each {
                    if (it.subCats) tempSubs.addAll(it.subCats)
                }

                Subcategory tempSub = tempSubs.get(random.nextInt(tempSubs.size()))



                //println(tempMain.name + " - " + tempSub.name + " # " + tempSub.name.getClass())
                if (!doc.tags) doc.tags = "${tempSub.name}"
                else doc.tags += "${tempSub.name}"
                //println('doc: ' + doc + ' ' + doc.docTitle)
                //println('cat: ' + tempSub + ' ' + tempSub.name)
                //doc.addToParentCats(tempSub)
                tempSub.addToDocs(doc)
                //println(doc.validate())
                //println(tempSub.validate())
                if (!tempSub.save(flush:true)) {
                    doc.errors?.allErrors?.each { log.error(it) }
                    tempSub.errors?.allErrors?.each { log.error(it) }
                }

            }
        }
    }
}
