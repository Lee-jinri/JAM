<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 합주실/연습실</title>
	
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
			
			
			$("#roomWriteBtn").click(function(){
				var accessToken = localStorage.getItem("Authorization");
				
				if(accessToken != null) $(location).attr('href','/roomRental/roomRentalWrite');
				else{
					if(confirm("로그인 후 이용할 수 있는 서비스 입니다. 로그인 페이지로 이동하겠습니까?"))$(location).attr('href', '/member/login');
					
				}
			})
			
			
			// 지역 선택
			var accessToken = 'none';													
			var errCnt = 0;
			var accessTimeout;
			
			getAccessToken();		
	     	
		    function getAccessToken(){												
		     	jQuery.ajax({																												
		     		type:'GET', 																											
		     		url: 'https://sgisapi.kostat.go.kr/OpenAPI3/auth/authentication.json',													
		     		data:{																													
		     		consumer_key : 'ba32b11e9af840f3b71f',																					
		     		consumer_secret : 'b90c1515eac4400a9e3e',																				
		     		},																														
		     		success:function(data){	
		     			console.log(data);
		     			errCnt = 0;																									
		     			accessToken = data.result.accessToken;
		     			accessTimeout = data.result.accessTimeout + (4 * 60 * 60 * 1000); // 현재 시간부터 4시간 뒤 만료됨.
		     			
		     			//console.log(accessTimeout);
		     			//getLocations('city');												
		     		},																													
		     		error:function(data) {	
		     			
		     		}																														
		     	});																		
		     }			
		    
			
			$("#city").change(function(){
				cd = $('option:selected', this).data('index');
				
				$("#gu option").not(":first").remove();
				$("#dong option").not(":first").remove();
				
				getLocations('city');
				
			})
			
			$("#gu").change(function(){
				cd = $('option:selected', this).data('index');
				
				$("#dong option").not(":first").remove();
				
				getLocations('gu');
			})
	     	
			
			
			function getLocations(clickedType){
				
				let currentTime = Date.now();
				
				// accessToken이 없거나 토큰의 시간이 만료됐다면 재발급
				if(accessToken == 'none' || currentTime > accessTimeout) getAccessToken();
		    							
				jQuery.ajax({																												
					type:'GET', 																											
					url: 'https://sgisapi.kostat.go.kr/OpenAPI3/addr/stage.json',													
					data:{																													
						accessToken : accessToken,																					
						cd : cd,																				
					},																														
					success:function(data){	
			     		
			     		// 구 변경 
			     		if(clickedType === 'city'){
			     			data.result.forEach(item => {
			     				$("#gu").append('<option value="' + item.addr_name + '" data-index="' + item.cd + '" class="gu">' + item.addr_name + '</option>');
							});
			     				
			     			// 동 변경	
			     		}else if(clickedType === 'gu'){
			     			data.result.forEach(item => {
			     				console.log(item);
								$("#dong").append('<option value="' + item.addr_name + '" class="dong">' + item.addr_name + '</button>');
				     		});
			     		}
			     	},																													
			     	error:function(data) {	
			     			
			    	}																														
				});																		
			     		
		    }
		})
		
		
		
		
		/*검색을 위한 실질적인 처리 함수*/
		function goPage(){
			if($("#search").val()=="all"){
				$("#keyword").val("");
			}
			$("#searchForm").attr({
				"method":"get",
				"action":"/roomRental/boards/"
			});
			$("#searchForm").submit();
		}
	</script>
