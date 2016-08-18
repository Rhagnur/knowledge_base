/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.transaction.Transactional
import groovy.time.TimeCategory

@Transactional
class InitService {
    def initTestModell() {
        println('Start init...')
        Random random = new Random()
        def start, stop
        def myDocs = []
        int numberOfDocs = 40

        //init docs
        start = new Date()
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
            myDocs.add(doc.save(flush: true))
        }
        stop = new Date()
        println('Zeit-InitDocs: ' + TimeCategory.minus(stop, start))


        start = new Date()
        def myMains = [:]

        def group = new Category(name: 'group')
                .addToSubCats(new Subcategory(name: 'anonym'))
                .addToSubCats(new Subcategory(name: 'student'))
                .addToSubCats(new Subcategory(name: 'staff'))
                .addToSubCats(new Subcategory(name: 'faculty'))
                .save(flush: true)
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
                .save(flush: true)
        myMains.put('theme', theme)

        def lang = new Category(name: 'lang')
                .addToSubCats(new Subcategory(name: 'de'))
                .addToSubCats(new Subcategory(name: 'eng'))
                .save(flush: true)
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
                    .addToSubCats(new Subcategory(name: 'mac_1011'))
                )
                .save(flush: true)
        myMains.put('os', os)

        def author = new Category(name: 'author')
                .addToSubCats(new Subcategory(name: 'didschu'))
                .addToSubCats(new Subcategory(name: 'rack'))
                .save(flush: true)
        myMains.put('author', author)
        stop = new Date()
        println('Zeit-InitCats: ' + TimeCategory.minus(stop, start))


        start = new Date()
        def myMainsAll = [:]
        myMains.each {
            def tempSubs = []
            tempSubs.addAll(it.value.subCats)
            it.value.subCats.each { subCat ->
                if (subCat.subCats) tempSubs.addAll(subCat.subCats)
            }
            myMainsAll.put(it.key, tempSubs)
        }
        stop = new Date()
        println('Zeit-getAllSubs: ' + TimeCategory.minus(stop, start))


        start = new Date()
        myDocs.eachWithIndex { doc, i ->
            def starti, stopi
            starti = new Date()
            def myGroup = myMainsAll.get('group')
            def myTheme = myMainsAll.get('theme')
            def myLang = myMainsAll.get('lang')
            def myAuthor = myMainsAll.get('author')
            def myOs = myMainsAll.get('os')
            Linker.link(myGroup.get(random.nextInt(myGroup.size())),doc)
            Linker.link(myTheme.get(random.nextInt(myTheme.size())),doc)
            Linker.link(myLang.get(random.nextInt(myLang.size())),doc)
            Linker.link(myAuthor.get(random.nextInt(myAuthor.size())),doc)
            Linker.link(myOs.get(random.nextInt(myOs.size())),doc)

            stopi = new Date()
            println("${i} : ${TimeCategory.minus(stopi, starti)}")
        }
        stop = new Date()
        println('Zeit-DocCatRelationsships: ' + TimeCategory.minus(stop, start))

        println('End init')
    }
}
