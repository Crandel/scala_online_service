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

        try{
            var messageObj = JSON.parse(message),
                sender = '',
                message_str = '';
            if (messageObj.hasOwnProperty('$type')){
                htmlText = htmlText + message
            } else {
              htmlText = htmlText + message;
            }
            htmlText = htmlText + '\n';
        } catch (e){
            htmlText = htmlText + message;
        }
        messageElem.append($('<p>').html(htmlText));

        messageElem.find('p').each(function(i, value){
            height += parseInt($(this).height());
        });
        messageElem.animate({scrollTop: height});
    }

    function sendMessage(msg){
        sock.send(msg);
    }

    sock.onopen = function(){
        showMessage('Connection to server started');
    };

    // send message from form
    $('#login').click(function() {
        var login = JSON.stringify({
                      "$type": "login",
                      "username": "user1234",
                      "password": "password1234"
                    });
        console.log(login)
        sendMessage(login);
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
