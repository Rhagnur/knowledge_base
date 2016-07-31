/*
  Created by IntelliJ IDEA.
  User: didschu
 */

package berlin.htw.hrz.kb

class Document {

    static mapWith = "neo4j"

    static constraints = {
        docContent nullable: true
        tags nullable: true
        changedBy nullable: true
        createDate nullable: true
        changeDate nullable: true
        locked nullable: true
        //faq nullable: true
        //steps nullable: true
    }

    //static hasMany = [steps: Step]
    //static hasOne = [faq: Faq]

    String docTitle, docContent
    String[] tags, changedBy
    Date createDate, changeDate
    Boolean locked
    int viewCount
}