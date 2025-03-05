<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 커뮤니티</title>
<script src="/resources/include/dist/js/userToggle.js"></script>
<script type="text/javascript">
	$(function(){
		toggleUserMenu();
		
		getBoards();
		
		$(document).on("click", ".boardLink", function (e) {
		    e.preventDefault();
		    var location = $(this).attr("data-location");
		    if (location) {
		        window.location.href = location;
		    }
		});

		
		$("#searchBtn").click(function(){
			let search = $("#search").val();
			let keyword = $("#keyword").val();
			
			if(search == "all") keyword = "";
			else{
				if(keyword.replace(/\s/g, "") == ""){
					alert("검색어를 입력하세요.");
					$("#keyword").focus();
					return;
				} 
			}
			location.href = '/community/boards?search='+search+'&keyword='+keyword+'&pageNum='+'1';
		})
			
		$("#comWriteBtn").click(function(){
			fetch('/api/member/checkAuthentication')
			.then(response =>{
				if(!response.ok){
					alert('시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
					locaion.attr('/community/boards');
				}
				return response.json();
			})
			.then((data) => {
				if(data.authenticated) $(location).attr('href', '/community/board/write');
				else if(confirm("로그인 후 이용할 수 있는 서비스 입니다. 로그인 하시겠습니까?"))$(location).attr('href', '/member/login');
				
			})
		})
	})
	
	function getBoards(){
		let params = new URLSearchParams(window.location.search);
	    let pageNum = params.get("pageNum") || "1";
	    let search = params.get("search") || "all";
	    let keyword = params.get("keyword") || "";
	    
		fetch('/api/community/boards?pageNum='+pageNum+'&search='+search+'&keyword='+keyword)
		.then(response=>{
			if(response.ok) return response.json();
		}).then(data=>{
			let $template = $("#boardTemplate");
	        let $boardList = $("#boardList");
	        
	        $boardList.empty(); 

	        data.communityList.forEach(board => {
	            let $clone = $template.clone().removeAttr("id").show();
	            $clone.find(".userName").text(board.user_name);
	            $clone.find(".boardDate").text(board.com_date);
	            $clone.find(".boardTitle").text(board.com_title);
	            $clone.find(".boardHits").text(board.com_hits);
	            $clone.find(".boardReplyCnt").text(board.com_reply_cnt);
	            $clone.find(".boardLink").attr("data-location", "/community/board/" + board.com_no);

	            $boardList.append($clone);
	        });
	        
	        loadPagination(data.pageMaker);
		})
		
		$("#search").val(search);
		$("#keyword").val(keyword);
	}
	
	function loadPagination(pageMaker) {
	    const $pagination = $("#pagination");
	    $pagination.empty(); // 기존 페이지 버튼 초기화

	    // 이전 버튼
	    if (pageMaker.prev) {
	        $pagination.append(
	            '<li class="paginate_button previous">' +
	                '<a href="#" data-page="' + (pageMaker.startPage - 1) + '">Previous</a>' +
	            '</li>'
	        );
	    }

	    // 페이지 번호 버튼
	    for (let num = pageMaker.startPage; num <= pageMaker.endPage; num++) {
	        $pagination.append(
	            '<li class="paginate_button ' + (pageMaker.cvo.pageNum === num ? 'active' : '') + '">' +
	                '<a href="#" data-page="' + num + '" class="font-weight-bold">' + num + '</a>' +
	            '</li>'
	        );
	    }

	    // 다음 버튼
	    if (pageMaker.next) {
	        $pagination.append(
	            '<li class="paginate_button next">' +
	                '<a href="#" data-page="' + (pageMaker.endPage + 1) + '">Next</a>' +
	            '</li>'
	        );
	    }

	    // 클릭 이벤트 추가 (페이지 이동)
	    $("#pagination a").click(function (e) {
	        e.preventDefault();
	        let pageNum = $(this).data("page");
	        let params = new URLSearchParams(window.location.search);
		    let search = params.get("search") || "all";
		    let keyword = params.get("keyword") || "";
		    
		    let url = "/community/boards?pageNum="+pageNum+"&search="+search+"&keyword="+keyword;
	        
			window.location.href = url;
	    });
	}


	
</script>
</head>
<body class="wrap">
	<div class="rem-20 my-top-15 my-bottom-15">
		<div class="text-center my-top-7">
			<p class="title font-color-blue">COMMUNITY</p>
		</div>
		<div class="content">
			<div class="justify-between flex py-4">
				<div class="write_btn write_btn_border flex items-center border-radius-7px">
					<img class="icon" src="/resources/include/images/write.svg">
					<button type="button" id="comWriteBtn" class="write_btn_font  border-none bColor_fff">작성하기</button>
				</div>
			</div>
			
			<div class="border-top border-bottom py-2rem flex justify-center">
				
				<div class="items-center">
					<div class="items-center flex">
						<select id="search" name="search" class="search">
							<option value="all">전체</option>
							<option value="com_title">제목</option>
							<option value="com_content">내용</option>
							<option value="user_name">작성자</option>
						</select>
						<div>
							<input type="text" name="keyword" id="keyword" placeholder="커뮤니티 내에서 검색" class="border border-radius-43px rem-2 search"/>
						</div>
						<img class="icon" id="searchBtn" style="cursor:pointer; width:3rem;" src="/resources/include/images/search.svg">
					</div>
				</div>
			</div>	
			
			<div>
				<ul id="boardList">
					<li id="boardTemplate" class="border-bottom">
						<div class="pd-2rem boardLink" style="cursor: pointer;">
							<div class="my-bottom-4">
								<span class="padding-right-1 userName"></span>
								<div class="userNameToggle absolute" style="z-index: 5;"></div>
								<span class="padding-right-1  boardDate"></span>
							</div>
							<div class="my-bottom-4">
					            <span class="font-weight-bold font-size-5 boardTitle"></span>
					            <div class="flex float-right items-center width-13rem justify-center my-top-4">
					                <img class="icon" style="width: 2.5rem;" src="/resources/include/images/hits.svg" alt="Views" />
					                <span class="font-size-4 ml-05 boardHits"></span>
					                <img class="icon ml-2" style="width: 2.5rem;" src="/resources/include/images/reply.svg" alt="Replies" />
					                <span class="font-size-4 ml-05 boardReplyCnt"></span>
					            </div>
					        </div>
						</div>
					</li>
				</ul>
			</div>
			
			<div>
				<!-- 페이징 영역 -->
				<div class="text-center">
				    <ul id="pagination" class="pagination pagination_border"></ul>
				</div>
			</div>
		</div>
	</div>
</body>
</html>