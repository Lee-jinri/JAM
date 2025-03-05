<!doctype html>
<html lang="ko">
<head>
<script type="text/javascript" src="https://static.nid.naver.com/js/naveridlogin_js_sdk_2.0.2.js" charset="utf-8"></script>
<script type="text/javascript" src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
</head>
<body>
<script>
		var naverLogin = new naver.LoginWithNaverId(
			{
				clientId: "TVknnflYlinxp0rriL8N",
				callbackUrl: "http://localhost:8080/member/naver_login",
				isPopup: false,
				callbackHandle: true
			}
		);

		naverLogin.init();

		window.addEventListener('load', function () {
			naverLogin.getLoginStatus(function (status) {
				if (status) {
            		const requestData = {
            			user_id: naverLogin.user.getId(),
            			user_name: naverLogin.user.getName(),
            			phone: naverLogin.user.getMobile(),
            			email: naverLogin.user.getEmail()
            		};

            		
            		// 사용자 정보로 로그인 시도
            		fetch('http://localhost:8080/api/member/naver_login', {
    					method: 'POST',
    					headers:  {
    					    'Content-Type': 'application/json',
    					},
    					body: JSON.stringify(requestData),
    				}) 
    				.then((response) => {
    					const Authorization = response.headers.get('Authorization');
    				        
    				    if(Authorization != null && Authorization != ""){
    				    	// jwt 토큰 로컬 스토리지에 저장
    				        localStorage.setItem('Authorization', Authorization);
    				        	
    				        // 로그인 이전 페이지로 이동
    				        const prevPage = response.headers.get('prev-page');
    					        
    					       if (prevPage != null && prevPage != "") {
    							// 회원가입에서 로그인으로 넘어온 경우 "/"로 redirect
    							if (prevPage.includes("/member/join")) {
    								$(location).attr('href', "/");
    							} else {
    								$(location).attr('href', prevPage);
    							}
    						} else $(location).attr('href', "/");
    				    }
    				    return null;
    				        
    				})
    				.then((body) => {
    					if(body != null) console.log(body);
    				})
    				.catch((error) => {
    					alert("로그인에 실패했습니다. 잠시 후 다시 시도해주세요.");
    					location.attr("href","/member/login");
    					console.log("error" , error)
    				})
				} else {
					alert("로그인에 실패했습니다. 잠시 후 다시 시도해주세요.");
					console.log("status id false.");
					location.attr("href","/member/login");
				}
			});
		});
	</script>
</body>
</html>