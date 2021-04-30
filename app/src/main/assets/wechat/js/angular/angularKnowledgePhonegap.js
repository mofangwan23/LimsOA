var angularKnowledgeApp = angular.module("angularKnowledgeApp", [ 'angularOrgTreeApp', 'ngTouch' ]);
angularKnowledgeApp.directive('kmRepeatDirective', function() {
	return function(scope, element, attrs) {
		if (scope.$last) {
			scope.$watch(function(value) {
				try {
					var isUnitRefresh=false;
					var ss =location.href.split("#");
					if(ss.length>1)
					{
						if(ss[1]=="pageCorpknowledge")
						{
							isUnitRefresh=true;
						}
					}
					if(isUnitRefresh==true)
					{
						$("#kmlistviewcorp").listview("refresh");
					}
					else
					{
						$("#kmlistviewuser").listview("refresh");
					}
				} catch (e) {
				}
			});
		}
	};
});
angularKnowledgeApp.service('knowledgeService', function($http) {
	this._showmore = function($scope, id, isFolder, obj, type) {
		showloading();
		if(isFolder=="1"){
		    id = obj.folderid;
		}else{
		    id= obj.fileid;
		}
		$('input[data-type="search"]').val("");
		$scope.imgsrc = "";
		if ($scope._isUserFolder(type)) {
			$scope._pagename = "#pageUserknowledge";
		} else if ($scope._isCorpFolder(type)) {
			$scope._pagename = "#pageCorpknowledge";
		}
		if (isFolder == '1') {
			//移除关闭,往前事件
			document.removeEventListener('backbutton',closeButton,false);			
			//注册详情返回键
			document.addEventListener("backbutton", goBackButton, false);
			if ($scope._isUserFolder(type)) {
				$scope.userTitlebar = obj.foldername;
				$scope._showFolderAndFileUser(id);
			} else if ($scope._isCorpFolder(type)) {
				$scope.corpTitlebar = obj.foldername;
				$scope._showFolderAndFileCorp(id);
			}
			if(window.localStorage)
			{
				localStorage.kn_folderId=id;
				localStorage.kn_type=type;
				localStorage.kn_foldername=obj.foldername;				
			}
		} else {
			//移除关闭
			document.removeEventListener('backbutton',closeButton,false);
			document.removeEventListener('backbutton',goBackButton,false);
			//注册返回键
			document.addEventListener("backbutton", detailBack, false);
			$("#downloadbtn").removeClass("ui-state-disabled");
			var imgType = [ '.jpg', '.jpeg', '.png', '.gif', '.bmp' ];
			$scope.showfileid = localStorage.feurl+"/servlet/mobileAttachmentServlet?fileGuid=" + id;
			$scope.fileid = id;
			if ($.inArray(obj.filetype, imgType) >= 0) {
				$scope.imgsrc = $scope.showfileid;
				$scope.imgstyle = "width: 100%;";
			} else {
                if(!obj.imgsrc || obj.imgsrc==""){
                    obj.imgsrc = "../../css/images/kmicon/"+obj.filetype.replace(".","")+".png";
                }
				$scope.imgsrc = obj.imgsrc;
				$scope.imgstyle = "margin-top: 2em;";
			}
			$scope._hasdelPermission(id);
			$scope.showfilename = obj.title + obj.filetype;
			$scope.pubname = obj.pubUserName;
			$scope.pubtime = obj.pubTimeLong;
			$scope.filesize = obj.filesize;
			$scope.filepath= obj.filepath;
			$scope.expiredtime = obj.expiredtimelong ? obj.expiredtimelong : "无限";
			$scope.filetype = obj.filetype;
			$.mobile.changePage('#pageknowledgeshow', {});
			if($scope._pagename == "#pageUserknowledge"){//个人文件
				$scope.detailTitlebar = "个人文件";
			}else{
				$scope.detailTitlebar = "单位文件";
			}
			if(window.localStorage)
			{
				localStorage.kn_fid=obj.fileid;
			}
		}
	};

	this._dodownload = function($scope, id) {
		navigator.fileDownload.download(localStorage.feurl+"/servlet/mobileAttachmentServlet?fileGuid="+id,$scope.showfilename,id,$scope.filetype);
	};

	this._sharefile = function($scope, fileid) {
		showloading();
		$scope._initOrgTree();// 加载全部人员
		$scope.readonly = false;
		$scope.isMulti = true;// 是否多选
		$scope.sharefileid = fileid;
		$scope._callback = "$scope._callbackok()";// 成功后回调的方法
		document.removeEventListener('backbutton',changeUserOrCorp,false);
		document.removeEventListener("backbutton",detailBack,false);//移除详情的返回键
		document.addEventListener("backbutton",shareBack,false);//增加分享的返回键
		hideloading();
	};
	this._kmswiperight = function() {
		$.mobile.changePage('#pageknowledge', {
			transition : "slide",
			reverse : "true"
		});
	};
	this._callbackok = function($scope) {
		showloading();
		var users = $scope.orgtreehide;
		var fileid = $scope.sharefileid;
		var showname = $scope.orgtreeshow;
		var jsonParam = {
			obj : 'knowledgeService',
			method : 'publishFile',
			count : 2,
			param1 : fileid,
			param2 : users
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if (data == "") {
				navigator.notification.alert("分享失败");
			} else {
				navigator.notification.alert("给" + showname + "分享文档成功");
				document.removeEventListener("backbutton",shareBack,false);
				document.addEventListener("backbutton",detailBack,false);
			}
			hideloading();
		});
	};

	this._deletefile = function($scope, fileid) {
		var id = "'" + fileid + "',";
		navigator.notification.confirm("确认删除此文件？",isDelete);
		function isDelete(msg){
        			if(msg != '1'){//不是按下确定按钮，不删除
        				return;
        			}else{
        				_godelete($scope,fileid);
        			}
        }
	}

	function _godelete($scope,fileid){
	        var id = "'" + fileid + "',";
			$("#deletebtn").addClass("ui-state-disabled");
			var jsonParam = {
				obj : 'fileAutoPOF',
				method : 'delfile',
				count : 1,
				param1 : id
			};
			$.angularAJAX($http, jsonParam, function(data) {
				if (data) {
					if($scope._pagename == "#pageUserknowledge"){
						//移除关闭
						document.removeEventListener('backbutton',closeButton,false);
						document.removeEventListener('backbutton',detailBack,false);
						//注册返回键
						document.addEventListener("backbutton", goBackButton, false);
						var temp = $scope.userFolders;
						for (var i = 0; i < temp.length; i++) {
							if (temp[i].fileid == fileid) {
								temp.splice(i, 1);
								$scope.userFolders = temp;// 删除掉我们已经删除的行
								$.mobile.changePage('#pageUserknowledge', {});
								break;
							}
						}
					}else if($scope._pagename == "#pageCorpknowledge"){
						//移除关闭
						document.removeEventListener('backbutton',closeButton,false);
						document.removeEventListener('backbutton',detailBack,false);
						//注册返回键
						document.addEventListener("backbutton", goBackButton, false);
						var temp = $scope.corpFolders;
						for (var i = 0; i < temp.length; i++) {
							if (temp[i].fileid == fileid) {
								temp.splice(i, 1);
								$scope.corpFolders = temp;// 删除掉我们已经删除的行
								$.mobile.changePage('#pageCorpknowledge', {});
								break;
							}
						}
					}
					$("#deletebtn").removeClass("ui-state-disabled");
				} else {
					navigator.notification.alert("删除失败");
					$("#deletebtn").removeClass("ui-state-disabled");
				}
			});
	}

	
	this._goback = function($scope, method) {
		showloading();
		//navigator.pluginClose.log("goback");//输出，debug
		var id = "";
		if ($scope._isUserFolder(method)) {
			id = $("body").data("userfolderid");
		} else if ($scope._isCorpFolder(method)) {
			id = $("body").data("corpfolderid");
		}
		var jsonParam = {
			obj : 'knowledgeService',
			method : method,
			count : 1,
			param1 : id
		};
		$.angularAJAX($http, jsonParam, function(data) {
			$scope.parentId = data;
			$scope._showFolderName(data, method);
			if ($scope._isUserFolder(method)) {
				$scope._showFolderAndFileUser($scope.parentId);
				$("body").data("userfolderid", data);
			} else if ($scope._isCorpFolder(method)) {
				$scope._showFolderAndFileCorp($scope.parentId);
				$("body").data("corpfolderid", data);
			}
			hideloading();
		});
	};
	this._showFolderName = function($scope,pid, method) {
		var jsonParam = {
			obj : 'knowledgeService',
			method : 'getParentFolderName',
			count : 1,
			param1 : pid
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if ($scope._isUserFolder(method)) {
				if (pid == 4) {
					$scope.userTitlebar = "个人文件";
					//navigator.pluginClose.log("===个人文件");
					//移除其他返回键，注册关闭返回键
					document.removeEventListener('backbutton',goBackButton,false);
					document.removeEventListener('backbutton',detailBack,false);
					document.addEventListener('backbutton',closeButton,false);
				} else {
					$scope.userTitlebar = data;
					//navigator.pluginClose.log("---->"+$scope.corpTitlebar);
				}
			} else if ($scope._isCorpFolder(method)) {
				if (pid == 3) {
					$scope.corpTitlebar = "单位文件";
					//navigator.pluginClose.log("====单位文件");
					//移除其他返回键，注册关闭返回键
					document.removeEventListener('backbutton',goBackButton,false);
					document.removeEventListener('backbutton',detailBack,false);
					document.addEventListener('backbutton',closeButton,false);
				} else {
					$scope.corpTitlebar = data;
					//navigator.pluginClose.log("---->"+$scope.corpTitlebar);
				}
			}
		});
	};
	this._showFolderAndFile = function($scope, id, method,type) {
		showloading();
		var jsonParam = {
			obj : 'knowledgeService',
			method : method,
			count : 1,
			param1 : id
		};
		var s_this=this;
		$.angularAJAX($http, jsonParam, function(data) {
			if ($scope._isUserFolder(type)) {
				$scope.userFolders = data;
				$("body").data("userfolderid", id);
				//navigator.pluginClose.log("...UserFolder");
			} else if ($scope._isCorpFolder(type)) {
				$scope.corpFolders = data;
				$("body").data("corpfolderid", id);
				//navigator.pluginClose.log("...CorpFolder");
			}
			var ss =location.href.split("#");
			if(ss[1]=="pageknowledgeshow")
			{
				if(window.localStorage)
				{
					var kn_fid=localStorage.kn_fid;
					console.log("kn_fid="+kn_fid+",kn_type="+kn_type);
					var kn_type=localStorage.kn_type;
					if(localStorage.kn_fid && kn_fid!="")
					{
						
						var obj={};
						var folders=data;
						for (var i=0;i<folders.length;i++)
						{
						   if(folders[i].fileid==kn_fid)
						   {
							   obj=folders[i];
                                if ($scope._isUserFolder(kn_type)) {
                                     $("body").data("userfolderid", "4");
                                     $scope.isunit = '0';
                                     $scope._showmoreUser($scope,"0",obj);
                                } else if ($scope._isCorpFolder(kn_type)) {
                                     $("body").data("corpfolderid", "3");
                                     $scope.isunit = '1';
                                     $scope._showmoreCorp($scope,"0",obj);
                                }
							   break;
						   }
						}
						
					}	
					$("#kmlistview").show(); 					
				}				
			}
			hideloading();
		});
	};
});
var Scope;
var isDefault;
var isDetail = false;

