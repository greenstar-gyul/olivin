<!-- 조회 테스트 페이지 -->
<script setup>
import { onMounted, ref } from 'vue';
import SearchTable from '../../components/common/SearchTable.vue';
import { StockService } from '@/service/StockService';
import { Dialog } from 'primevue';
import axios from '@/service/axios';
import DialogModal from '@/components/overray/DialogModal.vue';

// 조회 폼의 헤더 정보 (조회 테이블 컬럼 이름)
const header = ref({
  title: '조회 테스트', // 조회 폼 제목
  header: { // 테이블의 헤더 정보
    productId: 'ID', 
    productName: '제품명', 
    categoryMain: '제품 대분류', 
    categorySub: '제품 소분류', 
    vendorName: '공급사', 
    productSpec: '규격', 
    purchasePrice: '구매가',
    sellPrice: '판매가',
  },
  rightAligned: ['purchasePrice', 'sellPrice'] // 오른쪽 정렬할 컬럼 리스트
});

// 조회할 데이터
const items = ref([]);
items.value = StockService.getStockList();

// 검색 조건 필터 설정
const filters = ref({});
filters.value.title = '조회 조건'; // 검색 조건 폼 제목
filters.value.filters = [ // 검색 조건 필터 목록
  // type: 'text'는 일반 텍스트 입력 필드
  // type: 'dateRange'는 날짜 범위 선택 필드
  // type: 'select'는 드롭다운 선택 필드
  // type: 'item-search'는 아이템 검색 모달을 여는 필드
  // type: 'number'는 숫자 입력 필드
  // type: 'textarea'는 다중 행 텍스트 입력 필드
  // type: 'date'는 단일 날짜 선택 필드
  // label: 필드의 라벨. 사용자에게 보여지는 이름.
  // value: 필드의 초기 값. 특별한 경우가 아니면 일반적으로 빈 문자열.
  // placeholder: 필드에 대한 플레이스홀더 텍스트. 사용자가 입력하기 전에 보여지는 안내 텍스트.
  // name: 필드의 고유 이름. 데이터 바인딩에 사용됨.
  { type: 'text', label: 'ID', value: '', placeholder: '', name: 'id' },
  { type: 'text', label: '제품명', value: '', placeholder: '', name: 'name' },
  { type: 'text', label: '제품분류', value: '', placeholder: '', name: 'category' },
  { type: 'text', label: '공급사', value: '', fromPlaceholder: '', name: 'publisher' },
  { type: 'text', label: '지점', value: '', placeholder: '', name: 'store' },
  { type: 'dateRange', label: '공급사', value: '', fromPlaceholder: '', name: 'publisher' },
  { type: 'dateRange', label: '공급사', value: '', fromPlaceholder: '', name: 'publisher' },
  { type: 'dateRange', label: '공급사', value: '', fromPlaceholder: '', name: 'publisher' },
  { type: 'select', label: '선택', value: '', placeholder: '', name: 'select', options: [
    { name: '옵션 1', value: 'Option 1' },
    { name: '옵션 2', value: 'Option 2' },
    { name: '옵션 3', value: 'Option 3' }
  ] },
  { type: 'item-search', label: '제품 검색', value: '', placeholder: '검색', name: 'dialog' },
  { type: 'item-search', label: '제품 검색2', value: '', placeholder: '검색2', name: 'dialog2' },
];

// 모달창의 테이블 헤더 정보
// field: 테이블의 각 컬럼에 해당하는 데이터의 키
// header: 테이블의 각 컬럼에 해당하는 헤더 이름
const modalHeaders = ref([
  { field: 'id', header: 'ID' },
  { field: 'name', header: '제품명' },
  { field: 'category', header: '제품분류' },
  { field: 'publisher', header: '공급사' },
  { field: 'store', header: '지점' },
  { field: 'size', header: '규격' },
  { field: 'quantity', header: '현재 재고' },
  { field: 'safe', header: '안전 재고' }
]);

// 모달창의 데이터 아이템
const modalItems = ref([
  { id: 1, name: '제품 A', category: '분류 1', publisher: '공급사 A', store: '지점 A', size: '규격 A', quantity: 100, safe: 50 },
  { id: 2, name: '제품 B', category: '분류 2', publisher: '공급사 B', store: '지점 B', size: '규격 B', quantity: 200, safe: 100 },
  { id: 3, name: '제품 C', category: '분류 3', publisher: '공급사 C', store: '지점 C', size: '규격 C', quantity: 300, safe: 150 }
]);

