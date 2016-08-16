package berlin.htw.hrz.kb

class Linker {

    static constraints = {
        doc nullable: true
        subcat nullable: true
    }

    Document doc
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
