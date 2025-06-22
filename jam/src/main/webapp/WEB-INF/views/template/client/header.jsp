<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>
	
<head>
<meta charset="utf-8">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<style>
header {
    width: 100%;
    box-shadow: 0 2px 2px rgba(0, 0, 0, 0.05), 0 1px 0 rgba(0, 0, 0, 0.05);
    padding: 10px 20px;
}


.header-container {
    display: flex;
    align-items: center;
    justify-content: justify-between; 
    max-width: 114rem;
    margin: 0 auto;
    padding: 0 20px;
    height: 42px;
}

.nav-container {
    display: flex;
    justify-content: flex-start;
    align-items: center;
    gap: 3rem;
    flex-grow: 1;
}

.nav-menu {
    display: flex;
    align-items: center;
    gap: 15px;
}

.account-container {
    margin-left: auto; /* 오른쪽 끝으로 정렬 */
    display: flex;
    align-items: center;
    gap: 10px;
}

/* 로고 + 햄버거 아이콘 */
.header-left {
    display: flex;
    align-items: center;
    gap: 15px;
}


/* 로그인 버튼 정렬 */
.auth-buttons {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-left: 0;
    min-width: 170px;
}


.mypage_toggle_btn:hover {
    background-color: whitesmoke;
}

.mypage_toggle_btn:hover span {
    background-color: whitesmoke;
}

.mypage_toggle_btn {
    height: 3rem;
}

.header-board-a {
    position: relative;
    font-size: 15px;
    font-weight: 600;
    transition: color 0.3s ease-in-out;
}

.header-board-a:hover{
	color: #545051;
}
.header-board-a::after {
    content: "";
    position: absolute;
    left: 50%;
    bottom: -3px; /* 글자 아래에 여백 살짝 */
    width: 0;
    height: 3px;
    background: #EAECEF;
    transition: width 0.3s ease-in-out, left 0.3s ease-in-out;
}

.header-board-a:hover::after {
	color: #545051;
    width: 100%;
    left: 0;
}



.menu-toggle {
    display: none; 
    font-size: 24px;
    background: none;
    border: none;
    cursor: pointer;
}

/* 모바일 반응형 */
@media screen and (max-width: 900px) {
    .header-container {
        justify-content: space-between; /* 로고, 내비, 계정 정렬 */
    }

    .nav-menu {
        display: none; /* 햄버거 메뉴를 위해 숨김 */
    }

    .menu-toggle {
        display: block;
    }
}

</style>
<script>
	$(function() {
		/* 소셜 로그인 성공 후 파라미터로 jwt토큰을 받습니다. */
		var search = location.search;
		var params = new URLSearchParams(search);
		
		fetch('/api/member/me')
		.then(response =>{
			if (!response.ok) {
	            throw new Error('사용자 정보 가져오기 실패');
	        }
	        return response.json(); 
		})
		.then(data => {
			let userName = data.userName;
        	
        	if (data && userName) {
        		// 헤더에 닉네임과 로그아웃 버튼 추가
                headerName = $('#header_name');
                headerName.html(data.userName + "님");
                
                $("#loggedOutDiv").css("display", "none");
                $("#loggedInDiv").css("display", "flex");
	            
                $("#written").attr("data-userId", data.userId);
                
                /*
                //TODO: 사용자 정보 페이지에서 jwt토큰 검증하고 관리자 버튼 만들면 좋을 것 같아요옹
                // 로그인 한 사용자가 관리자라면 헤더에 관리자 버튼을 추가
                if(role.includes("ROLE_ADMIN")){
                	let headerNav = document.getElementById("headerNav");
                	
                	let anchor = document.createElement("a");
                	anchor.href = "/admin/admin"; 
                	anchor.className = "font-color-blue font-size-5 font-weight-bold padding-10"; // 클래스 설정
                	anchor.textContent = "관리자 페이지"; 

                	// 요소 추가
                	headerNav.appendChild(anchor);
                }*/
            }
	    })
	    .catch(error => {
	        console.error("에러 발생:", error);
	    });
		
		// 로그아웃
		$("#logout").click(function() {
			fetch('/api/member/loginType')
			.then(res => {
				if(res.status == 200) return res.text();
				else if(res.status == 401) {
					alert('로그인이 만료되었습니다. 메인 페이지로 이동합니다.');
			        $(location).attr('href', "/");
			        return;
				} else {
			    	throw new Error('응답 상태: ' + res.status);
			    }
			})
			.then(loginType => {
				logout(loginType);
			})
			.catch(error => {
				console.error("오류:", error);
				alert('알 수 없는 오류가 발생했습니다.');
			});
		})
		
		$("#account").click(function() {
			$(location).attr('href', '/mypage/account');
		});
		
		$("#written").click(function(){
			let user_id = $(this).attr("data-userId");
			$(location).attr('href','/mypage/written?user_id='+user_id);
		})
		
		$("#myFavorite").click(function(){
			$(location).attr('href','/mypage/favorite')
		})
		
		$("#msg").click(function(){
			$(location).attr('href','/message/receiveMessage')
			//getUserIDAndRedirect('/message/receiveMessage');
		})
		
		$("#chat").click(function(){
			$("#chatContainer").toggle();
			$('.mypage_toggle').toggle();
			
			const openChatListEvent = new CustomEvent("openChatList");
			document.dispatchEvent(openChatListEvent);
			/*
			window.open('/chat/chatrooms', "popupWindow", "width=600,height=400,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,directories=no,status=no");
			$(location).attr('href', '/chat/chatrooms');*/
		})
		
		
		$(".mypage_toggle").hide();
		
		$("#header_name").click(function() {
			$('.mypage_toggle').toggle();
		})
		
		
		$("#adminBtn").click(function(){
			$(location).attr('href','/admin/admin');
		})
		
	})

