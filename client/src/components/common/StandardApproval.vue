<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import InputForm from '../inputForm/ApproveInputForm.vue';
import SearchForm from '../inputForm/SearchForm.vue';
import BasicTable from '../table/BasicTable.vue';
import Button from 'primevue/button';

const emit = defineEmits([
  'searchData', 
  'approve', 
  'reject', 
  'rowSelect', 
  'saveData'
]); 

const props = defineProps({
  filters: {
    type: Array,
    required: true
  },
  items: {
    type: Array,
    default: () => []
  },
  header: {
    type: Object,
    required: true
  },
  inputs: {
    type: Object,
    required: true
  },
  checkType: {
    type: String,
    default: 'single'
  }
});

// ✅ 세부카테고리 옵션 정의 (ProductStandardPage.vue와 동일)
const categorySubOptions = {
  '110001': [ // 스킨케어
    { name: '스킨/토너', value: '121001' },
    { name: '에센스/세럼/앰플', value: '121002' },
    { name: '크림', value: '121003' },
    { name: '로션', value: '121004' },
    { name: '미스트/오일', value: '121005' },
    { name: '스킨케어 디바이스', value: '121006' }
  ],
  '110002': [ // 메이크업
    { name: '베이스 메이크업', value: '122001' },
    { name: '아이 메이크업', value: '122002' },
    { name: '치크&컨투어', value: '122003' },
    { name: '립 메이크업', value: '122004' },
    { name: '피니시&픽서', value: '122005' },
    { name: '네일 메이크업', value: '122006' }
  ],
  '110003': [ // 클렌징
    { name: '클렌징폼/젤', value: '123001' },
    { name: '오일/밤', value: '123002' },
    { name: '워터/밀크', value: '123003' },
    { name: '필링&스크럽', value: '123004' },
    { name: '티슈/패드', value: '123005' },
    { name: '립&아이리무버', value: '123006' },
    { name: '클렌징 디바이스', value: '123007' }
  ],
  '110004': [ // 헤어케어
    { name: '샴푸/린스', value: '124001' },
    { name: '트리트먼트/팩', value: '124002' },
    { name: '두피앰플/토닉', value: '124003' },
    { name: '헤어에센스', value: '124004' },
    { name: '염색약/펌', value: '124005' },
    { name: '헤어기기/브러시', value: '124006' },
    { name: '스타일링', value: '124007' }
  ],
  '110005': [ // 구강용품
    { name: '칫솔', value: '125001' },
    { name: '치약', value: '125002' },
    { name: '애프터구강케어', value: '125003' },
    { name: '구강가전', value: '125004' }
  ],
  '110006': [ // 선케어
    { name: '선크림', value: '126001' },
    { name: '선스틱', value: '126002' },
    { name: '선쿠션', value: '126003' },
    { name: '선스프레이/선패치', value: '126004' },
    { name: '태닝/애프터선', value: '126005' }
  ],
  '110007': [ // 뷰티소품
    { name: '메이크업소품', value: '127001' },
    { name: '아이소품', value: '127002' },
    { name: '스킨케어소품', value: '127003' },
    { name: '헤어소품', value: '127004' },
    { name: '네일/바디소품', value: '127005' },
    { name: '뷰티잡화', value: '127006' }
  ],
  '110008': [ // 건강/기능 식품
    { name: '비타민', value: '128001' },
    { name: '영양제', value: '128002' },
    { name: '유산균', value: '128003' },
    { name: '슬리밍/이너뷰티', value: '128004' }
  ],
  '110009': [ // 푸드
    { name: '식단관리/이너뷰티', value: '129001' },
    { name: '과자/초콜릿/디저트', value: '129002' },
    { name: '생수/음료/커피', value: '129003' },
    { name: '간편식/요리', value: '129004' },
    { name: '베이비푸드', value: '129005' }
  ]
};

