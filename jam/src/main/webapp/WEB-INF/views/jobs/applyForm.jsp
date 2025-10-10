<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript" src="/resources/include/dist/js/jquery-3.7.1.min.js"></script>
	
<style>
	:root{
		--bg:#f5f7fa; --card:#ffffff; --text:#1e1e1e; --muted:#6c757d;
		--primary:#2f6feb; --primary-light:#6ea8fe; --line:#dee2e6;
		--danger:#d9534f; --ok:#20c997;
		--radius:14px;
	}
	*{box-sizing:border-box}
	body{
		margin:0; background:var(--bg); color:var(--text);
		font-family:system-ui,-apple-system,"Segoe UI",Roboto,Apple SD Gothic Neo,Arial,sans-serif;
	}
	.wrap{max-width:560px; margin:28px auto; padding:0 14px;}
	.card{
		background:var(--card); border:1px solid var(--line);
		border-radius:var(--radius); padding:22px 20px 18px;
		box-shadow:0 4px 12px rgba(0,0,0,.08);
	}
	.head{
		display:flex; align-items:center; justify-content:space-between; gap:12px;
		margin-bottom:12px;
	}
	.title{font-size:20px; font-weight:700; letter-spacing:.2px;}
	.hint{font-size:12px; color:var(--muted)}
	form{display:grid; gap:14px; margin-top:8px}
	label{display:block; font-size:13px; color:var(--muted); margin-bottom:6px}
	input[type="text"], textarea{
		width:100%; color:var(--text); background:#fff;
		border:1px solid var(--line); border-radius:10px;
		padding:12px 12px; font-size:14px; outline:none;
		transition:border .15s ease, box-shadow .15s ease;
	}
	input[type="text"]:focus, textarea:focus{
		border-color:var(--primary); box-shadow:0 0 0 3px rgba(47,111,235,.2);
	}
	textarea{min-height:120px; resize:vertical; line-height:1.5}
	.row{display:grid; gap:12px}
	.file{
		background:#fafafa; border:1px dashed var(--line); border-radius:10px;
		padding:14px; display:flex; align-items:center; gap:12px; flex-wrap:wrap;
	}
	.file input{display:none}
	.file .btn{
		display:inline-flex; align-items:center; justify-content:center;
		padding:10px 14px; border-radius:10px; cursor:pointer;
		background:var(--primary); color:#fff; font-weight:700; border:none;
	}
	.file .meta{font-size:13px; color:var(--muted)}
	.file .name{font-size:13px; color:var(--text); font-weight:600}
	.rules{font-size:12px; color:var(--muted);}
	.actions{display:flex; gap:10px; justify-content:flex-end; margin-top:4px}
	.btn{
		padding:12px 16px; border-radius:12px; cursor:pointer; border:1px solid var(--line);
		background:#f8f9fa; color:var(--text); font-weight:700;
	}
	.btn.primary{background:var(--primary); color:#fff; border-color:transparent}
	.btn.primary:hover{filter:brightness(1.05)}
	.btn:disabled{opacity:.55; cursor:not-allowed}
	.count{font-size:12px; color:var(--muted); text-align:right; margin-top:-8px}
	.error{font-size:12px; color:var(--danger)}
	.ok{font-size:12px; color:var(--ok)}
	.xbtn{
		display:inline-flex; align-items:center; justify-content:center;
		margin-left:8px; padding:4px 8px; border-radius:8px;
		background:#f1f3f5; color:#333; border:1px solid #dee2e6;
		font-size:12px; line-height:1; cursor:pointer;
	}
	.xbtn:hover{ background:#e9ecef; }
	.xbtn:active{ transform:translateY(1px); }
</style>

</head>
<body>
	<div class="wrap">
		<div class="card">
			<div class="head">
				<div class="title">지원하기</div>
				<div class="hint">제출 전 내용을 한번 더 확인해 주세요.</div>
			</div>

			<form id="applyForm" method="post" action="/api/jobs/applications">
				<input type="hidden" name="postId" value="${postId}">
				<div class="row">
					<label for="title">제목</label>
					<input type="text" id="title" name="title" maxlength="60" placeholder="예) 피아노 세션 지원 – 홍길동" required />
					<div class="count" id="titleCount">0 / 60</div>
				</div>

				<div class="row">
					<label for="content">자기소개</label>
					<textarea id="content" name="content" maxlength="800" placeholder="간단한 자기소개 및 지원 동기를 적어주세요. (최대 800자)"></textarea>
					<div class="count" id="contentCount">0 / 800</div>
				</div>

				<div class="row file-row">
					<label>이력서 파일 업로드</label>
					<div class="file">
						<label for="resume" class="btn">파일 선택</label>
						<input id="resume" type="file" multiple accept=".pdf,.doc,.docx,.hwp" />
						<div class="meta">
							<div class="name">
								<span id="fileName">선택된 파일 없음</span>
								<span id="fileActions"></span>
								<ul id="fileList" style="margin:6px 0 0; padding-left:18px"></ul>
							</div>
							<div class="rules">허용 확장자: PDF, DOCX, HWP · 최대 20MB</div>
							<div id="fileMsg" class="error" style="display:none;"></div>
						</div>
					</div>
				</div>

				<div class="actions">
					<button type="button" class="btn" onclick="window.close()">취소</button>
					<button type="submit" class="btn primary" id="submitBtn" disabled>제출</button>
				</div>
			</form>
		</div>
	</div>
	
	
<script>
$(function() {
	var category = ${category}; 

	if (category === 1) {
		$(".file-row").remove();
		
		$("#content").attr("placeholder", "간단한 자기소개와 연락 가능한 방법(메일, 전화번호 등)을 적어주세요. (최대 800자)");
		$("#title").attr("placeholder", "예) 보컬 지원 – 홍길동");
		
		$("#submitBtn").prop("disabled", false);
	} 
});
	const titleEl = document.getElementById('title');
	const contentEl = document.getElementById('content');
	const titleCount = document.getElementById('titleCount');
	const contentCount = document.getElementById('contentCount');
	const resumeInput = document.getElementById('resume');
	const fileNameEl = document.getElementById('fileName');
	const fileMsg = document.getElementById('fileMsg');
	const submitBtn = document.getElementById('submitBtn');
	const fileActions = document.getElementById("fileActions");
	const fileListEl  = document.getElementById("fileList");
	
	const ALLOWED = [
	  'application/pdf',
	  'application/msword',
	  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
	  'application/x-hwp',
	  'application/haansofthwp'
	];
	const MAX = 20 * 1024 * 1024; // 20MB
		
	// 파일 크기 표시
	function formatBytes(n){
		if (n < 1024) return n + ' B';
		var kb = n / 1024;
		if (kb < 1024) return kb.toFixed(1) + ' KB';
		var mb = kb / 1024;
		
		return mb.toFixed(1) + ' MB';
	}

	// 글자수 카운트 업데이트
	titleEl.addEventListener('input', function(){
		titleCount.textContent = titleEl.value.length + ' / ' + titleEl.maxLength;
	});
	contentEl.addEventListener('input', function(){
		contentCount.textContent = contentEl.value.length + ' / ' + contentEl.maxLength;
	});

	if (resumeInput) {
		resumeInput.addEventListener('change', renderFiles);
	}
	function renderFiles(){
		fileMsg.style.display = 'none';
		fileMsg.textContent = '';

		const files = Array.from(resumeInput.files || []);
		fileListEl.innerHTML = '';
		while (fileActions.firstChild) fileActions.removeChild(fileActions.firstChild);

		if (files.length === 0) {
			fileNameEl.textContent = '선택된 파일 없음';
			submitBtn.disabled = true;
			return;
		}

		// 모든 파일 유효성(용량, 파일 형식) 검사
		for (const f of files) {
			const mime = getMime(f);
			
			if (f.size > MAX) {
				fileMsg.textContent = '파일 ' + f.name + '이(가) 20MB를 초과합니다. 현재: ' + formatBytes(f.size) + ' bytes';
				fileMsg.style.display = 'block';
				submitBtn.disabled = true;
				return;
			}
			if (!ALLOWED.includes(mime)) {
				fileMsg.textContent = '허용되지 않은 파일 형식: ' + f.name;
				fileMsg.style.display = 'block';
				submitBtn.disabled = true;
				return;
			}
		}

		// 총 개수/용량
		const total = files.reduce(function(s, f){ return s + f.size; }, 0);
		fileNameEl.textContent = files.length + '개 · 총 ' + formatBytes(total) + ' bytes';

		// 목록, 삭제 버튼
		files.forEach(function(f, idx){
			const li = document.createElement('li');
			li.style.margin = '4px 0';
			li.textContent = f.name + ' · ' + formatBytes(f.size) + ' bytes ';

			const delBtn = document.createElement('button');
			delBtn.type = 'button';
			delBtn.className = 'xbtn';
			delBtn.setAttribute('aria-label', f.name + ' 삭제');
			delBtn.textContent = '삭제';

			delBtn.addEventListener('click', function(){
				const dt = new DataTransfer();
				files.forEach(function(ff, i){ if (i !== idx) dt.items.add(ff); });
				resumeInput.files = dt.files;
				renderFiles();
			});

			li.appendChild(delBtn);
			fileListEl.appendChild(li);
		});

		submitBtn.disabled = false;
	}

	// 카운트 업데이트
	titleCount.textContent = '0 / ' + titleEl.maxLength;
	contentCount.textContent = '0 / ' + contentEl.maxLength;
	
	function getMime(file) {
		let name = file.name.toLowerCase();
		if (name.endsWith('.docx')) return 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
		if (name.endsWith('.doc')) return 'application/msword';
		if (name.endsWith('.pdf')) return 'application/pdf';
		if (name.endsWith('.hwp')) return 'application/x-hwp';
		return file.type || 'application/octet-stream';
	}
	
	const form = document.getElementById("applyForm");
	
	form.addEventListener('submit', async function(e){
		e.preventDefault(); 
		
		if (fileMsg) { 
		  fileMsg.style.display = 'none';
		  fileMsg.textContent = '';
		}
		
		const postId = Number(document.querySelector('input[name="postId"]').value);
		const title = titleEl.value;
		const content = contentEl.value;
		const category = ${category};
		
		if (!postId || isNaN(postId)) {
			alert("잘못된 접근입니다.");
			return;
		}
		if (!title.trim()) {
			alert("제목을 입력하세요.");
			return;
		}
		if (!content.trim()) {
			alert("내용을 입력하세요.");
			return;
		}
		
		// 버튼 잠금
		submitBtn.disabled = true;
		submitBtn.textContent = '제출 중...';
		
		// 멤버 모집: 파일 업로드 없이 바로 저장
		if (category === 1) {
			const payload = {
				post_id: postId,
				title: title,
				content: content,
				category: 1
			};
			fetch('/api/jobs/applications', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(payload)
			})
			.then(res => { 
				if (!res.ok) {
					let serverMsg = '업로드/저장 중 오류가 발생했습니다.';
					
					if (res.status !== 204) {
						const ct = res.headers.get('content-type') || '';
						if (ct.includes('application/json')) {
							return res.json().then(data => {
								if (data && typeof data.error === 'string') {
									serverMsg = data.error;
								}
								throw new Error(serverMsg);
							});
						} else {
							return res.text().then(text => {
								if (text) serverMsg = text;
								throw new Error(serverMsg);
							});
						}
					}
					
					if (res.status === 400) serverMsg = serverMsg || '잘못된 요청입니다.';
					if (res.status === 404) serverMsg = serverMsg || '해당 공고를 찾을 수 없습니다.';
					if (res.status === 409) serverMsg = serverMsg || '이미 해당 공고에 지원했습니다.';

					throw new Error(serverMsg);
				}
			})
			.then(() => {
				submitBtn.disabled = false;
				submitBtn.textContent = '제출';
				alert("참여 신청이 완료되었습니다.");
				window.close();
			})
			.catch(err => {
				const msg = err && err.message ? err.message : '업로드/저장 중 오류가 발생했습니다.';
				
				const el = document.getElementById('fileMsg');
				if (el && el.isConnected) {
					el.textContent = msg;
					el.style.display = 'block';
				} else {
					alert(msg);
				}
				submitBtn.disabled = false;
				submitBtn.textContent = '제출';
			});
			return; 
		}
		
		// 기업 공고 
		let files = Array.from(resumeInput.files || []);
		if (files.length === 0) {
			fileMsg.textContent = '이력서 파일을 선택하세요.';
			fileMsg.style.display = 'block';
			return;
		}

		function uploadOne(file) {
			if (file.size > MAX) {
				return Promise.reject(
			    	new Error('파일 ' + file.name + '이(가) 20MB를 초과합니다. 현재: ' + formatBytes(file.size))
			    );
			}
			
			const contentType = getMime(file) || 'application/octet-stream';
		
			// presigned url 발급
			return fetch('/api/files/s3/presign/upload?' + qs.toString(), { method: 'GET' })
			.then(res => res.json())
			.then(json => {
				if (json.error){
					throw new Error(json.error);
				}
				// S3 PUT 업로드
				return fetch(json.url, {
					method: 'PUT',
					headers: { 'Content-Type': json.contentType || contentType },
					body: file
				}).then(r => {
					if (!r.ok) throw new Error('S3 업로드 실패(' + r.status + ')');
					
					return {
						key: json.key,
						name: file.name,
						type: json.contentType || contentType,
						size: String(file.size)
					};
				});
			})
		}
		
		
		Promise.all(files.map(uploadOne))
		.then(results => {
			const payload = {
				post_id: postId,
				title: title,
				content: content,
				file_assets: results.map(m => ({
					file_key:  m.key,
					file_name: m.name,
					file_type: m.type,
					file_size: Number(m.size)
				}))
			};
			return fetch('/api/jobs/applications', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(payload)
			});
		})
		.then(res => {
			if (!res.ok) {
				let serverMsg = '업로드/저장 중 오류가 발생했습니다.';
				
				if (res.status !== 204) {
					const ct = res.headers.get('content-type') || '';
					if (ct.includes('application/json')) {
						return res.json().then(data => {
							if (data && typeof data.error === 'string') {
								serverMsg = data.error;
							}
							throw new Error(serverMsg);
						});
					} else {
						return res.text().then(text => {
							if (text) serverMsg = text;
							throw new Error(serverMsg);
						});
					}
				}
				
				if (res.status === 400) serverMsg = serverMsg || '잘못된 요청입니다.';
				if (res.status === 404) serverMsg = serverMsg || '해당 공고를 찾을 수 없습니다.';
				if (res.status === 409) serverMsg = serverMsg || '이미 해당 공고에 지원했습니다.';

				throw new Error(serverMsg);
			}
			
			submitBtn.disabled = false;
			submitBtn.textContent = '제출';
			alert("지원이 완료되었습니다.");
			window.close();
		})
		.catch(err => {
			const msg = err && err.message ? err.message : '업로드/저장 중 오류가 발생했습니다.';
			
			const el = document.getElementById('fileMsg');
			if (el && el.isConnected) {
				el.textContent = msg;
				el.style.display = 'block';
			} else {
				alert(msg);
			}
			submitBtn.disabled = false;
			submitBtn.textContent = '제출';
		});
	});
</script>
</body>
</html>