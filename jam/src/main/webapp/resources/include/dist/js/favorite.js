$(function(){
	$(document).on("click", ".favorite", function (e) {
		e.preventDefault();
		e.stopPropagation();
		
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
	
	if(!boardNo || !boardType) {
		alert("시스템 오류 입니다. 잠시 후 다시 시도하세요.");
		return false;
	}
	
	
	fetch(url,{
		method: "DELETE",
	})
	.then(response=>{
		if(response.ok) {
			// 북마크 페이지에서만 실행됨.
			const $item = $icon.closest(".bookmark-item");
			if ($item.length > 0) {
			    $item.remove(); 
			}

			$icon.removeClass("fa-solid").addClass("fa-regular"); // 즐겨찾기 해제
		}
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
	
	if(!boardNo || !boardType) {
		alert("시스템 오류 입니다. 잠시 후 다시 시도하세요.");
		return false;
	}
	
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