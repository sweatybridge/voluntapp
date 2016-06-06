// Document ready
$(function() {
  // Bind the chat commands
  // _this refers to the messageboard that contains the text box etc.
  app.commands = [
    { command: "/help", action: showHelpMessage, helpMessage: "displays this help message" },
    { command: "/clear", action: function(_this) { _this.$messagesWrapper.empty(); }, helpMessage: "clears the message box" },
    { command: "/weekend", action: function(_this) { $("#b_hide_weekend").click(); }, helpMessage: "toggle weekend" },
    { command: "/next", action: function(_this) { $("#next_day").click(); }, helpMessage: "show next week" },
    { command: "/prev", action: function(_this) { $("#prev_day").click(); }, helpMessage: "show previous week" }
    ];
}); // End of document ready

// Given a message board displays a message as if it came from the user
function displayMessage(_this, message) {
  var msg = { UserFromId: _this.options.userId,  Message: message };
    _this.addMessage(msg);
}

// Given the message board, dumps the commands available to the user
function showHelpMessage(_this) {
  displayMessage(_this, "Avaliable commands are:");
  $.each(app.commands, function (i, command) {
    displayMessage(_this, command.command + " - " + command.helpMessage);
  });
}

// Handles message board functions
function handleMessage(_this) {
  // _this is the message itself
  var text = _this.$textBox.val();
  // Check if it is a command
  if (text.charAt(0) == '/') {
    // We got a command, find which one it is
    var found = $.grep(app.commands, function(c) { return c.command == text; });
    if (found.length == 1) {
      found[0].action(_this); // execute command;
    } else {
      // Show invalid command message
      displayMessage(_this, "Invalid command, type /help");
    }
  } else {
    // Not a command just send the message
    _this.sendMessage(text);
  }
  // Clear the textbox
  _this.$textBox.val("").trigger("autosize.resize");
}

/**
 * Notification client used for chat and real time updates. Requires web sockets.
 */
var NotificationClientAdapter = (function() {
  function NotificationClientAdapter() {
    this.messagesChangedHandlers = [];
    this.typingSignalReceivedHandlers = [];
    this.userListChangedHandlers = [];
  }
  // adds a handler to the messagesChanged event
  NotificationClientAdapter.prototype.onMessagesChanged = function(handler) {
    this.messagesChangedHandlers.push(handler);
  };

  NotificationClientAdapter.prototype.unBindMessagesChanged = function(handler) {
    var i = this.messagesChangedHandlers.indexOf(handler);
    this.messagesChangedHandlers.splice(i, 1);
    // snap friends list back in view
    var chat = $(".chat-window").first();
    var position = parseInt(chat.css("right").slice(0, -2));
    if (position < 0) {
      chat.css({
        right: "10px"
      })
    }
  };
  // adds a handler to the typingSignalReceived event
  NotificationClientAdapter.prototype.onTypingSignalReceived = function(handler) {
    this.typingSignalReceivedHandlers.push(handler);
  };

  // adds a handler to the userListChanged event
  NotificationClientAdapter.prototype.onUserListChanged = function(handler) {
    this.userListChangedHandlers.push(handler);
  };

  NotificationClientAdapter.prototype.triggerMessagesChanged = function(message) {
    for (var i = 0; i < this.messagesChangedHandlers.length; i++)
      this.messagesChangedHandlers[i](message);
  };

  NotificationClientAdapter.prototype.triggerTypingSignalReceived = function(typingSignal) {
    for (var i = 0; i < this.typingSignalReceivedHandlers.length; i++)
      this.typingSignalReceivedHandlers[i](typingSignal);
  };

  NotificationClientAdapter.prototype.triggerUserListChanged = function(userListChangedInfo) {
    for (var i = 0; i < this.userListChangedHandlers.length; i++)
      this.userListChangedHandlers[i](userListChangedInfo);
  };
  return NotificationClientAdapter;
})();

