<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>지원 내역</title>
<link rel="stylesheet" href="/resources/include/dist/css/job-common.css">
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
			  job_status: null
	};
	
	$(function(){
		loadApps();
		
		document.getElementById("f-status").addEventListener("change", function(e) {
			const selected = e.target.value;
			state.job_status = selected;
			
			loadApps();
		});
		
		document.getElementById("f-period").addEventListener("change", function(e) {
			const selected = e.target.value;
			state.period = selected;
			
			loadApps();
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

	async function loadApps(){

		$('#apps-tbody').html('<tr><td colspan="7" class="empty">불러오는 중...</td></tr>');
		$('.apps-paging').empty();

		try{
			let queryString = toQuery(state);
			let url = "/api/jobs/my/applications?" + queryString;
			
			const res = await fetch(url);
			let data = await res.json();
			
			const items = data.apps;
			
			if(!items || items.length === 0){
				$('#apps-tbody').html('<tr><td colspan="7" class="empty">아직 지원한 공고가 없어요. <a href="/jobs/board" class="btn ghost" style="margin-left:8px">공고 보러가기</a></td></tr>');
				$('#result-count').text('0건');
				return;
			}

			let rows = items.map(function(it){
				return ''
					+ '<tr>'
					+	'<td>' + kindIcon(it.category) + '</td>'
					+	'<td class="ellipsis"><a href="/jobs/post/' + it.post_id + '">' + it.job_title + '</a></td>'
					+	'<td>' + it.company_name + '</td>'
					+	'<td>' + timeAgo(it.created_at) + '</td>'
					+	'<td>' + statusBadge(it.status) + '</td>'
					+	'<td class="ellipsis" title="' + it.application_title + '">' + it.application_title + '</td>'
					+	'<td>'
					+		'<div class="row-actions">'
					+			'<button class="btn" data-action="detail" data-id="' + it.application_id + '">상세</button>'
					+			'<button class="btn" data-action="withdraw" data-id="' + it.application_id + '" ' + (it.status !== 0 ? 'disabled' : '') + '>취소</button>'
					+		'</div>'
					+	'</td>'
					+ '</tr>';
			}).join('');
			$('#apps-tbody').html(rows);

			loadPagination(data.pageMaker);
		}catch(e){
			console.log(e);
			$('#apps-tbody').html('<tr><td colspan="7" class="empty">불러오기에 실패했어요. 잠시 후 다시 시도해주세요.</td></tr>');
		}
	}
	
	function statusBadge(s){
		let label = (s === 0) ? '진행중' : '마감';
		let cls = (s === 0) ? 'status-open' : 'status-close';
		return '<span class="badge ' + cls + '" aria-label="' + label + '">' + label + '</span>';
	}

	function kindIcon(kind){
		let label = (kind === 0) ? '회사공고' : '멤버모집';
		let cls = (kind === 0) ? 'company' : 'user';
		return '<span class="icon ' + cls + '">' + label + '</span>';
	}

	$(document).on('click', '[data-action]', async function(){
		let act = $(this).data('action');
		let id = $(this).data('id');
		
		if(act === 'withdraw'){
			if(!confirm('지원을 취소하겠습니까?')) return;
			try{
				let res = await fetch('/api/jobs/applications/' + id + '/withdraw', {method:'DELETE'});
				if(!res.ok) throw new Error();
				alert('지원 취소가 완료 되었습니다.');
				loadApps();
			}catch(_){
				alert('지원 취소 중 오류가 발생했습니다. 잠시 후 다시 시도하세요.');
			}
		}
		
		if(act === 'detail') {
			const baseUrl = '/jobs/applications/';
			const url = baseUrl + id;
			
			const option = "width=500,height=695,top=100,left=100,resizable=no,scrollbars=yes";
			window.open(url, "showApplicationPopup", option);
		}
	});
	
	function doSearch() {
		state.keyword = $("#f-keyword").val();
		loadApps();
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
	        loadApps();
	    });
	}
	function toQuery(state) {
		const entries = Object.entries(state).filter(([_, v]) => v !== null && v !== undefined && v !== "");
		return new URLSearchParams(Object.fromEntries(entries)).toString();
	}
</script>
</head>
<body>
	<div class="container">
		<div class="a-header">
			<div class="h-title">지원내역</div>
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

			<table class="table" aria-label="지원 내역 테이블">
				<thead>
					<tr>
						<th>구분</th>
						<th>공고 제목</th>
						<th>회사/작성자</th>
						<th>지원일</th>
						<th>상태</th>
						<th>이력서</th>
						<th></th>
					</tr>
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
