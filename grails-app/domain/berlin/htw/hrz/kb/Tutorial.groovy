package berlin.htw.hrz.kb

class Tutorial extends Document {

    static constraints = {
        steps nullable: true
    }

    static hasMany = [steps: Step]
}
