<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - Jobs</title>

<!-- 서머노트 -->
<script src="/resources/include/dist/summernote/summernote-lite.js"></script>
<script src="/resources/include/dist/summernote/summernote-ko-KR.js"></script>
<link rel="stylesheet" href="/resources/include/dist/summernote/summernote-lite.css">
<script src="/resources/include/dist/js/area.js"></script>	
<style>
/* layout.css에 추가했음 */
.jam-card {
    background: #fff;  
    padding: 20px;
    border-radius: 12px;
    border: 1px solid #ddd;
    margin-bottom: 16px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05); 
}
.jam-card-title {
    font-weight: bold;
    font-size: 16px;
    margin: 0;
    color: #333;
}
.jam-card-header {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}

.jam-flex {
    display: flex;
    gap: 10px;
}
.jam-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
}
.jam-grid label {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 40px;
    background: #f7f8fa;
    border: 1px solid #ddd;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.3s, border-color 0.3s;
    font-size: 14px;
    font-weight: 500;
}
.jam-grid label:hover {
    background-color: #e9f5ff;
    border-color: #007bff;
}
.jam-grid input[type="radio"] {
    display: none; /* 기본 라디오 숨김 */
}

.jam-type-select {
    display: flex;
    align-items: center;
    justify-content: flex-start;
}

.jam-radio-label {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    cursor: pointer;
    border-radius: 20px;
    border: 1px solid #ddd;
    background-color: #f9f9f9;
    transition: background-color 0.3s, border-color 0.3s, color 0.3s;
    font-size: 14px;
    font-weight: 500;
    color: #666;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    position: relative;
}

.jam-type-member {
    background-color: #ffdd77;
    color: #333;
}

.jam-type-company {
    background-color: #003366;
    color: #fff;
}
.jam-radio-text {
    padding: 6px 12px;
    border-radius: 20px;
    transition: background-color 0.3s, color 0.3s;
    text-align: center;
    line-height: 1;
    white-space: nowrap;
}

.jam-radio-label span::before {
    display: none;
}
.jam-type-select label {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-weight: 500;
    font-size: 14px;
    cursor: pointer;
    margin-right: 15px;
}
.jam-type-select-wrapper {
    display: inline-block;
    padding: 6px 10px; 
    border-radius: 12px;
    transition: all 0.3s;
    margin-bottom: 16px;
}
.jam-type-select-wrapper.focused {
    box-shadow: 0 0 0 3px rgba(255, 51, 85, 0.8); 
    border: 2px solid rgba(255, 51, 85, 0.8);
}

.area-wrapper.focused{
	background-color: #ffe7e7;
}

.position-wrapper.focused{
	background-color: #ffe7e7;
}

.jam-input {
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 8px;
    width: 100%;
    box-sizing: border-box;
}

.jam-btn-group{
	display: flex;
    gap: 10px;
    justify-content: center; 
    margin-top: 20px;
}

.jam-btn {
    padding: 10px;
    font-size: 15px;
    font-weight: 600;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 5px rgba(0,0,0,0.1);
    min-width: 70px;
    text-align: center;
}

/* 등록 버튼 */
.jam-btn.register {
    background-color: #007bff;
    color: #fff;
}
.jam-btn.register:hover {
    background-color: #0056b3;
    box-shadow: 0 3px 6px rgba(0,0,0,0.15);
}

