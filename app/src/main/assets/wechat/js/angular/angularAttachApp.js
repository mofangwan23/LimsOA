var angularAttachApp = angular.module("angularAttachApp", []);

angularAttachApp.controller("attachController", function($scope, $http) {
	var guid = '64565984-0276-41F5-A561-4CD62292912C';
	var jsonParam = {
		obj : 'weChatService',
		method : 'getAttachByGUID',
		count : 1,
		param1 : guid
	};
	$.angularAJAX($http, jsonParam, function(data) {
		$("body").data("attach", data);
		$scope.attachList = data;
	});
	
	$scope._closedialog = function (){
		$("#pagechoose").popup("close");
	};
});
