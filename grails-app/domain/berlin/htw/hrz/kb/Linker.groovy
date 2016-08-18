package berlin.htw.hrz.kb

class Linker {

    static constraints = {
        doc nullable: true
        subcat nullable: true
    }

    Document doc
    Subcategory subcat

    static Linker link (Subcategory sub, Document doc) {
        //Linker linker = Linker.findBySubcatAndDoc(sub, doc)
        //if (!linker) {
            Linker linker = new Linker()
            sub?.addToLinker(linker)
            doc?.addToLinker(linker)
            linker.save(flush: true)
        //}
    }

    static void unlink(Subcategory sub, Document doc) {
        Linker linker = Linker.findBySubcatAndDoc(sub, doc)
        if (linker) {
            sub?.removeFromLinker(linker)
            doc?.removeFromLinker(linker)
            linker.delete(flush: true)
        }
    }
}