// ✅ 조회 조건의 카테고리에 따른 세부카테고리 옵션 (ProductStandardPage.vue와 동일)
const filteredSearchCategorySubOptions = computed(() => {
  const categoryMainFilter = props.filters.find(f => f.name === 'categoryMain');
  const selectedMainCategory = categoryMainFilter?.value;
  
  console.log('StandardApproval - filteredSearchCategorySubOptions computed 실행됨:', selectedMainCategory);
  
  if (!selectedMainCategory) {
    return [];
  }
  
  const subOptions = categorySubOptions[selectedMainCategory] || [];
  console.log('StandardApproval - 조회용 세부카테고리 옵션:', subOptions);
  
  return subOptions;
});

// ✅ 조회 조건의 카테고리 변경 시 세부카테고리 초기화 (ProductStandardPage.vue와 동일)
const onSearchCategoryMainChange = (selectedCategoryMain) => {
  console.log('StandardApproval - 조회 카테고리 변경됨:', selectedCategoryMain);
  
  const categorySubFilter = props.filters.find(f => f.name === 'categorySub');
  if (categorySubFilter) {
    // 세부카테고리 값 초기화
    categorySubFilter.value = '';
    
    console.log('StandardApproval - 세부카테고리 값 초기화됨');
    console.log('StandardApproval - 사용 가능한 세부카테고리 옵션:', categorySubOptions[selectedCategoryMain] || []);
  }
};

// ✅ 단일 선택된 아이템만 관리 (라디오 버튼용) - productId 기반으로 관리
const selectedItems = ref(null);
const formData = ref({}); 

// ✅ 현재 선택된 productId만 추적
const selectedProductId = ref(null);

console.log('🏗️ StandardApproval 컴포넌트 초기화');

const searchData = (searchOptions) => {
  console.log('🔍 StandardApproval - searchData 호출:', searchOptions);
  emit('searchData', searchOptions);
};

// ✅ saveData 함수 개선 - InputForm에서 받은 데이터를 부모로 전달
const saveData = (inputData) => {
  console.log('💾 StandardApproval - InputForm에서 데이터 받음:', inputData);
  
  // formData 업데이트
  formData.value = { ...inputData };
  
  // 부모 컴포넌트로 전달
  emit('saveData', inputData);
};

// ✅ 승인 함수 개선 - 일괄승인과 개별승인 구분
const approve = (source = 'bulk') => {
  console.log(`✅ StandardApproval - 승인 버튼 클릭 (${source})`);
  console.log('📋 현재 formData:', formData.value);
  console.log('🎯 선택된 아이템:', selectedItems.value);
  
  if (!selectedItems.value) {
    alert('승인할 항목을 선택해주세요.');
    return;
  }
  
  // 부모 컴포넌트로 승인 이벤트 전달 (formData + 선택된 아이템 정보)
  const approvalData = {
    ...formData.value,
    selectedItem: selectedItems.value,
    source: source
  };
  
  emit('approve', approvalData);
};

// ✅ 반려 함수 개선 - 일괄반려와 개별반려 구분
const reject = (source = 'bulk') => {
  console.log(`❌ StandardApproval - 반려 버튼 클릭 (${source})`);
  console.log('📋 현재 formData:', formData.value);
  console.log('🎯 선택된 아이템:', selectedItems.value);
  
  if (!selectedItems.value) {
    alert('반려할 항목을 선택해주세요.');
    return;
  }
  
  // 반려 시 사유 필수 체크
  if (!formData.value.note || formData.value.note.trim() === '') {
    alert('반려 사유를 입력해주세요.');
    return;
  }
  
  // 부모 컴포넌트로 반려 이벤트 전달 (formData + 선택된 아이템 정보)
  const rejectionData = {
    ...formData.value,
    selectedItem: selectedItems.value,
    source: source
  };
  
  emit('reject', rejectionData);
};

