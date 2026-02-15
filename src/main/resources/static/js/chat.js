/* 
1. 우선 채팅방 아이디가 있는지 확인 (initWebSocket에서)
2. 채팅방 아이디 없으면 function getChatRoomId (만들어야 됨)에서 가져옴
3. 있으면 (사용자가 채팅방 리스트에서 특정 채팅방 클릭해서 들어옴) 스킵
4. 채팅 불러올 때 이 채팅방 아이디를 응용해서 사용해야 함.
함수 정리 제대로 할 것 
*/


// 
/* 특정 사용자와 채팅
-- userToggle에서 상대방 닉네임 클릭 - 채팅 버튼 누르면 실행됨.  */
document.addEventListener("openChat", async (event) => {
	
	// userToggle에서 가져옴
    const { userName } = event.detail;
    
	await getChatRoomId(userName); // 채팅방 아이디 가져옴
	initWebSocket(userName); // 웹소켓 초기화
    getChatMessages(); // 채팅방의 메세지 불러옴
});

/* header에서 채팅 클릭하면 실행됨*/
// 나의 채팅 목록 보기
document.addEventListener("openChatList", () => {
    loadChatRooms();
});

async function getChatRoomId(userName){
	try {
        const response = await fetch('/api/chat/chatRoomId?targetUserName=' + userName, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });
        if (!response.ok) {
			/**
			alert 시스템 오류입니다 잠시 후 다시 시도해주세요
			div 다 지우고 새로고침 (안지워도 되나?)
			 */
            throw new Error("Failed to get ChatRoomId.");
        }
        
        const chatRoomId = await response.text();

        // 채팅방 ID를 세션 스토리지에 저장
        sessionStorage.setItem("chatRoomId", chatRoomId);
	    
	} catch (error) {
        console.error("Error loading messages:", error);
    }
}

var socket = null;

// 웹소켓 초기화
function initWebSocket(userName) {
	
	// 웹소켓 중복 연결 방지
	if (socket && socket.readyState === WebSocket.OPEN) { 
        console.log("WebSocket is already initialized and open.");
        return;
    }
	
	socket = new WebSocket("ws://localhost:8080/ws");
			
	let chatRoomId = sessionStorage.getItem("chatRoomId");		
	
	socket.onopen = function(event) {
		// 채팅방 입장 메시지 전송
		socket.send(JSON.stringify({
			chatRoomId: chatRoomId,
			participant: userName,
			type: "ENTER"
		}));
	};

	socket.onclose = function(event) {
		console.log("WebSocket is closed now.");
	};

	socket.onerror = function(error) {
		console.log("WebSocket error: " + error);
	};

	socket.onmessage = function(event) {
		const data = JSON.parse(event.data); // JSON 데이터를 JavaScript 객체로 파싱
		displayMessage(data);
	};
}

// 채팅을 클릭하면 div 보이게 하고 채팅 이벤트 실행
function openChatPage(pageId, userName) {
  chatContainer.style.display = "block";
  chatRoomList.classList.add("hidden");
  chatRoom.classList.add("hidden");

  document.getElementById(pageId).classList.remove("hidden");
  
  if(userName != null) console.log(userName);
}

$(function(){
	
	$("#chatRooms").click(function(){
		
	})
	
	$("#send").click(function(){
		sendChat();
	})
	
	document.getElementById("chatMessage").addEventListener("keydown", function(event) {
	    if (event.key === "Enter") {
	        sendChat();
	    }
	});
})


// 사용자가 참여한 채팅방 목록을 조회
function loadChatRooms() {
	fetch('/api/chat/chatRooms/', {
		method: 'GET', 
	})
	.then(response => {
		if (response.ok) {
	    	return response.json();
	    } else {
	        throw new Error('Network response was not ok');
	    }
	})
	.then((chatRooms) => {
		console.log(chatRooms);
		const chatEvent = new CustomEvent("chatRoomListUi", { detail: { chatRooms } });
    	document.dispatchEvent(chatEvent);
		/** 
		// 이거 잘 됨
		
		
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
	        
	
	    });*/
	})
	.catch(error => {
	    console.error(error);
	});
		
}

