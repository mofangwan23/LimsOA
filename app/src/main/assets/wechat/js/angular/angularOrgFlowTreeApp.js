'use strict';
/**
 * 表单送办专用机构树，只有人员与岗位可供选择
 */
var orgFlowTreeApp = angular.module("angularOrgFlowTreeApp", []);
orgFlowTreeApp.directive('orgFlowTree', function () {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: '/wechat/html/common/common-org-flow-tree.html',
        controller: function ($scope, $http, $timeout) {
            $scope.groupImgHref = '/cooperate/flow/N_GROUP.png';
            $scope.postImgHref = '/cooperate/flow/N_ROLE.png';
            $scope.ORGANIZE_IS_MULTI = false;
            $scope.FLOW_ORGANIZE_TYPE = 1;          //显示类型 1、人员 3、岗位
            $scope.FLOW_ORGANIZE_TYPE_PERSON = 1;   //人员
            $scope.FLOW_ORGANIZE_TYPE_POST = 3;     //岗位
            $scope.FLOW_NODE_ID = 0;
            $scope.FLOW_FORM_GUID = '';
            $scope.FLOW_ORGANIZE_CHECK_KEYS = '';    //选中的值
            $scope.FLOW_ORGANIZE_CHECK_VALUES = '';  //选中名称
            var $organizeFlowChecked = $("#organizeFlowChecked"); //选中机构
            var CALLBACK;
            $scope._showFlowOrganizeTree = function (callback) {
                if (callback) {
                    CALLBACK = callback;
                }
                $scope._checkedDefaultFlowOrganize();
                $scope._showFlowPersonTree();
                $scope._checkedFlowOrganize();
            };

            $scope._showFlowPersonTree = function() {
                $timeout(function(){showloading();}, 0);
                $scope.FLOW_ORGANIZE_TYPE = $scope.FLOW_ORGANIZE_TYPE_PERSON;
                var organizeDataKey = $scope.FLOW_FORM_GUID + $scope.FLOW_ORGANIZE_TYPE_PERSON + $scope.FLOW_NODE_ID;
                var organizeData = $('body').data(organizeDataKey);
                if (organizeData) {
                    $scope.orgFlowPersonTrees = organizeData;
                    $timeout(function(){hideloading();}, 0);
                } else {
                    $.angularAJAX($http, {
                        obj: 'weChatService',
                        method: 'getCommonRequest',
                        count: 1,
                        param1: "{iq:{namespace:'FormSubnodeRequest',model:'0',query:{requestType:0, type:'"+ $scope.FLOW_ORGANIZE_TYPE_PERSON +"', id:'" + $scope.FLOW_NODE_ID + "',wfInfoID:'" + $scope.FLOW_FORM_GUID + "'}}}"
                    }, function (data) {
                        validateLogin(data);
                        $("body").data(organizeDataKey, data.items);
                        $scope.orgFlowPersonTrees = data.items;
                        $scope._checkedFlowOrganize();
                        $timeout(function(){hideloading();}, 0);
                    });
                }
            };

            $scope._showFlowPostTree = function() {
                $timeout(function(){showloading();}, 0);
                $scope.FLOW_ORGANIZE_TYPE = $scope.FLOW_ORGANIZE_TYPE_POST;
                var organizeDataKey = $scope.FLOW_FORM_GUID + $scope.FLOW_ORGANIZE_TYPE_POST + $scope.FLOW_NODE_ID;
                var organizeData = $('body').data(organizeDataKey);
                if (organizeData) {
                    $scope.orgFlowPostTrees = organizeData;
                    $timeout(function(){hideloading();}, 0);
                } else {
                    $.angularAJAX($http, {
                        obj: 'weChatService',
                        method: 'getCommonRequest',
                        count: 1,
                        param1: "{iq:{namespace:'FormSubnodeRequest',model:'0',query:{requestType:0, type:'"+ $scope.FLOW_ORGANIZE_TYPE_POST +"', id:'" + $scope.FLOW_NODE_ID + "',wfInfoID:'" + $scope.FLOW_FORM_GUID + "'}}}"
                    }, function (data) {
                        validateLogin(data);
                        $("body").data(organizeDataKey, data.items);
                        $scope.orgFlowPostTrees = data.items;
                        $scope._checkedFlowOrganize();
                        $timeout(function(){hideloading();}, 0);
                    });
                }
            };

            /**在已选机构中显示选中的值*/
            $scope._checkedDefaultFlowOrganize = function() {
                $timeout(function(){showloading();}, 0);
                $scope._removeAllFlowOrgChecked();
                if ($scope.FLOW_ORGANIZE_CHECK_KEYS) {
                    var keys = $scope.FLOW_ORGANIZE_CHECK_KEYS.split(',');
                    var values = $scope.FLOW_ORGANIZE_CHECK_VALUES.split(',');
                    var src, id;
                    for (var i = 0, len = keys.length; i < len; i++) {
                        id = keys[i];
                        if ( 'Y' == id.charAt(0) ) {
                            src = $scope.POST_IMAGE;
                        } else {
                            src = $('#pageFlowOrganizeSelector #photo' + id).attr('src');
                        }
                        $organizeFlowChecked.append('<img class="round_photo_small" src="'+ src +'" id="img'+ id +'" orgid="'+ id +'" orgname="'+ values[i] +'" />');
                    }
                }
            };
            /**勾选已选中值的的选中状态*/
            $scope._checkedFlowOrganize = function() {
                if ($scope.FLOW_ORGANIZE_CHECK_KEYS) {
                    var keys = $scope.FLOW_ORGANIZE_CHECK_KEYS.split(',');
                    for (var i = 0, len = keys.length; i < len; i++) {
                        $('#pageFlowOrganizeSelector #chk' + getJqueryLegalId(keys[i])).addClass("ui-btn-icon-right ui-icon-check");
                    }
                }
            };

            $scope._checkedFlowOrgBox = function (id) {
                if ($scope.ORGANIZE_IS_MULTI) {
                    if (id) {
                        var $this = $('#pageFlowOrganizeSelector #chk' + getJqueryLegalId(id));
                        if ($this.hasClass("ui-icon-check")) {
                            $this.removeClass("ui-btn-icon-right").removeClass("ui-icon-check");
                            $organizeFlowChecked.find("img[id=img" + getJqueryLegalId(id) + "]").remove();
                        } else {
                            $scope._checkedFlowOrgItem(id);
                        }
                    }
                } else { // 单选
                    var $this = $('#pageFlowOrganizeSelector #chk' + getJqueryLegalId(id));
                    if ($this.hasClass("ui-icon-check")) {
                        $this.removeClass("ui-btn-icon-right").removeClass("ui-icon-check");
                        $organizeFlowChecked.find("img[id=img" + getJqueryLegalId(id) + "]").remove();
                        $scope.FLOW_ORGANIZE_CHECK_KEYS = '';
                        $scope.FLOW_ORGANIZE_CHECK_VALUES = '';
                    } else {
                        $scope._removeAllFlowOrgChecked();
                        $scope.FLOW_ORGANIZE_CHECK_KEYS = id;
                        $scope.FLOW_ORGANIZE_CHECK_VALUES = $this.attr('orgname');
                        $scope._checkedFlowOrgItem(id);
                    }
                }
            };

            $scope._removeAllFlowOrgChecked = function () {
                $("#pageFlowOrganizeSelector li a.ui-icon-check").removeClass("ui-btn-icon-right").removeClass("ui-icon-check");
                $organizeFlowChecked.empty();
            };
            $scope._checkedFlowOrgItem = function (id) {
                if (!id || id == "") {
                    return;
                }
                var $this = $('#pageFlowOrganizeSelector #chk' + getJqueryLegalId(id));
                var $photo = $("#pageFlowOrganizeSelector #photo" + getJqueryLegalId(id));
                $this.addClass("ui-btn-icon-right ui-icon-check");
                var photosrc = $photo.prop("src");
                var size = $organizeFlowChecked.find("img[id=img" + getJqueryLegalId(id) + "]").size();
                if (size < 1) {
                    if (photosrc && photosrc != '') {
                        var $img = $("<img>").addClass("round_photo_small").prop("src", photosrc).prop("id", "img" + id).attr('orgid', id).attr('orgname', $this.attr('orgname'));
                        $organizeFlowChecked.append($img);
                    } else {
                        var jsonParam = {
                            obj: 'addressBookService',
                            method: 'getUserImageHref',
                            count: 1,
                            param1: id
                        };
                        $.angularAJAX($http, jsonParam, function (data) {
                            if (data && data != "") {
                                var $img = $("<img>").addClass("round_photo_small").prop("src", data).prop("id", "img" + id).attr('orgid', id).attr('orgname', $this.attr('orgname'));
                                $organizeFlowChecked.append($img);
                                $this.addClass("ui-btn-icon-right ui-icon-check");
                            }
                        });
                    }
                }
            };

            $scope._finishFlowOrganizeSelect = function () {
                var checks = $organizeFlowChecked.find('img');
                var resultshide = new Array();
                var resultsshow = new Array();
                $(checks).each(function (i) {
                    resultshide[i] = $(this).attr("orgid");
                    resultsshow[i] = $(this).attr("orgname");
                });
                if (CALLBACK && typeof(CALLBACK) === "function") {
                    CALLBACK(resultshide.join(","), resultsshow.join(","));
                }
                $('#pageFlowOrganizeSelector input[data-type=search]').val('').change();
            };
        }
    };
});
orgFlowTreeApp.directive('orgFlowTreeDirective', function () {
    return function (scope, element, attrs) {
        if (scope.$last) {
            var flag = true;
            scope.$watch(function () {
                if (flag) {
                    element.parent().listview().listview('refresh');
                    scope._checkedFlowOrganize();
                    flag = false;
                }
            });
        }
    };
});
Array.prototype.distinct = function () {
    var newArr = [], obj = {};
    for (var i = 0, len = this.length; i < len; i++) {
        if (!obj[this[i]]) {
            newArr.push(this[i]);
            obj[this[i]] = 'new';
        }
    }
    return newArr;
};
