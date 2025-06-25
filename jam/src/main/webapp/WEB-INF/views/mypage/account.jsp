<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<head>
<!-- 카카오 주소 검색 API -->
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<title>JAM-계정정보</title>
<style>
.common-box{
	width: 694px;
	height: 440px;
	padding: 0 17px;
    border-radius: 12px;
    box-shadow: 1px 1px 10px 0 rgba(72, 75, 108, .08);
    border: solid 1px #e3e9ed;
    background-color: #fff;
    box-sizing: border-box;
}

p{
	margin: 0;
}

.account_title_div{
	margin: 0 auto;
    width: 694px;
    margin-bottom: 22px;
}

.account-title{
	display: inline;
    font-size: 22px;
    font-weight: bold;
    line-height: 24px;
    letter-spacing: -.2px;
    color: #2e2e2e;
}

.item_text{
	padding-right: 4px;
    letter-spacing: -.3px;
    color: #303038;
    word-break: break-all;
    line-height: 33px;
        font-weight: bold;
}

.row_item{
	padding: 14px 65px 12px 30px;
    border-bottom: 1px solid #eee;
}

.drop_link{
	padding: 16px 0;
	margin: 0 auto;
    width: 694px;
}
.text-btn {
	background: none;
	border: none;
	padding: 4px 8px;
	font-size: 16px;
	font-weight: 500;
	color: #333;
	cursor: pointer;
	text-decoration: none;
}

.text-btn:hover {
	text-decoration: underline;
}

.info-row {
	display: flex;
	align-items: center;
}

.info-row span{
	flex: 1;
}

.btn-edit {
	background-color: #f5f5f7; 
	border: none;
	border-radius: 8px; 
	padding: 6px 12px;
	font-size: 14px;
	color: #333;
	cursor: pointer;
	font-weight: 500;
}

.modal-overlay {
	position: fixed;
	top: 0; left: 0; right: 0; bottom: 0;
	background-color: rgb(0 0 0 / 71%);
	display: none;
	justify-content: center;
	align-items: center;
	z-index: 999;
}

.modal-overlay.active {
	display: flex;
}


.modal-content {
	background: #fff;
	padding: 20px 30px;
	border-radius: 10px;
	box-shadow: 0 4px 15px rgba(0,0,0,0.2);
	width: 430px;
	text-align: center;
}

.modal-content input, #passwordVerifyInput {
	padding: 5px 8px;
	margin: 6px 0;
	border: #a7a7a7  2px solid;
	border-radius: 16px;
}

input:focus {
	border: 2px solid #007bff;
	box-shadow: 0 0 4px rgba(0, 123, 255, 0.5);
	outline: none;
}
.modal-buttons {
	display: flex;
	justify-content: center;
	margin: 20px 0 10px 0;
}

.modal-buttons button {
	padding: 6px 12px;
	margin: 0 3px;
	border-radius: 6px;
	border: none;
	cursor: pointer;
}
#address_search{
	margin-left: 15px;
	width: 75px;
	height: 45px;
	border-radius: 15px;
	font-size: 15px;
	border: none;
	letter-spacing: -0.9px;
}

.modalTitle{
	margin-bottom: 30px;
}
.modalHint{
	margin-bottom: 10px;
}
.modalConfirm{
  background-color: #4F70EC; /* 메인 컬러 */
  color: white;
}

.modalCancel{
	background-color: #f2f2f2;
} 

#password-box {
    display: none; /* 기본적으로 숨김 */
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%); 
    background: white;
    width: 350px;
    padding: 25px;
    border-radius: 12px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); 
    text-align: center;
}

#password-box .section {
    margin-bottom: 15px; /* 각 요소 간격 조정 */
}

#password-box p {
    font-size: 18px;
    font-weight: bold;
}

#submitBtn {
    width: 25%;
    padding: 7px 10px;
    background: #F4A261;
    color: white;
    font-size: 16px;
    font-weight: bold;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.3s ease-in-out;
}

#submitBtn:hover {
    background: #E78B44; 
    transition: background 0.3s ease-in-out;
}
</style>

