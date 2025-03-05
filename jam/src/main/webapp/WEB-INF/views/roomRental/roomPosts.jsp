<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>마이페이지 - 합주실/연습실 작성 글</title>
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
			
			$("#keyword").keypress(function(event) {
	            if (event.key === "Enter") {
	                event.preventDefault(); 
	                $("#searchBtn").click();
	            }
	        });
			
			$(".paginate_button a").click(function(e) {
				e.preventDefault();
				$("#searchForm").find("input[name='pageNum']").val($(this).attr("href"));
				goPage();
			})
			
			
			if($("#user_name").val() == null || $("#user_name").val() == "") {
				
				fetch("/api/member/getUserInfo", {
					method: 'GET',
					headers: {
						'Authorization': localStorage.getItem("Authorization")
					},
				})
				.then(response => {
					if(response.status == 401){
						localStorage.removeItem('Authorization');
						alert("로그인 유효시간이 초과 되었습니다. 다시 로그인 해주세요.");
						window.location.reload();
					}else if(!response.ok) throw new Error("Network response was not ok.");
					
					let Authorization = response.headers.get('Authorization');
					
					if(Authorization != null && Authorization != ""){
						localStorage.removeItem("Authorization");
						localStorage.setItem('Authorization', Authorization);
					}
					
					return response.json();
				})
				.then((data) => {
					$("#user_name").val(data.user_name);
				})
			}
			
			$("#comPosts").click(function(){
				$("#searchForm").attr({
					"method":"get",
					"action":"/community/comPosts?user_name=" + $("#user_name").val()
				});
				$("#searchForm").submit();
			})
			
			$("#fleaPosts").click(function(){
				$("#searchForm").attr({
					"method":"get",
					"action":"/fleaMarket/fleaPosts?user_name=" + $("#user_name").val()
				});
				$("#searchForm").submit();
			})
			
			$("#jobPosts").click(function(){
				$("#searchForm").attr({
					"method":"get",
					"action":"/job/jobPosts?user_name=" + $("#user_name").val()
				});
				$("#searchForm").submit();
			})
			
			$("#roomPosts").click(function(){
				$("#searchForm").attr({
					"method":"get",
					"action":"/roomRental/roomPosts?user_name=" + $("#user_name").val()
				});
				$("#searchForm").submit();
			})
		})
		
		/*검색을 위한 실질적인 처리 함수*/
		function goPage(){
			if($("#search").val()=="all"){
				$("#keyword").val("");
			}
			$("#searchForm").attr({
				"method":"get",
				"action":"/roomRental/roomPosts"
			});
			$("#searchForm").submit();
		}
		
	</script>
</head>
<body class="wrap">
	<div class="rem-20 my-top-15 my-bottom-15">
		<div class="title my-bottom-15">
			<p class="text-center my-7">작성한 글</p>
		</div>
		
		<div class="py-2rem flex justify-right">
			<div class="items-center">
				<form id="searchForm">
					<!-- 페이징 처리를 위한 파라미터 -->
					<input type="hidden" name="pageNum" id="pageNum"  value="${pageMaker.cvo.pageNum }">
					<input type="hidden" name="amount" value="${pageMaker.cvo.amount }">
					<input type="hidden" id="postUserName" name="user_name" value="${postUserName }">
					
					<div class="item-center flex">
						<!-- <label>검색조건</label> -->
						<select id="search" name="search" class="search">
							<option value="all">전체</option>
							<option value="room_title">제목</option>
							<option value="room_content">내용</option>
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
				<li><button type="button" class="postBtn" id="comPosts">커뮤니티</button></li>
				<li><button type="button" class="postBtn" id="fleaPosts">중고악기</button></li>
				<li><button type="button" class="postBtn" id="jobPosts">구인구직</button></li>
				<li><button type="button" class="postBtn" id="roomPosts"  style="background-color:#CED8F6;">합주실/연습실</button></li>
			</ul>
		</div>
		<div class="content">
			<table summary="합주실 리스트" class="table ">
				<thead>
					<tr>
						<th class="text-center col-md-3">제목</th>
						<th data-value="roomRental_date" class="order col-md-1 text-center">작성일</th>
						<th data-value="roomRental_hits" class="order col-md-1 text-center">조회수</th>
					</tr>
				</thead>
				<tbody id = "list" class="">
					<c:choose>
						<c:when test="${ not empty roomPosts }">
							<c:forEach items="${roomPosts }" var="roomPost" varStatus="status">
								<tr class="text-center" data-num="${roomPost.roomRental_no}">
									<td class="class">
										<a class="" href="/roomRental/board/${roomPost.roomRental_no }">${roomPost.roomRental_title}</a>
									</td>
									<td class="col-md-1 ">${roomPost.roomRental_date}</td>
									<td class="col-md-1 ">${roomPost.roomRental_hits }</td>
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