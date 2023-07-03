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
			$("#com_btn").click(function(){
				
			})
		})	
	
	</script>
</head>
<body>
	<div class="rem-20  my-top-15 my-bottom-15">
		<div class="title my-bottom-15">
			<p class="text-center my-7">작성한 글</p>
		</div>
		<div>
			<ul class="nav nav-tabs nav-justified">
				<li><a href="/member/comMyWrite">커뮤니티</a></li>
				<li><a href="/member/fleaMyWrite">중고악기</a></li>
				<li class="active"><a href="/member/jobMyWrite">구인구직</a></li>
				<li><a href="/member/roomMyWrite">합주실/연습실</a></li>
			</ul>
		</div>
		
		<form id="f_search" name="f_search" class="form-inline">
			<!-- 페이징 처리를 위한 파라미터 -->
			<input type="hidden" name="pageNum" value="${pageMaker.cvo.pageNum}">
			<input type="hidden" name="amount" value="${pageMaker.cvo.amount}">
		</form>
		
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
											<a class="" href="/job/jobDetail/${jobBoard.job_no }">${jobBoard.job_title}</a>
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
							<a id="${pageMaker.cvo.pageNum == num ? 'btnColor':''}" class="font-weight-bold"  style="color:#585858;" href="${num}">${num}</a>
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
</body>
</html>