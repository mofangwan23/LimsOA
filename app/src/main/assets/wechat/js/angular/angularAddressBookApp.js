var angularAddressBookApp = angular.module("angularAddressBookApp", [ 'ngTouch', 'angularEasemobChat' ]);

angularAddressBookApp.service('addressbookService', function($http) {
	this._removefriend = function($scope, id, flag) {
		showloading();
		var param = {
			obj : 'addressBookService',
			method : 'removeCommonUsers',
			count : 1,
			param1 : id
		};
		$.angularAJAX($http, param, function(data) {
			if (data && data == 'SUCCESS') {
				if (flag == "1") {
					// 如果是常用人的列表
					var temp = $scope.addressbooks;
					for (var i = 0; i < temp.length; i++) {
						if (temp[i].id == id) {
							temp.splice(i, 1);
							$scope.addressbooks = temp;
							if (i != temp.length - 1) {
								break;
							}
						}
					}
					alert('常用人删除成功');
					$.mobile.changePage('#pagecontact', {});
				} else {
					var temp = $scope.commonusers;
					for (var i = 0; i < temp.length; i++) {
						if (temp[i] == id) {
							temp.splice(i, 1);
							$scope.commonusers = temp;
							$("body").data("commonusers", temp);
							break;
						}
					}
					alert('常用人删除成功');
				}
			} else {
				alert(data);
			}
			hideloading();
		});
	};
	this._getCommonUsers = function($scope) {
		// 把常用人组成数组
		var commonusersdata = $("body").data("commonusers");
		if (commonusersdata) {
			$scope.commonusers = commonusersdata;
		} else {
			var param = {
				obj : 'addressBookService',
				method : 'getCommonFriends',
				count : 0
			};
			$.angularAJAX($http, param, function(data) {
				if (data && data != "") {
					data = data.split(",");
					$scope.commonusers = data;
					$("body").data("commonusers", data);
				}
			});
		}
	};
	// 是否常用人
	this._isCommonUsers = function($scope, id) {
		if (!id) {
			return;
		}
		var users = $scope.commonusers;
		return $.inArray(id, users) > -1;
	};
	this._showaddressbook = function($scope, id) {
		showloading();
		var param = {
			obj : 'addressBookService',
			method : 'getAddressBookById',
			count : 1,
			param1 : id
		};
		$.angularAJAX($http, param, function(data) {
			$scope.addressBook = data;
			hideloading();
		});
	};
	this._showtelchoose = function($scope, type) {
		var addressbook = $scope.addressBook;
		var json = "[";
		if (addressbook) {
			if ($.trim(addressbook.tel) != '') {
				json += '{"tel":"办公电话  ' + addressbook.tel + '","type":"' + type + '","click":"' + addressbook.tel + '"}';
			}
			if ($.trim(addressbook.phone) != '') {
				json += ',{"tel":"移动电话  ' + addressbook.phone + '","type":"' + type + '","click":"' + addressbook.phone + '"}';
			}
			json += "]";
		}
		$scope.tels = eval(json);
	};
	this._closedialog = function() {
		$("#pagechoose").popup("close");
	};
	this.defaultUser = function() {
		return "css/images/user.png";
	};
	this._addfriend = function($scope, id) {
		showloading();
		var param = {
			obj : 'addressBookService',
			method : 'addCommonUser',
			count : 1,
			param1 : id
		};
		$.angularAJAX($http, param, function(data) {
			if (data && data == 'SUCCESS') {
				var users = $scope.commonusers.concat([ id ]);
				$scope.commonusers = users;
				$("body").data("commonusers", users);
				alert('常用人添加成功');
			} else {
				alert(data);
			}
			hideloading();
		});
	};

});

