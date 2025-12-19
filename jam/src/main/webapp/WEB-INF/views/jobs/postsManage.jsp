<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>

<script src="/resources/include/dist/js/area.js"></script>
<link rel="stylesheet" href="/resources/include/dist/css/job-common.css">
<style>
:root{
	--card:#fff; --text:#1f2937; --muted:#6b7280; --line:#e5e7eb;
	--brand:#0F2D4A; --brand-2:#163B63; --blue:#00a1ef; --pill:#f1f5f9; --radius:12px;
	--daily:#ff501b; --weekly:#00b0a6; --monthly:#00a1ef;
}
*{box-sizing:border-box}
#content{max-width:1200px;}
.container{max-width:1200px;margin:56px auto;padding:0 20px}
.toolbar{display:flex;gap:10px;align-items:center;flex-wrap:wrap;margin:10px auto;max-width:600px;}
.chip{display:inline-flex;align-items:center;gap:8px;background:#fff;border:1px solid var(--line);border-radius:999px;padding:8px 12px;font-weight:700;font-size:15px;}
.chip svg{width:16px;height:16px}
.board{background:var(--card);border:1px solid var(--line);border-radius:var(--radius);overflow:hidden}
.job-board-header.row{
	display:grid;
	grid-template-columns:1.6fr 1fr 1fr 1fr 1fr 1fr 110px;
	padding:14px 16px;
	font-size:14px;color:var(--muted);font-weight:800;
	border-bottom:1px solid var(--line);
}
.row{
	display:grid;align-items:center;
	grid-template-columns:1.6fr 1fr 1fr 1fr 1fr 1fr 110px;
	padding:16px;border-bottom:1px solid var(--line);
}
.row:last-child{border-bottom:0}
.cell.title{display:flex;gap:10px;align-items:center}
.title__text{font-weight:800;font-size:16px;}
.cell.area,.cell.position{color:#374151;font-size:15px;}
.cell.date{color:var(--blue);font-weight:800}
.job-actions{cursor:pointer;}
.pay{display:flex;gap:8px;align-items:center;}
.badge{display:inline-flex;align-items:center;padding:2px 8px;border-radius:999px;font-size:12px;background:var(--pill);border:1px solid var(--line);font-weight:700}
.badge.daily{color:var(--daily);border-color:var(--daily)}
.badge.weekly{color:var(--weekly);border-color:var(--weekly)}
.badge.monthly{color:var(--monthly);border-color:var(--monthly)}
.badge.negotiate{color:#6b7280}
.empty-box{
	display:flex;justify-content:center;align-items:center;
	padding:40px 0;border:1px dashed var(--line);
	border-radius:var(--radius);background:#fafafa;margin-top:16px;
}
.empty-text{color:var(--muted);font-size:14px}
.closePostings span,.closePostings i{color:#848484;}
.closePostings .pay_category{border:1px solid #848484;border-color:#848484;}
.cell.actions{position:relative;display:inline-flex;justify-content:center;align-items:center;}
.cell.actions .action-btn{display:none;}
.cell.actions .more-btn{
	display:inline-flex;align-items:center;justify-content:center;
	width:28px;height:28px;border:none;border-radius:8px;background:#fff;cursor:pointer;font-size:16px;line-height:1;
}
.cell.actions .dropdown{
	position:absolute;top:calc(100% + 6px);min-width:90px;margin:0;padding:6px;
	background:#fff;border:1px solid var(--line);border-radius:10px;box-shadow:0 6px 20px rgba(0,0,0,.08);
	list-style:none;z-index:50;
}
.cell.actions .dropdown[hidden]{display:none;}
.cell.actions .dropdown li{margin:0;padding:0;font-size:14px;}
.cell.actions .dropdown button{
	width:100%;text-align:center;padding:8px 10px;border:0;background:transparent;border-radius:8px;font-size:14px;cursor:pointer;
}
.cell.actions .dropdown button:hover{background:#f3f4f6;}
.apps-container{background:#fafbff;border-bottom:1px solid var(--line);}
.apps-grid{display:grid;grid-template-columns:var(--cols);align-items:start;}
.apps-cell{grid-column:1 / 7;}
.apps-inner{padding:14px 0;}
.apps-list{display:flex;flex-direction:column;gap:12px;}
.app-item{
	display:flex;justify-content:space-between;align-items:center;
	padding:12px 16px;background:#fff;border:1px solid var(--line);
	border-radius:10px;transition:background .2s ease;cursor:pointer;margin:0 60px;
}
.app-item:hover{background:#f3f6ff;}
.app-left{display:flex;flex-direction:column;gap:4px;}
.app-title{font-weight:700;font-size:15px;color:var(--text);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;}
.app-right{display:flex;flex-direction:column;align-items:flex-end;gap:4px;}
.app-date{color:var(--muted);font-size:12px;}
.action-btn{border:1px solid var(--line);border-radius:8px;padding:4px 9px;font-size:13px;font-weight:600;cursor:pointer;transition:all .15s ease;}
.action-btn.edit{background:#ffffff;border-color:#d0d0d0;}
.action-btn.edit:hover{background:#e0e7ff;}
.action-btn.delete:hover{background:#fee2e2;}
.action-btn + .action-btn{margin-left:6px;}
.pagination{display:flex;gap:6px;justify-content:center;margin:26px 0 8px}
.pagination a{padding:8px 12px;border-radius:8px;border:1px solid var(--line);color:var(--text);text-decoration:none}
.pagination a.active{border-color:var(--brand);color:var(--brand);font-weight:900}
#boardList[data-mode="user"] .chip{border:2px solid #ebebeb;}
#boardList[data-mode="user"] .app-item{border:2px solid #e5e7eb;}
#boardList[data-mode="user"] .job-board-header.row{
	grid-template-columns:1.6fr 1fr 1fr 1fr 1fr 110px;
}
#boardList[data-mode="user"] .row{
	grid-template-columns:1.6fr 1fr 1fr 1fr 1fr 110px;
}
#boardList[data-mode="user"] .payDiv{display:none;}

#boardList .row{
	transition: background-color .15s ease;
}
#boardList .row:hover{
	background: #f8fafc;
}

#boardList .job-actions:hover .apply_count{
	filter: saturate(1.2);
}

.closePostings .title{
	text-decoration: line-through;
	color: #9ca3af;
}
.closePostings .area,
.closePostings .position,
.closePostings .created_at,
.closePostings .apply_count{
	color: #9ca3af;
}

@media (max-width: 860px){
	.search{min-width:unset;flex:1}
	.job-board-header{display:none}
	#boardList .row{
		background: #fff;
		border: 1px solid #e5e7eb;
		border-radius: 12px;
		padding: 14px;
		box-shadow: 0 2px 10px rgba(0,0,0,.03);
	}
	#boardList .row + .row{
		margin-top: 12px;
	}
	#boardList .title{
		font-size: 16px;
		font-weight: 800;
		color: #111827;
	}
	#boardList .area,
	#boardList .position,
	#boardList .created_at{
		font-size: 14px;
		color: #4b5563;
	}
	#boardList .job-actions{
		justify-self: end;
	}
	#boardList .payDiv{
		margin: 6px 0;
	}
}

