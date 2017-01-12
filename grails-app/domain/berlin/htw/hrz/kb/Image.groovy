package berlin.htw.hrz.kb

class Image {

    static constraints = {
        preview nullable: true
        altText nullable: true
        link nullable: true
        linkType nullable: true
    }

    static hasOne = [preview: ImageCached]
    /**
     * optional, can be null
     */
    byte[] blob
    /**
     *
     */
    String altText
    String mimeType
    String link
    String linkType
    int number
}
