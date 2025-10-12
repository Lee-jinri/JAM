<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
	
<head>
<meta charset="utf-8">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">

<style>
header {
    width: 100%;
    box-shadow: 0 2px 2px rgba(0, 0, 0, 0.05), 0 1px 0 rgba(0, 0, 0, 0.05);
    top:0; 
	background:rgba(255,255,255,.92); 
	backdrop-filter:saturate(180%) blur(8px); 
	z-index:1030; 
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
    margin: 0 60px;
}

.nav-container {
    display: flex;
    align-items: center;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 15px;
}

.jobs-nav{
	display: flex;
	align-items: center;
	margin-bottom: 0;
}

.jobs-nav li{
	display: flex;
    align-items: center;
    font-size: 14px;
}
/* 로그인 버튼 정렬 */
.auth-buttons {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-left: 0;
    max-width: 110px;
    min-width: auto;
    margin-right: 15px;
}

.auth-buttons a{
	color: #888888;
    font-size: 15px;
    font-weight: 400;
}

#mobileMenu{
	display: none;
}

.search-container {
	display: flex;
	align-items: center;
	background-color: white;
	border: 1px solid #ddd;
	border-radius: 9999px; /* pill shape */
	padding: 8px 8px;
	width: 100%;
	max-width: 600px;
	box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
	height: 53px;
}

.search-icon {
	color: #333;
	font-size: 20px;
}

.search-input {
	border: none;
	flex: 1;
	outline: none;
	font-size: 15px;
}

.search-div {
	max-width: 500px; 
	min-width: 345px;
	width: 100%;    
	margin: 0 15px;
	padding: 3px 8px;
	border: 2px solid #0F2D4A;
}

.search-bar-wrapper {
	display: flex;
	gap: 8px; /* 요소 간 간격 */
	width: 100%;
	max-width: 800px; /* 포지션/지역과 동일한 폭 */
}

.search-bar-wrapper select {
	flex: 0.2; /* select는 전체의 20%만 차지 */
	min-width: 100px; /* 너무 작아지는 거 방지 */
}

.search-bar-wrapper input {
	flex: 1; /* input은 나머지 공간 차지 */
	min-width: 0;
	box-sizing: border-box;
}

.search-bar-wrapper .glass_icon {
	cursor: pointer;
	font-size: 18px;
	align-self: center;
}

.main-favicon{
	border: .5px solid #e8e8e8;
    border-radius: 8px;
    margin-right: 4px;
    margin-left: 25px;
    width: 24px;
}

.brand-link{
	display:inline-flex;         
	flex-direction:row;
	align-items:center;   
	white-space:nowrap;           
	color: #565656;
	font-size:14px;
	font-weight:500;
}
.main-favicon{
	width:24px; 
	height:24px;
	display:block;                
	flex:0 0 auto;
}

