<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>마이페이지 - 구인구직 작성 글</title>
	<script>
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
			
			$("#myComBtn").click(function(){
				getUserIDAndRedirect("/member/comMyWrite/?user_id=", null, null);
			})
			$("#myFleaBtn").click(function(){
				getUserIDAndRedirect("/member/fleaMyWrite/?user_id=", null, null);
			})
			$("#myJobBtn").click(function(){
				getUserIDAndRedirect("/member/jobMyWrite/?user_id=", null, null);
			})
			$("#myRoomBtn").click(function(){
				getUserIDAndRedirect("/member/roomMyWrite/?user_id=", null, null);
			})
		})
		
		/*검색을 위한 실질적인 처리 함수*/
		function goPage(){
			if($("#search").val()=="all"){
				$("#keyword").val("");
			}
			
			if(search=="all"){
				$("#keyword").val("");
			}
			let url = "/member/jobMyWrite/?user_id=";
			
			getUserIDAndRedirect(url, search, keyword);
			
		}
		

		function getUserIDAndRedirect(redirectURL, search, keyword) {
		    fetch('http://localhost:8080/api/member/getUserInfo', {
		        method: 'GET',
		        headers: {
		            'Authorization': localStorage.getItem("Authorization")
		        },
		    })
		    .then(response => {
		        if (!response.ok) {
		            throw new Error('Network response was not ok');
		        }
		        return response.json();
		    })
		    .then((data) => {
		    	let user_id = data.user_id;
		        if (user_id) {
		        	$(location).attr('href', redirectURL + user_id + "&search=" + search + "&keyword=" + keyword);
		        } else {
		            $(location).attr('href', '/member/login');
		        }
		    })
		    .catch(error => {
		        console.error('사용자 정보를 가져오는 중 오류 발생:', error);
		    });
		}
	</script>
</head>
<body class="wrap">
	<div class="rem-20  my-top-15 my-bottom-15">
		<div class="title my-bottom-15">
			<p class="text-center my-7">작성한 글</p>
		</div>
		
		<div class="py-2rem flex justify-right">
			<div class="items-center">
				<form id="searchForm">
					<!-- 페이징 처리를 위한 파라미터 -->
					<input type="hidden" name="pageNum" value="${pageMaker.cvo.pageNum }">
					<input type="hidden" name="amount" value="${pageMaker.cvo.amount }">
					
					<div class="item-center flex">
						<!-- <label>검색조건</label> -->
						<select id="search" name="search" class="search">
							<option value="all">전체</option>
							<option value="job_title">제목</option>
							<option value="job_content">내용</option>
						</select>
						<div>
							<input type="text" name="keyword" id="keyword" placeholder="검색" class="border border-radius-43px rem-2 search"/>
						</div>
						<img class="icon" id="searchBtn" style="cursor:pointer; width:3rem;" src="/resources/include/images/search.svg">
					</div>
				</form>
			</div>
		</div>
		
		<div class="border-bottom">
			<ul class="nav nav-tabs nav-justified">
				<li><button type="button" id="myComBtn" class="myPageBtn">커뮤니티</button></li>
				<li><button type="button" id="myFleaBtn" class="myPageBtn">중고악기</button></li>
				<li><button type="button" id="myJobBtn" class="myPageBtn" style="background-color:#A9D0F5;">구인구직</button></li>
				<li><button type="button" id="myRoomBtn" class="myPageBtn">합주실/연습실</button></li>
			</ul>
		</div>
		
		<div class="content">
			<table summary="구인구직 리스트" class="table ">
				<thead>
					<tr>
						<th class="text-center col-md-3">제목</th>
						<th data-value="job_date" class="order col-md-1 text-center">작성일</th>
						<th data-value="job_hits" class="order col-md-1 text-center">조회수</th>
					</tr>
				</thead>
				<tbody id = "list" class="">
					<c:choose>
						<c:when test="${ not empty jobMyWrite }">
							<c:forEach items="${jobMyWrite }" var="jobBoard" varStatus="status">
								<tr class="text-center" data-num="${jobBoard.job_no}">
									<td class="class">
										<a class="" href="/job/board/${jobBoard.job_no }">${jobBoard.job_title}</a>
									</td>
									<td class=" ">${jobBoard.job_date}</td>
									<td class=" ">${jobBoard.job_hits }</td>
								</tr>
							</c:forEach>
						</c:when>
						<c:otherwise>
							<tr>
								<td colspan="5" class="tac text-center">등록된 게시물이 존재하지 않습니다.</td>
							</tr>
						</c:otherwise>
					</c:choose>
				</tbody>
			</table>
		</div>
		<!-- 페이징 -->
		<div class="text-center">
			<ul class="pagination">
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
</body>
</html>