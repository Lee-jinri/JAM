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
<script type="text/javascript" src="/resources/include/dist/js/jquery-3.7.1.min.js"></script>
<script type="text/javascript" src="/resources/include/dist/js/common.js"></script>


<style>

.delBtn, .upBtn, #replyUpdateBtn, #cancel {
    float: right;
    border: none;
    background-color: #fff;
    font-size: 15px;
    color: #444; /* 기존보다 약간 어두운 색상 */
    padding: 4px 8px;
    transition: all 0.2s ease-in-out; /* 부드러운 효과 */
    cursor: pointer;
}

.delBtn:hover, .upBtn:hover, #replyUpdateBtn:hover, #cancel:hover {
    background-color: #f0f0f0; /* hover 시 배경색 약간 변경 */
    color: #222; /* hover 시 글자 색 더 강조 */
    text-decoration: underline; /* 클릭 가능하다는 느낌 강조 */
    opacity: 0.8; /* 살짝 흐려졌다가 클릭하면 돌아옴 */
}

</style>
<script>
$(function(){
	listAll();
		
	// 댓글 입력
	$("#reply_insert").click(async function() {

	    let job_no = $("#job_no").val();
	    let jobReply_content = $("#jobReply_content").val().trim();
	
	    let url = "/jobReplies/replyInsert";
	
	    if (!jobReply_content) {
	        alert("댓글을 입력하세요.");
	        $("#jobReply_content").focus();
	        return;
	    }
	
	    let requestData = {
	        job_no: job_no,
	        jobReply_content: jobReply_content
	    };
	
	    try {
	        const response = await fetch(url, {
	            method: "POST",
	            headers: {
	                "Content-Type": "application/json"
	            },
	            body: JSON.stringify(requestData)
	        });
	
	        if (!response.ok) {
	            throw new Error("HTTP-" + response.status + "/"+ response.statusText);
	        }
	
	        const result = await response.json(); 
	
	        if (result.status === "SUCCESS") {
	            alert(result.message);
	            dataReset();
	            listAll();
	        } else {
	            alert(result.message);
	        }
	
	    } catch (error) {
	        alert(error.message);
	        console.error("댓글 등록 error:", error);
	    }
	})		
	
	// 수정 버튼 클릭
	$(document).on("click","button[data-btn='upBtn']",function(){
	let panel = $(this).parents("div.panel")
		let jobReply_no = panel.attr("data-num");
			
		updateForm(jobReply_no, panel);
	});
	
	//댓글수정
	$(document).on("click","#replyUpdateBtn", async function(){
		let jobReply_no = $(this).attr("data-jobReply_no");
		let panel_title = $(this).parents(".panel-title");
		let jobReply_content = panel_title.find('.panel-body > #upReplyContent').val().trim();
		
	    if (!jobReply_content) {
	        alert("댓글을 입력하세요.");
	        $("#upReplyContent").focus();
	        return;
	    }
	
	    try {
	        const response = await fetch("/jobReplies/" + jobReply_no, {
	            method: "PUT",
	            headers: {
	                "Content-Type": "application/json"
	            },
	            body: JSON.stringify({ jobReply_content: jobReply_content }) 
	        });
	
	        if (!response.ok) {
	            throw new Error("HTTP-" + response.status + "/"+ response.statusText);
	        }
	
	        const result = await response.json(); 
	
	        if (result.status === "SUCCESS") {
	            alert(result.message);
	            dataReset();
	            listAll();
	        } else {
	            alert(result.message);
	        }
	
	    } catch (error) {
	        alert(error.message);
	        console.error("댓글 등록 error:", error);
	    }
	});
	
	//수정 취소 버튼 클릭
	$(document).on("click","#cancel",function(){
		listAll();
	})
	
	// 댓글 삭제 버튼 클릭
	$(document).on("click","button[data-btn='delBtn']",function(){
		let jobReply_no = $(this).parents("div.panel").attr("data-num");
		deleteBtn(jobReply_no);
	})
})
		
			
// 수정 할 댓글 구성
function updateForm(jobReply_no, panel){
	$("#user_name").val(panel.find(".panel-title .name").html());
	$("#user_name").prop("readonly",true);
	
	let content = panel.find(".panel-body").html();
	content = content.replace(/(<br>|<br\/>|<br \/>)/g, '\r\n'); //<br><br/><br />
	
	let id = panel.find(".panel-title .name");
	
	let panelbody = panel.find(".panel-body");
	panelbody.html("<textarea id='upReplyContent' rows='3' cols='30'>"+content+"</textarea>");

	let panelbtn = panel.find(".panel-btn");
	panelbtn.html(
		"<button type='button' id='cancel'>취소</button>" +
		"<button type='button' id='replyUpdateBtn' data-jobReply_no =" + jobReply_no + ">등록</button>" );		
}			
		
	
async function getUserInfo() {
	try {
		const response = await fetch('/api/member/decode-token');
        if (!response.ok) throw new Error('JWT 디코딩 실패');
	
        const data = await response.json();
        return data ? { userId: data.userId, userName: data.userName } : null;
	
    } catch (error) {
        console.error("reply getUserInfo error:", error);
        return null;
    }
}
	
