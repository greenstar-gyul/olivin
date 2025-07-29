<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import InputForm from '../inputForm/ApproveInputForm.vue';
import SearchForm from '../inputForm/SearchForm.vue';
import BasicTable from '../table/BasicTable.vue';

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

// ✅ 반려 함수 개선 - 일괄반료와 개별반료 구분
const reject = (source = 'bulk') => {
  console.log(`❌ StandardApproval - 반료 버튼 클릭 (${source})`);
  console.log('📋 현재 formData:', formData.value);
  console.log('🎯 선택된 아이템:', selectedItems.value);
  
  if (!selectedItems.value) {
    alert('반료할 항목을 선택해주세요.');
    return;
  }
  
  // 반료 시 사유 필수 체크
  if (!formData.value.note || formData.value.note.trim() === '') {
    alert('반료 사유를 입력해주세요.');
    return;
  }
  
  // 부모 컴포넌트로 반료 이벤트 전달 (formData + 선택된 아이템 정보)
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

// ✅ InputForm에서 발생하는 승인/반료 이벤트 핸들러
const handleInputFormApprove = (data) => {
  console.log('✅ StandardApproval - InputForm 승인 이벤트:', data);
  // formData 업데이트 후 승인 처리
  formData.value = { ...data };
  approve('individual');
};

const handleInputFormReject = (data) => {
  console.log('❌ StandardApproval - InputForm 반료 이벤트:', data);
  // formData 업데이트 후 반료 처리
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

// ✅ 날짜 포맷 함수 추가
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  try {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    return dateString;
  }
};
</script>

<template>
  <div class="space-y-6">
    <!-- 상단: 검색 조건 -->
    <div class="card p-6">
      <SearchForm 
        :filters="{ title: '승인 요청 조회', filters: props.filters }" 
        @searchData="searchData" 
      />
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
              label="일괄반료" 
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
                      {{ item[key] ? formatDateTime(item[key]) : '-' }}
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