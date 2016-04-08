
window.junitui.createTreeView = function (id, names) {
    // full-stop not allowed in id
    var ide = id.split(".").join("_");
    if ($("#test-list").length === 0) {
        $("<ol class='tree' id='test-list'></ol>").insertAfter("#test-header");
    }  
    var isRerun = $( "#"+ide ).length;
    $("#test-list").append("<li id='" + ide + "'><label id='label"+ide+(isRerun?isRerun:"")
            +"' for='cb" + ide + "'>" + id + " "+ (isRerun?"" :"<a href='#' id='rerun" + ide + "'>(click to rerun)</a>")
            +"<progress value='0' max='100'></progress></label><input type='checkbox' id='cb" + ide + "' /><ol></ol></li>");
    // add later after testing it's ok
  
    for (var i = 0; i < names.length; i++) {
        $("#test-list li ol").last().append($("<li class='file'><a id='" + (id + "_" + i) + "' href='#'>" + names[i] + "</a></li>"));
    }
    return $("progress").last();
};

