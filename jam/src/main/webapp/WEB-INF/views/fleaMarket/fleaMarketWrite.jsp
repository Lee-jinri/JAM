<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>JAM - 중고악기</title>

  <!-- 서머노트를 위해 추가해야할 부분 -->
  <script src="/resources/include/dist/summernote/summernote-lite.js"></script>
  <script src="/resources/include/dist/summernote/summernote-ko-KR.js"></script>
  <link rel="stylesheet" href="/resources/include/dist/summernote/summernote-lite.css">

	<style type="text/css">
		.width-85 {width:80rem;}
		.border-radius-10 {border-radius:10px;}
		.com_title {border: 3px solid #ffdd77; }
		.com_content{    border: 3px solid #ffdd77;}
		.resize-none {resize: none;}
		.height4 {height:4rem;}
		#textarea{min-height:50rem;}
		.comWriteBtn{
			border-radius: 10px;
    border: 3px solid #ffdd77;
    /* padding: 3px 10px; */
    background-color: #fff;
    margin-right: 30px;
    height: 45px;
    width: 80px;
    /* color: black; */
    font-weight: 600;
    /* border: none;
		
		}
		#write {
		background-color:#ffdd77;
		color:#fff;}
	</style>
	<script>
		$(function(){
			
			$("#write").click(function(){
				let flea_title = $("#flea_title").val();
				let flea_content = $("#flea_content").val();
				
				if(flea_title.replace(/\s/g,"") == ""){
					alert("제목을 입력하세요.");
					$("#flea_title").focus();
					return false;
				}
				
				if(flea_content.replace(/\s/g,"") == ""){
					alert("본문을 입력하세요.");
					$("#flea_content").focus();
					return false;
				}
				
				if($("#price").val().replace(/\s/g,"") == ""){
					alert("가격을 입력하세요.");
					$("#price").focus();
					return false;
				}
				
				$("#fleaWrite").attr({
					"action" : "/fleaMarket/fleaWrite",
					"enctype": "multipart/form-data",
					"method" : "post"
				})
				
				$("#fleaWrite").submit();
				
			})
			
		})
	</script>
</head>
<body>
	<div class="rem-30">
		<div class="title flex justify-center">
			<h2>중고악기</h2>
		</div>
		<div class="content flex justify-center" >
			<form id="fleaWrite">
				<div>
					<input type="hidden" name="user_id" value="${member.user_id }"> 
					<input type="hidden" name="user_name" value="${member.user_name }">
					<input type="hidden" name="sales_status" value=0> 
				</div>
				<div>
					<select name="flea_category">
						<option value=0>판매</option>
						<option value=1>구매</option>
					</select><br/>
					<label>가격</label>
					<input type="number" name="price" id="price">원
				</div>
				<div>
					<label>제목</label>
				</div>
				<div>
					<input type="text" class="com_title height4 border width-85 border-radius-10" id="flea_title" name="flea_title">
				</div>
				<div>
					<label>본문</label>
				</div>
				<div class="content">
					<textarea id="flea_content" class="summernote" name="flea_content"></textarea>    
				</div>
				<div class=" flex justify-center my-top-8">
					<button type="button" class=fleaWriteBtn id="write">등록</button>
					<a href="/fleaMarket/fleaMarketList"  class="fleaWriteBtn text-center" id="cancel">취소</a>
				</div>
			</form>
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