/* 취소 버튼 */
.jam-btn.cancel {
    background-color: #f0f0f0;
    color: #333;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.jam-btn.cancel:hover {
    background-color: #e0e0e0;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.15);
}
.jam-pay-section {
    padding: 12px 16px;
    border: 1px solid #ddd;
    border-radius: 8px;
    background: #f9f9f9;
    display: flex;
    gap: 8px;
    align-items: center;
    width: fit-content; 
    margin-bottom: 16px;
}
.jam-pay-section h3 {
    font-size: 14px;
    font-weight: bold;
    margin: 0;
    color: #555;
}
.jam-pay-section select,
.jam-pay-section input {
    padding: 6px 10px;
    font-size: 14px;
    border: 1px solid #ccc;
    border-radius: 6px;
    max-height: 30px;
}
.pay_input{
	text-align: right;
	width: 100px;
}
.setting-base-row {
    display: flex;
    justify-content: space-between;
    gap: 8px;  /* 컬럼 사이 간격 */
}
.setting-base__col--title {
    flex: 1;
    height: 40px;
    line-height: 40px;
    font-weight: 600;
    font-size: 1.2rem;
    text-align: center;
    border-bottom: 1px solid #e5e8ec;
    color: #4c515b;
    background-color: #f7f8fa;
    border-right: 1px solid #e5e8ec;
}
.setting-base__col {
    flex: 1;  
    border: 1px solid #e5e8ec;
    border-radius: 4px;
    background-color: #f9f9f9;
    overflow: hidden;
}
.setting-base__col--list {
    max-height: 200px;
    overflow-y: auto;
    background-color: #fff;
    padding: 8px;
    border: 1px solid #e5e8ec;
    border-radius: 4px;
}
.setting-base__col--list ul {
    list-style: none;
    padding: 0;
    margin: 0;
}
.setting-base__col--list li {
    padding: 8px 12px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.2s;
    border-bottom: 1px solid #f0f0f0;
}
.setting-base__col--list li:last-child {
    border-bottom: none;
}
.setting-base__col--list li:hover {
    background-color: #f0f5ff;
}
.setting-base__col--list .placeholder {
    color: #aaa;
    font-style: italic;
    text-align: center;
}
/* 선택된 지역 표시 */
.selected-area-wrapper {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-left: 15px;
    padding: 8px 12px;
    background: #f0f5ff;
    border: 1px solid #cce5ff;
    border-radius: 16px;
    font-size: 14px;
    color: #007bff;
    font-weight: 500;
    max-width: fit-content;
    white-space: nowrap;
}

/* x버튼 */
.area-remove-btn {
    background: none;
    border: none;
    font-weight: bold;
    color: #ff6b6b;
    cursor: pointer;
    font-size: 14px;
    line-height: 1;
}