let currentPage = 0; // 현재 페이지
const pageSize = 100; // 한 번에 가져올 메시지 수
let isLoading = false; // 로딩 중인지 여부
let hasMoreData = true; // 더 가져올 데이터가 있는지 여부

function getChatMessages() {

// 이 때 채팅방 초기화 해야 됨

	const chatRoomId = sessionStorage.getItem("chatRoomId");

	fetch('/api/chat/messages?chatRoomId=' + chatRoomId, {
		method: "GET",
		headers: {
			"Content-Type": "application/json"
		}
	})
	.then(response => {
		if (!response.ok) {
			throw new Error("Failed to fetch messages");
		}

		return response.json(); 
	})
	.then(chats => {
		console.log(chats);
		
		appendMessages(chats);
		/**
		if (!messages || messages.length === 0) {
			 hasMoreData = false; // 더 이상 데이터가 없음
		} else {
			currentPage++; // 페이지 번호 증가
			appendMessages(messages); // 메시지 추가
		}
		*/
	})
	.catch(error => {
		console.error("Error loading messages:", error);
	});
}


document.getElementById("chatContainer").addEventListener("scroll", function () {
    const container = document.getElementById("chatContainer");

    // 스크롤이 상단에 도달했을 때 (더 많은 데이터를 가져오기 위해)
    if (container.scrollTop === 0 && hasMoreData && !isLoading) {
        loadMoreMessages();
    }
});




function sendChat() {  
	let chatMessage = $("#chatMessage").val();
	
	if(chatMessage.replace(/\s/g,"") == "") {
		return false;
	}
			
	var chatRoomId = sessionStorage.getItem("chatRoomId");			
	var messageType = "MESSAGE";

	if (chatMessage && socket.readyState === WebSocket.OPEN) {
		var chatObject = {
			chatRoomId: chatRoomId,
			type: messageType,
			message: chatMessage
		};

		var chatJson = JSON.stringify(chatObject); // 객체를 JSON 문자열로 변환
		socket.send(chatJson); // JSON 문자열을 WebSocket을 통해 전송
		
		$("#chatMessage").val(""); // 입력 필드를 비움
	}
}

let previousDate = null;

function appendMessages(chats) {
    let chatList = document.getElementById("chatList");
	chatList.innerHTML = "";
	
    chats.forEach((chat) => {
		let chatDiv = document.createElement("div");
		let chatSpan = document.createElement("span");
		let sendTimeSpan = document.createElement("span");
		let dateSpan = document.createElement("span");
		
		if(chat.mine){
			chatDiv.classList.add("chat", "myChat");
		} else {
	    	chatDiv.classList.add("chat", "otherChat");
	  	}
	  	
	  	let [date, time] = chat.chatDate.split(" ");
  
  		console.log("date : " + date);
  		console.log("time : " + time);
		// 날짜가 바뀌었으면 출력
		if (date !== previousDate ) {
			previousDate = date; // 현재 날짜 저장
			
			dateSpan.textContent = date;
		}
	  	
	  	chatSpan.textContent = chat.message;
	  	sendTimeSpan.textContent = time;
	  	
	  	chatDiv.appendChild(chatSpan);
	  	chatDiv.appendChild(sendTimeSpan);
	  	chatDiv.appendChild(dateSpan);
	  	chatList.prepend(chatDiv); // 이전 메시지를 상단에 추가
    });
}

function displayMessage(data){
	let chatList = document.getElementById("chatList");
	let chatDiv = document.createElement("div");
	let chatSpan = document.createElement("span");

	if(data.mine){
		chatDiv.classList.add("chat", "myChat");
	} else {
	    chatDiv.classList.add("chat", "otherChat");
	}
	
	let [date, time] = data.chatDate.split(" ");
  
	// 날짜가 바뀌었으면 출력
	if (date !== previousDate && date == null) {
		previousDate = date; // 현재 날짜 저장
	}
	  
    chatSpan.textContent = data.message;    
    chatDiv.appendChild(chatSpan);
    chatList.prepend(chatDiv);
}

