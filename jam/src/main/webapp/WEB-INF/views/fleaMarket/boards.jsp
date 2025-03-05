<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 중고악기</title>

<script src="/resources/include/dist/js/userToggle.js"></script>	
<script type="text/javascript">
$(function(){
	toggleUserMenu();
	
	$(".boardLink").click(function(e){
		if (!$(e.target).hasClass('userName')) {
			
			var locationUrl = $(this).data('location');
		    location.href = locationUrl;
	   }
	})
	
	$("#searchBtn").click(function(){
		if($("#search").val() != "all"){
			if($("#keyword").val().replace(/\s/g, "") == ""){
				alert("검색어를 입력하세요.");
				$("#keyword").focus();
				return;
			}
		}
		$("#pageNum").val(1);
		goPage();
	})
	
	$(".paginate_button a").click(function(e) {
		e.preventDefault();
		$("#searchForm").find("input[name='pageNum']").val($(this).attr("href"));
		goPage();
	})
	
	$("#fleaWriteBtn").click(function(){
		fetch('/api/member/checkAuthentication')
		.then(response =>{
			if(!response.ok){
				alert('시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
				locaion.attr('/fleaMarket/boards');
			}
			return response.json();
		})
		.then((data) => {
			if(data.authenticated) $(location).attr('href', '/fleaMarket/board/write');
			else if(confirm("로그인 후 이용할 수 있는 서비스 입니다. 로그인 하시겠습니까?"))$(location).attr('href', '/member/login');
			
		})
	})
})
	
/*검색을 위한 실질적인 처리 함수*/
function goPage(){
	if($("#search").val()=="all"){
		$("#keyword").val("");
	}
	$("#searchForm").attr({
		"method":"get",
		"action":"/fleaMarket/boards/"
	});
	$("#searchForm").submit();
}
</script>
</head>
<body class="wrap">
	<div class="rem-20  my-top-15 my-bottom-15">
		<div class="title">
			<p class="text-center font-color-blue">중고악기</p>
		</div>
		<div class="content">
			<div class="justify-between flex py-4">
				<div class="write_btn write_btn_border flex items-center border-radius-7px">
					<img class="icon" src="/resources/include/images/write.svg">
					<button type="button" id="fleaWriteBtn" class="write_btn_font border-none bColor_fff">작성하기</button>
				</div>
			</div>
			
			<div class="border-top border-bottom py-2rem flex justify-center">
				
				<div class="items-center">
					<form id="searchForm">
						<!-- 페이징 처리를 위한 파라미터 -->
						<input type="hidden" name="pageNum" id="pageNum" value="${pageMaker.cvo.pageNum }">
						<input type="hidden" name="amount" value="${pageMaker.cvo.amount }">
						
						<div class="items-center flex">
							<!-- <label>검색조건</label> -->
							<select id="search_category" name="search_category" class="search">
								<option value="all">전체</option>
								<option value="buy">구매</option>
								<option value="sale">판매</option>
							</select>
							<select id="search" name="search" class="search">
								<option value="flea_title">제목</option>
								<option value="flea_content">내용</option>
								<option value="user_name">닉네임</option>
							</select>
							<div class="">
								<input type="text" name="keyword" id="keyword" placeholder="중고악기 내에서 검색" class="border border-radius-43px rem-2 search"/>
							</div>
							<img class="icon" id="searchBtn" style="cursor:pointer; width:3rem;" src="/resources/include/images/search.svg">
						</div>
					</form>
				</div>
			</div>	
			<div>
				<c:choose>
					<c:when test="${not empty fleaMarketList }">
						<ul>
							<c:forEach items="${fleaMarketList }" var="fleaMarketBoard" varStatus="status">
								<li class="border-bottom">
									<div class="pd-2rem boardLink" data-location="/fleaMarket/board/${fleaMarketBoard.flea_no }" style="cursor:pointer;">
										<div class="my-bottom-4">
											<span class="padding-right-1 userName">${fleaMarketBoard.user_name }</span>
											<div class="userNameToggle absolute" style="z-index: 5;"></div><span class="padding-right-1"> | </span>
											<span>${fleaMarketBoard.flea_date }</span>
										</div>
										
										
										<div class=inline-block>
											<c:choose>
								        		<c:when test = "${fleaMarketBoard.sales_status == 0}">
								            		<c:if test="${fleaMarketBoard.flea_category == 0}">
														<span class="border-g " style="color:#04B431; padding: 2px;">판매</span>
														<div class="inline-block my-bottom-4 ml-1">
															<span class="font-weight-bold font-size-5" >${fleaMarketBoard.flea_title }</span>
														</div>
													</c:if>
													<c:if test="${fleaMarketBoard.flea_category == 1}">
														<span class="border-r" style="color:#FE2E2E; padding: 2px;">구매</span>
														<div class="inline-block my-bottom-4 ml-1">
															<span class="font-weight-bold font-size-5" >${fleaMarketBoard.flea_title }</span>
														</div>
													</c:if>
												</c:when>
												<c:otherwise>
													<span class="border" style="color:#A4A4A4; padding: 2px;">거래 완료</span>
													<div class="inline-block my-bottom-4 ml-1">
														<span class="font-weight-bold font-size-5" style="color:#A4A4A4;" >${fleaMarketBoard.flea_title }</span>
													</div>
										        </c:otherwise>
											</c:choose>
										</div>
										<div class="flex float-right items-center width-13rem justify-center my-top-4">
											<img class="icon" id="" style="width:2.5rem;" src="/resources/include/images/hits.svg">
											<span class="font-size-4 ml-05">${fleaMarketBoard.flea_hits }</span>
											<img class="icon ml-2" id="" style="width:2.5rem;" src="/resources/include/images/reply.svg">
											<span class="font-size-4 ml-05">${fleaMarketBoard.flea_reply_cnt }</span>
										</div>
									</div>
								</li>
							</c:forEach>
						</ul>
					</c:when>
					<c:otherwise>
						<div class="text-alignC pd-top5 font-size-3">
	            			<span>등록된 게시글이 없습니다.</span>
	            		</div>
					</c:otherwise>
				</c:choose>
			</div>
			<div>				
				<!-- 페이징 -->
				<div class="text-center">
					<ul class="pagination pagination_border">
						<c:if test="${pageMaker.prev}">
							<li class="paginate_button previous">
								<a href="${pageMaker.startPage -1}">Previous</a>
							</li>
						</c:if>
						
						<c:forEach var="num" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
							<li class="paginate_button"  >
								<a id="${pageMaker.cvo.pageNum == num ? 'btnColor':''}" class="font-weight-bold" href="${num}">${num}</a>
							</li>
						</c:forEach>
						
						<c:if test="${pageMaker.next}">
							<li class="paginate_button next">
								<a href="${pageMaker.endPage +1 }">Next</a>
							</li>
						</c:if>
					</ul>
				</div>
			</div>
		</div>
	</div>
</body>
</html>