function logout(loginType){
	let uri;
	
	if (loginType === 'kakao') {
		uri = '/oauth/kakao/logout';
	}else if(loginType === 'naver'){
		uri = '/oauth/naver/logout';	
	}else {
		uri = '/api/member/logout';
	}
			
	fetch(uri, {
	    method: 'POST'
    })
    .then(() => {
    	location.reload();
     })
    .catch(error => {
        console.error('로그아웃 중 오류 발생 : ', error);
    });
}
</script>
</head>
<header>
	<div class="header-container ">
		<div class="nav-container">
			<div class="header-left">
				<button class="menu-toggle" id="menuToggle">
					<i class="fa-solid fa-bars"></i>
				</button>
				<a href="/" class="flex"> <img class="nav-icon"
					src="/resources/include/images/JAM_icon3.png" alt="JAM">
				</a>
			</div>
	
			<!-- 가운데: 네비게이션 메뉴 -->
			<nav id="headerNav" class="nav-menu items-center">
				<a href="/community/boards"
					class="header-board-a font-color-blue font-size-5 font-weight-bold padding-10">커뮤니티</a>
				<a href="/roomRental/boards"
					class="header-board-a font-color-blue font-size-5 font-weight-bold padding-10">연습실</a>
				<a href="/fleaMarket/boards"
					class="header-board-a font-color-blue font-size-5 font-weight-bold padding-10">중고악기</a>
				<a href="/job/boards"
					class="header-board-a font-color-blue font-size-5 font-weight-bold padding-10">Jobs</a>
			</nav>
		</div>
		
		<!-- 오른쪽: 로그인/회원가입 -->
		<div id="loggedOutDiv" class="auth-buttons">
			<div class="inline-block mr-1 logoutDiv text-alignC">
				<a href="/member/login"
					class="font-color-gray justify-right logoutA">로그인</a>
			</div>
			<div class="inline-block logoutDiv text-alignC">
				<a href="/member/join" class="font-color-gray justify-right logoutA">회원가입</a>
			</div>
			
		</div>

		<!-- 로그인 했을 때 -->
		<div id="loggedInDiv" class="flex items-center" style="display: none;">
			<div id="adminPage"></div>
			<span
				class="font-size-4 font-weight-bold cursor-pointer mr-1 font-color-gray"
				id="header_name"></span>

			<div class="mypage_toggle absolute border border-radius-7px">
				<ul>
					<li id="account" class="mypage_toggle_btn cursor-pointer"><span
						class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">계정
							정보</span></li>
					<li id="written" class="mypage_toggle_btn cursor-pointer"><span
						class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">작성한 글</span>
							</li>
					<li id="myFavorite" class="mypage_toggle_btn cursor-pointer">
						<span
						class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">북마크</span>
					</li>
					<li id="msg" class="mypage_toggle_btn cursor-pointer"><span
						class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">쪽지</span>
					</li>
					<li id="chat" class="mypage_toggle_btn cursor-pointer"><span
						class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">채팅</span>
					</li>
				</ul>
			</div>

			<button type="button" id="logout"
				class="logoutA mr-2 bColor_fff font-color-blue">로그아웃</button>
		</div>
	</div>
</header>