angularKnowledgeApp.controller("knowledgeController", function($scope, $http, knowledgeService) {
	Scope = $scope;//全局变量
	isDefault = '0';
	$scope.feurl = localStorage.feurl;
	$scope._isUserFolder = function(type){
		if(type && type == "getParentId"){
			return true;
		}else{
			return false;
		}
	}
	$scope._isCorpFolder = function(type){
		if(type && type == "getUnitParentId"){
			return true;
		}else{
			return false;
		}
	}
	
	$scope._showUserKM = function() {
		var kmid = getParameter("id");
		if(kmid && kmid != ""){//消息中心查看详情
			$scope.isunit = '2';//代表是消息中心进来的
			var re = /^\d+$/gi;
			if(re.test(kmid))
			{
				var ss =location.href.split("#");
            	if(ss[1]!="pageknowledgeshow")
            	{
            	    if(window.localStorage)
            	    {
            	        localStorage.kn_type="getUnitParentId";
            	    }
                    $.mobile.changePage('#pageCorpknowledge', {});
                    $("body").data("corpfolderid", "3");
                   // $scope.isunit = '1';
                    $scope.folderpath = "../../css/images/kmicon/folder.png";
                    var jsonParam = {
                        obj : 'knowledgeService',
                        method : 'getPublishListForMsg',
                        param1 : kmid,
                        count : 1
                    };
                    $.angularAJAX($http, jsonParam, function(data) {
                        $scope.corpFolders = data;
                        //navigator.pluginClose.log("切换到Corp");
                        isDefault = '1';
                    });
                    //showCorpFile($scope);
                    console.info("test1");
            	}
			}
			else
			{
                $scope.detailTitlebar = "文件详情";
                isDetail = true;
                var jsonParam = {
                    obj : 'knowledgeService',
                    method : 'getFileViewById',
                    count : 1,
                    param1 : kmid
                };
                $.mobile.changePage('#pageknowledgeshow', {});
                $.angularAJAX($http, jsonParam, function(data){
                    if(data){
                        //$scope.delPermission = true;
                        //$scope.publishPermission = true;
                        $scope._showmoreUser(data.fileid,'0',data);
                        //$scope._hasdel();
                    }else{
                        navigator.notification.alert("文件加载失败或已经被删除");
    //				    $.mobile.changePage('#pagenotknowledgeshow', {});
                    }
                    hideloading();
                });
			}
		}else{
			$("body").data("userfolderid", "4");
			$scope.isunit = '0';
			$scope.folderpath = "../../css/images/kmicon/folder.png";
			var jsonParam = {
				obj : 'knowledgeService',
				method : 'getPersonalFolderList',
				count : 0
			};
			$.angularAJAX($http, jsonParam, function(data) {
				$scope.userFolders = data;
				//navigator.pluginClose.log("切换到User");
				if(isDefault != '0'){//第一次
					document.removeEventListener('backbutton',goBackButton,false);
					document.addEventListener('backbutton',changeUserOrCorp,false);
				}else{
					isDefault = '1';
					document.removeEventListener('backbutton',goBackButton,false);
					document.addEventListener('backbutton',changeUserOrCorp,false);
				}
			});
		}
        var kmid = getParameter("id");
        var isPublish=false;
        if(kmid && kmid != ""){//消息中心查看详情
            var re = /^\d+$/gi;
            if(re.test(kmid))
            {
                isPublish=true;
            }
        }
        if(isPublish==false)
        {
             $scope.corpTitlebar = "单位文件";
        }
        else
        {
             $scope.corpTitlebar = "被分享的文件";
        }
		$scope.userTitlebar = "个人文件";
	};
		initapp($scope._showUserKM);// 默认加载个人文件夹
	$scope._showCorpKM = function() {
		$("body").data("corpfolderid", "3");
		$scope.isunit = '1';
		$scope.folderpath = "../../css/images/kmicon/folder.png";
		var jsonParam = {
			obj : 'knowledgeService',
			method : 'getUnitFolderList',
			count : 0
		};
		$.angularAJAX($http, jsonParam, function(data) {
			$scope.corpFolders = data;
			//navigator.pluginClose.log("切换到Corp");
			isDefault = '1';
			document.removeEventListener('backbutton',goBackButton,false);
			document.addEventListener('backbutton',changeUserOrCorp,false);
		});
	};
	$scope._kmswiperight = function() {
		knowledgeService._kmswiperight();
	}

	$scope._gobackUser = function() {
		knowledgeService._goback($scope, 'getParentId');
	};

	$scope._showFolderName = function(pid, methed) {
		knowledgeService._showFolderName($scope,pid, methed);
	};

	$scope._showFolderAndFileUser = function(id) {
		knowledgeService._showFolderAndFile($scope, id, "getFolderAndFileByFolderId","getParentId");
	};

	$scope._showmoreUser = function(id, isFolder, title, type, imgsrc, pubname, pubtime) {
		knowledgeService._showmore($scope, id, isFolder, title, "getParentId");
	};
	$scope._showmoreCorp = function(id, isFolder, title, type, imgsrc, pubname, pubtime) {
		knowledgeService._showmore($scope, id, isFolder, title, "getUnitParentId");
	};

	$scope._dodownload = function(id) {
		knowledgeService._dodownload($scope, id);
	};
	$scope._sharefile = function(fileid) {
		knowledgeService._sharefile($scope, fileid);
	};
	$scope._callbackok = function() {
		knowledgeService._callbackok($scope);
	};
	$scope._deletefile = function(fileid) {
		knowledgeService._deletefile($scope, fileid);
	};
	$scope.isshowPublish = function() {
		var kmid = getParameter("id");
		if(kmid && kmid != ""){
			var re = /^\d+$/gi;
			if(re.test(kmid))
			{
                this._gobackCorp = function() {
                    navigator.app.exitApp();
                }
				return false;
			}
		}
		return true;
	};
	$scope.isshowCorp = function() {
		var flag = $("body").data("corpfolderid");
        if(!this.isshowPublish())
        {
            return true;
        }
		return flag != "3";
	};



	$scope.isshowUser = function() {
		var flag = $("body").data("userfolderid");
		return flag != "4";
	};
	$scope._gobackCorp = function() {
		knowledgeService._goback($scope, 'getUnitParentId');
	};

	$scope._showFolderAndFileCorp = function(id) {
		knowledgeService._showFolderAndFile($scope, id, "getUnitFolderAndFileByFolderId","getUnitParentId");
	};
	$scope._showFolderAndFilePublish = function(id) {
		knowledgeService._showFolderAndFile($scope, id, "getPublishListForMsg","getUnitParentId");
	};
	$scope._hasdel = function() {
		if ($scope.delPermission && $scope.delPermission == true) {
			return true;
		} else {
			return false;
		}
	};
	$scope._haspublish = function() {
		if ($scope.publishPermission && $scope.publishPermission == true) {
			return true;
		} else {
			return false;
		}
	};
	$scope._hasdelPermission = function(fileid) {
		$scope.delPermission = false;
		$scope.publishPermission = false;
		if($scope.isunit == 0){
			$scope.delPermission = true;
			$scope.publishPermission = true;
			return;
		}
		var jsonParam = {
			obj : 'knowledgeService',
			method : 'hasPermission',
			count : 1,
			param1 : fileid
		};
		$.angularAJAX($http, jsonParam, function(data) {
			$scope.delPermission = data.del;
			$scope.publishPermission = data.publish;
		});
	};
	$scope._replaceSrc = function(src){
		return src ? src.replace("/wechat","../.."): $scope.folderpath;
	
	}
	$scope.isDetail = function(){
		return !isDetail;
	}
	showCorpFile($scope);
	var ss =location.href.split("#");
	var kmid = getParameter("id");
	if(ss[1]=="pageCorpknowledge" && !(kmid && kmid != ""))
    {
    	$scope._showCorpKM();
    }
});

