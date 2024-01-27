<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
	<script type="text/javascript" src="/resources/include/dist/js/jquery-1.12.4.min.js"></script>
	<script type="text/javascript" src="/resources/include/dist/js/common.js"></script>

	<script>
		$(function(){
			let loggedInUserId = "";
			let loggedInUsername = "";
			
			// 사용자 정보를 가져오는 비동기 함수
			async function getUserInfo() {
			    try {
			        const response = await fetch('/api/member/getUserInfo', {
			            method: 'GET',
			            headers: {
			                'Authorization': localStorage.getItem("Authorization")
			            },
			        });
					if(!response.ok) throw new Error('Network response was not ok'); 
					
					const userInfo = await response.json();
					loggedInUserId = userInfo.user_id;
					loggedInUsername = userInfo.user_name;
					
					$("#reply_name").text(data.user_name);
					
					if (loggedInUserId == "" || loggedInUsername == "") {
						// textarea를 readonly로 변경
						const textarea = document.getElementById('jobReply_content');
						textarea.readOnly = true;
						
						// 로그인 메세지 생성
						const replyLogin = document.getElementById("reply_login");

						const span1 = document.createElement("span");
						span1.textContent = "댓글을 작성하려면 ";

						const loginLink = document.createElement("a");
						loginLink.href = "/member/login";
						loginLink.textContent = "로그인";
						const span2 = document.createElement("span");
						span2.textContent = "이 필요합니다.";

						replyLogin.appendChild(span1);
						replyLogin.appendChild(loginLink);
						replyLogin.appendChild(span2);
					}
			    } catch (error) {
			        console.error('사용자 정보를 가져오는 중 오류 발생:', error);
			    }
			}
			
			// getUserInfo 함수 호출
			getUserInfo().then(() => {
			    
			    listAll(job_no , loggedInUserId);
			    
			    /* 댓글 입력 */
				$("#reply_insert").click(function(){
					
					let insertUrl = "/jobReplies/replyInsert";
					
					let value = JSON.stringify({
						user_id : loggedInUserId,
						user_name : loggedInUsername,
						job_no : job_no,
						jobReply_content:$('#jobReply_content').val()
					});
					
					$.ajax({
						url:insertUrl,
						type:"post",
						headers : {
							"Content-Type" : "application/json"
						},
						dataType:"text",
						data:value,
						error:function(xhr,textStatus, errorThrown){
							alert(textStatus + "(HTTP-" +xhr.status+" / "+errorThrown+")");
						},
						beforeSend:function(){
							if($("#jobReply_content").val().replace(/\s/g, "") == ""){
								alert("댓글을 입력하세요.");
								$("#jobReply_content").focus();
								return false;
							}
						},
						success : function(result){
							if(result=="SUCCESS"){
								alert("댓글이 등록되었습니다.");
								dataReset();
								listAll(job_no , loggedInUserId);
							}
						}
					});
				})	
				/* 수정버튼 클릭  */
				$(document).on("click","button[data-btn='upBtn']",function(){
					let panel = $(this).parents("div.panel")
					let jobReply_no = panel.attr("data-num");
						
					updateForm(jobReply_no, panel);
				});
			})
				
			
			let job_no = ${job_no};
			
			/* 수정 할 댓글 html 구성 */
			function updateForm(jobReply_no, panel){
				$("#user_name").val(panel.find(".panel-title .name").html());
				$("#user_name").prop("readonly",true);
				let content = panel.find(".panel-body").html();
				content = content.replace(/(<br>|<br\/>|<br \/>)/g, '\r\n'); //<br><br/><br />

				let id = panel.find(".panel-title .name");
				let panelbody = panel.find(".panel-body");
				panelbody.html("<textarea id='upReplyContent' rows='3' cols='30' id='tt_"+id+"'>"+content+"</textarea>");
				
				let panelbtn = panel.find(".panel-btn");
				panelbtn.html(
					"<button type='button' id='cancel'>취소</button>" +
					"<button type='button' id='replyUpdateBtn' data-jobReply_no =" + jobReply_no + ">등록</button>" );
				
			}	
			
			/* 수정 취소 버튼 클릭*/
			$(document).on("click","#cancel",function(){
				listAll(job_no , loggedInUserId);
			})
			
			
			/* 댓글 수정 */
			$(document).on("click","#replyUpdateBtn",function(){
				let jobReply_no = $(this).attr("data-jobReply_no");
				let panel_title = $(this).parents(".panel-title");
				let jobReply_contents = panel_title.find('.panel-body > #upReplyContent').val();
				
				$.ajax({
					url:'/jobReplies/'+jobReply_no,
					type:'put',
					headers: {
						"Content-Type" : "application/json",
						"X-HTTP-Method-Override" : "PUT"},
					data:JSON.stringify({
						jobReply_content:jobReply_contents,
					}),
					dataType:'text',
					error:function(xhr, textStatus, errorThrown){
						alert("댓글 수정이 완료되지 않았습니다. 잠시후 다시 이용해주세요" + textStatus + " (HTTP -" +xhr.status + " / " + errorThrown + ")");
					},
					beforeSend:function(){
						if(!checkForm("#upReplyContent","댓글 내용을")) return false;
					},
					success:function(result){
						console.log("result: "+result);
						if(result == "SUCCESS"){
							alert("댓글 수정이 완료되었습니다.");
							dataReset();
							listAll(job_no , loggedInUserId);
						}
					}
				});
			});
			
			/* 댓글 삭제 버튼 클릭 */
			$(document).on("click","button[data-btn='delBtn']",function(){
				let jobReply_no = $(this).parents("div.panel").attr("data-num");
				deleteBtn(job_no, jobReply_no, loggedInUserId);
			})
		})
		
		
		
		function listAll(job_no, loggedInUserId){
			$(".reply").detach();
			
			// 로그인 시 현재 user_id와 댓글 user_id 비교
			if(loggedInUserId == null){
		    	url = "/jobReplies/all/" + job_no;
		    }else url = "/jobReplies/all/" + job_no + "?user_id=" + loggedInUserId;
			
			$.getJSON(url, function(data){ 
				$(data).each(function(){
					let jobReply_no = this.jobReply_no;
					let user_id = this.user_id;
					let user_name = this.user_name;
					let jobReply_content = this.jobReply_content;
					let jobReply_date = this.jobReply_date;
					jobReply_content = jobReply_content.replace(/(\r\n|\r\n)/g, "<br/>");
					template(jobReply_no,user_name,jobReply_content,jobReply_date,user_id, loggedInUserId);
				});
			}).fail(function(){
				alert("댓글 목록을 불러오는데 실패하였습니다. 잠시후에 다시 시도해 주세요.");
			});
		}
		
		function template(jobReply_no,user_name,jobReply_content,jobReply_date,user_id, loggedInUserId){
			let $div = $('#reviewList');
			
			let $element = $('#item-template').clone().removeAttr('id');
			$element.attr("data-num",jobReply_no);
			$element.addClass("reply");
			$element.find('.panel-heading > .panel-title > .name').html(user_name);
			
			$element.find('.panel-heading > .panel-title > .frmPopup').attr("id", "frmPopup_" + jobReply_no);
			$element.find('.panel-heading > .panel-title > .frmPopup > #receiver_id').attr("value", user_id);
			$element.find('.panel-heading > .panel-title > .frmPopup > #receiver').attr("value", user_name);
			
			
			$element.find('.panel-heading > .panel-title > .date').html(jobReply_date);
			$element.find('.panel-body').html(jobReply_content);
			
			$div.append($element);
			
			/* 사용자가 로그인 중이면 */
			
			if (loggedInUserId != null) {
				
				/* 댓글 작성자와 사용자 아이디가 일치 시 댓글 수정 삭제 버튼 생성 */
				if(loggedInUserId == user_id ){
					$element.find('.panel-heading > .panel-title > .panel-btn').html( 
							"<button type='button' class='delBtn' data-btn='delBtn' >삭제</button>"
							+ "<button type='button' class='upBtn' data-btn='upBtn'>수정</button>" );
				}else {  
					/* 댓글 작성자와 사용자 아이디 불일치 시 메세지 버튼 생성*/
					$element.find('.panel-heading > .panel-title > .message').html(
							"<button type='button' class='send_message'>"
							+ "<img class='message_icon' style='width:2rem;' alt='쪽지' src='/resources/include/images/message_icon.svg'>"
							+ "</button>");
				}
			}
			
			/* 메세지 버튼에 click 이벤트 */
			$element.find('.panel-heading > .panel-title > .message > .send_message').attr("onclick", "sendMsg('" + jobReply_no + "')");
		}
		
		/*입력 폼 초기화*/
		function dataReset(){
			$("#replyForm").each(function(){
				this.reset();
			});
			$("#replyForm button[type='button']").removeAttr("data-rnum");
			$("#replyForm button[type='button']").attr("id","reply_insert");
		}
		
		/* 댓글 삭제 */
		function deleteBtn(job_no, jobReply_no, loggedInUserId){
			if(confirm("댓글을 삭제하겠습니까?")){
				$.ajax({
					url : '/jobReplies/'+jobReply_no,
					type: 'delete',
					headers : {
						"X-HTTP-Method-Override" : "DELETE"
					},
					dataType : 'text',
					error : function(xhr, textStatus, errorThrown){ //실행시 오류가 발생했을 경우
						alert("댓글 삭제가 완료되지 않았습니다. 잠시후 다시 이용해주세요" + textStatus + " (HTTP -" +xhr.status + " / " + errorThrown + ")");
					},
					success : function(result){
						console.log("result : "+result);
						if(result == 'SUCCESS'){
							alert("댓글 삭제가 완료되었습니다.");
							dataReset();
							listAll(job_no, loggedInUserId);
						}
					}
				});
			}
		}
		
		/* 쪽지 전송 팝업 */
		function sendMsg(jobReply_no){
			
			var url = "/message/send";
			var option = "width=500, height=370, top=10, left=10";
			var name = "팝업";
			
			window.open("",name,option);
			
			let msg = "frmPopup_"+jobReply_no;
			
			$("#" + msg).attr("action", url);
			$("#" + msg).attr("target", name);
			$("#" + msg).attr("method", "POST");
			$("#" + msg).submit();
		}
	
	</script>
</head>
<body class="wrap">
	<div>
		<!-- 댓글 작성부 -->
		<div class="replyContainer">
			
			<form id="replyForm">
				<div class="reply_div">
					<div id="reply_login"></div>
					<table>
						<tr id="reply_userName">
							<td id="reply_name">${member.user_name } </td>
						</tr>
						<tr>
							<td>
								<textarea id="jobReply_content" name="jobReply_content" class="form-control" rows="3"></textarea>
							</td>
						</tr>
						<tr>
							<td><button type="button" id="reply_insert">등록</button></td>
						</tr>
					</table>
				</div>	
			</form>
		</div>
		
		
		<%-- 댓글 리스트 --%>
		<div id="reviewList">
			<div id="item-template" class="panel">
				<div class="panel-heading">
					<div class="panel-title">
						<span class="cursor-pointer name " id="user_name"></span>
						<div class="message inline"></div>
						<form class="frmPopup" name="frmPopup">
							<input type="hidden" id="receiver_id" name="receiver_id">
							<input type="hidden" id="receiver" name="receiver">
						</form>
						<span class="date"></span>
						<div class="panel-btn"></div>
						<div class="panel-body"></div>
					</div>	
				</div>
			</div>
		</div>
	</div>
</body>
</html>