<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">

<script type="text/javascript" src="/resources/include/dist/js/common.js"></script>
<style>
.commentContainer{
	width: 100%;
    margin-bottom: 3rem;
}
.comment_div {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
    max-width: 800px; /* 최대 너비 지정 (선택 사항) */
    margin: 0 auto;
    border: 1px solid #BDBDBD;
    padding: 10px 30px;
    border-radius: 10px;
    margin-bottom: 35px;
}
#comment_name {
	font-size: 15px;
	font-weight: 700;
	color: #6E6E6E;
}

.comment-insert-btn {
    padding: 8px 16px;
    float: right;
    border: none;
    background-color: #BFC8EA;
    border-radius: 10px;
    color: #F8F9FC;
    font-weight: 700;
    font-size: 14px;
    transition: all 0.2s ease-in-out;
    margin-top: 10px;
    align-self: flex-end;
}

.insert-btn:hover{
	background-color: #CAD1F0; /* hover 시 더 밝게 */
    border-color: #bbb; /* 테두리도 변경 */
    transform: scale(1.05); /* 살짝 커지는 효과 */
}

textarea.comment {
    width: 100%; 
    min-height: 100px;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 5px;
    resize: none;
}
.comment-input-container {
    width: 100%;
    margin-top: 10px;
}
</style>
<script>
$(function(){
	listAll();
	renderCommentForm();
})

function listAll(){
	fetch('/api/member/me/session')
    .then(response => response.json())
    .then(data => {
    	let loggedInUserId = data.userId;
    	let loggedInUserName = data.userName;
    	
    	setList(loggedInUserId, loggedInUserName);
    })
    
	let post_id = ${post_id};
	
	
	/*
	$(".reply").detach();
	$.getJSON(url, function(data){ 
		$(data).each(function(){
			let comment_id = this.comment_id;
			let user_id = this.user_id;
			let user_name = this.user_name;
			let content = this.content;
			let created_at = this.created_at;
			
			content = content.replace(/(\r\n|\r\n)/g, "<br/>");
			template(comment_id, user_name, content, created_at, user_id, loggedInUserId);
		});
	}).fail(function(){
		alert("댓글 목록을 불러오는데 실패하였습니다. 잠시후에 다시 시도해 주세요.");
	});*/
}

function setList(loggedInUserId, loggedInUserName){
	setReplyInput(loggedInUserId, loggedInUserName);	
	
	fetch('/fleaMarket/comment/all/' + post_id)
	.then(response => response.json())
	.then(data =>{
		$(data).each(function(){
			let comment_id = this.comment_id;
			let user_id = this.user_id;
			let user_name = this.user_name;
			let content = this.content;
			let created_at = this.created_at;
			
			content = content.replace(/(\r\n|\r\n)/g, "<br/>");
			template(comment_id, user_name, content, created_at, user_id, loggedInUserId);
		});
	})
}

function setReplyInput(userId, userName){
	if(userId == null){
		// textarea를 readonly로 변경
        const textarea = document.getElementById('comment');
        textarea.readOnly = true;
        
        // 로그인 메세지 생성
        const replyLogin = document.getElementById("comment_login");
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
		$("#comment_name").text(userName);
}

function template(comment_id, user_name, content, created_at, user_id, loggedInUserId){
	let $div = $('#reviewList');
	
	let $element = $('#item-template').clone().removeAttr('id');
	$element.attr("data-num", comment_id);
	$element.addClass("reply");
	$element.find('.panel-heading > .panel-title > .name').html(user_name);
	$element.find('.panel-heading > .panel-title > .frmPopup').attr("id", "frmPopup_" + comment_id);
	$element.find('.panel-heading > .panel-title > .frmPopup > #receiver_id').attr("value", user_id);
	$element.find('.panel-heading > .panel-title > .frmPopup > #receiver').attr("value", user_name);
	
	
	$element.find('.panel-heading > .panel-title > .date').html(created_at);
	
	$element.find('.panel-body').html(content);
	
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
	$element.find('.panel-heading > .panel-title > .message > .send_message').attr("onclick", "sendMsg('" + comment_id + "')");
}

/*입력 폼 초기화*/
function dataReset(){
	$("#commentForm").each(function(){
		this.reset();
	});
	$("#commentForm button[type='button']").removeAttr("data-rnum");
	$("#commentForm button[type='button']").attr("id","reply_insert");
}

/* 댓글 삭제 */
function deleteBtn(comment_id, post_id, loggedInUserId){
	if(confirm("댓글을 삭제하겠습니까?")){
		$.ajax({
			url : "/fleaMarket/comment/" + comment_id,
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
					listAll(flea_no, loggedInUserId);
				}
			}
		});
	}	
}
</script>	


</head>
<body class="wrap">
	<div>
		<!-- 댓글 작성부 -->
		<div class="commentContainer">
			<input type="hidden" id="post_id" value="${post_id }">
			
			<div class="comment_div">
				<div id="comment_login"></div>
				
				<div id="comment_userName">
					<span id="comment_name"></span>
				</div>
				
				<div class="comment-input-container">
					<textarea id="comment" name="content" class="comment form-control" rows="3" ></textarea>
				</div>
				
				<button type="button" id="comment_insert" class="comment-insert-btn">등록</button>
			</div>
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