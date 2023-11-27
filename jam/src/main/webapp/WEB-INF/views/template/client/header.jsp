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

<script>
	$(function() {
		
		/* 소셜 로그인 성공 후 파라미터로 jwt토큰 받음 */
		var search = location.search;
		var params = new URLSearchParams(search);
		var token = params.get('token');
		
		if(token != null){
			// jwt 토큰 로컬 스토리지에 저장
        	localStorage.setItem('Authorization', token);
		}
		
	        fetch('http://localhost:8080/member/getUserInfo', {
	            method: 'GET', 
	            headers: {
	            	'Authorization': localStorage.getItem("Authorization")
	            },
	        })
	        .then(response => {
	            if (response.ok) {
	                // 응답이 성공적인 경우
	                
	                let auth = response.headers.get('auth');
	                if(auth != "" && auth != null){
	                	if(auth.includes("ROLE_ADMIN")){
		                	const adminPageDiv = document.getElementById("adminPage");
		                	
		                	const adminButton = document.createElement("button");
		                    adminButton.setAttribute("type", "button");
		                    adminButton.setAttribute("id", "adminBtn");
		                    adminButton.textContent = "관리자 페이지";

		                    // 버튼을 "adminPage" div에 추가합니다.
		                    adminPageDiv.appendChild(adminButton);
		                }
	                	return response.text();
	                }
	                
	            } else {
	                // 오류 응답 처리
	                throw new Error('Network response was not ok');
	            }
	        })
	        .then((username) => {
	        	if (username) {
	                // username 헤더 값이 존재하는 경우
	                headerName = $('#header_name');
	                headerName.html(username + "님");
	                
	                $("#loggedOutDiv").css("display", "none");
	                $("#loggedInDiv").css("display", "flex");
	                
	            }
			})
	        .catch(error => {
	            // 오류 처리
	            console.error('사용자 정보를 가져오는 중 오류 발생:', error);
	        });

		// 로그아웃
		$("#logout").click(function() {
			fetch('http://localhost:8080/member/logout', {
	            method: 'GET', 
		        headers: {
		            'Authorization': localStorage.getItem("Authorization")
		        },
	        })
	        .then(response => {
	            if (response.ok) {
	            	// 로컬 스토리지의 Authorization 삭제
	            	localStorage.removeItem('Authorization');
	            	$(location).attr('href', "/");
	            } else {
	                // 오류 응답 처리
	                throw new Error('Network response was not ok');
	            }
	        })
	        .catch(error => {
	            // 오류 처리
	            console.error('사용자 데이터 삭제 중 오류 발생 : ', error);
	        });
		})
		
		function getUserIDAndRedirect(redirectURL) {
		    fetch('http://localhost:8080/member/getUserInfo', {
		        method: 'GET',
		        headers: {
		            'Authorization': localStorage.getItem("Authorization")
		        },
		    })
		    .then(response => {
		        if (response.ok) {
		        	// 관리자 페이지 접속
		        	 console.log("url " + redirectURL);
		        	if(redirectURL.includes("admin")){
		        		console.log("true");
		        		let auth = response.headers.get("auth");
		        		if(auth.includes("ROLE_ADMIN")){
		        			return "관리자 페이지";
		        		} else {
		        			let error = "권한이 없는 페이지입니다.";
		        			alert(error);
				            throw new Error(error);
				        }
		        	}
		            return response.headers.get('user_id');
		        } else {
		            throw new Error('Network response was not ok');
		        }
		    })
		    .then((user_id) => {
		    	if(user_id == "관리자 페이지"){
		    		 $(location).attr('href', redirectURL);
		    	}
		    	else if (user_id) {
		            $(location).attr('href', redirectURL + user_id);
		        } else {
		            $(location).attr('href', '/member/login');
		        }
		    })
		    .catch(error => {
		        console.error('사용자 정보를 가져오는 중 오류 발생:', error);
		    });
		}
		
		$("#account").click(function() {
		    getUserIDAndRedirect('/member/account?user_id=');
		});
		
		$("#myWrite").click(function(){
			getUserIDAndRedirect('/member/comMyWrite?user_id=');
		})
		
		$("#msg").click(function(){
			getUserIDAndRedirect('/message/receiveMessage?user_id=');
		})
		
		
		$(".mypage_toggle").hide();
		$("#header_name").click(function() {
			$('.mypage_toggle').toggle();
		})
		
		// 관리자 페이지 버튼 클릭
		$(document).on("click", "#adminBtn", function() {
			getUserIDAndRedirect('/admin/admin');
		});
	})

	
</script>
</head>
<body>

	<header>

		<div class="border-bottom">

			<div
				class="w-full flex my-top-8  justify-space-around items-center height60">
				<div>
					<a href="/" class="flex"> <img class="nav-icon"
						src="/resources/include/images/JAM_icon1.png" alt="JAM">
					</a>
				</div>
				<div class="ml-5">
					<a href="/" class="font-color-red font-size-2 font-weight-bold">HOME</a>
				</div>
				<div>
					<a href="/job/jobList"
						class="font-color-red font-size-2 font-weight-bold">구인&구직</a>
				</div>
				<div>
					<a href="/roomRental/roomRentalList"
						class="font-color-red font-size-2 font-weight-bold">합주실&연습실</a>
				</div>
				<div>
					<a href="/fleaMarket/fleaMarketList"
						class="font-color-red font-size-2 font-weight-bold">중고악기</a>
				</div>
				<div class="mr-11">
					<a href="/community/communityList"
						class="font-color-red font-size-2 font-weight-bold">커뮤니티</a>
				</div>
				<p></p>
			</div>

			<div class="flex justify-right my-bottom-4">
				<!-- 로그인 하지 않음 -->
				<div id="loggedOutDiv" >
					<div class="inline-block mr-1 logoutDiv text-alignC">
						<a href="/member/login" class="justify-right logoutA">로그인</a>
					</div>
					<div class="inline-block mr-2 logoutDiv text-alignC">
						<a href="/member/join" class="justify-right logoutA">회원가입</a>
					</div>
				</div>
				<!-- 로그인 했을 때-->
				<div id="loggedInDiv" class="flex items-center" style="display: none;">
					<div id="adminPage"></div>
					<span class="font-size-1 font-weight-bold cursor-pointer ml-1" id="header_name"></span>

					<div class="mypage_toggle absolute border border-radius-7px">
						<ul>
							<li class="font-weight-bold color_y my-top-4">마이페이지</li>
							<li class="pd-top1"><button type="button" id="account" class="mypage_font border-none bColor_fff">계정 정보</button></li>
							<li class="pd-top1"><button type="button" id="myWrite" class="mypage_font border-none bColor_fff">작성한 글</button></li>
						</ul>
					</div>
					<img id="msg" class="message_icon  mr-2 ml-05 cursor-pointer" style="width: 3rem;" alt="쪽지" src="/resources/include/images/message_icon.svg">
					
					
					<button type="button" id="logout" class="logoutA mr-2 bColor_fff">로그아웃</button>
				</div>
			</div>
		</div>


	</header>





</body>
</html>