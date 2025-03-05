<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 커뮤니티</title>

  <script src="/resources/include/dist/summernote/summernote-lite.js"></script>
  <script src="/resources/include/dist/summernote/summernote-ko-KR.js"></script>
  <link rel="stylesheet" href="/resources/include/dist/summernote/summernote-lite.css">
	
	
	<script>
	$(function() {
		fetch('/api/member/checkAuthentication')
		.then(response =>{
			if(!response.ok){
				alert('시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
				locaion.attr('/community/boards');
			}
			return response.json();
		})
		.then((data) => {
			if(!data.authenticated){
				if(confirm("로그인 후 이용할 수 있는 서비스 입니다. 로그인 하시겠습니까?"))$(location).attr('href', '/member/login');
				else $(location).attr('href','/community/boards');
			}
		})
	    
	    $("#write").click(function () {
	    	
		    // 유효성 검사
	        let com_title = $("#com_title").val();
	        let com_content = $("#com_content").val();

	        if (com_title.replace(/\s/g, "") == "") {
	            alert("제목을 입력하세요.");
	            $("#com_title").focus();
	            return false;
	        }

	        if (com_content.replace(/\s/g, "") == "") {
	            alert("본문을 입력하세요.");
	            $("#com_content").focus();
	            return false;
	        }

		    
	        try {
	            var data = {
	                'com_title': com_title,
	                'com_content': com_content
	            };

	            fetch('/api/community/board', {
	                method: 'POST',
	                headers: {
	                    'Content-Type': 'application/json'
	                },
	                body: JSON.stringify(data)
	            })
	            .then(response =>{
	            	if (response.ok) {
		            	alert("등록이 완료되었습니다.");
		            	
		            	return response.json();
		            }if(response === 401){
		            	alert("로그인이 필요한 작업입니다. 로그인 후 다시 시도해 주세요.");
		            	$(location).attr('href', '/member/login');
		            }else {
		                let errorText = response.json();
		                throw new Error(errorText);
		            }
	            })
	            .then(data =>{
	            	$(location).attr('href', '/community/board/' + data);
	            })
	        } catch (error) {
	            alert("게시글 작성을 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
	            console.error('Error:', error);
	        }
	    });
	    
	});
		
	</script>
</head>
<body class="wrap">
	<div class="rem-30">
		<div class="title flex justify-center">
			<h2>커뮤니티</h2>
		</div>
		<div class="content flex justify-center" >
			<form id="comWrite">
				<div>
					<input type="hidden" id="user_id" name="user_id" > 
					<input type="hidden" id="user_name" name="user_name" >
				</div>
				<div>
					<label>제목</label>
				</div>
				<div>
					<input type="text" class="com_title height4 border width-85 border-radius-10" id="com_title" name="com_title">
				</div>
				<div>
					<label>본문</label>
				</div>
				<div class="content">
					<textarea id="com_content" class="summernote" name="com_content" style="resize: none;"></textarea>    
				</div>
				<div class=" flex justify-right my-top-8">
					<button type="button" class="comWriteBtn mr-1" id="write">등록</button>
					<a href="/community/communityList"  class="comWriteBtn text-center">취소</a>
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