// =====
// 여러개의 모달창이 필요할 경우 여러개를 각각 정의
const modalHeaders2 = ref([
  { field: 'id', header: 'ID' },
  { field: 'name', header: '회사명' },
  { field: 'category', header: '업종' },
  { field: 'publisher', header: '대표자' },
  { field: 'store', header: '주소' },
  { field: 'size', header: '전화번호' },
  { field: 'quantity', header: '이메일' },
  { field: 'safe', header: '비고' }
]);

const modalItems2 = ref([
  { id: 1, name: '회사 A', category: 'IT', publisher: '대표 A', store: '서울', size: '규모 A', quantity: 10, safe: '비고 A' },
  { id: 2, name: '회사 B', category: '제조업', publisher: '대표 B', store: '부산', size: '규모 B', quantity: 20, safe: '비고 B' },
  { id: 3, name: '회사 C', category: '서비스업', publisher: '대표 C', store: '대구', size: '규모 C', quantity: 30, safe: '비고 C' }
]);

// =====

// 검색 모달이 필요할 때 선언해서 사용.
// 모달의 visible 상태를 관리하는 ref 변수
const testModalVisible = ref(false);
const testModalVisible2 = ref(false);

// searchForm의 searchOptions를 참조하는 ref 변수
const searchOptions = ref({});

// 검색 폼에서 검색 버튼 클릭 시 호출되는 함수
const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
};

<<<<<<< HEAD
// 검색 모달을 열 때 호출되는 함수
// case 문을 사용하여 모달 이름(item-search 타입의 name을 따름)에 따라 다른 모달을 열 수 있도록 구현
const handleOpenModal = (filterName) => {
  console.log('Open modal for filter:', filterName);
  switch (filterName) {
    case 'dialog':
      testModalVisible.value = true;
      break;
    case 'dialog2':
      testModalVisible2.value = true;
      break;
    default:
      console.warn('No modal defined for filter:', filterName);
  }
};

// 필요한 함수를 자유롭게 선언하는 공간 ======
const getSampleData = async () => {
  const result = await axios.get('/api/test');
  const data = await result.data;
  console.log('Loaded sample data:', data);

  // items에 데이터를 할당
  items.value = data;
};

// ======

// 모달창 닫기 함수. 필요한 만큼 생성
const closeModal = () => {
  testModalVisible.value = false;
};

// 모달창에서 선택한 아이템을 처리하는 함수
const updateFilterValue = (filterName, selectedItem) => {
  // SearchForm의 searchOptions를 직접 업데이트
  if (searchOptions.value && searchOptions.value.searchFormRef && selectedItem) {
    const displayValue = selectedItem.name || selectedItem.compName || selectedItem.categoryMain || '';
    // SearchForm의 searchOptions에 직접 값 설정
    if (searchOptions.value.searchFormRef.searchOptions) {
      searchOptions.value.searchFormRef.searchOptions[filterName] = displayValue;
    }
  }
};

// 모달창 확인 버튼 클릭 시 호출되는 함수
// 필요한 로직 작성
const confirmModal = (selectedItems) => {
  console.log('Selected items from modal:', selectedItems);
  // 필요한 로직 작성
  testModalVisible2.value = false;
};

const closeModal2 = () => {
  testModalVisible2.value = false;
};

const confirmModal2 = (selectedItems) => {
  console.log('Selected items from modal:', selectedItems);
  // 선택한 값으로 searchOptions 업데이트
  updateFilterValue('dialog2', selectedItems);


  testModalVisible2.value = false;
};

const searchModal1 = (searchValue) => {
  console.log('Search modal with value:', searchValue);
  // 검색 로직 구현
};

const searchModal2 = (searchValue) => {
  console.log('Search modal with value:', searchValue);
  // 검색 로직 구현
};

onMounted(() => {
  getSampleData();
});
=======
function searchData(options) {
  
}
>>>>>>> 5b8755b (거래처원장 2025.07.24)

</script>
<template>
  <SearchTable ref="searchOptions" :filters="filters" :items="items" :header="header" @searchData="searchData" @open-search-modal="handleOpenModal"></SearchTable>
  <DialogModal title="테스트 모달 1" :display="testModalVisible" :headers="modalHeaders" :items="modalItems" :selectionMode="'multiple'" @close="closeModal" @confirm="confirmModal" @search-modal="searchModal1"></DialogModal>
  <DialogModal title="테스트 모달 2" :display="testModalVisible2" :headers="modalHeaders2" :items="modalItems2" :selectionMode="'single'" @close="closeModal2" @confirm="confirmModal2" @search-modal="searchModal2"></DialogModal>
</template>