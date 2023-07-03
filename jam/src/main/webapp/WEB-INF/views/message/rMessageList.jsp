<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - MESSAGE</title>

<style>
	#receive_btn{
		background-color : #ffdd77;
		border : 2px #848484;
		border-top-left-radius: 1em;
		border-top-right-radius: 1em;
	}
	#send_btn {
		background-color : #f5ecce;
		border : 2px #848484;
		border-top-left-radius: 1em;
		border-top-right-radius: 1em;
	}
	.message_menu{font-weight:bold;}
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
	.message {color : #777;}
	.msg_title {
		border : none;
		background-color : white;
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
		
		$(".paginate_button a").click(function(e) {
			e.preventDefault();
			$("#searchForm").find("input[name='pageNum']").val($(this).attr("href"));
			goPage();
		})
	})
	
	/*검색을 위한 실질적인 처리 함수*/
	function goPage(){
		if($("#search").val()=="all"){
			$("#keyword").val("");
		}
		$("#searchForm").attr({
			"method":"get",
			"action":"/message/receiveMessage/"
		});
		$("#searchForm").submit();
	}
	
	function receiverPopup(message_no){
			
			$("#message_no").val(message_no);
			
			var url = "/message/rMessage_detail";
			var option = "width=450, height=610, top=10, left=10";
			var name = "팝업";
			
			window.open("",name,option);
			 
			$("#frmPopup").attr("action", url);
			$("#frmPopup").attr("target", name);
			$("#frmPopup").attr("method", "POST");
			$("#frmPopup").submit();
		}
</script>
</head>
<body>
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
			<button id="receive_btn" class="width_100px height_35px ml-1" onclick="location.href='/message/receiveMessage'" >받은 쪽지</button>
			<button id="send_btn" class="width_100px height_35px ml-1" onclick="location.href='/message/sendMessage'" >보낸 쪽지</button>
			
			<div class="border border-radius-15px" >
				<form id="frmPopup" name="frmPopup">
					<input type="hidden" id="message_no" name="message_no">
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
						<c:when test="${not empty rMessage }">
							<c:forEach items="${rMessage }" var="message" varStatus="status">
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
			<div>
				<!-- 페이징 -->
				<div class="text-center">
					<ul class="pagination">
						<c:if test="${pageMaker.prev}">
							<li class="paginate_button previous">
								<a href="${pageMaker.startPage -1}">Previous</a>
							</li>
						</c:if>
						
						<c:forEach var="num" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
							<li class="paginate_button">
								<a id="${pageMaker.cvo.pageNum == num ? 'btnColor':'' }" class="font-weight-bold" style="color:#585858;" href="${num}">${num}</a>
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
	</div>
</body>
</html>