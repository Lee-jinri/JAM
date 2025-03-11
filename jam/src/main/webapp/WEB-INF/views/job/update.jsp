<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - JOB</title>
<script src="/resources/include/dist/summernote/summernote-lite.js"></script>
<script src="/resources/include/dist/summernote/summernote-ko-KR.js"></script>
<link rel="stylesheet" href="/resources/include/dist/summernote/summernote-lite.css">
<script src="/resources/include/dist/js/area.js"></script>	
<script>
	$(function(){
		setInitialSelections();
		
		$('input[name="job_category"]').on('change', function() {
	        const selectedValue = $(this).val();
	      	$('.jam-type-select-wrapper').removeClass('focused'); 
	      
	        if (selectedValue === "0") {  
	            // 기업 구인 선택 시
	        	$(".jam-pay-section").stop().slideDown(300);
	        } else {
	            // 멤버 모집 선택 시
	            $(".jam-pay-section").stop().slideUp(300);
	        }
	    });
		
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
		
		$("#update").click(function(){
			let job_no = ${board.job_no};
			let job_title = $("#job_title").val();
			let job_content = $("#job_content").val();
			let pay = ${board.pay} ? ${board.pay} : null;
			let job_category = $('input[name="job_category"]:checked').val() || null;
			let job_status = $("#job_status").is(":checked") ? 1 : 0;
			let pay_category = $("#payNegotiable").is(":checked") ? '3' : $("#pay_category").val();
			let city = $("#city").val() || null;
			let gu = $("#gu").val() || null;
			let dong = $("#dong").val() || null;
			let position = $('input[name="position"]:checked').val() || null;
			
			if(job_category == null){
				alert("구인 종류를 선택하세요.");
				$('.jam-type-select-wrapper').addClass('focused');

				return false;
			}
			
			if(city == null){
				alert("지역을 선택하세요.");
				$(".area-wrapper").addClass('focused');
				return false;
			}
			
			if(job_category == '0' && job_status == 0){
				if(pay_category != '3' && pay == null){
					alert("급여를 입력하세요.");
					$("#pay").focus();
					
					return false;
				}else pay = 0;
			}
			
			if(position == null){
				alert("포지션을 선택하세요.");
				$('.position-wrapper').addClass('focused');
				
				return false;
			}
			
			if(job_title.replace(/\s/g,"") == ""){
				alert("제목을 입력하세요.");
				$("#job_title").focus();
				return false;
			}
			
			if(job_content.replace(/\s/g,"") == ""){
				alert("본문을 입력하세요.");
				$("#job_content").focus();
				return false;
			}
			
			var data = {
					'job_no': job_no,
					'job_title': job_title.trim(),
				    'job_content': job_content.trim(),
					'job_category':job_category,
					'job_status':job_status,
					'pay':pay,
					'pay_category':pay_category,
					'position':position,
					'city':city,
					'gu':gu,
					'dong':dong
			};
			
			fetch('/api/job/board', {
			    method: 'PUT',
			    headers: {
			        'Content-Type': 'application/json'
			    },
			    body: JSON.stringify(data)
			})
			.then(response => {
			    if (!response.ok) {
			    	return response.text().then(text => {
			            throw new Error(text || '수정에 실패했습니다.');
			        });
			    }
			    return response.text();
			})
			.then(body => {
			    alert("수정이 완료되었습니다.");

			    if (body) {
			        $(location).attr('href', '/job/board/' + body);
			    }
			})
			.catch(error => {
			    alert(error.message);
			});
		})
		
		
		
	})
	
	function setInitialSelections(){
		updateJobCategory();
		updateSelectedPosition();
		initSelectedArea();
	}
	
	function updateJobCategory(){
		let job_category = ${board.job_category};	
		$("input[name='job_category'][value='" + job_category + "']").prop("checked", true);
		
		if(job_category == 0){ 
			$(".jam-pay-section").css("display", "flex");
			
			let pay_category = ${board.pay_category};
			
			if (pay_category == 3) {
		        // "협의 후 결정" 체크박스 선택 & 급여 입력창 비활성화
		        $("#payNegotiable").prop("checked", true);
		        $("#pay").prop("disabled", true).val("");
		    } else {
		        // select에서 pay_category 값 선택
		        $("#pay_category").val(pay_category);
		    }
			
			$("#pay").val(${board.pay});
		}
	}
	
	function updateSelectedPosition(){
		let position = "${board.position}";
		
		let $selectedPosition = $("input[name='position'][value='" + position + "']");
		$selectedPosition.prop("checked", true);

		$selectedPosition.parent().css({
		    background: "#003366",
		    color: "#fff",
		    borderColor: "#003366"
		});
		    
		
	}
	
	function initSelectedArea(){
		let city = "${board.city}";
		let gu = ("${board.gu}" !== "null" && "${board.gu}" !== "") ? "${board.gu}" : "";
		let dong = ("${board.dong}" !== "null" && "${board.dong}" !== "") ? "${board.dong}" : "";

		$("#city").val(city);
	    $("#gu").val(gu);
	    $("#dong").val(dong);
	    
	    let selectedAreaText = city;
	    if (gu) selectedAreaText += ' > ' + (gu === 'all' ? '전체' : gu);
	    if (dong) selectedAreaText += ' > ' + (dong === 'all' ? '전체' : dong);

	    console.log(selectedAreaText);
	    // 선택 지역 표시 업데이트
	    $("#selectedArea").html(selectedAreaText);

	    // x 버튼 추가 (기존 있으면 삭제 후 새로 추가)
	    $(".area-remove-btn").remove();
	    const removeBtn = $("<button>")
	        .addClass("area-remove-btn")
	        .text("x")
	        .on("click", function() {
	            removeArea();  
	        });

	    $("#selectedAreaWrapper").append(removeBtn);
	}