<script type="text/javascript">	
$(function(){
	let isSocialLogin;
	let phone;
	let user_name;
	
	fetchVerifyStatus();
	
	document.querySelectorAll(".enter-submit").forEach(function (input) {
	    input.addEventListener("keydown", function (event) {
	        if (event.key === "Enter") {
	            event.preventDefault();
	            const btnId = input.dataset.button;
	            document.getElementById(btnId)?.click();
	        }
	    });
	});
	
	$("#submitBtn").click(function(){
		handlePasswordVerification();
	})
	
	
	/* 닉네임 수정 버튼 클릭 */
	$("#name_modi_btn").click(function(){
		$("#usernameModal").css("display","flex");
		$("#usernameModal").addClass("active");
		$("#usernameInput").focus();
		$("#usernameInput").val("");
	})
	
	
	/* 닉네임 수정 확인 버튼 클릭 */
	$("#nameChangeBtn").click(function(){
		handleNicknameUpdate();
	})

		
	// 전화번호 수정 버튼 클릭
	$("#phone_modi_btn").click(function(){
		$("#phoneModal").css("display","flex");
		$("#phoneInput").focus();
		$("#phoneInput").val("");
	})
		
	// 전화번호 수정
	$("#phoneChangeBtn").click(function(){
		handlePhoneUpdate();
	})
			
	// 비밀번호 변경 버튼 클릭
	$("#pw_modi_btn").click(function(){
		// 소셜로그인 확인
		if(isSocialLogin == 1) {
			alert("소셜 회원은 비밀번호를 변경할 수 없습니다.");
			return false;
		}
		$("#passwordModal").css("display","flex");
		$("#passwordInput").focus();
		$("#passwordInput").val("");
		$("#passwordCheck").val("");
	})
	
	// 비밀번호 변경
	$("#pwChangeBtn").click(function(){
		handlePasswordUpdate();
	})
		
	// 주소 수정 버튼 클릭
	$("#address_modi_btn").click(function(){
		$("#addressModal").css("display","flex");
	})
	
	// 주소 검색 (카카오 주소 API)
	$("#address_search").click(function(){
		new daum.Postcode({
			oncomplete: function(data) {
	            	
				var addr = ''; // 주소 변수
	
				//사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
				if (data.userSelectedType === 'R') { // 사용자가 도로명 주소를 선택했을 경우
					addr = data.roadAddress;
				} else { // 사용자가 지번 주소를 선택했을 경우
	                addr = data.jibunAddress;
	            }
		
	            $("#streetAddress").val(addr);
	            
				$("#detailAddress").focus();
			}
		}).open();
	})
	
	// 주소 변경
	$("#addressChangeBtn").click(function(){
		handleAddress();
	})
	
	// 전화번호, 닉네임, 비밀번호, 주소 수정 취소 버튼 클릭하면 새로고침
	$(".account_cancel").click(function(){
		location.reload();
	})
	
	$(".modalCancel").click(function(){
		$(".modal").removeClass("active");
		$(".modal").css("display","none");
	})
	
	// 회원 탈퇴 버튼 클릭 
	$("#delete_account_btn").click(function(){
		var result = confirm("정말 탈퇴 하시겠습니까?");
		
		if(result){
			fetch('/api/member/me',{
				method: 'DELETE',
				credentials: 'include',
			})
			.then(response =>{
				if(response.ok){
					alert("회원 탈퇴가 완료되었습니다.");
			    	window.location.href = "/";
				}else{
					throw new Error('Network response was not ok');
				}
			})
			.catch(error =>{
				alert('오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
				window.location.href = "/";
				console.error('Error: ' , error);
			})
		}
	})	
	
	// 관리자 페이지 버튼
	$(document).on("click", "#adminPageBtn", function() {
		location.href = "/admin/admin";
    });
})

function fetchVerifyStatus(){
	fetch('/api/mypage/account/verify-status',{
		credentials: 'include',
	})
	.then(response =>{
		if(response.status === 401){
			$(location).attr('href', '/');
		}else if(!response.ok){
			alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
			$(location).attr('href', '/');
		}
		return response.json();
	})
	.then((verifyStatus) =>{
		if (verifyStatus) {
			getAccount();
        }
		
        passwordConfirmUi(verifyStatus);
	})
}

function handlePasswordVerification(){
	let password = $("#passwordVerifyInput").val();
	
	if(isEmpty(password)){
		alert("비밀번호를 입력하세요.");
		return false;
	}
	
	fetch('/api/member/verify-password',{
		method: 'POST',
		credentials: 'include',
	    headers: {
	        'Content-Type': 'application/json'
	    },
	    body: JSON.stringify({ user_pw: password }) 
	})
	.then(response =>{
		if(response.ok){
			// 세션 설정 
			fetch('/api/mypage/verify-status/set', {
				method: 'GET',
				credentials: 'include',
			})
			
			alert("비밀번호가 확인되었습니다.");
			
			passwordConfirmUi('true');
			getAccount();
			
		}else if(response.status === 401){
			alert("잘못된 비밀번호 입니다. 다시 입력해 주세요.");
	        $("#passwordVerifyInput").val("");
	        $("#passwordVerifyInput").focus();
		} else if (response.status === 440) {
		    alert("잘못된 접근입니다. 다시 로그인해 주세요.");
		    $(location).attr('href', '/member/login');
		}else throw new Error(`비밀번호 확인 실패: HTTP ${response.status}`);
	})
	.catch(error =>{
		alert('일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.');
		console.error('Password confirm Error: ' , error);
		$(location).attr('href', '/');
	})
}	

function getAccount(){
	fetch('/api/mypage/account')
	.then(response =>{
		if(response.status === 401){
			fetchVerifyStatus();
		}else if(response.status === 500){
			alert("회원 정보를 가져올 수 없습니다. 잠시 후 다시 시도해주세요.");
			$(location).attr('href', '/');
			throw new Error('Network response was not ok.');
		}
		return response.json();
	})
	.then((data) =>{
		isSocialLogin = data.social_login;
		phone = data.phone;
		user_name = data.user_name;
			
		if(!isSocialLogin) $("#user_id").html(data.user_id);
		$("#user_name").html(data.user_name);
		$("#phone").html(data.phone);
		$("#address").html(data.address);
		
		if(data.role == 'ADMIN'){
			$(".drop_link").append( 
				`<button class="text-btn" id="adminPageBtn">관리자 페이지 &gt;</button>`
			);
		}
	})
}

function passwordConfirmUi(isPasswordVerified){
	// 비밀번호 인증 됨
	if(isPasswordVerified){
		$("#password-box").css('display','none');
		$(".info_box").css('display','block');
	}else{ // 인증 안됨
		$("#password-box").css('display','block');
		$(".info_box").css("display", "none");
	}
}

function handleNicknameUpdate(){
	let new_name = $("#usernameInput").val();
	
	if(new_name.replace(/\s/g,"")==""){
		alert("닉네임을 입력하세요.");
		$("#usernameInput").focus();
		return false;
	} 
		
	// 닉네임 유효성 검사
	let name_legExp = /^.{3,10}$/;
	if (name_legExp.test(new_name) == false) {
		alert("닉네임은 3자 이상 10자 이하로 입력해주세요.");
		$("#usernameInput").val("");
		$("#usernameInput").focus();
		
		return false;
	}
		
	// 변경할 닉네임과 원래 닉네임이 같으면 변경하지 않음
	if (user_name == new_name) {
	  location.reload();
	  return;
	}
		
	// 닉네임 중복 확인
	fetch('/api/member/userName/check?userName='+new_name, {
        method: 'GET',
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 409) {
                throw new Error("이미 사용중인 닉네임입니다.");
            } else if (response.status === 400) {
                throw new Error("사용할 수 없는 닉네임입니다. 닉네임을 확인해주세요.");
            } else {
                throw new Error("닉네임 확인 중 오류가 발생했습니다.");
            }
        }
    })
    .then(() => {
        // 닉네임 변경 요청
        return fetch('/api/member/userName', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                user_name: new_name,
            }),
        });
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("닉네임 변경 중 오류가 발생했습니다.");
        }
        alert('닉네임이 변경되었습니다.');
        location.reload();
    })
    .catch(error => {
        alert(error.message || '오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        console.error('Error:', error);
    });
}

