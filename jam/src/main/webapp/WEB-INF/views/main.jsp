<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<script>

</script>
<style>
	.main_container {display : block;}
	.search {	
			display: flex;
    		justify-content: center;
    	}
    	#search_bar {
    		width : 550px; 
    		height : 45px;
    		text-align:center;
		    border: 5px solid #ffb689;
		    border-radius: 30px;
		    font-size: 16px;
		    font-weight: 500;
		    font-color : #ff7130;
    		}
    	.search-icon{
			width : 30px;
			object-fit : contain;
			margin-left : 15px;
		}
		input::placeholder {
  color: #ffb689;
}
</style>
<body>
	<div class="main_container">
		<div class="search my-top-15">
			
			<input type="text" id="search_bar" placeholder="궁금한 내용을 JAM에서 찾아보세요!">
			<img class="search-icon" src="/resources/include/images/search.svg">
		</div>
		<div>
			<h2 class="text-center">메인 페이지~~</h2>
			<a href="/member/joinFinish">회원가입 완료</a>
		</div>
	</div>
	

</body>
</html>