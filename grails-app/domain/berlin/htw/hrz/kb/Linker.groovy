/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

/**
 * Domain class that represents a single Linker-element.
 * It's needed for providing a link between a single document and a subcategory.
 * Without it the many to many relationship will provide side effects.
 */
class Linker {

    static mapWith = "neo4j"

    static constraints = {
        doc nullable: true
        subcat nullable: true
    }

    /**
     * reference to a document
     */
    Document doc
    /**
     * reference to a subcategory
     */
    Subcategory subcat

    static Linker link (Subcategory sub, Document doc) {
        Linker linker = Linker.findBySubcatAndDoc(sub, doc)
        if (!linker) {
            linker = new Linker()
            sub?.addToLinker(linker)
            doc?.addToLinker(linker)
            linker.save()
        }
    }

    static void unlink(Subcategory sub, Document doc) {
        Linker linker = Linker.findBySubcatAndDoc(sub, doc)
        if (linker) {
            sub?.removeFromLinker(linker)
            doc?.removeFromLinker(linker)
            linker.delete()
        }
    }
}