@media (max-width: 860px){
	#boardList[data-mode="user"] .row{
		background: #fff;
		border: 1px solid #e5e7eb;
		border-radius: 12px;
		padding: 14px;
		box-shadow: 0 2px 10px rgba(0,0,0,.03);
	}
}

.cell.actions .more-btn{
	transition: background-color .12s ease, box-shadow .12s ease;
}
.cell.actions .more-btn:hover{
	background: #f3f4f6;
	box-shadow: 0 0 0 2px #e5e7eb inset;
}

</style>

<script>
let boardState = {
	  pageNum: 1,
	  search: "all",
	  category: "0", 
	  city: "",
	  gu: "",
	  dong: "",
	  positions: []
	};

const positionMap = {
	    vocal: '보컬',
	    piano: '피아노',
	    guitar: '기타',
	    bass: '베이스',
	    drum: '드럼',
	    midi: '작곡·미디',
	    lyrics: '작사',
	    chorus: '코러스',
	    brass: '관악기',
	    string: '현악기'
	};

const BOARD_CONFIG = {
	company: {
		headers: ['공고제목/기업명','근무지','포지션','급여','등록일','지원건수',''],
		showPay: true
	},
	user: {
		headers: ['제목','장소','포지션','등록일','지원현황',''],
		showPay: false
	}
};

