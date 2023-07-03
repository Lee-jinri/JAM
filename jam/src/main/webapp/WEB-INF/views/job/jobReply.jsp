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
			let job_no = ${detail.job_no};
			
			listAll(job_no);
			
			/* 댓글 입력 */
			$("#reply_insert").click(function(){
				
				let insertUrl = "/jobReplies/replyInsert";
				
				let value = JSON.stringify({
					job_no:job_no,
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
							listAll(job_no);
						}else{
							alert("댓글 등록에 실패했습니다. 잠시 후 다시 시도해주세요.");
						}
					}
				});
				
			});
			
			/* 비밀번호 확인없이 수정버튼 */
			$(document).on("click","button[data-btn='upBtn']",function(){
				let panel = $(this).parents("div.panel")
				let jobReply_no = panel.attr("data-num");
				
				updateForm(jobReply_no, panel);
			});
			
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
				listAll(job_no);
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
							listAll(job_no);
						}
					}
				});
			});
			
			/* 댓글 삭제 버튼 클릭 */
			$(document).on("click","button[data-btn='delBtn']",function(){
				let jobReply_no = $(this).parents("div.panel").attr("data-num");
				deleteBtn(job_no, jobReply_no);
			})
		})
		
		
		
		function listAll(job_no){
			$(".reply").detach();
			let url = "/jobReplies/all/"+job_no;
			
			$.getJSON(url, function(data){ 
				$(data).each(function(){
					let jobReply_no = this.jobReply_no;
					let user_id = this.user_id;
					let user_name = this.user_name;
					let jobReply_content = this.jobReply_content;
					let jobReply_date = this.jobReply_date;
					jobReply_content = jobReply_content.replace(/(\r\n|\r\n)/g, "<br/>");
					template(jobReply_no,user_name,jobReply_content,jobReply_date,user_id);
				});
			}).fail(function(){
				alert("댓글 목록을 불러오는데 실패하였습니다. 잠시후에 다시 시도해 주세요.");
			});
		}
		
		function template(jobReply_no,user_name,jobReply_content,jobReply_date,user_id){
			let $div = $('#reviewList');
			
			let $element = $('#item-template').clone().removeAttr('id');
			$element.attr("data-num",jobReply_no);
			$element.addClass("reply");
			$element.find('.panel-heading > .panel-title > .name').html(user_name);
			$element.find('.panel-heading > .panel-title > .date').html(jobReply_date);
			
			
			/* 댓글 작성자와 사용자 아이디가 일치 시 댓글 수정 삭제 버튼 생성 */
			if("${member.user_id}" != ""){
				
				if('${member.user_id}' == user_id ){
					
					$element.find('.panel-heading > .panel-title > .panel-btn').html( 
							"<button type='button' class='delBtn' data-btn='delBtn' >삭제</button>"
							+ "<button type='button' class='upBtn' data-btn='upBtn'>수정</button>" );
				}
			}

			
			$element.find('.panel-body').html(jobReply_content);
			
			
			$div.append($element);
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
		function deleteBtn(job_no, jobReply_no){
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
							listAll(job_no);
						}
					}
				});
			}
		}
	</script>
</head>
<body>
	<div>
		<div class="replyContainer">
			<c:choose>
				<c:when test="${member != null}">
					<form id="replyForm">
						<div class="reply_div">
							<table>
								<tr>
									<td id="reply_name">${member.user_name }</td>
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
		
		
		<%--리스트 영역 --%>
		<div id="reviewList">
			<div id="item-template" class="panel">
				<div class="panel-heading">
					<div class="panel-title">
						<span class="name" id="user_name"></span>
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