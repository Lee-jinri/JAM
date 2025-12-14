<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 커뮤니티</title>
<script src="https://unpkg.com/browser-image-compression@2.0.1/dist/browser-image-compression.js"></script>
<script src="/resources/include/dist/summernote/summernote-lite.js"></script>
<script src="/resources/include/dist/summernote/summernote-ko-KR.js"></script>
<link rel="stylesheet" href="/resources/include/dist/summernote/summernote-lite.css">
	
<style>
.container {
	max-width: 1000px;
	margin: 0 auto;
	padding-top: 50px;
}

.form-group {
	margin-bottom: 25px;
}

label {
    display: block;
    font-weight: 500;
    margin-bottom: 5px;
    font-size: 14px;
}

.input-text {
	width: 100%;
	padding: 12px;
	border: 1px solid #dadada;
	border-radius: 14px;
	font-size: 15px;
}
.input-text:focus {
	border: 1px solid #d8e1ff;
    box-shadow: 0 0 10px 2px #e5ebff;
    background: #fcfdff;
    outline: none !important;
}
.btn-area {
	display: flex;
	justify-content: center;
	gap: 12px;
	margin-top: 35px;
}

.btn-primary, .btn-secondary {
	padding: 10px 26px;
	border-radius: 6px;
	font-size: 15px;
	display: inline-block;
	text-decoration: none;
}

.btn-primary {
	background-color: #5C6BC0;
	color: white;
	border: none;
	cursor: pointer;
}

.btn-primary:hover {
	background-color: #3F4EA6;
}

.btn-secondary {
	background-color: #E0E0E0;
	color: #333;
}

.btn-secondary:hover {
	background-color: #C8C8C8;
}
.note-editable img,
.post-content img {
	max-width: 100%;
	height: auto;
}
</style>	