var NotificationServerAdapter = (function() {
  // class variables
  NotificationServerAdapter.DEFAULT_ROOM_ID = 1;
  NotificationServerAdapter.DEFAULT_CONVERSATION_ID = 1;

  function NotificationServerAdapter(clientAdapter, socket) {
    this.clientAdapter = clientAdapter;
    this.socket = socket;
    this.users = [];
    this.rooms = [];
  }

  NotificationServerAdapter.prototype.sendMessage = function(roomId, conversationId, otherUserId, messageText, clientGuid, done) {
    var _this = this;
    //console.log("NotificationServerAdapter: sendMessage");

    // we have to send the current message to the current user first
    // in chatjs, when you send a message to someone, the same message bounces back to the user
    // just so that all browser windows are synchronized
    var bounceMessage = new ChatMessageInfo();
    bounceMessage.UserFromId = app.user.userId; // It will from our user
    bounceMessage.UserToId = otherUserId;
    bounceMessage.RoomId = roomId;
    bounceMessage.ConversationId = conversationId;
    bounceMessage.Message = messageText;
    bounceMessage.ClientGuid = clientGuid;

    /*setTimeout(function() {
      _this.clientAdapter.triggerMessagesChanged(bounceMessage);
    }, 300);*/

    // Create our own ChatMessage that the server is going to route
    var chatMessage = {
      type: "text",
      destinationIds: [otherUserId],
      sourceId: app.user.userId,
      storeOffline: true,
      payload: messageText
    };
    this.socket.send(JSON.stringify(chatMessage));
    _this.clientAdapter.triggerMessagesChanged(bounceMessage);
  };

  NotificationServerAdapter.prototype.sendTypingSignal = function(roomId, conversationId, userToId, done) {
    //console.log("NotificationServerAdapter: sendTypingSignal");
    // Create our own ChatMessage that the server is going to route
    var chatMessage = {
      type: "typing",
      destinationIds: [userToId],
      sourceId: app.user.userId,
      storeOffline: false
    };
    this.socket.send(JSON.stringify(chatMessage));
  };

  NotificationServerAdapter.prototype.getMessageHistory = function(roomId, conversationId, otherUserId, done) {
    //console.log("NotificationServerAdapter: getMessageHistory");
    // We don't support message history yet
    done([]);
  };

  NotificationServerAdapter.prototype.getUserInfo = function(userId, done) {
    //console.log("NotificationServerAdapter: getUserInfo");
    var user = null;
    for (var i = 0; i < this.users.length; i++) {
      if (this.users[i].Id == userId) {
        user = this.users[i];
        break;
      }
    }
    if (user == null)
      throw "User doesn't exit. User id: " + userId;
    done(user);
  };

  NotificationServerAdapter.prototype.getUserList = function(roomId, conversationId, done) {
    //console.log("NotificationServerAdapter: getUserList");
    if (roomId == NotificationServerAdapter.DEFAULT_ROOM_ID) {
      done(this.users);
      return;
    }
    throw "The given room or conversation is not supported by the demo adapter";
  };

  NotificationServerAdapter.prototype.enterRoom = function(roomId, done) {
    //console.log("NotificationServerAdapter: enterRoom");

    if (roomId != NotificationServerAdapter.DEFAULT_ROOM_ID)
      throw "Only the default room is supported in the demo adapter";

    var userListChangedInfo = new ChatUserListChangedInfo();
    userListChangedInfo.RoomId = NotificationServerAdapter.DEFAULT_ROOM_ID;
    userListChangedInfo.UserList = this.users;

    this.clientAdapter.triggerUserListChanged(userListChangedInfo);
  };

  NotificationServerAdapter.prototype.leaveRoom = function(roomId, done) {
    console.log("NotificationServerAdapter: leaveRoom");
  };

  return NotificationServerAdapter;
})();

