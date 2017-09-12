/**
 * Created by didschu on 06.07.17.
 */
$(document).ready(function() {
    //wichtig, falls die Checkbox schon gesetzt wurde und man zurück geht, damit richtige Formularfelder angezeigt werden
    toggleVision();

    $("#is_extern").change(function () {
        toggleVision()
    });

    $("#intern_type_ahead").keypress(function (event) {
        if (event.keyCode == 13) {
            event.preventDefault();
            findPerson();
        }
    });
});

function toggleVision() {
    var isExtern = $('#is_extern');
    var divExtern = $('#extern');
    var divIntern = $('#intern');

    if (isExtern.is(':checked')) {
        divExtern.show();
        divIntern.hide();

    } else {
        divIntern.show();
        divExtern.hide();
        initTypeAhead();
    }
}

function initTypeAhead() {
    var internPersonSuggestion = new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        //prefetch: '../data/sd/sdf.json',
        remote: {
            url: '/researchers/suggest?query=%QUERY',
            wildcard: '%QUERY'
        }
    });
    internPersonSuggestion.initialize();

    $('#intern_type_ahead').typeahead(null, {
        name: 'internPersonSuggestion',
        display: function(item) {
            return item.sn + ", " + item.givenName;
        },
        source: internPersonSuggestion,
        hint: true,
        highlight: true,
        minLength: 3,
        limit: 500,
        templates: {
            suggestion: function(data) {
                var isActive = data.active?'Aktiv':'Inaktiv';
                return '<div><strong>' + data.sn + ', ' + data.givenName + '</strong> ('+ isActive + ')</div>'
            }
            //suggestion: Handlebars.compile('<div><strong>{{sn}}, {{givenName}}</strong> ({{active?}})</div>')
        }
    });

    $('.typeahead').bind('typeahead:select', function(ev, suggestion) {
        personSelected(suggestion);
    });
}

function personSelected(person) {
    console.log(person);
    //Infotext ausblenden
    $('#no_person_choosen').hide();
    //Alte Person löschen
    $('#person_card').remove();


    /*
    var fullname = person.label;

    var isActive = '';
    if (person.active == true) {
        isActive = '<br/><span>Aktiver Mitarbeiter</span>';
    } else {
        isActive = '<br/><span>Ehemaliger Mitarbeiter</span>';
    }

    var fieldOfActivity = '';
    if (person.fieldsOfActivity != null) {
        fieldOfActivity = '<br/><br/><span style="font-size: 0.9em; color: #888;">' + person.fieldsOfActivity + '</span>'
    }

    $('#intern_pvz_id').val(person.id);
    $("<div id='person_card'><strong><span>" + fullname + "</span></strong><br/><span>" + person.mail + "</span>" + isActive + fieldOfActivity + "</div>").appendTo("#intern_canvas");

    */

    var url = '/researchers/personInfo?pvzid=' + person.id;
    if (person.lsfId != null) {
        url += '&lsfid=' + person.lsfId;
    }

    $('#intern_pvz_id').val(person.id);
    $("#intern_canvas").load(url);
    $('#intern_type_ahead').typeahead('val', '');
}

function newPvzPerson(data) {

}