// ✅ 행 선택 처리 함수 - 스크롤 위치 보존하면서 단일 선택
const onRowSelect = (data) => {
  console.log('🎯 StandardApproval - 행 선택됨:', data);
  
  if (!data || !data.productId) {
    console.log('❌ 유효하지 않은 데이터');
    return;
  }
  
  // 중복 선택 방지
  if (selectedProductId.value === data.productId) {
    console.log('🔄 이미 선택된 동일한 제품:', data.productId);
    return;
  }
  
  // 현재 스크롤 위치 저장
  const scrollContainer = document.querySelector('.p-datatable-scrollable-body');
  const currentScrollTop = scrollContainer?.scrollTop || 0;
  
  // 새로운 선택
  selectedProductId.value = data.productId;
  selectedItems.value = data;
  
  console.log('✅ 새로운 제품 선택됨:', data.productId);
  
  // DOM 직접 조작으로 라디오 버튼 강제 제어 (스크롤 위치 보존)
  nextTick(() => {
    forceRadioSelection(data.productId);
    
    // 스크롤 위치 복원
    if (scrollContainer && currentScrollTop > 0) {
      scrollContainer.scrollTop = currentScrollTop;
    }
  });
  
  // 부모 컴포넌트로 rowSelect 이벤트 전달
  emit('rowSelect', data);
};

// ✅ DOM 직접 조작으로 라디오 버튼 단일 선택 강제
const forceRadioSelection = (targetProductId) => {
  console.log('🔧 DOM 조작으로 라디오 버튼 강제 제어:', targetProductId);
  
  try {
    // 모든 테이블 행 찾기
    const tableRows = document.querySelectorAll('.p-datatable-tbody tr');
    
    tableRows.forEach((row, index) => {
      // 각 행에서 productId 찾기 (첫 번째 데이터 셀)
      const productIdCell = row.querySelector('td:nth-child(2)'); // 라디오 버튼 다음 컬럼
      const productId = productIdCell?.textContent?.trim();
      
      // 라디오 버튼과 행 선택 상태 제어
      const radioInput = row.querySelector('.p-radiobutton input');
      const radioButton = row.querySelector('.p-radiobutton');
      
      if (productId === targetProductId) {
        // 선택된 제품: 라디오 버튼 체크 및 행 하이라이트
        if (radioInput) radioInput.checked = true;
        if (radioButton) radioButton.classList.add('p-radiobutton-checked');
        row.classList.add('p-datatable-row-selected');
        row.setAttribute('aria-selected', 'true');
        console.log('✅ 라디오 버튼 선택됨:', productId);
      } else {
        // 다른 제품들: 라디오 버튼 해제 및 행 하이라이트 제거
        if (radioInput) radioInput.checked = false;
        if (radioButton) radioButton.classList.remove('p-radiobutton-checked');
        row.classList.remove('p-datatable-row-selected');
        row.setAttribute('aria-selected', 'false');
      }
    });
    
    console.log('🎯 DOM 조작 완료 - 선택된 제품:', targetProductId);
  } catch (error) {
    console.error('❌ DOM 조작 실패:', error);
  }
};

// ✅ 선택 해제 처리 함수
const onRowUnselect = (data) => {
  console.log('🔄 StandardApproval - 행 선택 해제됨:', data);
  
  // 현재 선택된 제품과 일치하는 경우에만 해제
  if (data && data.productId === selectedProductId.value) {
    selectedProductId.value = null;
    selectedItems.value = null;
    
    console.log('📝 선택 상태 해제됨');
    
    // DOM에서도 모든 선택 해제
    nextTick(() => {
      clearAllRadioSelections();
    });
    
    // 선택 해제 시에도 부모에게 알림
    emit('rowSelect', null);
  }
};

