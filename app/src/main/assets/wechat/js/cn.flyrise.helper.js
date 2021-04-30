var FILE_TYPE_ARRAY = ['doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'apk', 'bt', 'audio', 'exe', 'folder', 'html', 'ipa', 'music', 'pdf', 'rar', 'txt', 'vcard', 'visio'];
var IMAGE_TYPE_ARRAY = ['jpg', 'png', 'peg', 'bmp', 'gif'];
var FILE_TYPE_UNKOWN = "unknown";

var PAGECOUNT = 10;
var DEFAULTPAGE = 1;

function isFileType(type) {
    if (type == null || type == '') {
        return false;
    }
    if (type.lastIndexOf('.') > -1) {
        type = type.substring(type.lastIndexOf('.') + 1);
    }
    return $.inArray(type.toLowerCase(), FILE_TYPE_ARRAY) >= 0;
}

function isImageFile(type) {
    if (type == null || type == '') {
        return false;
    }
    if (type.lastIndexOf('.') > -1) {
        type = type.substring(type.lastIndexOf('.') + 1);
    }
    return $.inArray(type.toLowerCase(), IMAGE_TYPE_ARRAY) >= 0;
}

function showloading(text) {
    var msg = "玩命加载中……";
    if (text && text != '') {
        msg = text;
    }
    $.mobile.loading('show', {
        text: msg,
        textVisible: true,
        theme: 'b',
        textonly: false,
        html: ""
    });
}

function shownoimgloading(text) {
    var msg = "已无更多数据";
    if (text && text != '') {
        msg = text;
    }
    $.mobile.loading('show', {
        text: msg,
        textVisible: true,
        theme: 'b',
        textonly: true,
        html: ""
    });
}

$(function(){
    $('body').append('<div id="loading-hidden" class="ui-popup-screen ui-overlay-b in" style="display: none;"></div>');
});

function showmessage(text) {
    var msg = "已无更多数据";
    if (text && text != '') {
        msg = text;
    }
    $.mobile.loading('show', {
        text: msg,
        textVisible: true,
        theme: 'a',
        textonly: true,
        modal : true,
        html: "<p style='text-align: center;font-size:14px;'>" + msg + "</p><div style='height: 10px; border-top: 1px solid #999'></div> <div style='text-align: center;' onclick='hideloading();'><a style='text-decoration: none' href='#'>好</a></div>"
    });
    if ( $('#loading-hidden').size()) {
        $('#loading-hidden').height($.mobile.activePage.height() + 45).show();
    }
}

function hideloading() {
    if ( $('#loading-hidden').size()) {
        $('#loading-hidden').hide();
    }
    $.mobile.loading('hide');
}

function isImage(type) {
    return '|jpg|png|jpeg|bmp|gif|'.indexOf(type.toLowerCase()) !== -1;
}

function getParameter(param) {
    var query = window.location.search;
    var iLen = param.length;
    var iStart = query.indexOf(param);
    if (iStart == -1) {
        return "";
    }
    iStart += iLen + 1;
    var iEnd = query.indexOf("&", iStart);
    if (iEnd == -1) {
        return query.substring(iStart);
    }
    return query.substring(iStart, iEnd);
}

Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1,
        "d+": this.getDate(),
        "h+": this.getHours(),
        "m+": this.getMinutes(),
        "s+": this.getSeconds(),
        "q+": Math.floor((this.getMonth() + 3) / 3),
        "S": this.getMilliseconds()

    };
    if (/(y+)/.test(fmt))
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

var DateFormat = {
    week: {
        0: '星期天',
        1: '星期一',
        2: '星期二',
        3: '星期三',
        4: '星期四',
        5: '星期五',
        6: '星期六'
    },

    getWeek: function (date) {
        return this.week[date.getDay()];
    },
    parseDate: function (str) {
        return new Date(Date.parse(str.replace(/-/g, "/")));
    },
    format: function (date, fmt) {
        var o = {
            "M+": date.getMonth() + 1,
            "d+": date.getDate(),
            "h+": date.getHours(),
            "m+": date.getMinutes(),
            "s+": date.getSeconds(),
            "q+": Math.floor((date.getMonth() + 3) / 3),
            "S": date.getMilliseconds()

        };
        if (/(y+)/.test(fmt))
            fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
        for (var k in o)
            if (new RegExp("(" + k + ")").test(fmt))
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        return fmt;
    }
}
/**
 * 验证用户是否登录
 *
 * @param {} data
 */
function validateLogin(data) {
    if (data == null || data.errorCode == -1) {
        window.location.href = "/wechat/index.html";
    }
}

/**
 * 回到微信方法调用
 */
var closeWinxin = function onBridgeReady() {
    $('body').append('<a style="display:none" id="closeWindow" />');
    document.querySelector('#closeWindow').addEventListener('click', function (e) {
        WeixinJSBridge.invoke('closeWindow', {}, function (res) {
        });
    });
}
if (typeof WeixinJSBridge === "undefined") {
    document.addEventListener('WeixinJSBridgeReady', closeWinxin, false);
}

function getJqueryLegalId(id) {
    var legalId = id;
    legalId = legalId.replace(/:/g, "\\:");
    legalId = legalId.replace(/\./g, "\\.");
    legalId = legalId.replace(/\//g, "\\/");
    legalId = legalId.replace(/\$/g, "\\$");
    legalId = legalId.replace(/\[/g, "\\[");
    legalId = legalId.replace(/\]/g, "\\]");
    legalId = legalId.replace(/\{/g, "\\{");
    legalId = legalId.replace(/\}/g, "\\}");
    legalId = legalId.replace(/\+/g, "\\+");
    legalId = legalId.replace(/=/g, "\\=");
    return legalId;
}