/* 선택된 지역이 없을 때 기본값 */
.selected-area-badge:empty::before {
    content: '선택된 지역 없음';
    color: #999;
    font-weight: normal;
}
.selected-area-badge {
    font-weight: 500;
    color: #007bff;
}
</style>
<script>
	$(function(){
		if(!window.MY_ID){
			if (confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")) {
				location.href = "/member/login";
			} else {
				location.href = "/jobs/boards";
			}
			return;
		}
		
		window.ROLE_COMPANY = !!window.ROLE_COMPANY;

		if (window.ROLE_COMPANY) {
			// 기업회원
			$('#jobCategoryText').text("기업 구인");
			$("#jobCategoryText").addClass("jam-type-company");
			$('.jam-pay-section').stop(true, true).slideDown(300);
		} else {
			// 개인회원
			$('#jobCategoryText').text("멤버 모집");
			$("#jobCategoryText").addClass("jam-type-member");
			$('.jam-pay-section').stop(true, true).hide();
		}
		
		$('input[name="position"]').on('change', function() {
			$('.position-wrapper').removeClass('focused'); 
	    });
		
		$('#payNegotiable').on('change', function() {
	        if ($(this).is(':checked')) {
	            $('#pay').prop('disabled', true).val(''); // 입력값도 지워줌
	        } else {
	            $('#pay').prop('disabled', false);
	        }
	    });
		
		$('#pay').on('input', function() {
	        let value = $(this).val();
	        $(this).val(value.replace(/[^0-9]/g, ''));  // 숫자만 허용
	    });
		
		$(".jam-grid input[type='radio']").on('change', function() {
		    $(".jam-grid label").css({ background: "", color: "", borderColor: "" });  // 초기화

		    if ($(this).is(":checked")) {
		        $(this).parent().css({
		            background: "#003366",
		            color: "#fff",
		            borderColor: "#003366"
		        });
		    }
		});


		$("#write").click(function(){
			$(this).prop("disabled", true);
			
			// 유효성 검사
			let title = $("#job_title").val();
			let job_content = $("#job_content").val();
			let pay = $("#pay").val() || null;
			let category = window.ROLE_COMPANY ? '0' : '1';
			let pay_category = $("#payNegotiable").is(":checked") ? '3' : $("#pay_category").val();
			let city = $("#city").val() || null;
			let gu = $("#gu").val() || null;
			let dong = $("#dong").val() || null;
			let position = $('input[name="position"]:checked').val() || null;
			
			if(city == null){
				alert("지역을 선택하세요.");
				$(".area-wrapper").addClass('focused');
				$("#write").prop("disabled", false); 
				return false;
			}
			
			if(category === '0'){
				if(pay_category === '3'){
					pay = 0;
				}else if(pay_category !== '3' && pay == null){
					alert("급여를 입력하세요.");
					$("#pay").focus();
					return false;
				}
			}
			
			if(position == null){
				alert("포지션을 선택하세요.");
				$('.position-wrapper').addClass('focused');
				$("#write").prop("disabled", false); 
				return false;
			}
			
			if(title.replace(/\s/g,"") == ""){
				alert("제목을 입력하세요.");
				$("#job_title").focus();
				$("#write").prop("disabled", false); 
				return false;
			}
			
			if(job_content.replace(/\s/g,"") == ""){
				alert("본문을 입력하세요.");
				$("#job_content").focus();
				$("#write").prop("disabled", false); 
				return false;
			}
			
			var data = {
					'title':title,
					'content':job_content,
					'category':category,
					'pay':pay,
					'pay_category':pay_category,
					'position':position,
					'city':city,
					'gu':gu,
					'dong':dong
			};
			
			fetch('/api/jobs/post', {
			    method: 'POST',
			    headers: {
			        'Content-Type': 'application/json'
			    },
			    body: JSON.stringify(data)
			})
			.then(res => {
				if (res.ok) {
			        return res.text(); 
			    }

		    	return res.json().then(err => {
		            throw err;
		        });
			})
			.then(body => {
			    alert("등록이 완료되었습니다.");

			    if (body) {
			        $(location).attr('href', '/jobs/post/' + body);
			    }
			})
			.catch(err => {
				if (handleApiError(err, "/jobs/board")) return;
			    alert(err.detail || '공고 등록에 실패했습니다.');
			    $("#write").prop("disabled", false); 
			});
		});
		
	})
