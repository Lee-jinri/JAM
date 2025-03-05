<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>

<script src="https://cdn.jsdelivr.net/npm/sockjs-client/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs/lib/stomp.min.js"></script>
    
<style type="text/css">
	/*popup*/
	.popup_layer {position:fixed;top:10%;right:0;z-index: 10000; height: 600px;
    width: 375px; }
	/*팝업 박스*/
	.popup_box{position: relative;top:50%;left:50%; overflow: auto; height: 600px; width:375px;transform:translate(-50%, -50%);z-index:1002;box-sizing:border-box;background:#fff;box-shadow: 2px 5px 10px 0px rgba(0,0,0,0.35);-webkit-box-shadow: 2px 5px 10px 0px rgba(0,0,0,0.35);-moz-box-shadow: 2px 5px 10px 0px rgba(0,0,0,0.35);}
	/*컨텐츠 영역*/
	.popup_box .popup_cont {padding:50px;line-height:1.4rem;font-size:14px; }
	.popup_box .popup_cont h2 {padding:15px 0;color:#333;margin:0;}
	.popup_box .popup_cont p{ border-top: 1px solid #666;padding-top: 30px;}
	/*버튼영역*/
	.popup_box .popup_btn {display:table;table-layout: fixed;width:100%;height:70px;background:#ECECEC;word-break: break-word;}
	.popup_box .popup_btn a {position: relative; display: table-cell; height:70px;  font-size:17px;text-align:center;vertical-align:middle;text-decoration:none; background:#ECECEC;}
	.popup_box .popup_btn a:before{content:'';display:block;position:absolute;top:26px;right:29px;width:1px;height:21px;background:#fff;-moz-transform: rotate(-45deg); -webkit-transform: rotate(-45deg); -ms-transform: rotate(-45deg); -o-transform: rotate(-45deg); transform: rotate(-45deg);}
	.popup_box .popup_btn a:after{content:'';display:block;position:absolute;top:26px;right:29px;width:1px;height:21px;background:#fff;-moz-transform: rotate(45deg); -webkit-transform: rotate(45deg); -ms-transform: rotate(45deg); -o-transform: rotate(45deg); transform: rotate(45deg);}
	.popup_box .popup_btn a.close_day {background:#5d5d5d;}
	.popup_box .popup_btn a.close_day:before, .popup_box .popup_btn a.close_day:after{display:none;}
	/*오버레이 뒷배경*/
	.popup_overlay{position:fixed;top:0px;right:0;left:0;bottom:0;z-index:1001;;background:rgba(0,0,0,0.5);}
	.popup_chatContent{height: 65px; border-top: 1px solid #666; padding-top: 10px;}
	.popup_lastChat{display: flex;padding-left: 40px;align-items: center;padding-top: 10px;}
	#closePop{float:right;}
	
	.message_input{text-align:center;}
	#content{margin:10px;}
</style>
<script>
$(function(){
	let userId = "";
	let chatRoomId = "";
	let currentPage = 0;
    const pageSize = 30;

	let senderId = '';
	let receiverId = '';
	
    
 	// 페이지 로드 시 초기 상태 저장
    initialPopupContent = $(".popup_cont").html();
	
	$("#chatButton").click(function(){
		chatRooms();
	})
	
	function chatRooms(){
		fetch('/api/member/getUserInfo', {
            method: 'GET', 
            headers: {
            	'Authorization': localStorage.getItem("Authorization")
            },
        })
        .then(response => {
        	if(response.status == 401){
        		localStorage.removeItem('Authorization');
        		
        		alert("로그인 유효시간이 초과 되었습니다. 다시 로그인 해주세요.");
        		window.location.reload();
        	}else if (!response.ok) throw new Error('Network response was not ok');
        	
			return response.json();   
        })
        .then((data) => {
        	
        	
        	userId = data.user_id;
        	
        	if (data && userId) {
        		//팝업 띄우기
				document.getElementById("popup_layer").style.display = "block";
				
        		
        		// 사용자 채팅방 목록 
        		loadUserChatRooms();
			}
		})
        .catch(error => {
            console.error('사용자 정보를 가져오는 중 오류 발생:', error);
        });
	}
	
	
	//팝업 닫기
	$("#closePop").click(function(){
		document.getElementById("popup_layer").style.display = "none";
	})
	
	// 뒤로가기
	$(document).on('click', '#backButton', function() {
		$(".popup_cont").html(initialPopupContent);
		chatRooms();
	});
	
	// 사용자가 참여한 채팅방 목록을 조회
	function loadUserChatRooms() {
		fetch('/api/chat/chatRooms/'+userId, {
            method: 'GET', 
        })
        .then(response => {
            if (response.ok) {
            	return response.json();
            } else {
                throw new Error('Network response was not ok');
            }
        })
        .then((roomsWithDetails) => {
        	
        	// 이거 잘 됨
        	let $div = $("#chatRoomList");
        	
        	// 여기 시간 들어가야 됨
        	Object.keys(roomsWithDetails).forEach(chatRoomId => {
        		let $element = $("#chat-template").clone().removeAttr('id');
        		let details = roomsWithDetails[chatRoomId];
        		
                let lastMessage = details.message;
                let otherUserId;
                
                if(details.senderId != userId) otherUserId = senderId;
                else otherUserId = details.receiverId;

                $element.attr("data-name",details.chatRoomId);
                $element.attr("data-otherUserId", otherUserId);
                $element.addClass("popup_chatContent");
                $element.find('.lastChat').html(lastMessage);
                $element.find('.otherUserId').html(otherUserId);
                
                $div.append($element);

            });
        })
        .catch(error => {
            console.error(error);
        });
		
    }
	
	// 동적으로 생성된 요소에 클릭 이벤트 바인딩
    $(document).on('click', '.popup_chatContent', function() {
        chatRoomId = $(this).attr("data-name");
        receiverId = $(this).attr("data-otherUserId");
        currentPage = 0; 

        loadChatMessages(chatRoomId, userId, receiverId, currentPage, pageSize);
    });

    var socket;
    
    function initWebSocket() {
        if (socket) {
            console.log("WebSocket is already initialized.");
            return;
        }
        
        socket = new WebSocket("ws://localhost:8080/ws");

        socket.onopen = function(event) {
            console.log("웹소켓 열림.");
        };

        socket.onclose = function(event) {
            console.log("WebSocket is closed now.");
        };

        socket.onerror = function(error) {
            console.log("WebSocket error: " + error);
        };

        socket.onmessage = function(event) {
        	
        	displayMessage(event.data);
        };
        
        
    }

    $(document).on('click', '#send', function() {
    	var message = $("#messageInput").val();  // 사용자가 입력한 메시지
        var senderId = userId;     // 보낸 사람 ID
        var receiverId = $("#receiverId").val(); // 받는 사람 ID
        var chatRoomId = $("#chatRoomId").val(); // 채팅방 ID
        var messageType = "MESSAGE";             

        if (message && socket.readyState === WebSocket.OPEN) {
        	var messageObject = {
                    senderId: senderId,
                    receiverId: receiverId,
                    chatRoomId: chatRoomId,
                    type: messageType,
                    message: message
                };

                var messageJson = JSON.stringify(messageObject); // 객체를 JSON 문자열로 변환
                socket.send(messageJson); // JSON 문자열을 WebSocket을 통해 전송
                $("#messageInput").val(""); // 입력 필드를 비움
        } else {
        	alert("메시지를 입력해주세요.");
        	$("#messageInput").focus();
            console.log("WebSocket is not open or message is empty.");
        }
    	
    });
    
    $("#send").click(function(){
    	
    })
    
    function loadChatMessages(chatRoomId, userId, receiverId, currentPage, pageSize) {
        initWebSocket();
        
        $(".popup_cont").empty(); // 기존 채팅 메시지 영역 비우기
        $(".popup_cont").append('<div class="chatRoomInfo"></div>');
        $(".chatRoomInfo").append('<input type="text" id="chatRoomId" value="' + chatRoomId + '"></input>');
        $(".chatRoomInfo").append('<input type="text" id="receiverId" class="border-none" value="' + receiverId + '"></input>'); // 이거 otherUserId 넣어야됨 이 함수 userId는 사용자임
        
        $(".popup_cont").append('<div id="messages"></div>'); // 새로운 메시지 영역 추가
		$(".popup_cont").append('<div class=""></div>');
        
        $(".message_input").append('<input type="text" id="messageInput"><button id="send">전송</button>')
		
     
        if (socket.readyState === WebSocket.OPEN) {
        	
            socket.send(JSON.stringify({
                chatRoomId: chatRoomId,
                senderId: userId,
                page: currentPage,
                size: pageSize,
                type: "ENTER"
            }));
        } else {
            socket.onopen = function() {
                socket.send(JSON.stringify({
                    chatRoomId: chatRoomId,
                    senderId: userId,
                    page: currentPage,
                    size: pageSize,
                    type: "ENTER"
                }));
            };
        }
        
     }


    // 메시지 표시 함수
    function displayMessage(message) {
    	
    	let chatMessage = JSON.parse(message);
    	
        let messageElement = '<div class="chatMessage"><strong>'+ chatMessage.senderId + ':</strong>' + chatMessage.message + '</div>';
        $("#messages").append(messageElement);
    }
    
    
    
    // 스크롤 이벤트 핸들러 (더 많은 메시지를 가져오기 위해)
    $("#chatRoomMessages").scroll(function() {
        if ($(this).scrollTop() + $(this).innerHeight() >= this.scrollHeight) {
            loadChatMessages(chatRoomId, userId, currentPage, pageSize); // Scroll to the bottom, load more messages
        }
    });
	
	

})
</script>
</head>
<body>
	<sec:authorize access="isAuthenticated()">
		<button id="chatButton" class="scroll-button" >Chat</button>
	</sec:authorize>
	
	
	<div class="popup_layer" id="popup_layer" style="display: none;">
		<div class="popup_box">
			<div style="height: 10px; width: 375px; float: top;">
				<button id="closePop">
					<img src="/resources/include/images/ic_close.svg" class="m_header-banner-close" width="30px" height="30px">
				</button>
				<button id="backButton">
				뒤로가기
				</button>
	      	</div>
	      	<!--팝업 컨텐츠 영역-->
	      	<div class="popup_cont">
	          	<h5>JAM CHAT</h5>
	          	
	          	<div id="chatRoomList">
	          		<div id="chat-template">
	          			<span class="timeStamp"></span>
	          			<span class="otherUserId"></span>
	          			<span class="lastChat"></span>
	          		</div>
	          	</div>
	      	</div>
	  	</div>
	  	
	  	
	</div>
</body>
</html>