$(function(){
	getPostings();
	
	$(document).on("click", ".postLink", function (e) {
		e.preventDefault();
		var location = $(this).attr("data-location");
		if (location) {
			window.location.href = location;
		}
	});
	
	$(document).on("click", ".job-actions", function() {
		
		const $actions = $(this);
		const hasCandidate = $actions.data("hasCandidate");
		const $rowLi = $actions.closest("li"); 
		
		if (!hasCandidate) {
			alert("지원자가 없습니다.");
			return;
		}
		
		if ($rowLi.hasClass("apps-open")) {
			hideApps($rowLi);
			return;
		}
		
		$("#boardList li.apps-open").not($rowLi).each(function(){ 
			hideApps($(this));
		});
		
		if (!$rowLi.next().hasClass("apps-container")){
			$rowLi.after(
				'<li class="apps-container">' +
					'<div class="apps-grid">' +
						'<div class="apps-cell">' +
							'<div class="apps-inner"></div>' +
						'</div>' +
					'</div>' +
				'</li>'
			);
		}

		const loaded = $rowLi.data("apps-loaded") === true;
		const postId = $(this).attr("data-post-id");
		
		if (loaded) {
			expandApps($rowLi);
		} else {
			getCandidates(postId, $rowLi);  
		}
	});
	
	$(document).on("click", ".app-item", function() {
		const appId = $(this).attr("data-app-id");
		if(!appId){
			alert("오류가 발생했습니다. 잠시 후 다시 시도하세요.");
			return;
		}
		const baseUrl = '/jobs/applications/';
		const url = baseUrl + appId;
		
		const option = "width=500,height=695,top=100,left=100,resizable=no,scrollbars=yes";
		window.open(url, "showApplicationPopup", option);
	})
	
	$("#positionBtn").click(function() {
		const $icon = $(this).find("i");
		const $pos = $("#positionContainer");
		const $area = $("#areaContainer");

		if ($pos.is(":visible")) {
			$pos.slideUp(300);
			$icon.removeClass("fa-caret-up").addClass("fa-caret-down");
		} else {
			$area.slideUp(300); 
			$pos.slideDown(300);
			$("#areaBtn i").removeClass("fa-caret-up").addClass("fa-caret-down");
			$icon.removeClass("fa-caret-down").addClass("fa-caret-up");
		}
	});

	$("#areaBtn").click(function() {
		const $icon = $(this).find("i");
		const $pos = $("#positionContainer");
		const $area = $("#areaContainer");

		if ($area.is(":visible")) {
			$area.slideUp(300);
			$icon.removeClass("fa-caret-up").addClass("fa-caret-down");
		} else {
			$pos.slideUp(300);
			$area.slideDown(300);
			$("#positionBtn i").removeClass("fa-caret-up").addClass("fa-caret-down");
			$icon.removeClass("fa-caret-down").addClass("fa-caret-up");
		}
	});
	
	$(document).on("change", ".position-checkbox", function() {
	    updateSelectedPosition();
	});
	
	$("#search-btn").click(function(){
	    boardState.category = $("#typeSwitch").is(":checked") ? "1" : "0";
	    boardState.positions= getSelectedPositions(); 

	    boardState.pageNum = 1;

		boardState.city = $("#city").val();
		boardState.gu = $("#gu").val();
		boardState.dong = $("#dong").val();
		
		getPostings();
	});
	
	// : 버튼
	$(document).on('click', '.cell.actions .more-btn', function (e) {
		e.stopPropagation();
		const $btn = $(this);
		const $menu = $btn.siblings('.dropdown');

		$('.cell.actions .dropdown').attr('hidden', true);
		$('.cell.actions .more-btn[aria-expanded="true"]').attr('aria-expanded', 'false');

		const willOpen = $menu.is('[hidden]');
		$menu.attr('hidden', !willOpen);
		$btn.attr('aria-expanded', willOpen ? 'true' : 'false');

		if (willOpen) {
			setTimeout(() => $menu.find('button[role="menuitem"]').first().trigger('focus'), 0);
		}
	});

	// 바깥 클릭하면 닫기
	$(document).on('click', function () {
		$('.cell.actions .dropdown').attr('hidden', true);
		$('.cell.actions .more-btn[aria-expanded="true"]').attr('aria-expanded', 'false');
	});
	$(document).on('keydown', function (e) {
		if (e.key === 'Escape') {
			$('.cell.actions .dropdown').attr('hidden', true);
			$('.cell.actions .more-btn[aria-expanded="true"]').attr('aria-expanded', 'false').trigger('focus');
		}
	});
	
	// 마감
	$(document).on("click", ".action-close", function(){
		const $btn = $(this);
		const $div = $btn.closest("div"); 
		const postId = $div.attr("data-post-id");
		
		fetch('/api/jobs/post/' + encodeURIComponent(postId), {
			method: 'PATCH'
		})
		.then(res => {
			if (res.ok) {
				alert("공고가 마감되었습니다.");
				location.reload();
			}
	    	return res.json().then(err => {
	            throw err;
	        });
		})
		.catch(err => {
			if (handleApiError(err, "/jobs/board")) return;
	    	alert(err.detail || '오류가 발생했습니다. 잠시 후 다시 시도하세요.');
		});
	})
	
	// 수정
	$(document).on("click", ".action-edit", function() {
		const $btn = $(this);
		const $div = $btn.closest("div"); 
		const postId = $div.attr("data-post-id");
		
		if(!postId){
			alert("오류가 발생했습니다. 잠시 후 다시 시도하세요.");
			return;
		}
		
		$(location).attr("href", "/jobs/post/update/" + postId);
	})

	// 삭제
	$(document).on("click", ".action-delete", function() {
		const $btn = $(this);
		const $div = $btn.closest("div"); 
		const postId = $div.attr("data-post-id");

		if(!postId){
			alert("오류가 발생했습니다. 잠시 후 다시 시도하세요.");
			return;
		}
		
		fetch('/api/jobs/post/' + encodeURIComponent(postId), {
			method: 'DELETE'
		})
		.then(res => {
			if (res.ok) {
				alert("삭제가 완료되었습니다.");
				location.reload();
			}

	    	return res.json().then(err => {
	            throw err;
	        });
		})
		.catch(err => {
			if (handleApiError(err, "/jobs/board")) return;
		    alert(err.detail || '오류가 발생했습니다. 잠시 후 다시 시도하세요.');
			location.reload();
		});
	})
})

