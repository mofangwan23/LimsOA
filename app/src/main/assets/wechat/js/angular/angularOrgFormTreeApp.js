'use strict';
var orgFormTreeApp = angular.module("angularOrgFormTreeApp", []);
orgFormTreeApp.directive('orgFormTree', function () {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: '/wechat/html/common/common-org-form-tree.html',
        controller: function ($scope, $http, $timeout) {
            $scope.GROUP_IMAGE = '/cooperate/flow/N_GROUP.png';
            $scope.POST_IMAGE = '/cooperate/flow/N_ROLE.png';
            $scope.USER_IMAGE = '/UserUploadFile/photo/photo.png';
            $scope.ORGANIZE_FILTER_TYPE = 1;	//机构显示类型（对应表单工具相同字段）
            $scope.ORGANIZE_TYPE = 'X';		    //当前机构类型
            $scope.ORGANIZE_SHOW_TYPE = 'X';	//显示机构类型
            $scope.ORGANIZE_CHECK_KEYS = '';    //选中的值
            $scope.ORGANIZE_CHECK_VALUES = '';  //选中名称
            var CALLBACK;
            var $organizeFormChecked = $("#organizeFormChecked"); //选中机构

            $('#pageFormOrganizeSelector').on('pagebeforeshow', function(){     //头部机构选择按钮
                $('#organizeFormHeaderListview li').removeClass('li-show').hide();
                var len = $scope.ORGANIZE_SHOW_TYPE.length;
                if ($scope.ORGANIZE_IS_MULTI) {
                    if ($scope.ORGANIZE_SHOW_TYPE.length == 1) {
                        $('#organize' + $scope.ORGANIZE_SHOW_TYPE).addClass('li-show').show();
                    } else {
                        var typeArr = $scope.ORGANIZE_SHOW_TYPE.split('');
                        len = typeArr.length;
                        for (var i = 0; i < len; i ++) {
                            $('#organize' + typeArr[i]).addClass('li-show').show();
                        }
                    }
                } else {
                    $('#organize' + $scope.ORGANIZE_SHOW_TYPE).addClass('li-show');
                }

                var $header = $(this).find('[data-role=header]');
                $header.find('.ui-btn-active').removeClass('ui-btn-active');
                $header.find('.pubut-left').removeClass('pubut-left');
                $header.find('.pubut-mid').removeClass('pubut-mid');
                $header.find('.pubut-right').removeClass('pubut-right');
                $header.find('.pubut-single').removeClass('pubut-single');
                if (len == 1) {
                    var $li = $header.find('ul li.li-show');
                    $li.width('99%').show();
                    $li.find('a').addClass('pubut-single').addClass('ui-btn-active');
                } else if (len == 2) {
                    var $lis = $header.find('ul li.li-show');
                    var $as = $header.find('ul li.li-show a');
                    $lis.eq(0).width('50%');
                    $lis.eq(1).width('50%');
                    $as.eq(0).addClass('pubut-left').addClass('ui-btn-active');
                    $as.eq(1).addClass('pubut-right');
                    $lis.show();
                } else if (len == 3) {
                    var $lis = $header.find('ul li.li-show');
                    var $as = $header.find('ul li.li-show a');
                    $lis.eq(0).width('33.33%');
                    $lis.eq(1).width('33.33%');
                    $lis.eq(2).width('33.33%');
                    $as.eq(0).addClass('pubut-left').addClass('ui-btn-active');
                    $as.eq(1).addClass('pubut-mid');
                    $as.eq(2).addClass('pubut-right');
                    $lis.show();
                }
            });

            $scope._showFormOrganizeTree = function (callback) {
                $.mobile.changePage('#pageFormOrganizeSelector');
                CALLBACK = callback;
                $scope._checkedDefaultFormOrganize();
                if ($scope.ORGANIZE_IS_MULTI) {
                    if ($scope.ORGANIZE_SHOW_TYPE.length == 1) {
                        $scope._showOrgTreeByType($scope.ORGANIZE_SHOW_TYPE);
                    } else {
                        var typeArr = $scope.ORGANIZE_SHOW_TYPE.split('');
                        $scope._showOrgTreeByType(typeArr[0]);
                    }
                } else {
                    $scope._showOrgTreeByType($scope.ORGANIZE_SHOW_TYPE);
                }
            };
            /**在已选机构中显示选中的值*/
            $scope._checkedDefaultFormOrganize = function() {
                $scope._removeAllFormOrgChecked();
                if ($scope.ORGANIZE_CHECK_KEYS) {
                    var keys = $scope.ORGANIZE_CHECK_KEYS.split(',');
                    var values = $scope.ORGANIZE_CHECK_VALUES.split(',');
                    var src, id;
                    for (var i = 0, len = keys.length; i < len; i++) {
                        id = keys[i];
                        if (!$scope.ORGANIZE_IS_MULTI && $scope.ORGANIZE_SHOW_TYPE.length == 1) {
                            if ( 'Y' == $scope.ORGANIZE_SHOW_TYPE ) {
                                src = $scope.POST_IMAGE;
                                id = '{' + id + '}';
                            } else if ( 'Z' == $scope.ORGANIZE_SHOW_TYPE ) {
                                src = $scope.GROUP_IMAGE;
                                id = '[' + id + ']';
                            } else {
                                src = $('#pageFormOrganizeSelector #photo' + id).attr('src');
                            }
                        } else {
                            if (id.charAt(0) == '[') {
                                src = src = $scope.GROUP_IMAGE;
                            } else if (id.charAt(0) == '{') {
                                src = $scope.POST_IMAGE;
                            } else {
                                src = $('#pageFormOrganizeSelector #photo' + id).attr('src');
                            }
                        }
                        $organizeFormChecked.append('<img class="round_photo_small" src="'+ src +'" id="img'+ id +'" orgid="'+ id +'" orgname="'+ values[i] +'" />');
                    }
                }
            };
            /**勾选已选中值的的选中状态*/
            $scope._checkedFormOrganize = function() {
                if ($scope.ORGANIZE_CHECK_KEYS) {
                    var keys = $scope.ORGANIZE_CHECK_KEYS.split(',');
                    var id;
                    for (var i = 0, len = keys.length; i < len; i++) {
                        id = keys[i];
                        if (!$scope.ORGANIZE_IS_MULTI && $scope.ORGANIZE_SHOW_TYPE.length == 1) {
                            if ( 'Y' == $scope.ORGANIZE_SHOW_TYPE ) {
                                id = '{' + id + '}';
                            } else if ( 'Z' == $scope.ORGANIZE_SHOW_TYPE ) {
                                id = '[' + id + ']';
                            }
                        }
                        $('#pageFormOrganizeSelector #chk' + getJqueryLegalId(id)).addClass("ui-btn-icon-right ui-icon-check");
                    }
                }
            };

            $scope._showOrgTreeByType = function (type) {
                $timeout(function(){showloading();}, 0);
                $scope.ORGANIZE_TYPE = type;
                if ('Z' == type) {
                    $scope._showFormGroupTree();
                } else if ('Y' == type) {
                    $scope._showFormPostTree();
                } else {
                    $scope._showFromPersonTree();
                }
            };

            $scope._showFromPersonTree = function () {
                $timeout(function(){showloading();}, 0);
                $scope.ORGANIZE_TYPE = 'X';
                var trees = $("body").data("fromOrgTrees" + $scope.ORGANIZE_CHECK_CONTROL_ID);
                if (trees) {
                    $scope.organizeFormPersonTreeData = trees;
                    $scope._checkedFormOrganize();
                    $timeout(function(){hideloading();}, 0);
                } else {
                    var jsonParam = {
                        obj: 'orgTreeService',
                        method: 'getPersonTree',
                        count: 1,
                        param1: $scope.ORGANIZE_FILTER_TYPE
                    };
                    $.angularAJAX($http, jsonParam, function (data) {
                        $("body").data("fromOrgTrees" + $scope.ORGANIZE_CHECK_CONTROL_ID, data);
                        $scope.organizeFormPersonTreeData = data;
                        $scope._checkedFormOrganize();
                        $timeout(function(){hideloading();}, 0);
                    });
                }
            };

            $scope._showFormGroupTree = function () {
                $timeout(function(){showloading();}, 0);
                $scope.ORGANIZE_TYPE = 'Z';
                var trees = $("body").data("fromGroupOrgTrees" + $scope.ORGANIZE_CHECK_CONTROL_ID);
                if (trees) {
                    $scope.organizeFormGroupTreeData = trees;
                    $scope._checkedFormOrganize();
                    $timeout(function(){hideloading();}, 0);
                } else {
                    var jsonParam = {
                        obj: 'orgTreeService',
                        method: 'getGroupTree',
                        count: 1,
                        param1: $scope.ORGANIZE_FILTER_TYPE
                    };
                    $.angularAJAX($http, jsonParam, function (data) {
                        $("body").data("fromGroupOrgTrees" + $scope.ORGANIZE_CHECK_CONTROL_ID, data);
                        $scope.organizeFormGroupTreeData = data;
                        $scope._checkedFormOrganize();
                        $timeout(function(){hideloading();}, 0);
                    });
                }
            };

            $scope._showFormPostTree = function () {
                $timeout(function(){showloading();}, 0);
                $scope.ORGANIZE_TYPE = 'Y';
                var trees = $("body").data("fromPostOrgTrees" + $scope.ORGANIZE_CHECK_CONTROL_ID);
                if (trees) {
                    $scope.organizeFormPostTreeData = trees;
                    $scope._checkedFormOrganize();
                    $timeout(function(){hideloading();}, 0);
                } else {
                    var jsonParam = {
                        obj: 'orgTreeService',
                        method: 'getPostTree',
                        count: 1,
                        param1: $scope.ORGANIZE_FILTER_TYPE
                    };
                    $.angularAJAX($http, jsonParam, function (data) {
                        $("body").data("fromPostOrgTrees" + $scope.ORGANIZE_CHECK_CONTROL_ID, data);
                        $scope.organizeFormPostTreeData = data;
                        $scope._checkedFormOrganize();
                        $timeout(function(){hideloading();}, 0);
                    });
                }
            };

            $scope._checkedFormOrgBox = function (id) {
                if ($scope.ORGANIZE_IS_MULTI) {
                    if (id) {
                        var $this = $('#pageFormOrganizeSelector #chk' + getJqueryLegalId(id));
                        if ($this.hasClass("ui-icon-check")) {
                            $this.removeClass("ui-btn-icon-right").removeClass("ui-icon-check");
                            $organizeFormChecked.find("img[id=img" + getJqueryLegalId(id) + "]").remove();
                        } else {
                            $scope._checkedFormOrgItem(id);
                        }
                    }
                } else { // 单选
                    $scope._removeAllFormOrgChecked();
                    $scope._checkedFormOrgItem(id);
                }
            };
            $scope._removeAllFormOrgChecked = function () {
                $("#pageFormOrganizeSelector li a").removeClass("ui-btn-icon-right").removeClass("ui-icon-check");
                $organizeFormChecked.empty();
            };
            $scope._checkedFormOrgItem = function (id) {
                if (!id || id == "") {
                    return;
                }
                var $this = $('#pageFormOrganizeSelector #chk' + getJqueryLegalId(id));
                var $photo = $("#pageFormOrganizeSelector #photo" + getJqueryLegalId(id));
                $this.addClass("ui-btn-icon-right ui-icon-check");
                var photosrc = $photo.prop("src");
                var size = $organizeFormChecked.find("img[id=img" + getJqueryLegalId(id) + "]").size();
                if (size < 1) {
                    if (photosrc && photosrc != '') {
                        var $img = $("<img>").addClass("round_photo_small").prop("src", photosrc).prop("id", "img" + id).attr('orgid', id).attr('orgname', $this.attr('orgname'));
                        $organizeFormChecked.append($img);
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
                                $organizeFormChecked.append($img);
                                $this.addClass("ui-btn-icon-right ui-icon-check");
                            }
                        });
                    }
                }
            };
            //确定按钮
            $scope._finishFormOrganizeSelect = function () {
                var checks = $organizeFormChecked.find('img');
                var resultshide = new Array();
                var resultsshow = new Array();
                $(checks).each(function (i) {
                    resultshide[i] = $(this).attr("orgid");
                    resultsshow[i] = $(this).attr("orgname");
                });
                var ids = resultshide.join(",");
                if (!$scope.ORGANIZE_IS_MULTI && $scope.ORGANIZE_SHOW_TYPE  != 'X') {
                    ids = ids.replace(/\{/, '').replace(/\}/, '').replace(/\[/, '').replace(/\]/, '');
                }
                if (CALLBACK && typeof(CALLBACK) === "function") {
                    CALLBACK(ids, resultsshow.join(","));
                }
                $scope.ORGANIZE_TYPE = '';
                $scope.ORGANIZE_SHOW_TYPE = '';
                $scope.ORGANIZE_FILTER_TYPE = '';
                $scope.ORGANIZE_CHECK_KEYS = '';
                $scope.ORGANIZE_CHECK_VALUES = '';
                $('#pageFormOrganizeSelector input[data-type=search]').val('').change();
            };

            $scope._isShowUser = function (id) {
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
        }
    };
});
orgFormTreeApp.directive('orgFormTreeDirective', function () {
    return function (scope, element, attrs) {
        if (scope.$last) {
            var flag = true;
            scope.$watch(function () {
                if (flag) {
                    element.parent().listview().listview('refresh');
                    element.parent().trigger('create');
                    scope._checkedFormOrganize();
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
