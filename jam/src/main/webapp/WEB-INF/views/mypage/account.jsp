<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<head>
	<!-- 카카오 주소 검색 API -->
	<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
	
	<title>JAM-회원정보수정</title>
	<style>
	.info_check{
		width:23px;
		height:20px;
		margin-left:5px;
		display: none;
	}
	
	.info_box {
		padding: 10px 25px 10px 0;
	    border-bottom: 1px solid #ebebeb;
	    font-size: 16px;
	    display: block;
		width : 480px;
	}
	
	.link_set{
		color: #252525;
	}
	.input-border-none {
    -webkit-appearance: none;
       -moz-appearance: none;
            appearance: none;
             border: none;
}
	</style>

	<script type="text/javascript">
		$(function(){
			
			let phone_result = "${phone_result}";
			if(phone_result == "success") alert("전화번호가 변경되었습니다.");
			else if (phone_result == "fail") alert("시스템 오류입니다. 잠시 후 다시 시도해주세요.");
			
			let pw_result = "${pw_result}";
			if(pw_result == "success") alert("비밀번호가 변경되었습니다.");
			else if (pw_result == "fail") alert("시스템 오류입니다. 잠시 후 다시 시도해주세요.");
			
			let add_result = "${add_result}";
			if(add_result == "success") alert("주소가 변경되었습니다.");
			else if (add_result == "fail") alert("시스템 오류입니다. 잠시 후 다시 시도해주세요.");
			
			
			/* 전화번호 수정 버튼 클릭 */
			$("#phone_modi_btn").click(function(){
				$("#phone").removeAttr("readonly"); 
				$("#phone").focus();
				$("#phone_modi_btn").attr("type", "hidden");
				$("#phone_btn").css("display","inline");
				$("#phone").val("");
			})
			
			/* 전화번호 수정 확인 버튼 클릭 */
			$("#phoneChange").click(function(){
				let phone = $("#phone").val(); // 변경할 전화번호
				let old_phone = "${account.phone}"; // 원래 전화번호
				
				if(phone.replace(/\s/g,"")==""){
					alert("전화번호를 입력하세요.");
					$("#phone").focus();
					return false;
				} 
				
				if(phone == old_phone){
					phone_cancel();
				}else{
					let regPhone = /^01([0|1|6|7|8|9])([0-9]{3,4})([0-9]{4})$/;
					
					if (regPhone.test(phone) == false) {
						alert("사용할 수 없는 전화번호 입니다. 전화번호를 확인해주세요.");
						$("#phone").val("");
						$("#phone").focus();
						
						return false;
					}

					$.ajax({
						type : "post",
						url : "/member/memberPhoneChk",
						data : {phone : phone},
						success : function(result){
							if(result == 'success'){
								$("#account_modi").attr({
									"method" : "post",
									"action" : "/member/phoneModi"
								})
								$("#account_modi").submit();
								alert("전화번호가 변경되었습니다.");
								
							} else {
								alert("이미 사용중인 전화번호 입니다.");
								$("#phone").val("");
								$("#phone").focus();
							}		
						}
					});
				}
				
			})
			
			/* 전화번호 수정 취소 버튼 클릭 */
			$("#phone_cancel").click(function(){
				phone_cancel();
			})
			
			/* 비밀번호 변경 버튼 클릭 */
			$("#pw_modi_btn").click(function(){
				$("#pw_modi_btn").attr("type","hidden");
				$("#pwConfirm").css("display","inline");
				$("#user_pw").focus();
			})
			
			/* 비밀번호 확인 */
			$("#pwConfirm_btn").click(function(){
				let user_pw = $("#user_pw").val();
				let user_id = $("#user_id").val();
				
				if(user_pw.replace(/\s/g,"")==""){
					alert("비밀번호를 입력하세요.");
					$("#user_pw").focus();
					return false;
				} 
				
				$.ajax({
					type : "post",
					url : "/member/pwConfirm",
					data : {
						user_id : user_id,
						user_pw : user_pw						
					}, 	// '컨트롤에 넘길 데이터 이름' : '데이터(.userId에 입력되는 값)'
					success : function(result){
						if(result == 'success'){
							alert("비밀번호 확인 완료");
							$("#pwConfirm").css("display","none");
							$("#pwModi_div").css("display","inline-block");
							$("#new_pw").pocus();
						} else {
							alert("비밀번호가 일치하지 않습니다.");
						}		
					}
				}); 
			})
			
			/* 비밀번호 변경 */
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
				let pw_legExp = /^[0-9a-zA-Z]{8,20}$/;
				
				if(!pw_legExp.test(new_pw)){
					alert("비밀번호는 영어 대,소문자와 숫자를 포함하여 8자 ~ 20자로 입력하세요.");
					return false;
				}else {
					$("#account_modi").attr({
						"method" : "post",
						"action" : "/member/pwModi"
					})
					$("#account_modi").submit();
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
			
			/* 주소 변경 */
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
				
				$.ajax({
				    type: "POST",
				    url: "/member/addressModi",
				    data: { user_id: user_id, address: address },  
				    success: function(data) {
				    	alert("주소가 변경되었습니다.");
				    	location.reload();
				    },
				    error: function(jqXHR, textStatus, errorThrown) {
				        console.error("주소 변경 중 오류 발생:", textStatus, errorThrown);
				        alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
				    }
				});
				
			})
			
			
			/* 회원 탈퇴 버튼 클릭 */
			$("#account_cancel_btn").click(function(){
				var result = confirm("정말 탈퇴 하시겠습니까?");
				
				if(result){
					
					var user_id = $("#user_id").val();

					$.ajax({
					    type: "POST",
					    url: "/member/withDraw",
					    data: { user_id: user_id },  
					    success: function(data) {
					    	localStorage.removeItem("Authorization");
					    	alert("회원 탈퇴가 완료되었습니다.");
					    	window.location.href = "/";
					    },
					    error: function(jqXHR, textStatus, errorThrown) {
					        console.error("회원 탈퇴 중 오류 발생:", textStatus, errorThrown);
					        alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
					    }
					});
				}
			})
				
		})
		
		
		function phone_cancel(){
			$("#phone").attr("readonly","readonly");
			$("#phone_btn").css("display","none");
			$("#phone_modi_btn").attr("type","button");
		}
		
		
	</script>