function getPostings(){
	let queryString = new URLSearchParams(boardState).toString();
	let url = "/api/jobs/my/posts?" + queryString;
	
	fetch(url)
	.then(res =>{
		if (res.ok) {
			return res.json();
		}
    	return res.json().then(err => {
            throw err;
        });
	}).then(data =>{
		if(!data) return;
		
		const mode = "${mode}" || "company";
		renderPostings(data, mode);
	}).catch(err =>{
		if (handleApiError(err, "/jobs/board")) return;
	    alert(err.detail || '일시적인 오류가 발생했습니다. 잠시 후 다시 시도하세요.');
	})
}

function renderPostings(data, mode){
	const cfg = BOARD_CONFIG[mode] || BOARD_CONFIG.company;

	const $jobHeader = $("#jobListHeader");
	const $boardList = $("#boardList");
	
	$jobHeader.empty();
	$jobHeader.nextAll().remove();
	
	if(!data.postings || data.postings.length === 0){
		$boardList.append(
				'<div class="empty-box">' + 
					'<span class="empty-text">등록된 게시글이 없습니다.</span>' + 
				'</div>'
			);
		return;
	}
	
	$jobHeader.append(
		'<div class="job-board-header row pd-2rem text-alignC items-center">' +
			cfg.headers.map(header => '<span>' + header + '</span>').join('') +
		'</div>'
	);
	
	$boardList.attr('data-mode', mode);
	
	if(mode == 'user') renderUserPostings(data);
	else renderCompanyPostings(data);
	
	loadPagination(data.pageMaker);
}

function getCandidates(postId, $rowLi){
	const qs = new URLSearchParams({
		  post_id: postId,
		  pageNum: 1
	});
		
	fetch('/api/jobs/candidates?' + qs.toString())
	.then(res => {
		if(res.ok) return res.json();
    	return res.json().then(err => {
            throw err;
        });
	})
	.then(data =>{
		renderApplications(data, $rowLi);
	})
	.catch(err => {
	    alert(err.detail || '일시적인 오류가 발생했습니다. 잠시 후 다시 시도하세요.');
	});
}

