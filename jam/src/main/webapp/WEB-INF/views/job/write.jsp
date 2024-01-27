<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 구인구직</title>

  <!-- 서머노트를 위해 추가해야할 부분 -->
  <script src="/resources/include/dist/summernote/summernote-lite.js"></script>
  <script src="/resources/include/dist/summernote/summernote-ko-KR.js"></script>
  <link rel="stylesheet" href="/resources/include/dist/summernote/summernote-lite.css">
	
	
	<script>
		$(function(){
			let loggedInUserId;
			let loggedInUsername; 
			
			$("#write").click(async function(){
				// 유효성 검사
				let job_title = $("#job_title").val();
				let job_content = $("#job_content").val();
				let pay = $("#pay").val();
				let job_category = $("#job_category").val();
				let pay_category = $("#pay_category").val();
				
				console.log(job_category);
				console.log(pay_category);
				if(pay.replace(/\s/g,"") == ""){
					alert("급여를 입력하세요.");
					$("#pay").focus();
					return false;
				}
				
				if(job_title.replace(/\s/g,"") == ""){
					alert("제목을 입력하세요.");
					$("#job_title").focus();
					return false;
				}
				
				if(job_content.replace(/\s/g,"") == ""){
					alert("본문을 입력하세요.");
					$("#job_content").focus();
					return false;
				}
				
				
				try{
					// 사용자의 아이디와 닉네임을 가져옵니다.
					await getUserInfo();
					
					var data = {
							'job_title':job_title,
							'job_content':job_content,
							'job_category':job_category,
							'pay':pay,
							'pay_category':pay_category,
							'user_id':loggedInUserId,
							'user_name':loggedInUsername
					};
					
					const response = await fetch('/api/job/board',{
						method: 'POST',
						headers: {
							'Content-Type':'application/json'
						},
						body: JSON.stringify(data)
					});
					
					if(response.ok){
						alert("등록이 완료되었습니다.");
						const body = await response.text();
						
						if(body){
							$(location).attr('href','/job/board/'+body);
						}
					}else{
						const errorText = await response.text();
						throw new Error(errorText);
					}
				}catch(error){
					alert("게시글 작성을 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
					console.error('Error:',error);
				}
			});
			
			async function getUserInfo() {
		        try {
		            const response = await fetch('http://localhost:8080/api/member/getUserInfo', {
		                method: 'GET',
		                headers: {
		                    'Authorization': localStorage.getItem("Authorization")
		                },
		            }).then(response => {
		            	if (!response.ok) {
		            		throw new Error('Network response was not ok');
				            
				        } 
		            	return response.json();
		            }).then(data => {
						loggedInUserId = data.user_id;
						loggedInUsername = data.user_name;
	            		
			            if(loggedInUserId == null || loggedInUsername == null) {
			            	alert("로그인이 필요한 작업입니다. 로그인 후 다시 시도해 주세요.");
			            	$(location).attr('href', '/member/login');
			            }
		            })	
		        } catch (error) {
		            console.error('사용자 정보를 가져오는 중 오류 발생:', error);
		            
		            throw error;
		        }
		    }
			
			
		})
	</script>
</head>
<body class="wrap">
	<div>	
		<input type="hidden" id="result" value="${result }">
	</div>
	<div class="rem-30 my-top-15 my-bottom-15">
		<div class="title flex justify-center my-bottom-8" >
			<h2>구인 / 구직</h2>
		</div>
		<div class="content flex justify-center" >
			<form id="jobWrite">
				<div>
					<input type="hidden" id="user_id" name="user_id"> 
					<input type="hidden" id="user_name" name="user_name">
				</div>
				<div class="flex my-bottom-7 items-center">
					<select id="job_category" name="job_category" class="mr-1">
						<option value=0>직원 구인</option>
						<option value=1>직원 구직</option>
						<option value=2>멤버 구인</option>
						<option value=3>멤버 구직</option>
					</select><br/>
					
					<div class="">
						<select id="pay_category" name="pay_category" class="mr-2">
							<option value=0>일급</option>
							<option value=1>주급</option>
							<option value=2>월급</option>
						</select><br/>
						
						<label class="mr-1">급여</label>
						<input type="number" name="pay" id="pay">&nbsp;원
					</div>
				</div>
				<div class="my-bottom-4">
					<label>제목</label>
				</div>
				<div>
					<input type="text" class="job_title my-bottom-7 height4 border width-80 border-radius-10" id="job_title" name="job_title">
				</div>
				<div class="my-bottom-4">
					<label>본문</label>
				</div>
				<div class="content">
					<textarea id="job_content" class="summernote" name="job_content" style="resize:none;"></textarea>    
				</div>
				<div class=" flex justify-right my-top-8">
					<button type="button" class="jobWriteBtn mr-1" id="write">등록</button>
					<a href="/job/jobList"  class="jobWriteBtn text-center">취소</a>
					
					
				</div>
			</form>
		</div>
	</div>
	
<script>
$('.summernote').summernote({
	toolbar: [
	    ['fontname', ['fontname']],
	    ['fontsize', ['fontsize']],
	    ['style', ['bold', 'italic', 'underline','strikethrough', 'clear']],
	    ['color', ['forecolor','color']],
	    ['table', ['table']],
	    ['para', ['ul', 'ol', 'paragraph']],
	    ['height', ['height']],
	    ['insert',['picture','link','video']]
	],
	fontNames: ['Arial', 'Arial Black', 'comic Sans MS', 'Courier New','맑은 고딕','궁서','굴림체','굴림','돋움체','바탕체'],
	fontSizes: ['8','9','10','11','12','14','16','18','20','22','24','28','30','36','50','72'],
	height: 450,
	lang: "ko-KR",
	placeholder : "내용을 작성하세요.",
	callbacks : {
    	onImageUpload : function(files, editor, welEditable){
    		for(var i = files.length - 1; i >= 0; i--){
    			uploadImageFile(files[i],this);
    		}
    	}
    }
});
function uploadImageFile(file, el) {
	data = new FormData();
	data.append("file", file);
	$.ajax({                                                              
		data : data,
		type : "POST",
		url : 'uploadImageFile',
		contentType : false,
		enctype : 'multipart/form-data',
		processData : false,
		success : function(data) {                                         
			$(el).summernote('editor.insertImage',data.url);
		}
	});
}
</script>
</body>
</html>