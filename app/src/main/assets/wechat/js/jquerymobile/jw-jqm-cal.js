(function($) {
   $.jqmCalendar = function(element, options) {
      var defaults = {
         events : [],
         eventHandler : {
        	 getImportanceOfDay : getImportanceOfDay,
        	 getEventsOnDay : getEventsOnDay
         },
         begin : "begin",
         end : "end",
         summary : "summary",
         icon: "icon",
         url: "url",
         allDayTimeString: '',
         theme : "c",
         date : new Date(),
         version: "1.0.1",
         months : ["01", "02", "03", "04", "05", "06", "07","08", "09", "10", "11", "12"],
         days : ["日", "一", "二", "三", "四", "五", "六"],
         weeksInMonth : undefined,
         startOfWeek : 0,
         listItemFormatter : listItemFormatter
      };

      var plugin = this;
      var today = new Date();
      plugin.settings = null;

      var $element = $(element).addClass("jq-calendar-wrapper"),
          $table,
          $header,
          $tbody,
          $listview;

      function init() {
         plugin.settings = $.extend({}, defaults, options);
         plugin.settings.theme = $.mobile.getInheritedTheme($element, plugin.settings.theme);
         
         $table = $("<table/>");
         var $thead = $("<thead/>").appendTo($table),
            $tr = $("<tr/>").appendTo($thead),
            $th = $("<th class='ui-bar-" + plugin.settings.theme + " header' colspan='7'/>");
         
         $("<a href='#' data-role='button' data-icon='arrow-l' data-iconpos='notext' class='previous-btn'>Previous</a>").click(function() {
            refresh(new Date(plugin.settings.date.getFullYear(), plugin.settings.date.getMonth() - 1, plugin.settings.date.getDate()));
         }).appendTo($th);
         $header = $("<span/>").appendTo($th);
         
         $("<a href='#' data-role='button' data-icon='arrow-r' data-iconpos='notext' class='next-btn'>Next</a>").click(function() {
            refresh(new Date(plugin.settings.date.getFullYear(), plugin.settings.date.getMonth() + 1, plugin.settings.date.getDate()));
         }).appendTo($th);
         
         $th.appendTo($tr);
         
         $tr = $("<tr/>").appendTo($thead);
         for ( var i = 0, days = [].concat(plugin.settings.days, plugin.settings.days).splice(plugin.settings.startOfWeek, 7); i < 7; i++ ) {
            $tr.append("<th class='ui-bar-" + plugin.settings.theme + "'><span class='darker'>"  + days[i] + "</span></th>");
         }
         
         $tbody = $("<tbody/>").appendTo($table);
         
         $table.appendTo($element);
         $listview = $("<ul data-role='listview'/>").insertAfter($table);
         
         refresh(plugin.settings.date);      
      }
      
      function _firstDayOfMonth(date) {
         return ( new Date(date.getFullYear(), date.getMonth(), 1) ).getDay();
      }
      
      function _daysBefore(date, fim) {
         var firstDayInMonth = ( fim || _firstDayOfMonth(date) ),diff = firstDayInMonth - plugin.settings.startOfWeek;
         return ( diff > 0 ) ? diff : ( 7 + diff );
      }
      
      function _daysInMonth(date) {
    	  return ( new Date ( date.getFullYear(), date.getMonth() + 1, 0 )).getDate();
      }
            
      function _weeksInMonth(date, dim, db) {
         return ( plugin.settings.weeksInMonth ) ? plugin.settings.weeksInMonth : Math.ceil( ( ( dim || _daysInMonth(date) ) + ( db || _daysBefore(date)) ) / 7 );
      }
      
      function getImportanceOfDay(date, callback) {
         var importance = 0;
         for ( var i = 0,
                   event,
                   begin = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0),
                   end = new Date(date.getFullYear(), date.getMonth(), date.getDate() + 1, 0, 0, 0, 0);
               event = plugin.settings.events[i]; i++ ) {
            if ( event[plugin.settings.end] >= begin && event[plugin.settings.begin] < end ) {
               importance++;
               if ( importance > 1 ) break;
            }
         }
         callback(importance);
      }
      
      function getEventsOnDay(begin, end, callback) {
         var ret_list = [];
         for ( var i = 0, event; event = plugin.settings.events[i]; i++ ) {
            if ( event[plugin.settings.end] >= begin && event[plugin.settings.begin] < end ) {
               ret_list[ret_list.length] = event;
            }
         }
         callback(ret_list);
      }

      function addCell($row, date, darker, selected) {
    	  var mm = (date.getMonth()+1)>=10?(date.getMonth()+1):"0"+(date.getMonth()+1);
    	  var dd = (date.getDate())>=10?(date.getDate()):"0"+(date.getDate());
    	  var id = "date-"+date.getFullYear()+"-"+mm+"-"+dd;
    	  var $td = $("<td class='ui-body-" + plugin.settings.theme + "'/>").appendTo($row),
             $a = $("<a href='#' class=' ui-btn ui-btn-up-" + plugin.settings.theme + "'/>").attr("id",id)
                  .html(date.getDate().toString()+"")
                  .data('date', date)
                  .click(cellClickHandler)
                  .appendTo($td);

         if ( selected ) $a.click();
         
         if ( darker ) {
             $td.addClass("darker");
         }

         plugin.settings.eventHandler.getImportanceOfDay(date,
            function(importance) {
              if ( importance > 0 ) {
                  $a.append("<span>&bull;</span>");
              }

              if ( date.getFullYear() === today.getFullYear() &&
                   date.getMonth() === today.getMonth() &&
                   date.getDate() === today.getDate() ) {
                  $a.addClass("ui-btn-today");
              } else {
                  $a.addClass("importance-" + importance.toString());
              }
         });
      }
      
      function cellClickHandler() {
         var $this = $(this),
            date = $this.data('date');
         //console.log("date>>"+date);
         $tbody.find("a.ui-btn-active").removeClass("ui-btn-active");
         $this.addClass("ui-btn-active");
         
         if ( date.getMonth() !== plugin.settings.date.getMonth() ) {
            refresh(date);
         } else {
            $element.trigger('change', date);
         }
      }
      
      function refresh(date) {
         plugin.settings.date = date = date ||  plugin.settings.date || new Date();
         var year = date.getFullYear(),
            month = date.getMonth(),
            daysBefore = _daysBefore(date),
            daysInMonth = _daysInMonth(date),
            weeksInMonth = plugin.settings.weeksInMonth || _weeksInMonth(date, daysInMonth, daysBefore);

         if (((daysInMonth + daysBefore) / 7 ) - weeksInMonth === 0)
             weeksInMonth++;
         
         $tbody.empty();
         $header.html( year.toString() + "年" +  plugin.settings.months[month]+"月");
         for (    var   weekIndex = 0,
                  daysInMonthCount = 1,
                  daysAfterCount = 1; weekIndex < weeksInMonth; weekIndex++ ) {
                     
            var daysInWeekCount = 0,
               row = $("<tr/>").appendTo($tbody);
            
            while ( daysBefore > 0 ) {
               addCell(row, new Date(year, month, 1 - daysBefore), true);
               daysBefore--;
               daysInWeekCount++;
            }
            while ( daysInWeekCount < 7 && daysInMonthCount <= daysInMonth ) {
               addCell(row, new Date(year, month, daysInMonthCount), false, daysInMonthCount === date.getDate() );
               daysInWeekCount++;
               daysInMonthCount++;
            }
            
            while ( daysInMonthCount > daysInMonth && daysInWeekCount < 7 ) {
            	//console.log("daysBefore>>>》》》》》>>"+new Date(year, month, daysInMonth + daysAfterCount).getDate());
               addCell(row, new Date(year, month, daysInMonth + daysAfterCount), true);
               daysInWeekCount++;
               daysAfterCount++;
            }
         }
         
         $element.trigger('create');
      }

      $element.bind('change', function(originalEvent, begin) {
         var end = new Date(begin.getFullYear(), begin.getMonth(), begin.getDate() + 1, 0,0,0,0);
         $listview.empty();
         //console.log("change>>"+end);
         plugin.settings.eventHandler.getEventsOnDay(begin, end, function(list_of_events) {
        	 
            for(var i = 0, event; event = list_of_events[i]; i++ ) {
               var summary    = event[plugin.settings.summary],
                   beginTime  = (( event[plugin.settings.begin] > begin ) ? event[plugin.settings.begin] : begin ).toTimeString().substr(0,5),
                   endTime    = (( event[plugin.settings.end] < end ) ? event[plugin.settings.end] : end ).toTimeString().substr(0,5),
                   timeString = beginTime + "-" + endTime,
                   $listItem  = $("<li></li>").appendTo($listview);
               
                   
               plugin.settings.listItemFormatter( $listItem, timeString, summary, event );
            }
            $listview.trigger('create').filter(".ui-listview").listview('refresh');
         });
      });
      
      function listItemFormatter($listItem, timeString, summary, event) {
         var text = ( ( timeString != "00:00-00:00" ) ? timeString : plugin.settings.allDayTimeString ) + " " + summary;
         if (event[plugin.settings.icon]) {
            $listItem.attr('data-icon', event.icon);
         }
         if (event[plugin.settings.url]) {
            $('<a></a>').text( text ).attr( 'href', event[plugin.settings.url] ).appendTo($listItem);
         } else {
            $listItem.text( text );
         }
      }
      
      $element.bind('refresh', function(event, date) {
         refresh(date);
      });

      init();
   };

   $.fn.jqmCalendar = function(options) {
      return this.each(function() {
         if (!$(this).data('jqmCalendar')) {
             $(this).data('jqmCalendar', new $.jqmCalendar(this, options));
         }
      });
   };

})(jQuery);
