'use strict';
var orgTreeApp = angular.module("angularOrgTreeApp", []);
orgTreeApp.directive('orgTree', function() {
	return {
		restrict : 'EA',
		replace : true,
		templateUrl : '../../html/common/common-orgtree.html',
		controller : function($scope, $http, $timeout) {
			$scope._initOrgTree = function(initchoose) {
				showloading();
				var orgtrees = $("body").data("orgtrees");
				if (orgtrees) {
					$scope.orgtrees = orgtrees;
					$scope.feurl = localStorage.feurl;
					$scope._chooseItem(initchoose);
					$.mobile.changePage('#pagecontact', {
						showLoadMsg : true
					});
					hideloading();
				} else {
					var jsonParam = {
						obj : 'addressBookService',
						method : 'getAllAddressBook',
						count : 0
					};
					$.angularAJAX($http, jsonParam, function(data) {
						$("body").data("orgtrees", data);
						$scope.orgtrees = data;
						$scope._chooseItem(initchoose);
						$scope.feurl = localStorage.feurl;
						$.mobile.changePage('#pagecontact', {});
						hideloading();
					});
				}
			};
			$scope._checkedbox = function(id) {
				var isMulti = $scope.isMulti;
				if (isMulti) {
					if (id) {
						var $this = $("#chk" + id);
						if ($this.hasClass("ui-icon-check")) {
							$this.removeClass("ui-btn-icon-right").removeClass("ui-icon-check");
							$("#users").find("img[id=img" + id + "]").remove();
							var choosedId = $scope.choosedId;
							var arr = choosedId.split(",");
							arr = arr.distinct();
							for ( var int = 0; int < arr.length; int++) {
								if (arr[int] == id) {
									arr.splice(int, 1);
									break;
								}
							}
							$scope.choosedId = arr.join(",");
						} else {
							$scope._checked(id);
						}
					}
				} else {
					// 单选
					$scope._removeAll();
					$scope._checked(id);
				}
			};
			$scope._chooseItem = function(choosedId) {
				$scope._removeAll();
				if (!choosedId) {
					choosedId = $scope.choosedId;
				} else {
					$scope.choosedId = choosedId;
				}
				if (!choosedId || choosedId == "") {
					return;
				}
				var arr = choosedId.split(",");
				if (arr == null || arr == "") {
					return;
				}
				arr = arr.distinct();
				for ( var i = 0; i < arr.length; i++) {
					$scope._checked(arr[i]);
				}
			};
			$scope._removeAll = function() {
				$("#orgtreelistview li a").removeClass("ui-btn-icon-right").removeClass("ui-icon-check");
				$scope.choosedId = "";
				$("#users").empty();
			};
			$scope._checked = function(id) {
				if (!id || id == "") {
					return;
				}
				var $this = $("#chk" + id);
				$this.addClass("ui-btn-icon-right ui-icon-check");
				var photosrc = $("#photo" + id).prop("src");
				var size = $("#users").find("img[id=img" + id + "]").size();
				if (size < 1) {
					if (photosrc && photosrc != '') {
						var $img = $("<img>").addClass("round_photo_small").prop("src", photosrc).prop("id", "img" + id);
						$("#users").append($img);
					} else {
						var jsonParam = {
							obj : 'addressBookService',
							method : 'getUserImageHref',
							count : 1,
							param1 : id
						};
						$.angularAJAX($http, jsonParam, function(data) {
							if (data && data != "") {
								var $img = $("<img>").addClass("round_photo_small").prop("src", data).prop("id", "img" + id);
								$("#users").append($img);
								var $this = $("#chk" + id);
								$this.addClass("ui-btn-icon-right ui-icon-check");
							}
						});
					}
					var temp = $scope.choosedId;
					if (temp && temp != "") {
						$scope.choosedId = temp + "," + id;
					} else {
						$scope.choosedId = id;
					}
				}

			};

			$scope._ok = function() {
				var checks = $("#orgtreelistview li .ui-icon-check");
				var resultshide = new Array();
				var resultsshow = new Array();
				$(checks).each(function(i) {
					resultshide[i] = $(this).attr("userid");
					resultsshow[i] = $(this).attr("username");
				});
				$scope.choosedId = resultshide.join(",");
				$scope.orgtreehide = resultshide.join(",");
				$scope.orgtreeshow = resultsshow.join(",");
				eval($scope._callback);
			};

			$scope._isShowUser = function(id) {
				var showItem = $scope._showItem;
				if (showItem) {
					if (("," + showItem + ",").indexOf("," + id + ",") >= 0) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			};
			if ($scope.autoload) {
				$scope._initOrgTree();// 默认加载全部人。
			}
		}
	};
});
orgTreeApp.directive('orgtreeDirective', function() {
	return function(scope, element, attrs) {
		if (scope.$last) {
			scope.$watch(function(value) {
				if ($("#orgtreelistview").length > 0) {
					try {
						$("#orgtreelistview").listview("refresh");
						$("#orgtreelistview").trigger('create');
					} catch (e) {
					}
				}
			});
		}
	};
});
Array.prototype.distinct = function() {
	var newArr = [], obj = {};
	for ( var i = 0, len = this.length; i < len; i++) {
		if (!obj[this[i]]) {
			newArr.push(this[i]);
			obj[this[i]] = 'new';
		}
	}
	return newArr;
};