</head>
<body class="wrap">
	<div class="common-box  my-top-15 my-bottom-15" id="info-box">
		<div class="info_title border-bottom">
			<p>계정 정보</p>
		</div>
		<form id="account_modi">
			<div class="my-top-8">
				<!-- 소셜로그인이 아닐 때만 아이디 표시 -->
				<c:choose>
				    <c:when test="${account.social_login == 0}">
				        <div class="info_box">
				            <span class="link_set">아이디</span>
				            <div>
				                <input type="text" id="user_id" name="user_id" class="border-none" value="${account.user_id}">
				            </div>
				        </div>
				    </c:when>
				    <c:otherwise>
				        <input type="hidden" id="user_id" name="user_id" class="border-none" value="${account.user_id}">
				    </c:otherwise>
				</c:choose>
				
				<div class="info_box">
					<span class="link_set">이름</span>
					<div>
						<span class="">${account.user_name }</span>
					</div>
				</div>
				<div class="info_box">
					<span class="link_set">전화번호</span>
					<div>
						<input type="number" class="input-border-none" id="phone" name="phone" value="${account.phone }" readonly=readonly placeholder="전화번호 입력">
						<input type="button" id="phone_modi_btn" class="float-right" value="수정">
						<div id="phone_btn" style="display:none;">
							<button type="button" id="phone_cancel" class="float-right ">취소</button>
							<button type="button" id="phoneChange" class="float-right mr-1">확인</button>
						</div>
					</div>
				</div>
				
				<!-- 소셜로그인이 아닐 때 비밀번호 변경 div 표시 -->
				<c:if test="${account.social_login == 0}">
				    <div id="password" class="info_box">
						<span class="link_set">비밀번호</span>
						<input type="button" id="pw_modi_btn" class="float-right" value="변경">
						<div id="pwConfirm" style="display:none;">
							<input type="text" id="user_pw" class="ml-1" placeholder="기존 비밀번호 입력">
							<input type="button" id="pwConfirm_btn" class="float-right" value="확인">
						</div>
						<div id="pwModi_div" style="display:none; " class="my-top-4">
							<span style="font-size: 1.5rem; color: #A4A4A4;">변경할 비밀번호를 입력하세요. </span><br>
							<span style="color: #A4A4A4;">(8~20자 이내로 영문 대소문자, 숫자를 혼용하여 입력하세요.)</span>
							<div class="my-top-4">
								<p >변경할 비밀번호</p>
								<input type="text" id="new_pw" name="user_pw" class=""><br>
								<p class="my-top-4">비밀번호 확인</p>
								<input type="text" id="pw_check" class="">
							</div>
							<button type="button" id="pwModi_btn" class="my-top-4">변경</button>
						</div>
					</div>
				</c:if>
			</div>
			
			<div class="my-top-8">
				<div class="info_box">
					<p class="link_set">주소 관리</p>
					<span class="">${account.address }</span>
					<input type="button" id="address_modi_btn" class="float-right" value="수정">
					<div id="address_div" style="display:none;" >
						<input type="text" id="streetAddress" class="" placeholder="주소" readonly="readonly" style="width: 350px;">
						<input type="text" id="detailAddress" class="" placeholder="상세주소 입력" style="width: 350px;" >
						<button type="button" id="address_search">검색</button>
						<button type="button" id="address_modi">변경</button>
						<input type="hidden" id="address" name="address">
					</div>
				</div>
			</div>
		</form>
		<div>
			<button type="button" id="account_cancel_btn" class="my-top-7">회원 탈퇴</button>
		</div>
	</div>		
</body>
