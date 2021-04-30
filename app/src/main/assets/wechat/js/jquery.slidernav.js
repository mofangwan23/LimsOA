
$.fn.sliderNav = function(options) {
	var defaults = { items: ["a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"], debug: false, height: null};
	var opts = $.extend(defaults, options); var o = $.meta ? $.extend({}, opts, $$.data()) : opts; var slider = $(this); $(slider).addClass('slider');
	$(slider).append('<div class="slider-nav"><ul></ul></div>');
	for(var i in o.items) $('.slider-nav ul', slider).append("<li><a alt='#divider"+o.items[i].toUpperCase()+"'>"+o.items[i]+"</a></li>");
	$('.slider-content, .slider-nav', slider).css('height',document.body.clientHeight-33);
	$('.slider-nav a', slider).mouseover(function(event){
		var target = $(this).attr('alt');
		var cOffset = $('.slider-content', slider).offset().top;
		if($('.slider-content '+target, slider).length>0){
			var tOffset = $('.slider-content '+target, slider).offset().top;
			var height = $('.slider-nav', slider).height(); if(o.height) height = o.height;
			var pScroll = (tOffset - cOffset) - height/9;
			//$('.slider-content li', slider).removeClass('selected');
			//$(target).addClass('selected');
			$('.slider-content', slider).stop().animate({scrollTop: '+=' + pScroll + 'px'});
		}
	});
	$('.slider-nav a', slider).scrollstart(function(event){
		event.preventDefault();
		$('.slider-nav a', slider).removeClass('selected');
		$(this).addClass('selected');
		var target = $(this).attr('alt');
		var cOffset = $('.slider-content', slider).offset().top;
		if($('.slider-content '+target, slider).length>0){
			var tOffset = $('.slider-content '+target, slider).offset().top;
			var height = $('.slider-nav', slider).height(); if(o.height) height = o.height;
			var pScroll = (tOffset - cOffset) - height/9;
			$('.slider-content', slider).stop().animate({scrollTop: '+=' + pScroll + 'px'});
		}
	}).touchstart(function(event){
		event.preventDefault();
	}).touchend(function (event){
		event.preventDefault();
	}).touchmove(function (event){
		event.preventDefault();
	});
};