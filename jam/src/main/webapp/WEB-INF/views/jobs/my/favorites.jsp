<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<link rel="stylesheet" href="/resources/include/dist/css/job-common.css">
<script src="/resources/include/dist/js/favorite.js"></script>
<style>
	:root{
		--bg:#f7f8fb; --card:#fff; --text:#1f2937; --muted:#6b7280; --line:#e5e7eb;
		--brand:#0F2D4A; --brand-2:#163B63; --blue:#00a1ef; --pill:#f1f5f9; --radius:12px;
		--ok:#10b981; --warn:#f59e0b; --bad:#ef4444; --info:#3b82f6;
	}
	*{box-sizing:border-box}
	html,body{margin:0;padding:0;background:var(--bg);color:var(--text);
		font-family:system-ui,-apple-system,Segoe UI,Roboto,Apple SD Gothic Neo,Noto Sans KR,sans-serif}
	a{color:inherit;text-decoration:none}
	.container{max-width:1100px;margin:24px auto;padding:0 16px}
	.a-header{display:flex;flex-wrap:wrap;align-items:center;gap:12px;margin-bottom:16px}
	.h-title{font-size:22px;font-weight:700}
	.summary{display:flex;gap:8px;flex-wrap:wrap}
	.pill{background:var(--pill);border:1px solid var(--line);border-radius:999px;padding:6px 10px;font-size:13px}
	.card{background:var(--card);border:1px solid var(--line);border-radius:var(--radius);padding:16px}
	
	.switch-container {display: flex;align-items: center;gap: 10px;	margin: 20px 0;font-weight: bold;font-size: 16px;}
	.switch {position: relative;display: inline-block;width: 50px;height: 26px;}
	.switch input {opacity: 0;width: 0;height: 0;}
	.slider {position: absolute;cursor: pointer;top: 0;left: 0;right: 0;bottom: 0;background-color: #ddd;transition: 0.4s;border-radius: 34px;}
	.slider:before {position: absolute;content: "";height: 20px;width: 20px;left: 3px;bottom: 3px;background-color: white;transition: 0.4s;border-radius: 50%;}
	.slider {background-color: #003366;}
	input:checked+.slider {background-color: #ffdd77;}
	input:checked+.slider:before {transform: translateX(24px);}

	.toolbar{display:flex;flex-wrap:wrap;gap:8px;margin-bottom:12px}
	.select, .input{border:1px solid var(--line);border-radius:8px;padding:8px 10px;background:#fff}
	.btn{border:1px solid var(--line);background:#fff;border-radius:8px;padding:8px 12px;cursor:pointer}
	.btn.primary{background:var(--brand);color:#fff;border-color:var(--brand)}
	.btn.ghost{background:#fff}
	.status-close{background:#ededed}

	.table{width:100%;border-collapse:separate;border-spacing:0}
	.table th, .table td{padding:12px;border-bottom:1px solid var(--line);font-size:14px;vertical-align:middle;text-align:center;}
	.table th{text-align:center;color:var(--muted);font-weight:600}
	.badge{border-radius:999px;padding:4px 8px;font-size:12px;border:1px solid var(--line);display:inline-flex;align-items:center;gap:6px}
	.badge.ok{background:#ecfdf5;border-color:#d1fae5;color:#065f46}
	.badge.warn{background:#fffbeb;border-color:#fde68a;color:#92400e}
	.badge.bad{background:#fef2f2;border-color:#fecaca;color:#991b1b}
	.badge.info{background:#eff6ff;border-color:#bfdbfe;color:#1e40af}
	.icon{width:64px;height:20px;border-radius:9px;display:inline-block;color:#fff; font-size:13px;text-align: center;align-items: center;font-weight: 500;box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
    transition: transform 0.2s ease, box-shadow 0.2s ease;}
	.icon.company{background:var(--blue)}
	.icon.user{background:var(--brand-2)}
	.tag{font-size:12px;color:var(--muted)}

	.row-actions{display:flex;gap:6px}
	.ellipsis{max-width:230px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;text-align:start;}


	.table .pay{text-align: left;}
	.pay_category {background-color: #fff;padding: 2px 8px;border-radius: 28px;font-size: .75rem;line-height: 1rem;align-items: center;display: inline-flex;font-weight: 400;}  
	.daily {border: 1px solid #ff501b; border-color: #ff501b; color: #ff501b;}  
	.weekly {border: 1px solid #00b0a6;border-color: #00b0a6;color: #00b0a6;}  
	.monthly {border: 1px solid #00a1ef;border-color: #00a1ef;color: #00a1ef;}

	.empty{padding:32px;text-align:center;color:var(--muted);border:1px dashed var(--line);border-radius:12px;background:#fafafa}
	.pagination{display:flex;gap:6px}
	.page{padding:6px 10px;border:1px solid var(--line);border-radius:8px;background:#fff;cursor:pointer}
	.page.active{background:var(--brand);color:#fff;border-color:var(--brand)}

	/* 모바일 카드형 */
	@media (max-width: 720px){
		.table thead{display:none}
		.table, .table tbody, .table tr, .table td{display:block;width:100%}
		.table tr{border-bottom:1px solid var(--line);padding:10px 0}
		.table td{border:none;padding:6px 0}
		.ellipsis{max-width:100%}
	}
</style>
<script>
let state = {
		  pageNum: 1,
		  keyword: null,
		  period: null,
		  category: 0,
		  status: null
};

$(function(){
	getFavorites();
	
	$('#typeSwitch').on('change', function() {
	    let category = $(this).is(':checked') ? "1" : "0";
	   
		state = {
				  pageNum: 1,
				  keyword: null,
				  period: null,
				  category: category
				};
		
		getFavorites();
	});
	
	document.getElementById("f-status").addEventListener("change", function(e) {
		const selected = e.target.value;
		state.status = selected;
		
		getFavorites();
	});
	
	document.getElementById("f-period").addEventListener("change", function(e) {
		const selected = e.target.value;
		state.period = selected;
		
		getFavorites();
	});
	
	document.getElementById("btn-search").addEventListener("click", function(e) {
		doSearch();
	});
	
	$('#f-keyword').on('keydown', function(e){
		if(e.key === 'Enter') {
			doSearch();
		}
	});

})
	
async function getFavorites(){
	try{
		if(state.category == null) state.category = 0;
		
		let queryString = toQuery(state);
		let url = "/api/jobs/my/favorites?" + queryString;
		
		const res = await fetch(url);
		let data = await res.json();
		
		const items = data.favorites;
		
		if(!items || items.length === 0){
			$('#apps-tbody').html('<tr><td colspan="7" class="empty">아직 스크랩 한 공고가 없어요. <a href="/jobs/board" class="btn ghost" style="margin-left:8px">공고 보러가기</a></td></tr>');
			$('#result-count').text('0건');
			return;
		}

		console.log(items);
		
		if(state.category == 0) renderCompanyFavorites(items);
		else renderMemberFavorites(items);
		
		loadPagination(data.pageMaker);
	}catch(e){
		console.log(e);
		$('#apps-tbody').html('<tr><td colspan="7" class="empty">불러오기에 실패했어요. 잠시 후 다시 시도해주세요.</td></tr>');
	}
}

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
	
function renderCompanyFavorites(items){
	let header = 
		'<tr>'
		+	'<th></th>'
		+ 	'<th>공고 제목</th>'
		+	'<th>회사명</th>'
		+ 	'<th>근무지</th>'
		+ 	'<th>포지션</th>'
		+ 	'<th>급여</th>'
		+ 	'<th>등록일</th>'
		+	'<th></th>'
		+'</tr>';
	$('#apps-theader').html(header);
	
	let rows = items.map(function(it){
		var postId = Number(it.post_id) || 0;
		var jobTitle = esc(it.job_title) || '-';
		var company = esc(it.company_name) || '-';
		var areaText = buildAreaText(it) || '-';
		var positionText = buildPositionText(it.position);
		var payCell = buildPayCell(it);
		var created = esc(timeAgo(it.created_at)); 
		var status = statusBadge(it.status); 
		
		return ''
			+ '<tr>'
			+	'<td>'
			+		'<span class="favoriteSpan" data-board-no="' + postId + '" data-board-type="job">'
			+			'<i class="favorite fa-star fa-solid" style="color: #FFD43B; cursor: pointer;"></i>'
			+		'</span>'
			+	'</td>'
			+	'<td class="ellipsis"><a href="/jobs/post/' + postId + '">' + jobTitle + '</a></td>'
			+	'<td>' + company + '</td>'
			+	'<td>' + areaText + '</td>'
			+	'<td>' + positionText + '</td>'
			+	'<td class="pay">' + payCell + '</td>'
			+	'<td>' + created + '</td>'
			+	'<td>' + status + '</td>'
			+ '</tr>'
	}).join('');
	$('#apps-tbody').html(rows);
}

function  renderMemberFavorites(items){
	let header = 
		'<tr>'
		+	'<th></th>'
		+ 	'<th>제목</th>'
		+ 	'<th>연습/활동 장소</th>'
		+ 	'<th>포지션</th>'
		+ 	'<th>등록일</th>'
		+ 	'<th></th>'
		+'</tr>';
	$('#apps-theader').html(header);
	
	let rows = items.map(function(it){
		var postId = Number(it.post_id) || 0;
		var jobTitle = esc(it.job_title) || '-';
		var areaText = buildAreaText(it) || '-';
		var positionText = buildPositionText(it.position);
		var created = esc(timeAgo(it.created_at)); 
		var status = statusBadge(it.status); 
		
		return ''
			+ '<tr>'
			+	'<td>'
			+		'<span class="favoriteSpan" data-board-no="' + postId + '" data-board-type="job">'
			+			'<i class="favorite fa-star fa-solid" style="color: #FFD43B; cursor: pointer;"></i>'
			+		'</span>'
			+	'</td>'
			+	'<td class="ellipsis"><a href="/jobs/post/' + postId + '">' + jobTitle + '</a></td>'
			+	'<td>' + areaText + '</td>'
			+	'<td>' + positionText + '</td>'
			+	'<td>' + created + '</td>'
			+ 	'<td>' + status + '</td>'
			+ '</tr>';
	}).join('');
	$('#apps-tbody').html(rows);
}
function statusBadge(s){
	let label = (s === 0) ? '진행중' : '마감';
	let cls = (s === 0) ? 'status-open' : 'status-close';
	return '<span class="badge ' + cls + '" aria-label="' + label + '">' + label + '</span>';
}
function loadPagination(pageMaker) {
    const $pagination = $("#pagination");
    $pagination.empty(); 
    
    for (let num = pageMaker.startPage; num <= pageMaker.endPage; num++) {
        $pagination.append(
            '<li class="paginate_button">' +
                '<a href="#" data-page="' + num + '" class="page font-weight-bold ' + (pageMaker.cvo.pageNum === num ? 'active' : 'default_btn') + '">' + num + '</a>' +
            '</li>'
        );
    }

    $("#pagination a").click(function (e) {
        e.preventDefault();
        let pageNum = $(this).data("page");
        
        state.pageNum = pageNum;
        getFavorites();
    });
}
function toQuery(state) {
	const entries = Object.entries(state).filter(([_, v]) => v !== null && v !== undefined && v !== "");
	return new URLSearchParams(Object.fromEntries(entries)).toString();
}
function formatNumberKo(pay) {
	const num = Number(String(pay).replace(/[^\d]/g, ''));
	return num ? num.toLocaleString('ko-KR') : '';
}

function esc(v) {
	if (v === null || v === undefined) return '';
	return String(v)
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;')
		.replace(/'/g, '&#39;');
}

function buildAreaText(it) {
	const parts = [it.city, it.gu, it.dong].filter(Boolean).map(esc);
	return parts.join('&nbsp;');
}

function buildPayCell(it) {
	if (it.pay_category > 2) {
		return (
			'<span class="negotiable">협의 후 결정</span>'
		);
	}

	let payCategoryClass = '';
	let payCategoryText = '';
	if (it.pay_category === 0) {
		payCategoryClass = 'daily';
		payCategoryText = '건당';
	} else if (it.pay_category === 1) {
		payCategoryClass = 'weekly';
		payCategoryText = '주급';
	} else if (it.pay_category === 2) {
		payCategoryClass = 'monthly';
		payCategoryText = '월급';
	}

	const hasPay = it.pay !== null && it.pay !== undefined && Number(it.pay) > 0;
	const payText = hasPay ? esc(formatNumberKo(it.pay)) +'원' : '';

	return (
		'<span class="pay_category  ' + esc(payCategoryClass) + '">' + esc(payCategoryText) + '</span>'
		+ (hasPay ? '&nbsp;<span class="pay-amount">' + payText + '</span>' : '')
	);
}

function buildPositionText(key) {
	const label = positionMap[key] || '미정';
	return esc(label);
}
function doSearch() {
	state.keyword = $("#f-keyword").val();
	getFavorites();
}

</script>
</head>
<body>
<div class="container">
		<div class="a-header">
			<div class="h-title">스크랩</div>
		</div>
		
		<div class="justify-between py-4 flex">
			<div class="switch-container">
			    <span class="switch-label">기업 구인🏢</span>
			    <label class="switch">
			        <input type="checkbox" id="typeSwitch">
			        <span class="slider"></span>
			    </label>
			    <span class="switch-label">멤버 모집🏕️</span>
			</div>
			
		    <div class="write_btn items-center none">
		    	<sec:authorize access="isAuthenticated()">
			    	<button type="button" id="jobWriteBtn" class="write_btn_font border-none ">
						<i class="fa-solid fa-pen-to-square write-icon"></i>
						작성하기
					</button>
		    	</sec:authorize>
			</div>
		</div>
		
		<div class="card">
			<div class="toolbar">
				<select id="f-status" class="select" aria-label="상태">
					<option value="">상태 전체</option>
					<option value="0">진행중</option>
					<option value="1">마감</option>
				</select>
				<select id="f-period" class="select" aria-label="기간">
					<option value="all" selected>전체</option>
					<option value="30">최근 30일</option>
					<option value="90">최근 3개월</option>
					<option value="180">최근 6개월</option>
				</select>
				<input id="f-keyword" class="input" placeholder="회사/공고/키워드 검색" />
				<button id="btn-search" class="btn">검색</button>
			</div>

			<table class="table" aria-label="스크랩 테이블">
				<thead id="apps-theader">
				</thead>
				<tbody id="apps-tbody">
					<tr aria-live="polite"><td colspan="7" class="empty">불러오는 중...</td></tr>
				</tbody>
			</table>
 
			<div>
				<div class="text-center">
				    <ul id="pagination" class="apps-paging pagination pagination_border"></ul>
				</div>
			</div>
		</div>
	</div>
</body>
</html>