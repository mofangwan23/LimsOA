var angularScheduleApp = angular.module("angularScheduleApp", [ 'angularOrgTreeApp', 'ngTouch' ]);

angularScheduleApp.filter('trustHtml', function ($sce) {
        return function (input) {
            return $sce.trustAsHtml(input);
        }
});

angularScheduleApp.directive('dateDirective', function() {
	return function(scope, element, attrs) {
		scope.$watch(function(value) {
			var flagMap = eval(attrs.dates);
			if (!flagMap) {
				return;
			}
			$(document).find(".jbstyle").remove();// 先移除其他月份的状态。
			for (var int = 0; int < flagMap.length; int++) {
				var id = flagMap[int];
				var html = $("#date-" + id).html();
				if (html && html.indexOf("sup") < 0) {
					$("#date-" + id).html("<sup class='jbstyle'>&nbsp;</sup>" + html + "<sup class='jbstyle'>●</sup>");
				}
			}
		});
	};
});
angularScheduleApp.directive('loadEditor', function() {
	return function(scope, element, attrs) {
		scope.$watch(function(value) {
			var _newseditor_ = new UM.Editor();
			_newseditor_.render("sharecontent");
			_newseditor_.ready(function() {
				var html = $("#_sharecontent_").val();
				_newseditor_.setContent(html);
				$("#sharecontent").removeAttr("style");
				$("#sharecontent").css("background-color", "#fff");
				$("#sharecontent ").css("border", "1px solid #e5e5e5");
				$("#sharecontent ").css("min-height", "50px");
//				$("#sharecontent img").css("width", "100%");
				$("#sharecontent img").each(function (i, img) {
                    img.src = getImgSrc(img.src);//将图片地址标准化，加上地址url
                    $(img).on("load", function () {
                        if (img.width > $(window).width()) {
                            img.style.width = "100%";
                        }
                    });
                });
			});
		});
	};
});
function getImgSrc(imgSrc){
	var src;
	if(imgSrc && imgSrc.valueOf("/ueditor/jsp/download_json")){
		src = localStorage.feurl+imgSrc.replace("file://","");
	}
	return src;
}
function initContent(){
	var _activityeditor_ = new UM.Editor({'readonly' : false});
	_activityeditor_.render("ScheduleContent");
	_activityeditor_.ready(function() {
	var html = $("#_ScheduleContent_").val();
	_activityeditor_.setContent(html);

	$("#ScheduleContent").removeAttr("style");
	$("#ScheduleContent").css("background-color", "#fff");
	$("#ScheduleContent ").css("border", "1px solid #e5e5e5");
	$("#ScheduleContent ").css("min-height", "50px");
	$("#ScheduleContent img").each(function (i, img) {
        img.src = getImgSrc(img.src);//将图片地址标准化，加上地址url
        $(img).on("load", function () {
            if (img.width > $(window).width()) {
                img.style.width = "100%";
            }
        });
    });
	});
}
angularScheduleApp.controller("addScheduleController", function($scope, $http) {
	$scope.isMulti = true;
	$scope.readonly = false;
	$scope._scheduleInit = function() {
		showloading();
		var jsonParam = {
			obj : 'scheduleService',
			method : 'remindReference',
			count : 0
		};
		$.angularAJAX($http, jsonParam, function(data) {
			$scope.remindTimes = data;
		});
		var jsonParam1 = {
			obj : 'scheduleService',
			method : 'recycleReference',
			count : 0
		};
		$.angularAJAX($http, jsonParam1, function(data) {
			$scope.recycles = data;
			hideloading();
		});
	};
	$scope._saveSchedule = function() {
		showloading();
		if (!$scope.vo) {
			navigator.notification.alert("内容不能为空");
			hideloading();
			return;
		}
		var title = $scope.vo.title;
		if (!title) {
			navigator.notification.alert("标题不能为空");
			hideloading();
			return;
		}
		var content = $scope.vo.content;
		if (!content) {
			navigator.notification.alert("内容不能为空");
			hideloading();
			return;
		}
		var starttimeStr = $scope.vo.starttimeStr;
		if (!starttimeStr) {
			navigator.notification.alert("开始日期不能为空");
			hideloading();
			return;
		}
		var endtimeStr = $scope.vo.endtimeStr;
		if (!endtimeStr) {
			navigator.notification.alert("结束日期不能为空");
			hideloading();
			return;
		}
		var datelimit =Date.parse(endtimeStr) - Date.parse(starttimeStr);
		if(datelimit < 0){
			navigator.notification.alert("结束日期不能小于开始日期");
			hideloading();
			return;
		}
		var remindTime = $scope.vo.remindTime;
		navigator.pluginClose.log(remindTime);
		if(!remindTime){
			if(remindTime!=0){
			navigator.notification.alert("提醒时间不能为空");
			hideloading();
			return;
			}
		}
		var recycleStr = $scope.vo.recycle;
		navigator.pluginClose.log(recycleStr);
		if(!recycleStr){
			if(recycleStr!=0){
			navigator.notification.alert("重复周期不能为空");
			hideloading();
			return;
			}
		}
		$('#publish-btn').addClass("ui-state-disabled");
		$scope.vo.shareUsers = $scope.orgtreehide;
		var json = angular.toJson($scope.vo);
		var jsonParam = {
			obj : 'scheduleService',
			method : 'saveSchedule',
			count : 1,
			param1 : json
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if (data && data == 'SUCCESS') {
				$scope.showone = true;
				$scope.vo = {};
				$scope.vo.remindTime =0;
				$scope.vo.recycle =0;
				//$scope.orgtreehide = "";
				//$scope.orgtreeshow = "";
				if(location.href.indexOf("&pageId=1")>-1)
				{
					//navigator.notification.confirm('备忘保存成功', function(button){navigator.app.exitApp(); }, '温馨提示', '确定, ');
					navigator.notification.alert('备忘保存成功',function(){navigator.app.exitApp()},"","");
				}
				else
				{
					navigator.notification.alert('备忘保存成功');
					$scope._refresh();
					$.mobile.changePage('#pageSchedule', {
						transition : "slide",
						reverse : "true"
					});
				}
			} else {
				navigator.notification.alert(data);
			}
			$('#publish-btn').removeClass("ui-state-disabled");
			hideloading();
		});
	};
	$scope._showOrgAdd = function() {
		$scope._initOrgTree($scope.orgtreehide);
	};
	$scope.$watch('vo', function() {
		try {
			if ($scope.vo) {
				$("#add_remindTime").val($scope.vo.remindTime).selectmenu().selectmenu("refresh");
				$("#add_recycle").val($scope.vo.recycle).selectmenu().selectmenu("refresh");
			}
		} catch (e) {
			console.log(e);
		}
	});
	initapp($scope._scheduleInit);
});
var isDetail = false;