//注册返回键,开始是结束的事件
document.addEventListener("backbutton", closeButton, false);

//往前返回的事件
function goBackButton(){
	if(Scope.isunit == '0'){//个人
		Scope._gobackUser();
	}else if(Scope.isunit == '1'){
		Scope._gobackCorp();
	}
};
//close的事件
function closeButton(){
	navigator.pluginClose.close();
};
//详情的事件
function detailBack(){
	if(Scope.isunit == '2'){
		navigator.pluginClose.close();
		return;
	}
	document.addEventListener('backbutton',goBackButton,false);//注册目录返回键
	document.removeEventListener('backbutton',detailBack,false);
	if(Scope._pagename == "#pageUserknowledge"){//个人文件
		$.mobile.changePage('#pageUserknowledge', {transition: "slide",reverse:"true"});
	}else{
		$.mobile.changePage('#pageCorpknowledge', {transition: "slide",reverse:"true"});
	}
};
//切换个人与单位事件
function changeUserOrCorp(){
	document.addEventListener('backbutton',goBackButton,false);
	document.removeEventListener('backbutton',changeUserOrCorp,false);
	if(Scope._pagename == "#pageUserknowledge"){//个人文件
		Scope.isunit = '0';
		//navigator.pluginClose.log("返回到User");
		$.mobile.changePage('#pageUserknowledge', {transition: "slide",reverse:"true"});
	}else{
		Scope.isunit = '1';
		//navigator.pluginClose.log("返回到Corp");
		$.mobile.changePage('#pageCorpknowledge', {transition: "slide",reverse:"true"});
	}
};
//分享返回键
function shareBack(){
	//navigator.pluginClose.log("分享回退");
	window.history.go(-1);//返回到上一个页面，相当于直接按了返回键
	document.removeEventListener("backbutton",shareBack,false);
	document.addEventListener("backbutton",detailBack,false);
}

