<!-- 조회 테스트 페이지 -->
<script setup>
import { onMounted, ref } from 'vue';
import SearchTable from '../../components/common/SearchTable.vue';
import { StockService } from '@/service/StockService';
import { Dialog } from 'primevue';
import AccountTable from './AccountTable.vue';
import SearchForm from '@/components/inputForm/SearchForm.vue';
import axios from 'axios';
import DialogModal from '@/components/overray/DialogModal.vue';

// 조회 폼의 헤더 정보 (조회 테이블 컬럼 이름)
const header = ref({
  title: '거래처원장', // 조회 폼 제묵
  header: { // 테이블의 헤더 정보
    accountLederId: 'ID', 
    plus: '증가', 
    minus: '감소', 
    balance: '잔액', 
    description: '상세', 
    compId: '회사ID', 
    productId: '제품ID', 
    accountId: '계정ID', 
  },
  rightAligned: ['accountId', 'balance'] // 오른쪽 정렬할 칼럼리스트
});

// 조회할 데이터
const items = ref([]);
items.value = StockService.getStockList();

// 검색조건 필터 설정 
const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
  { type: 'select', label: '회계단위', value:'', placeholder: '', name: 'select2', options: [
      { name: '전년도', value: '전기' },
      { name: '올해', value: '당해' }
    ]},
  { type: 'text', label: '계정코드', value: '', placeholder: '', name: 'accountId' },
  { type: 'text', label: '계정과목', value: '', fromPlaceholder: '', name: 'publisher' },
  { type: 'dateRange', label: '회계기간', value: '', fromPlaceholder: '', name: 'publisher' },
  { type: 'text', label: '거래처코드', value: '', placeholder: '', name: 'compId' },
  { type: 'text', label: '거래처명', value: '', placeholder: '', name: 'compName' },
];

// 모달창의 테이블 헤더 정보
// field: 테이블의 각 칼럼에 해당하는 데이터의 키
// header: 테이블의 각 칼럼에 해당하는 헤더 이름
const modalHeaders = ref([
  {field: 'accountLederId', header: 'ID'},
  {field: 'plus', header: '증가'},
  {field: 'minus', header: '감소'},
  {field: 'balance', header: '잔액'},
  {field: 'description', header: '상세'},
]);

// 모달 창의 데이터 아이템
const modalItems = ref([
  { accountLederId:  1, plus:'100', minus: '', balance: '100', description: '', compId: '', productId: 1001, accountId: 100},
  { accountLederId:  2, plus:'200', minus: '', balance: '200', description: '', compId: '', productId: 1002, accountId: 200},
  { accountLederId:  3, plus:'', minus: '300', balance: '300', description: '', compId: '', productId: 1003, accountId: 300},
]);

// 여러 개의 모달창이 필요할 경우 여러 개를 각각 정의
const modalHeaders2 = ref([
  {field: 'accountLederId', header: 'ID'},
  {field: 'plus', header: '증가'},
  {field: 'minus', header: '감소'},
  {field: 'balance', header: '잔액'},
  {field: 'description', header: '상세'},
]);

const modalItems2 = ref([
  { accountLederId:  1, plus:'100', minus: '', balance: '100', description: '', compId: '', productId: 1001, accountId: 100},
  { accountLederId:  2, plus:'200', minus: '', balance: '200', description: '', compId: '', productId: 1002, accountId: 200},
  { accountLederId:  3, plus:'', minus: '300', balance: '300', description: '', compId: '', productId: 1003, accountId: 300},
]);


// 검색 모달이 필요할 때 선언해서 사용.
// 모달의 visible 상태를 관리하는 ref 변수
const testModalVisible = ref(false);
const testModalVisible2 = ref(false);

// 검색 폼에서 검색 버튼 클릭 시 호출되는 함수
const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
};


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


// 필요한 함수 선언
const getSampleData = async() => {
  const result = await axios.get('/api/account');
  const data = await result.data;
  console.log('Sample data:', data);

    items.value = data;
};

// 모달창 닫기 함수. 필요한 만큼 생성
const closeModal = () => {
  testModalVisible.value = false;
}

// 모달창 확인 버튼 클릭 시 호출되는 함수
// 필요한 로직 작성
const confirmModal = (selectedItems) => {
  console.log('Selected items from modal:', selectedItems);
  // 필요한 로직 작성

  testModalVisible2.value = false;
};

const closeModal2 = () => {
  testModalVisible2.value = false;
}

const confirmModal2 = (selectedItems) => {
  console.log('Selected items from modal:', selectedItems);
  testModalVisible2.value = false;
};

onMounted(() => {
  getSampleData();
});


</script>
<template>
  <SearchForm :filters="filters" :items="items" :header="header" @searchData="searchData" @open-search-modal="handleOpenModal"></SearchForm>
  <AccountTable :filters="filters" :items="items" :header="header" @searchData="searchData" @open-search-modal="handleOpenModal"></AccountTable>
  <DialogModal title="테스트 모달 1" :display="testModalVisible" :headers="modalHeaders" :items="modalItems" :selectionMode="'multiple'" @close="closeModal" @confirm="confirmModal"></DialogModal>
  <DialogModal title="테스트 모달 2" :display="testModalVisible2" :headers="modalHeaders2" :items="modalItems2" :selectionMode="'single'" @close="closeModal2" @confirm="confirmModal2"></DialogModal>
</template>