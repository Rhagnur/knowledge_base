package berlin.htw.hrz.kb

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Subcategory)
@Mock([Subcategory, Category, Document])
class SubcategorySpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test subcategory all"() {
        when:
            Subcategory sub = new Subcategory(name: 'TestingSubCat', parentCat: new Subcategory(name: 'parentSub'), mainCat: new Category(name: 'mainCat')).addToDocs(new Document(docTitle: 'Test', viewCount: 3)).addToSubCats(new Subcategory(name: 'subCat1')).addToSubCats(new Subcategory(name: 'subCat2')).save()
        then:
            sub.validate() == true
            sub.name == 'TestingSubCat'
            sub.parentCat instanceof Subcategory
            sub.parentCat.name == 'parentSub'
            sub.docs instanceof Set<Document>
            sub.docs.size() == 1
            sub.subCats instanceof Set<Subcategory>
            sub.subCats.size() == 2
    }

    void "test subcategory not-nullable attrs = null"() {
        when:
        Subcategory sub = new Subcategory(name: null, parentCat: null)
        then:
        sub.validate() == false
    }
}
