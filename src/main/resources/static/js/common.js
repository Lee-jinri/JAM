/*chkData(유효성 체크 대상, 메시지 내용)*/
function chkData(item, msg){
	if($(item).val().replace(/\s/g,"")==""){
		alert(msg+" 입력해 주세요.");
		$(item).val("");
		$(item).focus();
		return false;
	} else{
		return true;
	}
}

/* 함수명:chkData(유효성 체크 대상, 메시지 내용)
 *출력 영역 :alert으로
 
*/
function dataCheck(item,out,msg){
	if($(item).val().replace(/\s/g,"")==""){
		$(out).html(msg+" 입력해 주세요");
		$(item).val("");
		return false;
	}else{
		return true;
	}
}
function checkForm(item, msg){
	let message = "";
	if($(item).val().replace(/\s/g,"")==""){
		message = msg+" 입력해 주세요.";
		$(item).attr("placeholder",message);
		return false;
	}else{
		return true;
	}
}

function formCheck(main, item, msg){
	if($(main).val().replace(/\s/g,"")==""){
		$(item).css("color", "#000099").html(msg+" 입력해 주세요");
		$(main).val("");
		return false;
	}else{
		return true;
	}
}

function chkFile(item){
	let ext = item.val().split('.').pop().toLowerCase();
	if(jQuery.inArray(ext,['gif','png','jpg','jpeg'])==-1){
		alert('gif','png','jpg','jpeg 파일만 업로드 할 수 있습니다.');
		item.val("");
		return false;
	}else{
		return true;
	}
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
	
	// 개월 계산 (30일 단위)
    let months = Math.floor(diff / 2592000); 
    if (months < 12) return months + '개월 전';
    
	let dateStr = past.toLocaleDateString();
	return dateStr.replace(/\.$/, '');
}

function handleApiError(err, fallbackUrl = "/") {
	const isPopup = window.opener != null;
	// 401
	if (err?.loginRequired) {
		if (isPopup) {
			// 팝업 -> 부모 페이지 로그인 페이지로 이동 + 팝업 닫기
			if(window.opener && !window.opener.closed){
				if (confirm("로그인이 필요한 서비스입니다.\n로그인 페이지로 이동하겠습니까?")) {
					window.opener.location.href = "/member/login";
				} 
				window.close();
			}
			else {
				window.close();
			}
		} else {
			// 일반 페이지
			if (confirm("로그인이 필요한 서비스입니다.\n로그인 페이지로 이동하겠습니까?")) {
				location.href = "/member/login";
			} else {
				location.href = fallbackUrl;
			}
		}
		return true;
	}
	
	// 403
	if (err?.forbidden) {
		alert("접근 권한이 없습니다.");
		if (isPopup) window.close();
		else location.href = fallbackUrl;
		return true;
	}
	return false;
}

// 이미지 리사이징 함수 (5MB 이하로 변환)
async function optimizeImage(file) {
	const options = {
		maxSizeMB: 5,
		maxWidthOrHeight: 2000,
		useWebWorker: true
	};

	if (file.size <= 5 * 1024 * 1024) return file;

	const compressed = await imageCompression(file, options);
	return compressed;
}