function renderUserPostings(data){
	const $template = $("#boardTemplate");
	const $boardList = $("#boardList");
	
	data.postings.forEach(posting => {
		let $clone = $template.clone().removeAttr("id").show();
		
		let $boardDiv = $clone.find(".boardDiv");
		
		if(posting.status == 1) {
			$boardDiv.addClass("closePostings");
			$clone.find('.actions').empty();
			$clone.find('.actions').append('<span>[ 마감 ]</span>')
		}else{
			$clone.find('.actions').attr("data-post-id", posting.post_id);
		}
		
		let areaText = posting.city;
		if(posting.gu != null) areaText += "\u00A0" + posting.gu;
		if(posting.dong != null) areaText += "\u00A0" + posting.dong;
		
		$clone.find(".area").text(areaText);
		$clone.find(".position").text(positionMap[posting.position]);
		$clone.find(".title").text(posting.title);
		$clone.find(".postLink").attr("data-location", "/jobs/post/" + posting.post_id);
		$clone.find(".payDiv").hide().empty();
		
		let date = timeAgo(posting.created_at);
		$clone.find(".created_at").text(timeAgo(posting.created_at));
		
		$clone.find(".apply_count").text('지원 ' +posting.applyCount + '건');
		
		if(posting.applyCount > 0) {
			$clone.find('.job-actions')
				.attr("data-has-candidate", true)
				.attr("data-post-id", posting.post_id);
		}else{
			$clone.find('.job-actions').attr("data-has-candidate", false);
		}
		
		$boardList.append($clone);
	});
}

function renderCompanyPostings(data){
	const $template = $("#boardTemplate");
	const $boardList = $("#boardList");
	
	data.postings.forEach(posting => {
		let $clone = $template.clone().removeAttr("id").show();
		
		let $boardDiv = $clone.find(".boardDiv");
		
		if(posting.status == 1) {
			$boardDiv.addClass("closePostings");
			$clone.find('.actions').empty();
			$clone.find('.actions').append('<span>[ 마감 ]</span>')
		}else{
			$clone.find('.actions').attr("data-post-id", posting.post_id);
		}
		
		let areaText = posting.city;
		if(posting.gu != null) areaText += "\u00A0" + posting.gu;
		if(posting.dong != null) areaText += "\u00A0" + posting.dong;
		
		$clone.find(".area").text(areaText);
		$clone.find(".position").text(positionMap[posting.position] || posting.position || '');
		$clone.find(".title").text(posting.title);
		$clone.find(".postLink").attr("data-location", "/jobs/post/" + posting.post_id);
		
		const payTexts = ["건별 ", "주급 ", "월급 ", "협의 후 결정"];
		let payText = payTexts[posting.pay_category] || "협의 후 결정";
		
		let $payDiv = $clone.find(".payDiv");
		$payDiv.find(".pay").text(posting.pay_category < 3 ? formatNumberKo(posting.pay) + "원" : "협의 후 결정");
		
		let $payCategory = $payDiv.find(".pay_category");
		if(posting.pay_category < 3){
			$payCategory.text(payText)
		} else{
			$payCategory.css("display","none");
			$payDiv.css("justifyContent","center");	
		}
		
		if(posting.pay_category == 0) $payCategory.addClass("daily");
		else if(posting.pay_category == 1) $payCategory.addClass("weekly");
		else if(posting.pay_category == 2) $payCategory.addClass("monthly");

		let date = timeAgo(posting.created_at);
		$clone.find(".created_at").text(date);
		
		$clone.find(".apply_count").text('지원 ' + posting.applyCount + '건');
		
		if(posting.applyCount > 0) {
			$clone.find('.job-actions')
				.attr("data-has-candidate", true)
				.attr("data-post-id", posting.post_id);
		}else{
			$clone.find('.job-actions').attr("data-has-candidate", false);
		}
		
		$boardList.append($clone);
	});
}

