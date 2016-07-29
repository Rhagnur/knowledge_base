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
        for (int i = 0; i < 200; i++) {
            def magicNumber = random.nextInt(3)
            println("Next doc with magicNumber: ${magicNumber}")

            if (magicNumber == 0) {
                Document doc = new Tutorial(docTitle: "Testanleitung${i}", viewCount: random.nextInt(1000), hiddenTags: ['Test'])
                for (int j = 0; j < (random.nextInt(8) + 1); j++) {
                    println('next Step')
                    doc.addToSteps(new Step(number: (j + 1), stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b>', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                }

                myDocs.add(doc.save(flush: true))
            } else if (magicNumber == 1) {
                myDocs.add(new Faq(docTitle: "Testfrage${i}", viewCount: random.nextInt(1000), hiddenTags: ['Test'], question: 'Testfrage?', answer: 'Testantwort!').save(flush: true))
            } else if (magicNumber == 2) {
                myDocs.add(new Article(docTitle: "Testartikel${i}", viewCount: random.nextInt(1000), hiddenTags: ['Test'], docContent: "Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b><img src='https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png' alt='Testbild' />").save(flush: true))
            }
        }

/*
        def doc1 = new Tutorial(docTitle: 'WLAN für Windows 7', hiddenTags: ['wifi', 'wlan', 'windows 7'] as String[], viewCount: 42)
                .addToSteps(new Step(number: 1, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 2, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 3, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
        .save()
        def doc2 = new Tutorial(docTitle: 'WLAN for Windows 7 (HTML)', hiddenTags: ['wifi', 'wlan', 'windows 7'] as String[], viewCount: 2)
                .addToSteps(new Step(number: 1, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b>', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 2, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b>', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 3, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b>', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
        .save()
        def doc3 = new Tutorial(docTitle: 'Lan für Linux', hiddenTags: ['lan', 'linux'] as String[], viewCount: 815)
                .addToSteps(new Step(number: 1, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 2, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 3, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
        .save()
        def doc4 = new Tutorial(docTitle: 'Cisco-Telefonie', hiddenTags: ['telefonie', 'cisco', 'telephone'] as String[], viewCount: 66)
                .addToSteps(new Step(number: 1, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 2, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 3, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
        .save()
        def doc5 = new Faq(docTitle: 'Was ist die Antwort auf das Leben?', hiddenTags: ['Leben', '42'] as String[], viewCount: 1456, question: 'Was ist die Antwort auf das Leben?', answer: '42').save()
        def doc6 = new Faq(docTitle: 'What shall we do with a drunken sailor?', hiddenTags: ['drunk', 'sailor'] as String[], viewCount: 48, question: 'What shall we do with a drunken sailor?', answer: 'Way hay and up she rises, Early in the morning').save()
        def doc7 = new Faq(docTitle: 'Can i access the HTW WiFi with my Windows 7 Phone?', hiddenTags: ['wifi', 'windows phone'] as String[], viewCount: 586, question: 'Can i access the HTW WiFi with my Windows 7 Phone?', answer: 'No, because there is no such device like a windows 7 phone ').save()
        def doc8 = new Faq(docTitle: 'Kann ich mein eigenes Cisco-Endgerät einfach zum Telefonieren nutzen?', hiddenTags: ['cisco', 'telefon'] as String[], viewCount: 173, question: 'Kann ich mein eigenes Cisco-Endgerät einfach zum Telefonieren nutzen?', answer: 'Nein, Sie müssen dieses erst durch den verantwortlichen Administrator freischalten lassen.').save()
*/

        def myMains = [:]

        def group = new Maincategory(name: 'group')
                .addToSubCats(new Subcategory(name: 'anonym'))
                .addToSubCats(new Subcategory(name: 'student'))
                .addToSubCats(new Subcategory(name: 'staff'))
                .addToSubCats(new Subcategory(name: 'faculty'))
                .save()
        myMains.put('group', group)

        def theme = new Maincategory(name: 'theme')
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

        def lang = new Maincategory(name: 'lang')
                .addToSubCats(new Subcategory(name: 'de'))
                .addToSubCats(new Subcategory(name: 'eng'))
                .save()
        myMains.put('lang', lang)

        def os = new Maincategory(name: 'os')
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

        def author = new Maincategory(name: 'author')
                .addToSubCats(new Subcategory(name: 'didschu'))
                .addToSubCats(new Subcategory(name: 'rack'))
                .save()
        myMains.put('author', author)

        myDocs.each { doc ->
            myMains.each {
                Maincategory tempMain = it.value
                def tempSubs = tempMain.subCats.findAll() asList()
                Subcategory tempSub = tempSubs.get(random.nextInt(tempSubs.size()))
                println(tempMain.name + " - " + tempSub.name + " # " + tempSub.name.getClass())
                doc.hiddenTags += (tempSub.name as String )
                tempSub.addToDocs(doc.save(flush:true))
                tempSub.save(flush: true)
            }
        }
    }
}
