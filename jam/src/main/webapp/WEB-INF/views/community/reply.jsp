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
<script type="text/javascript" src="/resources/include/dist/js/common.js"></script>
<script type="text/javascript" src="/resources/include/dist/js/jquery-3.7.1.min.js"></script>
		
<script>
	$(function(){
		listAll();
		
		// 댓글 입력
		$("#reply_insert").click(function(){
			
			let insertUrl = "/comreplies/reply";
			
			// 입력 값 검증
			
			let value = JSON.stringify({
				com_no:${com_no},
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
					console.log(textStatus + "(HTTP-" +xhr.status+" / "+errorThrown+")");
					alert("댓글 작성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
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
						dataReset();
						listAll();
						alert("등록 되었습니다.");
					}else alert("댓글 작성 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
				}
			});
			
		})
		
		// 수정 버튼 클릭
		$(document).on("click","button[data-btn='upBtn']",function(){
			let panel = $(this).parents("div.panel");
			let comReply_no = panel.attr("data-num");
			updateForm(comReply_no, panel);
		});
		
		// 수정 할 댓글 html 구성
		function updateForm(comReply_no, panel){
			$("#user_name").val(panel.find(".panel-title .name").html());
			$("#user_name").prop("readonly",true);
			let content = panel.find(".panel-body").html();
			content = content.replace(/(<br>|<br\/>|<br \/>)/g, '\r\n'); 
			//$("#comReply_content").val(content);
			let id = panel.find(".panel-title .name");
			let panelbody = panel.find(".panel-body");
			panelbody.html("<textarea id='upReplyContent' rows='3' cols='30' id='tt_"+id+"'>"+content+"</textarea>");
			
			let panelbtn = panel.find(".panel-btn");
			panelbtn.html(
				"<button type='button' id='cancel'>취소</button>" +
				"<button type='button' id='replyUpdateBtn' data-comReply_no =" + comReply_no + ">등록</button>");
			
		}	
		// 수정 취소 버튼 클릭
		$(document).on("click","#cancel",function(){
			listAll();
		})
		
		// 댓글 수정 
		$(document).on("click","#replyUpdateBtn",function(){
			let comReply_no = $(this).attr("data-comReply_no");
			let panel_title = $(this).parents(".panel-title");
			let comReply_contents = panel_title.find('.panel-body > #upReplyContent').val();
			
			$.ajax({
				url:'/comreplies/reply',
				type:'put',
				headers: {
					"Content-Type" : "application/json",
					"X-HTTP-Method-Override" : "PUT"},
				data:JSON.stringify({
					comReply_no:comReply_no,
					comReply_content:comReply_contents,
				}),
				dataType:'text',
				error:function(xhr, textStatus, errorThrown){
					console.log(textStatus + " (HTTP-"+xhr.status+" / " + errorThrown + ")");
					alert("댓글 수정 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
				},
				beforeSend:function(){
					if(!checkForm("#upReplyContent","댓글 내용을")) return false;
				},
				success:function(result){
					console.log("result: "+result);
					if(result == "SUCCESS"){
						
						dataReset();
						listAll();
						alert("수정이 완료되었습니다.");
					}
				}
			});
		});
		
		// 댓글 삭제 버튼 클릭
		$(document).on("click","button[data-btn='delBtn']",function(){
			let comReply_no = $(this).parents("div.panel").attr("data-num");
			deleteBtn(comReply_no);
		});
		
	}) // end
		
	function listAll() {
	    fetch('/api/member/me/session')
	        .then(response => {
	        	if(response.status === 401) {
	        		setList(null);
	        		return null;
	        	}
	        	else return response.json(); 
	        })
	        .then(data => {
	        	console.log(data);
	        	if(data)setList(data.userId, data.userName);
	        })
	        .catch(error => {
	            console.error("Error fetching user info:", error);
	            setList(null); 
	        });
	}
		
	function setList(loggedInUserId, loggedInUserName){
		setReplyInput(loggedInUserId, loggedInUserName);
			
		$(".reply").detach();
		    
		let url = "/comreplies/reply/" + ${com_no};
		 	
		$.getJSON(url, function(data){ 
			console.log(data);
			$(data).each(function(){
				let comReply_no = this.comReply_no;
				let user_id = this.user_id;
				let user_name = this.user_name;
				let comReply_content = this.comReply_content;
				let comReply_date = this.comReply_date;
				comReply_content = comReply_content.replace(/(\r\n|\r\n)/g, "<br/>");
				template(comReply_no,user_name,comReply_content,comReply_date,user_id, loggedInUserId);
			});
		}).fail(function(){
			alert("댓글 목록을 불러오는데 실패하였습니다. 잠시후에 다시 시도해 주세요.");
		});
	}
	
	function setReplyInput(userId, userName){
		if(userId == null){
			// textarea를 readonly로 변경
            const textarea = document.getElementById('comReply_content');
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
		}else
			$("#reply_name").text(userName);
	}
	
	function template(comReply_no,user_name,comReply_content,comReply_date,user_id, loggedInUserId){
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
		
		// 사용자가 로그인 중이면 
		if (loggedInUserId != null) {
			
			// 댓글 작성자와 사용자 아이디가 일치 시 댓글 수정 삭제 버튼 생성 
			if(loggedInUserId == user_id ){
				$element.find('.panel-heading > .panel-title > .panel-btn').html( 
						"<button type='button' class='delBtn' data-btn='delBtn' >삭제</button>"
						+ "<button type='button' class='upBtn' data-btn='upBtn'>수정</button>" );
			}else {  
				// 댓글 작성자와 사용자 아이디 불일치 시 메세지 버튼 생성
				$element.find('.panel-heading > .panel-title > .message').html(
						"<button type='button' class='send_message'>"
						+ "<img class='message_icon' style='width:2rem;' alt='쪽지' src='/resources/include/images/message_icon.svg'>"
						+ "</button>");
			}
		}
		// 메세지 버튼에 click 이벤트 
		$element.find('.panel-heading > .panel-title > .message > .send_message').attr("onclick", "sendMsg('" + comReply_no + "')");
	}
	
	//입력 폼 초기화
	function dataReset(){
		$("#replyForm").each(function(){
			this.reset();
		});
		$("#replyForm button[type='button']").removeAttr("data-rnum");
		$("#replyForm button[type='button']").attr("id","reply_insert");
	}
	
	// 댓글 삭제 
	function deleteBtn(comReply_no){
		console.log(comReply_no);
		if(confirm("댓글을 삭제하겠습니까?")){
			$.ajax({
				url : "/comreplies/reply/"+comReply_no,
				type: 'delete',
				dataType : 'text',
				error : function(xhr, textStatus, errorThrown){ 
					console.log(textStatus + " (HTTP -" +xhr.status + " / " + errorThrown + ")");
					alert("댓글 삭제 중 오류가 발생했습니다. 잠시후 다시 이용해주세요");
				},
				success : function(result){
					console.log("result : "+result);
					if(result == 'SUCCESS'){
						alert("댓글 삭제가 완료되었습니다.");
						dataReset();
						listAll();
					}
				}
			});
		}	
	}
	
	// 쪽지 전송 팝업 
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
<body class="wrap">
	<div>
		<!-- 댓글 작성부 -->
		<div class="replyContainer">
		    <input type="hidden" id="com_no" value="${com_no }">
		
		    <div class="reply_div">
		        <!-- 로그인 정보 -->
		        <div id="reply_login"></div>
		
		        <!-- 닉네임 -->
		        <div id="reply_userName">
		            <span id="reply_name"></span>
		        </div>
		
		        <!-- 댓글 입력창 -->
		        <div class="reply-input-container">
		            <textarea id="comReply_content" name="comReply_content" class="reply_content form-control" rows="3"></textarea>
		        </div>
		
		        <!-- 등록 버튼 -->
		        <button type="button" id="reply_insert" class="replyInsert-btn">등록</button>
		    </div>
		</div>
		
				
		<%-- 댓글 리스트 --%>
		<div id="reviewList">
			<div id="item-template" class="panel">
				<div class="panel-heading">
					<div class="panel-title">
						<span class="cursor-pointer name " id="user_name"></span>
						<div class="message inline"></div>
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