let selectedCity;
let selectedGu;
let selectedDong;

$(function() {

	$(".city").click(function() {
		let cityName = this.dataset.city;
		showGuList(cityName);
		selectedCity = cityName;
		updateSelectedArea(cityName, '', '');

		$(".city").css("background-color", "#fff");  
		$(this).css("background-color", "#f0f5ff");  
	})
	
		
	$(document).on("click", ".gu", function() {
	    $(".gu").css("background-color", "#fff");  
	    $(this).css("background-color", "#f0f5ff");
	});

	
	$(document).on("click", ".dong", function() {
	    $(".dong").css("background-color", "#fff");  
	    $(this).css("background-color", "#f0f5ff");
	});

	$('#citySelect').change(function() {
        const selectedOption = $('#citySelect option:selected');
        const cityName = selectedOption.data('city');
        if (cityName) {
            selectedOption.trigger('click');  // 기존 .city 클릭 이벤트 강제 호출
        } else {
            // 시 선택 해제 시 구/동 초기화
            $('#guList').empty();
            $('#dongList').html('<li class="placeholder">동 선택</li>');
        }
    });
});

let locationData = {};  // JSON 데이터를 담아둘 전역 변수


// 1. JSON 파일 로드 (페이지 로드 시)
async function loadLocationData() {
	try {
		const response = await fetch('/resources/include/dist/json/locationData.json');
		locationData = await response.json();
	} catch (error) {
		console.error("지역 데이터 로드 실패:", error);
		alert("지역 데이터를 불러오는데 실패했습니다.");
	}
}

// 2. 시 클릭 시 해당 시의 구 리스트 표시
function showGuList(cityName) {
	
    const guListEl = document.getElementById('guList');
    const dongListEl = document.getElementById('dongList');

	guListEl.innerHTML = '';  // 초기화
    dongListEl.innerHTML = '<li class="placeholder">동 선택</li>';  // 동 초기화

    // '전체' li 생성해서 클릭 이벤트 걸기
    const guAllLi = document.createElement('li');
    //guAllLi.classList.add('gu-all', 'cursor-pointer');
    guAllLi.textContent = '전체';
    guAllLi.classList.add('gu');
    guAllLi.onclick = () => {
        updateSelectedArea(cityName, 'all', '');
        
        document.getElementById('gu').value = '';
        document.getElementById('dong').value = '';
    };
    guListEl.appendChild(guAllLi);
    
    if (locationData[cityName]) {
        const guList = Object.keys(locationData[cityName]);
        guList.forEach(gu => {
            const li = document.createElement('li');
            li.textContent = gu;
            li.dataset.gu = gu;
            li.classList.add('cursor-pointer');
            li.classList.add('gu');
            li.onclick = () => {
			    showDongList(cityName, gu);
			    updateSelectedArea(cityName, gu);
			};
            guListEl.appendChild(li);
        });
    }
}

// 3. 구 클릭 시 해당 구의 동 리스트 표시
function showDongList(cityName, guName) {
	
	// '전체' li 생성해서 클릭 이벤트 걸기
	const dongListEl = document.getElementById('dongList');
    const dongAllLi = document.createElement('li');
    
    dongListEl.innerHTML = '';
    
    dongAllLi.classList.add('dong');
    dongAllLi.textContent = '전체';
    dongAllLi.onclick = () => {
        updateSelectedArea(cityName, guName, 'all');
    };
    dongListEl.appendChild(dongAllLi);
	

    if (locationData[cityName] && locationData[cityName][guName]) {
        const dongList = locationData[cityName][guName];
        dongList.forEach(dong => {
            const li = document.createElement('li');
            li.textContent = dong;
            li.dataset.dong = dong;
            li.classList.add('cursor-pointer');
            li.classList.add('dong');
            li.onclick = () => updateSelectedArea(cityName, guName, dong);
            dongListEl.appendChild(li);
        });
    }
}

function updateSelectedArea(selectedCity, selectedGu, selectedDong){
    let selectedArea = selectedCity;
    
    document.getElementById('city').value = selectedCity;
    document.getElementById('gu').value = '';
    document.getElementById('dong').value = '';

    if (selectedGu === 'all') {
        selectedArea += ' > 전체';
    } else {
		if(selectedGu){
			document.getElementById('gu').value = selectedGu;
	        selectedArea += ' > ' + selectedGu;
	
	        if (selectedDong === 'all') {
	            selectedArea += ' > 전체';
	        } else if (selectedDong) {
	            document.getElementById('dong').value = selectedDong;
	            selectedArea += ' > ' + selectedDong;
	        }
		}
    }

    // 선택된 지역 표시 업데이트
    $("#selectedArea").html(selectedArea);

    // 기존에 있던 삭제 버튼 제거 (중복 방지)
    $(".area-remove-btn").remove();

    // 새로운 x버튼 추가
    const removeBtn = $("<button>")
        .addClass("area-remove-btn")
        .html('<i class="fa-solid fa-x"></i>')
        .on("click", function() {
            removeArea();
        });

    $("#selectedAreaWrapper").append(removeBtn);
}

function removeArea(){
    // hidden 값 초기화
    $("#city").val('');
    $("#gu").val('');
    $("#dong").val('');

    // 선택된 지역 표시 초기화
    $("#selectedArea").html('');

    // x버튼도 삭제
    $(".area-remove-btn").remove();
}

// 페이지 로드 시 JSON 데이터 로드
loadLocationData();
