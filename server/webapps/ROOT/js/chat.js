// Global chat object
var Chat = {};

Chat.connect = (function(host) {
  // Check if we have WebSocket support
  if ('WebSocket' in window) {
    Chat.socket = new WebSocket(host);
  } else if ('MozWebSocket' in window) {
    Chat.socket = new MozWebSocket(host);
  } else {
    Console.log('Error: WebSocket is not supported by this browser.');
    return;
  }

  // Bind on open function
  Chat.socket.onopen = function () {
    console.log('Info: WebSocket connection opened.');
  };

  // Bind on close function
  Chat.socket.onclose = function () {
    console.log('Info: WebSocket closed.');
  };

  // Main message handler
  Chat.socket.onmessage = function (e) {
    // Parse the data, we are expecting a ChatMessage
    // having fields: -type, -destinationIds, -sourceId
    // -date, -storeOffline, -payload
    var msg = JSON.parse(e.data);
    console.log(msg);
  };
});
