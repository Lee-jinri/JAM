<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 중고악기</title>
<style>
.flea-post-container{
	padding: 60px;
	justify-content: space-between;
}
/*
.flea-slider {
	position: relative;
	width: 372px;
	height: 372px;
	overflow: hidden;
	margin-right: 40px;
}

.flea-slider-images {
	display: flex;
	transition: transform 0.3s ease-in-out;
}*/

.flea-slider-img {
	width: 300px;
	height: 200px;
	flex-shrink: 0;
	object-fit: cover;
}

.flea-slider {
	width: 372px;
	height: 372px;
	overflow: hidden;
	position: relative;
	display: flex;
	align-items: center;
}

.flea-slider-images {
	display: flex;
	height: 100%;
	position: relative;
	transition: transform 0.5s ease;
}

.flea-slider-images img {
	width: 372px;
	height: 372px;
	object-fit: cover;
	object-position: center;
	flex-shrink: 0;
}

.prev-btn,
.next-btn {
	border: none;
	border-radius: 50%;
    width: 40px;
    height: 40px;
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(33, 33, 33, 0.35);
    /*display: none;*/ 
    -webkit-box-align: center;
    align-items: center;
    -webkit-box-pack: center;
    justify-content: center;
    right: 20px;
    font-size: 33px;
    color: #ffffff78;
    z-index: 10;
}

.prev-btn {
	display: none; 
}


.flea-info{
    height: 372px;
    width: 455px;
}
.image-box {
	position: relative;
}
.sold-out-overlay {
	position: absolute;
	top: 40%;
	left: 50%;
	transform: translate(-50%, -50%);
	background: rgba(0, 0, 0, 0.6);
	color: #fff;
	font-size: 20px;
	padding: 10px 20px;
	border-radius: 30px;
}

#title{
	font-size: 24px;
    font-weight: 600;
    line-height: 1.4;
    margin-top: 45px;
    margin-bottom: 30px;
}
#price{
	font-size: 33px;
    font-weight: 500;
}
.meta{
	height: 30px;
    margin-top: 15px;
    margin-bottom: 25px;
    display: flex;
    -webkit-box-pack: justify;
    align-items: center;
    border-top: #ccc 1px solid;
    padding-top: 20px;
}

.meta li{
	font-size: 16px;
	margin-right: 15px;
	margin-left: 5px;
	color: rgb(204, 204, 204);
}
.prev-btn {
	left: 10px;
}

.next-btn {
	right: 10px;
}
#flea-postBtn{
    display: flex;
    -webkit-box-align: center;
    align-items: center;
    column-gap: 10px;
    font-size: 18px;
    font-weight: 700;
    line-height: normal;
    height: 52px;
    margin-top: 105px;
}
#fleaUpdateBtn, #fleaDeleteBtn{
	display: flex;
    -webkit-box-align: center;
    align-items: center;
    -webkit-box-pack: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background: rgb(255, 164, 37);
    border: 1px solid rgb(243, 150, 20);
    color: rgb(255, 255, 255);
    font-size: 18px;
    font-weight: 700;
    line-height: normal;
}
#chatBtn{
    background: rgb(255, 164, 37);
    border: 1px solid rgb(243, 150, 20);
    color: rgb(255, 255, 255);
    font-weight: 700;
    display: flex;
    -webkit-box-align: center;
    align-items: center;
    -webkit-box-pack: center;
    justify-content: center;
    -webkit-box-flex: 1;
    width: 100%;
    height: 100%;
}
#favoriteBtn{
    width: 100%;
    height: 100%;
    font-weight: 600;
    color: rgb(255, 255, 255);
    display: flex;
    -webkit-box-align: center;
    align-items: center;
    -webkit-box-pack: center;
    justify-content: center;
    line-height: 1;
    background: rgb(51, 51, 51);
}
.slide-image {
	width: 372px;         /* 원하는 가로 크기 */
	height: 372px;        /* 원하는 세로 크기 */
	object-fit: cover;    /* 비율 유지하면서 넘치는 부분 자르기 */
	object-position: center; /* 자를 때 중앙 기준으로 */
}