</script>
</head>
<body class="wrap">
	<div class="rem-30 my-top-15 my-bottom-15">
		<div class="jam-type-select-wrapper">
			<div class="jam-type-select">
			    <label class="jam-radio-label">
			        <input type="radio" name="job_category" value="0">
			        <span class="jam-radio-text">기업 구인</span>
			    </label>
			    <label class="jam-radio-label">
			        <input type="radio" name="job_category" value="1">
			        <span class="jam-radio-text">멤버 모집</span>
			    </label>
			</div>	
			<div class="">
				<label><input id="job_status" type="checkbox">구인 완료 시 체크하세요.</label>
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
				<input type="text" id="job_title" class="jam-input" style="margin-bottom: 10px;" value="${board.job_title }">
			</div>
			<div>
			    <textarea id="job_content" class="summernote" >${board.job_content }</textarea>
		    </div>
		</div>
		
		<!-- 등록/취소 버튼 -->
		<div class="jam-btn-group">
		    <button type="button" id="update" class="jam-btn register">수정</button>
		    <button type="button" class="jam-btn cancel" onclick="location.href='/job/boards'">취소</button>
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
		    ['insert',['picture','link','video']]
		],
		fontNames: ['Arial', 'Arial Black', 'Comic Sans MS', 'Courier New','맑은 고딕','궁서','굴림체','굴림','돋움체','바탕체'],
		fontSizes: ['8','9','10','11','12','14','16','18','20','22','24','28','30','36','50','72'],
		height: 450,
		lang: "ko-KR",
		placeholder : "내용을 작성하세요.",
		callbacks : {
	    	onImageUpload : function(files, editor, welEditable){
	    		for(var i = files.length - 1; i >= 0; i--){
	    			uploadImageFile(files[i],this);
	    		}
	    	}
	    }
	});
	function uploadImageFile(file, el) {
		data = new FormData();
		data.append("file", file);
		$.ajax({                                                              
			data : data,
			type : "POST",
			url : 'uploadImageFile',
			contentType : false,
			enctype : 'multipart/form-data',
			processData : false,
			success : function(data) {                                         
				$(el).summernote('editor.insertImage',data.url);
			}
		});
	}
	</script>
</body>
</html>