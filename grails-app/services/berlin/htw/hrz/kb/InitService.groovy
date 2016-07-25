/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.transaction.Transactional

@Transactional
class InitService {

    def initTestModell() {
        def doc1 = new Document(docTitle: 'WLAN für Windows 7', hiddenTags: ['wifi', 'wlan', 'windows 7'] as String[], viewCount: 42)
                .addToSteps(new Step(number: 1, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 2, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 3, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
        def doc2 = new Document(docTitle: 'WLAN for Windows 7 (HTML)', hiddenTags: ['wifi', 'wlan', 'windows 7'] as String[], viewCount: 2)
                .addToSteps(new Step(number: 1, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b>', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 2, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b>', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 3, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz<br/><ul><li>Punkt 1</li><li>Punkt 2</li></ul><br/><b>Test</b>', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
        def doc3 = new Document(docTitle: 'Lan für Linux', hiddenTags: ['lan', 'linux'] as String[], viewCount: 815)
                .addToSteps(new Step(number: 1, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 2, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 3, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
        def doc4 = new Document(docTitle: 'Cisco-Telefonie', hiddenTags: ['telefonie', 'cisco', 'telephone'] as String[], viewCount: 66)
                .addToSteps(new Step(number: 1, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 2, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
                .addToSteps(new Step(number: 3, stepTitle: 'Dies ist ein Titel', stepText: 'Dies ist ein Absatz', mediaLink: 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Image_manquante_2.svg/320px-Image_manquante_2.svg.png'))
        def doc5 = new Document(docTitle: 'Was ist die Antwort auf das Leben?', hiddenTags: ['Leben', '42'] as String[], viewCount: 1456, faq: new Faq(question: 'Was ist die Antwort auf das Leben?', answer: '42'))
        def doc6 = new Document(docTitle: 'What shall we do with a drunken sailor?', hiddenTags: ['drunk', 'sailor'] as String[], viewCount: 48, faq: new Faq(question: 'What shall we do with a drunken sailor?', answer: 'Way hay and up she rises, Early in the morning'))
        def doc7 = new Document(docTitle: 'Can i access the HTW WiFi with my Windows 7 Phone?', hiddenTags: ['wifi', 'windows phone'] as String[], viewCount: 586, faq: new Faq(question: 'Can i access the HTW WiFi with my Windows 7 Phone?', answer: 'No, because there is no such device like a windows 7 phone '))
        def doc8 = new Document(docTitle: 'Kann ich mein eigenes Cisco-Endgerät einfach zum Telefonieren nutzen?', hiddenTags: ['cisco', 'telefon'] as String[], viewCount: 173, faq: new Faq(question: 'Kann ich mein eigenes Cisco-Endgerät einfach zum Telefonieren nutzen?', answer: 'Nein, Sie müssen dieses erst durch den verantwortlichen Administrator freischalten lassen.'))


        new Maincategorie(name: 'group')
                .addToSubCats(new Subcategorie(name: 'anonym').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc5).addToDocs(doc6))
                .addToSubCats(new Subcategorie(name: 'student').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc5).addToDocs(doc6).addToDocs(doc7))
                .addToSubCats(new Subcategorie(name: 'stuff').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4).addToDocs(doc7).addToDocs(doc8))
                .addToSubCats(new Subcategorie(name: 'faculty').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4).addToDocs(doc7).addToDocs(doc8))
                .save()

        new Maincategorie(name: 'doctype')
                .addToSubCats(new Subcategorie(name: 'tutorial').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4))
                .addToSubCats(new Subcategorie(name: 'faq').addToDocs(doc5).addToDocs(doc6).addToDocs(doc7).addToDocs(doc8))
                .addToSubCats(new Subcategorie(name: 'article'))
                .save()

        new Maincategorie(name: 'theme')
                .addToSubCats(new Subcategorie(name: 'tele').addToDocs(doc4).addToDocs(doc8))
                .addToSubCats(new Subcategorie(name: 'lan').addToDocs(doc3))
                .addToSubCats(new Subcategorie(name: 'wlan').addToDocs(doc1).addToDocs(doc2).addToDocs(doc7))
                .addToSubCats(new Subcategorie(name: 'lsf'))
                .save()

        new Maincategorie(name: 'lang')
                .addToSubCats(new Subcategorie(name: 'de').addToDocs(doc1).addToDocs(doc3).addToDocs(doc4).addToDocs(doc5).addToDocs(doc8))
                .addToSubCats(new Subcategorie(name: 'eng').addToDocs(doc2).addToDocs(doc6).addToDocs(doc7))
                .save()

        new Maincategorie(name: 'os')
                .addToSubCats(new Subcategorie(name: 'windows').addToSubCats(new Subcategorie(name: 'win_7').addToDocs(doc1).addToDocs(doc2).addToDocs(doc7)).addToSubCats(new Subcategorie(name: 'win_8').addToSubCats(new Subcategorie(name: 'win_8.1'))))
                .addToSubCats(new Subcategorie(name: 'linux').addToDocs(doc3))
                .addToSubCats(new Subcategorie(name: 'android'))
                .addToSubCats(new Subcategorie(name: 'ios'))
                .save()

        new Maincategorie(name: 'author')
                .addToSubCats(new Subcategorie(name: 'didschu').addToDocs(doc1).addToDocs(doc2).addToDocs(doc3).addToDocs(doc4))
                .addToSubCats(new Subcategorie(name: 'rack').addToDocs(doc5).addToDocs(doc6).addToDocs(doc7).addToDocs(doc8))
                .save()


        new Subcategorie(name: 'Test').addToSubCats(new Subcategorie(name: 'TestOld1')).addToSubCats(new Subcategorie(name: 'TestOld2')).save()
        new Subcategorie(name: 'TestNew1').save()
        new Subcategorie(name: 'TestNew2').save()


        new Maincategorie(name: 'TestMain')
                .addToSubCats(new Subcategorie(name: 'TestSub').addToSubCats(new Subcategorie(name: 'TestSubSub1').addToDocs(new Document(docTitle: 'TestDoc1'))).addToSubCats(new Subcategorie(name: 'TestSubSub2').addToDocs(new Document(docTitle: 'TestDoc2'))))
                .save()

    }
}
