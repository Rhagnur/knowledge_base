/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.neo4j.graphdb.NotFoundException
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(CategoryService)
@Mock([CategoryService, DocumentService, Maincategory, Subcategory, Document, Step, Faq])
class CategoryServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test getCategory valid MainCat"() {
        given:
            Maincategory mainCat = new Maincategory(name: 'TestingGetMainCat').save()
        expect:
            mainCat instanceof Maincategory
            mainCat != null
        when:
            def temp = service.getCategory(mainCat.name)
        then:
            notThrown Exception
            temp instanceof Maincategory
            temp != null
            temp == mainCat
    }

    void "test getCategory null MainCat"() {
        given:
            Maincategory mainCat = new Maincategory(name: 'TestingGetMainCat').save()
        expect:
            mainCat instanceof Maincategory
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
            thrown NotFoundException
    }

    void "test deleteExistingCategory"() {
        given:
            Maincategory mainCat = new Maincategory(name: 'TestingDeleteCat').save()
        expect:
            mainCat instanceof Maincategory
            mainCat != null
        when:
            service.deleteCategory(mainCat.name)
            def temp = Maincategory.findByName('TestingDeleteCat')
        then:
            notThrown Exception
            temp == null
    }

    void "test deleteNonExistingCategory"() {
        when:
            service.deleteCategory('Nonsense')
        then:
            thrown NotFoundException
    }

    void "test addSubCategory with parent = mainCat valid arguments"() {
        when:
            Subcategory sub = service.newSubCategory('Testing', new Maincategory(name: 'TestMain'), [new Subcategory(name: 'TestSub1'), new Subcategory(name: 'TestSub2')] as Subcategory[])
        then:
            notThrown Exception
            sub.validate() == true
            sub instanceof Subcategory
            sub.parentCat == null
            sub.mainCat instanceof Maincategory
            sub.mainCat.name == 'TestMain'
            sub.subCats instanceof Set<Subcategory>
            sub.subCats.size() == 2
    }

    void "test addSubCategory with parent = subCat valid arguments"() {
        when:
            Subcategory sub = service.newSubCategory('Testing', new Subcategory(name: 'TestParent'), [new Subcategory(name: 'TestSub1'), new Subcategory(name: 'TestSub2')] as Subcategory[])
        then:
            notThrown Exception
            sub.validate() == true
            sub.mainCat == null
            sub instanceof Subcategory
            sub.parentCat instanceof Subcategory
            sub.parentCat.name == 'TestParent'
            sub.subCats instanceof Set<Subcategory>
            sub.subCats.size() == 2
    }

    void "test addSubCategory null name"() {
        when:
            Subcategory sub = service.newSubCategory(null, new Maincategory(name: 'TestMain'), null)
        then:
            thrown IllegalArgumentException
    }

    void "test addSubCategory null parent"() {
        when:
        Subcategory sub = service.newSubCategory('Test', null as Maincategory, null)
        then:
        thrown IllegalArgumentException
    }

    void "test getDocCount SubCat with existing Docs"() {
        given:
            Subcategory cat = new Subcategory(name: 'TestingDocCount').addToDocs(new Document(docTitle: 'TestDoc1')).addToDocs(new Document(docTitle: 'TestDoc2')).save()
        expect:
            cat instanceof Subcategory
            cat != null
            cat.docs.size() > 0
        when:
            def count = service.getDocCount(cat.name)
        then:
            count instanceof Integer
            count == 2
    }

    void "test getDocCount MainCat"() {
        given:
            Maincategory cat = new Maincategory(name: 'TestingGetDocCountMainCat').save()
        expect:
            cat instanceof Maincategory
            cat != null
        when:
            def count = service.getDocCount(cat.name)
        then:
            thrown NoSuchMethodException
    }

    void "test getDocCount non-existing SubCat"() {
        when:
            def count = service.getDocCount('Nonsense')
        then:
            thrown NotFoundException
    }

    void "test getAllSubCats existing Cat"() {
        given:
            Subcategory cat = new Subcategory(name: 'TestingGetSubCats')
                    .addToSubCats(new Subcategory(name: 'TestSubCat1'))
                    .addToSubCats(new Subcategory(name: 'TestSubCat2'))
                    .addToSubCats(new Subcategory(name: 'TestSubCat3'))
                    .save()
        expect:
            cat instanceof Subcategory
            cat.subCats?.size() > 0
        when:
            def subCats = service.getAllSubCats(cat.name)
        then:
            subCats.size() == 3
    }

    void "test getAllSubCats non-existing Cat"() {
        when:
            def subCats = service.getAllSubCats('Nonsense')
        then:
            thrown NotFoundException
    }

    void "test getIterativeAllSubCats"() {
        given:
            Maincategory cat = new Maincategory(name: 'TestingGetSubCats')
                    .addToSubCats(new Subcategory(name: 'TestSubCat1').addToSubCats(new Subcategory(name: 'TestSubSubCat11')))
                    .addToSubCats(new Subcategory(name: 'TestSubCat2').addToSubCats(new Subcategory(name: 'TestSubSubCat21')).addToSubCats(new Subcategory(name: 'TestSubSubCat22')))
                    .addToSubCats(new Subcategory(name: 'TestSubCat3').addToSubCats(new Subcategory(name: 'TestSubSubCat31').addToSubCats(new Subcategory(name: 'TestSubSubSubCat311'))))
                    .save()
        expect:
            cat instanceof Maincategory
            cat.subCats?.size() > 0
        when:
            def subCats = service.getIterativeAllSubCats(cat.name)
        then:
            subCats.size() == 8
    }


}
