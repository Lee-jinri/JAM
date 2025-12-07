/**
 * JSP에서 <script type="module"> 사용 시 코드 포맷팅 깨져서
 * 모듈 대신 전역 객체(window) 사용함.
 */
window.bigCategory = {
	0: "전체", 1: "기타", 2: "건반악기", 3: "드럼",
	4: "관악기", 5: "현악기", 6: "장비", 7: "그 외"
}
window.subCategories = {
	1: [ ['어쿠스틱기타', 8], ['일렉기타', 9], ['베이스기타', 10], ['통기타', 11] ],
	2: [ ['디지털피아노', 12], ['신디사이저', 13], ['일반 피아노', 14] ],
	3: [ ['전자드럼', 15], ['어쿠스틱드럼', 16], ['심벌', 17], ['하드웨어', 18] ],
	4: [ ['플룻', 19], ['색소폰', 20], ['트럼펫', 21], ['그 외 관악기', 22] ],
	5: [ ['바이올린', 23], ['첼로', 24], ['그 외 현악기', 25] ],
	6: [ ['앰프', 26], ['이펙터', 27], ['오디오인터페이스', 28], ['마이크', 29] ],
	7: [ ['케이스', 30], ['케이블', 31], ['튜너', 32], ['그 외 악세서리', 33] ]
};

// 역방향 Map
window.subMap = {};
for (const big in window.subCategories) {
	for (const [name, num] of window.subCategories[big]) {
		window.subMap[num] = { big: Number(big), name };
	}
}

$(function(){
	$('#mainCategory li').click(function () {
		$('#mainCategory li').removeClass('active');
		$(this).addClass('active');
		
		const mainIndex = $(this).index() + 1;
		const subList = subCategories[mainIndex];
		
		const $subUl = $('#subCategory');
		$subUl.empty(); 
		
		subList.forEach(([name, id]) => {
			$('#subCategory').append("<li data-id=" + id + ">" + name + "</li>");
		});
	});

	$('#subCategory').on('click', 'li', function () {
		$('#subCategory li').removeClass('active');
		$(this).addClass('active');

		const category_id = $(this).data('id');
		$('#category_id').val(category_id);
	});
		
})
	