var angularActivityApp = angular.module("angularActivityApp", ['ngTouch']);

angularActivityApp.directive('repeatDirective', function() {
			return function(scope, element, attrs) {
				if (scope.$last) {
					scope.$watch(function(value) {
								try {
									$("#activityListview").listview("refresh");
								} catch (e) {
									console.log("错误信息" + e);
								}
							});
				}
			};
		});
angularActivityApp.directive('loadEditor', function() {
			return function(scope, element, attrs) {
				scope.$watch(function(value) {
				var _activityeditor_ = new UM.Editor();
				_activityeditor_.render("commentcontent");
				_activityeditor_.ready(function() {
				var html = $("#_commentcontent_").val();
				_activityeditor_.setContent(html);

				$("#commentcontent").removeAttr("style");
				$("#commentcontent p").css("background-color", "");
				$("#commentcontent span").css("background-color", "");
				$("#commentcontent img").each(function (i, img) {
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
function activity_Intru(){
	var _activityeditor_ = new UM.Editor();
	_activityeditor_.render("activitycontent");
	_activityeditor_.ready(function() {
	var html = $("#_activitycontent_").val();
	_activityeditor_.setContent(html);

	$("#activitycontent").removeAttr("style");
	$("#activitycontent p").css("background-color", "");
	$("#activitycontent span").css("background-color", "");
	$("#activitycontent img").each(function (i, img) {
		img.src = getImgSrc(img.src);//将图片地址标准化，加上地址url
		$(img).on("load", function () {
			if (img.width > $(window).width()) {
				img.style.width = "100%";
			}
				hideloading();
		});
	});
	});
}
		
var isDetail = false;// 是否是直接查看详情页面

angularActivityApp.controller("activityController", ['$scope', '$http',
		function($scope, $http) {
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
	
			var DEFAULTPAGE = 1;
			showloading();
			$scope.feurl = localStorage.feurl;
			$scope.inited =0; 
			$scope._init = function() {
			    var activityid =null;
			    if(location.href.lastIndexOf("?id=")>-1)
			    {
			        var pattern=new RegExp("\\?id=(.+)");
                    var result;
                    if((result=pattern.exec(location.href))!=null)
                    {
                        activityid=result[1];
                    }
			    }
				//var activityid = getParameter("id");
				if (activityid && activityid != "") {
					isDetail = true;
					var jsonParam = {
						obj : 'activityService',
						method : 'getActivityVO',
						count : 1,
						param1 : activityid
					};

					$.angularAJAX($http, jsonParam, function(data) {
								hideloading();
                                $("#main").removeAttr("style");
								if (!data.activename) {
                                    var newDiv = document.createElement("div");
                                    newDiv.style.display = "block";
                                    newDiv.style.position="absolute";
                                    newDiv.style.top=0;
                                    newDiv.style.left=0;
                                    newDiv.style.zIndex=999;
                                    newDiv.style.backgroundColor="#FFF";
                                    var w=document.body.clientWidth;
                                    var h=document.body.clientHeight;
                                    newDiv.style.width=w+"px";
                                    newDiv.style.height=h+"px";
                                    newDiv.onclick=function(){navigator.pluginClose.close();};
                                    document.body.removeChild(document.getElementById("editActivity"));
                                    newDiv.focus=function(){navigator.pluginClose.close();};
                                    document.body.appendChild(newDiv);
                                    navigator.notification.alert("当前活动已被删除!",function(){navigator.pluginClose.close();});
								} else {
									$scope.detail = data;
									/**
									$.mobile.changePage('#editActivity', {
												transition : "slide"
											});
											**/
									$scope.safeApply(setTimeout(activity_Intru,50));
								}
							});

				} else {
					$scope.ising = 1;
					$("body").data("page", DEFAULTPAGE);
					var jsonParam = {
						obj : 'activityService',
						method : 'getAllList',
						count : 1,
						param1 : DEFAULTPAGE
					};
					$.angularAJAX($http, jsonParam, function(data) {
								if (data) {
									$scope.activitylist = data;
									hideloading();
                                  $("#main").removeAttr("style");
								}
							});
				}
				
			};

			$scope._next = function() {
				showloading();
				var page = $("body").data("page");
				if (page) {
					page = eval(parseInt(page) + 1);
				} else {
					page = DEFAULTPAGE;
				}
				var jsonParam = {
					obj : 'activityService',
					method : 'getAllList',
					count : 1,
					param1 : page
				};
				$.angularAJAX($http, jsonParam, function(data) {
							if (data && $.trim(data) != "") {
								$scope.activitylist = $scope.activitylist
										.concat(data);
								$("body").data("page", page);
							} else {
								$("#nextBtn").html("数据加载完毕");
								$("#nextBtn").addClass("ui-state-disabled");
							}
							hideloading();
						});
			};

			$scope._registration = function(id) {
				$scope.ising = 0;
				var jsonParam = {
					obj : 'activityService',
					method : 'activeApply',
					count : 1,
					param1 : id
				};
				$.angularAJAX($http, jsonParam, function(data) {
					if (data && data != "") {
						if (data == '1') {
							navigator.notification.alert('参加成功', function() {
										if (!isDetail) {
											$.mobile.changePage('#main', {
														transition : "slide",
														reverse : "true"
													});
											$scope.ising = 0;
										} else {
											navigator.pluginClose.close();
										}
									});

							for (var i = 0; i <= $scope.activitylist.length; i++) {
								if ($scope.activitylist[i].activeid == id) {
									$scope.activitylist[i].activestatus = "已报名";
									$scope.activitylist[i].appnum = parseInt($scope.activitylist[i].appnum)
											+ 1;
									break;
								}
							}

						} else if (data == '2') {
							navigator.notification.alert('活动已经结束');
						} else if (data == '0') {
							navigator.notification.alert('您已经报过名了');
						}
					}
				});
			};

			$scope._cancelregistration = function(id) {
				$scope.ising = 0;
				var jsonParam = {
					obj : 'activityService',
					method : 'deleteActiveApp',
					count : 1,
					param1 : id
				};
				$.angularAJAX($http, jsonParam, function(data) {
					if (data && data == 'success') {
						navigator.notification.alert('退出成功', function() {
									if (!isDetail) {
										$.mobile.changePage('#main', {
													transition : "slide",
													reverse : "true"
												});
									} else {
										navigator.pluginClose.close();
									}

								});
						for (var i = 0; i <= $scope.activitylist.length; i++) {
							console.log($scope.activitylist[i].activeid);
							if ($scope.activitylist[i].activeid == id) {
								console.log(1111);
								$scope.activitylist[i].activestatus = "报名中";
								$scope.activitylist[i].appnum = parseInt($scope.activitylist[i].appnum)
										- 1;
								break;
							}
						}
					} else {
						navigator.notification.alert("取消失败");
					}
				});
			};

			$scope._ed = function() {
				var jsonParam = {
					obj : 'activityService',
					method : 'getListed',
					count : 1,
					param1 : "1"
				};
				$.angularAJAX($http, jsonParam, function(data) {
							if (data) {
								$scope.activitylist = data;
							}
						});
			};

			$scope._showdetail = function(activeid) {
				showloading();
				var jsonParam = {
					obj : 'activityService',
					method : 'getActivityVO',
					count : 1,
					param1 : activeid
				};
				$.angularAJAX($http, jsonParam, function(data) {
							if (data) {
								$scope.detail = data;
								$scope.safeApply(setTimeout(activity_Intru,50));
								
							}
						});
				
			};

			$scope._addcomment = function(activeid) {
				var message = $scope.discussmsg;
				if (!message) {
					navigator.notification.alert("评论内容不能为空!");
					return;
				}
				var state = $scope.detail.activestatus; 
				if(state == '已结束')
				{
					navigator.notification.alert("活动已结束!");
					return;
				}
				var jsonParam = {
					obj : 'activityService',
					method : 'addComment',
					count : 2,
					param1 : activeid,
					param2 : message
				};
				$.angularAJAX($http, jsonParam, function(data) {
							if (data && data != '') {
								$scope.showIntput();
								$scope.detail.comments = $scope.detail.comments
										.concat(data);
							}
							$scope.discussmsg = "";
						});
			};

			$scope._isshowcancel = function(state) {
				if (state == '报名中') {
					return true;
				} else {
					return false;
				}
			};
			$scope._isshow = function(state) {
				if (state != '报名中') {
					return true;
				} else {
					return false;
				}
			};
			$scope.statusForBotton = function(status) {
				switch (status) {
					case "报名中" :
						return "参加活动";
					case "已报名" :
						return "退出活动";
					case "已结束" :
						return "活动已结束";

				}
			}
			$scope.statusForBottonAction = function(status, activeid) {
				switch (status) {
					case "报名中" :
						return $scope._registration(activeid);
					case "已报名" :
						return $scope._cancelregistration(activeid);
					case "已结束" :
						return "";

				}
			}
			$scope.timeDebug = function(time) {
				var date = time;
				date = date.substring(0, date.lastIndexOf(":"));
				return date;
			};
			$scope.showIntput = function(index) {
				if (!index) {
					$("#discuss_bar").toggleClass("silde_up");
					return;
				}
				$("#discuss_bar").removeClass("silde_up");
			}
			$scope.statusForColor = function(status) {
				switch (status) {
					case "已报名" :
						return "#048d25";
					case "已结束" :
						return "#999";
					case "报名中" :
						return "#f26a43";
				}
			}
			initapp($scope._init);

			$scope.isDetail = function() {
				return !isDetail;
			}
		}]);

// 注册返回键,开始是结束的事件
document.addEventListener("backbutton", _backButton, false);

function _backButton() {
	var whichPageId = $.mobile.activePage.attr("id");
	// navigator.pluginClose.log(" 当前id是 "+whichPageId);
	if (whichPageId == 'main') {// 当前main list页面
		navigator.pluginClose.close();
	} else if (isDetail == true && whichPageId == 'editActivity') {
		navigator.pluginClose.close();
	} else {
		history.go(-1);
	}
}
