/**
 * Created by didschu on 13.06.17.
 */
$(document).ready(function() {
    $("#doct_graduations_select").change(function () {
        addGraduation(this.value);
    });
});

function addGraduation(type) {
    if (type != '0') {
        var canvasClearObj = $("#canvas_graduations").find(".clear");
        var count = getGradCount();
        var id = type + count;
        $.get('/doctorates/graduation?grad=' + type, function (label) {
            if ($.type(label) === 'string') {
                var newGrad = $("<div class='canvas_obj' id=" + id + "><input type='hidden' value=" + type + " name='doctorand_graduations' id=" + id + "/><span>" + label + "</span><a onclick='removeCanvasObj(&quot;" + id + "&quot;)'><i class='icon-sym-false2'></i></a></div>");

                newGrad.insertBefore(canvasClearObj);
            }
        });

    }
    $("#doct_graduations_select").prop('selectedIndex', 0);
}

function getGradCount() {
    return $("#canvas_graduations").children().size() - 1;
}

function removeCanvasObj(id) {
    $("#"+id).remove();
}
