<script setup>
import { ref, watch } from 'vue';
import ApproveInputForm from '../inputForm/ApproveInputForm.vue';
import SearchForm from '../inputForm/SearchForm.vue';
import BasicTable from '../table/BasicTable.vue';

// ✅ StandardInput과 동일한 emit 구조
const emit = defineEmits(['searchData', 'saveData', 'approve', 'reject', 'rowSelect', 'rowUnselect']);

const props = defineProps({
  filters: {
    type: Object,  // ✅ Array에서 Object로 변경
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
  scrollHeight: {
    type: String,
    default: '400px'
  },
  checkType: {
    type: String,
    default: 'single'
  }
});

// ✅ StandardInput과 동일한 선택 관리
const selectedItems = ref(null);

const searchFormRef = ref(null);
const approveInputFormRef = ref(null);

// ✅ 세부카테고리 옵션 정의
const categorySubOptions = {
  '110001': [
    { name: '스킨/토너', value: '121001' },
    { name: '에센스/세럼/앰플', value: '121002' },
    { name: '크림', value: '121003' },
    { name: '로션', value: '121004' },
    { name: '미스트/오일', value: '121005' },
    { name: '스킨케어 디바이스', value: '121006' }
  ],
  '110002': [
    { name: '베이스 메이크업', value: '122001' },
    { name: '아이 메이크업', value: '122002' },
    { name: '치크&컨투어', value: '122003' },
    { name: '립 메이크업', value: '122004' },
    { name: '피니시&픽서', value: '122005' },
    { name: '네일 메이크업', value: '122006' }
  ],
  '110003': [
    { name: '클렌징폼/젤', value: '123001' },
    { name: '오일/밤', value: '123002' },
    { name: '워터/밀크', value: '123003' },
    { name: '필링&스크럽', value: '123004' },
    { name: '티슈/패드', value: '123005' },
    { name: '립&아이리무버', value: '123006' },
    { name: '클렌징 디바이스', value: '123007' }
  ],
  '110004': [
    { name: '샴푸/린스', value: '124001' },
    { name: '트리트먼트/팩', value: '124002' },
    { name: '두피앰플/토닉', value: '124003' },
    { name: '헤어에센스', value: '124004' },
    { name: '염색약/펌', value: '124005' },
    { name: '헤어기기/브러시', value: '124006' },
    { name: '스타일링', value: '124007' }
  ],
  '110005': [
    { name: '칫솔', value: '125001' },
    { name: '치약', value: '125002' },
    { name: '애프터구강케어', value: '125003' },
    { name: '구강가전', value: '125004' }
  ],
  '110006': [
    { name: '선크림', value: '126001' },
    { name: '선스틱', value: '126002' },
    { name: '선쿠션', value: '126003' },
    { name: '선스프레이/선패치', value: '126004' },
    { name: '태닝/애프터선', value: '126005' }
  ],
  '110007': [
    { name: '메이크업소품', value: '127001' },
    { name: '아이소품', value: '127002' },
    { name: '스킨케어소품', value: '127003' },
    { name: '헤어소품', value: '127004' },
    { name: '네일/바디소품', value: '127005' },
    { name: '뷰티잡화', value: '127006' }
  ],
  '110008': [
    { name: '비타민', value: '128001' },
    { name: '영양제', value: '128002' },
    { name: '유산균', value: '128003' },
    { name: '슬리밍/이너뷰티', value: '128004' }
  ],
  '110009': [
    { name: '식단관리/이너뷰티', value: '129001' },
    { name: '과자/초콜릿/디저트', value: '129002' },
    { name: '생수/음료/커피', value: '129003' },
    { name: '간편식/요리', value: '129004' },
    { name: '베이비푸드', value: '129005' }
  ]
};

// ✅ 검색 조건 카테고리 변경 처리 함수 - props.filters.filters로 접근
const handleSearchCategoryMainChange = (categoryMainValue) => {
  console.log('승인 페이지 검색 조건 카테고리 변경됨:', categoryMainValue);
  
  // ✅ props.filters.filters로 접근하도록 수정
  const categorySubFilter = props.filters.filters?.find(f => f.name === 'categorySub');
  if (categorySubFilter) {
    categorySubFilter.options = categorySubOptions[categoryMainValue] || [];
    console.log('승인 페이지 검색 조건 세부카테고리 옵션 업데이트됨:', categorySubFilter.options);
  }
  
  // 검색 조건의 현재 선택된 세부카테고리 초기화
  if (searchFormRef.value?.searchOptions) {
    searchFormRef.value.searchOptions.categorySub = '';
  }
};

// ✅ StandardInput과 동일한 이벤트 처리
const searchData = (searchOptions) => {
  // ✅ 검색 조건 카테고리 변경 처리
  if (searchOptions.categoryMain) {
    handleSearchCategoryMainChange(searchOptions.categoryMain);
  }
  
  emit('searchData', searchOptions);
};

const saveData = (inputData) => {
  emit('saveData', inputData);
};

const onRowSelect = (data) => {
  console.log('StandardApproval - 행 선택됨:', data);
  
  if (props.checkType === 'single') {
    selectedItems.value = data;
  } else {
    if (!selectedItems.value) {
      selectedItems.value = [];
    }
    selectedItems.value.push(data);
  }
  
  // 부모 컴포넌트로 이벤트 전달
  emit('rowSelect', data);
};

const onRowUnselect = (data) => {
  if (props.checkType !== 'single') {
    selectedItems.value = selectedItems.value.filter(item => item !== data);
  } else {
    selectedItems.value = null;
  }
  
  // 부모 컴포넌트로 이벤트 전달
  emit('rowUnselect', data);
};

// ✅ 승인/반려 처리 함수
const handleApprove = (approvalData) => {
  emit('approve', approvalData);
};

const handleReject = (rejectionData) => {
  emit('reject', rejectionData);
};

// ✅ StandardInput과 동일하게 ref 노출
defineExpose({
  searchFormRef,
  approveInputFormRef
});
</script>

<template>
  <!-- ✅ StandardInput과 동일한 구조 -->
  <SearchForm 
    ref="searchFormRef" 
    :filters="props.filters" 
    @searchData="searchData" 
  />
  
  <div class="grid grid-cols-7 gap-4 mb-4 items-stretch">
    <!-- ✅ BasicTable 사용 (StandardInput과 동일) -->
    <BasicTable 
      :data="props.items" 
      :header="props.header" 
      :checked="true" 
      :checkType="props.checkType"
      :scrollHeight="props.scrollHeight" 
      @rowSelect="onRowSelect" 
      @rowUnselect="onRowUnselect" 
      class="col-span-4"
    >
      <template #btn>
        <!-- ✅ 일괄 승인/반려 버튼 -->
        <Button 
          label="일괄승인" 
          severity="success" 
          class="min-w-fit whitespace-nowrap" 
          outlined
          :disabled="!selectedItems"
          @click="() => handleApprove({ selectedItem: selectedItems, source: 'bulk' })"
        />
        <Button 
          label="일괄반려" 
          severity="danger" 
          class="min-w-fit whitespace-nowrap" 
          outlined
          :disabled="!selectedItems"
          @click="() => handleReject({ selectedItem: selectedItems, source: 'bulk' })"
        />
      </template>
    </BasicTable>
    
    <!-- ✅ ApproveInputForm 사용 (InputForm과 동일한 위치) -->
    <ApproveInputForm 
      ref="approveInputFormRef" 
      :inputs="props.inputs" 
      @saveData="saveData"
      @approve="handleApprove"
      @reject="handleReject"
      class="col-span-3"
    />
  </div>
</template>

<style scoped>
</style>