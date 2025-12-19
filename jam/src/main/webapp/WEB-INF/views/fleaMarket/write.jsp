<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 중고악기</title>
<script src="/resources/include/dist/js/categories.js"></script>

<style>
#flea_content{
	width: 1000px;
}
#imageUpload-label{
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	width: 170px;
	height: 170px;
	border: 1px solid #ccc;
	background-color: #fafafa;
	cursor: pointer;
	gap: 8px;
}

.previewBox{
	position: relative;
	display: inline-block;
	margin-right: 10px;
}

.previewBox img{
	width: 170px;
	height: 170px;
	border: 1px solid #ccc;
	border-radius: 4px;
	object-fit: cover;
}

.delete-btn {
	position: absolute;
	top: 8px;
	right: 8px;
	background: #0000005c;
	color: white;
	border: none;
	border-radius: 50%;
	cursor: pointer;
	width: 23px;
	height: 23px;
	font-size: 12px;
	
	display: flex;
	align-items: center;
	justify-content: center;
}
.form-row{
	margin: 35px 0;
}
.form-textarea{
	padding: 1rem;
    resize: none;
    line-height: 1.35;
    font-size: 16px;
    font-weight: 400;
    line-height: 125%;
    color: rgb(25, 25, 25);
    border-radius: 2px;
    height: 115px;
}

.form-span{
	width: 150px;
}
.price-span{
	position: absolute;
    right: 14px;
    font-size: 16px;
    font-weight: 400;
    line-height: 20px;
    color: rgb(140, 140, 140);
    top: 50%;
    transform: translateY(-50%);
}
#price{
	height: 2rem;
	padding: 0 13px;
    font-size: 16px;
    font-weight: 400;
    line-height: 125%;
}
.category-container {
  display: flex;
  gap: 20px;
  border: 1px solid #ddd;
  width: 710px;
}

.category-column {
  flex: 1;
  overflow-y: auto;
  max-height: 300px;
}

.category-column h4 {
  margin-bottom: 10px;
  font-size: 16px;
}

.category-column ul {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 16px;
    letter-spacing: 0.5px;
}

.category-column li {
	padding: 11px 14px;
	cursor: pointer;
	border-radius: 4px;
}

.category-column li:hover {
  background-color: #f0f0f0;
}

.category-column li.active {
  background-color: #ddd;
  font-weight: bold;
}
#mainCategory li.active {
  background-color: #ddd;
  font-weight: bold;
}
.btn-group{
	float: right;
    border: none;
    background-color: #fff;
    font-size: 15px;
    color: #444; 
    padding: 4px 8px;
    transition: all 0.2s ease-in-out; 
    cursor: pointer;
}
.btn-group:hover{
	background-color: #f0f0f0; 
    color: #222; 
    text-decoration: underline; 
    opacity: 0.8; 
}
.flea_title{
	width: 1000px;
}

