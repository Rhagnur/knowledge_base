/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

class Document {

    static mapWith = "neo4j"

    static constraints = {
        tags nullable: true
        changedBy nullable: true
        createDate nullable: true
        changeDate nullable: true
        locked nullable: true
        linker nullable: true
    }

    static hasMany = [linker: Linker]

    String docTitle
    String[] tags, changedBy
    Date createDate, changeDate
    Boolean locked
    int viewCount
}