var angularVoteApp = angular.module("angularVoteApp", [ 'angularOrgTreeApp', 'ngTouch' ]);
angularVoteApp.service('voteService', function($http) {
	this.PAGECOUNT = function() {
		return 10;
	};
	this.DEFAULTPAGE = function() {
		return 1;
	};
	this._showvoteditem = function($scope, chooseItem) {
		$scope.autoload = false;
		$scope.isMulti = false;
		$scope.readonly = true;
		var jsonParam = {
			obj : 'addressBookService',
			method : 'getAddressBookFromIds',
			count : 1,
			param1 : chooseItem
		};
		$.angularAJAX($http, jsonParam, function(data) {
			// $("body").data("orgtrees", data);
            $("#pagecontact li[role=heading]").remove();
			$scope.orgtrees = data;
			$.mobile.changePage('#pagecontact', {});
		});

	};
	this._delQuestion = function($scope, id) {
		if (id && id != "") {
			var temp = $scope.votelist;
			for ( var i = 0; i < temp.length; i++) {
				if (temp[i].voteId == id) {
					temp.splice(i, 1);
					$scope.votelist = temp;
					break;
				}
			}
		}
		$.mobile.changePage('#addVotePage', {});
	};
	this._addNewQuestion = function($scope) {
		$scope.choice = "0";// 默认为单选
		$scope.questionDesc = "";
		$scope.voteId = null;
		$scope.questlist = [ {
			"questionId" : this._getRandom()
		}, {
			"questionId" : this._getRandom()
		}, {
			"questionId" : this._getRandom()
		} ];// 默认三个选项
		this._refreshRadio($scope.choice);
	};
	this._saveQuestion = function($scope) {
		var voteId = $scope.voteId;
		var questDesc = $scope.questionDesc;
		if (questDesc == "") {
			navigator.notification.alert("问题描述不能为空");
			return;
		}
		var choice = $scope.choice;
		var questlist = $scope.questlist;
		for ( var i = 0; i < questlist.length; i++) {
			if (!questlist[i].questTxt || questlist[i].questTxt == "") {
				navigator.notification.alert("选项" + (i + 1) + "不能为空");
				return;
			}
		}
		if (questlist.length < 2) {
			navigator.notification.alert("选项不能少于两项");
			return;
		}
		var questionVO = {
			"voteId" : voteId ? voteId : this._getRandom(),
			"questionDesc" : questDesc,
			"choice" : choice,
			"questlist" : questlist
		};

		if (voteId) {// 如果是修改的话，就替换
			var temp = $scope.votelist;
			for ( var i = 0; i < temp.length; i++) {
				if (temp[i].voteId == voteId) {
					temp.splice(i, 1, questionVO);
					$scope.votelist = temp;
					break;
				}
			}
		} else {
			$scope.votelist = $scope.votelist.concat(questionVO);
		}
		$.mobile.changePage('#addVotePage', {});
	};

	// 保存调查问卷
	this._saveVote = function($scope, isPublish, flag) {
		try {
			var title = $scope.title;
			if (!title) {
				navigator.notification.alert("主题不能为空");
				return;
			}
			var content = $scope.content;
			if (!content) {
				navigator.notification.alert("内容不能为空");
				return;
			}
			var endtime = $scope.endtimeStr;
			if (!endtime) {
				navigator.notification.alert("截止时间不能为空");
				return;
			}
			var orgtreehide = $scope.orgtreehide;
			if (!orgtreehide) {
				navigator.notification.alert("投票参与人不能为空");
				return;
			}
			var votelist = angular.toJson($scope.votelist);
			if (!votelist || votelist == '[]') {
				navigator.notification.alert("问题列表不能为空");
				return;
			}
			var id = $scope.id;
			if (!id) {
				id = "";
			}
			$('#save-btn').addClass("ui-state-disabled");
			$('#publish-btn').addClass("ui-state-disabled");

			var json = '{"id":"' + id + '","isPublish":"' + isPublish + '","title":"' + title + '","content":"' + content + '","endtimeStr":"' + endtime + '","orgtreehide":"' + orgtreehide + '","votelist":' + votelist + '}';
			var jsonParam = {
				obj : 'voteService',
				method : 'saveVote',
				count : 1,
				param1 : json
			};
			$.angularAJAX($http, jsonParam, function(data) {
				if (data == 'SUCCESS') {
					if (isPublish == "0") {
						navigator.notification.alert("保存成功");
					} else {
						navigator.notification.alert("发布成功");
					}
					$scope.title = "";
					$scope.content = "";
					$scope.endtimeStr = "";
					$scope.orgtreehide = "";
					$scope.orgtreeshow = "";
					$scope.choosedId = "";
					$scope.votelist = [];
					$scope.orgtrees=null;
					if(id == ""){
						$("body").data("list_published", null);
					    $scope.changePageStatue($scope.publishOptionJson);
						$.mobile.changePage('#voteListPage', {});
						$scope._publishlist();
						$('#save-btn').removeClass("ui-state-disabled");
						$('#publish-btn').removeClass("ui-state-disabled");
						return;

					}
					if (flag == "list") {// 返回到列表
						if (isPublish == "1") {
							var temp = $("body").data("list_notPublish");
							for ( var i = 0; i < temp.length; i++) {
								if (temp[i].id == id) {
									var obj = temp.splice(i, 1);
									$scope.list_R = temp;
									$("body").data("list_notPublish", temp);
									var pub = $("body").data("list_published");
									if(pub){
										$("body").data("list_published", pub.concat(obj));
										$scope.list_L = $("body").data("list_published");
										console.log($scope.list_L);
									}

									break;
								}
							}
						}
						$scope.publish=0;
						$.mobile.changePage('#voteListPage', {});
					} else {
						$('#save-btn').removeClass("ui-state-disabled");
						$('#publish-btn').removeClass("ui-state-disabled");
						WeixinJSBridge.invoke('closeWindow', {}, function(res) {});
						$.mobile.changePage('#addVotePage', {});
					}
				} else {
					navigator.notification.alert(data);
					$('#save-btn').removeClass("ui-state-disabled");
					$('#publish-btn').removeClass("ui-state-disabled");
				}
			});
		} catch (e) {
			console.log("错误信息：" + e);
		}
	};
	// 获取随机数
	this._getRandom = function() {
		var time = (new Date()).valueOf();
		var ran = Math.floor(Math.random() * (1000 + 1));
		return eval(time + ran);
	};
	this._refreshRadio = function(choice) {
		if (choice == "0") {
			$("#radio2").removeAttr("checked").checkboxradio("refresh");
			$("#radio1").prop("checked", true).checkboxradio("refresh");
		} else {
			$("#radio1").removeAttr("checked").checkboxradio("refresh");
			$("#radio2").prop("checked", true).checkboxradio("refresh");
		}
	};
	this._showresult = function($scope, id) {//查看结果
		showloading();
		$scope.isshowchart = true;
		$scope.chartdata = "";
		var jsonParam = {
			obj : 'voteService',
			method : 'getChartResults',
			count : 1,
			param1 : id
		};
		$.angularAJAX($http, jsonParam, function(data) {
			$scope.chartdata = data;
			if (data == "") {
				$scope.isshowchart = false;
			} else {
				$scope.isshowchart = true;
			}
			hideloading();
		});
	};
	this._showquestion = function($scope, id) {
		showloading();
		$scope.voteId = "";
		$scope.questionDesc = "";
		$scope.choice = "";
		$scope.questlist = "";
		var temp = $scope.votelist;
		var questionVO = "";
		for ( var i = 0; i < temp.length; i++) {
			if (temp[i].voteId == id) {
				questionVO = temp[i];
				break;
			}
		}
		$scope.voteId = questionVO.voteId;
		$scope.questionDesc = questionVO.questionDesc;
		$scope.choice = questionVO.choice;
		$scope.questlist = questionVO.questlist;
		this._refreshRadio($scope.choice);
		hideloading();
	};
	// 删除问题选项
	this._removeItem = function($scope, id) {
		var temp = $scope.questlist;
		for ( var i = 0; i < temp.length; i++) {
			if (temp[i].questionId == id) {
				temp.splice(i, 1);
				$scope.questlist = temp;// 删除掉我们已经删除的行
				break;
			}
		}
	};
	this._showOrg = function($scope) {
		$scope._initOrgTree($scope.orgtreehide);
		$scope.isMulti = true;
		$scope.readonly = false;
	};
	// 添加了一个空的选项
	this._addchoose = function($scope) {
		$scope.questlist = $scope.questlist.concat({
			"questionId" : this._getRandom()
		});// 在后面添加一个选项
	};

	this._showdefaulttime = function($scope) {
		var now = new Date();
		// $scope.endtimeStr = new
		// Date(now.getFullYear(),now.getMonth()+2,now.getDate(),now.getHours(),now.getMinutes(),now.getSeconds());
	};

});
angularVoteApp.directive('repeatDirective', function() {
	return function(scope, element, attrs) {
		if (scope.$last) {
			scope.$watch(function(value) {
				try {
					if ($("#questListview").length != 0) {
						$("#questionListview").listview("refresh");// 刷新listview
						$('#questionListview').trigger('create');// 更新textinput
					}
					if ($("#questlistlistview").length != 0) {
						$("#questlistlistview").listview("refresh");// 刷新listview
					}
				} catch (e) {
				}

			});
		}
	};
});
angularVoteApp.directive('listrepeatDirective', function() {
	return function(scope, element, attrs) {
		if (scope.$last) {
			scope.$watch(function(value) {
				try {
					if ($("#questListview").length != 0) {
						$("#questListview").listview("refresh");// 刷新listview
						$("#questListview").trigger('create');
					}
				} catch (e) {
				}
			});
		}
	};
});