</head>
<body class="wrap">
	<div class="container">
		<div class="page-header">
			<p class="page-title">포스트 작성</P>
			<p>좋은 하루예요! 커뮤니티에 유익한 글을 공유해주세요 :)</p>
		</div>
		<form id="comWrite">
			
			<div class="form-group">
				<label for="com_title">제목</label>
				<input type="text" id="title" name="com_title" class="input-text">
			</div>

			<div class="form-group">
				<label for="com_content">본문</label>
				<textarea id="com_content" name="com_content" class="summernote" style="resize: none;"></textarea>
			</div>
		    <div class="post-buttons">
				<button type="button" class="action-btn" id="write">등록</button>
			    <a href="/community/board" class="board-btn">취소</a>
			</div>
		</form>
	</div>
	
	<script>

	// 업로드된 이미지 데이터 저장
	let uploadedImages = []; 
	
	$(function() {
		if(!window.MY_ID){
			if (confirm("로그인이 필요한 서비스입니다. 로그인 하시겠습니까?")) {
				location.href = "/member/login";
			} else {
				location.href = "/community/board";
			}
			return;
		}
		
	    $("#write").click(function () {

	    	// 이미지 width를 px -> % 로 변환
	    	$('.note-editable img').each(function () {
	    		const $img = $(this);
	    		const parentWidth = $img.parent().width();
	    		const imgWidth = $img.width();

	    		if (parentWidth > 0 && imgWidth > 0) {
	    			const percent = (imgWidth / parentWidth) * 100;
	    			$img.css({
	    				width: percent + '%',
	    				height: 'auto'
	    			});
	    		}
	    	});
	    	
		    // 유효성 검사
	        let title = $("#title").val();
	        let content = $('.summernote').summernote('code');

	        if (title.replace(/\s/g, "") == "") {
	            alert("제목을 입력하세요.");
	            $("#com_title").focus();
	            return false;
	        }
	        
	        const hasText = $(content).text().trim() !== "";
	        const hasImage = $(content).find("img").length > 0;
	        
	        if (!hasText && !hasImage) {
	            alert("본문을 입력하세요.");
	            $('.summernote').summernote('focus');
	            return false;
	        }
	        
	        // 큰따옴표가 아닌 문자 1개 이상 반복
	        const regex = /data-key="([^"]+)"/g;
	        const matches = content.matchAll(regex);

	        // key만 배열에 추출
	        const fileKeys = [];
	        for (const match of matches) {
	        	fileKeys.push(match[1]);
	        }
	        
	        const file_assets = uploadedImages.filter(img => fileKeys.includes(img.file_key));
	        
	            var data = {
	                'title': title,
	                'content': content,
	                'file_assets': file_assets 
	            };
	            
	            fetch('/api/community/post', {
	                method: 'POST',
	                headers: {
	                    'Content-Type': 'application/json'
	                },
	                body: JSON.stringify(data)
	            })
	            .then(res =>{
	            	if (res.ok) {
		            	alert("등록이 완료되었습니다.");
		            	return res.text();
	            	}
	            	
	            	return res.json().then(err => {
			            throw err;
			        });
	            })
	            .then(data =>{
	            	$(location).attr('href', '/community/post/' + data);
	            })
	            .catch (err => {
		        	if (handleApiError(err)) return;
				    alert(err.detail || "게시글 작성을 완료할 수 없습니다. 잠시 후 다시 시도해주세요.");
		            console.error('Error:', err);
		        })
	        
	    });
		
		$('.note-editable').on('click', 'img', function() {
		    $(this).resizable({
		        aspectRatio: true
		    });
		});
	});
	
	
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
		minHeight: 450,
		maxHeight: null,
		lang: "ko-KR",
		placeholder : "내용을 작성하세요.",
		callbacks : {
	    	onImageUpload : function(files, editor, welEditable){
	    		for(var i = files.length - 1; i >= 0; i--){
	    			uploadImageFile(files[i], $(this));
	    		}
	    	}
	    }
	});

	const ALLOWED = [
	  'image/jpeg',
	  'image/png',
	  'image/webp',
	  'image/gif'
	];
	function getMime(file){
		
		let name = file.name.toLowerCase();
		if (name.endsWith('.jpeg')) return 'image/jpeg';
		if (name.endsWith('.jpg')) return 'image/jpeg';
		if (name.endsWith('.png')) return 'image/png';
		if (name.endsWith('.webp')) return 'image/webp';
		if (name.endsWith('.gif')) return 'image/gif'
		return file.type || 'application/octet-stream';
	}
	
	async function uploadImageFile(file, editor) {
		try {

			const contentType = getMime(file);

			if (!ALLOWED.includes(contentType)) {
				alert("이미지 파일만 업로드할 수 있습니다.");
				return;
			}
			
			// 1. 이미지 압축
			const optimized = await optimizeImage(file);
			console.log(optimized);
			// 2. presigned URL 요청
			const res = await fetch('/api/files/upload-url', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					file_name: optimized.name,
					file_type: contentType,
					file_size: optimized.size,
					file_category: "POST_IMAGE"
				})
			});
			const json = await res.json();
			if (json.error) throw new Error(json.error);
			
			uploadedImages.push({
	            file_key: json.key,
	            file_name: json.filename,
	            file_type: contentType,
	            file_size: optimized.size,
	            file_category: "POST_IMAGE"
	        });
			
			// 3. S3 업로드
			await fetch(json.url, {
				method: 'PUT',
				headers: { 'Content-Type': contentType },
				body: optimized
			});

			// 4. Summernote에 이미지 삽입
			const viewRes = await fetch('/api/files/view-url?key=' + json.key);
			const viewUrl = await viewRes.text();
			
			$(editor).summernote('editor.insertImage', viewUrl, function ($image) {
			    $image.attr("data-key", json.key);
			});
			
		} catch (err) {
			console.error(err);
			alert('이미지 업로드에 실패했습니다. 잠시 후 다시 시도하세요.');
		}
	}
	</script>
</body>
</html>