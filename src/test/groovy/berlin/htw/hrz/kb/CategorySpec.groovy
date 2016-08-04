package berlin.htw.hrz.kb

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Category)
@Mock([Category, Subcategory])
class CategorySpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test mainCat all"() {
        when:
            Category mainCat = new Category(name: 'TestingMainCat').addToSubCats(new Subcategory(name: 'TestingMainCatSubCat1')).addToSubCats(new Subcategory(name: 'TestingMainCatSubCat1')).save()
        then:
            mainCat.validate() == true
            mainCat.name == 'TestingMainCat'
            mainCat.subCats.size() == 2
    }

    void "test mainCat null name"() {
        when:
            Category mainCat = new Category(name: null)
        then:
            mainCat.validate() == false
    }
}