angularVoteApp.directive('permissionDirective', function() {
	return function(scope, element, attrs) {
		if (scope.$last) {
			scope.$watch(function(value) {
				$("#btnPermission").trigger('create');
			});
		}
	};
});
function labelFormatter(label, series) {
	return '' + label + '(' + Math.round(series.percent) + '%)</a>';
}
angularVoteApp.directive('chartShow', function() {
	return function(scope, element, attrs) {
		scope.$watch(function(value) {
			$.plot(element, eval(attrs.content), {
				series : {
					pie : {
						show : true,
						align : 'left'
					}
				},
				legend : {
					show : true,
					labelFormatter : labelFormatter,
					position : 'se'
				}
			});
		});
	};
});

var Scope;
var isDetail = false;//是否是查看详情页面
angularVoteApp.controller("voteController",function($scope, $http, voteService){
//菜单时间
       Scope = $scope;
	    voteService._showdefaulttime($scope);
	    $scope.votelist = [];
        $scope.vote = 0;
        /*页面状态*/
        //$scope.PUBLLISHED = 0;
        $scope.VOTED = 1;
        $scope.feurl = localStorage.feurl;
        $scope.status = $scope.VOTED;
        $scope.voteOptionJson={
        		    "btnName_l":"未投票",
        	        "btnName_R" : "已投票",
        	        "vote" : 1,
        	        "ahref_done" : "#pageVoteDetail",
        	        "ahref_notdo" : "#pageVoteDetail",
        	        "status":$scope.VOTED
        };
        $scope.publishOptionJson={
        		 "btnName_l":"已发布",
    	         "btnName_R" : "未发布",
    	        "vote" : 0,
    	        "ahref_done" : "#pageVoteDetail",
    	        "ahref_notdo" : "#addVotePage",
    	        "status":$scope.PUBLLISHED
        };

        $scope.changePageStatue = function(Json){
        	$scope.btnName_l = Json.btnName_l;
            $scope.btnName_R = Json.btnName_R;
            $scope.ahref_done = Json.ahref_done;
            $scope.ahref_notdo = Json.ahref_notdo;
            $scope.vote = Json.vote;
            $scope.status = Json.status;
            $("#nextBtn,#nextBtn1").html("更多");
            $("#nextBtn,#nextBtn1").removeClass("ui-state-disabled");


        }

        $scope.changePageStatue($scope.voteOptionJson);


    $scope._listswiperight = function() {
        if($scope.status == $scope.PUBLLISHED)
        {
          $scope._publishlist();
            return;
        }
		$scope._voteList();
	};
	$scope._listswipeleft = function() {
         if($scope.status == $scope.PUBLLISHED)
        {
          $scope._notPublishList();
            return;
        }
        $scope._initNotVote();
	};

    $scope.enterVote_R = function(id){
         if($scope.status == $scope.PUBLLISHED)
        {
          $scope._showeditdetail(id);
            return;
        }
        $scope._showdetail(id);
    };

    $scope._loadNext = function(){
        if($scope.status == $scope.PUBLLISHED)
        {
          $scope._publishNext();
            return;
        }
        $scope._notVoteNext();
    }

    $scope._tagAction = function(){
          _voteSetStatus($scope,0);
    	if($scope.status == $scope.PUBLLISHED)
        {
          $scope._publishlist();
            return;
        }
        $scope._initNotVote();

    }

     $scope._tagNotAction = function(){
          _voteSetStatus($scope,1);
    	if($scope.status == $scope.PUBLLISHED){
          $scope._notPublishList();
            return;
        }
        $scope._voteList();
    }

     $scope._loadNotNext = function(){
        if($scope.status == $scope.PUBLLISHED)
        {
          $scope._notPublishNext();
            return;
        }
        $scope._voteNext();
     }

	$scope._addswiperight = function() {
		$.mobile.changePage('#voteListPage', {
			transition : "slide",
			reverse : "true"
		});
	};
	$scope._detailswiperight = function() {
		$.mobile.changePage('#voteListPage', {
			transition : "slide",
			reverse : "true"
		});
	};

    $scope._listinit = function() {
       // $scope.publish=-1;
       var voteid =null;
       if(location.href.lastIndexOf("?id=")>-1)
       {
           var pattern=new RegExp("\\?id=(.+)");
           var result;
           if((result=pattern.exec(location.href))!=null)
           {
               voteid=result[1];
           }
       }
		if (voteid && voteid != "") {
			isDetail = true;
			$scope.publish = 0;
			$scope.postVoteid = voteid;
			$scope._showdetail(voteid);
		} else {
			showloading();
			var list = $("body").data("list_notVote");
			if (list) {
				$scope.list_L = list;
				hideloading();
			} else {
			if(window.localStorage)
			{
			   var firstVisitFlag= sessionStorage.getItem("vote-firstVisitFlag");
			   if(firstVisitFlag==null)
			   {
			     $scope._initNotVote();
			     sessionStorage.setItem("vote-firstVisitFlag", 1);
			   }
			   else
			   {
                    if(localStorage.vote_status==1)
                    {
                        $scope.changePageStatue($scope.voteOptionJson);
                        if(localStorage.MNo==1)
                        {
                            $scope._voteList();
                        }
                        else
                        {
                            $scope._initNotVote();
                        }
                    }
                    else
                    {
                        $scope.changePageStatue($scope.publishOptionJson);
                        if(localStorage.MNo==1)
                        {
                            $scope._notPublishList();
                        }
                        else
                        {
                            $scope._publishlist();
                        }
                    }
			   }

			}
			else
			{
			    $scope._initNotVote();
			}


			}
		}
	};

	$scope._initNotVote=function()
	{
		$scope.publish = 0;
		$scope.status=1;
        _voteSetStatus($scope,0);
        var jsonParam = {
            obj : 'voteService',
            method : 'getNotVote',
            count : 2,
            param1 : voteService.DEFAULTPAGE(),
            param2 : voteService.PAGECOUNT()
        };
        $.angularAJAX($http, jsonParam, function(data) {
            $scope.list_L = data;
            $("body").data("page_notVote", voteService.DEFAULTPAGE());
            $("body").data("list_notVote", data);
            hideloading();
        });
	}

	$scope._showvoteditem = function(chooseItem) {
		voteService._showvoteditem($scope, chooseItem);
	};

	$scope._notVoteNext = function() {
		showloading();
		var page = $("body").data("page_notVote");
		if (page) {
			page = eval(parseInt(page) + 1);
		} else {
			page = voteService.DEFAULTPAGE();
		}
		var jsonParam = {
			obj : 'voteService',
			method : 'getNotVote',
			count : 2,
			param1 : page,
			param2 : voteService.PAGECOUNT()
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if ($.trim(data) != "") {
				$("body").data("list_notVote", $scope.list_L.concat(data));
				$scope.list_L = $("body").data("list_notVote");
				$("body").data("page_notVote", page);
			} else {
				$("#nextBtn").html("数据加载完毕");
				$("#nextBtn").addClass("ui-state-disabled");
			}
			hideloading();
		});
	};

	$scope._voteList = function() {
		showloading();
		$scope.publish = 1;
		var list = $("body").data("list_vote");

		if (list) {
			$scope.list_R = list;
			hideloading();
		} else {
			var jsonParam = {
				obj : 'voteService',
				method : 'getVoted',
				count : 2,
				param1 : voteService.DEFAULTPAGE(),
				param2 : voteService.PAGECOUNT()
			};
			$.angularAJAX($http, jsonParam, function(data) {
				$scope.list_R = data;
				$("body").data("page_vote", voteService.DEFAULTPAGE());
				$("body").data("list_vote", data);
				hideloading();
			});
		}
	};

	$scope._voteNext = function() {
		showloading();
		var page = $("body").data("page_vote");
		if (page) {
			page = eval(parseInt(page) + 1);
		} else {
			page = voteService.DEFAULTPAGE();
		}
		var jsonParam = {
			obj : 'voteService',
			method : 'getVoted',
			count : 2,
			param1 : page,
			param2 : voteService.PAGECOUNT()
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if ($.trim(data) != "") {
				$("body").data("list_vote", $scope.list_R.concat(data));
				$scope.list_R = $scope.list_R.concat(data);
				$("body").data("page_vote", page);
			} else {
				$("#nextBtn1").html("数据加载完毕");
				$("#nextBtn1").addClass("ui-state-disabled");
			}
			hideloading();
		});
	};
	$scope._submit = function() {
		var detail = $scope.votedetail;
		var votelist = detail.votelist;
		for ( var i = 0; i < votelist.length; i++) {
			var temp = votelist[i];
			if (temp.choice == "0") {
				// 单选
				if (!temp.selected || temp.selected == "") {
					navigator.notification.alert('请填完再提交');
					return;
				}
			} else {
				var list = temp.questlist;
				var flag = false;
				for ( var int = 0; int < list.length; int++) {
					if (list[int].selected && list[int].selected==true) {
						flag = true;
						break;
					}
				}
				if (flag == false || flag == "false") {
					navigator.notification.alert('请填完再提交');
					return;
				}
			}
		}
		var json = angular.toJson(detail);
		var jsonParam = {"obj" : "voteService","method" : "saveVoteDetail","count" : "1"};
		var datas = "param1="+json;
		$.angularAJAX($http,jsonParam,function(data) {
			if (data == 'SUCCESS') {
				var temp = $("body").data("list_notVote");
				var postVoteid = $scope.postVoteid;
				if (!postVoteid || postVoteid == "") {
					for ( var i = 0; i < temp.length; i++) {
						if (temp[i].voteId == json.voteId) {
							var obj = temp.splice(i, 1);
							$("body").data("list_notVote",temp);
							var votedlist = $("body").data("list_vote");
							if(votedlist){
								$("body").data("list_vote",votedlist.concat(obj));
							}
							break;
						}
					}
					navigator.notification.alert("投票成功");
					$scope.publish=0;
					$.mobile.changePage('#voteListPage', {});
				} else {
					navigator.notification.alert("投票成功");
					$scope._showdetail(postVoteid);
					$.mobile.changePage('#pageVoteDetail', {});
				}

			} else {
				navigator.notification.alert(data);
			}
		},function (){},datas);
	};
	$scope._notPublishList = function() {
		$scope.publish = 1;
		showloading();
		var list = $("body").data("list_notPublish");
		if (list) {
			$scope.list_R = list;
			hideloading();
		} else {
			var jsonParam = {
				obj : 'voteService',
				method : 'getNotPublishList',
				count : 2,
				param1 : voteService.DEFAULTPAGE(),
				param2 : voteService.PAGECOUNT()
			};
			$.angularAJAX($http, jsonParam, function(data) {
				$scope.list_R = data;
				$("body").data("page_notPublish", voteService.DEFAULTPAGE());
				$("body").data("list_notPublish", data);
				hideloading();
			});
		}
	};


	$scope._notPublishNext = function() {
		showloading();
		var page = $("body").data("page_notPublish");
		if (page) {
			page = eval(parseInt(page) + 1);
		} else {
			page = voteService.DEFAULTPAGE();
		}
		var jsonParam = {
			obj : 'voteService',
			method : 'getNotPublishList',
			count : 2,
			param1 : page,
			param2 : voteService.PAGECOUNT()
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if ($.trim(data) != "") {
				$("body").data("list_notPublish", $scope.list_R.concat(data));
				$scope.list_R = $scope.list_R.concat(data);
				$("body").data("page_notPublish", page);
			} else {
				$("#nextBtn1").html("数据加载完毕");
				$("#nextBtn1").addClass("ui-state-disabled");
			}
			hideloading();
		});
	};

	$scope._publishlist = function() {
		$scope.publish = 0;
		showloading();
		var list = $("body").data("list_published");
		if (list) {
			$scope.list_L = list;
			hideloading();
		} else {
			var jsonParam = {
				obj : 'voteService',
				method : 'getPublishList',
				count : 2,
				param1 : voteService.DEFAULTPAGE(),
				param2 : voteService.PAGECOUNT()
			};
			$.angularAJAX($http, jsonParam, function(data) {
				console.log(data);
				$scope.list_L = data;
				$("body").data("page_published", voteService.DEFAULTPAGE());
				$("body").data("list_published", data);
				hideloading();
			});
		}
	};

	$scope._publishNext = function() {
		showloading();
		var page = $("body").data("page_published");
		if (page) {
			page = eval(parseInt(page) + 1);
		} else {
			page = voteService.DEFAULTPAGE();
		}
		var jsonParam = {
			obj : 'voteService',
			method : 'getPublishList',
			count : 2,
			param1 : page,
			param2 : voteService.PAGECOUNT()
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if ($.trim(data) != "") {
				$("body").data("list_published", $scope.list_L.concat(data));
				$scope.list_L = $scope.list_L.concat(data);
				$("body").data("page_published", page);
			} else {
				$("#nextBtn").html("数据加载完毕");
				$("#nextBtn").addClass("ui-state-disabled");
			}
			hideloading($("body").data("page_published"));
		});
	};

	$scope._showdetail = function(id) {//已发布
		showloading();
		$scope.publish = 0;
		$scope.id= id;
		var jsonParam = {
			obj : 'voteService',
			method : 'getVoteDetail',
			count : 1,
			param1 : id
		};

		$.angularAJAX($http, jsonParam, function(data) {
			$scope.votedetail = data;
			hideloading();
		});
	};

	$scope._showeditdetail = function(id) {//未发布
		showloading();
		$scope.id= id;
		$scope.publish = 1;
		$('#save-btn').removeClass("ui-state-disabled");
		$('#publish-btn').removeClass("ui-state-disabled");
		var jsonParam = {
			obj : 'voteService',
			method : 'getVoteDetail',
			count : 2,
			param1 : id,
		    param2 : 'EDIT'
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if (data && data != "") {
				$scope.id = id;
				$scope.title = data.title;
				$scope.content = data.content;
				$scope.endtimeStr = data.endtimeStr;
				$scope.orgtreehide = data.orgtreehide;
				$scope.orgtreeshow = data.orgtreeshow;
				$scope.votelist = data.votelist;

			}
			hideloading();
		});
	};
	$scope._showresult = function(id) {
		voteService._showresult($scope, id);
	};

	$scope._deleteVote = function(id) {
		navigator.notification.confirm("是否撤销此调查问卷?",isDelete)
		function isDelete(msg){
			if(msg != '1'){//不是按下确定按钮，不删除
				return;
			}else{
				_godelete(id);
			}
		}

	};

	function _godelete(id){
		var jsonParam = {
				obj : 'voteService',
				method : 'cancelVote',
				count : 1,
				param1 : id
			};
			$.angularAJAX($http, jsonParam, function(data) {
				if (data && data != "SUCCESS") {
					navigator.notification.alert(data);
				} else {
					// 如果成功了的话，就先删除掉已发布列表里的数据，并且把删除的数据添加到待发布列表里。$scope.publishlist
					var temp = $("body").data("list_published");
					console.log(">>>>>>>>>"+temp);
					for ( var i = 0; i < temp.length; i++) {
						console.log(">>>>>>>>>"+temp[i].id+">>>>>>>>>"+$scope.id);
						if (temp[i].id == $scope.id) {
							var obj = temp.splice(i, 1);
							$scope.list_L = temp;
							$("body").data("list_published",temp);
							var notpub = $("body").data("list_notPublish");
							if (notpub) {
								$scope.list_R = notpub.concat(obj);
								$("body").data("list_notPublish", $scope.list_R);
							}
							break;
						}
					}
					publish=0;
					$.mobile.changePage('#voteListPage', {});
				}
				hideloading();
			});
	}

	$scope._showOrg = function() {
		voteService._showOrg($scope);
	};
	// 删除问题选项
	$scope._removeItem = function(id) {
		voteService._removeItem($scope, id);
	};
	// 新增一个默认的添加问题界面
	$scope._addNewQuestion = function() {
		voteService._addNewQuestion($scope);
	};
	// 显示问题界面
	$scope._showquestion = function(id) {
		voteService._showquestion($scope, id);
	};
	// 添加了一个空的选项
	$scope._addchoose = function() {
		voteService._addchoose($scope);
	};
	// 保存问题
	$scope._saveQuestion = function() {
		voteService._saveQuestion($scope);
	};
	// 保存调查问卷
	$scope._saveVote = function(isPublish) {
		voteService._saveVote($scope, isPublish, "list");
	};
	// 删除问题
	$scope._delQuestion = function(id) {
		voteService._delQuestion($scope, id);
	};
         /*myvote.method*/
	showloading();
    initapp($scope._listinit);

    $scope.backHis = function(){
        history.go(-1);
}