angularAddressBookApp.config(function($compileProvider) {
	$compileProvider.aHrefSanitizationWhitelist(/^\s*(sms|mailto|tel):/);
});
angularAddressBookApp.directive('commonDirective', function() {
	return function(scope, element, attrs) {
		scope.$watch(function(value) {
			try {
				$("#addressbooklistview").listview("refresh");
				$("#chatlistview").listview("refresh");
			} catch (e) {
				console.log("错误信息" + e);
			}
		});
	};
});
angularAddressBookApp.directive('repeatDirective', function() {
	return function(scope, element, attrs) {
		if (scope.$last) {
			scope.$watch(function(value) {
				try {
					$("#addressbooklistview").listview("refresh");
					$("#chatlistview").listview("refresh");
				} catch (e) {
					console.log("错误信息" + e);
				}
			});
		}
	};
});
angularAddressBookApp.directive('chooseDirective', function() {
	return function(scope, element, attrs) {
		if (scope.$last) {
			scope.$watch(function(value) {
				try {
					$("#chooselistview").listview("refresh");
				} catch (e) {
					console.log("错误信息" + e);
				}
			});
		}
	};
});
var addressbookMap = new Map();
angularAddressBookApp.controller("allAddressBookController", [ '$scope', '$http', '$compile', '$timeout', 'addressbookService', function($scope, $http, $compile, $timeout, addressbookService) {
	$scope.defaultUser = addressbookService.defaultUser();
	showloading();
	$scope._init = function() {
		var jsonParam = {
			obj : 'chatService',
			method : 'getChatListNAddressbooks',
			count : 3,
			param1 : "1",
			param2 : 1,
			param3 : 10

		};
		$.angularAJAX($http, jsonParam, function(data) {
			if (data) {
				$scope.chatlist = data.chatList;
				$scope.addressbooks = data.addressbooks;
				for ( var i in data.addressbooks) {
					addressbookMap.put(data.addressbooks[i].IMID, data.addressbooks[i]);
				}
				$('body').data('addressbookMap', addressbookMap);
			}
		});
		addressbookService._getCommonUsers($scope);
	};
	$scope._isCommonUsers = function(id) {
		return addressbookService._isCommonUsers($scope, id);
	};
	$scope._showaddressbook = function(imid) {
		showloading();
		if (!imid) {
			imid = $scope.currChatUserImid;
		}
		$scope.addressBook = addressbookMap.get(imid);
		hideloading();
	};

	$scope._showtelchoose = function(type) {
		addressbookService._showtelchoose($scope, type);
	};
	$scope._closedialog = function() {
		addressbookService._closedialog();
	};

	$scope._removefriend = function(id) {
		addressbookService._removefriend($scope, id);
	};
	$scope._addfriend = function(id) {
		addressbookService._addfriend($scope, id);
	};
	$scope._init();
} ]);

angularAddressBookApp.controller("commonAddressBookController", function($scope, $http, addressbookService) {
	showloading();
	$scope._init = function() {
		var jsonParam = {
			obj : 'addressBookService',
			method : 'getCommonUserAddressBook',
			count : 0
		};
		$.angularAJAX($http, jsonParam, function(data) {
			if (data && data != "") {
				$scope.addressbooks = data;
			} else {
				$scope.addressmsg = "暂无数据";
			}
			hideloading();
		});
	};
	$("#infocontact").on("swiperight", function() {
		$.mobile.changePage('#pagecontact', {
			transition : "slide",
			reverse : "true"
		});
	});
	$scope._isCommonUsers = function(id) {
		return addressbookService._isCommonUsers($scope, id);
	};

	$scope._showaddressbook = function(id) {
		addressbookService._showaddressbook($scope, id);
	};
	$scope._showtelchoose = function(type) {
		addressbookService._showtelchoose($scope, type);
	};
	$scope._closedialog = function() {
		addressbookService._closedialog();
	};
	$scope._removefriend = function(id) {
		addressbookService._removefriend($scope, id, "1");
	};
	$scope._init();
});

angularAddressBookApp.controller("deptAddressBookController", function($scope, $http, addressbookService) {
	$scope.defaultUser = addressbookService.defaultUser();
	showloading();
	$scope._init = function() {
		var jsonParam = {
			obj : 'addressBookService',
			method : 'getDeptAddressBook',
			count : 0
		};
		$.angularAJAX($http, jsonParam, function(data) {
			$scope.addressbooks = data;
			hideloading();
		});
		addressbookService._getCommonUsers($scope);
	};
	$("#infocontact").on("swiperight", function() {
		$.mobile.changePage('#pagecontact', {
			transition : "slide",
			reverse : "true"
		});
	});
	$scope._init();
	$scope._isCommonUsers = function(id) {
		return addressbookService._isCommonUsers($scope, id);
	};
	$scope._showaddressbook = function(id) {
		addressbookService._showaddressbook($scope, id);
	};
	$scope._showtelchoose = function(type) {
		addressbookService._showtelchoose($scope, type);
	};
	$scope._closedialog = function() {
		addressbookService._closedialog();
	};
	$scope._removefriend = function(id) {
		addressbookService._removefriend($scope, id);
	};
	$scope._addfriend = function(id) {
		addressbookService._addfriend($scope, id);
	};
});
