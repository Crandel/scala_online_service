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

    console.log(message);
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
    console.log(msg);
    sock.send(msg);
  }

  sock.onopen = function(){
    showMessage('Connection to server started');
  };

  $('#login').click(function() {
    var login = JSON.stringify({
      "$type": "login",
      "username": "user1234",
      "password": "password1234"
    });
    console.log("login");
    sendMessage(login);
  });

  $('#table_list').click(function() {
    var tl = JSON.stringify({
      "$type": "table_list",
      "tables": [
        {
          "id": 1,
          "name": "table - James Bond",
          "participants": 7
        }, {
          "id": 2,
          "name": "table - Mission Impossible",
          "participants": 4
        }
      ]
    }
                           );
    console.log("table_list");
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
    showMessage(error);
  };
});
