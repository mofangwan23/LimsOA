function createMask(){
    $("body").append("<div id=\"mask\" class=\"\" ></div>")
    var height = $("body").css("height");
    var width = $("body").css("width");
    $("#mask").css("height",height);
    $("#mask").css("width",width);
    $("#mask").css("z-index",1998);
    $("#mask").css("position","fixed");
    $("#mask").css("display","none");
}
createMask();
$("body").on("click","#mask,#slidemenu ul li a,#selcetM",function(event){
    var id = event.target.id;
    var type = event.type;
    switch(id){
        case "mask":
                $(this).removeClass("showDiv");
                $("#slidemenu").fadeOut(150);
	            event.preventDefault();
                return;
                
        case "selcetM":
                $("#slidemenu").fadeToggle(150);
                $("#mask").toggleClass("showDiv");
                return;
                
        case "slidemenu":
                $("#slidemenu").fadeOut(150);
                $("#mask").removeClass("showDiv");
                return;
                
    }
                $("#slidemenu").fadeOut(150);
                $("#mask").removeClass("showDiv");

});
