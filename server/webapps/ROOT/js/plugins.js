// Avoid `console` errors in browsers that lack a console.
(function() {
    var method;
    var noop = function () {};
    var methods = [
        'assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error',
        'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log',
        'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd',
        'timeline', 'timelineEnd', 'timeStamp', 'trace', 'warn'
    ];
    var length = methods.length;
    var console = (window.console = window.console || {});

    while (length--) {
        method = methods[length];

        // Only stub undefined methods.
        if (!console[method]) {
            console[method] = noop;
        }
    }
}());

/*! toastr */
!function(e){e(["jquery"],function(e){return function(){function t(e,t,n){return f({type:O.error,iconClass:g().iconClasses.error,message:e,optionsOverride:n,title:t})}function n(t,n){return t||(t=g()),v=e("#"+t.containerId),v.length?v:(n&&(v=c(t)),v)}function i(e,t,n){return f({type:O.info,iconClass:g().iconClasses.info,message:e,optionsOverride:n,title:t})}function o(e){w=e}function s(e,t,n){return f({type:O.success,iconClass:g().iconClasses.success,message:e,optionsOverride:n,title:t})}function a(e,t,n){return f({type:O.warning,iconClass:g().iconClasses.warning,message:e,optionsOverride:n,title:t})}function r(e){var t=g();v||n(t),l(e,t)||u(t)}function d(t){var i=g();return v||n(i),t&&0===e(":focus",t).length?void h(t):void(v.children().length&&v.remove())}function u(t){for(var n=v.children(),i=n.length-1;i>=0;i--)l(e(n[i]),t)}function l(t,n){return t&&0===e(":focus",t).length?(t[n.hideMethod]({duration:n.hideDuration,easing:n.hideEasing,complete:function(){h(t)}}),!0):!1}function c(t){return v=e("<div/>").attr("id",t.containerId).addClass(t.positionClass).attr("aria-live","polite").attr("role","alert"),v.appendTo(e(t.target)),v}function p(){return{tapToDismiss:!0,toastClass:"toast",containerId:"toast-container",debug:!1,showMethod:"fadeIn",showDuration:300,showEasing:"swing",onShown:void 0,hideMethod:"fadeOut",hideDuration:1e3,hideEasing:"swing",onHidden:void 0,extendedTimeOut:1e3,iconClasses:{error:"toast-error",info:"toast-info",success:"toast-success",warning:"toast-warning"},iconClass:"toast-info",positionClass:"toast-top-right",timeOut:5e3,titleClass:"toast-title",messageClass:"toast-message",target:"body",closeHtml:'<button type="button">&times;</button>',newestOnTop:!0,preventDuplicates:!1,progressBar:!1}}function m(e){w&&w(e)}function f(t){function i(t){return!e(":focus",l).length||t?(clearTimeout(O.intervalId),l[r.hideMethod]({duration:r.hideDuration,easing:r.hideEasing,complete:function(){h(l),r.onHidden&&"hidden"!==b.state&&r.onHidden(),b.state="hidden",b.endTime=new Date,m(b)}})):void 0}function o(){(r.timeOut>0||r.extendedTimeOut>0)&&(u=setTimeout(i,r.extendedTimeOut),O.maxHideTime=parseFloat(r.extendedTimeOut),O.hideEta=(new Date).getTime()+O.maxHideTime)}function s(){clearTimeout(u),O.hideEta=0,l.stop(!0,!0)[r.showMethod]({duration:r.showDuration,easing:r.showEasing})}function a(){var e=(O.hideEta-(new Date).getTime())/O.maxHideTime*100;f.width(e+"%")}var r=g(),d=t.iconClass||r.iconClass;if("undefined"!=typeof t.optionsOverride&&(r=e.extend(r,t.optionsOverride),d=t.optionsOverride.iconClass||d),r.preventDuplicates){if(t.message===C)return;C=t.message}T++,v=n(r,!0);var u=null,l=e("<div/>"),c=e("<div/>"),p=e("<div/>"),f=e("<div/>"),w=e(r.closeHtml),O={intervalId:null,hideEta:null,maxHideTime:null},b={toastId:T,state:"visible",startTime:new Date,options:r,map:t};return t.iconClass&&l.addClass(r.toastClass).addClass(d),t.title&&(c.append(t.title).addClass(r.titleClass),l.append(c)),t.message&&(p.append(t.message).addClass(r.messageClass),l.append(p)),r.closeButton&&(w.addClass("toast-close-button").attr("role","button"),l.prepend(w)),r.progressBar&&(f.addClass("toast-progress"),l.prepend(f)),l.hide(),r.newestOnTop?v.prepend(l):v.append(l),l[r.showMethod]({duration:r.showDuration,easing:r.showEasing,complete:r.onShown}),r.timeOut>0&&(u=setTimeout(i,r.timeOut),O.maxHideTime=parseFloat(r.timeOut),O.hideEta=(new Date).getTime()+O.maxHideTime,r.progressBar&&(O.intervalId=setInterval(a,10))),l.hover(s,o),!r.onclick&&r.tapToDismiss&&l.click(i),r.closeButton&&w&&w.click(function(e){e.stopPropagation?e.stopPropagation():void 0!==e.cancelBubble&&e.cancelBubble!==!0&&(e.cancelBubble=!0),i(!0)}),r.onclick&&l.click(function(){r.onclick(),i()}),m(b),r.debug&&console&&console.log(b),l}function g(){return e.extend({},p(),b.options)}function h(e){v||(v=n()),e.is(":visible")||(e.remove(),e=null,0===v.children().length&&(v.remove(),C=void 0))}var v,w,C,T=0,O={error:"error",info:"info",success:"success",warning:"warning"},b={clear:r,remove:d,error:t,getContainer:n,info:i,options:{},subscribe:o,success:s,version:"2.1.0",warning:a};return b}()})}("function"==typeof define&&define.amd?define:function(e,t){"undefined"!=typeof module&&module.exports?module.exports=t(require("jquery")):window.toastr=t(window.jQuery)});

