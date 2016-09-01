/**
 * Created by didschu
 */
var counter = 1;

function addOneStep() {
    if (counter < 15) {
        counter++;
        console.log($('.stepHolder').last());
        var container = $(document.createElement('div'))
            .attr('class', 'stepHolder');

        container.after().html('<label for="stepTitle_'+counter+'">Titel Step '+counter+'</label><br/>'+
            '<input type="text" name="stepTitle_'+counter+'"/>'+
            '<br/>'+
            '<label for="stepText_'+counter+'">Inhalt Step '+counter+'</label><br/>'+
            '<textarea name="stepText_'+counter+'"/>'+
            '<br/>'+
            '<label for="stepLink_'+counter+'">Link Step '+counter+'</label><br/>'+
            '<input type="text"  name="stepLink_'+counter+'"/>'
        );
        container.insertAfter($('.stepHolder').last());
    }
    else {
        alert('Es können nicht mehr als 15 Elemente angefügt werden!');
    }

}

function deleteStep() {
    if (counter > 1) {
        $('.stepHolder').last().remove();
        counter--;
    }
    else {
        alert('Das letzte Element darf nicht entfernt werden!');
    }
}
