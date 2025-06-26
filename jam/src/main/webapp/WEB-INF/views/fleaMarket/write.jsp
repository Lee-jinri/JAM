<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 중고악기</title>

  <!-- 서머노트를 위해 추가해야할 부분 -->
  <script src="/resources/include/dist/summernote/summernote-lite.js"></script>
  <script src="/resources/include/dist/summernote/summernote-ko-KR.js"></script>
  <link rel="stylesheet" href="/resources/include/dist/summernote/summernote-lite.css">

	<script>
		$(function(){
			checkLoginStatus();
			
			$("#write").click(async function(){
				
				// 유효성 검사
				let flea_title = $("#flea_title").val();
				let flea_content = $("#flea_content").val();
				let price = $("#price").val();
				
				if(flea_title.replace(/\s/g,"") == ""){
					alert("제목을 입력하세요.");
					$("#flea_title").focus();
					return false;
				}
				
				if(flea_content.replace(/\s/g,"") == ""){
					alert("본문을 입력하세요.");
					$("#flea_content").focus();
					return false;
				}
				
				if(price.replace(/\s/g,"") == ""){
					alert("가격을 입력하세요.");
					$("#price").focus();
					return false;
				}
				
				try {
		        	// 사용자의 아이디와 닉네임을 가져옵니다. 
		            await getUserInfo();
		            
		            var data = {
		                'flea_title': flea_title,
		                'flea_content': flea_content,
		                'price': price,
		                'user_id': loggedInUserId,
		                'user_name': loggedInUsername
		            };

		            const response = await fetch('/api/fleaMarket/board', {
		                method: 'POST',
		                headers: {
		                    'Content-Type': 'application/json'
		                },
		                body: JSON.stringify(data)
		            });

		            if (response.ok) {
		            	alert("등록이 완료되었습니다.");
		                const body = await response.text();
		                if (body) {
		                    $(location).attr('href', '/fleaMarket/board/' + body);
		                }
		            } else {
		                const errorText = await response.text();
		                throw new Error(errorText);
		            }
		        } catch (error) {
		            alert("게시글 작성을 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
		            console.error('Error:', error);
		        }
		    });
			
			async function getUserInfo() {
		        try {
		            const response = await fetch('/api/member/me/token', {
		                method: 'GET'
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
		
		function checkLoginStatus(){
		fetch("/api/member/auth/check").then((res) => {
			if (res.status === 401) {
				if (confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")) {
					location.href = "/member/login";
				} else {
					location.href = "/fleaMarket/boards";
				}
			}
		})
	}
	</script>
</head>
<body class="wrap">
	<div>	
		<input type="hidden" id="result" value="${result }">
	</div>	
	<div class="rem-30 my-top-15 my-bottom-15">
		<div class="title flex justify-center my-bottom-8" >
			<h2>중고 악기</h2>
		</div>
		<div class="content flex justify-center" >
			<form id="fleaWrite">
				<div>
					<input type="hidden" id="user_id" name="user_id"> 
					<input type="hidden" id="user_name" name="user_name">
				</div>
				<div class="flex my-bottom-7 items-center">
					<select name="flea_category" class="mr-2">
						<option value=0>판매</option>
						<option value=1>구매</option>
					</select><br/>
					
					<label class="mr-1">가격</label>
					<input type="number" name="price" id="price">&nbsp;원
				</div>
				<div class="my-bottom-4">
					<label>제목</label>
				</div>
				<div>
					<input type="text" class="flea_title my-bottom-7 height4 border width-85 border-radius-10" id="flea_title" name="flea_title">
				</div>
				<div class="my-bottom-4">
					<label>본문</label>
				</div>
				<div class="content">
					<textarea id="flea_content" class="summernote" name="flea_content" style="resize:none;"></textarea>    
				</div>
				<div class=" flex justify-right my-top-8">
					<button type="button" class="fleaWriteBtn mr-1" id="write">등록</button>
					<a href="/fleaMarket/fleaMarketList"  class="fleaWriteBtn text-center">취소</a>
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