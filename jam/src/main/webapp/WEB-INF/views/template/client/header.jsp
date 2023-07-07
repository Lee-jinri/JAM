<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<head>
	
	
	<style>
		#header-main:hover{ cursor : pointer; }
		
    	.position-relativ {position : relative;}
    	.float-right {float : right;}
		
		
		#join, #login {
		    padding: 0 15px;
		    font-size: 12px;
		    color: #a5a5a5;
		}
		
		
		.nav-icon {
		    width: 130px;
		    height: 100px;
		    object-fit: contain;
		    margin-left : 100px;
		}
		
		.nav-items { display: inline-block; float: right; margin-top: 25px; margin-right:80px; }
		.nav-items > li {float:left; font-weight: 700; }
		.nav-items > li > a {font-size : 20px; color:#F4BC97;}
		.nav-items > li > a:hover {text-decoration:none;}
		.item {
  flex-grow: 1; /* default 0 */
}
		.font-color-red {color:#ffb689;}
		.display-block{display:block;}
		.w-full {
    width: 100%;
}
	.flex-end {justify-content: right}
	.btn{    color: #ffb689;
    font-weight: bold;
    font-size: 1.4rem;
    width:8rem;}
    .btn2{
    border: 1px solid #ffb689;
    border-radius: 35px;
    background-color: #fff;}
    .text-center{text-align:center;}
    
    
    .mypage_toggle{
    	background-color : #fff; 
	    width: 17rem;
    	height: 10rem;
	    text-align: center;
	    box-shadow : 0px 0px 7px #E6E6E6;
	    }
	</style>
	
	<script>
		$(function() {
			// 로그아웃
			$("#logout").click(function() {	
				$.ajax({
		            type:"get",
		            url:"/member/logout",
		            success:function(data){
		            	location.href="/";
		            } 
		        });  
			})
			$(".mypage_toggle").hide();
			$("#header_name").click(function() {
				$('.mypage_toggle').toggle();
			})
		})
	</script>
</head>
<body>

<header>

	<div class="border-bottom">
		
		<div class="w-full flex my-top-8  justify-space-around items-center height60">
			<div>
				<a href="/" class="flex">
					<img class="nav-icon" src="/resources/include/images/JAM_icon1.png" alt="JAM">
				</a>
			</div>
			<div class="ml-5">
				<a href="/" class="font-color-red font-size-2 font-weight-bold">HOME</a>
			</div>
			<div>
				<a href="/job/jobList" class="font-color-red font-size-2 font-weight-bold">구인&구직</a>
			</div>
			<div>
				<a href="/roomRental/roomRentalList" class="font-color-red font-size-2 font-weight-bold">합주실&연습실</a>
			</div>
			<div>
				<a href="/fleaMarket/fleaMarketList" class="font-color-red font-size-2 font-weight-bold">중고악기</a>
			</div>
			<div class="mr-11">
				<a href="/community/communityList" class="font-color-red font-size-2 font-weight-bold">커뮤니티</a>
			</div>
			<p>
			</p>
			
		</div>
		<div class="flex flex-end my-bottom-4">
			<c:choose>
				<c:when test="${member != null}">
					<div class="flex items-center">
						<span class="font-size-1 font-weight-bold cursor-pointer" id="header_name">${member.user_name } 님</span>
						
						<div class="mypage_toggle absolute border border-radius-7px">
							<ul>
								<li class="pd-top1 font-weight-bold color_y">마이페이지</li>
								<li class="pd-top1"><a class="mypage_font" href="/member/account">계정 정보</a></li>
								<li class="pd-top1"><a class="mypage_font" href="/member/comMyWrite">작성한 글</a></li>
							</ul>
						</div>
						<a href="/message/receiveMessage" class="">
							<img class="message_icon  mr-2 ml-05" style="width:3rem;" alt="쪽지" src="/resources/include/images/message_icon.svg">
						</a>
						<button type="button" id="logout" class="btn mr-2">로그아웃</button>
					</div>
				</c:when>
				<c:otherwise>
					<div class="inline-block mr-1 btn2 text-center">
						<a href="/member/login" class="flex-end btn">로그인</a>
					</div>
					<div class="inline-block mr-2 btn2 text-center">
						<a href="/member/join" class="flex-end btn">회원가입</a>
					</div>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
	
	
</header>





</body>
</html>