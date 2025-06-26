<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 커뮤니티</title>

  <!-- 서머노트를 위해 추가해야할 부분 -->
  <script src="/resources/include/dist/summernote/summernote-lite.js"></script>
  <script src="/resources/include/dist/summernote/summernote-ko-KR.js"></script>
  <link rel="stylesheet" href="/resources/include/dist/summernote/summernote-lite.css">

	<script>
		$(function(){
			// summernote 초기화
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
			
			// summernote 이미지 업로드 함수
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
			
			// community no
			let com_no = $("#com_no").val();
			
			// 수정할 글 정보 불러오는 함수
			function getEditBoard(){
				
				if(com_no == null) {
					alert("게시글을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
					$(location).attr('href','/community/boards');
				}else{
					fetch('/api/community/board/edit/'+com_no)
					.then(response => {
						if(!response.ok){
							throw new Error('Network response was not ok')
						}
						return response.json();
					})
					.then(data => {
						console.log(data);
						console.log(data.com_title);
						console.log(data.com_content);
						$("#com_title").val(data.com_title);
			        	$('#com_content').summernote('code', data.com_content);
					})
				}
			}
			
			getEditBoard();
			
			
			let loggedInUserId;
		    let loggedInUsername;
		    
		    
			// 수정 버튼 클릭
			$("#update").click(async function(){
				
				// 유효성 검사
				let com_title = $("#com_title").val();
				let com_content = $("#com_content").val();
				
				if(com_title.replace(/\s/g,"") == ""){
					alert("제목을 입력하세요.");
					$("#com_title").focus();
					return false;
				}
				
				if(com_content.replace(/\s/g,"") == ""){
					alert("본문을 입력하세요.");
					$("#com_content").focus();
					return false;
				}
				
			    
		        try {
		        	// 사용자의 아이디와 닉네임을 가져옵니다. 
		            await getUserInfo();
		            
		            var data = {
		            	'com_no': com_no,
		                'com_title': com_title,
		                'com_content': com_content,
		                'user_id': loggedInUserId,
		                'user_name': loggedInUsername
		            };

		            const response = await fetch('/api/community/board', {
		                method: 'PUT',
		                headers: {
		                    'Content-Type': 'application/json'
		                },
		                body: JSON.stringify(data)
		            });

		            if (response.ok) {
		            	alert("수정이 완료되었습니다.");
		                const body = await response.text();
		                if (body) {
		                    $(location).attr('href', '/community/board/' + body);
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
		    
			// 사용자 아이디, 닉네임 가져오는 함수
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
					<input type="hidden" name="user_id" >
					<input type="hidden" name="user_name">
					<input type="hidden" id="com_no" name="com_no" value="${com_no }">
				</div>
				<div class="my-bottom-4">
					<label>제목</label>
				</div>
				<div class="my-bottom-4">
					<input type="text" id="com_title" class="com_title height4 border width-85 border-radius-10" id="com_title" name="com_title" >
				</div>
				<div class="my-bottom-4">
					<label>본문</label>
				</div>
				<div class="content">
					<textarea id="com_content" class="summernote" name="com_content" style="resize:none;"></textarea>    
				</div>
				<div class=" flex justify-center my-top-8">
					<button type="button" class="comWriteBtn" id="update">수정</button>
					<a href="/community/board/${com_no }"  class="comWriteBtn text-center" id="cancel">취소</a>
				</div>
			</form>
		</div>
	</div>
	
</body>
</html>