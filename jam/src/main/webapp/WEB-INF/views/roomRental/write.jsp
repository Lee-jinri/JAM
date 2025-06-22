<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 합주실/연습실</title>

  <!-- 서머노트를 위해 추가해야할 부분 -->
  <script src="/resources/include/dist/summernote/summernote-lite.js"></script>
  <script src="/resources/include/dist/summernote/summernote-ko-KR.js"></script>
  <link rel="stylesheet" href="/resources/include/dist/summernote/summernote-lite.css">

	<script>
		$(function(){
			checkLoginStatus();
			
			let loggedInUserId;
			let loggedInUsername; 
			
			let city;
			let gu;
			let dong;
			
			$("#write").click(async function(){
				let roomRental_title = $("#roomRental_title").val();
				let roomRental_content = $("#roomRental_content").val();
				
				if(city == null || gu == null || dong == null){
					alert("지역을 선택하세요.");
					return false;
				}
				
				if(roomRental_title.replace(/\s/g,"") == ""){
					alert("제목을 입력하세요.");
					$("#roomRental_title").focus();
					return false;
				}
				
				if(roomRental_content.replace(/\s/g,"") == ""){
					alert("본문을 입력하세요.");
					$("#roomRental_content").focus();
					return false;
				}
				
				try{
					await getUserInfo();
					
					var data = {
							'roomRental_title':roomRental_title,
							'roomRental_content':roomRental_content,
							'user_id':loggedInUserId,
							'user_name':loggedInUsername,
							'city':city,
							'gu':gu,
							'dong':dong
					};
					
					const response = await fetch('/api/roomRental/board',{
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
							$(location).attr('href','/roomRental/board/'+body);
						}
					}else{
						const errorText = await response.text();
						throw new Error(errorText);
					}
					
				}catch(error){
					alert("게시글 작성을 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
					console.error('Error:',error);
				}
				
			})	
			
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
				
			// 지역 선택
			var accessToken = 'none';													
			var errCnt = 0;
			var accessTimeout;
			
			getAccessToken();	
			
			function getAccessToken(){												
		     	jQuery.ajax({																												
		     		type:'GET', 																											
		     		url: 'https://sgisapi.kostat.go.kr/OpenAPI3/auth/authentication.json',													
		     		data:{																													
		     		consumer_key : '8958e9f0906441ae9b29',																					
		     		consumer_secret : 'c71281e1aca94a479331',																				
		     		},																														
		     		success:function(data){	
		     			errCnt = 0;						
		     			
		     			accessToken = data.result.accessToken;
		     			accessTimeout = data.result.accessTimeout + (4 * 60 * 60 * 1000); // 현재 시간부터 4시간 뒤 만료됨.
		     			
		     			//getLocations('city');												
		     		},																													
		     		error:function(data) {	
		     			alert('시스템 오류 입니다. 잠시 후 다시 시도해 주세요.');
		     			location.attr = '/roomRental/boards';
		     		}																														
		     	});																		
		     }			
		    
			
			$("#city").change(function(){
				cd = $('option:selected', this).data('index');
				
				$("#gu option").not(":first").remove();
				$("#dong option").not(":first").remove();
				
				city = $("#city").val();
				gu = null;
				dong = null;
				
				getLocations('city');
				
			})
			
			$("#gu").change(function(){
				cd = $('option:selected', this).data('index');
				
				$("#dong option").not(":first").remove();
				
				gu = $("#gu").val();
				dong = null;
				
				getLocations('gu');
			})
	     	
			$("#dong").change(function(){
				dong = $("#dong").val();
			})
			
			
			function getLocations(clickedType){
				
				let currentTime = Date.now();
				
				// accessToken이 없거나 토큰의 시간이 만료됐다면 재발급
				if(accessToken == 'none' || currentTime > accessTimeout) getAccessToken();

				jQuery.ajax({																												
					type:'GET', 																											
					url: 'https://sgisapi.kostat.go.kr/OpenAPI3/addr/stage.json',													
					data:{																													
						accessToken : accessToken,																					
						cd : cd,																				
					},																														
					success:function(data){	
			     		
			     		// 구 변경 
			     		if(clickedType === 'city'){
			     			data.result.forEach(item => {
			     				$("#gu").append('<option value="' + item.addr_name + '" data-index="' + item.cd + '" class="gu">' + item.addr_name + '</option>');
							});
			     				
			     			// 동 변경	
			     		}else if(clickedType === 'gu'){
			     			data.result.forEach(item => {
								$("#dong").append('<option value="' + item.addr_name + '" class="dong">' + item.addr_name + '</button>');
				     		});
			     		}
			     	},																													
			     	error:function(data) {	
			     		alert('시스템 오류입니다. 잠시 후 다시 시도해 주세요.');
			     		location.attr = '/roomRental/boards';
			    	}																														
				});																		
			     		
		    }
			
			
		})
		
		
		function checkLoginStatus(){
			fetch("/api/member/auth/check").then((res) => {
				if (res.status === 401) {
					if (confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")) {
						location.href = "/member/login";
					} else {
						location.href = "/job/boards";
					}
				}
			})
		}
	</script>
</head>
<body class="wrap">
	<div class="rem-30 my-top-15 my-bottom-15">
		<div class="title flex justify-center my-bottom-8" >
			<h2>합주실 / 연습실</h2>
		</div>
		<div class="content flex justify-center" >
			<form id="roomRentalWrite">
				<div>
					<input type="hidden" id="user_id" name="user_id"> 
					<input type="hidden" id="user_name" name="user_name">
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
				<div class="my-bottom-4">
					<label>제목</label>
				</div>
				<div>
					<input type="text" class="roomRental_title my-bottom-7 height4 border width-85 border-radius-10" id="roomRental_title" name="roomRental_title">
				</div>
				<div class="my-bottom-4">
					<label>본문</label>
				</div>
				<div class="content">
					<textarea id="roomRental_content" class="summernote" name="roomRental_content" style="resize:none;"></textarea>    
				</div>
				<div class=" flex justify-right my-top-8">
					<button type="button" class="roomWriteBtn mr-1" id="write">등록</button>
					<a href="/roomRental/roomRentalList"  class="roomWriteBtn text-center">취소</a>
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