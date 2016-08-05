/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import java.rmi.NoSuchObjectException

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(CategoryService)
@Mock([CategoryService, DocumentService, Category, Subcategory, Document, Step, Faq])
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
            thrown NoSuchObjectException
    }

    void "test deleteExistingCategory"() {
        given:
            Subcategory cat = new Subcategory(name: 'TestingDeleteCat', parentCat: new Category(name: 'Test')).save()
        expect:
            cat instanceof Subcategory
            cat != null
        when:
            service.deleteSubCategory(cat)
        then:
            notThrown Exception
    }

    void "test deleteNonExistingCategory"() {
        when:
            service.deleteSubCategory(null)
        then:
            thrown IllegalArgumentException
    }

    void "test changeCatName valid"() {
        given:
            Subcategory cat = new Subcategory(name: 'TestChangeCatName', parentCat: new Category(name: 'Test')).save()
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
            Subcategory cat = new Subcategory(name: 'TestChangeCatName', parentCat: new Category(name: 'Test')).save()
        expect:
            cat instanceof Subcategory
        when:
            Subcategory temp = service.changeCategoryName(cat, null)
        then:
            thrown IllegalArgumentException

    }

    void "test addSubCategory with valid arguments"() {
        when:
            Subcategory sub = service.newSubCategory('Testing', new Category(name: 'TestMain'), [new Subcategory(name: 'TestSub1'), new Subcategory(name: 'TestSub2')] as Subcategory[])
        then:
            notThrown Exception
            sub.validate() == true
            sub instanceof Subcategory
            sub.parentCat instanceof Category
            sub.parentCat.name == 'TestMain'
            sub.subCats instanceof Set<Subcategory>
            sub.subCats.size() == 2
    }

    void "test addSubCategory null name"() {
        when:
            Subcategory sub = service.newSubCategory(null, new Category(name: 'TestMain'), null)
        then:
            thrown IllegalArgumentException
    }

    void "test addSubCategory null parent"() {
        when:
        Subcategory sub = service.newSubCategory('Test', null as Category, null)
        then:
        thrown IllegalArgumentException
    }

    void "test getDocCount SubCat with existing Docs"() {
        given:
            Subcategory cat = new Subcategory(name: 'TestingDocCount', parentCat: new Category(name: 'Test')).addToDocs(new Document(docTitle: 'TestDoc1')).addToDocs(new Document(docTitle: 'TestDoc2')).save()
        expect:
            cat instanceof Subcategory
            cat != null
            cat.docs.size() > 0
        when:
            def count = service.getDocCount(cat)
        then:
            count instanceof Integer
            count == 2
    }

    void "test getDocCount null SubCat"() {
        when:
            def count = service.getDocCount(null)
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

    void "test getAllSubCats wrong type"() {
        when:
            def subCats = service.getAllSubCats('Hallo')
        then:
            thrown IllegalArgumentException
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
            def subCats = service.getIterativeAllSubCats(cat.name)
        then:
            subCats.size() == 8
    }


}
