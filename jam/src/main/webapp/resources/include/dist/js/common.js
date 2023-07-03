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
