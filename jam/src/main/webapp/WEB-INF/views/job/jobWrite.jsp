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
	
	
	
	
	<style type="text/css">
		.width-85 {width:80rem;}
		.border-radius-10 {border-radius:10px;}
		.job_title {border: 3px solid #ffdd77; }
		.job_content{    border: 3px solid #ffdd77;}
		.resize-none {resize: none;}
		.height4 {height:4rem;}
		#textarea{min-height:50rem;}
		.jobWriteBtn{
			border-radius: 10px;
		    border: 3px solid #ffdd77;
		    background-color: #fff;
		    height: 35px;
    		width: 60px;
		    font-weight: 600;
			align-items: center;
		    justify-content: center;
		    display: flex;
		    color : #848484;
		}
	</style>
	<script>
		$(function(){
			$("#write").click(function(){
				let job_title = $("#job_title").val();
				let job_content = $("#job_content").val();
				let pay = $("#pay").val();
				
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
				
				$("#jobWrite").attr({
					"action" : "/job/jobWrite",
					"enctype": "multipart/form-data",
					"method" : "post"
				})
				
				$("#jobWrite").submit();
				
				alert("등록이 완료되었습니다.");
			})
			
		})
	</script>
</head>
<body>
	<div class="rem-30 my-top-15 my-bottom-15">
		<div class="title flex justify-center my-bottom-8" >
			<h2>구인 / 구직</h2>
		</div>
		<div class="content flex justify-center" >
			<form id="jobWrite">
				<div>
					<input type="hidden" name="user_id" value="${member.user_id }"> 
					<input type="hidden" name="user_name" value="${member.user_name }">
				</div>
				<div class="flex my-bottom-7 items-center">
					<select name="job_category" class="mr-1">
						<option value=0>구인</option>
						<option value=1>구직</option>
					</select><br/>
					
					<select name="pay_category" class="mr-2">
						<option value=0>일급</option>
						<option value=1>주급</option>
						<option value=2>월급</option>
					</select><br/>
					
					<label class="mr-1">급여</label>
					<input type="number" name="pay" id="pay">&nbsp;원
				</div>
				<div class="my-bottom-4">
					<label>제목</label>
				</div>
				<div>
					<input type="text" class="job_title my-bottom-7 height4 border width-85 border-radius-10" id="job_title" name="job_title">
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