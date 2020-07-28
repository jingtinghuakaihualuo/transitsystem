$(function () {
    var search = $("#search-btn");
    search.click(function () {
        var sno = $("#sno");
        if (sno.val() === null || sno.val() === '') {
            alert("请输入~");
            return;
        }
        search.submit();
    })

    var msg = $("#msg");
    if(sno.val() != null) {
        alert(msg);
    }
});