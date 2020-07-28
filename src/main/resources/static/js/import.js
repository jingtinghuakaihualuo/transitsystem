
$(function () {
    var file = $("#file");

    $("#sb").click(function () {
        $("form").submit();
    });

    $("#back").click(function () {
        history.back(-1);
    })
});