/**
 * Created by didschu on 13.06.17.
 */
$(document).ready(function() {
    $("#doctorate_categories_select").change(function () {
        addCategory(this.value);
    });

});


function addCategory(type) {
    if (type != '0' && !exists(type)) {
        var canvasClearObj = $("#canvas_categories").find(".clear");
        var id = type;
        var url = '/doctorates/category/' + id;

        $.get(url, function(label) {
            if ($.type(label) === 'string') {
                var newGrad = $("<div class='canvas_obj' id=" + id + "><input type='hidden' value=" + id + " name='doctorate_categories'/><span>" + label + "</span><a onclick='removeCanvasObj(&quot;" + id + "&quot;)'> <i class='icon-sym-false2'></i></a></div>");

                newGrad.insertBefore(canvasClearObj);
                $("#doctorate_categories_select option[value='" + id + "']").addClass('usedCat');
            }
        });
    }
    $("#doctorate_categories_select").prop('selectedIndex', 0);
}

function exists(type) {
    return $("#"+type).length > 0
}

function removeCanvasObj(id) {
    $("#"+id).remove();
    $("#doctorate_categories_select option[value='" + id + "']").removeClass('usedCat');
}

