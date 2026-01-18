$(function(){
	let chatContainer = document.getElementById("chatContainer");
	let chatDrag = document.getElementById("chatDrag");
	let offsetX = 0;
	let offsetY = 0;
	let isDragging = false;
	
	
	chatDrag.addEventListener("mousedown", (e) => {
		isDragging = true;
		offsetX = e.clientX - chatContainer.offsetLeft;
		offsetY = e.clientY - chatContainer.offsetTop;
	});

	document.addEventListener("mousemove", (e) => {
		if (isDragging) {
			// 위치 업데이트 (페이지 전체에서 자유롭게 이동 가능)
			chatContainer.style.left = `${e.clientX - offsetX}px`;
			chatContainer.style.top = `${e.clientY - offsetY}px`;
		}
	});

	document.addEventListener("mouseup", () => {
		isDragging = false;
	});
		
	let chatRoomList = document.getElementById("chatRoomList");
	let chatRoom = document.getElementById("chatRoom");
	
	
	$(".openChat").click(function(){
		chatUi();
	})
	/**
	document.addEventListener("DOMContentLoaded", () => {
		const backBtn = document.getElementById("chatBack");
	
		// 버튼 클릭 이벤트 등록
		backBtn.addEventListener("click", () => {
			console.log("backBtn clicked!");
			alert("버튼 클릭");
		});
		
		// user Toggle에서 채팅 클릭 시 스타일 변경 감지
		const observer = new MutationObserver((mutations) => {
			mutations.forEach((mutation) => {
				if (mutation.attributeName === "style") {
					const display = window.getComputedStyle(chatRoom).display;
					if (display === "flex") {
						chatRoomList.style.display = "none";
						let userName = window.selectedUserName;
						
						$("#targetUserName").text(userName);
						initiateChat(userName);
						initWebSocket();
					}
				}
			});
		});
		
		observer.observe(chatRoomList, { attributes: true });
		observer.observe(chatRoom, { attributes: true });
	});  */
})



function openChat(userName){
	
}


function openChatList(){
	
}

// 채팅을 클릭하면 div 보이게 하고 채팅 이벤트 실행
function openChatPage(pageId, userName) {
  chatContainer.style.display = "block";
  chatRoomList.classList.add("hidden");
  chatRoom.classList.add("hidden");

  document.getElementById(pageId).classList.remove("hidden");
  
  if(userName != null) console.log(userName);
}

/*
function initUi(){
	let chatContainer = document.getElementById("chatContainer");
	let chatDrag = document.getElementById("chatDrag");
	
	chatContainer.style.left = "75%"; // 화면 오른쪽에서 20% 떨어진 위치
    chatContainer.style.top = "35%"; // 화면 아래에서 20% 떨어진 위치

	let offsetX = 0;
	let offsetY = 0;
	let isDragging = false;
	
	chatDrag.addEventListener("mousedown", (e) => {
	    isDragging = true;
	    offsetX = e.clientX - chatContainer.offsetLeft;
	    offsetY = e.clientY - chatContainer.offsetTop;
	});
	
	document.addEventListener("mousemove", (e) => {
	    if (isDragging) {
	        // 위치 업데이트 (페이지 전체에서 자유롭게 이동 가능)
	        chatContainer.style.left = `${e.clientX - offsetX}px`;
	        chatContainer.style.top = `${e.clientY - offsetY}px`;
	    }
	});
	
	document.addEventListener("mouseup", () => {
	    isDragging = false;
	});
}*/

/*
let chatContainer = document.getElementById("chatContainer");
let chatDrag = document.getElementById("chatDrag");


document.addEventListener("DOMContentLoaded", () => {
    initializeEventListeners();
    //loadUserChatRooms(window.chatUserId);
});

function initializeEventListeners(){
	
} */


// 나의 채팅 목록 보기
document.addEventListener("openChatList", () => {
	chatListUi();
    /*chatContainer.style.display = "block";
    chatRoomList.style.display = "block";
    chatRoom.style.display = "none";*/
    
});

// 특정 사용자와 채팅 
document.addEventListener("openChat", (event) => {
	/** chatContainer.style.display = "block";
	chatRoomList.style.display = "none";
	chatRoom.style.display = "block";
	*/
    const { userName } = event.detail;
    
    console.log(userName);
    $("#chatTargetUserName").text(userName);
    chatUi();
});

document.addEventListener("chatRoomListUi", (event) => {
	let { chatRooms } = event.detail;
	
	
	console.log("chatRoomListUi");
	console.log("chatRooms : " +chatRooms);
	
	// 이거 잘나옴
	chatRooms.forEach((room, index) => {
		let $element = $("#chat-template").clone().removeAttr('id');
		
		let message = room.message;
		let participant = room.participant;
		console.log(message);
		
		/**
		<div id="chat-template" class="openChat">
	        <span class="userName"></span>
	        <span class="listMessage"></span>
	        <span class="timeStamp"></span>
        </div> 
        console.log(`Room ${index + 1}:`);
        console.log(`Message ID: ${room.chatRoomId}`);
        console.log(`Message: ${room.message}`);
        console.log(`Sender ID: ${room.senderId}`);
        console.log(`Receiver ID: ${room.receiverId}`);*/
    });
})

function chatListUi(){
    chatContainer.style.display = "block";
    chatRoomList.style.display = "block";
    chatRoom.style.display = "none";
    
    
}

function chatUi(){
	chatContainer.style.display = "block";
	chatRoomList.style.display = "none";
	chatRoom.style.display = "block";
}
