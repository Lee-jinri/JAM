$(function(){
	$(document).on("click", ".favorite", function () {
	    $icon = $(this);
	    let isFavorite = $icon.hasClass("fa-solid"); // 현재 즐겨찾기 여부 확인
	    
	    let $favoriteSpan = $(this).closest(".favoriteSpan");
	    
	    let boardNo = $favoriteSpan.data("board-no"); // 게시글 번호 가져오기
		let boardType = $favoriteSpan.data("board-type"); 
		
	    if (isFavorite) {
	        removeFavorite(boardNo, boardType, $icon);
	    } else {
	        addFavorite(boardNo, boardType, $icon);
	    }
	});
})


function removeFavorite(boardNo, boardType, $icon){
	let url = '/api/favorite/'+boardNo + '?boardType=' + boardType;
	
	fetch(url,{
		method: "DELETE",
	})
	.then(response=>{
		if(response.ok) $icon.removeClass("fa-solid").addClass("fa-regular"); // 즐겨찾기 해제
		else {
			return response.text().then(errorMessage => {
                throw { status: response.status, message: errorMessage };
            });
        }
    })
    .catch(error => {
		if (error.status === 401) { 
            if (confirm(error.message)) {
                location.href = "/member/login";  // 로그인 페이지로 이동
            }
        } else {
            alert(error.message);
            console.error("즐겨찾기 삭제 에러 :", error);
        }
	});
}
		
function addFavorite(boardNo, boardType, $icon){
	let url = '/api/favorite/'+boardNo + '?boardType=' + boardType;
	
	fetch(url,{
		method: "POST",
	})
	.then(response=>{
		if(response.ok) $icon.removeClass("fa-regular").addClass("fa-solid"); // 즐겨찾기 추가
		else {
			return response.text().then(errorMessage => {
                throw { status: response.status, message: errorMessage };
            });
        }
    })
    .catch(error => {
		if (error.status === 401) { 
            if (confirm(error.message)) {
                location.href = "/member/login";  // 로그인 페이지로 이동
            }
        } else {
            alert(error.message);
            console.error("즐겨찾기 삭제 에러 :", error);
        }
	});
}