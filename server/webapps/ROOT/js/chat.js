var DemoClientAdapter = (function() {
  function DemoClientAdapter() {
    this.messagesChangedHandlers = [];
    this.typingSignalReceivedHandlers = [];
    this.userListChangedHandlers = [];
  }
  // adds a handler to the messagesChanged event
  DemoClientAdapter.prototype.onMessagesChanged = function(handler) {
    this.messagesChangedHandlers.push(handler);
  };

  // adds a handler to the typingSignalReceived event
  DemoClientAdapter.prototype.onTypingSignalReceived = function(handler) {
    this.typingSignalReceivedHandlers.push(handler);
  };

  // adds a handler to the userListChanged event
  DemoClientAdapter.prototype.onUserListChanged = function(handler) {
    this.userListChangedHandlers.push(handler);
  };

  DemoClientAdapter.prototype.triggerMessagesChanged = function(message) {
    for (var i = 0; i < this.messagesChangedHandlers.length; i++)
      this.messagesChangedHandlers[i](message);
  };

  DemoClientAdapter.prototype.triggerTypingSignalReceived = function(typingSignal) {
    for (var i = 0; i < this.typingSignalReceivedHandlers.length; i++)
      this.typingSignalReceivedHandlers[i](typingSignal);
  };

  DemoClientAdapter.prototype.triggerUserListChanged = function(userListChangedInfo) {
    for (var i = 0; i < this.userListChangedHandlers.length; i++)
      this.userListChangedHandlers[i](userListChangedInfo);
  };
  return DemoClientAdapter;
})();

