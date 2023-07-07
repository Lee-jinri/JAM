<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>마이페이지 - 커뮤니티 작성 글</title>
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
		})
		
		/*검색을 위한 실질적인 처리 함수*/
		function goPage(){
			if($("#search").val()=="all"){
				$("#keyword").val("");
			}
			$("#searchForm").attr({
				"method":"get",
				"action":"/member/comMyWrite/"
			});
			$("#searchForm").submit();
		}
	</script>
</head>
<body>
	<div class="rem-20 my-top-15 my-bottom-15">
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
							<option value="com_title">제목</option>
							<option value="com_content">내용</option>
						</select>
						<div>
							<input type="text" name="keyword" id="keyword" placeholder="검색" class="border border-radius-43px rem-2 search"/>
						</div>
						<img class="icon" id="searchBtn" style="cursor:pointer; width:3rem;" src="/resources/include/images/search.svg">
					</div>
				</form>
			</div>
		</div>	
		
		<div class="clear-both">
			<ul class="nav nav-tabs nav-justified">
				<li class="active"><a href="/member/comMyWrite">커뮤니티</a></li>
				<li><a href="/member/fleaMyWrite">중고악기</a></li>
				<li><a href="/member/jobMyWrite">구인구직</a></li>
				<li><a href="/member/roomMyWrite">합주실/연습실</a></li>
			</ul>
		</div>
		
		
		
		<div class="content">
			<table summary="커뮤니티 리스트" class="table ">
				<thead>
					<tr>
						<th class="text-center col-md-3">제목</th>
						<th data-value="com_date" class="order col-md-1 text-center">작성일</th>
						<th data-value="com_hits" class="order col-md-1 text-center">조회수</th>
					</tr>
				</thead>
				<tbody id = "list" class="">
					<c:choose>
						<c:when test="${ not empty comMyWrite }">
							<c:forEach items="${comMyWrite }" var="comBoard" varStatus="status">
								<tr class="text-center" data-num="${comBoard.com_no}">
									<td class="class">
										<a class="" href="/community/communityDetail/${comBoard.com_no }">${comBoard.com_title}</a>
									</td>
									<td class="col-md-1 ">${comBoard.com_date}</td>
									<td class="col-md-1 ">${comBoard.com_hits }</td>
								</tr>
							</c:forEach>
						</c:when>
						<c:otherwise>
							<tr>
								<td colspan="5" class="text-center">등록된 게시물이 존재하지 않습니다.</td>
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