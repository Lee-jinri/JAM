<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - JOB</title>
  <!-- 서머노트를 위해 추가해야할 부분 -->
  <script src="/resources/include/dist/summernote/summernote-lite.js"></script>
  <script src="/resources/include/dist/summernote/summernote-ko-KR.js"></script>
  <link rel="stylesheet" href="/resources/include/dist/summernote/summernote-lite.css">

	<script>
		$(function(){
			
			let city;
			let gu;
			let dong;
			
			$("#update").click(function(){
				let job_title = $("#job_title").val();
				let job_content = $("#job_content").val();
				let pay = $("#pay").val();
				
				// job_category? pay_category?
						
				if(city == null || gu == null || dong == null){
					alert("지역을 선택하세요.");
					return false;
				}		
				
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
				
				// 사용자 id, name 가져옴
				fetch('http://localhost:8080/api/member/getUserInfo', {
			        method: 'GET',
			        headers: {
			            'Authorization': localStorage.getItem("Authorization")
			        },
			    })
			    .then(response => {
			        if (response.ok) {
			        	user_id = response.headers.get('user_id');
			            $("#user_id").val(user_id);
			            
			            if(user_id == null) $(location).attr('href', '/member/login');
			            
			            return response.text();
			        } else {
			            throw new Error('Network response was not ok');
			        }
			    })
			    .then((user_name) => {
		        	if (user_name) {
						$("#user_name").val(user_name);
						
						$("#jobUpdate").attr({
							"action" : "/job/jobUpdate",
							"enctype": "multipart/form-data",
							"method" : "post"
						})
						
						$("#jobUpdate").submit();
						/* 글 수정 중 오류 발생 */
						let result = $("#result");
						
						if(result == 'error') alert("게시글 수정을 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
						else alert("수정이 완료되었습니다.");
	                
		            }
		        	else $(location).attr('href', '/member/login');
				})
			    .catch(error => {
			        console.error('사용자 정보를 가져오는 중 오류 발생:', error);
			    });
			})
			
			
		})
	</script>
</head>
<body class="wrap">
	<div>
		<input type="hidden" value="${result }">
	</div>
	<div class="rem-30 my-top-15 my-bottom-15">
		<div class="title flex justify-center my-bottom-8" >
			<h2>구인 / 구직</h2>
		</div>
		<div class="content flex justify-center" >
			<form id="jobUpdate">
				<div>
					<input type="hidden" name="user_id"> 
					<input type="hidden" name="user_name">
					<input type="hidden" name="job_no" value="${updateData.job_no }">
				</div>
				<div>
					<label class="my-bottom-4"><input type="checkbox" name="job_status" value=1> 구인,구직 완료 시 체크하세요.</label>
				</div>
				<div class="flex">
					<div class="block">
						<select name="city" id="city">
							<option value="" class="city">시·도</option>
							<option value="서울" data-index="11" class="city">서울</option>
							<option value="부산" data-index="21" class="city">부산</option>
							<option value="대구" data-index="22" class="city">대구</option>
							<option value="인천" data-index="23" class="city">인천</option>
							<option value="광주" data-index="24" class="city">광주</option>
							<option value="대전" data-index="25" class="city">대전</option>
							<option value="울산" data-index="26" class="city">울산</option>
							<option value="세종" data-index="29" class="city">세종</option>
							<option value="경기" data-index="31" class="city">경기</option>
							<option value="강원" data-index="32" class="city">강원</option>
							<option value="충북" data-index="33" class="city">충북</option>
							<option value="충남" data-index="34" class="city">충남</option>
							<option value="전북" data-index="35" class="city">전북</option>
							<option value="전남" data-index="36" class="city">전남</option>
							<option value="경북" data-index="37" class="city">경북</option>
							<option value="경남" data-index="38" class="city">경남</option>
							<option value="제주" data-index="39" class="city">제주</option>
						</select>
					</div>
					
					<div id="guDiv" class="">
						<select name="gu" id="gu">
							<option value="">시·구·군</option>
						</select>
					</div>
					
					<div id="dongDiv" class="">
						<select name="dong" id="dong">
							<option value="">동·읍·면</option>
						</select>
					</div>
				</div>
				<div class="flex my-bottom-7 items-center">
					<select name="job_category" class="mr-1">
						<option value=0>직원 구인</option>
						<option value=1>직원 구직</option>
						<option value=2>멤버 구인</option>
						<option value=3>멤버 구직</option>
					</select><br/>
					
					<select name="pay_category" class="mr-2">
						<option value=0>일급</option>
						<option value=1>주급</option>
						<option value=2>월급</option>
					</select><br/>
					
					<label class="mr-1">급여</label>
					<input type="number" name="pay" id="pay" value="${updateData.pay }">&nbsp;원
				</div>
				
				<div class="my-bottom-4">
					<label>제목</label>
				</div>
				<div class="my-bottom-4">
					<input type="text" class="job_title my-bottom-7 height4 border width-85 border-radius-10" id="job_title" name="job_title" value="${updateData.job_title }">
				</div>
				<div class="my-bottom-4">
					<label>본문</label>
				</div>
				<div class="content">
					<textarea id="job_content" class="summernote" name="job_content" style="resize:none;">${updateData.job_content }</textarea>
				</div>
				<div class=" flex justify-right my-top-8">
					<button type="button" class="jobUpdateBtn mr-1" id="update">수정</button>
					<a href="/job/jobDetail/${updateData.job_no }"  class="jobUpdateBtn text-center" >취소</a>
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
		fontNames: ['Arial', 'Arial Black', 'Comic Sans MS', 'Courier New','맑은 고딕','궁서','굴림체','굴림','돋움체','바탕체'],
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