</style>
<script>	
	$(function(){
		checkLoginStatus();
			
		const imageInput = document.getElementById("imageUpload");
		const previewContainer = document.getElementById("imagePreviewContainer");
		const imageCountDisplay = document.getElementById("imageCount");

		let imageFiles = [];

		imageInput.addEventListener("change", function (e) {
			const files = Array.from(e.target.files);

			// 5장 제한
			if (imageFiles.length + files.length > 5) {
				alert("이미지는 최대 5장까지 등록할 수 있습니다.");
				return;
			}

			files.forEach(file => {
				const reader = new FileReader();
				reader.onload = function (e) {
					const previewBox = document.createElement("div");
					previewBox.classList.add("previewBox");
					
					const img = document.createElement("img");
					img.src = e.target.result;
		
					const deleteBtn = document.createElement("button");
					deleteBtn.innerText = "X";
					deleteBtn.classList.add("delete-btn");
					
			        deleteBtn.onclick = () => {
			        	previewBox.remove();
			        	imageFiles = imageFiles.filter(f => f !== file);
			        	updateCount();
			        };
	
				    previewBox.appendChild(img);
				    previewBox.appendChild(deleteBtn);
				    previewContainer.appendChild(previewBox);
				    
				    imageFiles.push(file);
				    updateCount();
			    };
		    	reader.readAsDataURL(file);
	    	});

			// 등록 후 input 초기화 (같은 파일 재선택 가능)
			imageInput.value = "";
		});

		function updateCount() {
			imageCountDisplay.textContent = "(" + imageFiles.length + "/5)";
		}
		
		$('#mainCategory li').click(function () {
			$('#mainCategory li').removeClass('active');
			$(this).addClass('active');
			
			const mainIndex = $(this).index() + 1;
			const subList = subCategories[mainIndex];
			
			const $subUl = $('#subCategory');
			$subUl.empty(); 
			
			subList.forEach(([name, id]) => {
				$('#subCategory').append("<li data-id=" + id + ">" + name + "</li>");
			});
		});
		
		$('#subCategory').on('click', 'li', function () {
			$('#subCategory li').removeClass('active');
			$(this).addClass('active');
	
			const category_id = $(this).data('id');
			$('#category_id').val(category_id);
		});

		$("#submit").click(async function(){
			// 유효성 검사
			let title = $("#title").val();
			let content = $("#flea_content").val();
			let price = $("#price").val();
			let category_id = $("#category_id").val();
			
			if (imageFiles.length === 0) {
				alert("사진은 최소 1장 이상 등록해주세요.");
				return false;
			}

		
			if(title.replace(/\s/g,"") == ""){
				alert("제목을 입력하세요.");
				$("#title").focus();
				return false;
			}
			
			if(content.trim() === ""){
				alert("설명을 입력하세요.");
				$("#flea_content").focus();
				return false;
			}
			
			if(category_id.replace(/\s/g,"") == ""){
				alert("카테고리를 선택하세요.");
				return false;
			}
			
			if(price.replace(/\s/g,"") == ""){
				alert("가격을 입력하세요.");
				$("#price").focus();
				return false;
			}
			
			try {
				const formData = new FormData();
				
				formData.append('title', title);
				formData.append('content', content);
				formData.append('price', price);
				formData.append('category_id', category_id);

				imageFiles.forEach((file, idx) => {
					formData.append('images', file); 
				});

	            const response = await fetch('/api/fleaMarket/post', {
	                method: 'POST',
	                body: formData,
	            });

	            if (response.ok) {
	            	alert("등록이 완료되었습니다.");
	                const body = await response.text();
	                if (body) {
	                    $(location).attr('href', '/fleaMarket/post/' + body);
	                }
	            } else {
	                const errorText = response.text();
	                throw new Error(errorText);
	            }
	        } catch (error) {
	            alert("게시글 작성을 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
	            console.error('Error:', error);
	        }
	    });
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
	<div class="my-top-15 my-bottom-15">
		<div class="page-header">
			<p class="page-title">판매 상품 등록</P>
			<p>구매자가 궁금해할 정보들을 자세히 남겨주세요 ✨</p>
			<p>사진·사용감·보유기간 등이 상세할수록 거래가 빠릅니다.</p>
		</div>
		<div style="margin-bottom: 100px;">
			<h3>상품정보</h3>
			<hr>
			
			<label style="display: block; margin-bottom: 10px;">상품이미지 <span id="imageCount">(0/5)</span></label>
			<div id="imagePreviewContainer" style="display: flex; gap: 10px; flex-wrap: wrap;">
			  
				<!-- 이미지 등록 버튼 -->
				<label id="imageUpload-label" for="imageUpload">
					<i class="fa-solid fa-camera" style="font-size: 24px; color: #aaa;"></i>
					<span style="font-size: 14px; color: #777;">이미지 등록</span>
				</label>
				<input type="file" id="imageUpload" accept="image/*" multiple style="display: none;">
			</div>
			
			<div class="form-row my-bottom-4 flex items-center">
				<span class="form-span">상품명</span>
				<input type="text" class="flea_title height4 border" id="title" name="title">
			</div>
			<hr>
			
			<div class="form-row my-bottom-4 flex">
				<span class="form-span">설명</span>
				<textarea id="flea_content" class="form-textarea border" name="content" placeholder="브랜드, 모델명, 구매 시기, 하자 유무 등 상품 설명을 최대한 자세히 적어주세요."></textarea>    
			</div>
			<hr>
			
			<div class="form-row my-bottom-4 flex">
				<input type="hidden" id="category_id">
				<span class="form-span">카테고리</span>
				<div class="category-container">
					<div class="category-column">
						<ul id="mainCategory">
							<li data-id="1" data-sub="guitar">기타류</li>
							<li data-id="2" data-sub="keyboard">키보드/피아노</li>
							<li data-id="3" data-sub="drum">드럼</li>
							<li data-id="4" data-sub="wind">관악기</li>
							<li data-id="5" data-sub="string">현악기</li>
							<li data-id="6" data-sub="equipment">장비</li>
							<li data-id="7" data-sub="accessory">그 외 악세서리</li>
						</ul>
					</div>
			
					<div class="category-column">
						<ul id="subCategory">
							<li>소분류 선택</li>
						</ul>
					</div>
				</div>
			</div>
		</div>
		<div>
			<h3>가격</h3>
			<hr>
			<div class="form-row my-bottom-4 flex items-center">
				<span class="form-span">가격</span>
				<div class="flex" style="position: relative;">
					<div style="position: static; width: 100%;">
						<input type="text" name="price" id="price" autocomplete="off" class="border" placeholder="가격을 입력해 주세요." value="" style="width: 300px;">
						<span class="price-span">원</span>
					</div>
				</div>
			</div>
		</div>
		<div class="flex justify-end">
			<button id="submit" class="btn-group">등록</button>
			<button id="cancel" class="btn-group">취소</button>
		</div>
	</div>
</body>
</html>