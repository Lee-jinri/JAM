<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>커뮤니티 작성 글</title>
<script>
	$(function(){

		loadPosts();
		
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
		
		$("#comPosts").click(function(){
			setURL("/community/comPosts");
		})
		
		$("#fleaPosts").click(function(){
			setURL("/fleaMarket/fleaPosts");
		})
		$("#jobPosts").click(function(){
			setURL("/job/jobPosts");
		})
		$("#roomPosts").click(function(){
			setURL("/roomRental/roomPosts");
		})
	})
	
	function goPage(){
		if($("#search").val()=="all"){
			$("#keyword").val("");
		}
		
		let search = $("#search").val();
	    let keyword = $("#keyword").val();
	    
	    let params = new URLSearchParams(window.location.search);
	    
	    params.delete("search");
	    params.delete("keyword");
	    
	    params.append("search", search);
	    params.append("keyword", keyword);
	    
	    let url = "/community/comPosts?" + params.toString();

	    console.log(url);
	    
	    location.href = url;
	}
	
	function setURL(url) {
	    let params = new URLSearchParams(window.location.search);
	    let type = params.get("type");
	    let userId = params.get("userId");
	
	    if(type == 'my') url += '?type=my';
	    else {
	        url += '?userId=' + encodeURIComponent(userId);
	    }
	    
	    location.href = url;
	}
	
	/*검색을 위한 실질적인 처리 함수
	function goPage(){
			let search = $("#search").val();
			let keyword = $("#keyword").val();
			
			if(search=="all"){
				$("#keyword").val("");
			}
			이거 바꿔야됨 restful한 방식으로 할건데
			sessionStorage.setItem('postUserId', user_id);
			이렇게 해서 누구의 글을 볼건지 저장하고 세션에서 꺼내서
			클라이언트에서 fetch해서 가져와야 될 듯? 너가 알아서 해봥ㅎ
			let url = "/posts/comPosts?user_id=";
			
			$(location).attr('href', redirectURL + user_id + "&search=" + search + "&keyword=" + keyword);
			//getUserIDAndRedirect(url, search, keyword);
		}*/
		/*
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
		            alert("");
		        	$(location).attr('href', '/member/login');
		        }
		    })
		    .catch(error => {
		        console.error('사용자 정보를 가져오는 중 오류 발생:', error);
		    });
		}
		
		*/
		
		function loadPosts() {
			let params = new URLSearchParams(window.location.search);
			
		    let queryString = new URLSearchParams(window.location.search);
		    //let url = "/api/community/posts" + (queryString ? "?" + queryString : "");
		    let url = "/api/community/posts?"+queryString;
		    console.log(url);
		    
			fetch(url, {
		        method: 'GET'
		    })
		    .then(response => {
		        if (!response.ok) {
		        	throw new Error('Network response was not ok');
		        } 
		        return response.json();
		    })
		    .then((data) => {
		    	let html;
		    	let posts = data.posts;
		    	if (posts.length > 0) {
	                $.each(posts, function(index, item) {
	                    html += '<tr class="text-center" data-num="' + item.com_no + '">';
	                    html += '  <td class="class">';
	                    html += '    <a href="/community/board/' + item.com_no + '">' + item.com_title + '</a>';
	                    html += '  </td>';
	                    html += '  <td class="col-md-1">' + item.com_date + '</td>';
	                    html += '  <td class="col-md-1">' + item.com_hits + '</td>';
	                    html += '</tr>';
	                });
	            } else {
	                html += '<tr>';
	                html += '  <td colspan="3" class="text-center">등록된 게시물이 존재하지 않습니다.</td>';
	                html += '</tr>';
	            }

	            $('#postList').html(html);
	            
	         	// 페이징 처리
	            let pageMaker = data.pageMaker;
	            
	            console.log(pageMaker.cvo.pageNum);
	            
	            let paginationHtml = '';
	            for (let i = pageMaker.startPage; i <= pageMaker.endPage; i++) {
	            	let currentParams = new URLSearchParams(window.location.search);
	                currentParams.set("pageNum", i); 

	                paginationHtml += '<li class="paginate_button">';
	                paginationHtml += '  <a href="?' + currentParams.toString() + '" ' + (pageMaker.cvo.pageNum === i ? 'id="btnColor"' : '') + '>' + i + '</a>';
	                paginationHtml += '</li>';
	            }
	            
	            // 페이징 버튼 렌더링
	            $('#pagination').html(paginationHtml);
		    })
		    .catch(error => {
		        console.error('error:', error);
		    });
		    
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
					<div class="item-center flex">
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
		<div class="border-bottom">
			<ul class="nav nav-tabs nav-justified">
				<li><button type="button" id="comPosts" class="postBtn" style="background-color:#CED8F6;">커뮤니티</button></li>
				<li><button type="button" id="fleaPosts" class="postBtn">중고악기</button></li>
				<li><button type="button" id="jobPosts" class="postBtn">구인구직</button></li>
				<li><button type="button" id="roomPosts" class="postBtn">합주실/연습실</button></li>
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
        		<tbody id="postList">
	            	<!-- 게시물 리스트가 존재하는 경우에만 표시 
	            	<c:if test="${not empty comPosts}">
	                	<c:forEach items="${comPosts}" var="comPost" varStatus="status">
	                    	<tr class="text-center" data-num="${comPost.com_no}">
		                        <td class="class">
		                            <a class="" href="/community/board/${comPost.com_no}">${comPost.com_title}</a>
		                        </td>
		                        <td class="col-md-1">${comPost.com_date}</td>
		                        <td class="col-md-1">${comPost.com_hits}</td>
		                    </tr>
	                	</c:forEach>
	            	</c:if>-->
	
			        <!-- 게시물이 없을 경우에만 표시
			        <c:if test="${empty comPosts}">
			        	<tr>
			            	<td colspan="3" class="text-center">등록된 게시물이 존재하지 않습니다.</td>
			            </tr>
			        </c:if>
			         -->
		    	</tbody>
		    </table>
		</div> 

		 페이징 
		<div class="text-center">
			<ul id="pagination" class="pagination pagination_border"><!--
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
				</c:if>-->
			</ul>
		</div>
		
	</div>
</body>
</html>