$scope.addPage = function(){

	$scope.publish = 0;
	$scope.id = "";
	$scope.title = "";
	$scope.content = "";
	$scope.endtimeStr = "";
	$scope.orgtreehide = "";
	$scope.orgtreeshow = "";
	$scope.choosedId = "";
	$scope.votelist = [];
    $.mobile.changePage('#addVotePage', {});
}

$scope.publishPage = function(){
    $scope.status=0;
    _voteSetStatus($scope,0);
    $scope.changePageStatue($scope.publishOptionJson);
    $.mobile.changePage('#voteListPage', {});
    showloading();
    $scope._publishlist();

}

$scope.votePage = function(){
    $scope.status=1;
    _voteSetStatus($scope,0);
    $scope.changePageStatue($scope.voteOptionJson);
    $.mobile.changePage('#voteListPage', {});
    showloading();
    $scope._initNotVote();

}
$scope.isDetail = function(){
		return !isDetail;
	}
});
//注册返回键,开始是结束的事件
document.addEventListener("backbutton", _backButton, false);

function _backButton(){
	var whichPageId = $.mobile.activePage.attr( "id" );
	//navigator.pluginClose.log("  当前id是 "+whichPageId);
	if(whichPageId == 'voteListPage'){//当前list页面
		navigator.pluginClose.close();
	}
	else if(isDetail == true && whichPageId == 'pageVoteDetail'){//消息中心过来的数据
		navigator.pluginClose.close();
	}else{
		$(".dwb").click();
		history.go(-1);
	}
}

function _voteSetStatus($scope,MNo)
{
    if(window.localStorage)
    {
        localStorage.vote_status=$scope.status;
        localStorage.MNo=MNo;
    }
}