/* 드롭다운 */
.menu { position: relative; }
.menu__toggle{
	display:inline-flex; 
	align-items:center; gap:6px;
	padding:6px 10px; 
	border:0; 
	background:transparent; 
	cursor:pointer; 
	border-radius:8px;
	padding:8px 12px; 
	border-radius:9999px;
}
.menu__toggle .fa-caret-down{ transition:transform .15s ease; }
.menu__toggle[aria-expanded="true"] .fa-caret-down{ transform:rotate(180deg); }
.menu__toggle:hover{ background:#f6f7f8; }


.menu__list{
	position:absolute; 
	right:0; 
	top:calc(100% + 6px);
	min-width:180px; 
	margin:0; 
	list-style:none;
	background:#fff; 
	border:1px solid #e5e7eb; 
	border-radius:10px;
	z-index:1000;
	padding:8px 0; 
	box-shadow:0 12px 28px rgba(0,0,0,.12), 0 2px 4px rgba(0,0,0,.06);
}
.menu__list[hidden]{ display:none; }
.menu__list a{
	display:block; 
	padding:10px 14px; 
	text-decoration:none; 
	color:#111;
	width: 100%;
	padding:10px 14px;
}
.menu__list a:hover{
	background:#DAEAED; 
	color:#0F2D4A; 
}
.userTypeSpan{
	display: inline-block;
    padding: 4px 10px;
    border-radius: 12px;
    color: white;
    font-size: 0.85rem;
    font-weight: 600;
    letter-spacing: 0.5px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.typeUser{
    background: linear-gradient(135deg, #ffd400, #ffdd77);
}

.typeCompany{
    background: linear-gradient(135deg, #4facfe, #00f2fe);
}

@media (max-width: 691px) {
	.header{padding:0;}
	.nav-icon {
    	width: 35px;
    }

	.header-container { 
		margin: 0 10px;
		height: 30px;
	}

	.search-div {
		min-width: 0;
		 /*flex-basis: 240px; max-width: 280px;*/ 
		max-width: 240px;
		width: 100%;
		margin: 0 8px
		padding: 0 8px;
		border: 1px solid #0F2D4A; 
	}
	
	.main-favicon { 
		margin: 0; 
		width: 12px;
    	height: 12px;
	}
	
	.brand-link span,
	.brand-link {
		font-size: 7px;
		margin-left: 2px;
	}
	#header_name, .userTypeSpan {
        font-size: 7px;
    }
    .userTypeSpan {
        padding: 3px 5px;
    }
	.menu{
		margin-right: 10px;
	}
	.menu__list{
		min-width: 100px;
		padding: 0;
	}
	.menu__list a {
	    padding: 5px 13px;
	    font-size: 8px;
	}
	.menu__toggle{
		padding: 3px 3px;
		margin-right: 6px;
	}
	.search-bar-wrapper .glass_icon{font-size:8px; margin: 3px 0;}
	.search-input {font-size: 8px;}
	.search{margin:0;}
	.fa-caret-down{font-size:6px;}
	.auth-buttons a{
    	font-size: 8px;
    }
}

@media (min-width: 691px) and (max-width: 1024px) {
	.auth-buttons { 
		min-width: 0; 
	}
	.nav-icon {
    	width: 65px;
    }
	.header-container { 
		margin: 0 12px; 
		padding: 0 12px; 
	}
	#header_name, .userTypeSpan{font-size:12px;}
	.brand-link span{font-size: 12px;}
	.search-div {
		/*flex-basis: 420px; */
		max-width: 350px;
    	min-width: 250px;
    	padding: 3px 8px;
    }
    .search-bar-wrapper .glass_icon{font-size: 14px;}
    .search{margin:0;}
    
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
	$(function() {
		if(window.MY_NAME) {
			// 헤더에 닉네임과 로그아웃 버튼 추가
            headerName = $('#header_name');
			
			if(window.COMPANY_NAME)headerName.html(window.COMPANY_NAME + "님");
			else headerName.html(window.MY_NAME + "님");
            
            $("#loggedOutDiv").css("display", "none");
            $("#loggedInDiv").css("display", "flex");
            
            $("#written").attr("data-userId", window.MY_ID);
        }
		
		const btn  = document.getElementById('jobsMenuBtn');
	    const menu = document.getElementById('jobsMenu');
	    if (btn && menu) {
	    	function open(){ btn.setAttribute('aria-expanded','true'); menu.hidden = false; }
		    function close(){ btn.setAttribute('aria-expanded','false'); menu.hidden = true; }

		    btn.addEventListener('click', function(){
		    	(btn.getAttribute('aria-expanded') === 'true') ? close() : open();
		    });

		    document.addEventListener('click', function(e){
		    	if (!btn.contains(e.target) && !menu.contains(e.target)) close();
		    });

		    btn.addEventListener('keydown', (e)=>{
		    	if(e.key==='Enter' || e.key===' '){ 
					e.preventDefault(); 
					(btn.getAttribute('aria-expanded')==='true'?close():open()); 
				}
		    });
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
	    
		$("#account").click(function() {$(location).attr('href', '/mypage/account');});
		
		$("#지원현황").click(function(){$(location).attr('href','/jobs/지원현황');})
		
		$("#스크랩").click(function(){$(location).attr('href','/jobs/스크랩');})	
		
		$("#공고관리").click(function(){$(location).attr('href','/jobs/공고관리')})
		
		$("#지원자관리").click(function(){$(location).attr('href','/jobs/지원자관리')})
		
		$(".mypage_toggle").hide();
		
		$("#keyword").val();
		
		document.getElementById("searchBtn").addEventListener("click", searchJobs);
		document.getElementById("keyword").addEventListener("keydown", function (e) {
			if (e.key === "Enter") {
			 searchJobs();
			}
		});
		
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
	
function searchJobs(){
	let keyword = $("#keyword").val();
	$(location).attr('href', "/jobs/board?keyword="+keyword);
}

function openBusinessPopup() {
	var url = "<c:url value='/jobs/toBusiness'/>"; 
	var option = "width=500,height=695,top=100,left=100,resizable=no,scrollbars=yes";
	window.open(url, "convertBusinessPopup", option);
}

</script>
</head>
<header>
	<div class="header">
		<div class="header-container">
			<div class="flex items-center" style="flex: 1 1 auto;">
				<div class="nav-container">    
					<div class="header-left">
						<a href="/jobs/board" class="flex"> 
							<img class="nav-icon"src="/resources/include/images/jobs-icon.png" alt="JAM">
						</a>
					</div>
				</div>
				
				<div class="search-div flex justify-center items-center border-radius-43px">
					<div class="search-bar-wrapper item-center flex justify-space-around">
						<input type="text" name="keyword" id="keyword" class="search search-input">
						<i id="searchBtn" class="glass_icon fa-solid fa-magnifying-glass"></i>
					</div>
				</div>
			</div>
			
			<ul class="jobs-nav">
				<!-- 로그인 전 -->
				<sec:authorize access="isAnonymous()">
					<li class="auth-buttons">
						<a href="/member/login">로그인</a>
						<a href="/member/join">회원가입</a>
					</li>
				</sec:authorize>
				
				<!-- 로그인 후: 드롭다운(닉네임 + caret) -->
				<sec:authorize access="isAuthenticated()">
					<li class="userType">
						<sec:authorize access="hasRole('COMPANY')">
							<span class="userTypeSpan typeCompany">기업회원</span>
						</sec:authorize>
						<sec:authorize access="!hasRole('COMPANY')">
							<span class="userTypeSpan typeUser">개인회원</span>
						</sec:authorize>
					</li>
					<li class="menu">
						<button type="button" class="menu__toggle" id="jobsMenuBtn" aria-haspopup="true" aria-expanded="false" aria-controls="jobsMenu">
							<span id="header_name"></span>
							<i class="fa-solid fa-caret-down" aria-hidden="true"></i>
						</button>
						
						<ul class="menu__list" id="jobsMenu" role="menu" hidden>
							<!-- 기업회원 전용 -->
							<sec:authorize access="hasRole('COMPANY')">
								<li role="none"><a role="menuitem" href="<c:url value='/jobs/post/write"'/>">공고등록</a></li>
								<li role="none"><a role="menuitem" href="<c:url value='/jobs/postsManage/company'/>">채용관리</a></li>
								<li role="none"><a role="menuitem" href="javascript:void(0);" id="logout">로그아웃</a></li>
							</sec:authorize>
							
							<!-- 개인회원 전용 -->
							<sec:authorize access="isAuthenticated() and !hasRole('COMPANY')">
								<li role="none"><a role="menuitem" href="<c:url value='/jobs/postsManage/user'/>">작성한 글</a></li>
								<li role="none"><a role="menuitem" href="<c:url value='/jobs/user/applications'/>">지원현황</a></li>
								<li role="none"><a role="menuitem" href="<c:url value='/jobs/user/favorites'/>">스크랩</a></li>
								<li role="none"><a role="menuitem" href="javascript:void(0);" onclick="openBusinessPopup();">기업회원 전환</a>
								<li role="none"><a role="menuitem" href="javascript:void(0);" id="logout">로그아웃</a></li>
							</sec:authorize>
						</ul>
					</li>
				</sec:authorize>
				
				<!-- 우측 브랜드 -->
				<li>
					<a href="/" class="brand-link" target="_blank" rel="noopener noreferrer">
						<img class="main-favicon" src="/resources/include/images/main-favicon.svg" alt="">
						<span>JAM</span>
					</a>
				</li>
			</ul>
		</div>
	</div>
</header>