var DemoServerAdapter = (function() {
  this.DEFAULT_ROOM_ID = 1;

  function DemoServerAdapter(clientAdapter, socket) {
    this.clientAdapter = clientAdapter;
    this.socket = socket;
    this.users = new Array();
    this.rooms = new Array();
  }
  DemoServerAdapter.prototype.sendMessage = function(roomId, conversationId, otherUserId, messageText, clientGuid, done) {
    var _this = this;
    //console.log("DemoServerAdapter: sendMessage");

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

  DemoServerAdapter.prototype.sendTypingSignal = function(roomId, conversationId, userToId, done) {
    //console.log("DemoServerAdapter: sendTypingSignal");
    // Create our own ChatMessage that the server is going to route
    var chatMessage = {
      type: "typing",
      destinationIds: [userToId],
      sourceId: app.user.userId,
      storeOffline: false,
      payload: {}
    };
    this.socket.send(JSON.stringify(chatMessage));
  };

  DemoServerAdapter.prototype.getMessageHistory = function(roomId, conversationId, otherUserId, done) {
    //console.log("DemoServerAdapter: getMessageHistory");
    // We don't support message history yet
    done([]);
  };

  DemoServerAdapter.prototype.getUserInfo = function(userId, done) {
    //console.log("DemoServerAdapter: getUserInfo");
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

  DemoServerAdapter.prototype.getUserList = function(roomId, conversationId, done) {
    //console.log("DemoServerAdapter: getUserList");
    if (roomId == DEFAULT_ROOM_ID) {
      done(this.users);
      return;
    }
    throw "The given room or conversation is not supported by the demo adapter";
  };

  DemoServerAdapter.prototype.enterRoom = function(roomId, done) {
    //console.log("DemoServerAdapter: enterRoom");

    if (roomId != DEFAULT_ROOM_ID)
      throw "Only the default room is supported in the demo adapter";

    var userListChangedInfo = new ChatUserListChangedInfo();
    userListChangedInfo.RoomId = DEFAULT_ROOM_ID;
    userListChangedInfo.UserList = this.users;

    this.clientAdapter.triggerUserListChanged(userListChangedInfo);
  };

  DemoServerAdapter.prototype.leaveRoom = function(roomId, done) {
    console.log("DemoServerAdapter: leaveRoom");
  };

  return DemoServerAdapter;
})();

var DemoAdapter = (function() {
  function DemoAdapter() {}
  // called when the adapter is initialized
  DemoAdapter.prototype.init = function(done) {
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
    this.client = new DemoClientAdapter();
    this.server = new DemoServerAdapter(this.client, this.socket);

    // Bind on open function
    this.socket.onopen = function() {
      console.log('Info: WebSocket connection opened.');
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
      console.log(msg);
      switch (msg.type) {
        case 'roster':
          _this.handleRoster(msg.payload.roster);
          break;
        case 'text':
          _this.handleText(msg.sourceId, msg.payload);
          break;
        case 'typing':
          _this.handleTyping(msg.sourceId);
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

  DemoAdapter.prototype.handleDeleteCalendar = function(calendar) {
    // ignore message to self
    if (app.user.userId === calendar.userId) {
      return;
    }
    // TODO: need calendar id
  };

  DemoAdapter.prototype.handleUpdateCalendar = function(calendar) {
    // ignore message to self
    if (app.user.userId === calendar.userId) {
      return;
    }
    // TODO: need calendar id
  };

  DemoAdapter.prototype.handleJoinCalendar = function(join) {
    // join object: calendarId, user field. User expands to normal user object
    // ignore message to self
    if (app.user.userId === join.user.userId) {
      return;
    }
    // Check if the user already exists somehow
    for (var i = 0; i < this.server.users.length; i++) {
      if (this.server.users[i].Id == join.user.userId) {
        return; // We found somebody
      }
    }
    
    // Otherwise lets add him to the list
    // configure user info
    /*var userInfo = new ChatUserInfo();
    userInfo.Id = user.uid;
    userInfo.RoomId = DEFAULT_ROOM_ID;
    userInfo.Name = user.firstName + " " + user.lastName;
    //userInfo.Email = user.email;
    userInfo.ProfilePictureUrl = "img/user_chat_icon.png";
    userInfo.Status = user.isOnline;
    this.server.users.push(userInfo);
    this.server.enterRoom(1); // Refresh list */
    console.log("Gonna add user.");
  };

  DemoAdapter.prototype.handleDeleteEvent = function(event) {
    // ignore message to self
    if (app.user.userId === event.userId) {
      return;
    }
    // TODO: need calendar id
  };

  DemoAdapter.prototype.handleUpdateEvent = function(event) {
    var active_calendars = getActiveCalendarIds();
    if (active_calendars.indexOf(event.calendarId) === -1) {
      // update notification badge
      $("#d_user_calendars").children().each(function(k, elem) {
        var view = $(elem);
        var cid = view.data("calid");
        if (cid === event.calendarId) {
          var notification = view.find(".badge");
          if (notification.hasClass("hidden")) {
            notification.removeClass("hidden").text("1");
          } else {
            var count = parseInt(notification.text());
            notification.text(count + 1);
          }
        }
      });
    }
  };

  DemoAdapter.prototype.handleUnjoinEvent = function(unjoin) {
    // ignore message to self
    if (app.user.userId === unjoin.userId) {
      return;
    }
    // update count badge if event is rendered in calendar
    $(".event").each(function(k, elem) {
      var event = $(elem);
      if (event.data("eventId") === unjoin.eventId) {
        updateAttendeeCount(event, -1);
      }
    });
  };

  DemoAdapter.prototype.handleJoinEvent = function(join) {
    // ignore message to self
    if (app.user.userId === join.userId) {
      return;
    }
    // update count badge if event is rendered in calendar
    $(".event").each(function(k, elem) {
      var event = $(elem);
      if (event.data("eventId") === join.eventId) {
        updateAttendeeCount(event, 1);
      }
    });
  };

  DemoAdapter.prototype.handleOffline = function(userId) {
    for (var i = 0; i < this.server.users.length; i++) {
      if (this.server.users[i].Id == userId) {
        this.server.users[i].Status = 0;
        break;
      }
    }
    this.server.enterRoom(1);
  };

  DemoAdapter.prototype.handleOnline = function(userId) {
    for (var i = 0; i < this.server.users.length; i++) {
      if (this.server.users[i].Id == userId) {
        this.server.users[i].Status = 1;
        break;
      }
    }
    this.server.enterRoom(1);
  };

  DemoAdapter.prototype.handleText = function(fromId, text) {
    var bounceMessage = new ChatMessageInfo();
    bounceMessage.UserFromId = fromId; // It will from our user
    bounceMessage.UserToId = app.user.userId;
    bounceMessage.RoomId = 1;
    bounceMessage.ConversationId = 1;
    bounceMessage.Message = text;
    bounceMessage.ClientGuid = null;
    this.client.triggerMessagesChanged(bounceMessage);
  };
  
  DemoAdapter.prototype.handleTyping = function(fromId) {
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

  DemoAdapter.prototype.handleRoster = function(roster) {
    this.server.users = roster.map(function(user) {
      // configure user info
      var userInfo = new ChatUserInfo();
      userInfo.Id = user.uid;
      userInfo.RoomId = DEFAULT_ROOM_ID;
      userInfo.Name = user.firstName + " " + user.lastName;
      //userInfo.Email = user.email;
      userInfo.ProfilePictureUrl = "img/user_chat_icon.png";
      userInfo.Status = user.isOnline /* Online */ ;
      return userInfo;
    });

    var me = new ChatUserInfo();
    me.Id = app.user.userId;
    me.RoomId = DEFAULT_ROOM_ID;
    me.Name = app.user.firstName + " " + app.user.lastName;
    me.Email = app.user.email;
    me.ProfilePictureUrl = "img/user_chat_icon.png";
    me.Status = 1 /* Online */ ;
    this.server.users.push(me);

    // configuring rooms
    var defaultRoom = new ChatRoomInfo();
    defaultRoom.Id = 1;
    defaultRoom.Name = "Default Room";
    //defaultRoom.UsersOnline = this.server.users.length;

    this.server.rooms = [defaultRoom];
    this.server.enterRoom(1);
  };

  return DemoAdapter;
})();
