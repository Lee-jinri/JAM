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

@media screen and (max-width: 768px) {
  
  #header_name{
  	font-size: 12px;
  }
}

header {
    width: 100%;
    box-shadow: 0 2px 2px rgba(0, 0, 0, 0.05), 0 1px 0 rgba(0, 0, 0, 0.05);
}

.header{
	padding: 10px 20px;
}
.header-container {
    display: flex;
    align-items: center;
    justify-content: space-between; 
    max-width: 114rem;
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
    margin-left: auto; 
    display: flex;
    align-items: center;
    gap: 10px;
}


#mobile-menu-btn{
	display: none;
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
    padding-top: 12px;
}

.header-board-a {
    position: relative;
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

#mobileMenu{
	display: none;
}
#mobile-menu-btn{
	border: none;
    background-color: #fff;
}
/* 모바일 반응형 */
@media screen and (max-width: 900px) {
	#headerNav{
		display:none;
	}
	
	.auth-buttons{
		min-width: 0;
	}
	
	.logoutA{
		font-size: 10px ;
	}
	
	.logoutDiv {
		margin: 0;
	}
	
	#mobile-menu-btn{
		display: block;
	}
	#mobileMenu{
		display: none;
	    flex-direction: column;
	    background-color: white;
	    position: absolute;
	    top: 62px; 
	    width: 100%;
	    z-index: 1000;
	    border-top: 1px solid #ccc;

	    animation: fadeDown 0.2s ease-in-out;
	}
  
	#mobileMenu a {
		text-align: center;
		padding: 14px 20px;
		margin: 4px 12px;
		font-size: 14px;
		font-weight: 500;
		color: #333;
		border-radius: 8px;
		border-bottom: 1px solid #eee;
		transition: background-color 0.2s ease, color 0.2s ease;
	}
	  
	#mobileMenu a:hover {
		background-color: #f9f9f9;
	}
	
	@keyframes fadeSlide {
		from {
			opacity: 0;
			transform: translate(-50%, -10px);
		}
		to {
			opacity: 1;
			transform: translate(-50%, 0);
		}
	}
}

</style>
<script>
// FIXME : XSS 3원칙 적용할 것
	$(function() {
		if(window.MY_NAME) {
			// 헤더에 닉네임과 로그아웃 버튼 추가
            headerName = $('#header_name');
            headerName.html(window.MY_NAME + "님");
            
            $("#loggedOutDiv").css("display", "none");
            $("#loggedInDiv").css("display", "flex");
            
            $("#written").attr("data-userId", window.MY_ID);
        }
		
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
		
		$(document).mouseup(function(e) {
			const $mypage = $(".mypage_toggle");

			// 클릭한 대상이 .mypage_toggle 바깥일 때
			if (!$mypage.is(e.target) && $mypage.has(e.target).length === 0) {
				$mypage.hide();
			}
		});
  
		$("#account").click(function() {
			$(location).attr('href', '/mypage/account');
		});
		
		$("#written").click(function(){
			let user_id = $(this).attr("data-userId");
			$(location).attr('href','/mypage/written?user_id='+user_id);
		})
		
		$("#myStore").click(function(){
			$(location).attr('href','/fleaMarket/my?view=store');
		})	
		
		$("#myFavorite").click(function(){
			$(location).attr('href','/mypage/favorite')
		})
		
		$("#msg").click(function(){
			$(location).attr('href','/message/receiveMessage')
			//getUserIDAndRedirect('/message/receiveMessage');
		})
		
		$("#chat").click(function(){
			sessionStorage.removeItem("chatRoomId");
			location.href = '/chat';
		})
		
		
		$(".mypage_toggle").hide();
		
		$("#header_name").click(function() {
			$('.mypage_toggle').toggle();
		})
		
		
		$("#adminBtn").click(function(){
			$(location).attr('href','/admin/admin');
		})
		
		$("#mobile-menu-btn").click(function(){
			const $menu = $("#mobileMenu");
			
			if ($menu.is(":visible")) {
				$menu.hide(); 
			} else {
				$menu.css("display", "flex"); 
			}
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
	<div class="header">
		<div class="header-container">
			<div class="nav-container">    
				<div class="header-left">
					<button id="mobile-menu-btn"><i class="fa-solid fa-bars"></i></button>
					<a href="/" class="flex"> <img class="nav-icon"
						src="/resources/include/images/JAM_icon3.png" alt="JAM">
					</a>
				</div>
		
				<!-- 가운데: 네비게이션 메뉴 -->
				<nav id="headerNav" class="nav-menu items-center">
					<a href="/community/boards"
						class="header-board-a font-color-blue font-weight-bold padding-10">커뮤니티</a>
					<a href="/roomRental/boards"
						class="header-board-a font-color-blue font-weight-bold padding-10">연습실</a>
					<a href="/fleaMarket/board"
						class="header-board-a font-color-blue font-weight-bold padding-10">중고악기</a>
					<a href="<c:url value='/jobs/boards'/>" target="_blank" rel="noopener noreferrer"
						class="header-board-a font-weight-bold padding-10" style="color: #0B7285;">
							Jobs
							<i class="fa-solid fa-arrow-up-right-from-square" style="color: #969696;font-size: 13px;"></i>
					</a>
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
					class=" font-weight-bold cursor-pointer mr-1 font-color-gray"
					id="header_name"></span>
	
				<div class="mypage_toggle absolute border border-radius-7px">
					<ul>
						<li id="account" class="mypage_toggle_btn cursor-pointer"><span
							class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">계정
								정보</span></li>
						<li id="written" class="mypage_toggle_btn cursor-pointer"><span
							class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">작성한 글</span>
								</li>
						<li id="myStore" class="mypage_toggle_btn cursor-pointer"><span
							class="font-weight-bold font-color-blue mypage_font border-none bColor_fff">내 상점</span>
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
	</div>
	<div id="mobileMenu" class="mobile-nav" >
			<a href="/community/boards">커뮤니티</a>
			<a href="/roomRental/boards">연습실</a>
			<a href="/fleaMarket/boards">중고악기</a>
			<a href="/jobs/board">Jobs</a>
		</div>
</header>
