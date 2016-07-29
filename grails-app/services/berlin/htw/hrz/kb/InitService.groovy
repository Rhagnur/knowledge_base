/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.transaction.Transactional

@Transactional
class InitService {

    def initTestModell() {

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


        new Maincategory(name: 'group')
                .addToSubCats(new Subcategory(name: 'anonym').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc5).addToDocs(doc6))
                .addToSubCats(new Subcategory(name: 'student').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc5).addToDocs(doc6).addToDocs(doc7))
                .addToSubCats(new Subcategory(name: 'staff').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4).addToDocs(doc7).addToDocs(doc8))
                .addToSubCats(new Subcategory(name: 'faculty').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4).addToDocs(doc7).addToDocs(doc8))
                .save()


        new Maincategory(name: 'theme')
                .addToSubCats(new Subcategory(name: 'tele').addToDocs(doc4).addToDocs(doc8))
                .addToSubCats(new Subcategory(name: 'lan').addToDocs(doc3))
                .addToSubCats(new Subcategory(name: 'wlan').addToDocs(doc1).addToDocs(doc2).addToDocs(doc7))
                .addToSubCats(new Subcategory(name: 'lsf'))
                .save()

        new Maincategory(name: 'lang')
                .addToSubCats(new Subcategory(name: 'de').addToDocs(doc1).addToDocs(doc3).addToDocs(doc4).addToDocs(doc5).addToDocs(doc8))
                .addToSubCats(new Subcategory(name: 'eng').addToDocs(doc2).addToDocs(doc6).addToDocs(doc7))
                .save()

        new Maincategory(name: 'os')
                .addToSubCats(new Subcategory(name: 'windows').addToSubCats(new Subcategory(name: 'win_7').addToDocs(doc1).addToDocs(doc2).addToDocs(doc7)).addToSubCats(new Subcategory(name: 'win_8').addToSubCats(new Subcategory(name: 'win_8.1'))))
                .addToSubCats(new Subcategory(name: 'linux').addToDocs(doc3))
                .addToSubCats(new Subcategory(name: 'android'))
                .addToSubCats(new Subcategory(name: 'ios'))
                .save()

        new Maincategory(name: 'author')
                .addToSubCats(new Subcategory(name: 'didschu').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4))
                .addToSubCats(new Subcategory(name: 'rack').addToDocs(doc5).addToDocs(doc6).addToDocs(doc7).addToDocs(doc8))
                .save()

    }
}