// ✅ 모든 라디오 버튼 선택 해제
const clearAllRadioSelections = () => {
  console.log('🔧 모든 라디오 버튼 선택 해제');
  
  try {
    const tableRows = document.querySelectorAll('.p-datatable-tbody tr');
    
    tableRows.forEach(row => {
      const radioInput = row.querySelector('.p-radiobutton input');
      const radioButton = row.querySelector('.p-radiobutton');
      
      if (radioInput) radioInput.checked = false;
      if (radioButton) radioButton.classList.remove('p-radiobutton-checked');
      row.classList.remove('p-datatable-row-selected');
      row.setAttribute('aria-selected', 'false');
    });
  } catch (error) {
    console.error('❌ 라디오 버튼 해제 실패:', error);
  }
};

// ✅ 테이블 이벤트 핸들러 통합 - 라디오 버튼 방식
const handleTableEvent = (data) => {
  console.log('🖱️ StandardApproval - 테이블 이벤트:', data);
  
  // 라디오 버튼처럼 클릭하면 해당 아이템 선택
  onRowSelect(data);
};

// ✅ InputForm에서 발생하는 승인/반려 이벤트 핸들러
const handleInputFormApprove = (data) => {
  console.log('✅ StandardApproval - InputForm 승인 이벤트:', data);
  // formData 업데이트 후 승인 처리
  formData.value = { ...data };
  approve('individual');
};

const handleInputFormReject = (data) => {
  console.log('❌ StandardApproval - InputForm 반려 이벤트:', data);
  // formData 업데이트 후 반려 처리
  formData.value = { ...data };
  reject('individual');
};

// ✅ props.items가 변경될 때 선택 상태 초기화
watch(() => props.items, () => {
  selectedItems.value = null;
  selectedProductId.value = null;
  console.log('📋 아이템 목록 변경으로 선택 상태 초기화');
  
  // DOM에서도 모든 선택 해제
  nextTick(() => {
    clearAllRadioSelections();
  });
});

// ✅ 날짜 포맷 함수 수정 - 시간 표시 제거
const formatDate = (dateString) => {
  if (!dateString) return '';
  try {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    
    return `${year}.${month}.${day}`;
  } catch (error) {
    console.error('날짜 포맷 오류:', error);
    return dateString;
  }
};