</style>
<script type="text/javascript">
$(function(){
	let post_id = $("#post_id").val();
	let authorUserId;
	let authorUserName;
	let currentIndex = 0;
	
	getBoard();
	
	function getBoard(){
		if(post_id == "" || post_id == null) alert("게시글을 불러올 수 없습니다. 잠시 후 다시 시도해주세요.");
		else{
			fetch("/api/fleaMarket/post/" + post_id)
			.then(response =>{
				if(!response.ok){
					throw new Error('Network response was not ok.');
				}
				return response.json();
			})
			.then(data =>{
				let post = data.post;
				
				authorUserId = post.user_id;
				authorUserName = post.user_name;
				
				$("#title").html(post.title);
				$("#user_name").html(post.user_name);
				$("#created_at").html(timeAgo(post.created_at));
				$("#view_count").html(post.view_count);
				$("#post_content").html(post.content);
				$("#price").html(post.price.toLocaleString());
				
				if(post.sales_status == 1) $("#saleDone").html("거래 완료 된 글 입니다.");
				
				renderImages(data.images);
				currentUserIsAuthor(authorUserId);
			})
			.catch(error => console.error('Error : ', error));
		}
	}
	
	// 현재 로그인한 사용자와 글쓴이가 같은지 비교하는 함수
	function currentUserIsAuthor(authorUserId){

		let isAuthor = false;
       	if(window.MY_ID == authorUserId) isAuthor = true;
       	
       	renderPostActionButtons(isAuthor);
	}
	
	// 수정 버튼 클릭
	$(document).on("click", "#fleaUpdateBtn", function() {
	    if(post_id == null) alert("오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
	    else $(location).attr('href','/fleaMarket/post/edit/'+post_id);
	});
	
	// 삭제 버튼 클릭
	$(document).on("click", "#fleaDeleteBtn", function() {
		if(confirm("정말 삭제하시겠습니까?")){
			fetch('/api/fleaMarket/post/'+post_id,{
				method: 'DELETE'
			})
			.then(response =>{
				if (!response.ok) {
					throw new Error('Network response was not ok');
				}
				alert("삭제가 완료 되었습니다.");
				$(location).attr('href', '/fleaMarket/board');
			})
			.catch(error =>{
				alert('게시글 삭제를 완료할 수 없습니다. 잠시 후 다시 시도해주세요.');
				console.error('Error : ' , error);
			});
		}
	});
	
	$(document).on("click", "#chatBtn", async function(){
		if (!authorUserId) {
			alert("사용자 정보를 불러올 수 없습니다.");
			return;
		}
		
		try {
			const res = await fetch('/api/chat/chatRoomId?targetUserId=' + authorUserId);

			if (!res.ok) {
				if (res.status === 401) {
					if(confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")){
						window.location.href = '/member/login';
					}
				} else {
					alert("채팅방 정보를 불러오는 데 실패했습니다. 잠시 후 다시 시도해주세요.");
				}
				return;
			}
			const chatRoomId = await res.text();
			sessionStorage.setItem("chatRoomId", chatRoomId);
			window.location.href = '/chat';

		} catch (error) {
			console.error("채팅방 요청 중 오류:", error);
			alert("네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		}
	})

	// 글쓴이와 로그인한 사용자가 다르면 채팅 버튼 생성
	function renderPostActionButtons(isAuthor) { 
		var btnDiv = document.getElementById("flea-postBtn");
		
		if(isAuthor){
	           	// 수정 버튼 생성
	           	var updateButton = document.createElement("button");
	           	updateButton.type = "button";
	           	updateButton.id = "fleaUpdateBtn";
	           	updateButton.textContent = "수정"; 

	           	// 삭제 버튼 생성
	           	var deleteButton = document.createElement("button");
	           	deleteButton.type = "button";
	           	deleteButton.id = "fleaDeleteBtn";
	           	deleteButton.textContent = "삭제"; 

	           	btnDiv.appendChild(updateButton);
	           	btnDiv.appendChild(deleteButton);
		} else {
			var favoriteBtn = document.createElement("button");
			favoriteBtn.type = "button";
			favoriteBtn.id = "favoriteBtn";
			favoriteBtn.innerHTML = '<i class="fa-regular fa-heart"></i>&nbsp; 찜';
			
			var chatBtn = document.createElement("button");
			chatBtn.type = "button";
			chatBtn.id = "chatBtn";
			chatBtn.innerHTML = '<i class="fa-regular fa-message"></i>&nbsp; 채팅';
			
			btnDiv.appendChild(favoriteBtn);
	       	btnDiv.appendChild(chatBtn);
		}
	}

	function renderImages(images){
		let container = document.querySelector(".flea-slider-images");
		
		images.forEach(function(image) {
			let img = document.createElement("img");
			img.src = '/images/flea/' + image.image_name; 
			img.alt = "상품 이미지";
			img.className = "slide-image"; 
			container.appendChild(img);
			
		});
		renderSliderButtons(images.length);
	}

	function renderSliderButtons(imageCount) {
		const container = document.querySelector(".flea-slider-images");
		const prevBtn = document.querySelector(".prev-btn");
		const nextBtn = document.querySelector(".next-btn");

		prevBtn.addEventListener("click", () => {
			if (currentIndex > 0) {
				currentIndex--;
				updateSlide(container, imageCount);
			}
		});

		nextBtn.addEventListener("click", () => {
			if (currentIndex < imageCount - 1) {
				currentIndex++;
				updateSlide(container, imageCount);
			}
		});
	}

	function updateSlide(container, imageCount) {
		let offset = -currentIndex * 372;
		container.style.transform = "translateX(" + offset + "px)";

		// 버튼 상태 갱신
		const prevBtn = document.querySelector(".prev-btn");
		const nextBtn = document.querySelector(".next-btn");

		// 처음이면 prev 버튼 숨김
		if (currentIndex === 0) {
			prevBtn.style.display = "none";
			nextBtn.style.display = "block";
		} else {
			prevBtn.style.display = "block";
		}

		// 마지막이면 next 버튼 숨김
		if (currentIndex === imageCount - 1) {
			nextBtn.style.display = "none";
			prevBtn.style.display = "block";
		} else {
			nextBtn.style.display = "block";
		}
	}




	function timeAgo(dateString) {
		let now = new Date();
		let past = new Date(dateString);
		let diff = Math.floor((now - past) / 1000); 

		if (diff < 10) return '방금 전';
		if (diff < 60) return diff + '초 전';
		if (diff < 3600) return Math.floor(diff / 60) + '분 전';
		if (diff < 86400) return Math.floor(diff / 3600) + '시간 전';
		if (diff < 172800) return '어제';
		if (diff < 2592000) return Math.floor(diff / 86400) + '일 전';
		
		let dateStr = past.toLocaleDateString();
		return dateStr.replace(/\.$/, '');
	}	
})




</script>
</head>
<body class="wrap">
	<div>
		<input id="post_id" type="hidden" name="post_id" value="${post_id }"/>
		
		<div class="flea-post-container flex">
			<div class="flea-slider">
				<button class="prev-btn">&lt;</button>
				<div class="flea-slider-images">
				</div>
				<button class="next-btn">&gt;</button>
			</div>

			<div class="flea-info">
				<h2 id="title"></h2>
				<span id="price"></span>
				<span style="font-size:24px;">원</span>
				<ul class="meta flex">
					<i class="fa-regular fa-eye" style="color: #cccccc;"></i>
					<li id="view_count"></li>
					<i class="fa-solid fa-clock" style="color: #cccccc;"></i>
					<li id="created_at"></li>
				</ul>
				<div id="flea-postBtn">
				</div>
			</div>
		</div>
		<div>
			<span id="post_content"></span>
		</div>
	</div>
</body>
</html>