async function listAll() {
    $(".reply").detach(); // 기존 댓글 제거
	    try {
        // 로그인한 사용자 정보 가져오기
        const userInfo = await getUserInfo();
        const loggedInUserId = userInfo ? userInfo.userId : null;
	        
	    if (!userInfo || !userInfo.userId) { 
			replyInputUi();
        } else {
            $("#reply_name").text(userInfo.userName);
        }

        
        let job_no = $("#job_no").val();
        let url = "/jobReplies/all/" + job_no;
	        // 댓글 목록 가져오기
        let response = await fetch(url);
        if (!response.ok) throw new Error("댓글 목록 불러오기 실패");
	        const data = await response.json();
	        data.forEach(reply => {
            let { jobReply_no, user_id, user_name, jobReply_content, jobReply_date } = reply;
            jobReply_content = jobReply_content.replace(/(\r\n|\n)/g, "<br/>");
	            // 댓글을 UI에 추가
            template(jobReply_no, user_name, jobReply_content, jobReply_date, user_id, loggedInUserId);
        });
	    } catch (error) {
        console.error("reply listAll error:", error);
        alert("댓글 목록을 불러오는데 실패하였습니다. 잠시 후 다시 시도해주세요.");
    }
}
	
function template(jobReply_no,user_name,jobReply_content,jobReply_date,user_id, loggedInUserId){
	let $div = $('#reviewList');
	
	let $element = $('#item-template').clone().removeAttr('id');
	$element.attr("data-num",jobReply_no);
	$element.addClass("reply");
	$element.find('.panel-heading > .panel-title > .userName').html(user_name);
	
	$element.find('.panel-heading > .panel-title > .frmPopup').attr("id", "frmPopup_" + jobReply_no);
	$element.find('.panel-heading > .panel-title > .frmPopup > #receiver_id').attr("value", user_id);
	$element.find('.panel-heading > .panel-title > .frmPopup > #receiver').attr("value", user_name);
		
	$element.find('.panel-heading > .panel-title > .date').html(jobReply_date);
	$element.find('.panel-body').html(jobReply_content);
	
	$div.append($element);
		
	// 사용자가 로그인 중	
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
}
	
//입력 폼 초기화
function dataReset(){
    $("#jobReply_content").val("");  

    $("#reply_insert").removeAttr("data-rnum"); 
    $("#reply_insert").attr("id", "reply_insert"); 
}
	
// 댓글 삭제
async function deleteBtn(jobReply_no) {
    if (!confirm("댓글을 삭제하겠습니까?")) {
        return;
    }

    try {
        const response = await fetch("/jobReplies/" + jobReply_no, {
            method: "DELETE"
        });

        if (!response.ok) {
            throw new Error("HTTP-" + response.status + " / " + response.statusText);
        }

        const result = await response.json(); 

        if (result.status === "SUCCESS") {
            alert(result.message);
            dataReset();
            listAll();
        } else {
            alert(result.message);
        }
    } catch (error) {
        alert("댓글 삭제 중 오류 발생: " + error.message);
        console.error("댓글 삭제 에러:", error);
    }
}

	
function replyInputUi(){
	// textarea를 readonly로 변경
	const textarea = document.getElementById('jobReply_content');
	textarea.readOnly = true;
			
	// 로그인 메세지 생성
	const replyLogin = document.getElementById("reply_login");
	replyLogin.innerHTML = "";
	
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
	
</script>
</head>
<body class="wrap">
	<div>
		<!-- 댓글 작성부 -->
		<div class="replyContainer">
			<input type="hidden" id="job_no" value="${job_no }">
			<div class="reply_div">
				<div id="reply_login"></div>
				<table>
					<tr id="reply_userName">
						<td id="reply_name"></td>
					</tr>
					<tr>
						<td>
							<textarea id="jobReply_content" name="jobReply_content" class="reply_content form-control" rows="3"></textarea>
						</td>
					</tr>
					<tr>
						<td><button type="button" id="reply_insert" class="replyInsert-btn">등록</button></td>
					</tr>
				</table>
			</div>	
		</div>
		
		
		<%-- 댓글 리스트 --%>
		<div id="reviewList">
			<div id="item-template" class="panel">
				<div class="panel-heading">
					<div class="panel-title">
						<span class="cursor-pointer userName" id="user_name"></span>
						<div class="userNameToggle"></div> 
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