/*! jQuery Mobile v1.4.5 | Copyright 2010, 2014 jQuery Foundation, Inc. | jquery.org/license */
(function(e,t,n){typeof define=="function"&&define.amd?define(["jquery"],function(r){return n(r,e,t),r.mobile}):n(e.jQuery,e,t)})(this,document,function(e,t,n,r){(function(e,t,n,r){function T(e){while(e&&typeof e.originalEvent!="undefined")e=e.originalEvent;return e}function N(t,n){var i=t.type,s,o,a,l,c,h,p,d,v;t=e.Event(t),t.type=n,s=t.originalEvent,o=e.event.props,i.search(/^(mouse|click)/)>-1&&(o=f);if(s)for(p=o.length,l;p;)l=o[--p],t[l]=s[l];i.search(/mouse(down|up)|click/)>-1&&!t.which&&(t.which=1);if(i.search(/^touch/)!==-1){a=T(s),i=a.touches,c=a.changedTouches,h=i&&i.length?i[0]:c&&c.length?c[0]:r;if(h)for(d=0,v=u.length;d<v;d++)l=u[d],t[l]=h[l]}return t}function C(t){var n={},r,s;while(t){r=e.data(t,i);for(s in r)r[s]&&(n[s]=n.hasVirtualBinding=!0);t=t.parentNode}return n}function k(t,n){var r;while(t){r=e.data(t,i);if(r&&(!n||r[n]))return t;t=t.parentNode}return null}function L(){g=!1}function A(){g=!0}function O(){E=0,v.length=0,m=!1,A()}function M(){L()}function _(){D(),c=setTimeout(function(){c=0,O()},e.vmouse.resetTimerDuration)}function D(){c&&(clearTimeout(c),c=0)}function P(t,n,r){var i;if(r&&r[t]||!r&&k(n.target,t))i=N(n,t),e(n.target).trigger(i);return i}function H(t){var n=e.data(t.target,s),r;!m&&(!E||E!==n)&&(r=P("v"+t.type,t),r&&(r.isDefaultPrevented()&&t.preventDefault(),r.isPropagationStopped()&&t.stopPropagation(),r.isImmediatePropagationStopped()&&t.stopImmediatePropagation()))}function B(t){var n=T(t).touches,r,i,o;n&&n.length===1&&(r=t.target,i=C(r),i.hasVirtualBinding&&(E=w++,e.data(r,s,E),D(),M(),d=!1,o=T(t).touches[0],h=o.pageX,p=o.pageY,P("vmouseover",t,i),P("vmousedown",t,i)))}function j(e){if(g)return;d||P("vmousecancel",e,C(e.target)),d=!0,_()}function F(t){if(g)return;var n=T(t).touches[0],r=d,i=e.vmouse.moveDistanceThreshold,s=C(t.target);d=d||Math.abs(n.pageX-h)>i||Math.abs(n.pageY-p)>i,d&&!r&&P("vmousecancel",t,s),P("vmousemove",t,s),_()}function I(e){if(g)return;A();var t=C(e.target),n,r;P("vmouseup",e,t),d||(n=P("vclick",e,t),n&&n.isDefaultPrevented()&&(r=T(e).changedTouches[0],v.push({touchID:E,x:r.clientX,y:r.clientY}),m=!0)),P("vmouseout",e,t),d=!1,_()}function q(t){var n=e.data(t,i),r;if(n)for(r in n)if(n[r])return!0;return!1}function R(){}function U(t){var n=t.substr(1);return{setup:function(){q(this)||e.data(this,i,{});var r=e.data(this,i);r[t]=!0,l[t]=(l[t]||0)+1,l[t]===1&&b.bind(n,H),e(this).bind(n,R),y&&(l.touchstart=(l.touchstart||0)+1,l.touchstart===1&&b.bind("touchstart",B).bind("touchend",I).bind("touchmove",F).bind("scroll",j))},teardown:function(){--l[t],l[t]||b.unbind(n,H),y&&(--l.touchstart,l.touchstart||b.unbind("touchstart",B).unbind("touchmove",F).unbind("touchend",I).unbind("scroll",j));var r=e(this),s=e.data(this,i);s&&(s[t]=!1),r.unbind(n,R),q(this)||r.removeData(i)}}}var i="virtualMouseBindings",s="virtualTouchID",o="vmouseover vmousedown vmousemove vmouseup vclick vmouseout vmousecancel".split(" "),u="clientX clientY pageX pageY screenX screenY".split(" "),a=e.event.mouseHooks?e.event.mouseHooks.props:[],f=e.event.props.concat(a),l={},c=0,h=0,p=0,d=!1,v=[],m=!1,g=!1,y="addEventListener"in n,b=e(n),w=1,E=0,S,x;e.vmouse={moveDistanceThreshold:10,clickDistanceThreshold:10,resetTimerDuration:1500};for(x=0;x<o.length;x++)e.event.special[o[x]]=U(o[x]);y&&n.addEventListener("click",function(t){var n=v.length,r=t.target,i,o,u,a,f,l;if(n){i=t.clientX,o=t.clientY,S=e.vmouse.clickDistanceThreshold,u=r;while(u){for(a=0;a<n;a++){f=v[a],l=0;if(u===r&&Math.abs(f.x-i)<S&&Math.abs(f.y-o)<S||e.data(u,s)===f.touchID){t.preventDefault(),t.stopPropagation();return}}u=u.parentNode}}},!0)})(e,t,n),function(e){e.mobile={}}(e),function(e,t){var r={touch:"ontouchend"in n};e.mobile.support=e.mobile.support||{},e.extend(e.support,r),e.extend(e.mobile.support,r)}(e),function(e,t,r){function l(t,n,i,s){var o=i.type;i.type=n,s?e.event.trigger(i,r,t):e.event.dispatch.call(t,i),i.type=o}var i=e(n),s=e.mobile.support.touch,o="touchmove scroll",u=s?"touchstart":"mousedown",a=s?"touchend":"mouseup",f=s?"touchmove":"mousemove";e.each("touchstart touchmove touchend tap taphold swipe swipeleft swiperight scrollstart scrollstop".split(" "),function(t,n){e.fn[n]=function(e){return e?this.bind(n,e):this.trigger(n)},e.attrFn&&(e.attrFn[n]=!0)}),e.event.special.scrollstart={enabled:!0,setup:function(){function s(e,n){r=n,l(t,r?"scrollstart":"scrollstop",e)}var t=this,n=e(t),r,i;n.bind(o,function(t){if(!e.event.special.scrollstart.enabled)return;r||s(t,!0),clearTimeout(i),i=setTimeout(function(){s(t,!1)},50)})},teardown:function(){e(this).unbind(o)}},e.event.special.tap={tapholdThreshold:750,emitTapOnTaphold:!0,setup:function(){var t=this,n=e(t),r=!1;n.bind("vmousedown",function(s){function a(){clearTimeout(u)}function f(){a(),n.unbind("vclick",c).unbind("vmouseup",a),i.unbind("vmousecancel",f)}function c(e){f(),!r&&o===e.target?l(t,"tap",e):r&&e.preventDefault()}r=!1;if(s.which&&s.which!==1)return!1;var o=s.target,u;n.bind("vmouseup",a).bind("vclick",c),i.bind("vmousecancel",f),u=setTimeout(function(){e.event.special.tap.emitTapOnTaphold||(r=!0),l(t,"taphold",e.Event("taphold",{target:o}))},e.event.special.tap.tapholdThreshold)})},teardown:function(){e(this).unbind("vmousedown").unbind("vclick").unbind("vmouseup"),i.unbind("vmousecancel")}},e.event.special.swipe={scrollSupressionThreshold:30,durationThreshold:1e3,horizontalDistanceThreshold:30,verticalDistanceThreshold:30,getLocation:function(e){var n=t.pageXOffset,r=t.pageYOffset,i=e.clientX,s=e.clientY;if(e.pageY===0&&Math.floor(s)>Math.floor(e.pageY)||e.pageX===0&&Math.floor(i)>Math.floor(e.pageX))i-=n,s-=r;else if(s<e.pageY-r||i<e.pageX-n)i=e.pageX-n,s=e.pageY-r;return{x:i,y:s}},start:function(t){var n=t.originalEvent.touches?t.originalEvent.touches[0]:t,r=e.event.special.swipe.getLocation(n);return{time:(new Date).getTime(),coords:[r.x,r.y],origin:e(t.target)}},stop:function(t){var n=t.originalEvent.touches?t.originalEvent.touches[0]:t,r=e.event.special.swipe.getLocation(n);return{time:(new Date).getTime(),coords:[r.x,r.y]}},handleSwipe:function(t,n,r,i){if(n.time-t.time<e.event.special.swipe.durationThreshold&&Math.abs(t.coords[0]-n.coords[0])>e.event.special.swipe.horizontalDistanceThreshold&&Math.abs(t.coords[1]-n.coords[1])<e.event.special.swipe.verticalDistanceThreshold){var s=t.coords[0]>n.coords[0]?"swipeleft":"swiperight";return l(r,"swipe",e.Event("swipe",{target:i,swipestart:t,swipestop:n}),!0),l(r,s,e.Event(s,{target:i,swipestart:t,swipestop:n}),!0),!0}return!1},eventInProgress:!1,setup:function(){var t,n=this,r=e(n),s={};t=e.data(this,"mobile-events"),t||(t={length:0},e.data(this,"mobile-events",t)),t.length++,t.swipe=s,s.start=function(t){if(e.event.special.swipe.eventInProgress)return;e.event.special.swipe.eventInProgress=!0;var r,o=e.event.special.swipe.start(t),u=t.target,l=!1;s.move=function(t){if(!o||t.isDefaultPrevented())return;r=e.event.special.swipe.stop(t),l||(l=e.event.special.swipe.handleSwipe(o,r,n,u),l&&(e.event.special.swipe.eventInProgress=!1)),Math.abs(o.coords[0]-r.coords[0])>e.event.special.swipe.scrollSupressionThreshold&&t.preventDefault()},s.stop=function(){l=!0,e.event.special.swipe.eventInProgress=!1,i.off(f,s.move),s.move=null},i.on(f,s.move).one(a,s.stop)},r.on(u,s.start)},teardown:function(){var t,n;t=e.data(this,"mobile-events"),t&&(n=t.swipe,delete t.swipe,t.length--,t.length===0&&e.removeData(this,"mobile-events")),n&&(n.start&&e(this).off(u,n.start),n.move&&i.off(f,n.move),n.stop&&i.off(a,n.stop))}},e.each({scrollstop:"scrollstart",taphold:"tap",swipeleft:"swipe.left",swiperight:"swipe.right"},function(t,n){e.event.special[t]={setup:function(){e(this).bind(n,e.noop)},teardown:function(){e(this).unbind(n)}}})}(e,this)});

