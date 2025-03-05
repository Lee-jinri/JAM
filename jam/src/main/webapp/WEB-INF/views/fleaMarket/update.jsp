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
			
			
			let flea_no = $("#flea_no").val();
			
			// 수정할 글 정보 불러오는 함수
			function getEditBoard(){
				
				if(flea_no == null){
					alert("게시글을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
					$(location).attr('href','/fleaMarket/boards');
				}else{
					fetch('/api/fleaMarket/board/edit/'+flea_no)
					.then(response => {
						if(!response.ok) throw new Error('Network response was not ok.')
						return response.json();
					})
					.then(data => {
						$("#flea_title").val(data.flea_title);
						$("#flea_content").summernote('code', data.flea_content);
						$("#price").val(data.price);
						
						// 카테고리가 판매인지 구매인지 판단
						const selectElement = document.getElementById("flea_category");

						selectElement.value = data.flea_category.toString();
						
						// 판매 완료라면 checked로 변경
						const saleDone = document.getElementById("saleDone");
						if(data.sales_status == 1) saleDone.checked = true;
						
						
					})
				}
			}
			
			getEditBoard();
			
			let loggedInUserId;
			let loggedInUsername;
			
			// 수정 버튼 클릭
			$("#update").click(async function(){
				
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
				
				try{
					// 사용자의 아이디와 닉네임 가져오기
					await getUserInfo();
					
					// 판매완료인지도 가져와야됨
					// flea_category 판매인지 구매인지 가져와야됨
					let flea_category = $("#flea_category").val();
					
					let sales_status = 0; 
					if($("#saleDone").is(":checked")) sales_status = 1;
					
					let data ={
							'flea_no':flea_no,
							'flea_title':flea_title,
							'flea_content':flea_content,
							'price':price,
							'flea_category': flea_category,
							'sales_status': sales_status,
							'user_id':loggedInUserId,
							'user_name':loggedInUsername
					};
					
					const response = await fetch('/api/fleaMarket/board',{
						method :'PUT',
						headers:{
							'Content-Type':'application/json'
						},
						body: JSON.stringify(data)
					});
					
					if(response.ok){
						alert('수정이 완료되었습니다.');
						const body = await response.text();
						if(body){
							$(location).attr('href','/fleaMarket/board/'+body);
						}
					} else{
						const errorText = await response.text();
						throw new Error(errorText);
					}
				}catch(error){
					alert("게시글 작성을 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
					console.error('Error: ', error);
				}
			});
			
			// 사용자의 아이디, 닉네임 가져오는 함수
			async function getUserInfo(){
				try{
					const response = await fetch('http://localhost:8080/api/member/getUserInfo',{
						method: 'GET',
						headers:{
							'Authorization': localStorage.getItem("Authorization")
						},
					}).then(response =>{
						if(!response.ok){
							throw new Error('Network response was not ok.');
						}
						return response.json();
					}).then(data =>{
						console.log(loggedInUserId +" " + loggedInUsername);
					
						loggedInUserId = data.user_id;
						loggedInUsername = data.user_name;
						
						if(loggedInUserId == null || loggedInUsername == null){
							alert("로그인이 필요한 작업입니다. 로그인 후 다시 시도해 주세요.");
							$(location).attr('href','/member/login');
						}
					})
					
					}catch(error){
						console.error('사용자 정보를 가져오는 중 오류 발생:', error);
						throw error;
					
				}
			}
			
			
			
		})
	</script>
</head>
<body class="wrap">
	<div class="rem-30 my-top-15 my-bottom-15">
		<div class="title flex justify-center my-bottom-8" >
			<h2>중고 악기</h2>
		</div>
		<div class="content flex justify-center" >
			<form id="fleaUpdate">
				<div>
					<input type="hidden" id="flea_no" name="flea_no" value="${flea_no }">
				</div>
				<div>
					<label class="my-bottom-4"><input type="checkbox" id="saleDone" name="sales_status" value=1> 거래 완료 시 체크하세요.</label>
				</div>
				<div class="flex my-bottom-7 items-center">
					<select id="flea_category" name="flea_category" class="mr-2">
						<option value=0>판매</option>
						<option value=1>구매</option>
					</select><br/>
					
					<label class="mr-1">가격</label>
					<input type="number" name="price" id="price" value="${updateData.price }">&nbsp;원
					<br/>
					
				</div>
				<div class="my-bottom-4">
					<label>제목</label>
				</div>
				<div class="my-bottom-4">
					<input type="text" id="flea_title" class="flea_title my-bottom-7 height4 border width-85 border-radius-10" id="flea_title" name="flea_title" value="${updateData.flea_title }">
				</div>
				<div class="my-bottom-4">
					<label>본문</label>
				</div>
				<div class="content">
					<textarea id="flea_content" class="summernote flea_content height50 width-85 border-radius-10 resize-none" name="flea_content" style="resize: none;">${updateData.flea_content }</textarea>
				</div>
				<div class=" flex justify-right my-top-8">
					<button type="button" class="fleaUpdateBtn mr-1" id="update">수정</button>
					<a href="/fleaMarket/boards/${flea_no }"  class="fleaUpdateBtn text-center">취소</a>
				</div>
			</form>
		</div>
	</div>
	
		<script>
	
	</script>
</body>
</html>