function handlePhoneUpdate(){
	let new_phone = $("#phoneInput").val(); 
	
	if(new_phone.replace(/\s/g,"")==""){
		alert("전화번호를 입력하세요.");
		$("#phoneInput").focus();
		return false;
	} 
	
	// 전화번호 유효성 검사
	let regPhone = /^01([016789])([0-9]{3,4})([0-9]{4})$/;
		
	if (regPhone.test(new_phone) == false) {
		alert("사용할 수 없는 전화번호 입니다. 전화번호를 확인해주세요.");
		$("#phoneInput").val("");
		$("#phoneInput").focus();
		
		return false;
	}
		
	// 변경할 전화번호와 원래 전화번호가 같으면 변경하지 않음
	if(phone == new_phone){
		location.reload();
		return;
	}
	
	// 전화번호 중복 확인
	fetch('/api/member/phone/check?phone=' + encodeURIComponent(new_phone), {
	    method: 'GET',
	})
    .then(response => {
        if (!response.ok) {
            if (response.status === 409) {
                throw new Error("이미 사용중인 전화번호입니다.");
            } else if (response.status === 400) {
                throw new Error("사용할 수 없는 전화번호입니다. 전화번호를 확인해주세요.");
            } else {
                throw new Error("전화번호 확인 중 오류가 발생했습니다.");
            }
        }
        return Promise.resolve();
    })
    .then(() => {
        // 전화번호 변경 요청
        return fetch('/api/member/phone', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                phone: new_phone,
            }),
        });
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("전화번호 변경 중 오류가 발생했습니다.");
        }
        alert("전화번호가 변경되었습니다.");
        location.reload();
    })
    .catch(error => {
        alert(error.message || "오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        location.href = "/";
	});	
}