/*! chatjs */
(function($) {
  var defaults = {
      className: "autosizejs",
      id: "autosizejs",
      append: "",
      callback: false,
      resizeDelay: 10,
      placeholder: true
    },
    copy = '<textarea tabindex="-1" style="position:absolute; top:-999px; left:0; right:auto; bottom:auto; border:0; padding: 0; -moz-box-sizing:content-box; -webkit-box-sizing:content-box; box-sizing:content-box; word-wrap:break-word; height:0 !important; min-height:0 !important; overflow:hidden; transition:none; -webkit-transition:none; -moz-transition:none;"/>',
    typographyStyles = ["fontFamily", "fontSize", "fontWeight", "fontStyle", "letterSpacing", "textTransform", "wordSpacing", "textIndent"],
    mirrored, mirror = $(copy).data("autosize", true)[0];
  mirror.style.lineHeight = "99px";
  if ($(mirror).css("lineHeight") === "99px") {
    typographyStyles.push("lineHeight")
  }
  mirror.style.lineHeight = "";
  $.fn.autosize = function(options) {
    if (!this.length) {
      return this
    }
    options = $.extend({}, defaults, options || {});
    if (mirror.parentNode !== document.body) {
      $(document.body).append(mirror)
    }
    return this.each(function() {
      var ta = this,
        $ta = $(ta),
        maxHeight, minHeight, boxOffset = 0,
        callback = $.isFunction(options.callback),
        originalStyles = {
          height: ta.style.height,
          overflow: ta.style.overflow,
          overflowY: ta.style.overflowY,
          wordWrap: ta.style.wordWrap,
          resize: ta.style.resize
        },
        timeout, width = $ta.width();
      if ($ta.data("autosize")) {
        return
      }
      $ta.data("autosize", true);
      if ($ta.css("box-sizing") === "border-box" || $ta.css("-moz-box-sizing") === "border-box" || $ta.css("-webkit-box-sizing") === "border-box") {
        boxOffset = $ta.outerHeight() - $ta.height()
      }
      minHeight = Math.max(parseInt($ta.css("minHeight"), 10) - boxOffset || 0, $ta.height());
      $ta.css({
        overflow: "hidden",
        overflowY: "hidden",
        wordWrap: "break-word",
        resize: $ta.css("resize") === "none" || $ta.css("resize") === "vertical" ? "none" : "horizontal"
      });

      function setWidth() {
        var width;
        var style = window.getComputedStyle ? window.getComputedStyle(ta, null) : false;
        if (style) {
          width = ta.getBoundingClientRect().width;
          if (width === 0) {
            width = parseInt(style.width, 10)
          }
          $.each(["paddingLeft", "paddingRight", "borderLeftWidth", "borderRightWidth"], function(i, val) {
            width -= parseInt(style[val], 10)
          })
        } else {
          width = Math.max($ta.width(), 0)
        }
        mirror.style.width = width + "px"
      }

      function initMirror() {
        var styles = {};
        mirrored = ta;
        mirror.className = options.className;
        mirror.id = options.id;
        maxHeight = parseInt($ta.css("maxHeight"), 10);
        $.each(typographyStyles, function(i, val) {
          styles[val] = $ta.css(val)
        });
        $(mirror).css(styles);
        setWidth();
        if (window.chrome) {
          var width = ta.style.width;
          ta.style.width = "0px";
          var ignore = ta.offsetWidth;
          ta.style.width = width
        }
      }

      function adjust() {
        var height, original;
        if (mirrored !== ta) {
          initMirror()
        } else {
          setWidth()
        }
        if (!ta.value && options.placeholder) {
          mirror.value = ($ta.attr("placeholder") || "") + options.append
        } else {
          mirror.value = ta.value + options.append
        }
        mirror.style.overflowY = ta.style.overflowY;
        original = parseInt(ta.style.height, 10);
        mirror.scrollTop = 0;
        mirror.scrollTop = 9e4;
        height = mirror.scrollTop;
        if (maxHeight && height > maxHeight) {
          ta.style.overflowY = "scroll";
          height = maxHeight
        } else {
          ta.style.overflowY = "hidden";
          if (height < minHeight) {
            height = minHeight
          }
        }
        height += boxOffset;
        if (original !== height) {
          ta.style.height = height + "px";
          if (callback) {
            options.callback.call(ta, ta)
          }
        }
      }

      function resize() {
        clearTimeout(timeout);
        timeout = setTimeout(function() {
          var newWidth = $ta.width();
          if (newWidth !== width) {
            width = newWidth;
            adjust()
          }
        }, parseInt(options.resizeDelay, 10))
      }
      if ("onpropertychange" in ta) {
        if ("oninput" in ta) {
          $ta.on("input.autosize keyup.autosize", adjust)
        } else {
          $ta.on("propertychange.autosize", function() {
            if (event.propertyName === "value") {
              adjust()
            }
          })
        }
      } else {
        $ta.on("input.autosize", adjust)
      }
      if (options.resizeDelay !== false) {
        $(window).on("resize.autosize", resize)
      }
      $ta.on("autosize.resize", adjust);
      $ta.on("autosize.resizeIncludeStyle", function() {
        mirrored = null;
        adjust()
      });
      $ta.on("autosize.destroy", function() {
        mirrored = null;
        clearTimeout(timeout);
        $(window).off("resize", resize);
        $ta.off("autosize").off(".autosize").css(originalStyles).removeData("autosize")
      });
      adjust()
    })
  }
})(window.jQuery || window.$);
var ChatJsUtils = function() {
  function ChatJsUtils() {}
  ChatJsUtils.setOuterHeight = function(jQuery, height) {
    var heights = new Array;
    heights.push(parseInt(jQuery.css("padding-top").replace("px", "")));
    heights.push(parseInt(jQuery.css("padding-bottom").replace("px", "")));
    heights.push(parseInt(jQuery.css("border-top-width").replace("px", "")));
    heights.push(parseInt(jQuery.css("border-bottom-width").replace("px", "")));
    heights.push(parseInt(jQuery.css("margin-top").replace("px", "")));
    heights.push(parseInt(jQuery.css("margin-bottom").replace("px", "")));
    var calculatedHeight = height;
    for (var i = 0; i < heights.length; i++) calculatedHeight -= heights[i];
    jQuery.height(calculatedHeight)
  };
  ChatJsUtils.setOuterWidth = function(jQuery, width) {
    var widths = new Array;
    widths.push(parseInt(jQuery.css("padding-left").replace("px", "")));
    widths.push(parseInt(jQuery.css("padding-right").replace("px", "")));
    widths.push(parseInt(jQuery.css("border-top-left").replace("px", "")));
    widths.push(parseInt(jQuery.css("border-bottom-right").replace("px", "")));
    widths.push(parseInt(jQuery.css("margin-left").replace("px", "")));
    widths.push(parseInt(jQuery.css("margin-right").replace("px", "")));
    var calculatedWidth = width;
    for (var i = 0; i < widths.length; i++) calculatedWidth -= widths[i];
    jQuery.width(calculatedWidth)
  };
  return ChatJsUtils
}();
var ChatJsUtils = function() {
  function ChatJsUtils() {}
  ChatJsUtils.setOuterHeight = function(jQuery, height) {
    var heights = new Array;
    heights.push(parseInt(jQuery.css("padding-top").replace("px", "")));
    heights.push(parseInt(jQuery.css("padding-bottom").replace("px", "")));
    heights.push(parseInt(jQuery.css("border-top-width").replace("px", "")));
    heights.push(parseInt(jQuery.css("border-bottom-width").replace("px", "")));
    heights.push(parseInt(jQuery.css("margin-top").replace("px", "")));
    heights.push(parseInt(jQuery.css("margin-bottom").replace("px", "")));
    var calculatedHeight = height;
    for (var i = 0; i < heights.length; i++) calculatedHeight -= heights[i];
    jQuery.height(calculatedHeight)
  };
  ChatJsUtils.setOuterWidth = function(jQuery, width) {
    var widths = new Array;
    widths.push(parseInt(jQuery.css("padding-left").replace("px", "")));
    widths.push(parseInt(jQuery.css("padding-right").replace("px", "")));
    widths.push(parseInt(jQuery.css("border-top-left").replace("px", "")));
    widths.push(parseInt(jQuery.css("border-bottom-right").replace("px", "")));
    widths.push(parseInt(jQuery.css("margin-left").replace("px", "")));
    widths.push(parseInt(jQuery.css("margin-right").replace("px", "")));
    var calculatedWidth = width;
    for (var i = 0; i < widths.length; i++) calculatedWidth -= widths[i];
    jQuery.width(calculatedWidth)
  };
  return ChatJsUtils
}();
var ChatMessageInfo = function() {
  function ChatMessageInfo() {}
  return ChatMessageInfo
}();
var UserStatusType;
(function(UserStatusType) {
  UserStatusType[UserStatusType["Offline"] = 0] = "Offline";
  UserStatusType[UserStatusType["Online"] = 1] = "Online"
})(UserStatusType || (UserStatusType = {}));
var ChatUserInfo = function() {
  function ChatUserInfo() {}
  return ChatUserInfo
}();
var ChatRoomInfo = function() {
  function ChatRoomInfo() {}
  return ChatRoomInfo
}();
var ChatTypingSignalInfo = function() {
  function ChatTypingSignalInfo() {}
  return ChatTypingSignalInfo
}();
var ChatUserListChangedInfo = function() {
  function ChatUserListChangedInfo() {}
  return ChatUserListChangedInfo
}();
var ChatRoomListChangedInfo = function() {
  function ChatRoomListChangedInfo() {}
  return ChatRoomListChangedInfo
}();
var SignalRServerAdapter = function() {
  function SignalRServerAdapter(chatHubServer) {
    this.hubServer = chatHubServer
  }
  SignalRServerAdapter.prototype.sendMessage = function(roomId, conversationId, otherUserId, messageText, clientGuid, done) {
    this.hubServer.sendMessage(roomId, conversationId, otherUserId, messageText, clientGuid).done(function() {
      done()
    })
  };
  SignalRServerAdapter.prototype.sendTypingSignal = function(roomId, conversationId, userToId, done) {
    this.hubServer.sendTypingSignal(roomId, conversationId, userToId).done(function() {
      done()
    })
  };
  SignalRServerAdapter.prototype.getMessageHistory = function(roomId, conversationId, otherUserId, done) {
    this.hubServer.getMessageHistory(roomId, conversationId, otherUserId).done(function(messageHistory) {
      done(messageHistory)
    })
  };
  SignalRServerAdapter.prototype.getUserInfo = function(userId, done) {
    this.hubServer.getUserInfo(userId).done(function(userInfo) {
      done(userInfo)
    })
  };
  SignalRServerAdapter.prototype.getUserList = function(roomId, conversationId, done) {
    this.hubServer.getUserList(roomId, conversationId).done(function(userList) {
      done(userList)
    })
  };
  SignalRServerAdapter.prototype.getRoomsList = function(done) {
    this.hubServer.getRoomsList().done(function(roomsList) {
      done(roomsList)
    })
  };
  SignalRServerAdapter.prototype.enterRoom = function(roomId, done) {
    this.hubServer.enterRoom(roomId).done(function() {
      done()
    })
  };
  SignalRServerAdapter.prototype.leaveRoom = function(roomId, done) {
    this.hubServer.leaveRoom(roomId).done(function() {
      done()
    })
  };
  return SignalRServerAdapter
}();
var SignalRClientAdapter = function() {
  function SignalRClientAdapter(chatHubClient) {
    var _this = this;
    this.messagesChangedHandlers = [];
    this.typingSignalReceivedHandlers = [];
    this.userListChangedHandlers = [];
    this.roomListChangedHandlers = [];
    this.hubClient = chatHubClient;
    this.hubClient.sendMessage = function(message) {
      _this.triggerMessagesChanged(message)
    };
    this.hubClient.sendTypingSignal = function(typingSignal) {
      _this.triggerTypingSignalReceived(typingSignal)
    };
    this.hubClient.userListChanged = function(userListChangedInfo) {
      _this.triggerUserListChanged(userListChangedInfo)
    };
    this.hubClient.roomListChanged = function(roomListChangedInfo) {
      _this.triggerRoomListChanged(roomListChangedInfo)
    }
  }
  SignalRClientAdapter.prototype.onMessagesChanged = function(handler) {
    this.messagesChangedHandlers.push(handler)
  };
  SignalRClientAdapter.prototype.onTypingSignalReceived = function(handler) {
    this.typingSignalReceivedHandlers.push(handler)
  };
  SignalRClientAdapter.prototype.onUserListChanged = function(handler) {
    this.userListChangedHandlers.push(handler)
  };
  SignalRClientAdapter.prototype.onRoomListChanged = function(handler) {
    this.roomListChangedHandlers.push(handler)
  };
  SignalRClientAdapter.prototype.triggerMessagesChanged = function(message) {
    for (var i = 0; i < this.messagesChangedHandlers.length; i++) this.messagesChangedHandlers[i](message)
  };
  SignalRClientAdapter.prototype.triggerTypingSignalReceived = function(typingSignal) {
    for (var i = 0; i < this.typingSignalReceivedHandlers.length; i++) this.typingSignalReceivedHandlers[i](typingSignal)
  };
  SignalRClientAdapter.prototype.triggerUserListChanged = function(userListChangedInfo) {
    for (var i = 0; i < this.userListChangedHandlers.length; i++) this.userListChangedHandlers[i](userListChangedInfo)
  };
  SignalRClientAdapter.prototype.triggerRoomListChanged = function(roomListChangedInfo) {
    for (var i = 0; i < this.roomListChangedHandlers.length; i++) this.roomListChangedHandlers[i](roomListChangedInfo)
  };
  return SignalRClientAdapter
}();
var SignalRAdapterOptions = function() {
  function SignalRAdapterOptions() {}
  return SignalRAdapterOptions
}();
var SignalRAdapter = function() {
  function SignalRAdapter(options) {
    var defaultOptions = new SignalRAdapterOptions;
    defaultOptions.chatHubName = "chatHub";
    this.options = $.extend({}, defaultOptions, options)
  }
  SignalRAdapter.prototype.init = function(done) {
    this.hub = $.connection[this.options.chatHubName];
    this.client = new SignalRClientAdapter(this.hub.client);
    this.server = new SignalRServerAdapter(this.hub.server);
    if (!window.chatJsHubReady) window.chatJsHubReady = $.connection.hub.start();
    window.chatJsHubReady.done(function() {
      done()
    })
  };
  return SignalRAdapter
}();
var ChatWindowOptions = function() {
  function ChatWindowOptions() {}
  return ChatWindowOptions
}();
var ChatWindow = function() {
  function ChatWindow(options) {
    var _this = this;
    var defaultOptions = new ChatWindowOptions;
    defaultOptions.isMaximized = true;
    defaultOptions.canClose = true;
    defaultOptions.onCreated = function() {};
    defaultOptions.onClose = function() {};
    defaultOptions.onMaximizedStateChanged = function() {};
    this.options = $.extend({}, defaultOptions, options);
    this.$window = $("<div/>").addClass("chat-window").appendTo($("body"));
    if (this.options.width) this.$window.css("width", this.options.width);
    this.$windowTitle = $("<div/>").addClass("chat-window-title").appendTo(this.$window);
    if (this.options.canClose) {
      var $closeButton = $("<div/>").addClass("close").appendTo(this.$windowTitle);
      $closeButton.click(function(e) {
        e.stopPropagation();
        _this.options.onClose(_this);
        _this.$window.remove();
      })
    }
    $("<div/>").addClass("text").text(this.options.title).appendTo(this.$windowTitle);
    this.$windowContent = $("<div/>").addClass("chat-window-content").appendTo(this.$window);
    if (this.options.height) this.$windowContent.css("height", this.options.height);
    this.$windowInnerContent = $("<div/>").addClass("chat-window-inner-content").appendTo(this.$windowContent);
    this.$windowTitle.click(function() {
      _this.toggleMaximizedState()
    });
    this.setState(this.options.isMaximized, false);
    this.options.onCreated(this)
  }
  ChatWindow.prototype.getWidth = function() {
    return this.$window.outerWidth()
  };
  ChatWindow.prototype.setRightOffset = function(offset) {
    this.$window.css("right", offset)
  };
  ChatWindow.prototype.setTitle = function(title) {
    $("div[class=text]", this.$windowTitle).text(title)
  };
  ChatWindow.prototype.setVisible = function(visible) {
    if (visible) this.$window.show();
    else this.$window.hide()
  };
  ChatWindow.prototype.getState = function() {
    return !this.$window.hasClass("minimized")
  };
  ChatWindow.prototype.setState = function(state, triggerMaximizedStateEvent) {
    if (typeof triggerMaximizedStateEvent === "undefined") {
      triggerMaximizedStateEvent = true
    }
    if (state) {
      this.$window.removeClass("minimized");
      this.$windowContent.show()
    } else {
      this.$window.addClass("minimized");
      this.$windowContent.hide()
    }
    if (triggerMaximizedStateEvent) this.options.onMaximizedStateChanged(this, state)
  };
  ChatWindow.prototype.toggleMaximizedState = function() {
    this.setState(this.$window.hasClass("minimized"))
  };
  ChatWindow.prototype.focus = function() {};
  return ChatWindow
}();
$.chatWindow = function(options) {
  var chatWindow = new ChatWindow(options);
  return chatWindow
};
var MessageBoardOptions = function() {
  function MessageBoardOptions() {}
  return MessageBoardOptions
}();
var MessageBoard = function() {
  function MessageBoard(jQuery, options) {
    var _this = this;
    this.$el = jQuery;
    var defaultOptions = new MessageBoardOptions;
    defaultOptions.typingText = " is typing...";
    defaultOptions.playSound = true;
    defaultOptions.height = 100;
    defaultOptions.chatJsContentPath = "/chatjs/";
    defaultOptions.newMessage = function(message) {};
    this.options = $.extend({}, defaultOptions, options);
    this.$el.addClass("message-board");
    ChatJsUtils.setOuterHeight(this.$el, this.options.height);
    this.$messagesWrapper = $("<div/>").addClass("messages-wrapper").appendTo(this.$el);
    var $windowTextBoxWrapper = $("<div/>").addClass("chat-window-text-box-wrapper").appendTo(this.$el);
    this.$textBox = $("<textarea />").attr("rows", "1").addClass("chat-window-text-box").appendTo($windowTextBoxWrapper);
    this.$textBox.autosize({
      callback: function(ta) {
        var messagesHeight = _this.options.height - $(ta).outerHeight();
        ChatJsUtils.setOuterHeight(_this.$messagesWrapper, messagesHeight)
      }
    });
    this.$textBox.val(this.$textBox.val());
    this.options.adapter.client.onTypingSignalReceived(function(typingSignal) {
      var shouldProcessTypingSignal = false;
      if (_this.options.otherUserId) {
        shouldProcessTypingSignal = typingSignal.UserToId == _this.options.userId && typingSignal.UserFrom.Id == _this.options.otherUserId
      } else if (_this.options.roomId) {
        shouldProcessTypingSignal = typingSignal.RoomId == _this.options.roomId && typingSignal.UserFrom.Id != _this.options.userId
      } else if (_this.options.conversationId) {
        shouldProcessTypingSignal = typingSignal.ConversationId == _this.options.conversationId && typingSignal.UserFrom.Id != _this.options.userId
      }
      if (shouldProcessTypingSignal) _this.showTypingSignal(typingSignal.UserFrom)
    });
    this.options.adapter.client.onMessagesChanged(this.bindOnMessage.bind(this));
    this.options.adapter.server.getMessageHistory(this.options.roomId, this.options.conversationId, this.options.otherUserId, function(messages) {
      for (var i = 0; i < messages.length; i++) {
        _this.addMessage(messages[i], null, false)
      }
      _this.adjustScroll();
      _this.$textBox.keypress(function(e) {
        if (_this.sendTypingSignalTimeout == undefined) {
          _this.sendTypingSignalTimeout = setTimeout(function() {
            _this.sendTypingSignalTimeout = undefined
          }, 3e3);
          _this.sendTypingSignal()
        }
        if (e.which == 13) {
          e.preventDefault();
          if (_this.$textBox.val()) {
            handleMessage(_this);
          }
        }
      })
    })
  }
  MessageBoard.prototype.bindOnMessage = function(message) {
      var shouldProcessMessage = false;
      var _this = this;
      if (_this.options.otherUserId) {
        shouldProcessMessage = message.UserFromId == _this.options.userId && message.UserToId == _this.options.otherUserId || message.UserFromId == _this.options.otherUserId && message.UserToId == _this.options.userId
      } else if (_this.options.roomId) {
        shouldProcessMessage = message.RoomId == _this.options.roomId
      } else if (_this.options.conversationId) {
        shouldProcessMessage = message.ConversationId == _this.options.conversationId
      }
      if (shouldProcessMessage) {
        _this.addMessage(message);
        if (message.UserFromId != _this.options.userId) {
          if (_this.options.playSound) _this.playSound()
        }
        _this.options.newMessage(message)
      }
    };
  MessageBoard.prototype.unBindOnMessage = function() {
    this.options.adapter.client.unBindMessagesChanged(this.bindOnMessage);
  };
  MessageBoard.prototype.showTypingSignal = function(user) {
    var _this = this;
    if (this.$typingSignal) this.$typingSignal.remove();
    this.$typingSignal = $("<p/>").addClass("typing-signal").text(user.Name + this.options.typingText);
    this.$messagesWrapper.append(this.$typingSignal);
    if (this.typingSignalTimeout) clearTimeout(this.typingSignalTimeout);
    this.typingSignalTimeout = setTimeout(function() {
      _this.removeTypingSignal()
    }, 5e3);
    this.adjustScroll()
  };
  MessageBoard.prototype.removeTypingSignal = function() {
    if (this.$typingSignal) this.$typingSignal.remove();
    if (this.typingSignalTimeout) clearTimeout(this.typingSignalTimeout)
  };
  MessageBoard.prototype.adjustScroll = function() {
    this.$messagesWrapper[0].scrollTop = this.$messagesWrapper[0].scrollHeight
  };
  MessageBoard.prototype.sendTypingSignal = function() {
    this.options.adapter.server.sendTypingSignal(this.options.roomId, this.options.conversationId, this.options.otherUserId, function() {})
  };
  MessageBoard.prototype.sendMessage = function(messageText) {
    var generateGuidPart = function() {
      return ((1 + Math.random()) * 65536 | 0).toString(16).substring(1)
    };
    var clientGuid = generateGuidPart() + generateGuidPart() + "-" + generateGuidPart() + "-" + generateGuidPart() + "-" + generateGuidPart() + "-" + generateGuidPart() + generateGuidPart() + generateGuidPart();
    var message = new ChatMessageInfo;
    message.UserFromId = this.options.userId;
    message.Message = messageText;
    this.addMessage(message, clientGuid);
    this.options.adapter.server.sendMessage(this.options.roomId, this.options.conversationId, this.options.otherUserId, messageText, clientGuid, function() {})
  };
  MessageBoard.prototype.playSound = function() {
    var $soundContainer = $("#soundContainer");
    if (!$soundContainer.length) $soundContainer = $("<div>").attr("id", "soundContainer").appendTo($("body"));
    var baseFileName = this.options.chatJsContentPath + "sounds/chat";
    var oggFileName = baseFileName + ".ogg";
    var mp3FileName = baseFileName + ".mp3";
    var $audioTag = $("<audio/>").attr("autoplay", "autoplay");
    $("<source/>").attr("src", oggFileName).attr("type", "audio/mpeg").appendTo($audioTag);
    $("<embed/>").attr("src", mp3FileName).attr("autostart", "true").attr("loop", "false").appendTo($audioTag);
    $audioTag.appendTo($soundContainer)
  };
  MessageBoard.prototype.focus = function() {
    this.$textBox.focus()
  };
  MessageBoard.prototype.addMessage = function(message, clientGuid, scroll) {
    if (scroll == undefined) scroll = true;
    if (message.UserFromId != this.options.userId) {
      this.removeTypingSignal()
    }

    function linkify($element) {
      var inputText = $element.html();
      var replacedText, replacePattern1, replacePattern2, replacePattern3;
      replacePattern1 = /(\b(https?|ftp):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/gim;
      replacedText = inputText.replace(replacePattern1, '<a href="$1" target="_blank">$1</a>');
      replacePattern2 = /(^|[^\/])(www\.[\S]+(\b|$))/gim;
      replacedText = replacedText.replace(replacePattern2, '$1<a href="http://$2" target="_blank">$2</a>');
      replacePattern3 = /(\w+@[a-zA-Z_]+?\.[a-zA-Z]{2,6})/gim;
      replacedText = replacedText.replace(replacePattern3, '<a href="mailto:$1">$1</a>');
      return $element.html(replacedText)
    }

    function emotify($element) {
      var inputText = $element.html();
      var replacedText = inputText;
      var emoticons = [{
        pattern: ":-)",
        cssClass: "happy"
      }, {
        pattern: ":)",
        cssClass: "happy"
      }, {
        pattern: "=)",
        cssClass: "happy"
      }, {
        pattern: ":-D",
        cssClass: "very-happy"
      }, {
        pattern: ":D",
        cssClass: "very-happy"
      }, {
        pattern: "=D",
        cssClass: "very-happy"
      }, {
        pattern: ":-(",
        cssClass: "sad"
      }, {
        pattern: ":(",
        cssClass: "sad"
      }, {
        pattern: "=(",
        cssClass: "sad"
      }, {
        pattern: ":-|",
        cssClass: "wary"
      }, {
        pattern: ":|",
        cssClass: "wary"
      }, {
        pattern: "=|",
        cssClass: "wary"
      }, {
        pattern: ":-O",
        cssClass: "astonished"
      }, {
        pattern: ":O",
        cssClass: "astonished"
      }, {
        pattern: "=O",
        cssClass: "astonished"
      }, {
        pattern: ":-P",
        cssClass: "tongue"
      }, {
        pattern: ":P",
        cssClass: "tongue"
      }, {
        pattern: "=P",
        cssClass: "tongue"
      }];
      for (var i = 0; i < emoticons.length; i++) {
        replacedText = replacedText.replace(emoticons[i].pattern, "<span class='" + emoticons[i].cssClass + "'></span>")
      }
      return $element.html(replacedText)
    }
    if (message.ClientGuid && $("p[data-val-client-guid='" + message.ClientGuid + "']").length) {
      $("p[data-val-client-guid='" + message.ClientGuid + "']").removeClass("temp-message").removeAttr("data-val-client-guid")
    } else {
      var $messageP = $("<p/>").text(message.Message);
      if (clientGuid) $messageP.attr("data-val-client-guid", clientGuid).addClass("temp-message");
      linkify($messageP);
      emotify($messageP);
      var $lastMessage = $("div.chat-message:last", this.$messagesWrapper);
      if ($lastMessage.length && $lastMessage.attr("data-val-user-from") == message.UserFromId.toString()) {
        $messageP.appendTo($(".chat-text-wrapper", $lastMessage))
      } else {
        var $chatMessage = $("<div/>").addClass("chat-message").attr("data-val-user-from", message.UserFromId);
        $chatMessage.appendTo(this.$messagesWrapper);
        var $gravatarWrapper = $("<div/>").addClass("chat-gravatar-wrapper").appendTo($chatMessage);
        var $textWrapper = $("<div/>").addClass("chat-text-wrapper").appendTo($chatMessage);
        $messageP.appendTo($textWrapper);
        var $img = $("<strong/>").addClass("profile-picture").appendTo($gravatarWrapper);
        this.options.adapter.server.getUserInfo(message.UserFromId, function(user) {
          $img.text(user.Name.slice(0, 2))
        })
      }
    }
    if (scroll) this.adjustScroll()
  };
  return MessageBoard
}();
$.fn.messageBoard = function(options) {
  if (this.length) {
    this.each(function() {
      var data = new MessageBoard($(this), options);
      $(this).data("messageBoard", data);
    })
  }
  return this
};
$.fn.unBindOnMessage = function() {
  if (this.length) {
    this.each(function() {
      var data = $(this).data("messageBoard");
      data.unBindOnMessage();
    });
  }
};
var UserListOptions = function() {
  function UserListOptions() {}
  return UserListOptions
}();
var UserList = function() {
  function UserList(jQuery, options) {
    var _this = this;
    this.$el = jQuery;
    var defaultOptions = new UserListOptions;
    defaultOptions.emptyRoomText = "No users available for chatting.";
    defaultOptions.height = 100;
    defaultOptions.excludeCurrentUser = false;
    defaultOptions.userClicked = function() {};
    this.options = $.extend({}, defaultOptions, options);
    this.$el.addClass("user-list");
    ChatJsUtils.setOuterHeight(this.$el, this.options.height);
    this.options.adapter.client.onUserListChanged(function(userListData) {
      if (_this.options.roomId && userListData.RoomId == _this.options.roomId || _this.options.conversationId && _this.options.conversationId == userListData.ConversationId) {
        var userList = userListData.UserList;
        _this.populateList(userList)
      }
    });
    this.options.adapter.server.getUserList(this.options.roomId, this.options.conversationId, function(userList) {
      _this.populateList(userList)
    })
  }
  UserList.prototype.populateList = function(rawUserList) {
    var _this = this;
    var userList = rawUserList.slice(0);
    if (this.options.excludeCurrentUser) {
      var j = 0;
      while (j < userList.length) {
        if (userList[j].Id == this.options.userId) userList.splice(j, 1);
        else j++
      }
    }
    this.$el.html("");
    if (userList.length == 0) {
      $("<div/>").addClass("user-list-empty").text(this.options.emptyRoomText).appendTo(this.$el)
    } else {
      for (var i = 0; i < userList.length; i++) {
        var $userListItem = $("<div/>").addClass("user-list-item").attr("data-val-id", userList[i].Id).appendTo(this.$el);
        $("<strong/>").addClass("profile-picture").text(userList[i].Name.slice(0, 2)).appendTo($userListItem);
        $("<div/>").addClass("profile-status").addClass(userList[i].Status == 0 ? "offline" : "online").appendTo($userListItem);
        $("<div/>").addClass("content").text(userList[i].Name).appendTo($userListItem);
        (function(userId) {
          $userListItem.click(function() {
            _this.options.userClicked(userId)
          })
        })(userList[i].Id)
      }
    }
  };
  return UserList
}();
$.fn.userList = function(options) {
  if (this.length) {
    this.each(function() {
      var data = new UserList($(this), options);
      $(this).data("userList", data)
    })
  }
  return this
};
var PmWindowInfo = function() {
  function PmWindowInfo() {}
  return PmWindowInfo
}();
var PmWindowState = function() {
  function PmWindowState() {}
  return PmWindowState
}();
var ChatPmWindowOptions = function() {
  function ChatPmWindowOptions() {}
  return ChatPmWindowOptions
}();
var ChatPmWindow = function() {
  function ChatPmWindow(options) {
    var _this = this;
    var defaultOptions = new ChatPmWindowOptions;
    defaultOptions.typingText = " is typing...";
    defaultOptions.isMaximized = true;
    defaultOptions.onCreated = function() {};
    defaultOptions.onClose = function() {};
    defaultOptions.chatJsContentPath = "/chatjs/";
    this.options = $.extend({}, defaultOptions, options);
    this.options.adapter.server.getUserInfo(this.options.otherUserId, function(userInfo) {
      var chatWindowOptions = new ChatWindowOptions;
      chatWindowOptions.title = userInfo.Name;
      chatWindowOptions.canClose = true;
      chatWindowOptions.isMaximized = _this.options.isMaximized;
      chatWindowOptions.onCreated = function(window) {
        var messageBoardOptions = new MessageBoardOptions;
        messageBoardOptions.adapter = _this.options.adapter;
        messageBoardOptions.userId = _this.options.userId;
        messageBoardOptions.height = 235;
        messageBoardOptions.otherUserId = _this.options.otherUserId;
        messageBoardOptions.chatJsContentPath = _this.options.chatJsContentPath;
        window.$windowInnerContent.messageBoard(messageBoardOptions);
        window.$windowInnerContent.addClass("pm-window")
      };
      chatWindowOptions.onClose = function(window) {
        window.$windowInnerContent.unBindOnMessage();
        _this.options.onClose(_this)
      };
      chatWindowOptions.onMaximizedStateChanged = function(chatPmWindow, isMaximized) {
        _this.options.onMaximizedStateChanged(_this, isMaximized)
      };
      _this.chatWindow = $.chatWindow(chatWindowOptions);
      _this.options.onCreated(_this)
    })
  }
  ChatPmWindow.prototype.focus = function() {};
  ChatPmWindow.prototype.setRightOffset = function(offset) {
    this.chatWindow.setRightOffset(offset)
  };
  ChatPmWindow.prototype.getWidth = function() {
    return this.chatWindow.getWidth()
  };
  ChatPmWindow.prototype.getState = function() {
    var state = new PmWindowState;
    state.isMaximized = this.chatWindow.getState();
    state.otherUserId = this.options.otherUserId;
    return state
  };
  ChatPmWindow.prototype.setState = function(state) {
    this.chatWindow.setState(state.isMaximized)
  };
  return ChatPmWindow
}();
$.chatPmWindow = function(options) {
  var pmWindow = new ChatPmWindow(options);
  return pmWindow
};
var ChatFriendsWindowState = function() {
  function ChatFriendsWindowState() {}
  return ChatFriendsWindowState
}();
var ChatFriendsWindowOptions = function() {
  function ChatFriendsWindowOptions() {}
  return ChatFriendsWindowOptions
}();
var ChatFriendsWindow = function() {
  function ChatFriendsWindow(options) {
    var _this = this;
    var defaultOptions = new ChatFriendsWindowOptions;
    defaultOptions.titleText = "Friends";
    defaultOptions.isMaximized = true;
    defaultOptions.offsetRight = 10;
    defaultOptions.emptyRoomText = "No users available for chatting.";
    this.options = $.extend({}, defaultOptions, options);
    this.options.adapter.server.enterRoom(this.options.roomId, function() {});
    var chatWindowOptions = new ChatWindowOptions;
    chatWindowOptions.title = this.options.titleText;
    chatWindowOptions.canClose = false;
    chatWindowOptions.height = 300;
    chatWindowOptions.isMaximized = this.options.isMaximized;
    chatWindowOptions.onMaximizedStateChanged = function(chatWindow, isMaximized) {
      _this.options.onStateChanged(isMaximized)
    };
    chatWindowOptions.onCreated = function(window) {
      var userListOptions = new UserListOptions;
      userListOptions.adapter = _this.options.adapter;
      userListOptions.roomId = _this.options.roomId;
      userListOptions.userId = _this.options.userId;
      userListOptions.height = _this.options.contentHeight;
      userListOptions.excludeCurrentUser = true;
      userListOptions.emptyRoomText = _this.options.emptyRoomText;
      userListOptions.userClicked = _this.options.userClicked;
      window.$windowInnerContent.userList(userListOptions)
    };
    this.chatWindow = $.chatWindow(chatWindowOptions);
    this.chatWindow.setRightOffset(this.options.offsetRight)
  }
  ChatFriendsWindow.prototype.focus = function() {};
  ChatFriendsWindow.prototype.setRightOffset = function(offset) {
    this.chatWindow.setRightOffset(offset)
  };
  ChatFriendsWindow.prototype.getWidth = function() {
    return this.chatWindow.getWidth()
  };
  ChatFriendsWindow.prototype.getState = function() {
    var state = new ChatFriendsWindowState;
    state.isMaximized = this.chatWindow.getState();
    return state
  };
  ChatFriendsWindow.prototype.setState = function(state) {
    this.chatWindow.setState(state.isMaximized)
  };
  return ChatFriendsWindow
}();
$.chatFriendsWindow = function(options) {
  var friendsWindow = new ChatFriendsWindow(options);
  return friendsWindow
};
var ChatControllerOptions = function() {
  function ChatControllerOptions() {}
  return ChatControllerOptions
}();
var ChatJsState = function() {
  function ChatJsState() {
    this.pmWindows = [];
    this.mainWindowState = new ChatFriendsWindowState
  }
  return ChatJsState
}();
var ChatController = function() {
  function ChatController(options) {
    var _this = this;
    var defaultOptions = new ChatControllerOptions;
    defaultOptions.roomId = null;
    defaultOptions.friendsTitleText = "Volunteers";
    defaultOptions.availableRoomsText = "Available rooms";
    defaultOptions.typingText = " is typing...";
    defaultOptions.offsetRight = 10;
    defaultOptions.windowsSpacing = 5;
    defaultOptions.enableSound = true;
    defaultOptions.persistenceMode = "cookie";
    defaultOptions.persistenceCookieName = "chatjs";
    defaultOptions.chatJsContentPath = "/chatjs/";
    this.options = $.extend({}, defaultOptions, options);
    if (!this.options.roomId) throw "Room id option is required";
    this.pmWindows = [];
    this.options.adapter.init(function() {
      var state = _this.getState();
      _this.options.adapter.client.onMessagesChanged(function(message) {
        if (message.UserToId && message.UserToId == _this.options.userId && !_this.findPmWindowByOtherUserId(message.UserFromId)) {
          _this.createPmWindow(message.UserFromId, true, true)
        }
      });
      var friendsWindowOptions = new ChatFriendsWindowOptions;
      friendsWindowOptions.roomId = _this.options.roomId;
      friendsWindowOptions.adapter = _this.options.adapter;
      friendsWindowOptions.userId = _this.options.userId;
      friendsWindowOptions.offsetRight = _this.options.offsetRight;
      friendsWindowOptions.titleText = _this.options.friendsTitleText;
      friendsWindowOptions.isMaximized = state ? state.mainWindowState.isMaximized : true;
      friendsWindowOptions.onStateChanged = function() {
        _this.saveState()
      };
      friendsWindowOptions.userClicked = function(userId) {
        if (userId != _this.options.userId) {
          var existingPmWindow = _this.findPmWindowByOtherUserId(userId);
          if (existingPmWindow) existingPmWindow.focus();
          else _this.createPmWindow(userId, true, true)
        }
      };
      _this.mainWindow = $.chatFriendsWindow(friendsWindowOptions);
      _this.setState(state)
    });
    window.chatJs = this
  }
  ChatController.prototype.createPmWindow = function(otherUserId, isMaximized, saveState) {
    var _this = this;
    var chatPmOptions = new ChatPmWindowOptions;
    chatPmOptions.userId = this.options.userId;
    chatPmOptions.otherUserId = otherUserId;
    chatPmOptions.adapter = this.options.adapter;
    chatPmOptions.typingText = this.options.typingText;
    chatPmOptions.isMaximized = isMaximized;
    chatPmOptions.chatJsContentPath = this.options.chatJsContentPath;
    chatPmOptions.onCreated = function(pmWindow) {
      _this.pmWindows.push({
        otherUserId: otherUserId,
        conversationId: null,
        pmWindow: pmWindow
      });
      _this.organizePmWindows();
      if (saveState) _this.saveState()
    };
    chatPmOptions.onClose = function() {
      for (var i = 0; i < _this.pmWindows.length; i++)
        if (_this.pmWindows[i].otherUserId == otherUserId) {
          _this.pmWindows.splice(i, 1);
          _this.saveState();
          _this.organizePmWindows();
          break
        }
    };
    chatPmOptions.onMaximizedStateChanged = function() {
      _this.saveState()
    };
    return $.chatPmWindow(chatPmOptions)
  };
  ChatController.prototype.saveState = function() {
    var state = new ChatJsState;
    for (var i = 0; i < this.pmWindows.length; i++) {
      state.pmWindows.push({
        otherUserId: this.pmWindows[i].otherUserId,
        conversationId: null,
        isMaximized: this.pmWindows[i].pmWindow.getState().isMaximized
      })
    }
    state.mainWindowState = this.mainWindow.getState();
    switch (this.options.persistenceMode) {
      case "cookie":
        this.createCookie(this.options.persistenceCookieName, state);
        break;
      case "server":
        throw "Server persistence is not supported yet";
      default:
        throw "Invalid persistence mode. Available modes are: cookie and server"
    }
    return state
  };
  ChatController.prototype.getState = function() {
    var state;
    switch (this.options.persistenceMode) {
      case "cookie":
        state = this.readCookie(this.options.persistenceCookieName);
        break;
      case "server":
        throw "Server persistence is not supported yet";
      default:
        throw "Invalid persistence mode. Available modes are: cookie and server"
    }
    return state
  };
  ChatController.prototype.setState = function(state) {
    if (typeof state === "undefined") {
      state = null
    }
    if (!state) state = this.getState();
    if (!state) return;
    for (var i = 0; i < state.pmWindows.length; i++) {
      var shouldCreatePmWindow = true;
      if (this.pmWindows.length) {
        for (var j = 0; j < this.pmWindows.length; j++) {
          if (state.pmWindows[i].otherUserId && this.pmWindows[j].otherUserId == state.pmWindows[j].otherUserId) {
            shouldCreatePmWindow = false;
            break
          }
        }
      }
      if (shouldCreatePmWindow) this.createPmWindow(state.pmWindows[i].otherUserId, state.pmWindows[i].isMaximized, false)
    }
    this.mainWindow.setState(state.mainWindowState, false)
  };
  ChatController.prototype.eraseCookie = function(name) {
    this.createCookie(name, "", -1)
  };
  ChatController.prototype.readCookie = function(name) {
    var nameEq = name + "=";
    var ca = document.cookie.split(";");
    var cookieValue;
    for (var i = 0; i < ca.length; i++) {
      var c = ca[i];
      while (c.charAt(0) == " ") c = c.substring(1, c.length);
      if (c.indexOf(nameEq) == 0) {
        cookieValue = c.substring(nameEq.length, c.length)
      }
    }
    if (cookieValue) {
      try {
        return JSON.parse(cookieValue)
      } catch (e) {
        return cookieValue
      }
    } else return null
  };
  ChatController.prototype.createCookie = function(name, value, days) {
    var stringedValue;
    if (typeof value == "string") stringedValue = value;
    else stringedValue = JSON.stringify(value);
    if (value) var expires;
    if (days) {
      var date = new Date;
      date.setTime(date.getTime() + days * 24 * 60 * 60 * 1e3);
      expires = "; expires=" + date.toUTCString()
    } else {
      expires = ""
    }
    document.cookie = name + "=" + stringedValue + expires + "; path=/"
  };
  ChatController.prototype.findPmWindowByOtherUserId = function(otherUserId) {
    for (var i = 0; i < this.pmWindows.length; i++)
      if (this.pmWindows[i].otherUserId == otherUserId) return this.pmWindows[i].pmWindow;
    return null
  };
  ChatController.prototype.organizePmWindows = function() {
    var rightOffset = +this.options.offsetRight + this.mainWindow.getWidth() + this.options.windowsSpacing;
    for (var i = 0; i < this.pmWindows.length; i++) {
      this.pmWindows[i].pmWindow.setRightOffset(rightOffset);
      rightOffset += this.pmWindows[i].pmWindow.getWidth() + this.options.windowsSpacing
    }
  };
  return ChatController
}();
$.chat = function(options) {
  var chat = new ChatController(options);
  return chat
};
