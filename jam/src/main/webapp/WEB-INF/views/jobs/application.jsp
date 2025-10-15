<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
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
	.head{display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:12px;}
	.title{font-size:20px; font-weight:700; letter-spacing:.2px;}
	label{display:block; font-size:13px; color:var(--muted); margin-bottom:6px}
	input[type="text"]{
		width:100%; color:var(--text); background:#fff;
		border:1px solid var(--line); border-radius:10px;
		padding:12px 12px; font-size:14px; outline:none;
	}
	input[readonly]{background:#f8f9fa}
	.readonly-box{
		width:100%; border:1px solid var(--line); border-radius:10px; background:#fff;
		padding:12px; font-size:14px; line-height:1.5;
		white-space:pre-wrap; word-break:break-word;
		max-height:380px; overflow:auto;
	}
	.row{display:grid; gap:12px; margin-top: 35px;}
	.file{
		background:#fafafa; border:1px dashed var(--line); border-radius:10px;
		padding:14px; display:flex; align-items:center; gap:12px; flex-wrap:wrap; justify-content:space-between;
	}
	.actions{display:flex; gap:10px; justify-content:flex-end; margin-top:14px}
	.btn{
		padding:10px 14px; border-radius:10px; cursor:pointer; border:1px solid var(--line);
		background:#f8f9fa; color:var(--text); font-weight:700;
	}
	.btn.primary{background:var(--primary); color:#fff; border-color:transparent}
	.btn.primary:disabled{opacity:.5; cursor:not-allowed}
	.btn.primary:hover:not(:disabled){filter:brightness(1.05)}
</style>

<script>
$(function(){	
	getApplication();
});

async function getApplication(){
	const appId = ${applicationId};
	if(!appId){
		alert("오류가 발생했습니다. 잠시 후 다시 시도하세요.");
		window.close();
		return;
	}

	try {
		const res = await fetch('/api/jobs/applications/' + appId, {credentials:'same-origin'});
		if(!res.ok){
			let msg = "오류가 발생했습니다. 잠시 후 다시 시도하세요.";
			try {
				const data = await res.json();
				if (data && data.error) msg = data.error;
			} catch (_) {  }
			alert(msg);
			window.close();
			return;
		}
		const data = await res.json();
		if(!data || !data.app){
			alert("오류가 발생했습니다. 잠시 후 다시 시도하세요.");
			window.close();
			return;
		}

		const category = data.category;
		const app  = data.app;
		const files = data.files;

		$("#title").val(app.title ?? "");
		$("#content").text(app.content ?? "");

		if(category === "USER"){
			$(".file-row").remove();
		}else if(category === "COMPANY"){
			const $template = $("#file").clone().removeAttr("id");
			$(".file").remove();
			
			files.forEach(file =>{
				const $clone = $template.clone();
				const displayName = file.file_name || "파일";
				
				$clone.find(".fileName").text(displayName);
				$clone.find(".downloadBtn").data("fileId", file.file_id).prop("disabled", false);

				$(".file-row").append($clone);
			})
		}
	} catch (e){
		console.error(e);
		alert("오류가 발생했습니다. 잠시 후 다시 시도하세요.");
		window.close();
	}
}

// 다운로드
$(document).on("click", ".downloadBtn", function(){
	const fileId = $(this).data("fileId");
	if(!fileId){
		alert("파일 정보를 찾을 수 없습니다.");
		return;
	}
	
	fetch('/api/files/' + fileId + '/download-url')
	.then(res => res.text())
	.then(url => {
		window.open(url, '_blank');
	});

});
</script>
</head>
<body>
<input type="hidden" id="applicationId" name="applicationId" value="${applicationId}">
<div class="wrap">
	<div class="card">
		<div class="head">
			<div class="title">지원서</div>
		</div>

		<div class="row">
			<label for="title">제목</label>
			<input type="text" id="title" name="title" readonly />
		</div>

		<div class="row">
			<label>자기소개</label>
			<div id="content" class="readonly-box"></div>
		</div>

		<div class="row file-row">
			<label>이력서 파일</label>
			<div id="file" class="file">
				<span class="fileName">파일 정보를 불러오는 중…</span>
				<button type="button" class="downloadBtn btn primary" disabled>다운로드</button>
			</div>
		</div>

		<div class="actions">
			<button type="button" class="btn" onclick="window.close()">닫기</button>
		</div>
	</div>
</div>
</body>
</html>
