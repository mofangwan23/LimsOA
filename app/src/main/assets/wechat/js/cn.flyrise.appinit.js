//当设备加载完毕后，执行
document.addEventListener("deviceready", onDeviceReady, false);

function onDeviceReady() {
	//解决闪屏
	navigator.appinit.init(callBackSuccess);
}
//成功后返回的数据
function callBackSuccess(feurl){
	localStorage.feurl = feurl;
}
function initapp(callfunction){
	if(typeof(callfunction) != "function"){
		//alert("参数不是方法对象");
		return;
	}
//	localStorage.feurl = "";
	var timer = setInterval(checkurl,100);
	function checkurl(){
		var url = localStorage.feurl;
		if(url && url!="undefined"){
			callfunction();
			clearInterval(timer);
		}
	}
	
}