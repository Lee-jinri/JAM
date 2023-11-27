<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 커뮤니티</title>

	
	<script type="text/javascript">
		$(function(){
			
			fetch('http://localhost:8080/member/getUserInfo', {
	            method: 'GET', 
	            headers: {
	            	'Authorization': localStorage.getItem("Authorization")
	            },
	        })
	        .then(response => {
	            if (response.ok) {
	                let detail_userID = '${detail.user_id}';
	                let user_id = response.headers.get('user_id');
	                
	                // 글쓴이와 현재 로그인한 사용자가 다르면 메세지 버튼 생성
	                if (user_id != null) {
					    if (detail_userID != user_id) {
					    	
					        var msgDiv = document.getElementById("msgDiv");
					
					        var imgElement = document.createElement("img");
					        imgElement.id = "send_msg";
					        imgElement.className = "message_icon ml-05 cursor-pointer";
					        imgElement.style.width = "2rem";
					        imgElement.alt = "쪽지";
					        imgElement.src = "/resources/include/images/message_icon.svg";
					
					        msgDiv.appendChild(imgElement);
					
					    } else { // 글쓴이와 사용자가 같으면 수정, 삭제 버튼 추가
					        var btnDiv = document.getElementById("btn_div");
					
					        // 수정 버튼 생성
					        var updateButton = document.createElement("button");
					        updateButton.type = "button";
					        updateButton.id = "comUpdateBtn";
					        updateButton.textContent = "수정"; // 버튼 텍스트 설정
					
					        // 삭제 버튼 생성
					        var deleteButton = document.createElement("button");
					        deleteButton.type = "button";
					        deleteButton.id = "comDeleteBtn";
					        deleteButton.textContent = "삭제"; // 버튼 텍스트 설정
	
					        btnDiv.appendChild(updateButton);
					        btnDiv.appendChild(deleteButton);
					    }
					}
	            } else {
	                throw new Error('Network response was not ok');
	            }
	        })
	        .catch(error => {
	            console.error('사용자 정보를 가져오는 중 오류 발생:', error);
	        });
			
			// 수정 버튼 클릭
			$(document).on("click", "#comUpdateBtn", function() {
			    // 클릭 이벤트 처리 코드
			    $("#c_data").attr({
			        "action": "/community/communityUpdateForm",
			        "method": "GET"
			    });
			    $("#c_data").submit();
			});
			
			// 삭제 버튼 클릭
			$(document).on("click", "#comDeleteBtn", function() {
				if(confirm("정말 삭제하시겠습니까?")){
					$("#c_data").attr({
						"action" : "/community/communityDelete",
						"method" : "POST"
					})
					$("#c_data").submit();
					
					let result = $("#result").val();
					
					/* 글 삭제 중 오류 발생 */
					if(result == 'error') alert("게시글 삭제를 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
					else alert("삭제 완료 되었습니다.");
				}
			});
			
			/* 쪽지 아이콘 클릭 */
			$(document).on("click", "#send_msg", function() {
				var url = "/message/send";
				var option = "width=500, height=370, top=10, left=10";
				var name = "팝업";
				
				window.open("",name,option);
				
				$("#frmPopup").attr("action", url);
				$("#frmPopup").attr("target", name);
				$("#frmPopup").attr("method", "POST");
				$("#frmPopup").submit();
			});
			
			/* 글 삭제 중 오류 발생 */
			let result = $("#result").val();
			
			if(result == 'error') alert("게시글 삭제를 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
			
		})
		
	</script>
</head>
<body class="wrap">
	<div>
		<input type="hidden" value="${result }">
	</div>
	<div class="rem-30 my-top-15 my-bottom-15">
		<form name="c_data" id="c_data">
			<input type="hidden" name="com_no" value="${detail.com_no}"/>
		</form>
		<form name="frmPopup" id="frmPopup">
			<input type="hidden" id="receiver_id" name="receiver_id" value="${detail.user_id }">
			<input type="hidden" id="receiver" name="receiver" value="${detail.user_name }">
		</form>
		
		<div class="py-8 text-center">
			<h1 class="font-weight-bold">${detail.com_title }</h1>
		</div>
		<div  class="content flex items-center float-right">
			<span>${detail.user_name }</span>
			<div id="msgDiv"></div>

			<span class="ml-1">${detail.com_date }</span>
			<img class="icon ml-2" style="width:3rem;" src="/resources/include/images/hits.svg">
			<span class="ml-05">${detail.com_hits }</span>
		</div>
		<div class="my-top-8 py-8 border-top border-bottom m-height350">
			<p>${detail.com_content }</p>
		</div>
		<div class="text-right my-top-7">
			<div id="btn_div"></div>
		</div>
		<div class="py-8">
			<jsp:include page="communityReply.jsp"/>
		</div>
		<div class="text-center">
			<a id="comList" href="/community/communityList">- 목록 -</a>
		</div>
	</div>
		
</body>
</html>