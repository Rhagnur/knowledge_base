package berlin.htw.hrz.kb

class ImageCached {

    static constraints = {
        altText nullable: true
    }
    byte[] blob
    String altText
    String mimeType
}
