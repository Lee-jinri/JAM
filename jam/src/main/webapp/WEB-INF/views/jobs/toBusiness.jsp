<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
	body {
		margin: 0;
		font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif;
		background: #fff;
		color: #212529;
	}
	.popup-wrap {
		min-height: 100vh;
		display: flex;
		flex-direction: column;
	}
	.popup-header {
		padding: 14px 18px;
		border-bottom: 1px solid #eee;
		font-weight: 700;
	}
	.popup-body {
		padding: 16px 18px 8px;
	}
	.warning {
		margin: 0 0 10px 0;
		display: flex;
		align-items: flex-start;
		gap: 8px;
		padding: 12px 14px;
		border: 1px solid #ffe1b3;
		background: #fff7e6; 
		color: #7a4b00;      
		border-radius: 8px;
		font-size: 14px;
		line-height: 1.5;
	}
	.help {
		margin: 4px 0 14px;
		font-size: 12px;
		color: #6c757d;
	}
	form .form-grid {
		display: grid;
		grid-template-columns: 1fr;
		gap: 10px;
	}
	.form-row {
		display: flex;
		flex-direction: column;
		gap: 6px;
	}
	label {
		font-size: 13px;
		font-weight: 600;
		color: #495057;
	}
	input[type="text"],
	input[type="email"],
	input[type="tel"],
	input[type="url"],
	input[type="number"],
	textarea,
	select {
		padding: 10px 12px;
		border: 1px solid #dfe3e8;
		border-radius: 8px;
		font-size: 14px;
		outline: none;
		transition: border-color .15s ease, box-shadow .15s ease;
	}
	input:focus,
	textarea:focus,
	select:focus {
		border-color: #86b7fe;
		box-shadow: 0 0 0 3px rgba(13,110,253,.15);
	}
	textarea {
		min-height: 72px;
		resize: vertical;
	}
	.inline-2 {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 10px;
	}
	.popup-footer {
		margin-top: auto;
		border-top: 1px solid #eee;
		padding: 12px 18px;
		display: flex;
		justify-content: flex-end;
		gap: 8px;
	}
	.btn {
		border: 1px solid #e9ecef;
		background: #fff;
		color: #212529;
		padding: 10px 14px;
		border-radius: 8px;
		font-size: 14px;
		cursor: pointer;
		transition: box-shadow .15s ease, transform .05s ease, background .15s ease;
	}
	.btn:hover { box-shadow: 0 6px 18px rgba(0,0,0,.06); }
	.btn:active { transform: translateY(1px); }
	.btn-primary {
		border-color: transparent;
		background: #0d6efd;
		color: #fff;
		box-shadow: 0 6px 14px rgba(13,110,253,.25);
	}
	.btn-primary:hover { background: #0b5ed7; }
	.btn-ghost { background: #f8f9fa; }
	.required::after {
		content: " *";
		color: #dc3545;
	}
	.field-error {
		font-size: 12px;
		color: #dc3545;
		display: none;
	}
	@media (max-width: 520px) {
		.inline-2 { grid-template-columns: 1fr; }
	}
</style>
</head>
<body>
	<div class="popup-wrap">
		<sec:authorize access="isAuthenticated()">
			<script>window.AUTHENTICATED = true;</script>
		</sec:authorize>
		<sec:authorize access="hasRole('COMPANY')">
			<script>window.ROLE_COMPANY = true;</script>
		</sec:authorize>
		<div class="popup-header">기업회원 전환</div>

		<div class="popup-body">
			<p class="warning">⚠️기업 회원 전환 후 되돌릴 수 없습니다.</p>
			<p class="help">아래 정보를 입력해 주세요. <br>
				실제 저장은 <b>회사명</b>만 반영되며 나머지는 더미 데이터로 처리됩니다.
			</p>

			<form id="businessForm">
				<div class="form-grid">
					<div class="form-row">
						<label for="companyName" class="required">회사명</label>
						<input type="text" id="companyName" name="company_name" placeholder="예) JAM Inc." autocomplete="organization">
						<div class="field-error" id="companyNameError">회사명을 입력해 주세요.</div>
					</div>

					<div class="inline-2">
						<div class="form-row">
							<label for="bizNo">사업자등록번호</label>
							<input type="text" id="bizNo" name="biz_reg_no" placeholder="예) 123-45-67890">
						</div>
						<div class="form-row">
							<label for="repName">대표자명</label>
							<input type="text" id="repName" name="rep_name" placeholder="예) 홍길동">
						</div>
					</div>

					<div class="inline-2">
						<div class="form-row">
							<label for="companyEmail">회사 이메일</label>
							<input type="email" id="companyEmail" name="company_email" placeholder="예) hr@company.com" autocomplete="email">
						</div>
						<div class="form-row">
							<label for="companyPhone">회사 연락처</label>
							<input type="tel" id="companyPhone" name="company_phone" placeholder="예) 02-1234-5678" autocomplete="tel">
						</div>
					</div>

					<div class="inline-2">
						<div class="form-row">
							<label for="industry">업종</label>
							<select id="industry" name="industry">
								<option value="">선택</option>
								<option>음악 기획/제작</option>
								<option>공연/이벤트</option>
								<option>교육/아카데미</option>
								<option>악기/장비 유통</option>
								<option>미디어/콘텐츠</option>
								<option>기타</option>
							</select>
						</div>
					</div>

					<div class="form-row" style="gap:10px;">
						<label style="display:flex; gap:8px; align-items:center; font-weight:500;">
							<input type="checkbox" id="agree" name="agree_terms">
							기업회원 약관에 동의합니다. 
						</label>
					</div>
				</div>
			</form>
		</div>

		<div class="popup-footer">
			<button type="button" class="btn btn-ghost" onclick="window.close()">닫기</button>
			<button type="button" class="btn btn-primary" id="submitBtn">전환하기</button>
		</div>
	</div>
	<script>
		document.getElementById('submitBtn').addEventListener('click', async() => {
			let nameEl = document.getElementById('companyName');
			let errEl = document.getElementById('companyNameError');
			let companyName = nameEl.value.trim();

			if (!companyName) {
				errEl.style.display = 'block';
				nameEl.focus();
				return;
			}
			errEl.style.display = 'none';

			let payload = {
				company_name: companyName
			};

			try {
				const res = await fetch('/api/member/convertBusiness', {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify(payload)
				});

				if (!res.ok) throw new Error('Failed');
				
				alert('기업회원으로 전환되었습니다.');
				
				// 부모 창 새로고침
				if (window.opener && !window.opener.closed) {
					window.opener.location.reload();	
				}
				window.close();
			} catch (e) {
				alert('기업회원 전환에 실패했습니다. 잠시 후 다시 시도해 주세요.');
				console.error(e);
			}
		});
	</script>
</body>
</html>