<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - MESSAGE</title>

<style>
	
	ul {
		padding : 0 30px; 
		padding-top : 10px; 
		padding-bottom : 10px; 
		line-height: 1.42857143; 
		margin-bottom : 0; 
		}
	li {
		display: inline-block;
		text-align : left;
	}
	#receive_btn{background-color : #ffdd77;}
	#send_btn {background-color : #f5ecce;}
</style>
<script>
	$(function(){
		$("#searchBtn").click(function(){
			if($("#search").val() != "all"){
				if($("#keyword").val().replace(/\s/g, "") == ""){
					alert("검색어를 입력하세요.");
					$("#keyword").focus();
					return;
				}
			}
			$("#pageNum").val(1);
			goPage();
		})
		
		$("#keyword").keypress(function(event){
		    if(event.keyCode == 13){ 
		        event.preventDefault(); 
		        $("#searchBtn").click();
		    }
		});
		
		$(".paginate_button a").click(function(e) {
			e.preventDefault();
			$("#searchForm").find("input[name='pageNum']").val($(this).attr("href"));
			goPage();
		})
		
		$("#receive_btn").click(function(){
			getUserIDAndRedirect('/message/receiveMessage?user_id=');
		})
		
		$("#send_btn").click(function(){
			getUserIDAndRedirect('/message/sendMessage?user_id=');
		})
	})
	
	let user_id = "";

	function getUserInfo(){
		return fetch('/api/member/getUserInfo', {
	        method: 'GET',
	        headers: {
	            'Authorization': localStorage.getItem("Authorization")
	        },
	    })
	    .then(response => {
	        if (!response.ok) throw new Error('Network response was not ok');
	        	
	        return response.json();
	        
	    })
	    .then(data =>{
	    	console.log(data);
	    	user_id = data.user_id;
            
	    	console.log("getUserInfo : " +user_id);
            if(user_id == null) {
            	$(location).attr('href', '/member/login');
            	return false;
            }
            
            $("#user_id").val(user_id);
	    })
	    .catch(error => {
	        console.error('사용자 정보를 가져오는 중 오류 발생:', error);
	    });
	}
	
	/*검색을 위한 실질적인 처리 함수*/
	function goPage(){
		if($("#search").val()=="all"){
			$("#keyword").val("");
		}
		
		getUserInfo()
		.then(() => {
			$("#searchForm").attr({
	 			"method":"get",
	 			"action":"/message/receiveMessage/"
	 		});
	 		$("#searchForm").submit();
		})
		
	}
	
	function receiverPopup(message_no){
			
		getUserInfo()
        .then(() => {
            $("#message_no").val(message_no);
            $("#receiver_id").val(user_id);
            
            var url = "/message/receiveMsgDetail";
    		var option = 'width=500, height=370, top=10, left=10';
    		var name='팝업';
    		
    		window.open("",name,option);
    		
    		$("#formPopup").attr("action", url);
    		$("#formPopup").attr("target", "팝업");
    		$("#formPopup").attr("method", "POST");
    		$("#formPopup").submit();
        });
		
	}
	
	function getUserIDAndRedirect(redirectURL) {
		getUserInfo()
	    .then(() => {
	    	console.log("getUserIDAndRedirect : "+ user_id);
		    if (user_id != null && user_id != "") {
	        	$(location).attr('href', redirectURL + user_id);
	        } else {
	            $(location).attr('href', '/member/login');
	        }
	    })
	}
</script>
</head>
<body class="wrap">
	<div class="rem-20 my-top-15 my-bottom-15">
		<div class="title">
			<p class="text-center my-7">쪽지</p>
		</div>
		
		<div class="py-2rem flex justify-right">
			<div class="items-center">
				<form id="searchForm">
					<!-- 페이징 처리를 위한 파라미터 -->
					<input type="hidden" name="pageNum" value="${pageMaker.cvo.pageNum }">
					<input type="hidden" name="amount" value="${pageMaker.cvo.amount }">
					<input type="hidden" id="user_id" name="user_id">
					
					<div class="item-center flex">
						<!-- <label>검색조건</label> -->
						<select id="search" name="search" class="search">
							<option value="all">전체</option>
							<option value="message_title">제목</option>
							<option value="message_content">내용</option>
							<option value="sender">보낸 사람</option>
						</select>
						<div>
							<input type="text" name="keyword" id="keyword" placeholder="쪽지 검색" class="border border-radius-43px rem-2 search"/>
						</div>
						<img class="icon" id="searchBtn" style="cursor:pointer; width:3rem;" src="/resources/include/images/search.svg">
					</div>
				</form>
			</div>
		</div>	
		<div class="content">
			<button id="receive_btn" class="width_100px height_35px ml-1"  >받은 쪽지</button>
			<button id="send_btn" class="width_100px height_35px ml-1" >보낸 쪽지</button>
			
			<div class="border border-radius-15px" >
				<form id="formPopup" name="formPopup">
					<input type="hidden" id="message_no" name="message_no">
					<input type="hidden" id="receiver_id" name="receiver_id">
				</form>
				<div id="message_menu">
					<ul class="message_menu">
						<li class='message_menu width_20'>보낸 사람</li>
						<li class='message_menu width_45'>제목</li>
						<li class='message_menu '>받은 시간</li>
					</ul>
				</div>
				<div id="message_list">
					<c:choose>
						<c:when test="${not empty receiveMsg }">
							<c:forEach items="${receiveMsg }" var="message" varStatus="status">
								<ul class="message border-top" style="<c:if test="${message.read_chk == 1}">color:#A4A4A4;</c:if>">
									<li class="width_20">${message.sender }</li>
									<li class="width_45"> 
										<button type='button' class='msg_title' onclick="receiverPopup(${message.message_no});">
											${message.message_title } 
										</button>
									</li>
									<li class="">${message.sendTime }</li>
								</ul>
							</c:forEach>
						</c:when>
					</c:choose>
				</div>
			</div>
			<!-- 페이징 -->
			<div class="text-center">
				<ul class="pagination">
					<c:if test="${pageMaker.prev}">
						<li class="paginate_button previous">
							<a href="${pageMaker.startPage -1}">Previous</a>
						</li>
					</c:if>
					
					<c:forEach var="num" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
						<li class="paginate_button"  >
							<a id="${pageMaker.cvo.pageNum == num ? 'btnColor':''}" class="font-weight-bold" href="${num}">${num}</a>
						</li>
					</c:forEach>
					
					<c:if test="${pageMaker.next}">
						<li class="paginate_button next">
							<a href="${pageMaker.endPage +1 }">Next</a>
						</li>
					</c:if>
				</ul>
			</div>
		</div>
	</div>
</body>
</html>