function handlePasswordUpdate(){
	let new_pw = $("#passwordInput").val();
	let pw_check = $("#passwordCheck").val();
	
	if(isEmpty(new_pw) || isEmpty(pw_check)){
		alert("변경할 비밀번호를 입력하세요.");
		return false;
	}else if(new_pw != pw_check) {
		alert("비밀번호가 일치하지 않습니다. 비밀번호를 확인하세요.");
		$("#passwordInput").val("");
		$("#passwordCheck").val("");
		return false;
	}
	
	// 비밀번호 정규식 : 영어 대,소문자, 숫자 8~20자 
	let pw_legExp = /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{8,20}$/;
	
	if(!pw_legExp.test(new_pw)){
		alert("비밀번호는 영어 대,소문자와 숫자를 포함하여 8자 ~ 20자로 입력하세요.");
		return false;
	}else {
		fetch('/api/member/password', {
			method: 'PUT',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				user_pw: new_pw
			}),
		})
		.then(response => {
			if (response.status === 400) {
		        return response.text().then(errorMsg => {
		            throw new Error(errorMsg);
		        });
		    }
			if (!response.ok) throw new Error('Network response was not ok');
            alert('비밀번호가 변경되었습니다.');
            location.reload();
		})
		.catch(error => {
			alert('오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
			location.href = "/"; 
		})
	}
}

function handleAddress(){
	let streetAddress = $("#streetAddress").val();
	let detailAddress = $("#detailAddress").val();
	
	if(isEmpty(streetAddress)){
		alert("주소를 검색하세요.");
		$("#streetAddress").focus();
		return false;
	}else if(isEmpty(detailAddress)){
		alert("상세 주소를 입력하세요.");
		$("#detailAddress").focus();
		return false;
	}
	
	var address = streetAddress + " " + detailAddress;
	
	fetch('/api/member/address',{
		method: 'PUT',
		headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            address: address,
        }), 
	})
	.then(response => {
		if(response.ok){
			alert('주소가 변경되었습니다.');
			location.reload();
		}else throw new Error;
	})
	.catch(error =>{
		alert('오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
		location.href="/";
	})
}

function isEmpty(str){
	return !str || str.trim() === "";
}