</script>
</head>
<body class="wrap">
	<div class="my-top-15 my-bottom-15">
		<div class="jam-type-select-wrapper">
			<sec:authorize access="hasRole('COMPANY')">
				<script>window.ROLE_COMPANY = true;</script>
			</sec:authorize>
			<div class="jam-type-select">
				<span class="jam-radio-text" id="jobCategoryText"></span>
			</div>	
		</div>
		
		<!-- 기업 구인일 때만 보여줄 급여 정보 -->
		<div class="jam-pay-section" style="display: none;">
			<div class="job-card-header">
		    	<h3>급여 정보</h3>
		    </div>
		    <select id="pay_category">
		    	<option value="0">건별</option>
		        <option value="1">주급</option>
		        <option value="2">월급</option>
		    </select>
		    <input type="number" id="pay" class="pay_input" placeholder="급여 (원)" style="text-align: right;">
		    <label><input type="checkbox" id="payNegotiable" value="3"> 협의 후 결정</label>
		</div>

		
		<div class="jam-card area-wrapper">
		    <div class="jam-card-header">
		        <h3 class="jam-card-title">지역 선택</h3>
		        <div id="selectedAreaWrapper" class="selected-area-wrapper">
		            <span id="selectedArea" class="selected-area-badge"></span>
		            <input type="hidden" id="city">
					<input type="hidden" id="gu">
					<input type="hidden" id="dong">
		        </div>
		    </div>

		    
		    <div class="setting-base-row">
				<div class="setting-base__col setting-base__col--title">시·도</div>
				<div class="setting-base__col setting-base__col--title">구·군</div>
				<div class="setting-base__col setting-base__col--title">동·읍·면</div>
			</div>
			<div class="setting-base-row">
				<div class="setting-base__col setting-base__col--list">
					<ul id="cityList">
						<li class="city cursor-pointer" data-city="서울">서울</li>
		                <li class="city cursor-pointer" data-city="부산">부산</li>
		                <li class="city cursor-pointer" data-city="대구">대구</li>
		                <li class="city cursor-pointer" data-city="인천">인천</li>
		                <li class="city cursor-pointer" data-city="광주">광주</li>
		                <li class="city cursor-pointer" data-city="대전">대전</li>
		                <li class="city cursor-pointer" data-city="울산">울산</li>
		                <li class="city cursor-pointer" data-city="세종">세종</li>
		                <li class="city cursor-pointer" data-city="경기">경기</li>
		                <li class="city cursor-pointer" data-city="강원">강원</li>
		                <li class="city cursor-pointer" data-city="충북">충북</li>
		                <li class="city cursor-pointer" data-city="충남">충남</li>
		                <li class="city cursor-pointer" data-city="전북">전북</li>
		                <li class="city cursor-pointer" data-city="전남">전남</li>
		                <li class="city cursor-pointer" data-city="경북">경북</li>
		                <li class="city cursor-pointer" data-city="경남">경남</li>
		                <li class="city cursor-pointer" data-city="제주">제주</li>
					</ul>
				</div>
				<div class="setting-base__col setting-base__col--list">
		            <ul id="guList">
		                <li class="placeholder">구 선택</li>
		            </ul>
		        </div>
		
		        <!-- 동 목록 -->
		        <div class="setting-base__col setting-base__col--list">
		            <ul id="dongList">
		                <li class="placeholder">동 선택</li>
		            </ul>
		        </div>
			</div>
		</div>

		<div class="jam-card position-wrapper">
			<div class="jam-card-header">
		    	<h3 class="jam-card-title">포지션 선택</h3>
		    </div>
		    <div class="jam-grid">
		        <label><input type="radio" name="position" class="position-radio" value="vocal"> 보컬</label>
		        <label><input type="radio" name="position" class="position-radio" value="piano"> 피아노</label>
		        <label><input type="radio" name="position" class="position-radio" value="guitar"> 기타</label>
		        <label><input type="radio" name="position" class="position-radio" value="bass"> 베이스</label>
		        <label><input type="radio" name="position" class="position-radio" value="drum"> 드럼</label>
		        <label><input type="radio" name="position" class="position-radio" value="midi"> 작곡·미디</label>
		        <label><input type="radio" name="position" class="position-radio" value="lyrics"> 작사</label>
		        <label><input type="radio" name="position" class="position-radio" value="chorus"> 코러스</label>
		        <label><input type="radio" name="position" class="position-radio" value="brass"> 관악기</label>
		        <label><input type="radio" name="position" class="position-radio" value="string"> 현악기</label>
		    </div>
		</div>

		
		<div class="jam-card">
			<div>
				<div class="jam-card-header">
					<h3 class="jam-card-title">제목</h3>
				</div>	
				<input type="text" id="job_title" class="jam-input" style="margin-bottom: 10px;">
			</div>
			<div>
			    <textarea id="job_content" class="summernote"></textarea>
		    </div>
		</div>
		
		<!-- 등록/취소 버튼 -->
		<div class="jam-btn-group">
		    <button type="button" id="write" class="jam-btn register">등록</button>
		    <button type="button" class="jam-btn cancel" onclick="location.href='/jobs/board'">취소</button>
		</div>
	</div>
<script>
$('.summernote').summernote({
	toolbar: [
	    ['fontname', ['fontname']],
	    ['fontsize', ['fontsize']],
	    ['style', ['bold', 'italic', 'underline','strikethrough', 'clear']],
	    ['color', ['forecolor','color']],
	    ['table', ['table']],
	    ['para', ['ul', 'ol', 'paragraph']],
	    ['height', ['height']],
	    ['insert',['link']]
	],
	fontNames: ['Arial', 'Arial Black', 'comic Sans MS', 'Courier New','맑은 고딕','궁서','굴림체','굴림','돋움체','바탕체'],
	fontSizes: ['8','9','10','11','12','14','16','18','20','22','24','28','30','36','50','72'],
	height: 450,
	lang: "ko-KR",
	placeholder : "내용을 작성하세요."
});
</script>
</body>
</html>