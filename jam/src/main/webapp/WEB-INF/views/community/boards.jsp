<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 커뮤니티</title>
	<script type="text/javascript">
		$(function(){
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
			
			$("#comWriteBtn").click(function(){
				var accessToken = localStorage.getItem("Authorization");
			
				if(accessToken != null) $(location).attr('href', '/community/board/write');
				else{
					if(confirm("로그인 후 이용할 수 있는 서비스 입니다. 로그인 하시겠습니까?"))$(location).attr('href', '/member/login');
					
				}
			})
		})
		
		
		/*검색을 위한 실질적인 처리 함수*/
		function goPage(){
			if($("#search").val()=="all"){
				$("#keyword").val("");
			}
			$("#searchForm").attr({
				"method":"get",
				"action":"/community/boards/"
			});
			$("#searchForm").submit();
		}
	</script>
</head>
<body class="wrap">
	<div class="rem-20 my-top-15 my-bottom-15">
		<div class="text-center my-top-7">
			<p class="title">COMMUNITY</p>
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
					<form id="searchForm">
						<!-- 페이징 처리를 위한 파라미터 -->
						<input type="hidden" name="pageNum" value="${pageMaker.cvo.pageNum }">
						<input type="hidden" name="amount" value="${pageMaker.cvo.amount }">
						
						<div class="items-center flex">
							<!-- <label>검색조건</label> -->
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
					</form>
				</div>
			</div>	
			
			<div>
				<ul>
					<c:forEach items="${communityList }" var="communityBoard" varStatus="status">
						<li class="border-bottom">
							<div class="pd-2rem">
								<div class="my-bottom-4">
									<span>${communityBoard.user_name }</span>
									<span> | </span>
									<span>${communityBoard.com_date }</span>
								</div>
								<div class="my-bottom-4">
									<a class="font-weight-bold" href="/community/board/${communityBoard.com_no }" >${communityBoard.com_title }</a>
									<div class="flex float-right items-center width-13rem justify-between">
										<img class="icon" id="" style="width:3rem;" src="/resources/include/images/hits.svg">
										<span class="font-size-1 ml-05">${communityBoard.com_hits }</span>
										<img class="icon ml-2" id="" style="width:3rem;" src="/resources/include/images/reply.svg">
										<span class="font-size-1 ml-05">${communityBoard.com_reply_cnt }</span>
									</div>
								</div>
								
							</div>
						</li>
					</c:forEach>
				</ul>
			</div>
			
			<div>
				
				<!-- 페이징 -->
				<div class="text-center">
					<ul class="pagination">
						<c:if test="${pageMaker.prev}">
							<li class="paginate_button previous">
								<a href="${pageMaker.startPage -1}" >Previous</a>
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