</script>
</head>
<body class="wrap">
	<div style="height: 400px;">
		<!-- 비밀번호 확인 모달 -->
		<div class="" id="password-box" style="display: none;">
			<div class="section">
				<p class="text-alignC">비밀번호 확인</p>
			</div>
			<div class="section">
				<input type="password" id="passwordVerifyInput" placeholder="비밀번호 입력" class="enter-submit" data-button="submitBtn">
			</div>
			<div class="section">
				<button id="submitBtn">확인</button>
			</div>
		</div>
		
		<div class="info_box my-top-15 my-bottom-15">
			<div class="account_title_div">
				<h2 class="account-title">계정 정보 관리</h2>
			</div>
			<div class="common-box">
				<div>
					<ul>
						<li>
							<div class="row_item">
								<span class="item_text">아이디</span>
								<div class="info-row">
									<span id="user_id" class="border-none"></span>
								</div>
							</div>
							
						</li>
						<li>
							<div class="row_item">
								<span class="item_text">닉네임</span>
								<div class="info-row">
									<span id="user_name" class="border-none"></span>
									<button type="button" id="name_modi_btn" class="btn-edit">변경</button> 
								</div>
							</div>
						</li>
						<li>
							<div class="row_item">
								<span class="item_text">휴대전화</span>
								<div class="info-row">
									<span class="input-border-none" id="phone"></span>
									<button type="button" id="phone_modi_btn" class="btn-edit">변경</button>
								</div>
							</div>
						</li>
						<li>
							<div class="row_item">
								<div class="info-row">
							    	<span class="item_text">비밀번호</span>
									<button type="button" id="pw_modi_btn" class="btn-edit">변경</button>
								</div>
							</div>
						</li>
						<li>
							<div class="row_item">
								<span class="item_text">주소</span>
								<div class="info-row">
									<span id="address"></span> 
									<button type="button" id="address_modi_btn" class="btn-edit">변경</button>
								</div>
							</div>
						</li>
					</ul>
				</div>
			</div>
			<div class="drop_link">
				<button type="button" class="text-btn" id="delete_account_btn">회원탈퇴 &gt;</button>
			</div>
			
			
			<div class="flex">
				<div id="usernameModal" class="modal modal-overlay" >
					<div class="modal-content">
						<h3 class="modalTitle">닉네임 변경</h3>
						<p class="modalHint">3자 이상 10자 이하로 입력하세요.</p>
						<input type="text" id="usernameInput" class="modalInput enter-submit" data-button="nameChangeBtn"/>
						<div class="modal-buttons">
							<button id="nameChangeBtn" class="modalConfirm">확인</button>
							<button class="modalCancel">취소</button>
						</div>
					</div>
				</div>
				
				<div id="phoneModal" class="modal modal-overlay" >
					<div class="modal-content">
						<h3 class="modalTitle">전화번호 변경</h3>
						<p class="modalHint"></p>
						<input type="text" id="phoneInput" class="modalInput enter-submit" data-button="phoneChangeBtn"/>
						
						<div class="modal-buttons">
							<button id="phoneChangeBtn" class="modalConfirm">확인</button>
							<button class="modalCancel">취소</button>
						</div>
					</div>
				</div>
				
				<div id="passwordModal" class="modal modal-overlay" >
					<div class="modal-content" style="width: 510px;">
						<h3 class="modalTitle">비밀번호 변경</h3>
						<p class="modalHint">영문 대소문자와 숫자를 포함해 8~20자 이내로 입력해 주세요.</p>
						<input type="password" id="passwordInput" class="modalInput" style="width: 270px;" placeholder="비밀번호 입력"/>
						<input type="password" id="passwordCheck" class="modalInput enter-submit" style="width: 270px;" placeholder="비밀번호 확인" data-button="pwChangeBtn"/>
						<div class="modal-buttons">
							<button id="pwChangeBtn" class="modalConfirm passwordConfirm">확인</button>
							<button class="modalCancel">취소</button>
						</div>
					</div>
				</div>
				
				<div id="addressModal" class="modal modal-overlay" >
					<div class="modal-content" style="width: 470px;">
						<h3 class="modalTitle">주소 변경</h3>
						<input type="text" id="streetAddress" class="" placeholder="주소" readonly="readonly" style="width: 385px;"> 
						<input type="text" id="detailAddress" class="enter-submit" placeholder="상세주소 입력" data-button="addressChangeBtn" style="width: 293px;">
						<button type="button" id="address_search">검색</button>
						
						<div class="modal-buttons">
							<button id="addressChangeBtn" class="modalConfirm">확인</button>
							<button class="modalCancel">취소</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
