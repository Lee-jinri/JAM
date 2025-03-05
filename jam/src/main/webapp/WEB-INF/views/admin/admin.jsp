<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/common.jspf"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM</title>

<script>
	$(function() { 
			fetch('http://localhost:8080/api/member/getUserInfo', {
				method: 'GET'
			})
			.then(response => {
				if(!response.ok) throw new Error('Network response was not ok.');
				return response.json();
			})
			.then(data => {				
				console.log(data);
				
				if (!data.role.includes("ROLE_ADMIN")) {
					alert("접근 권한이 없는 페이지 입니다.");
					window.location.href = "/";
				}
			})
			.catch(error => {
				console.error('사용자 정보를 가져오는 중 오류 발생:', error);
				alert("시스템 오류입니다. 잠시 후 다시 시도해주세요.");
				window.location.href = "/";
			});
			
			
			// 지역 설정 버튼 클릭 이벤트
		    $("#areaUpdate").click(async function() {
		        if (!confirm("전국 지역 데이터를 새로 저장하시겠습니까?")) {
		            return;
		        }

		        try {
		            const response = await fetch('/admin/updateAreas', {
		                method: 'POST',
		                headers: {
		                    'Content-Type': 'application/json'
		                }
		            });

		            const result = await response.json();

		            if (result.success) {
		                alert("전국 지역 데이터 저장 완료!");
		            } else {
		                alert("저장 중 오류 발생: " + (result.error || "알 수 없는 오류"));
		            }
		        } catch (error) {
		            console.error("지역 데이터 저장 요청 중 오류:", error);
		            alert("서버 요청 실패");
		        }
		    });
	});

	
	</script>
</head>
<body class="wrap">
	<header>
		<h1>관리자 페이지</h1>
	</header>
	<nav>
		
	</nav>
	<main>
		<section>
			<button id="areaUpdate">지역 설정</button>
			<ul>
				<li><a href="#">대시보드</a></li>
				<li><a href="#">사용자 관리</a></li>
				<li><a href="#">설정</a></li>
			</ul>
		</section>
		<span>
			1. 사용자 통계<br>
			사용자 등록 수: 일별, 월별, 연도별 신규 등록 사용자 수<br>
			활성 사용자 수: 일정 기간 동안 로그인한 사용자 수<br>
			회원가입 경로: 사용자가 어떤 경로(소셜 미디어, 직접 가입, 추천 등)를 통해 회원가입했는지<br>
			사용자 세그먼트: 연령대, 성별, 지역별 사용자 분포<br>
			사용자 유지율: 일정 기간 후에도 계속해서 사이트를 사용하는 사용자의 비율<br>
			2. 경제적 통계<br>
			매출액: 제품 판매, 서비스 이용료 등으로부터 발생하는 총 매출<br>
			거래수: 완료된 거래의 총 수<br>
			환불 및 취소 통계: 환불이나 취소된 거래의 수와 그로 인한 금액<br>
			수익성 분석: 상품이나 서비스 별 수익성 평가<br>
			3. 웹사이트 활동 통계<br>
			페이지 뷰: 웹페이지 별 방문 횟수<br>
			세션 수: 사이트 방문 세션 수<br>
			평균 세션 지속 시간: 사용자가 웹사이트에서 보내는 평균 시간<br>
			반송률 (Bounce Rate): 단일 페이지에서 세션을 종료하는 사용자의 비율<br>
			트래픽 소스: 웹사이트로 유입되는 트래픽의 출처(직접 방문, 검색 엔진, 소셜 미디어 등)<br>
			4. 콘텐츠 및 서비스 이용 통계<br>
			가장 인기 있는 콘텐츠: 가장 많이 조회된 페이지나 게시물<br>
			서비스 이용 행태: 사용자들이 가장 많이 이용하는 서비스 종류<br>
			피드백 및 평가: 사용자 리뷰와 평점<br>
			5. 기술적 통계<br>
			시스템 성능: 페이지 로딩 시간, 서버 응답 시간<br>
			에러 로그: 시스템 에러, 방문자에게 나타난 문제의 로그<br>
			모바일 대비 데스크톱 사용: 사용자들이 사이트를 방문할 때 사용하는 기기 종류<br>
			6. 마케팅 및 광고 성과<br>
			광고 ROI: 광고 비용 대비 수익<br>
			캠페인 성과: 마케팅 캠페인별 성과 분석<br>
			소셜 미디어 반응: 포스팅에 대한 좋아요, 공유, 댓글 수<br>
			7. 지역 통계<br>
			지역별 사용자 활동: 특정 지역에서의 사용자 활동 및 선호도<br>
			지역별 매출 분석: 지역별 매출과 수익성<br>
		</span>
	</main>
</body>
</html>