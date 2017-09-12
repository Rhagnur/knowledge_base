package berlin.htw.uss.fo

import java.sql.Timestamp

class FileDataHolder {
    String fileName
    String attachCat
    Timestamp created
    InputStream optInputStream
    File optOutputFile
    long fileSize
    String prettySize
    String mimeType

    String sizeToPrettySize(long size) {
        if (size >= 1000 * 1000) {
            "${((size/1000*1000)as float).trunc(1)} MB"
        } else if(size >= 1000) {
            "${((size/1000)as float).trunc(1)} kB"
        } else {
            "$size Byte"
        }
    }

}