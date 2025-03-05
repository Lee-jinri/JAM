<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>

<script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js"></script>
    
    
<style type="text/css">
	#chatHeader {
	    display: flex;
	}

.chat-btn {
    width: 35px;
    height: 70%; 
	border: none;
    background-color: #ddd; /* 버튼 배경색 */
    cursor: pointer;
    margin: 15px;
    border-radius: 50%;
}

.chat-btn:not(:last-child) {
    margin-right: 5px; /* 버튼 사이 간격 */
}

</style>

<script>
    $(function(){
    	//getChatRooms();
    })
    
    function getChatRooms(){
    	fetch('/api/chat/getChatRooms')
    	.then(response =>{
    		if(!response.ok){
    			alert('시스템 오류 입니다. 잠시 후 다시 시도해주세요.');
    		}
    		return response.json();
    	})
    	.then((data) =>{
    		console.log(data);
    	})
    }
    
    
</script>
</head>
<body>
	<div id="chatContainer" class="border-radius-43px" style="display: none; overflow: hidden;width: 300px; height: 500px; background-color: lightgray; position: absolute; flex-direction: column;">
	    <div id="chatHeader" style="height: 50px;">
	    	<button id="chatBack" class="chat-btn">←</button>
	        <div id="chatDrag" style="flex-grow: 1;"></div>
	        <button id="chatExit" class="chat-btn">X</button>
	    </div>
	    <div id="chatContent" style="flex-grow: 2; padding: 10px;">
	    	
	    	
	    	<div id="chatRoomList" style="display: none;">
	    		<h1>chatRoomList</h1>
          		<div id="chat-template" class="openChat">
          			<span class="userName"></span>
          			<span class="listMessage"></span>
          			<span class="timeStamp"></span>
          		</div>
          	</div>
          	
          	
          	
          	<div id="chatRoom" style="display: none;" class="chatRoom">
          		<div id="chatRoomInfo">
          			<span id="chatTargetUserName"></span>
          			<span id="chatRoomId"></span>
          		</div>
          		<div id="chatList" class="chatList">
					<div class="otherChat">
						<span class="">안녕ㅎ라세용</span>
						<span class="sendTimeSpan"></span>
					</div>
          			<div class="myChat">
          				<span class="">네 안녕핫빈당</span>
          			</div>
          		</div>
          		<div id="chatInput" class="chatInput">
          			<button id="chatOption">+</button>
          			<input id="chatMessage" type="text" style="flex:1; width:1px;">
          			<button id="send" style="width: 50px;">전송</button>
          		</div>
          	</div>
	    </div>
	</div>
	
	
</body>
</html>