function toggleUserMenu() {
	$(".user_toggle").hide();
	
	setUserToggle(window.MY_ID);
}

// 메인 페이지에서 사용할 UI 업데이트 함수
function setUserToggle(currentUserId) {
    $(".userName").each(function() {
        let userId = $(this).attr("data-userId");

        // 현재 로그인한 사용자와 아이디 비교
        if (userId !== currentUserId && userId) {
            $(this).addClass("cursor-pointer");

            let userToggleDiv = $("<div>").addClass("user_toggle absolute border border-radius-7px bColor_fff text-alignC");
            userToggleDiv.css('width', '9rem');
            
            let ul = $("<ul>");
            let li1 = $("<li>").addClass("pd-top1");
            let button1 = $("<button>").attr("type", "button")
                .addClass("otherPosts font-weight-bold font-color-blue mypage_font border-none bColor_fff")
                .text("작성한 글");
            li1.append(button1);

            let li2 = $("<li>").addClass("pd-top1");
            let button2 = $("<button>").attr("type", "button")
                .addClass("send userToggle_msg font-weight-bold font-color-blue mypage_font border-none bColor_fff")
                .text("쪽지");
            li2.append(button2);

            let li3 = $("<li>").addClass("pd-top1");
            let button3 = $("<button>").attr("type", "button")
                .addClass("userToggle_chat font-weight-bold font-color-blue mypage_font border-none bColor_fff")
                .text("채팅");
            li3.append(button3);

            // ul에 li 항목 추가
            ul.append(li1).append(li2).append(li3);

            // 생성된 UL을 DIV에 추가
            userToggleDiv.append(ul);

            // 동적으로 생성된 내용을 화면에 추가
            $(this).next(".userNameToggle").append(userToggleDiv);
            $(this).next(".userNameToggle").find(".user_toggle").hide();
        }
    });
}

// 닉네임 클릭하면 토글 열기
$(document).on('click', '.userName', function(e) {
	
	e.preventDefault();
	e.stopPropagation();
	e.stopImmediatePropagation(); // 모든 이벤트 전파 막기
	
	var $toggle = $(this).siblings('.userNameToggle').find('.user_toggle'); // 현재 클릭한 닉네임에 연결된 토글 요소
	if ($toggle.is(':visible')) {
		// 이미 열려 있으면 닫기
		$toggle.hide();
	} else {
		// 모든 토글 숨기고 현재 클릭한 토글만 열기
		$(".user_toggle").hide(); // 다른 토글 요소 숨기기
		$toggle.show(); // 클릭한 요소의 토글 열기
	}
	
	return false;
});
    
	
/* 
$(document).on('click', '.userName', function(e) {
	$(".user_toggle").hide();
	$(this).siblings('.userNameToggle').find('.user_toggle').toggle();
	e.stopPropagation();
});*/

// 토글된 div 클릭 시 이벤트 전파 막기
$(document).on('click', '.user_toggle', function(e) {
	e.stopPropagation(); // 토글된 div를 클릭해도 문서의 클릭 이벤트가 발생하지 않도록 전파 막기
});

// 문서의 다른 부분을 클릭하면 토글 닫기
$(document).click(function() {
	$('.user_toggle').hide(); // 토글을 닫음
});



$(document).on('click', '.otherPosts', function() {
	//let user_id = $(this).closest('.user_toggle').data('userId');
	
	let userId = $(this).closest('.userNameToggle').prev('.userName').attr("data-userid");

	location.href = "/mypage/written?boardType=community&user_id=" + userId;
});

$(document).on('click', '.send', function(){
	let userId = $(this).closest('.userNameToggle').prev('.userName').attr("data-userid");
	
	var url = "/message/send/" + userId;
		var option = "width=500, height=370, top=10, left=10";
		var name = "message";
				
		window.open(url, name, option);
})


$(".userToggle_msg").click(function(){
	
})

$(document).on('click', '.userToggle_chat', function() {
	let chatContainer = document.getElementById("chatContainer");
	let chatRoom = document.getElementById("chatRoom");
	
	let userId = $(this).closest('.userNameToggle').prev('.userName').attr("data-userid");

	const chatEvent = new CustomEvent("openChat", { detail: { userId } });
    document.dispatchEvent(chatEvent);
/* 
	if (chatContainer.style.display === "none" || chatContainer.style.display === "") {
		chatContainer.style.display = "flex";
	}
	
	let userName = $(this).closest('.userNameToggle').prev('.userName').text();
		window.selectedUserName = userName;

		console.log(userName);
		chatRoom.style.display = "flex";*/
})