</head>
<body class="wrap">
	<div class="rem-20 my-top-15 my-bottom-15">
		<div class="title">
			<p class="text-center">합주실/연습실 </p>
		</div>
		<div class="content">
			<div class="justify-between flex py-4">
				<div class="write_btn write_btn_border flex items-center border-radius-7px">
					<img class="icon" src="/resources/include/images/write.svg">
					<button type="button" id="roomWriteBtn" class="write_btn_font border-none bColor_fff">작성하기</button>
				</div>
			</div>
			
			<div class="border-top border-bottom py-2rem flex justify-center">
				
				<div class="items-center">
					<form id="searchForm">
						<!-- 페이징 처리를 위한 파라미터 -->
						<input type="hidden" name="pageNum" value="${pageMaker.cvo.pageNum }">
						<input type="hidden" name="amount" value="${pageMaker.cvo.amount }">
						
						<div class="py-8">
							<div class="flex">
								<div>
									<div class="block">
										<select name="city" id="city">
											<option value="" class="city">시·도</option>
											<option value="서울" data-index="11" class="city">서울</option>
											<option value="부산" data-index="21" class="city">부산</option>
											<option value="대구" data-index="22" class="city">대구</option>
											<option value="인천" data-index="23" class="city">인천</option>
											<option value="광주" data-index="24" class="city">광주</option>
											<option value="대전" data-index="25" class="city">대전</option>
											<option value="울산" data-index="26" class="city">울산</option>
											<option value="세종" data-index="29" class="city">세종</option>
											<option value="경기" data-index="31" class="city">경기</option>
											<option value="강원" data-index="32" class="city">강원</option>
											<option value="충북" data-index="33" class="city">충북</option>
											<option value="충남" data-index="34" class="city">충남</option>
											<option value="전북" data-index="35" class="city">전북</option>
											<option value="전남" data-index="36" class="city">전남</option>
											<option value="경북" data-index="37" class="city">경북</option>
											<option value="경남" data-index="38" class="city">경남</option>
											<option value="제주" data-index="39" class="city">제주</option>
										</select>
									</div>
								</div> 
									
								<div>
									<div id="guDiv" class="">
										<select name="gu" id="gu">
											<option value="">시·구·군</option>
										</select>
									</div>
								</div>
									
								<div>
									<div id="dongDiv" class="">
										<select name="dong" id="dong">
											<option value="">동·읍·면</option>
										</select>
									</div>
								</div>
							</div>
						</div>
						
						
						<div class="items-center flex">
							<select id="search" name="search" class="search">
								<option value="all">전체</option>
								<option value="room_title">제목</option>
								<option value="room_content">내용</option>
								<option value="user_name">작성자</option>
							</select>
							<input type="text" name="keyword" id="keyword" placeholder="구인구직 내에서 검색" class="border border-radius-43px rem-2 search"/>
							<img class="icon" id="searchBtn" style="cursor:pointer; width:3rem;" src="/resources/include/images/search.svg">
						</div>
					</form>
				</div>
			</div>	
			
			
			
			
			<div>
				<ul>
					<c:forEach items="${roomList }" var="room" varStatus="status">
						<li class="border-bottom">
							<div class="pd-2rem">
								<div class="my-bottom-4">
									<span>${room.user_name }</span>
									<span> | </span>
									<span>${room.roomRental_date }</span>
								</div>
								
								<div class=inline-block>
									<c:if test="${room.roomRental_status == 0}">
										<span class="border-g " style="color:#04B431;">거래중</span>
										<div class="inline-block my-bottom-4 ml-1">
											<a class="font-weight-bold"  href="/roomRental/board/${room.roomRental_no }">${room.roomRental_title }</a>
										</div>
										
									</c:if>
									<c:if test="${room.roomRental_status == 1}">
										<span class="border" style="color:#A4A4A4;">거래 완료</span>
										<div class="inline-block my-bottom-4 ml-1">
											<a class="font-weight-bold" style="color:#A4A4A4;"href="/roomRental/board/${room.roomRental_no }">${room.roomRental_title }</a>
										</div>
										
									</c:if>
								</div>
								<div class="flex float-right items-center width-13rem justify-between">
									<img class="icon" id="" style="width:3rem;" src="/resources/include/images/hits.svg">
									<span>${room.roomRental_hits }</span>
									<img class="icon ml-2" id="" style="width:3rem;" src="/resources/include/images/reply.svg">
									<span>${room.roomRental_reply_cnt }</span>
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