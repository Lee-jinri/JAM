<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<head>
<!-- 카카오 주소 검색 API -->
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<title>JAM-회원정보수정</title>
<style>
p{
	margin: 0;
}

.info_check {
	width: 23px;
	height: 20px;
	margin-left: 5px;
	display: none;
}

.info_box {
	padding: 10px 25px 10px 0;
	border-bottom: 1px solid #ebebeb;
	font-size: 16px;
	display: block;
	width: 480px;
}

.link_set {
	color: #252525;
}

.input-border-none {
	-webkit-appearance: none;
	-moz-appearance: none;
	appearance: none;
	border: none;
}
#password-box {
    display: none; /* 기본적으로 숨김 */
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%); /* 📌 화면 중앙 정렬 */
    background: white;
    width: 350px;
    padding: 25px;
    border-radius: 12px; /* 🎨 둥글게 */
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* ✨ 은은한 그림자 */
    text-align: center;
}

#password-box .section {
    margin-bottom: 15px; /* 각 요소 간격 조정 */
}

#password-box p {
    font-size: 18px;
    font-weight: bold;
}

#passwordInput {
    width: 85%;
    padding: 10px;
    border: 2px solid #ddd;
    border-radius: 8px; /* ✅ 입력창 둥글게 */
    font-size: 16px;
    text-align: center;
    transition: all 0.3s ease-in-out;
}