function renderApplications(data, $rowLi){

	const $inner = $rowLi.next(".apps-container").find(".apps-inner");
	$inner.empty();
	
	const apps = (data && data.apps) ? data.apps : [];
	if (!apps.length) {
		$inner.append('<div class="apps-empty">지원자가 없습니다.</div>');
	} else {
		const $wrap = $('<div class="apps-list"></div>');
		apps.forEach(app => {
			const created = app.created_at ? timeAgo(app.created_at) : '';
			$wrap.append(
				'<div class="app-item" data-app-id="' + app.application_id + '">' +
					'<div class="app-left">' +
						'<div class="app-title">'  + app.title + '</div>' +
					'</div>' +
					'<div class="app-right">' +
						'<div class="app-date">' + created + '</div>' +
					'</div>' +
				'</div>'
			);
		});
		$inner.append($wrap);
	}
	
	$rowLi.data("apps-loaded", true);
	expandApps($rowLi);
}

function expandApps($rowLi){
	const $container = $rowLi.next(".apps-container");
	
	$rowLi.addClass("apps-open");
	$rowLi.find(".job-actions i").removeClass("fa-play fa-caret-right").addClass("fa-caret-down");
	$container.slideDown(180);
}
	
function hideApps($rowLi){
	const $container = $rowLi.next(".apps-container");
	
	$rowLi.removeClass("apps-open");
	$rowLi.find(".job-actions i").removeClass("fa-caret-down").addClass("fa-caret-right");
	$container.slideUp(160);
}

function getSelectedPositions() {
    const positions = [];
    $(".position-checkbox:checked").each(function() {
        positions.push($(this).val());
    });
    return positions.join(","); 
}


// 선택한 포지션 업데이트 
function updateSelectedPosition() {
  const selectedPosition = [];

  $(".position-grid input[type='checkbox']:checked").each(function() {
  	selectedPosition.push($(this).parent().text().trim());
  });

  displaySelectedPosition(selectedPosition);
}

// 선택한 포지션 표시 
function displaySelectedPosition(Position) {
  const $selectedPosition = $("#selectedPosition");
  $selectedPosition.empty();  

  $selectedPosition.show();  

  Position.forEach(pos => {
      const span = $("<span>").addClass("selected-position-span").text(pos);
      
      const removeBtn = $("<button>")
      .addClass("remove-btn")
      .html('<i class="fa-solid fa-x"></i>')
      .on("click", function() {
          removePosition(pos);
      });

      span.append(removeBtn);
      $selectedPosition.append(span);
  });
}

//포지션 제거 함수 (x버튼 클릭)
function removePosition(position) {
  $(".position-grid input[type='checkbox']").each(function() {
      if ($(this).parent().text().trim() === position) {
          $(this).prop("checked", false); 
      }
  });

  updateSelectedPosition(); 
}

//모든 포지션 선택 취소
function removeAllPosition(){
	$(".position-grid input[type='checkbox']").each(function() {
      $(this).prop("checked", false);  
  });

  updateSelectedPosition();
}

function timeAgo(dateString) {
	let now = new Date();
	let past = new Date(dateString);
	let diff = Math.floor((now - past) / 1000); 

	if (diff < 10) return '방금 전';
	if (diff < 60) return diff + '초 전';
	if (diff < 3600) return Math.floor(diff / 60) + '분 전';
	if (diff < 86400) return Math.floor(diff / 3600) + '시간 전';
	if (diff < 172800) return '어제';
	if (diff < 2592000) return Math.floor(diff / 86400) + '일 전';
	
    let months = Math.floor(diff / 2592000); 
    if (months < 12) return months + '개월 전';
    
	let dateStr = past.toLocaleDateString();
	return dateStr.replace(/\.$/, '');
}

function formatNumberKo(pay) {
	const num = Number(String(pay).replace(/[^\d]/g, ''));
	return num ? num.toLocaleString('ko-KR') : '';
}


