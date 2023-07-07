<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 중고악기</title>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
	<style type="text/css">
		.icon {width : 35px;}
		a { 
			text-decoration: none; 
		}
		.inline-block {display : inline-block;}
		.justify-between {
		    justify-content: space-between;
		}
		
		.items-center {
		    align-items: center;
		}
		.flex {
		    display: flex;
		}
		.border-top {
			border-top-color: hsla(220,9%,46%,.3);
			border-top: 1px solid #e5e7eb;
		}
		.border-bottom {
			border-bottom-color: hsla(220,9%,46%,.3);
			border-bottom: 1px solid #e5e7eb;
		}
		.py-4 {
		    padding-top: 1rem;
		    padding-bottom: 1rem;
		}
		.my-7 {
		    margin-top: 1.75rem;
		    margin-bottom: 1.75rem;
		}
		.border-none {
		    border-style: none;
		}
		.width-13rem {
			width : 13rem;
		}
		#btnColor {background-color : #FDE4A4}
	</style>
	
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
	})
	
	/*검색을 위한 실질적인 처리 함수*/
	function goPage(){
		if($("#search").val()=="all"){
			$("#keyword").val("");
		}
		$("#searchForm").attr({
			"method":"get",
			"action":"/fleaMarket/fleaMarketList/"
		});
		$("#searchForm").submit();
	}
	</script>
</head>
<body>
	<div class="rem-20  my-top-15 my-bottom-15">
		<div class="title">
			<p class="text-center">중고악기</p>
		</div>
		<div class="content">
			<div class="justify-between flex py-4">
				<div class="write_btn write_btn_border flex items-center border-radius-7px">
					<img class="icon" src="/resources/include/images/write.svg">
					<a class="write_btn_font" href="/fleaMarket/fleaWrite">작성하기</a>
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
				<ul>
					<c:forEach items="${fleaMarketList }" var="fleaMarketBoard" varStatus="status">
						<li class="border-bottom">
							<div class="pd-2rem">
								<div class="my-bottom-4">
									<span>${fleaMarketBoard.user_name }</span>
									<span> | </span>
									<span>${fleaMarketBoard.flea_date }</span>
								</div>
								
								
								<div class=inline-block>
									<c:choose>
						        		<c:when test = "${fleaMarketBoard.sales_status == 0}">
						            		<c:if test="${fleaMarketBoard.flea_category == 0}">
												<span class="border-g " style="color:#04B431;">판매</span>
												<div class="inline-block my-bottom-4 ml-1">
													<a class="font-weight-bold" href="/fleaMarket/fleaMarketDetail/${fleaMarketBoard.flea_no }">${fleaMarketBoard.flea_title }</a>
												</div>
											</c:if>
											<c:if test="${fleaMarketBoard.flea_category == 1}">
												<span class="border-r" style="color:#FE2E2E;">구매</span>
												<div class="inline-block my-bottom-4 ml-1">
													<a class="font-weight-bold" href="/fleaMarket/fleaMarketDetail/${fleaMarketBoard.flea_no }">${fleaMarketBoard.flea_title }</a>
												</div>
											</c:if>
										</c:when>
										<c:otherwise>
											<span class="border" style="color:#A4A4A4;">거래 완료</span>
											<div class="inline-block my-bottom-4 ml-1">
												<a class="font-weight-bold" style="color:#A4A4A4;" href="/fleaMarket/fleaMarketDetail/${fleaMarketBoard.flea_no }">${fleaMarketBoard.flea_title }</a>
											</div>
								        </c:otherwise>
									</c:choose>
								</div>
								<div class="">
									<span>${fleaMarketBoard.price }원</span>
									<div class="flex float-right items-center width-13rem justify-between">
										<img class="icon" id="" style="width:3rem;" src="/resources/include/images/hits.svg">
										<span class="font-size-1 ml-05">${fleaMarketBoard.flea_hits }</span>
										<img class="icon ml-2" id="" style="width:3rem;" src="/resources/include/images/reply.svg">
										<span class="font-size-1 ml-05">${fleaMarketBoard.flea_reply_cnt }</span>
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