function showCorpFile($scope){
	var ss =location.href.split("#");
	console.log("showCorpFile");
	if(ss[1]=="pageknowledgeshow")
	{
	    var isPublish=false;
        var kmid = getParameter("id");
        if(kmid && kmid != ""){//消息中心查看详情
            $scope.isunit = '2';//代表是消息中心进来的
            var re = /^\d+$/gi;
            if(re.test(kmid))
            {
                isPublish=true;
                $scope._pagename = "#pageCorpknowledge";
                if(window.localStorage)
                {
                    localStorage.kn_type="getUnitParentId";
                }
                $scope._showFolderAndFilePublish(kmid);
            }
         }
        if(!isPublish)
        {
            if(window.localStorage)
            {
                var kn_folderId="",kn_type="",kn_foldername="";
                kn_folderId=localStorage.kn_folderId;
                kn_type=localStorage.kn_type;
                kn_foldername=localStorage.kn_foldername;
                if(localStorage.kn_folderId && kn_folderId!="")
                {
                    if ($scope._isUserFolder(kn_type)) {
                        $scope._pagename = "#pageUserknowledge";
                    } else if ($scope._isCorpFolder(kn_type)) {
                        $scope._pagename = "#pageCorpknowledge";
                    }
                    if ($scope._isUserFolder(kn_type)) {
                        $scope.userTitlebar =kn_foldername;
                        $scope._showFolderAndFileUser(kn_folderId);
                    } else if ($scope._isCorpFolder(kn_type)) {
                        $scope.corpTitlebar =kn_foldername;
                        $scope._showFolderAndFileCorp(kn_folderId);
                    }
                }
            }
        }
	}
}