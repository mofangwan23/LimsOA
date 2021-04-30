var angularAppCenterApp = angular.module("angularAppCenterApp", ['ngTouch' ]);
angularAppCenterApp.controller("appCenterController", function($scope, $http) {
	$scope._appInit = function() {
		$scope.apps=[{"appid":"1","appname":"通讯录","url":"","ico":"111.png"},
		             {"appid":"2","appname":"会议管理","url":"","ico":"111.png"},
		             {"appid":"3","appname":"报表中心","url":"","ico":"111.png"},
		             {"appid":"4","appname":"工作计划","url":"","ico":"111.png"},
		             {"appid":"5","appname":"位置上报","url":"","ico":"111.png"},
		             {"appid":"6","appname":"知识管理","url":"","ico":"111.png"},
		             {"appid":"7","appname":"问卷调查","url":"","ico":"111.png"}
		             ];
	};
	$scope._appInit();
});
