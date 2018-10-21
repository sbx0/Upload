$("#copyBtn").click(function () {
    var input = document.getElementById("url");
    input.select(); // 选中文本
    document.execCommand("copy"); // 执行浏览器复制命令
    $("#copyBtn").toggle();
    $("#msg").html("复制成功");
    $("#des").html("即刻分享<br>机不可失");
})