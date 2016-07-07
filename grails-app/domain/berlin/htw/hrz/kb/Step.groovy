package berlin.htw.hrz.kb

class Step {

    static constraints = {
        mediaLink nullable: true
    }

    int number;
    String stepTitle, stepText, mediaLink;
}
