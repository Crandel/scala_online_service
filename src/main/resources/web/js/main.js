$(document).ready(function(){
  var sock = {};
  try{
    sock = new WebSocket('ws://' + window.location.host + '/ws_api');
  }
  catch(err){
    sock = new WebSocket('wss://' + window.location.host + '/ws_api');
  }

  // show message in div#subscribe
  function showMessage(message) {
    var messageElem = $('#subscribe'),
        height = 0,
        date = new Date(),
        options = { hour12: false },
        htmlText = '[' + date.toLocaleTimeString('en-US', options) + '] ';

    console.log("message is " + message);
    try{
      var messageObj = JSON.parse(message);
      console.log(messageObj);
      if (messageObj.hasOwnProperty('$type')){
        console.log("messageObj.hasOwnProperty");
        htmlText = htmlText + message;
      } else {
        console.log("messageObj.hasOwnProperty else");
        htmlText = htmlText + message;
      }
      htmlText = htmlText + '\n';
    } catch (e){
      console.log("catch e");
      htmlText = htmlText + message;
    }
    messageElem.append($('<p>').html(htmlText));

    messageElem.find('p').each(function(i, value){
      height += parseInt($(this).height());
    });
    messageElem.animate({scrollTop: height});
  }

  function sendMessage(msg){
    console.log("sendMessage");
    console.log(msg);
    sock.send(msg);
  }

  sock.onopen = function(){
    showMessage('Connection to server started');
  };

  $('#login').click(function() {
    var login = JSON.stringify({
      "$type": "login",
      "username": "admin",
      "password": "admin"
    });
    console.log("login");
    sendMessage(login);
  });

  $('#login_failed').click(function() {
    var login = JSON.stringify({
      "$type": "login",
      "username": "admin22",
      "password": "admin22"
    });
    console.log("login");
    sendMessage(login);
  });

  $('#ping').click(function() {
    var p = JSON.stringify({
      "$type": "ping",
      "seq": 1
    });
    console.log("ping");
    sendMessage(p);
  });

  $('#pong').click(function() {
    var p = JSON.stringify({
      "$type": "pong",
      "seq": 1
    });
    console.log("pong");
    sendMessage(p);
  });

  $('#subscribeme').click(function() {
    var tl = JSON.stringify({
      "$type": "subscribe_tables"
    });
    console.log("subscribeme");
    sendMessage(tl);
  });

  $('#unsubscribeme').click(function() {
    var tl = JSON.stringify({
      "$type": "unsubscribe_tables"
    });
    console.log("unsubscribeme");
    sendMessage(tl);
  });

  $('#add_table').click(function() {
    var tl = JSON.stringify({
      "$type": "add_table",
      "after_id": 1,
      "table": {
        "name": "table - Foo Fighters",
        "participants": 4
      }
    });
    console.log("add_table");
    sendMessage(tl);
  });

  $('#add_table_start').click(function() {
    var tl = JSON.stringify({
      "$type": "add_table",
      "after_id": -1,
      "table": {
        "name": "table - Mission Impossible",
        "participants": 7
      }
    });
    console.log("add_table_start");
    sendMessage(tl);
  });

  $('#update_table').click(function() {
    var tl = JSON.stringify({
      "$type": "update_table",
      "table": {
        "id": 1,
        "name": "table - Foo Fighters 2",
        "participants": 6
      }
    });
    console.log("update_table");
    sendMessage(tl);
  });

  $('#remove_table').click(function() {
    var tl = JSON.stringify({
      "$type": "remove_table",
      "id": 1
    });
    console.log("remove_table");
    sendMessage(tl);
  });


  // income message handler
  sock.onmessage = function(event) {
    showMessage(event.data);
  };

  sock.onclose = function(event){
    if(event.wasClean){
      showMessage('Clean connection end');
    }else{
      showMessage('Connection broken');
    }
  };

  sock.onerror = function(error){
    console.log(error)
    showMessage(error);
  };
});
