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
			
			let com_no = ${detail.com_no};
			
			listAll(com_no);
			
			/* 댓글 입력 */
			$("#reply_insert").click(function(){
				
				let insertUrl = "/comreplies/replyInsert";
				
				let value = JSON.stringify({
					com_no:com_no,
					comReply_content:$('#comReply_content').val()
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
						if($("#comReply_content").val().replace(/\s/g, "") == ""){
							alert("댓글을 입력하세요.");
							$("#comReply_content").focus();
							return false;
						}
					},
					success : function(result){
						if(result=="SUCCESS"){
							alert("댓글이 등록되었습니다.");
							dataReset();
							listAll(com_no);
						}
					}
				});
				
			})
			
			/* 수정 버튼 클릭*/
			$(document).on("click","button[data-btn='upBtn']",function(){
				let panel = $(this).parents("div.panel");
				let comReply_no = panel.attr("data-num");
				updateForm(comReply_no, panel);
			});
			
			function updateForm(comReply_no, panel){
				$("#user_name").val(panel.find(".panel-title .name").html());
				$("#user_name").prop("readonly",true);
				let content = panel.find(".panel-body").html();
				content = content.replace(/(<br>|<br\/>|<br \/>)/g, '\r\n'); //<br><br/><br />
				//$("#comReply_content").val(content);
				let id = panel.find(".panel-title .name");
				let panelbody = panel.find(".panel-body");
				panelbody.html("<textarea id='upReplyContent' rows='3' cols='30' id='tt_"+id+"'>"+content+"</textarea>");
				
				let panelbtn = panel.find(".panel-btn");
				panelbtn.html(
					"<button type='button' id='cancel'>취소</button>" +
					"<button type='button' id='replyUpdateBtn' data-comReply_no =" + comReply_no + ">등록</button>");
				
			}			
			
			/* 수정 취소 버튼 클릭*/
			$(document).on("click","#cancel",function(){
				listAll(com_no);
			})
			
			/* 댓글 수정 */
			$(document).on("click","#replyUpdateBtn",function(){
				let comReply_no = $(this).attr("data-comReply_no");
				let panel_title = $(this).parents(".panel-title");
				let comReply_contents = panel_title.find('.panel-body > #upReplyContent').val();
				
				$.ajax({
					url:'/comreplies/'+comReply_no,
					type:'put',
					headers: {
						"Content-Type" : "application/json",
						"X-HTTP-Method-Override" : "PUT"},
					data:JSON.stringify({
						comReply_content:comReply_contents,
					}),
					dataType:'text',
					error:function(xhr, textStatus, errorThrown){
						alert(textStatus + " (HTTP-"+xhr.status+" / " + errorThrown + ")");
					},
					beforeSend:function(){
						if(!checkForm("#upReplyContent","댓글 내용을")) return false;
					},
					success:function(result){
						console.log("result: "+result);
						if(result == "SUCCESS"){
							alert("댓글 수정이 완료되었습니다.");
							dataReset();
							listAll(com_no);
						}
					}
				});
			});
			
			/* 삭제 버튼 클릭 */
			$(document).on("click","button[data-btn='delBtn']",function(){
				let comReply_no = $(this).parents("div.panel").attr("data-num");
				deleteBtn(comReply_no, com_no);
			});
			
			
			
		})
		
		function listAll(com_no){
			$(".reply").detach();
			let url = "/comreplies/all/"+com_no;
			
			$.getJSON(url, function(data){ 
				$(data).each(function(){
					let comReply_no = this.comReply_no;
					let user_id = this.user_id;
					let user_name = this.user_name;
					let comReply_content = this.comReply_content;
					let comReply_date = this.comReply_date;
					comReply_content = comReply_content.replace(/(\r\n|\r\n)/g, "<br/>");
					template(comReply_no,user_name,comReply_content,comReply_date,user_id);
				});
			}).fail(function(){
				alert("댓글 목록을 불러오는데 실패하였습니다. 잠시후에 다시 시도해 주세요.");
			});
		}
		
		function template(comReply_no,user_name,comReply_content,comReply_date,user_id){
			let $div = $('#reviewList');
			
			let $element = $('#item-template').clone().removeAttr('id');
			$element.attr("data-num",comReply_no);
			$element.addClass("reply");
			$element.find('.panel-heading > .panel-title > .name').html(user_name); 
			
			$element.find('.panel-heading > .panel-title > .frmPopup').attr("id", "frmPopup_" + comReply_no);
			$element.find('.panel-heading > .panel-title > .frmPopup > #receiver_id').attr("value", user_id);
			$element.find('.panel-heading > .panel-title > .frmPopup > #receiver').attr("value", user_name);
			
			$element.find('.panel-heading > .panel-title > .date').html(comReply_date);
			
			$element.find('.panel-body').html(comReply_content);
			
			
			$div.append($element);
			
			/* 댓글 작성자와 사용자 아이디가 일치 시 댓글 수정 삭제 버튼 생성 */
			if("${member.user_id}" != ""){
				
				if('${member.user_id}' == user_id ){
					
					$element.find('.panel-heading > .panel-title > .panel-btn').html( 
							"<button type='button' class='delBtn' data-btn='delBtn' >삭제</button>"
							+ "<button type='button' class='upBtn' data-btn='upBtn'>수정</button>" );
				}else { /* 댓글 작성자와 사용자 아이디 불일치 시 메세지 버튼 생성 */
					$element.find('.panel-heading > .panel-title > .message').html(
							"<button type='button' class='send_message'>"
							+ "<img class='message_icon' style='width:2rem;' alt='쪽지' src='/resources/include/images/message_icon.svg'>"
							+ "</button>");
				}
			}
			
			/* 메세지 버튼에 click 이벤트 */
			$element.find('.panel-heading > .panel-title > .message > .send_message').attr("onclick", "sendMsg('" + comReply_no + "')");
			
			
			
			
		}
		
		/*입력 폼 초기화*/
		function dataReset(){
			$("#replyForm").each(function(){
				this.reset();
			});
			$("#replyForm button[type='button']").removeAttr("data-rnum");
			$("#replyForm button[type='button']").attr("id","replyInsertBtn");
		}
		
		/* 댓글 삭제 */
		function deleteBtn(comReply_no, com_no){
			if(confirm("댓글을 삭제하겠습니까?")){
				$.ajax({
					url : "/comreplies/"+comReply_no,
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
							listAll(com_no);
						}
					}
				});
			}	
		}
		
		/* 쪽지 전송 팝업 */
		function sendMsg(comReply_no){
			
			var url = "/message/send";
			var option = "width=500, height=370, top=10, left=10";
			var name = "팝업";
			
			window.open("",name,option);
			
			let msg = "frmPopup_"+comReply_no;
			
			$("#" + msg).attr("action", url);
			$("#" + msg).attr("target", name);
			$("#" + msg).attr("method", "POST");
			$("#" + msg).submit();
			
			
		}
	
	</script>
</head>
<body>
	<div>
		<!-- 댓글 작성부 -->
		<div class="replyContainer">
			<c:choose>
				<c:when test="${member != null}">
					<form id="replyForm">
						<div class="reply_div">
							<table>
								<tr>
									<td id="reply_name">${member.user_name } </td>
								</tr>
								<tr>
									<td>
										<textarea id="comReply_content" name="comReply_content" class="form-control" rows="3"></textarea>
									</td>
								</tr>
								<tr>
									<td><button type="button" id="reply_insert">등록</button></td>
								</tr>
							</table>
						</div>	
					</form>
				</c:when>
				<c:otherwise>
					<div>
						<span>댓글을 작성하려면</span>
						<a href="/member/login">로그인</a>
						<span>이 필요합니다.</span>
					</div>
				</c:otherwise>
			</c:choose>
		</div>
		
		
		<%-- 댓글 리스트 --%>
		<div id="reviewList">
			<div id="item-template" class="panel">
				<div class="panel-heading">
					<div class="panel-title">
						<span class="cursor-pointer name " id="user_name"></span>
						<div class="message"></div>
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