// ✅ 날짜시간 포맷 함수 (필요시 사용)
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  try {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}.${month}.${day} ${hours}:${minutes}`;
  } catch (error) {
    console.error('날짜시간 포맷 오류:', error);
    return dateString;
  }
};
</script>

<template>
  <div class="space-y-6">
    <!-- 상단: 검색 조건 - ProductStandardPage.vue와 동일하게 구현 -->
    <div class="card p-6">
      <div class="font-semibold text-xl mb-4">승인 요청 조회</div>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 mb-4">
        <div v-for="filter in props.filters" :key="filter.name" class="flex flex-col">
          <label class="block text-sm font-medium mb-2">{{ filter.label }}</label>
          
          <!-- 텍스트/숫자 입력 -->
          <input
            v-if="filter.type === 'text' || filter.type === 'number'"
            v-model="filter.value"
            :type="filter.type"
            :placeholder="filter.placeholder"
            class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          
          <!-- 셀렉트 박스 - ProductStandardPage.vue와 동일 -->
          <select
            v-else-if="filter.type === 'select'"
            v-model="filter.value"
            @change="filter.name === 'categoryMain' ? onSearchCategoryMainChange(filter.value) : null"
            class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">{{ filter.placeholder }}</option>
            <option 
              v-for="option in filter.name === 'categorySub' ? filteredSearchCategorySubOptions : filter.options" 
              :key="option.value" 
              :value="option.value"
            >
              {{ option.name }}
            </option>
          </select>
          
          <!-- 날짜 범위 -->
          <div v-else-if="filter.type === 'dateRange'" class="flex gap-2">
            <input
              v-model="filter.value[0]"
              type="date"
              class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 flex-1"
            />
            <span class="self-center">~</span>
            <input
              v-model="filter.value[1]"
              type="date"
              class="p-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 flex-1"
            />
          </div>
        </div>
      </div>
      
      <div class="flex justify-center gap-3">
        <Button 
          label="초기화" 
          @click="() => {
            props.filters.forEach(filter => {
              if (filter.type === 'dateRange') {
                filter.value = ['', ''];
              } else {
                filter.value = '';
              }
            });
            // 초기화 후 검색 실행
            const searchOptions = {};
            props.filters.forEach(filter => {
              if (filter.type === 'dateRange') {
                searchOptions[filter.name] = filter.value;
              } else {
                searchOptions[filter.name] = filter.value;
              }
            });
            searchData(searchOptions);
          }"
          severity="secondary"
        />
        <Button 
          label="조회" 
          @click="() => {
            const searchOptions = {};
            props.filters.forEach(filter => {
              if (filter.type === 'dateRange') {
                searchOptions[filter.name] = filter.value;
              } else {
                searchOptions[filter.name] = filter.value;
              }
            });
            searchData(searchOptions);
          }"
          severity="success"
        />
      </div>
    </div>
    
    <!-- 하단: 좌우 분할 -->
    <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <!-- 좌측: 제품 목록 -->
      <div class="card p-6">
        <div class="font-semibold text-xl mb-4 flex justify-between">
          <div>{{ header.title }}</div>
          <div class="flex items-center gap-2 flex-nowrap">
            <Button 
              label="일괄승인" 
              severity="success" 
              class="min-w-fit whitespace-nowrap" 
              outlined 
              @click="() => approve('bulk')"
            />
            <Button 
              label="일괄반려" 
              severity="danger" 
              class="min-w-fit whitespace-nowrap" 
              outlined 
              @click="() => reject('bulk')"
            />
          </div>
        </div>
        <div class="overflow-x-auto">
          <div class="min-w-max">
            <table class="w-full border-collapse border border-gray-300">
              <thead>
                <tr class="bg-gray-100">
                  <th class="border border-gray-300 p-2 text-center sticky left-0 bg-gray-100 z-10 min-w-[60px]">선택</th>
                  <th v-for="(headerText, key) in header.header" :key="key" 
                      class="border border-gray-300 p-2 text-center whitespace-nowrap min-w-[100px]">
                    {{ headerText }}
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in props.items" :key="item.productId" 
                    class="hover:bg-gray-50 cursor-pointer" 
                    @click="handleTableEvent(item)">
                  <td class="border border-gray-300 p-2 text-center sticky left-0 bg-white z-10">
                    <input 
                      type="radio" 
                      :name="'product-select'" 
                      :value="item.productId" 
                      :checked="selectedProductId === item.productId"
                      @change="onRowSelect(item)"
                    />
                  </td>
                  <td v-for="(headerText, key) in header.header" :key="key" 
                      class="border border-gray-300 p-2 whitespace-nowrap"
                      :class="header.rightAligned?.includes(key) ? 'text-right' : 'text-left'">
                    <span v-if="key === 'note' && item[key]" 
                          class="inline-block max-w-[200px] truncate" 
                          :title="item[key]">
                      {{ item[key] }}
                    </span>
                    <span v-else-if="key === 'purchasePrice' || key === 'sellPrice'">
                      {{ item[key] ? item[key].toLocaleString() : '' }}원
                    </span>
                    <span v-else-if="key === 'regDate' || key === 'updateDate'">
                      {{ item[key] ? formatDate(item[key]) : '-' }}
                    </span>
                    <span v-else-if="key === 'updateUser'">
                      {{ item[key] || '-' }}
                    </span>
                    <span v-else>
                      {{ item[key] || '' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      
      <!-- 우측: 승인/반려 폼 -->
      <div class="card p-6">
        <InputForm 
          :inputs="props.inputs" 
          @saveData="saveData"
          @approve="handleInputFormApprove"
          @reject="handleInputFormReject"
        />
      </div>
    </div>
  </div>
</template>