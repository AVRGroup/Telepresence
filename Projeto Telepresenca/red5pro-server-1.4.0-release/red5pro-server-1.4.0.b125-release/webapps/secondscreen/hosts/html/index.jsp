<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.net.Inet4Address"%>
<%@ page import="com.red5pro.server.secondscreen.net.NetworkUtil"%>
<%@ page import="java.io.*,java.util.regex.*,java.net.URL,java.nio.charset.Charset"%>
<%
  String cookieStr = "";
  String cookieName = "storedIpAddress";
  Pattern addressPattern = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");

  String ip = null;
  String localIp = NetworkUtil.getLocalIpAddress();
  boolean ipExists = false;

  // Flip localIp to null if unknown.
  localIp = addressPattern.matcher(localIp).find() ? localIp : null;

  // First check if provided as a query parameter...
  if(request.getParameter("host") != null) {
    ip = request.getParameter("host");
    ip = addressPattern.matcher(ip).find() ? ip : null;
  }

  Cookie cookie;
  Cookie[] cookies = request.getCookies();

  // If we have stored cookies check if already stored ip address by User.
  if(ip == null && cookies != null) {
    for(int i = 0; i < cookies.length; i++) {
      cookie = cookies[i];
      cookieStr += cookie.getName() + "=" + cookie.getValue() + "; ";
      if(cookie.getName().equals(cookieName)) {
        ip = cookie.getValue();
        ip = addressPattern.matcher(ip).find() ? ip : null;
        break;
      }
    }
  }

  // Is a valid IP address from stored IP by User:
  if(ip == null) {

    ip = localIp;

    if(ip != null && addressPattern.matcher(ip).find()) {
      // The IP returned from NetworkUtils is valid...
    }
    else {

      // Invoke AWS service
      URL whatismyip = new URL("http://checkip.amazonaws.com");
      BufferedReader in = null;
      try {
        in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        ip = in.readLine();
      }
      catch(Exception e) {
        ip = null;
      }
      finally {
        if (in != null) {
          try {
            in.close();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

      // If failure in AWS service and/or IP still null => flag to show alert.
    }

  }

  ipExists = ip != null && !ip.isEmpty();
%>
<!doctype html>
<html lang="eng">
  <head>
    <meta http-equiv="cache-control" content="no-cache">
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Welcome to the Red5 Pro Server Pages!">
    <link rel="stylesheet" href="../../css/main.css">
    <link href="http://fonts.googleapis.com/css?family=Lato:400,700" rel="stylesheet" type="text/css">
    <title>Second Screen HTML Controller Example with the Red5 Pro Server!</title>
    <link rel="stylesheet" type="text/css" href="style/main.css">
    <script src="lib/host/secondscreen-host.min.js"></script>
  </head>
  <body>
    <div class="header-bar clear-fix">
      <div id="header-field" class="clear-fix">
        <p class="left"><a class="red5pro-header-link" href="/">
            <img class="red5pro-logo-header" src="../../images/red5pro_logo.svg">
        </a></p>
      </div>
      <!-- view jsp_header for details on variables used. -->
      
      <!-- Display IP Address used through the page -->
      <div id="ip-field">
        <% if (ipExists) { %>
          <p><span class="black-text">Your server IP address is:</span>&nbsp;&nbsp;<span id="ip-address-field" class="red-text medium-font-size"><%= ip %></span></p>
        <% } else { %>
          <p><span class="black-text">Uh-Oh!!</span></p>
        <% } %>
        <p><a id="ip-overlay-button" class="white-text" href="#">Why would I need to know the server IP address?</a></p>
        <p><a id="ip-incorrect-button" class="black-text" href="#">Not the correct IP address?</a></p>
      </div>
      
      <!-- Overlay explaining what the IP Address is used for. -->
      <div id="ip-overlay" class="hidden">
        <p class="overlay-close-button"><a id="ip-overlay-close" href="#" class="red-text">Close</a></p>
        <p>This IP address needs to be provided to applications integrated with the Red5 Pro SDKs.</p>
        <p>If using the example projects from our <a id="header-github-link" class="link" href="http://github.com/red5pro" target="_blank">Github</a>, you will enter this IP into the <strong>Server</strong> input field of the Settings menu.</p>
        <hr>
        <p class="top-nudge"><a id="ip-overlay-ip-incorrect-button" href="#" class="red-text">Not the correct IP address?</a></p>
      </div>
      
      <!-- Overlay to allow User to update IP Address to be used. -->
      <div id="ip-address-overlay" class="hidden">
        <p class="overlay-close-button"><a id="ip-overlay-close" href="#" class="red-text">Close</a></p>
        <p class="black-text" style="font-weight: bold">Do you think the server IP address above is incorrect?</p>
        <div>
          <p>Select from the following suggestions:</p>
          <table id="ip-suggestions-table">
            <tbody><tr><td class="red-text">No suggestions.</tr></td>
          </table>
        </div>
        <hr>
        <p class="black-text"> Or enter in the correct address below:</p>
        <p id="ip-input-error-field" class="hidden red-text">Invalid IP address.</p>
        <input id="ip-address-input-field" type="text">
        <button id="ip-address-input-submit">submit</button>
      </div>
      <script>
      (function(window, document) {
        'use strict';
      
        // Host IP state
        var currentIp = "<%= ip %>";
        var hasValidIp = <%= ipExists %>;
        var suggestHostIp = window.location.hostname;
        var ipAddressField = document.getElementById('ip-address-field');
        var validIpRegex = /^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$/gi;
      
        // IP Overlay
        var isOverlayShown = false;
        var isIpAddressOverlayShown = false;
      
        // Element References
        var ipOverlay = document.getElementById('ip-overlay');
        var ipAddressOverlay = document.getElementById('ip-address-overlay');
        var ipOverlayButton = document.getElementById('ip-overlay-button');
        var ipIncorrectButton = document.getElementById('ip-incorrect-button');
        var ipOverlayIpIncorrectButton = document.getElementById('ip-overlay-ip-incorrect-button');
        var githubLink = document.getElementById('header-github-link');
        var ipAddressInput = document.getElementById('ip-address-input-field');
        var ipAddressInputSubmit = document.getElementById('ip-address-input-submit');
        var ipAddressErrorField = document.getElementById('ip-input-error-field');
        var ipSuggestionsTable = document.getElementById('ip-suggestions-table');
      
        // Listeners to Change in IP
        var ipChangeListeners = [];
        var registerIpChangeListener = function(func, immediatelyInvoke) {
          if(immediatelyInvoke) {
            func.call(null, currentIp);
          }
          ipChangeListeners.push(func);
        };
        var unregisterIpChangeListener = function(func) {
          var i = ipChangeListeners.length;
          while(--i > -1) {
            if(ipChangeListeners[i] === func) {
              ipChangeListeners.splice(i, 1);
              break;
            }
          }
        };
        var notifyIpChangeListeners = function(newValue) {
          var i = ipChangeListeners.length;
          while(--i > -1) {
            ipChangeListeners[i].call(null, newValue);
          }
        };
        window.r5pro_registerIpChangeListener = registerIpChangeListener;
        window.r5pro_unregisterIpChangeListener = unregisterIpChangeListener;
      
        var normalizeIp = function(value) {
          var isValid = value !== null;
          isValid = isValid && value !== undefined;
          isValid = isValid && value !== "null";
          isValid = isValid && value !== "undefined";
          return isValid ? value : null;
        };
        var fillInIpSuggestions = function(currentIp) {
          var items = [];
          var localIp = normalizeIp("<%= localIp %>");
          var addIpToList = function(value) {
            if(value !== null && currentIp !== value) {
            items.push('<tr>' +
                '<td>' +
                  '<a class="red-text ip-suggestion-link" href="#">' +
                    value +
                  '</a>' +
                '</td>' +
              '</tr>');
            }
          };
          addIpToList(localIp);
          addIpToList(suggestHostIp);
          if(items.length > 0) {
            ipSuggestionsTable.innerHTML = items.join('');
          }
        }
        var updateAndStoreUserEnteredIpAddress = function(value) {
          var expiry = 60*60*24;
          if(ipAddressField.hasOwnProperty('innerText')) {
            ipAddressField.innerText = value;
          }
          else {
            ipAddressField.textContent = value;
          }
          document.cookie = '<%= cookieName %>=' + value + '; path=/; max-age=' + expiry;
          currentIp = value;
          notifyIpChangeListeners(value);
        };
      
        var toggleOverlay = function(event) {
          event.preventDefault();
          event.stopPropagation();
          if(!isOverlayShown) {
            showOverlay();
          }
          else {
            hideOverlay();
          }
        };
        var showOverlay = function() {
          isOverlayShown = true;
          if(isIpAddressOverlayShown) {
             hideIpAddressOverlay();
          }
          ipOverlay.classList.remove('hidden');
        };
        var hideOverlay = function() {
          isOverlayShown = false;
          ipOverlay.classList.add('hidden');
        };
        var handleOverlayClose = function(event) {
          if(event.target !== githubLink &&
              event.target !== ipOverlayIpIncorrectButton) {
            event.stopPropagation();
            event.preventDefault();
            hideOverlay();
            return false;
          }
          else if(event.target === ipOverlayIpIncorrectButton) {
            toggleIpAddressOverlay(event);
          }
          return true;
        };
      
        var toggleIpAddressOverlay = function(event) {
          event.stopPropagation();
          event.preventDefault();
          if(!isIpAddressOverlayShown) {
            showIpAddressOverlay();
          }
          else {
            hideIpAddressOverlay();
          }
          return false;
        };
        var showIpAddressOverlay = function() {
          isIpAddressOverlayShown = true;
          if(isOverlayShown) {
            hideOverlay();
          }
          ipAddressOverlay.classList.remove('hidden');
        };
        var hideIpAddressOverlay = function() {
          isIpAddressOverlayShown = false;
          ipAddressOverlay.classList.add('hidden');
          ipAddressErrorField.classList.add('hidden');
        };
        var handleIpAddressOverlayClose = function(event) {
          if(event.target !== ipAddressInput &&
              event.target !== ipAddressInputSubmit &&
              !event.target.classList.contains('ip-suggestion-link')) {
              event.preventDefault();
              event.stopPropagation();
              hideIpAddressOverlay();
              return false;
          }
          else if(event.target.classList.contains('ip-suggestion-link')) {
            var value;
            event.preventDefault();
            event.stopPropagation();
            hideIpAddressOverlay();
            if(event.target.hasOwnProperty('innerText')) {
              value = event.target.innerText;
            }
            else {
              value = event.target.textContent;
            }
            updateAndStoreUserEnteredIpAddress(value);
            return false;
          }
          return true;
        };
        var handleIpAddressInputSubmit = function(event) {
          var value = ipAddressInput.value;
          event.stopPropagation();
          event.preventDefault();
          ipAddressErrorField.classList.add('hidden');
          // Removing Regex check for now.
      //      if(validIpRegex.test(value)) {
            updateAndStoreUserEnteredIpAddress(value);
            hideIpAddressOverlay();
      //      }
      //      else {
      //        ipAddressErrorField.classList.remove('hidden');
      //      }
          return false;
        };
        ipOverlayButton.addEventListener('click', toggleOverlay);
        ipOverlayButton.addEventListener('mouseover', showOverlay);
        ipOverlay.addEventListener('mousedown', handleOverlayClose);
        ipOverlay.addEventListener('touchstart', handleOverlayClose);
      
        ipIncorrectButton.addEventListener('click', toggleIpAddressOverlay);
        ipAddressInput.addEventListener('keyup', function(event) {
          if(event.keyCode === 13) {
            handleIpAddressInputSubmit(event);
          }
        });
        ipAddressInputSubmit.addEventListener('click', handleIpAddressInputSubmit);
        ipAddressOverlay.addEventListener('mousedown', handleIpAddressOverlayClose);
        ipAddressOverlay.addEventListener('touchstart', handleIpAddressOverlayClose);
      
        registerIpChangeListener(fillInIpSuggestions, true);
      
      }(this, document));
      
      </script>
      
    </div>
    <div id="server-version-field" class="small-font-size">
      <span class="black-text">Red5 Pro Server Version</span>&nbsp;&nbsp;<span class="red-text">1.4.0.b125-release</span>
    </div>
    <div class="container main-container clear-fix">
      <div id="menu-section">
        <!-- view jsp_header to view variables used in this page -->
        <div class="menu-content">
          <ul class="menu-listing">
            <li><a class="red-text menu-listing-internal" href="/">Welcome</a></li>
            <li><a class="red-text menu-listing-internal" href="/live">Live Streaming</a></li>
              <ul class="menu-sublisting">
                <li><a class="menu-broadcast-link red-text menu-listing-nested" href="/live/broadcast.jsp?host=<%= ip %>">Broadcast</a></li>
                <li><a class="red-text menu-listing-nested" href="/live/subscribe.jsp">Subscribe</a></li>
                <li><a class="red-text menu-listing-nested" href="/live/playback.jsp">VOD Playback</a></li>
        <!--         <li><a class="red-text menu-listing-nested" href="/live/twoway.jsp">Two-Way Streaming Example</a></li> -->
              </ul>
            <li><a class="red-text menu-listing-internal" href="/secondscreen">Second Screen</a></li>
              <ul class="menu-sublisting">
                  <li><a class="red-text menu-listing-nested" href="/secondscreen/hosts/html">HTML Controller</a></li>
                  <li><a class="red-text menu-listing-nested" href="/secondscreen/hosts/gamepad">Gamepad Controller</a></li>
                  <li><a class="red-text menu-listing-nested" href="/secondscreen/hosts/dpad">DPAD Controller</a></li>
              </ul>
            <li><a class="red-text menu-listing-internal" href="/streammanager">Stream Manager</a></li><li><a class="red-text menu-listing-internal" href="/api"></a></li>
          </ul>
          <ul class="menu-listing">
            <li><a class="red-text menu-listing-external" href="http://red5pro.com" target="_blank">&gt;&nbsp;Red5 Pro Site</a></li>
            <li><a class="red-text menu-listing-external" href="http://red5pro.com/docs" target="_blank">&gt;&nbsp;Red5 Pro Docs</a></li>
            <li><a class="red-text menu-listing-external" href="http://account.red5pro.com" target="_blank">&gt;&nbsp;Red5 Pro Accounts</a></li>
            <li><a class="red-text menu-listing-external" href="http://github.com/red5pro" target="_blank">&gt;&nbsp;Red5 Pro Github</a></li>
          </ul>
        <hr>
          <ul class="menu-listing">
            <li><a class="red-text menu-listing-external" href="https://red5pro.zendesk.com?origin=webapps" target="_blank">Looking For Help?</a></li>
          </ul>
          <script>
            (function(window, document) {
              var className = 'menu-broadcast-link';
              function handleLiveIpChange(value) {
                var elements = document.getElementsByClassName(className);
                var length = elements ? elements.length : 0;
                var index = 0;
                for(index = 0; index < length; index++) {
                  elements[index].href = ['/live/broadcast.jsp?host', value].join('=');
                }
              }
              window.r5pro_registerIpChangeListener(handleLiveIpChange);
             }(this, document));
          </script>
        </div>
      </div>
      <div id="content-section">
        <div>
          <div class="clear-fix">
            <p class="left">
                <a class="red5pro-header-link" href="/">
                  <img class="red5pro-logo-page" src="../../images/red5pro_logo.svg">
               </a>
            </p>
          </div>
          <h2 class="tag-line">SECOND SCREEN HTML CONTROLLER</h2>
        </div>
        <div>
          <div>
            <p>Once the color chip below is no longer black, this page has become a <span class="red-text">Second Screen Host</span>!</p>
            <p>To connect and communicate with this page, open a native application integrated with the <a class="link" href="http://red5pro.com/docs/streaming/overview/" target="_blank">Second Screen SDKs</a> on your favorite device to turn it into a <span class="red-text">Second Screen Client</span>!</p>
          </div>
          <div id="secondscreen-example-container">
            <div id="version-field">Red5 Pro Second Screen Not loaded.</div>
            <div id="slot"></div>
            <p id="sendForm">
              <input type="text" id="sendInput" value="Hello, World!"></input>
              <button id="sendButton" onclick="window.notifyClient('Host Message', window.document.getElementById('sendInput').value);">Send Message!</button>
            </p>
          </div>
          <script>
            (function(window, document) {
              var host;
              var checkId;
              var checkForHost = function() {
                clearTimeout(checkId);
                if(typeof window.secondscreenHost !== 'undefined') {
                  host = window.secondscreenHost.noConflict();
                  document.getElementById('version-field').innerText = host.versionStr();
                }
                else {
                  checkId = setTimeout(checkForHost, 100);
                }
              }
              checkForHost();
             }(this, document));
          </script>
          <div style="width: 100%; text-align: right;">
            <p><a class="red-text link" href="../downloads/html.zip">Download</a> this example.</p>
          </div>
          <div>
            <p>This example demonstrates how the <span class="red-text">Second Screen Host</span> can pass controller displays written using the webstack of HTML/CSS/JS to the <span class="red-text">Second Screen Client</span> running on a mobile device! In fact, you can visit the actual HTML page being delivered <a class="red-text link" href="scheme/basic-scheme.html" target="_blank">here</a>.</p>
            <p class="medium-font-size">Connect more devices and have some fun!</p>
          </div>
        </div>
        <hr class="top-padded-rule" />
        <h3><a class="link" href="http://red5pro.com/docs/streaming/overview/" target="_blank">Second Screen SDKs</a></h3>
        <p>You can download the Red5 Pro Second Screen SDKs from your <a class="link" href="http://account.red5pro.com/download" target="_blank">Red5 Pro Accounts</a> page.</p>
        <p>Please visit the online <a class="link" href="http://red5pro.com/docs/secondscreen/overview/" target="_blank">Red5 Pro Documentation</a> for further information about integrating the Second Screen SDKs into your own native application!</p>
        <hr class="top-padded-rule" />
        <div>
          <h3>Example Native Applications</h3>
          <h4>You will need a native application integrated with the Red5 Pro SDKs installed on your favorite device in order to broadcast and consume live streams and experience Red5 Pro Second Screen.</h4>
          <p>You can find the following Open Source native application examples on our <a class="link" href="https://github.com/red5pro" target="_blank">Github</a>:</p>
          <div class="menu-content">
            <ul class="menu-listing application-listing">
              <li><a class="red-text" href="https://github.com/red5pro/red5pro-ios-app" target="_blank">&gt;&nbsp;Red5 Pro iOS Application</a></li>
              <li><a class="red-text" href="https://github.com/red5pro/red5pro-android-app" target="_blank">&gt;&nbsp;Red5 Pro Android Application</a></li>
            </ul>
          </div>
          <p>Follow the project setup and build instructions in each project to easily create Red5 Pro native clients to begin using the above server applications!</p>
        </div>
        
        <hr class="top-padded-rule" />
        <div>
          <h3>API Documentation</h3>
          <p>To find more in-depth information about Red5 Pro Server and the server and mobile SDKs, please visit <a class="link" href="http://red5pro.com/docs/">http://red5pro.com/docs/</a>.</p>
        </div>
        
        </div>
      </div>
    </div>
    <script>
      (function(window) { 

              var timestamp = new Date().getTime();
              var secondscreenHost = window.secondscreenHost.noConflict();
              var config = {
                name: 'HTML Control Basic',
                maxPlayers: 10,
                registryAddress: '<%= ip %>',
                swfobjectUrl: 'lib/host/swf/swfobject.js',
                swfUrl: "lib/host/swf/secondscreenHost.swf",
                minimumVersion: {
                  major: 0,
                  minor: 0
                },
                controlsUrl: 'scheme/basic-scheme.html?' + timestamp,
                controlMode: secondscreenHost.ControlModes.HTML,
                error: function(error) {
                  secondscreenHost.log.error('Registry connection error: ' + error);
                },
                success: function() {
                  secondscreenHost.log.info('Registry connected.');
                }
              };
              secondscreenHost.setLogLevel(secondscreenHost.LogLevels.INFO);
              secondscreenHost.start(config);

              secondscreenHost.on(secondscreenHost.EventTypes.SHOW_SLOT_COLOR, function(e) {
                var slot = document.getElementById('slot');
                slot.style.background = e.color;
              });

              secondscreenHost.on(secondscreenHost.EventTypes.DEVICE_CONNECTED, function (e) {
                window.notifyClient('Host Welcome', 'Welcome!');
                print("<p class=\"red-text medium-font-size\">Device Connected (id, name):<br>&nbsp;&nbsp;" + e.device.id + ', ' + e.device.name + "</p>");
              });

              secondscreenHost.on(secondscreenHost.EventTypes.DEVICE_DISCONNECTED, function (e) {
                print("<p class=\"red-text medium-font-size\">Device Disconnected (id, name):<br>&nbsp;&nbsp;" + e.device.id + ', ' + e.device.name + "</p>");
              });

              secondscreenHost.on(secondscreenHost.EventTypes.CONTROLS_URL_CHANGE, function(e) {
                print('Controls url will change: ' + e.url);
              });

              secondscreenHost.on(secondscreenHost.EventTypes.MESSAGE, function(e) {
                var deviceId = e.device.id,
                    message = e.message,
                    state = e.state;

                if(message === 'ready') {
                  e.device.send("test");
                  e.device.send("testing", 1, 2, 3);
                  e.device.send("testing", 1.126, "52 skidoo", [1, 2, 3]);
                }

                if(state !== undefined) {
                  var log = 'Event.Message: ' +
                            '(' + message + ') ' +
                            '<br>&nbsp;&nbsp;From: ' + deviceId +
                            '<br>&nbsp;&nbsp;Message: ' + JSON.stringify(state,null,2);
                  print(log);
                }

              });

              function print(message) {
                var p = document.createElement('p');
                p.innerHTML = message;
                document.getElementById('secondscreen-example-container').appendChild(p);
               secondscreenHost.log.info(message);
              }

              window.notifyClient = function(type, message) {
                secondscreenHost.allDevices().send('state', {
                  state: type,
                  message: message
                });
              };

              function handleSecondScreenHostIpChange(value) {
                var slot = document.getElementById('slot');
                secondscreenHost.stop();
                slot.style.backgroundColor = "#000";
                config.registryAddress = value;
                secondscreenHost.start(config);
              }
              window.r5pro_registerIpChangeListener(handleSecondScreenHostIpChange);

      }(window));
    </script>
   <div class="footer">
     <div>
       <a id="footer-email" href="https://red5pro.zendesk.com?origin=webapps" target="_blank">
         <span class="red-text email-header">Looking for something else?</span>
         <br>
         <br>
         <span id="footer-email-lead" class="black-text">We're here to help!</span>      <span class="red-text">Get in touch</span>
       </a>
     </div>
     <br>
     <hr>
     <p class="footer-copyright">
       <span>Copyright &copy; 2015,</span>&nbsp;
           <img class="red5pro-logo-footer" src="../../images/red5pro_logo.svg">
     </p>
   </div>
  </body>
</html>