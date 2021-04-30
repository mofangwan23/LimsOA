// todolist
var todoArr = [ "daiban", "yiban", "genzong", "daifa", "yifa" ];
$("#pagetodo").on("swiperight", function() {
	rightTodo();
}).on("swipeleft", function() {
	leftTodo();
});

function rightTodo() {
	var id = $("#todonav li a.ui-btn-active").attr("id");
	var index = eval($.inArray(id, todoArr) - 1);
	var curr = Math.max(index, 0);
	$("#pagetodo #" + todoArr[curr]).click();
}

function leftTodo() {
	var id = $("#todonav li a.ui-btn-active").attr("id");
	var index = eval($.inArray(id, todoArr) + 1);
	var curr = Math.min(index, 4);
	$("#pagetodo #" + todoArr[curr]).click();
}

$(document).on("pagebeforeshow", "#page1", function() {
	$("#mainpage").click();
});

// msglist
var msgArr = [ "newmsg", "hismsg" ];
$("#pagemsg").on("swiperight", function() {
	var id = $("#msgnav li a.ui-btn-active").attr("id");
	var index = eval($.inArray(id, msgArr) - 1);
	var curr = Math.max(index, 0);
	$("#msgnav #" + msgArr[curr]).click();
}).on("swipeleft", function() {
	var id = $("#msgnav li a.ui-btn-active").attr("id");
	var index = eval($.inArray(id, msgArr) + 1);
	var curr = Math.min(index, 1);
	$("#msgnav #" + msgArr[curr]).click();
});

// newslist
$("#pagenewsedit").on("swipeleft", function() {
	$("#newseditreturn").click();
});
var newsArr = [ "gonggaoa", "newsa" ];
$("#pagenews").on("swiperight", function() {
	var id = $("#newsnav li a.ui-btn-active").attr("id");
	var index = eval($.inArray(id, newsArr) - 1);
	var curr = Math.max(index, 0);
	$("#newsnav #" + newsArr[curr]).click();
}).on("swipeleft", function() {
	var id = $("#newsnav li a.ui-btn-active").attr("id");
	var index = eval($.inArray(id, newsArr) + 1);
	var curr = Math.min(index, 1);
	$("#newsnav #" + newsArr[curr]).click();
});

// addressbook
$("#pagecontact").on("swipeleft", function() {
	$("#contactreturn").click();
});

var jqmCalendar = null;

$("#pageschedule").on(
		'pageinit',
		function(event, ui) {
			jqmCalendar = $("#calendar").jqmCalendar(
					{
						months : [ "01", "02", "03", "04", "05", "06", "07",
								"08", "09", "10", "11", "12" ],
						days : [ "日", "一", "二", "三", "四", "五", "六" ],
						startOfWeek : 0,
						weeksInMonth : 6
					});
		});

$("#pageaddschedule").on('pageinit', function(event, ui) {
	var currYear = (new Date()).getFullYear();
	var opt = {};
	opt.datetime = {
		preset : 'datetime'
	};
	opt.defaults = {
		theme : 'jquery mobile',
		display : 'modal',
		mode : 'scroller',
		lang : 'zh',
		startYear : currYear - 10,
		endYear : currYear + 10
	};

	var optDateTime = $.extend(opt['datetime'], opt['defaults']);
	$("#starttime").mobiscroll(optDateTime).datetime(optDateTime);
	$("#endtime").mobiscroll(optDateTime).datetime(optDateTime);
});