var NotificationAdapter = (function() {
  function NotificationAdapter() {}
  // called when the adapter is initialized
  NotificationAdapter.prototype.init = function(done) {
    var _this = this;
    // get authorization token
    var cookie = getCookie("token");
    if (!cookie) {
      console.log("No token.");
      return;
    }

    // open websocket connection
    var host = (window.location.protocol === 'http:') ? 'ws://' : 'wss://';
    host += window.location.host + "/chat?token=" + cookie;
    this.socket = new WebSocket(host);

    // build client and server adapters
    this.client = new NotificationClientAdapter();
    this.server = new NotificationServerAdapter(this.client, this.socket);

    // Bind on open function
    _socket = this.socket;
    this.socket.onopen = function() {
      console.log('Info: WebSocket connection opened.');
      setInterval(function () {
        var ping = { type: "ping",
                     destinationIds: [-1],
                     sourceId: app.user.userId }; // No payload needed
        _socket.send(JSON.stringify(ping));
      }, 60000); // Ping every minute to keep connection alive
    };

    // Bind on close function
    this.socket.onclose = function() {
      console.log('Info: WebSocket closed.');
    };

    // Main message handler
    this.socket.onmessage = function(e) {
      // Parse the data, we are expecting a ChatMessage
      // having fields: -type, -destinationIds, -sourceId
      // -date, -storeOffline, -payload
      var msg = JSON.parse(e.data);
      //console.log(msg);
      switch (msg.type) {
        case 'ping':
          // Ping back
          var pong = { type: "pong",
                     destinationIds: [msg.sourceId],
                     sourceId: app.user.userId }; // No payload needed
          _socket.send(JSON.stringify(pong));
          break;
        case 'roster':
          _this.handleRoster(msg.payload.roster);
          break;
        case 'text':
          _this.handleText(msg.sourceId, msg.payload);
          break;
        case 'typing':
          _this.handleTyping(msg.sourceId);
          break;
        case 'online/user':
          _this.handleOnline(msg.payload.userId);
          break;
        case 'offline/user':
          _this.handleOffline(msg.payload.userId);
          break;
        case 'join/event':
          _this.handleJoinEvent(msg.payload);
          break;
        case 'unjoin/event':
          _this.handleUnjoinEvent(msg.payload);
          break;
        case 'update/event':
          _this.handleUpdateEvent(msg.payload);
          break;
        case 'delete/event':
          _this.handleDeleteEvent(msg.payload);
          break;
        case 'join/calendar':
          _this.handleJoinCalendar(msg.payload);
          break;
        case 'unjoin/calendar':
          _this.handleUnjoinCalendar(msg.payload);
          break;
        case 'update/calendar':
          _this.handleUpdateCalendar(msg.payload);
          break;
        case 'delete/calendar':
          _this.handleDeleteCalendar(msg.payload);
          break;
      }
      //toastr.info(e.data);
    };
    done();
  };

  NotificationAdapter.prototype.handleDeleteCalendar = function(calendar) {
    // Update calendar if it is already in the list
    for (var i = 0; i < app.calendars.length; i++) {
      if (app.calendars[i].calendarId == calendar.calendarId) {
        app.calendars.splice(i, 1); // Remove from list
        renderCalendars(); // Rerender calendars
        return;
      }
    }
    // The events of the calendar disappear but they are still avaliable
    // inside app.events, we might call refreshCalendars() to flush the
    // state of the calendar.
  };

  NotificationAdapter.prototype.handleUpdateCalendar = function(calendar) {
    // Update calendar if it is already in the list
    for (var i = 0; i < app.calendars.length; i++) {
      if (app.calendars[i].calendarId == calendar.calendarId) {
        if (calendar.name) {
          app.calendars[i].name = calendar.name; // We found it
        }
        if (calendar.role) {
          app.calendars[i].role = calendar.role;
          refreshCalendars();
          return;
        }
        renderCalendars();
        return;
      }
    }
    // We don't which calendar this is, ignore for now
    // we might add it to the list
  };

  NotificationAdapter.prototype.handleJoinCalendar = function(join) {
    // join object: calendarId, user field. User expands to normal user object
    // ignore message to self
    // if (app.user.userId == join.user.userId) {
    //   return;
    // }
    // Check if the user already exists somehow
    for (var i = 0; i < this.server.users.length; i++) {
      if (this.server.users[i].Id == join.user.userId) {
        return; // We found somebody
      }
    }
    
    // Otherwise lets add him to the list
    // configure user info
    var userInfo = new ChatUserInfo();
    userInfo.Id = join.user.userId;
    userInfo.RoomId = NotificationServerAdapter.DEFAULT_ROOM_ID;
    userInfo.Name = join.user.firstName + " " + join.user.lastName;
    //userInfo.Email = join.user.email;
    userInfo.ProfilePictureUrl = "img/user_chat_icon.png";
    userInfo.Status = 1; // The user is online as he joined just now
    this.server.users.push(userInfo);
    this.server.enterRoom(NotificationServerAdapter.DEFAULT_ROOM_ID); // Refresh list
    
    // Notify if admin
    var calendar = getCalendarById(join.calendarId);
    if (calendar.role === "admin") {
      toastr.info(join.user.firstName + " has just joined " + calendar.name);
    }
  };
  
  NotificationAdapter.prototype.handleUnjoinCalendar = function(unjoin) {
    // unjoin object same as in joinCalendar, see above function
    // ignore message to self
    if (app.user.userId === unjoin.user.userId) {
      refreshCalendars();
      return;
    }
    // Check if the user already exists, probably should
    for (var i = 0; i < this.server.users.length; i++) {
      if (this.server.users[i].Id == unjoin.user.userId) {
        this.server.users.splice(i, 1);
        this.server.enterRoom(NotificationServerAdapter.DEFAULT_ROOM_ID); // Refresh list
        return;
      }
    }
  };

  NotificationAdapter.prototype.handleDeleteEvent = function(event) {
    // Try to update badge
    if(!notifyBadge(event.calendarId)) {
      // Update the actual event object in the state of the app
      // TODO: update saved events if present
      for (var i = 0; i < app.events.length; i++) {
        if (app.events[i].model.eventId == event.eventId) {
          app.events.splice(i, 1)[0].view.remove(); // Remove event
          break;
        }
      }
    }
  };

  NotificationAdapter.prototype.handleUpdateEvent = function(event) {
    // Try to update badge
    if(!notifyBadge(event.calendarId)) {
      var controller = getEventControllerById(event.eventId);
      if (controller) {
        // server does not return the correct current count on notification
        delete event.currentCount;
        controller.update(event);
      } else {
        controller = new Event(event);
        app.events.push(controller);
        controller.render();
      }
    }
  };

  NotificationAdapter.prototype.handleUnjoinEvent = function(unjoin) {
    // update count badge if event is rendered in calendar
    var controller = getEventControllerById(unjoin.eventId);
    controller.update({
      currentCount: controller.model.currentCount - 1,
      hasJoined: app.user.userId === unjoin.userId ? false : controller.model.hasJoined
    });
  };

  NotificationAdapter.prototype.handleJoinEvent = function(join) {
    // update count badge if event is rendered in calendar
    var controller = getEventControllerById(join.eventId);
    controller.update({
      currentCount: controller.model.currentCount + 1,
      hasJoined: app.user.userId === join.userId ? true : controller.model.hasJoined
    });
  };

  NotificationAdapter.prototype.handleOffline = function(userId) {
    for (var i = 0; i < this.server.users.length; i++) {
      if (this.server.users[i].Id == userId) {
        this.server.users[i].Status = 0;
        break;
      }
    }
    this.server.enterRoom(NotificationServerAdapter.DEFAULT_ROOM_ID);
  };

  NotificationAdapter.prototype.handleOnline = function(userId) {
    for (var i = 0; i < this.server.users.length; i++) {
      if (this.server.users[i].Id == userId) {
        this.server.users[i].Status = 1;
        break;
      }
    }
    this.server.enterRoom(NotificationServerAdapter.DEFAULT_ROOM_ID);
  };

  NotificationAdapter.prototype.handleText = function(fromId, text) {
    var bounceMessage = new ChatMessageInfo();
    bounceMessage.UserFromId = fromId; // It will from our user
    bounceMessage.UserToId = app.user.userId;
    bounceMessage.RoomId = NotificationServerAdapter.DEFAULT_ROOM_ID;
    bounceMessage.ConversationId = NotificationServerAdapter.DEFAULT_CONVERSATION_ID;
    bounceMessage.Message = text;
    bounceMessage.ClientGuid = null;
    this.client.triggerMessagesChanged(bounceMessage);
  };
  
  NotificationAdapter.prototype.handleTyping = function(fromId) {
    // Create the typing signal object, it is used in trigger function
    var typingSignal = { UserToId: app.user.userId };
    // Look for the user who sent this
    for (var i = 0; i < this.server.users.length; i++) {
      if (this.server.users[i].Id == fromId) {
        // Attach the user to the typingSignal
        typingSignal.UserFrom = this.server.users[i];
        // Trigger typing
        this.client.triggerTypingSignalReceived(typingSignal);
        return;
      }
    }
    // Otherwise we don't know who this is
  };

  NotificationAdapter.prototype.handleRoster = function(roster) {
    this.server.users = roster.map(function(user) {
      // configure user info
      var userInfo = new ChatUserInfo();
      userInfo.Id = user.uid;
      userInfo.RoomId = NotificationServerAdapter.DEFAULT_ROOM_ID;
      userInfo.Name = user.firstName + " " + user.lastName;
      //userInfo.Email = user.email;
      userInfo.ProfilePictureUrl = "img/user_chat_icon.png";
      userInfo.Status = user.isOnline /* Online */ ;
      return userInfo;
    });

    var me = new ChatUserInfo();
    me.Id = app.user.userId;
    me.RoomId = NotificationServerAdapter.DEFAULT_ROOM_ID;
    me.Name = app.user.firstName + " " + app.user.lastName;
    me.Email = app.user.email;
    me.ProfilePictureUrl = "img/user_chat_icon.png";
    me.Status = 1 /* Online */ ;
    this.server.users.push(me);

    // configuring rooms
    var defaultRoom = new ChatRoomInfo();
    defaultRoom.Id = NotificationServerAdapter.DEFAULT_ROOM_ID;
    defaultRoom.Name = "Default Room";
    //defaultRoom.UsersOnline = this.server.users.length;

    this.server.rooms = [defaultRoom];
    this.server.enterRoom(NotificationServerAdapter.DEFAULT_ROOM_ID);
  };

  return NotificationAdapter;
})();
