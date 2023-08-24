<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - JOB</title>

	<style type="text/css">
		.icon {width : 35px;}
		a { 
			text-decoration: none; 
		}
		.inline-block {display : inline-block;}
		.justify-between {
		    justify-content: space-between;
		}
		
		.items-center {
		    align-items: center;
		}
		.flex {
		    display: flex;
		}
		.border {
		    border: 1px solid #e5e7eb;
		}
		.border-top {
			border-top-color: hsla(220,9%,46%,.3);
			border-top: 1px solid #e5e7eb;
		}
		.border-bottom {
			border-bottom-color: hsla(220,9%,46%,.3);
			border-bottom: 1px solid #e5e7eb;
		}
		.py-4 {
		    padding-top: 1rem;
		    padding-bottom: 1rem;
		}
		.my-7 {
		    margin-top: 1.75rem;
		    margin-bottom: 1.75rem;
		}
		.border-radius-43px{border-radius: 43px;}
		.border-none {
		    border-style: none;
		}
		
	</style>
	
	<script type="text/javascript">
		$(function(){
			
			$("#jobUpdateBtn").click(function(){
				$("#c_data").attr({
					"action" : "/job/jobUpdateForm",
					"method" : "POST"
				})
				$("#c_data").submit();
			})
			
			$("#jobDeleteBtn").click(function(){
				if(confirm("정말 삭제하시겠습니까?")){
					$("#c_data").attr({
						"action" : "/job/jobDelete",
						"method" : "POST"
					})
					$("#c_data").submit();
				}return;
			})
			
			/* 쪽지 아이콘 클릭 */
			$("#send_msg").click(function(){
				var url = "/message/send";
				var option = "width=500, height=370, top=10, left=10";
				var name = "팝업";
				
				window.open("",name,option);
				
				$("#frmPopup").attr("action", url);
				$("#frmPopup").attr("target", name);
				$("#frmPopup").attr("method", "POST");
				$("#frmPopup").submit();
			})
			
		})
		
	</script>
</head>
<body>
	<div class="rem-30 my-top-15 my-bottom-15">
		<form name="c_data" id="c_data">
			<input type="hidden" name="job_no" value="${detail.job_no}"/>
		</form>
		<form name="frmPopup" id="frmPopup">
			<input type="hidden" id="receiver_id" name="receiver_id" value="${detail.user_id }">
			<input type="hidden" id="receiver" name="receiver" value="${detail.user_name }">
		</form>
		
		
		<div class="py-8 text-center">
			<h1 class="font-weight-bold">${detail.job_title }</h1>
		</div>
		
		<div class="content flex items-center float-right">
			<span>${detail.user_name }</span>
			<c:if test="${member.user_id != detail.user_id}">
				<img id='send_msg' class='message_icon ml-05 cursor-pointer' style='width:2rem;' alt='쪽지' src='/resources/include/images/message_icon.svg'>			
			</c:if>
			<span class="ml-1">${detail.job_date }</span>
			<img class="icon ml-2" style="width:3rem;" src="/resources/include/images/hits.svg">
			<span class="ml-05">${detail.job_hits }</span>
		</div>
		
		<div class="my-top-8 py-8 border-top border-bottom m-height350">
			<div>
				<c:if test="${detail.pay_category == 0}">
					<span class="">일급</span>
				</c:if>
				<c:if test="${detail.pay_category == 1 }">
					<span>주급</span>
				</c:if>
				<c:if test="${detail.pay_category == 2 }">
					<span>월급</span>
				</c:if>
				<span>급여 : ${detail.pay }</span>
			</div>
			<p>${detail.job_content }</p>
		</div>
		
		
		<div class="text-right my-top-7">
			<c:if test="${member.user_id == detail.user_id }">
				<div>
					<button type="button" id="jobUpdateBtn" class="">수정</button>
					<button type="button" id="jobDeleteBtn" class="">삭제</button>
				</div>
			</c:if>
		</div>
		<div class="py-8">
			<jsp:include page="jobReply.jsp"/>
		</div>
		<div class="text-center">
			<a id="jobList" href="/job/jobList">- 목록 -</a>
		</div>
		
		
		
		
	</div>
</body>
</html>