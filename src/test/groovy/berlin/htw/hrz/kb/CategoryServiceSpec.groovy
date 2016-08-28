/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(CategoryService)
@Mock([CategoryService, DocumentService, Category, Subcategory, Document, Step, Faq, Linker])
class CategoryServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test getCategory valid MainCat"() {
        given:
            Category mainCat = new Category(name: 'TestingGetMainCat').save()
        expect:
            mainCat instanceof Category
            mainCat != null
        when:
            def temp = service.getCategory(mainCat.name)
        then:
            notThrown Exception
            temp instanceof Category
            temp != null
            temp == mainCat
    }

    void "test getCategory null MainCat"() {
        given:
            Category mainCat = new Category(name: 'TestingGetMainCat').save()
        expect:
            mainCat instanceof Category
            mainCat != null
        when:
            def temp = service.getCategory(null)
        then:
            thrown IllegalArgumentException
    }

    void "test getCategory not-existing MainCat"() {
        when:
            def temp = service.getCategory('Nonsense')
        then:
            thrown NoSuchObjectFoundException
    }

    void "test deleteExistingCategory"() {
        setup:
            Subcategory cat = new Subcategory(name: 'TestingDeleteCat').save(flush: true)
            String name = cat.name
            service.deleteSubCategory(cat)
        when:
            service.getCategory(name)
        then:
            thrown NoSuchObjectFoundException
    }

    void "test deleteSubcat attr = null"() {
        when:
            service.deleteSubCategory(null)
        then:
            thrown IllegalArgumentException
    }

    void "test changeCatName valid"() {
        given:
            Subcategory cat = new Subcategory(name: 'TestChangeCatName').save()
        expect:
            cat instanceof Subcategory
        when:
            Subcategory temp = service.changeCategoryName(cat, 'NeuerName')
        then:
            notThrown Exception
            temp.name == 'NeuerName'

    }

    void "test changeCatName null newName"() {
        given:
            Subcategory cat = new Subcategory(name: 'TestChangeCatName').save()
        expect:
            cat instanceof Subcategory
        when:
            Subcategory temp = service.changeCategoryName(cat, null)
        then:
            thrown IllegalArgumentException

    }

    void "test newSubCategory with valid arguments"() {
        when:
            Subcategory sub = service.newSubCategory('Testing', new Category(name: 'TestMain'))
        then:
            notThrown Exception
            sub.validate() == true
            sub instanceof Subcategory
            sub.parentCat instanceof Category
            sub.parentCat.name == 'TestMain'
            sub.subCats instanceof Set<Subcategory>
            sub.subCats.size() == 2
    }

    void "test newSubCategory IllArgEx"() {
        when:
            service.newSubCategory(null, null)
        then:
            thrown IllegalArgumentException
    }

    void "test getAllSubCats existing Cat"() {
        given:
            Subcategory cat = new Subcategory(name: 'TestingGetSubCats', parentCat: new Category(name: 'Test'))
                    .addToSubCats(new Subcategory(name: 'TestSubCat1'))
                    .addToSubCats(new Subcategory(name: 'TestSubCat2'))
                    .addToSubCats(new Subcategory(name: 'TestSubCat3'))
                    .save()
        expect:
            cat instanceof Subcategory
            cat.subCats?.size() > 0
        when:
            def subCats = service.getAllSubCats(cat)
        then:
            subCats.size() == 3
    }

    void "test getAllSubCats null argument"() {
        when:
            def subCats = service.getAllSubCats(null)
        then:
            thrown IllegalArgumentException
    }


    void "test getIterativeAllSubCats"() {
        given:
            Category cat = new Category(name: 'TestingGetSubCats')
                    .addToSubCats(new Subcategory(name: 'TestSubCat1').addToSubCats(new Subcategory(name: 'TestSubSubCat11')))
                    .addToSubCats(new Subcategory(name: 'TestSubCat2').addToSubCats(new Subcategory(name: 'TestSubSubCat21')).addToSubCats(new Subcategory(name: 'TestSubSubCat22')))
                    .addToSubCats(new Subcategory(name: 'TestSubCat3').addToSubCats(new Subcategory(name: 'TestSubSubCat31').addToSubCats(new Subcategory(name: 'TestSubSubSubCat311'))))
                    .save()
        expect:
            cat instanceof Category
            cat.subCats?.size() > 0
        when:
            def subCats = service.getIterativeAllSubCats(cat)
        then:
            subCats.size() == 8
    }

    void "test getIterativeAllSubCats IllArgEx"() {
        when:
            service.getIterativeAllSubCats(null)
        then:
            thrown IllegalArgumentException
    }

    void "test changeParent"() {
        setup:
            Subcategory sub = new Subcategory(name:'Test').save(flush:true)
            new Category(name:'Old').addToSubCats(sub).save(flush: true)
            Category newParent = new Category(name: 'New').save(flush:true)
        when:
            sub = service.changeParent(sub, newParent)
        then:
            sub.parentCat
            sub.parentCat.name == 'New'
    }

    void "test changeParent IllArgEx"() {
        when:
            service.changeParent(null, null)
        then:
            thrown IllegalArgumentException
    }

    void "test getDocs"() {
        setup:
            Subcategory sub = new Subcategory(name:'Test').save()
            Document doc = new Document(docTitle: 'Testing', viewCount: 0).save()
            Linker.link(sub, doc)
        when:
            List<Document> docs = service.getDocs(sub)
        then:
            docs.size() == 1
            docs.docTitle.contains('Testing')
    }

    void "test getDocs IllArgEx"() {
        when:
            service.getDocs(null)
        then:
            thrown IllegalArgumentException
    }

    void "test findUnlinked Subcats"() {
        setup:
            new Subcategory(name: 'Test').save()
            new Subcategory(name: 'Test2').save()
        when:
            List<Subcategory> subs = service.findUnlinkedSubcats()
        then:
            subs.size() == 2
            subs.name.containsAll(['Test', 'Test2'])
    }

    void "test getAllMainCats"() {
        setup:
            new Category(name: 'Test').save()
            new Category(name: 'Test2').save()
        when:
            List<Category> cats = service.getAllMainCats()
        then:
            Category.findAll() as List == cats
    }

    void "test getAllMaincatsWithSubcats"() {
        setup:
            new Category(name: 'Test').addToSubCats(new Subcategory(name: 'Test-1')).addToSubCats(new Subcategory(name: 'Test-2')).save()
            new Category(name: 'Test2').addToSubCats(new Subcategory(name: 'Test2-1').addToSubCats(new Subcategory(name: 'Test2-11'))).save()
        when:
            HashMap cats = service.getAllMaincatsWithSubcats()
        then:
            cats.size() == 2
            cats.Test.size() == 2
            cats.Test2.size() == 2
    }

    void "test getAllMaincatsWithSubcats with excluded"() {
        setup:
            new Category(name: 'Test').addToSubCats(new Subcategory(name: 'Test-1')).addToSubCats(new Subcategory(name: 'Test-2')).save()
            Category exCat = new Category(name: 'Test2').addToSubCats(new Subcategory(name: 'Test2-1').addToSubCats(new Subcategory(name: 'Test2-11'))).save()
        when:
            HashMap cats = service.getAllMaincatsWithSubcats([exCat] as List)
        then:
            cats.size() == 1
            cats.Test.size() == 2
    }

    void "test getAllSubCats"() {
        setup:
            new Subcategory(name: 'Test').save()
            new Subcategory(name: 'Test2').save()
        when:
            List<Subcategory> subs = service.getAllSubCats()
        then:
            subs.size() == 2
            subs.name.containsAll(['Test', 'Test2'])
    }

    void "test getSubcategories"() {
        setup:
            new Subcategory(name: 'Test').save()
            new Subcategory(name: 'Test2').save()
        when:
            List<Subcategory> subs = service.getSubcategories(['Test', 'Test2'] as String[])
        then:
            subs.size() == 2
            subs.name.containsAll(['Test', 'Test2'])
    }

    void "test getSubcategories IllArgEx 1"() {
        when:
            service.getSubcategories(null)
        then:
            thrown IllegalArgumentException
    }

    void "test getSubcategories IllArgEx 2"() {
        when:
            service.getSubcategories(['Test'] as String[])
        then:
            thrown NoSuchObjectFoundException
    }


}
