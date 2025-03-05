<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js" integrity="sha512-1QvjE7BtotQjkq8PxLeF6P46gEpBRXuskzIVgjFpekzFVF4yjRgrQvTG1MTOJ3yQgvTteKAcO7DSZI92+u/yZw==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js"></script>
    
<script>
	$(function(){
		
		let currentPage = 0;
		let pageSize = 30;
	    
		// 서버의 WebSocket 엔드포인트 URL
		var socket = new WebSocket("ws://localhost:8080/ws");

		// WebSocket 연결이 열렸을 때 호출되는 이벤트 핸들러
		socket.onopen = function(event) {
		    console.log("웹소켓 열림.");
		    
		 	// 채팅방 입장 메시지 전송
            socket.send(JSON.stringify({
                chatRoomId: "room123",
                senderId: $("#senderId").val(),
                page: currentPage,
                size: pageSize,
                type: "ENTER"
            }));
		    
		};
		
		// 서버로부터 메시지를 수신했을 때 호출되는 이벤트 핸들러
		socket.onmessage = function(event) {
		    var message = event.data;
		    console.log("Received message from server: " + message);
		    // 메시지를 화면에 표시하거나 처리할 수 있음
		};

		// WebSocket 연결이 닫혔을 때 호출되는 이벤트 핸들러
		socket.onclose = function(event) {
		    console.log("WebSocket is closed now.");
		};

		// WebSocket 오류가 발생했을 때 호출되는 이벤트 핸들러
		socket.onerror = function(error) {
		    console.log("WebSocket error: " + error);
		};
        

        $("#send").click(function(){
			// 서버로 메시지 보내기
		    /* LEAVE: 클라이언트가 채팅방을 떠날 때
				MESSAGE: 일반적인 채팅 메시지
				NOTIFICATION: 특정 이벤트에 대한 알림 */
		    socket.send(JSON.stringify({
		        chatRoomId: "room123",
		        type: "MESSAGE",
		        senderId: "abcd1234",
		        receiverId: "admin",
		        message: "김한글"
		    }));
        
        })
        
        $("#back").click(function(){
        	socket.send(JSON.stringify({
        		chatRoomId: "room123",
        		type: "LEAVE",
        		senderId: "abcd1234"
        	}))
        })

	})
</script>
</head>
<body>
	<input type="text" id="senderId" name="senderId" value="abcd1234"> 
	<input type="text" id="receiverId" name="receiverId" value="admin">
	<input type="text" id="content" name="content" value="한글입니다.">
	<button id="send">전송</button>
	<button id="back">뒤로가기</button>
</body>
</html>