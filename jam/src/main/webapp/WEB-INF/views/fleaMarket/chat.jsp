<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<style>
.chat-container {
	display: flex;
	width: 800px;
	height: 600px;
	border: 1px solid #ccc;
	font-family: 'Arial', sans-serif;
	background-color: white;
	margin: auto;
	margin-top: 20px;
	/*box-shadow: 0 0 10px rgba(0,0,0,0.2);
	z-index: 999;*/
}

.chat-sidebar {
	width: 280px;
	border-right: 1px solid #eee;
	overflow-y: auto;
}

.chat-main {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.chat-header {
	padding: 10px;
	font-weight: bold;
	border-bottom: 1px solid #ddd;
}

.chat-room-list {
	padding: 0;
	margin: 0;
}

.chat-room-item {
	display: flex;
	align-items: center;
	padding: 10px;
	border-bottom: 1px solid #f0f0f0;
	cursor: pointer;
}

.chat-room-item:hover {
	background-color: #f9f9f9;
}

.chat-room-item.active {
    background-color: #f9f9f9;
}

.chat-profile-img {
	width: 36px;
	height: 36px;
	border-radius: 50%;
	margin-right: 10px;
}

.chat-room-info {
	flex-grow: 1;
}

.chat-user-name {
	font-weight: bold;
}

.chat-last-message {
	font-size: 12px;
	color: #888;
}

.chat-room-time {
	font-size: 11px;
	color: #aaa;
	white-space: nowrap;
}

.chat-topbar {
	padding: 10px 12px;
	background: #f5f5f5;
	border-bottom: 1px solid #ddd;
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-weight: bold;
}

.chat-messages {
	display: flex;
	flex-direction: column;
	overflow-y: auto;
	padding: 10px;
	background: #fafafa;
    flex: 1;
    gap: 8px;
}

.chat-input-container {
	display: flex;
	border-top: 1px solid #ddd;
}

.chat-input-container input {
	flex: 1;
	padding: 10px;
	border: none;
	outline: none;
}

.chat-input-container button {
	padding: 10px 20px;
	background: #ffa425;
	color: white;
	border: none;
	cursor: pointer;
}
.chat {
	display: inline-block;
	max-width: 60%;
	padding: 8px 12px;
	border-radius: 12px;
	word-wrap: break-word;
	position: relative;
	margin-bottom: 8px;
}

.myChat {
	background-color: #ffa425;
	color: white;
	align-self: flex-end;
	border-bottom-right-radius: 0;
}
.otherChat {
	background-color: #eee;
	color: black;
	align-self: flex-start;
	border-bottom-left-radius: 0;
}

.chat-time {
	font-size: 10px;
	color: #888;
	margin-top: 4px;
	text-align: right;
}

/* 방을 아직 선택하지 않은 상태 */
.chat-main.empty .chat-topbar,
.chat-main.empty .chat-input-container,
.chat-main.empty .chat-messages { display: none; }

.chat-main.empty::before {
	content: "대화를 선택해 주세요";
	display: flex;
	justify-content: center;
	align-items: center;
	height: 100%;
	color: #aaa;
	font-size: 14px;
}
</style>

<script>
$(function(){
	if (!window.MY_ID) { history.back(); }
	loadChatRooms().then(() => {
		const chatRoomId = sessionStorage.getItem('chatRoomId');
		if (chatRoomId) {
			setEmptyUI(false);
			getChatMessages(chatRoomId);
			initWebSocket(chatRoomId);
			
			requestAnimationFrame(() => {
				$('.chat-room-item').removeClass('active');
				$('.chat-room-item[data-chatRoomId="' + chatRoomId + '"]').addClass('active');
				
			});
		} else {
			setEmptyUI(true);
		}
	});
	
	const $input = document.getElementById("chatMessage");
	const $send = document.getElementById("send");
	
	if ($send) {
		$send.addEventListener("click", function() {
			const roomId = sessionStorage.getItem("chatRoomId");
			if (!roomId) return;
			sendChat(roomId);
		});
	}
	
	if ($input) {
		$input.addEventListener("keydown", function(e) {
			if (e.key === "Enter" && !e.shiftKey) {
				e.preventDefault();
				$send?.click();
			}
		});
	}
	
	window.addEventListener('beforeunload', () => {
		try { if (currentRoomId) socket?.send(JSON.stringify({ type:'LEAVE', chatRoomId: currentRoomId })); } catch(e){}
		try { socket?.close(1000); } catch(e){}
	});
})

function setEmptyUI(isEmpty) {
	const main = document.getElementById("chatRoom");
	if (!main) return;
	if (isEmpty) {
		main.classList.add("empty");
	} else {
		main.classList.remove("empty");
	}
}

function timeAgo(dateString) {
	  if (!dateString) return "";
	  const past = new Date(dateString.replace(" ", "T"));
	  if (isNaN(past)) return dateString;

	  const diff = Math.floor((Date.now() - past.getTime()) / 1000);
	  if (diff < 10) return "방금 전";
	  if (diff < 60) return diff + "초 전";
	  if (diff < 3600) return Math.floor(diff / 60) + "분 전";
	  if (diff < 86400) return Math.floor(diff / 3600) + "시간 전";
	  if (diff < 172800) return "어제";
	  if (diff < 2592000) return Math.floor(diff / 86400) + "일 전";
	  return past.toLocaleDateString().replace(/\.$/, "");
	}


function getChatMessages(chatRoomId){
	fetch('/api/chat/messages?chatRoomId='+chatRoomId)
	.then(res=>{
		if (!res.ok){
			if (res.status === 401){
				alert("로그인이 필요합니다.");
				location.href = "/member/login";
				return Promise.reject();
			}
			if (res.status === 403 || res.status === 404){
				alert("채팅방에 접근할 수 없습니다.");
				location.href = "/chat";
				return Promise.reject();
			}
			throw new Error("서버 오류: " + res.status);
		}
		return res.json();
	})
	.then(data =>{
		renderChatMessages(data);
	})
	.catch(err =>{
		console.error('채팅 메시지 불러오기 실패:', err);
	})
}

let socket = null;
let currentRoomId = null;

function sendEnter(roomId) {
	if (!socket || socket.readyState !== WebSocket.OPEN) return;
	socket.send(JSON.stringify({ type: "ENTER", chatRoomId: roomId }));
	currentRoomId = roomId;
}

function sendLeave(roomId) {
	if (!socket || socket.readyState !== WebSocket.OPEN) return;
	socket.send(JSON.stringify({ type: "LEAVE", chatRoomId: roomId }));
	currentRoomId = null; // 상태 초기화
}

//웹소켓 초기화
function initWebSocket(chatRoomId) {
	try {
		// 웹소켓 중복 연결 방지
		if (socket) {
			if (socket.readyState === WebSocket.OPEN) {
				sendEnter(chatRoomId);
				return;
			}
			if (socket.readyState === WebSocket.CONNECTING) {
				return; // 연결 중이면 대기
			}
		}
		
		socket = new WebSocket("ws://localhost:8080/ws");	
		
		let retried = false;
		socket.onopen = function() { 
		  retried = false; 
		  sendEnter(chatRoomId); 
		};

		socket.onclose = function() {
		  console.log("WebSocket is closed now.");
		  if (currentRoomId && !retried) {
		    retried = true;
		    setTimeout(() => initWebSocket(currentRoomId), 2000);
		  }
		};
		
		socket.onerror = function(error) {
			console.log("WebSocket error: " + error);
		};

		socket.onmessage = function(event) {
			const { type, data } = JSON.parse(event.data);
			if (type === "MESSAGE") {
				const isMine = (data.senderId === window.MY_ID);
				displayChat({ ...data, mine: isMine });
				updateRoomPreview({ chatRoomId: data.chatRoomId, message: data.message, chatDate: data.chatDate });
			} else if (type === "PARTNER_INFO") {
				renderPartnerName(data.partnerName);
			} else if (type === 'ROOM_CREATED'){
				prependRoomItem(data);
				$('.chat-room-item[data-chatRoomId="' + data.chatRoomId + '"]').trigger('click');
			} else if (type === "ERROR") {
				sessionStorage.removeItem("chatRoomId");

				if(data.code === 401){
					if (confirm(data.message)) location.href = '/member/login';
					else location.href = '/';
				}else if ([403, 404].includes(data.code)) {
					alert(data.message);
					location.href = '/chat';
				} else { // 400, 500
					alert(data.message);
					window.location.reload();
				}
			}
		};
	} catch (err) {
		console.error("채팅 초기화 중 오류 발생:", err);
	}
}

function sendChat(chatRoomId) {
	let chatMessage = $("#chatMessage").val();
	
	if(!chatRoomId || chatRoomId.trim() === "") return;
	if(!chatMessage || chatMessage.trim() === "") return;

	if (socket && socket.readyState === WebSocket.OPEN) {
		var chatObject = {
			chatRoomId: chatRoomId,
			type: "MESSAGE",
			message: chatMessage
		};

		var chatJson = JSON.stringify(chatObject); // 객체를 JSON 문자열로 변환
		socket.send(chatJson); // JSON 문자열을 WebSocket을 통해 전송
		
		$("#chatMessage").val(""); // 입력 필드를 비움
	}
}

function displayChat(data) {
	const chatList = document.getElementById("chatList");

	const chatDiv = document.createElement("div");
	chatDiv.className = "chat " + (data.mine ? "myChat" : "otherChat");

	const textSpan = document.createElement("span");
	textSpan.textContent = data.message;

	const timeDiv = document.createElement("div");
	timeDiv.className = "chat-time";
	// 서버가 시간을 안 주면 현재 시각으로 표시
	const timeText = data.chatDate ? data.chatDate.split(" ")[1] : new Date().toTimeString().slice(0,5);
	timeDiv.textContent = timeText;

	chatDiv.appendChild(textSpan);
	chatDiv.appendChild(timeDiv);

	chatList.appendChild(chatDiv);
	chatList.scrollTop = chatList.scrollHeight; // 스크롤 맨 아래로
}


function renderChatMessages(data){
	const chatList = document.getElementById("chatList");
	chatList.innerHTML = "";

	if (!Array.isArray(data) || data.length === 0) return;
	
	data.forEach(msg => {
		const isMine = (msg.senderId === window.MY_ID);
		displayChat({
			...msg,
			mine: isMine
		});
	});
	chatList.scrollTop = chatList.scrollHeight;
}

function renderPartnerName(partnerName) {
	const chatPartnerName = $("#chatPartnerName");
	chatPartnerName.text(partnerName);
}

const roomsIndex = new Map(); // chatRoomId -> jQuery item

function prependRoomItem({ chatRoomId, partnerId, partner, message, chatDate }) {
	// 이미 있으면 새로 만들지 말고 위로 올림
	if (roomsIndex.has(chatRoomId)) {
		const $item = roomsIndex.get(chatRoomId);
		updateRoomPreview({ chatRoomId, message, chatDate });
		$item.prependTo('.chat-room-list');
		return;
	}

	const $list = $('.chat-room-list');
	const $tpl  = $('<div class="chat-room-item"></div>')
		.attr('data-chatRoomId', chatRoomId)
		.append(`
			<div class="chat-room-info">
				<div class="chat-user-name"></div>
				<div class="chat-last-message"></div>
			</div>
			<div class="chat-room-time"></div>
		`);

	$tpl.find('.chat-user-name').text(partner || '');
	$tpl.find('.chat-last-message').text(message || '');
	$tpl.find('.chat-room-time').text(chatDate ? timeAgo(chatDate) : '');

	$tpl.on('click', function() {
		// 기존 active 제거
	    $('.chat-room-item').removeClass('active');
	    // 현재 클릭한 아이템만 active
	    $(this).addClass('active');
	    
		const roomId = chatRoomId;
		
		if (currentRoomId && currentRoomId !== roomId) sendLeave(currentRoomId);
		sessionStorage.setItem('chatRoomId', roomId);
		setEmptyUI(false);
		
		getChatMessages(roomId);
		
		if (socket && socket.readyState === WebSocket.OPEN) sendEnter(roomId);
		else initWebSocket(roomId);
	});

	$list.prepend($tpl);
	roomsIndex.set(chatRoomId, $tpl);
}

function updateRoomPreview({ chatRoomId, message, chatDate }) {
	const $item = roomsIndex.get(chatRoomId);
	if (!$item) return; // 리스트에 없으면 패스 (ROOM_CREATED로 들어올 때 생성됨)
	$item.find('.chat-last-message').text(message || '');
	$item.find('.chat-room-time').text(chatDate ? timeAgo(chatDate) : '');
	$item.prependTo('.chat-room-list'); // 최근 활동이므로 맨 위로
}

function loadChatRooms() {
	return fetch('/api/chat/chatRooms')
	.then(res => res.json())
	.then(list => {
		roomsIndex.clear();
		
		const $list = $('.chat-room-list').empty();
		list.forEach(r => {
			prependRoomItem({
				chatRoomId: r.chatRoomId,
				partnerId: r.partnerId,
				partner: r.partner || '',
				message: r.message || '',
				chatDate: r.chatDate || ''
			});
		});
		highlightCurrentRoom();
	})
	.catch(console.error);
}

function selectRoom(roomId) {
	if (!roomId) return;

	// 이미 선택된 방이면 스킵
	if (currentRoomId === roomId) {
		highlightCurrentRoom();
		return;
	}

	$('.chat-room-item').removeClass('active');
	$(`.chat-room-item[data-chatRoomId="${roomId}"]`).addClass('active');

	// LEAVE → 상태 갱신 → 메시지 로드 → ENTER/연결
	if (currentRoomId) sendLeave(currentRoomId);
	currentRoomId = roomId;
	sessionStorage.setItem('chatRoomId', roomId);
	setEmptyUI(false);
	getChatMessages(roomId);

	if (socket && socket.readyState === WebSocket.OPEN) sendEnter(roomId);
	else initWebSocket(roomId);
}

function highlightCurrentRoom() {
	const id = sessionStorage.getItem('chatRoomId');
	$('.chat-room-item').removeClass('active');
	if (id) $(`.chat-room-item[data-chatRoomId="${id}"]`).addClass('active');
}
</script>

</head>
<body>
<div id="chatContainer" class="chat-container">
	<!-- 왼쪽: 채팅방 리스트 -->
	<div id="chatRoomList" class="chat-sidebar">
		<div class="chat-header">
			<span>전체 대화</span>
		</div>
		<div class="chat-room-list">
			<div class="chat-room-item openChat" data-user="닉네임">
				<div class="chat-room-info">
					<div class="chat-user-name"></div>
					<div class="chat-last-message"></div>
				</div>
				<div class="chat-room-time"></div>
			</div>
		</div>
	</div>

	<!-- 오른쪽: 채팅창 -->
	<div id="chatRoom" class="chat-main">
		<div id="chatDrag" class="chat-topbar">
			<span id="chatPartnerName"></span>
		</div>
		<div id="chatList" class="chat-messages">
		</div>
		<div class="chat-input-container">
			<input id="chatMessage" placeholder="메시지를 입력하세요" />
			<button id="send">전송</button>
		</div>
	</div>
</div>
</body>
</html>