angularScheduleApp.controller("listScheduleController", function($scope, $http) {
	$scope.BackShow=function()
	{
	    return true;
	}
	$scope.safeApply = function(fn) {
        var phase = this.$root.$$phase;
        if(phase == '$apply' || phase == '$digest') {
			if(fn && (typeof(fn) === 'function')) {
				fn();
			}
        }else {
			this.$apply(fn);
		}
	};
	$scope.isMulti = true;
	$scope.feurl = localStorage.feurl;
	$scope.readonly = false;
	$scope._addSchedule = function() {
		$scope.orgtreehide = "";
		$scope.orgtreeshow = "";
	};
	$scope._scheduleInit = function() {
		var pageId = getParameter("pageId");
		if("1"==pageId){
			$(document).ready(function(){
				$scope._addSchedule();
				$.mobile.changePage('#addSchedulePage', {
					transition : "slide"
				});
			});
			$scope.hide=true;
			return;
		}
        var scheduleid =null;
        if(location.href.lastIndexOf("?id=")>-1)
        {
            var pattern=new RegExp("\\?id=(.+)");
            var result;
            if((result=pattern.exec(location.href))!=null)
            {
                scheduleid=result[1];
            }
        }

		if (scheduleid && scheduleid != "") {
			isDetail = true;
			var jsonParam = {
				obj : 'scheduleService',
				method : 'shareOrOwn',
				count : 1,
				param1 : scheduleid
			};
			$.angularAJAX($http, jsonParam, function(data) {
				if (data && data == "0") {
                    $scope.BackShow=function()
                    {
                        return false;
                    }
					$scope._showdetailbyEnent(scheduleid, 1);
				} else if (data && data == "1") {
					$scope._showdetailbyEnent(scheduleid, 2);
				} else {
					alert(data);
				}
			});
		} else {
			showloading();
			var jsonParam = {
				obj : 'scheduleService',
				method : 'remindReference',
				count : 0
			};
			$.angularAJAX($http, jsonParam, function(data) {
				$scope.remindTimes = data;
			});

			var jsonParam1 = {
				obj : 'scheduleService',
				method : 'recycleReference',
				count : 0
			};
			$.angularAJAX($http, jsonParam1, function(data) {
				$scope.recycles = data;
				hideloading();
			});
			try {
				var param = {
					startOfWeek : 0,
					weeksInMonth : 6,
					eventHandler : {
						getEventsOnDay : function(date1, date2) {
							$scope._getEventsOnDay(date1, date2);
						},
						getImportanceOfDay : function() {
						}
					}
				};
				$("#calendarLocation").jqmCalendar(param);
				hideloading();
			} catch (e) {
			}
		}
	};

	$scope._getEventsOnDay = function(date1, date2) {
		var mm = (date1.getMonth() + 1) >= 10 ? (date1.getMonth() + 1) : "0" + (date1.getMonth() + 1);
		var param1 = date1.getFullYear() + "-" + mm;
		var monthMap = $("body").data("monthMapSchedule");
		if (monthMap != param1) {
			var jsonParam = {
				obj : 'scheduleService',
				method : 'getScheduleDaysByMonth',
				count : 1,
				param1 : param1
			};
			$.angularAJAX($http, jsonParam, function(data) {
				if (data && data != "") {
					$("body").data("monthMapSchedule", param1);
					$scope.flagMap = data;
				}
			});
		}
		jsonParam = {
			obj : 'scheduleService',
			method : 'getScheduleByDate',
			count : 1,
			param1 : date2
		};
		$.angularAJAX($http, jsonParam, function(data) {
			$("body").data("dateMapSchedule", date2);
			$scope.eventlist = data;
			$scope.showone = false;
			$scope.showtwo = false;
			if (data && data != "") {
				for (var int = 0; int < data.length; int++) {
					if (data[int].share == "0") {
						$scope.showone = true;
					} else if (data[int].share == "1") {
						$scope.showtwo = true;
					}
				}
			}
		});
	};
	$scope._showdetail = function(id, type) {
		var jsonParam = {
			obj : 'scheduleService',
			method : 'getScheduleById',
			count : 1,
			param1 : id
		};
		$.angularAJAX($http, jsonParam, function(data) {
			//navigator.pluginClose.log("显示详情");
			if (type == 1) {
				//$scope.orgtreehide = data.shareUsers;
				//$scope.orgtreeshow = data.shareUsersName;
				$scope.detail = data;
				$scope.safeApply(setTimeout(initContent,50));
			} else {
				$scope.detailother = data;
			}
		});
	};

	$scope._showdetailbyEnent = function(id, type) {
		var jsonParam = {
			obj : 'scheduleService',
			method : 'getScheduleByEventId',
			count : 1,
			param1 : id
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if (type == 1) {
				$scope.orgtreehide = data.shareUsers;
				$scope.orgtreeshow = data.shareUsersName;
				$scope.detail = data;
				$scope.safeApply(setTimeout(initContent,50));
				location.hash="#editSchedulePage";
			} else {
				$scope.detailother = data;
				//$.mobile.changePage('#pageother', {transition : "slide"});
			}
		});
	};
    $scope._isDisabled=function(url){
        if(typeof(url)=="undefined"){
            return false;
        }
        if(url.indexOf("035-408-000")>-1){
            console.log(true);
            return true;
        }
        return false;
    };
	$scope._showOrg = function() {
        if(arguments.length>0)
        {
            if(arguments[0].indexOf("035-408-000")>-1)
            {
                return;
            }
        }
		$scope._initOrgTree($scope.orgtreehide);
	};
	$scope._saveSchedule = function() {
        var url="";
        if(arguments.length>0)
        {
            url=arguments[0];
        }
        if(url.indexOf("035-408-000")>-1)
        {
            return;
        }
		showloading();
		$scope.detail.content = $("#ScheduleContent").html();
		if (!$scope.detail) {
			navigator.notification.alert("内容不能为空");
			hideloading();
			return;
		}
		var title = $scope.detail.title;
		if (!title) {
			navigator.notification.alert("标题不能为空");
			hideloading();
			return;
		}
		var content = $scope.detail.content;
		if (!content) {
			navigator.notification.alert("内容不能为空");
			hideloading();
			return;
		}
		var starttimeStr = $scope.detail.starttimeStr;
		if (!starttimeStr) {
			navigator.notification.alert("开始日期不能为空");
			hideloading();
			return;
		}
		var endtimeStr = $scope.detail.endtimeStr;
		if (!endtimeStr) {
			navigator.notification.alert("结束日期不能为空");
			hideloading();
			return;
		}
		var scheduleType =  $scope.detail.url;
		var share = $scope.orgtreehide;
		if( scheduleType.indexOf("SYS.ID=017-001-000") <=0 && share){
			navigator.notification.alert("会议日程不能分享给他人");
			hideloading();
			return;
		}
		var datelimit =Date.parse(endtimeStr) - Date.parse(starttimeStr);
		if(datelimit < 0){
			navigator.notification.alert("结束日期不能小于开始日期");
			hideloading();
			return;
		}
		var remindTime = $scope.detail.remindTime;
		if(!remindTime){
			if(remindTime == 0){
			    return;
			}
			navigator.notification.alert("提醒时间不能为空");
			hideloading();
			return;
		}
		var recycleStr = $scope.detail.recycle;
		if(!recycleStr){
			if(recycleStr == 0)
			{
			return;
			}
			navigator.notification.alert("重复周期 不能为空");
			hideloading();
			return;
		}
		$('#publish-btn').addClass("ui-state-disabled");
		$('#publish-btn1').addClass("ui-state-disabled");
		$scope.detail.shareUsers = $scope.orgtreehide;
		var txt = $("#scheduleEditContent").html();
		if (txt.length >= 1000) {
			navigator.notification.alert('内容不能超过1000字');
			$('#publish-btn').removeClass("ui-state-disabled");
			$('#publish-btn1').removeClass("ui-state-disabled");
			hideloading();
			return;
		}
		var json = angular.toJson($scope.detail);
		var jsonParam = {
			obj : 'scheduleService',
			method : 'saveSchedule',
			count : 1,
			param1 : json
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if (data && data == 'SUCCESS') {
				navigator.notification.alert('备忘保存成功');
				$scope.detail = null;
				//$scope.orgtreehide = "";
				//$scope.orgtreeshow = "";
				$scope._refresh();
				$.mobile.changePage('#pageSchedule', {
					transition : "slide",
					reverse : "true"
				});
			} else {
				navigator.notification.alert(data);
			}
			$('#publish-btn').removeClass("ui-state-disabled");
			$('#publish-btn1').removeClass("ui-state-disabled");
			hideloading();
		});
	};

	$scope._refresh = function() {
		var monthMap = $("body").data("monthMapSchedule");
		var jsonParam = {
			obj : 'scheduleService',
			method : 'getScheduleDaysByMonth',
			count : 1,
			param1 : monthMap
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if (data) {
				$scope.flagMap = data;
			}
		});
		var date2 = $("body").data("dateMapSchedule");
		jsonParam = {
			obj : 'scheduleService',
			method : 'getScheduleByDate',
			count : 1,
			param1 : date2
		};
		$.angularAJAX($http, jsonParam, function(data) {
			$scope.eventlist = data;
			$scope.showone = false;
			$scope.showtwo = false;
			if (data && data != "") {
				for (var int = 0; int < data.length; int++) {
					if (data[int].share == "0") {
						$scope.showone = true;
					} else if (data[int].share == "1") {
						$scope.showtwo = true;
					}
				}
			}
		});
	};

	$scope._joinSchedule = function(id) {
		showloading();
		$('#join-btn').addClass("ui-state-disabled");
		var jsonParam = {
			obj : 'scheduleService',
			method : 'joinScheduleById',
			count : 1,
			param1 : id
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if (data && data == "SUCCESS") {
				navigator.notification.alert('加入成功');
				$scope.detail = null;
				//$scope.orgtreehide = "";
				//$scope.orgtreeshow = "";
				$scope._refresh();
				$.mobile.changePage('#pageSchedule', {
					transition : "slide",
					reverse : "true"
				});
			} else {
				navigator.notification.alert(data);
			}
			$('#join-btn').removeClass("ui-state-disabled");
			hideloading();
		});
	};

	$scope._delSchedule = function(id,url) {
	    if(url.indexOf("035-408-000")>-1)
	    {
	        return;
	    }
		navigator.notification.confirm("是否删除备忘?",isDelete)
		function isDelete(msg){
			if(msg != '1'){//不是按下确定按钮，不删除
				return;
			}else{
				_godelete(id,url);
			}
		}

	};

	function _godelete(id,url){//执行删除
		showloading();
		$('#publish-btn').addClass("ui-state-disabled");
		$('#publish-btn1').addClass("ui-state-disabled");
		var jsonParam = {
			obj : 'scheduleService',
			method : 'deleteScheduleById',
			count : 2,
			param1 : id,
			param2 : url
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if (data && data == "SUCCESS") {
				navigator.notification.alert('删除成功');
				$scope.detail = null;
				//$scope.orgtreehide = "";
				//$scope.orgtreeshow = "";
				$scope._refresh();
				$.mobile.changePage('#pageSchedule', {
					transition : "slide",
					reverse : "true"
				});
			} else {
				navigator.notification.alert(data);
			}
			$('#publish-btn').removeClass("ui-state-disabled");
			$('#publish-btn1').removeClass("ui-state-disabled");
			hideloading();
		});
	}

	$scope.$watch('detail', function() {
		try {
			if ($scope.detail) {
				$("#remindTime").val($scope.detail.remindTime).selectmenu("refresh");
				$("#recycle").val($scope.detail.recycle).selectmenu("refresh");
			}
		} catch (e) {
			console.log(e);
		}
	});
	$scope._scheduleInit();

	$scope.isDetail = function(){
		return !isDetail;
	}
});

//注册返回键,开始是结束的事件
document.addEventListener("backbutton", closeButton, false);
//关闭事件
function closeButton(){
	//navigator.pluginClose.close();
	var whichPageId = $.mobile.activePage.attr( "id" );
	navigator.pluginClose.log("  当前id是"+whichPageId);
	if(whichPageId == "pageSchedule"){//当前是首页
		//navigator.pluginClose.log("  当前是pageSchedule");
		navigator.pluginClose.close();
	}else if(whichPageId == "pageother" && isDetail == true){//消息中心过来，直接关闭
		navigator.pluginClose.close();
	}else {
		back();
	}
}
function back(){
	$(".dwb").click();
	history.go(-1);
}