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
<style>
.mypage_toggle_btn:hover {
    background-color: whitesmoke;
}

.mypage_toggle_btn:hover span {
    background-color: whitesmoke;
}

.mypage_toggle_btn {
    height: 3rem;
}
	

</style>
<script>
	$(function() {
		
		/* 소셜 로그인 성공 후 파라미터로 jwt토큰을 받습니다. */
		var search = location.search;
		var params = new URLSearchParams(search);
		
		fetch('/api/member/decode-token')
		.then(response =>{
			if (!response.ok) {
	            throw new Error('JWT 디코딩 실패');
	        }
	        return response.json(); 
		})
		.then(data => {
			
			let userName = data.userName;
        	let role = data.role;
        	
        	if (data && userName && role) {
        		// 헤더에 닉네임과 로그아웃 버튼 추가
                headerName = $('#header_name');
                headerName.html(data.userName + "님");
                
                $("#loggedOutDiv").css("display", "none");
                $("#loggedInDiv").css("display", "flex");
	                
                // 로그인 한 사용자가 관리자라면 헤더에 관리자 버튼을 추가
                if(role.includes("ROLE_ADMIN")){
                	let headerNav = document.getElementById("headerNav");
                	
                	let anchor = document.createElement("a");
                	anchor.href = "/admin/admin"; 
                	anchor.className = "font-color-blue font-size-5 font-weight-bold padding-10"; // 클래스 설정
                	anchor.textContent = "관리자 페이지"; 

                	// 요소 추가
                	headerNav.appendChild(anchor);
                }
            }
	    })
	    .catch(error => {
	        console.error("에러 발생:", error);
	    });
		
		// 로그아웃
		$("#logout").click(function() {
			
			fetch('/api/member/logout', {
	            method: 'GET'
	        })
	        .then(response => {
	        	return response.text();
	        })
	        .then(data => {
	        	if (data === "Logout success") {
	                // 로그아웃 후 메인 페이지로 리다이렉트
	                $(location).attr('href', "/");
	            } else {
	                throw new Error('Unexpected response message: ' + data);
	            }
	        })
	        .catch(error => {
	            console.error('로그아웃 중 오류 발생 : ', error);
	        });
		})
		
		$("#account").click(function() {
			$(location).attr('href', '/member/account');
		});
		
		$("#myWrite").click(function(){
			$(location).attr('href','/community/comPosts?type=my');
		})
		
		$("#myFavorite").click(function(){
			$(location).attr('href','/member/favorite')
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

</script>
</head>
	<header>
		<div class="flex border-bottom justify-center">
			<div class="flex items-center height75 justify-between" style="width: 70%;">
				<div class="flex">
					<div>
						<a href="/" class="flex"> <img class="nav-icon"
							src="/resources/include/images/JAM_icon3.png" alt="JAM">
						</a>
					</div>
					<div class="flex justify-between">
						<nav id="headerNav" class="flex items-center">
							<a href="/community/boards"
								class="font-color-blue font-size-5 font-weight-bold padding-10">커뮤니티</a>
							<a href="/roomRental/boards"
								class="font-color-blue font-size-5 font-weight-bold padding-10">합주실&연습실</a>
							<a href="/fleaMarket/boards"
								class="font-color-blue font-size-5 font-weight-bold padding-10">중고악기</a>
							<a href="/job/boards"
								class="font-color-blue font-size-5 font-weight-bold padding-10">Jobs</a>
						</nav>
					</div>	
				</div>
				<div>
					<div class="flex justify-right join_button">
						<!-- 로그인 하지 않음 -->
						<div id="loggedOutDiv" >
							<div class="inline-block mr-1 logoutDiv text-alignC">
								<a href="/member/login" class="font-color-gray justify-right logoutA">로그인</a>
							</div>
							<div class="inline-block logoutDiv text-alignC">
								<a href="/member/join" class="font-color-gray justify-right logoutA">회원가입</a>
							</div>
						</div>
						<!-- 로그인 했을 때-->
						<div id="loggedInDiv" class="flex items-center" style="display: none;">
							<div id="adminPage"></div>
							<span class="font-size-4 font-weight-bold cursor-pointer mr-1 font-color-gray" id="header_name"></span>
		
							<div class="mypage_toggle absolute border border-radius-7px">
								<ul>
									<li id="account" class="mypage_toggle_btn cursor-pointer">
										<span class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">계정 정보</span>
									</li>
									<li id="myWrite" class="mypage_toggle_btn cursor-pointer">
										<span class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">작성한 글</span>
									</li>
									<li id="myFavorite" class="mypage_toggle_btn cursor-pointer">
										<span class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">북마크</span>
									</li>
									<li id="msg" class="mypage_toggle_btn cursor-pointer">
										<span class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">쪽지</span>
									</li>
									<li id="chat" class="mypage_toggle_btn cursor-pointer">
										<span class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">채팅</span>
									</li>
								</ul>
							</div>
							
							<button type="button" id="logout" class="logoutA mr-2 bColor_fff font-color-blue">로그아웃</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</header>
