(function($) {
	$.angularAJAX = function($http, paramJson, sucss_callback, err_callback, datas) {
		$http({
			url : localStorage.feurl+"/remoteServlet",
			method : "POST",
			params : eval(paramJson),
			cache : true,
			data : datas,
			headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		}).success(sucss_callback).error(err_callback);
	};
})(jQuery);