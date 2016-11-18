package berlin.htw.hrz.kb

class Image {

    static constraints = {
        preview nullable: true
        altText nullable: true
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
}
