cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "../plugins/cn.flyrise.feep.cordova.plugin/www/appinit.js",
        "id": "cn.flyrise.feep.cordova.plugin.AppInit",
        "merges": [
            "navigator.appinit"
        ]
    },
	{
        "file": "../plugins/org.apache.cordova.splashscreen/www/splashscreen.js",
        "id": "org.apache.cordova.splashscreen.SplashScreen",
        "merges": [
            "navigator.splashscreen"
        ]
    },
    {
        "file": "../plugins/cn.flyrise.feep.cordova.plugin/www/fileDownload.js",
        "id": "cn.flyrise.feep.cordova.plugin.PluginDownLoad",
        "merges": [
            "navigator.fileDownload"
        ]
    },
	{
        "file": "../plugins/org.apache.cordova.dialogs/www/notification.js",
        "id": "org.apache.cordova.dialogs.notification",
        "merges": [
            "navigator.notification"
        ]
    },
    {
        "file": "../plugins/org.apache.cordova.dialogs/www/android/notification.js",
        "id": "org.apache.cordova.dialogs.notification_android",
        "merges": [
            "navigator.notification"
        ]
    },   
    {
        "file": "../plugins/cn.flyrise.feep.cordova.plugin/www/close.js",
        "id": "cn.flyrise.feep.cordova.plugin.PluginCloseActivity",
        "merges": [
            "navigator.pluginClose"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    //"org.apache.cordova.camera": "0.2.7",
    "org.apache.cordova.dialogs": "0.3.0",
    //"org.apache.cordova.vibration": "0.3.7",
    "org.apache.cordova.splashscreen" :"0.3.5",
    "cn.flyrise.feep.cordova.plugin.AppInit" :"0.0.1",
}
// BOTTOM OF METADATA
});