function loadPagination(pageMaker) {
    const $pagination = $("#pagination");
    $pagination.empty(); 
    
    for (let num = pageMaker.startPage; num <= pageMaker.endPage; num++) {
        $pagination.append(
            '<li class="paginate_button">' +
                '<a href="#" data-page="' + num + '" class="font-weight-bold ' + (pageMaker.cvo.pageNum === num ? 'selected_btn' : 'default_btn') + '">' + num + '</a>' +
            '</li>'
        );
    }

    $("#pagination a").click(function (e) {
        e.preventDefault();
        let pageNum = $(this).data("page");
        
        boardState.pageNum = pageNum;
        getPostings();
    });
}
</script>
</head>
<body>
	<div class="container">
		
		<input type="hidden" id="city">
		<input type="hidden" id="gu">
		<input type="hidden" id="dong">
		<div class="toolbar">
			<button type="button" id="areaBtn" class="chip">
				<svg viewBox="0 0 24 24" aria-hidden="true">
					<path d="M12 2a7 7 0 0 0-7 7c0 5.25 7 13 7 13s7-7.75 7-13a7 7 0 0 0-7-7zm0 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5z"/>
				</svg>
				지역
			</button>
			
			<button type="button" id="positionBtn" class="chip">
				<svg viewBox="0 0 24 24" aria-hidden="true">
					<path d="M5 3h14v2H5V3zm2 4h10v2H7V7zm-2 4h14v2H5v-2zm2 4h10v2H7v-2z"/>
				</svg>
				포지션
			</button>
		</div>
		<div style="margin-bottom: 30px;">
			<div id="positionContainer" class="setting-filter" style="display: none;">
			    <div class="position-grid">
			        <label><input type="checkbox" class="position-checkbox" value="vocal"> 보컬</label>
			        <label><input type="checkbox" class="position-checkbox" value="piano"> 피아노</label>
			        <label><input type="checkbox" class="position-checkbox" value="guitar"> 기타</label>
			        <label><input type="checkbox" class="position-checkbox" value="bass"> 베이스</label>
			        <label><input type="checkbox" class="position-checkbox" value="drum"> 드럼</label>
			        <label><input type="checkbox" class="position-checkbox" value="midi"> 작곡·미디</label>
			        <label><input type="checkbox" class="position-checkbox" value="lyrics"> 작사</label>
			        <label><input type="checkbox" class="position-checkbox" value="chorus"> 코러스</label>
			        <label><input type="checkbox" class="position-checkbox" value="brass"> 관악기</label>
			        <label><input type="checkbox" class="position-checkbox" value="string"> 현악기</label>
			    </div>
			</div>
			<div id="areaContainer" class="setting-filter">
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
			<div class="selected-wrapper">
			    <div id="selectedAreaWrapper" class="selected-location inline">
			        <span id="selectedArea" class="selected-location-span"></span>
			    </div>
			    <div id="selectedPosition" class="selected-position inline">
			    </div>
			    <div class="searchDiv">
			    	<button id="search-btn" class="searchBtn">검색</button>
			    </div>
			</div>
		</div>	
		<div>
			<ul style="display:none">
				<li id="boardTemplate" class="border-bottom">
					<div class="boardDiv pd-2rem job-list-row text-alignC items-center row">
						<span class="font-size-5 title postLink"  style="cursor: pointer;"></span>
				        
						<span class="area"></span>
						<span class="position"></span>
						
				        <div class="payDiv">
				        	<span class="pay"></span>
				        	<span class="pay_category"></span>
				        </div>
				        
				        <span class="created_at"></span>
				        
				        <div class="job-actions">
							<span class="apply_count"></span>
							<i class="fas fa-caret-right"></i>
							
						</div>
						<div class="cell actions">
							<button class="more-btn" type="button" aria-haspopup="true" aria-expanded="false" aria-controls="job-actions-menu" title="더 보기">⋮</button>
							<ul class="dropdown" id="job-actions-menu" role="menu" hidden>
								<li role="none"><button role="menuitem" type="button" class="action-edit">수정</button></li>
								<li role="none"><button role="menuitem" type="button" class="action-delete">삭제</button></li>
								<li role="none"><button role="menuitem" type="button" class="action-close">마감</button></li>
							</ul>
						</div>
					</div>
				</li>
			</ul>
			
			<ul id="boardList">
				<li id="jobListHeader" class="border-top border-bottom">
				</li>
			</ul>
		</div>
		
		<div>
			<div class="text-center">
			    <ul id="pagination" class="pagination pagination_border"></ul>
			</div>
		</div>
	</div>
</body>
</html>