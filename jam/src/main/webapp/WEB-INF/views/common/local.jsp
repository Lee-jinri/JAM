<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset=UTF-8>
<title></title>

<link rel="stylesheet" href="/resources/include/dist/css/layout.css" />
<script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js"></script>
		
		
<script type="text/javascript">
	$(function(){
		
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
	     			getLocations('city');												
	     		},																													
	     		error:function(data) {	
	     			
	     		}																														
	     	});																		
	     }			
	    
		// 기본 값 서울
		var cd = 11;
		
		$(".city").click(function(){
			cd = this.value;
			$("#guDiv").empty();
			
			getLocations('city');
			
		})
		
		$("#guDiv").on("click", ".gu", function() {
		    cd = this.value;
		    $("#dongDiv").empty();
		
		    getLocations('gu');
		});
		
		$("#dongDiv").on("click", ".dong", function() {
			console.log(this.value);
		});
     	
	    
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
		     			console.log(clickedType);
		     			console.log("cd : " + cd);
		     			// 구 변경 
		     			if(clickedType === 'city'){
		     				data.result.forEach(item => {
								$("#guDiv").append('<button value="' + item.cd + '" class="gu">' + item.addr_name + '</button>');
			     			});
		     				
		     			// 동 변경	
		     			}else if(clickedType === 'gu'){
		     				data.result.forEach(item => {
								$("#dongDiv").append('<button value="' + item.full_addr + '" class="dong">' + item.addr_name + '</button>');
			     			});
		     			}else if(clickedType === 'dong'){
		     				console.log(data.result);
		     				
		     			}
		     		},																													
		     		error:function(data) {	
		     			
		     		}																														
		     	});																		
		     		
	    }
	    
		
	})
</script>
</head>
<body>
	<div>
		<div>
			<input type="text" id="location" name="location">
		</div>
		<div>
			<div>
				<span>시·도</span>
			</div>
			<div>
				<button value="11" class="city">서울</button>
				<button value='21' class="city">부산</button>
				<button value='22' class="city">대구</button>
				<button value='23' class="city">인천</button>
				<button value='24' class="city">광주</button>
				<button value='25' class="city">대전</button>
				<button value='26' class="city">울산</button>
				<button value='29' class="city">세종</button>
				<button value='31' class="city">경기</button>
				<button value='32' class="city">강원</button>
				<button value='33' class="city">충북</button>
				<button value='34' class="city">충남</button>
				<button value='35' class="city">전북</button>
				<button value='36' class="city">전남</button>
				<button value='37' class="city">경북</button>
				<button value='38' class="city">경남</button>
				<button value='39' class="city">제주</button>
			</div>
		</div>
			<div>
				<span>시·구·군</span>
			</div>
			<div id="guDiv">
			
			</div>
		<div>
		
		</div>
			<div>
				<span>동·읍·면</span>
			</div>
			<div id="dongDiv">
			
			</div>
		<div>
		
		</div>
	</div>


</body>
</html>