#passwordInput:focus {
    border-color: #6A5ACD; /* ✨ 포커스 시 강조 */
    outline: none;
    box-shadow: 0 0 8px rgba(106, 90, 205, 0.3);
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
	
	verifyStatus();
	
	function verifyStatus(){
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
		
	document.getElementById("passwordInput").addEventListener("keydown", function (event) {
	    if (event.key === "Enter") { 
	        event.preventDefault(); 
	        document.getElementById("submitBtn").click(); // 확인 버튼 클릭
	    }
	});
	
	$("#submitBtn").click(function(){
		let password = $("#passwordInput").val();
		if(password.replace(/\s/g,"")=="" || password.replace(/\s/g,"")==""){
			alert("비밀번호를 입력하세요.");
			return false;
		}
		fetch('/api/member/verify-password',{
			method: 'POST',
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
				alert("비밀번호가 틀렸습니다. 다시 입력해 주세요.");
		        $("#passwordInput").val("");
		        $("#passwordInput").focus();
			}else throw new Error("비밀번호 확인 중 오류가 발생");
		       
		})
		.catch(error =>{
			alert('일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.');
			console.error('Password confirm Error: ' , error);
			$(location).attr('href', '/');
		})
	})
	
	function getAccount(){
			
		fetch('/api/mypage/account')
		.then(response =>{
			if(!response.ok){
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
				
			$("#user_id").val(data.user_id);
			$("#user_name").val(data.user_name);
			$("#phone").val(data.phone);
			$("#address").html(data.address);
		})
	}
	
	function passwordConfirmUi(isPasswordVerified){
		// 비밀번호 인증 됨
		if(isPasswordVerified){
			$("#password-box").css('display','none');
			$("#info-box").css('display','block');
		}else{ // 인증 안됨
			$("#password-box").css('display','block');
			$("#info-box").css("display", "none");
		}
	}
	
	/* 닉네임 수정 버튼 클릭 */
	$("#name_modi_btn").click(function(){
		$("#user_name").removeAttr("readonly"); 
		$("#user_name").focus();
		$("#name_modi_btn").attr("type", "hidden");
		$("#name_btn").css("display","inline");
		$("#user_name").val("");
	})
	
	
	/* 닉네임 수정 확인 버튼 클릭 */
	$("#nameChange").click(function(){
			
		let new_name = $("#user_name").val();
			
		if(new_name.replace(/\s/g,"")==""){
			alert("닉네임을 입력하세요.");
			$("#user_name").focus();
			return false;
		} 
			
		// 닉네임 유효성 검사
		let name_legExp = /^.{3,10}$/;
		if (name_legExp.test(new_name) == false) {
			console.log(new_name);
			alert("사용할 수 없는 닉네임 입니다. 닉네임을 확인해주세요.");
			$("#user_name").val("");
			$("#user_name").focus();
			
			return false;
		}
			
		// 변경할 닉네임과 원래 닉네임이 같으면 변경하지 않음
		if(user_name == new_name){
			location.reload();
		}else{
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
	})
		
	// 전화번호 수정 버튼 클릭했을 때 바꿀 전화번호 입력하도록 스타일 변경
	$("#phone_modi_btn").click(function(){
		$("#phone").removeAttr("readonly"); 
		$("#phone").focus();
		$("#phone_modi_btn").attr("type", "hidden");
		$("#phone_btn").css("display","inline");
		$("#phone").val("");
	})
		
	// 전화번호 수정
	$("#phoneChange").click(function(){
		let new_phone = $("#phone").val(); 
		
		if(new_phone.replace(/\s/g,"")==""){
			alert("전화번호를 입력하세요.");
			$("#phone").focus();
			return false;
		} 
		
		// 전화번호 유효성 검사
		let regPhone = /^01([016789])([0-9]{3,4})([0-9]{4})$/;
			
		if (regPhone.test(new_phone) == false) {
			alert("사용할 수 없는 전화번호 입니다. 전화번호를 확인해주세요.");
			$("#phone").val("");
			$("#phone").focus();
			
			return false;
		}
			
		// 변경할 전화번호와 원래 전화번호가 같으면 변경하지 않음
		if(phone == new_phone){
			location.reload();
		}else{
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
		    	console.error("Error:", error);
			});
		}
	})
			
	// 비밀번호 변경 버튼 클릭
	$("#pw_modi_btn").click(function(){
		// 소셜로그인 확인
		if(isSocialLogin == 1) {
			alert("소셜 회원은 비밀번호를 변경할 수 없습니다.");
			return false;
		}
		
		$("#pw_modi_btn").attr("type","hidden");
		$("#pwConfirm").css("display","inline");
		$("#user_pw").focus();
		
	})
	
	// 사용자의 비밀번호 확인 
	$("#pwConfirm_btn").click(function(){
		let user_pw = $("#user_pw").val();
		
		if(user_pw.replace(/\s/g,"")==""){
			alert("비밀번호를 입력하세요.");
			$("#user_pw").focus();
			return false;
		} 
		
		fetch('/api/member/verify-password',{
			method: 'POST',
		    headers: {
		        'Content-Type': 'application/json'
		    },
		    body: JSON.stringify({ user_pw: user_pw }) 
		})
		.then(response =>{
			if(response.ok){
				alert("비밀번호 확인 완료");
				$("#pwConfirm").css("display","none");
				$("#pwModi_div").css("display","inline-block");
				$("#new_pw").focus();
			}else if(response.status === 401){
				alert("비밀번호가 일치하지 않습니다.");
	            $("#user_pw").val("");
	            $("#user_pw").focus();
			}else throw new Error("비밀번호 확인 중 오류가 발생했습니다.");
	        
		})
		.catch(error =>{
			alert('오류가 발생했습니다. 잠시 후 다시 시도하세요.');
			console.error('Password confirm Error: ' , error);
		})
	})
	
	// 비밀번호 변경
	$("#pwModi_btn").click(function(){
		let new_pw = $("#new_pw").val();
		let pw_check = $("#pw_check").val();
		
		if(new_pw.replace(/\s/g,"")=="" || pw_check.replace(/\s/g,"")==""){
			alert("변경할 비밀번호를 입력하세요.");
			return false;
		}else if(new_pw != pw_check) {
			alert("비밀번호가 일치하지 않습니다. 비밀번호를 확인하세요.");
			$("#new_pw").val("");
			$("#pw_check").val("");
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
				console.error('Error:', error);
			})
		}
	})
		
		
	// 주소 수정 버튼 클릭
	$("#address_modi_btn").click(function(){
		$("#address_div").css("display","inline-block");
		$("#address_modi_btn").attr("type","hidden");
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
	            
	            // 커서를 상세주소 필드로 이동한다.
				$("#detailAddress").focus();
			}
		}).open();
	})
	
	// 주소 변경
	$("#address_modi").click(function(){
		let streetAddress = $("#streetAddress").val();
		let detailAddress = $("#detailAddress").val();
			
		if(streetAddress.replace(/\s/g,"")==""){
			alert("주소를 검색하세요.");
			$("#streetAddress").focus();
			return false;
		}else if(detailAddress.replace(/\s/g,"")==""){
			alert("상세 주소를 입력하세요.");
			$("#detailAddress").focus();
			return false;
		}
		
		var address = streetAddress + " " + detailAddress;
		var user_id = $("#user_id").val();
		
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
			console.error('Error : ' + error);
		})
	})
	
	// 전화번호, 닉네임, 비밀번호, 주소 수정 취소 버튼 클릭하면 새로고침
	$(".account_cancel").click(function(){
		location.reload();
	})
	
	// 회원 탈퇴 버튼 클릭 
	$("#delete_account_btn").click(function(){
		var result = confirm("정말 탈퇴 하시겠습니까?");
		
		if(result){
			
			fetch('/api/member/withDraw',{
				method: 'DELETE',
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
				console.error('Error: ' , error);
			})
		}
	})	
})

	
</script>
</head>
<body class="wrap">
	<div class="common-box my-top-15 my-bottom-15">
		<!-- 비밀번호 확인 모달 -->
		<div class="" id="password-box" style="display: none;">
			<div class="section">
				<p class="text-alignC">비밀번호 확인</p>
			</div>
			<div class="section">
				<input type="password" id="passwordInput" placeholder="비밀번호 입력">
			</div>
			<div class="section">
				<button id="submitBtn">확인</button>
			</div>
		</div>
		<div id="info-box" style="display: none">
			<div class="info_title border-bottom">
				<p>계정 정보</p>
			</div>
			<div class="my-top-8">
				<!-- 소셜로그인이 아닐 때만 아이디 표시 -->
				<div class="info_box">
					<span class="link_set">아이디</span>
					<div>
						<input type="text" id="user_id" name="user_id" class="border-none" readonly=readonly>
					</div>
				</div>
				<div class="info_box">
					<span class="link_set">이름</span>
					<div>
						<input type="text" id="user_name" class="input-border-none user_name" readonly=readonly></input> 
						<input type="button" id="name_modi_btn" class="float-right" value="수정">
						<div id="name_btn" style="display: none;">
							<button type="button" class="account_cancel float-right">취소</button>
							<button type="button" id="nameChange" class="float-right mr-1">확인</button>
						</div>
					</div>
				</div>
				<div class="info_box">
					<span class="link_set">전화번호</span>
					<div>
						<input type="number" class="input-border-none" id="phone" name="phone" readonly=readonly placeholder="전화번호 입력"> 
						<input type="button" id="phone_modi_btn" class="float-right" value="수정">
						<div id="phone_btn" style="display: none;">
							<button type="button" class="account_cancel float-right">취소</button>
							<button type="button" id="phoneChange" class="float-right mr-1">확인</button>
						</div>
					</div>
				</div>

				<div id="password" class="info_box">
					<div>
						<span class="link_set">비밀번호</span> <input type="button" id="pw_modi_btn" class="float-right" value="변경">
					</div>
					<div id="pwConfirm" style="display: none;">
						<input type="password" id="user_pw" class="" placeholder="기존 비밀번호 입력">
						<div style="display: inline;">
							<button class="account_cancel float-right">취소</button>
							<button id="pwConfirm_btn" class="float-right mr-1">확인</button>
						</div>
					</div>
					<div id="pwModi_div" style="display: none;" class="my-top-4">
						<span style="font-size: 1.5rem; color: #A4A4A4;">변경할 비밀번호를 입력하세요. </span><br> 
						<span style="color: #A4A4A4;">(8~20자 이내로 영문 대소문자, 숫자를 혼용하여 입력하세요.)</span>
						<div class="my-top-4">
							<p>변경할 비밀번호</p>
							<input type="password" id="new_pw" name="user_pw" class=""><br>
							<p class="my-top-4">비밀번호 확인</p>
							<input type="password" id="pw_check" class="">
						</div>
						<button type="button" id="pwModi_btn" class="my-top-4">변경</button>
					</div>
				</div>
			</div>

			<div class="my-top-8">
				<div class="info_box">
					<p class="link_set">주소 관리</p>
					<span id="address"></span> <input type="button" id="address_modi_btn" class="float-right" value="수정">
					<div id="address_div" style="display: none;">
						<input type="text" id="streetAddress" class="" placeholder="주소" readonly="readonly" style="width: 350px;"> 
						<input type="text" id="detailAddress" class="" placeholder="상세주소 입력" style="width: 350px;">
						<button type="button" id="address_search">검색</button>
						<div>
							<button type="button" id="address_modi" class="mr-1">변경</button>
							<button type="button" class="account_cancel">취소</button>
						</div>
					</div>
				</div>
			</div>
			<div>
				<button type="button" id="delete_account_btn" class="my-top-7">회원 탈퇴</button>
			</div>
		</div>
	</div>
</body>
