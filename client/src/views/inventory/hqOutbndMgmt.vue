<script setup>
import InputMultiTable from '@/components/common/InputMultiTable.vue';
import { onMounted, ref } from 'vue';
import axios from '@/service/axios';


// 테스트 데이터
const items = ref([]);

// 모달의 visible 상태를 관리하는 ref 변수
const testModalVisible = ref(false);
const testModalVisible2 = ref(false);

// 모달창의 테이블 헤더 정보
// field: 테이블의 각 컬럼에 해당하는 데이터의 키
// header: 테이블의 각 컬럼에 해당하는 헤더 이름
const modalHeaders = ref([
  { field: 'orderTitle', header: '발주명' },
  { field: 'userId', header: '등록자' },
  { field: 'orderType', header: '발주유형' },
  { field: 'orderFrom', header: '입고지' },
  { field: 'orderTo', header: '출고지' },
  { field: 'reason', header: '발주사유' },
  { field: 'orderDate', header: '발주일' },
  { field: 'orderStatus', header: '발주상태' },
  { field: 'dueDate', header: '납기예정일' }
]);

// 모달창의 데이터 아이템
const modalItems = ref([
  { orderTitle: '발주1', name: '제품 A', category: '분류 1', publisher: '공급사 A', store: '지점 A', size: '규격 A', quantity: 100, safe: 50 },
  { orderTitle: '발주2', name: '제품 B', category: '분류 2', publisher: '공급사 B', store: '지점 B', size: '규격 B', quantity: 200, safe: 100 },
  { orderTitle: '발주3', name: '제품 C', category: '분류 3', publisher: '공급사 C', store: '지점 C', size: '규격 C', quantity: 300, safe: 150 }
]);

// 모달창 닫기 함수. 필요한 만큼 생성 -> 어떤건지 테스트 필요
const closeModal = () => {
  testModalVisible.value = false;
};

// 모달창 확인 버튼 클릭 시 호출되는 함수
// 필요한 로직 작성
const confirmModal = (selectedItems) => {
  console.log('Selected items from modal:', selectedItems);
  // 필요한 로직 작성
  testModalVisible.value = false;
};

// 검색 모달을 열 때 호출되는 함수
// case 문을 사용하여 모달 이름(item-search 타입의 name을 따름)에 따라 다른 모달을 열 수 있도록 구현
const loadPurchaseOnClick = () => {
  testModalVisible.value = true;
};

const searchModal1 = (searchValue) => {
  console.log('Search modal with value:', searchValue);
  // 검색 로직 구현
};

/* Form Data */
// 폼 기본값
const formData = {};

// 폼 스키마
const formSchema = [
  { type: 'text', label: '출고번호', id: 'outbndNo' },
  { type: 'text', label: '발주명', id: 'orderId' },
  { type: 'text', label: '출고지', id: 'outbndFrom' },
  { type: 'text', label: '입고지', id: 'inbndTo' },
  { type: 'date', label: '출고일', id: 'outbndDate'}, 
  { type: 'text', label: '출고상태', id: 'outbndStatus', 
    // options: [
    //   { name: '옵션1', value: 1 },
    //   { name: '옵션2', value: 2 }
    // ]
  },
];

/* Input Table */
const tableData = ref([]);

const tableHeader = {
  title: '제품 목록',
  header: {
    productName: '제품명',
    orderQuantity: '발주수량',
    totalOutbndQuantity  : '출고수량',
    unit: '단위',
    outbndStatus: '출고상태',
  },
  rightAligned: ['price']
};

/* Input Table Detail */
const detailData = ref([]);

// 테이블 컬럼 정의
const columns = [
  { inputType: 'text', header: 'LOT', field: 'lotNo' },
  { inputType: 'number', header: '출고수량', field: 'outbndQuantity' },
  { inputType: 'text', header: '단위', field: 'unit' },
  { inputType: 'text', header: '출고상태', field: 'outbnStatus' },
];
/* Submit */

// 폼과 테이블 데이터를 저장하는 핸들러
const onRowSelect = (select) => {
  // console.log('Selected Row:', select);
  
  // fetch 사용하는 시간때문에 250ms 지연
  // 실제로는 서버에서 데이터를 가져오는 로직이 필요합니다.
  setTimeout(() => {
    // 선택된 행의 데이터를 폼에 채우기
    // axios나 fetch를 사용하여 서버에서 데이터를 가져올 수 있습니다.
    if (select.id === 1) {
      detailData.value = [
        { id: 1, lot: 'LOT001', quantity: 10, status: '출고대기' },
        { id: 2, lot: 'LOT002', quantity: 20, status: '출고대기' }
      ];
    } else if (select.id === 2) {
      detailData.value = [
        { id: 3, lot: 'LOT003', quantity: 30, status: '출고대기' },
        { id: 4, lot: 'LOT004', quantity: 40, status: '출고완료' }
      ];
    } else {
      detailData.value = [];
    }
  }, 250);
};

const importHandler = () => {
  console.log('데이터 불러오기');
};

const exportHandler = () => {
  console.log('출고 처리');
};



// 테스트 함수
const getSampleData = async () => {
  try {
    const result = await axios.get('/api/hqOutbndMgmt');
    // const data = await result.data;
    tableData.value = await result.data;
    console.log('Loaded sample data:', tableData.value);
    console.log('test data:', result.data);
    // console.log('Loaded sample data:', data);
    // console.log('test:', data[0].inbndNo);
    // console.log('testGGG : ', tableData[0].name);
  
    // items에 데이터를 할당
    // items.value = data;
    // console.log('test items', items.value[0].inbndNo)
    // tableData[0].name = data[0].inbndNo;
  } catch (e) {
    console.error('출고 데이터 불러오기 실패:', e)
  }
};


onMounted(() => {  
  getSampleData();
});


</script>
<template>
  <InputMultiTable title="출고정보"
    :defaultForm="formData"
    :formSchema="formSchema"
    :tableData="tableData"
    :tableHeader="tableHeader"
    :detailData="detailData"
    :detailColumns="columns" :detailCRUD="true"
    @onRowSelect="onRowSelect"
    >
    <template #btn>
      <Button label="발주정보불러오기" class="min-w-fit whitespace-nowrap" severity="info" @click="loadPurchaseOnClick" outlined />
    </template>
    <template #detailBtn>
      <Button label="출고처리" class="min-w-fit whitespace-nowrap" severity="success" @click="exportHandler" outlined />
    </template>
  </InputMultiTable>
  <DialogModal title="테스트 모달" :display="testModalVisible" :headers="modalHeaders" :items="modalItems" :selectionMode="'multiple'" @close="closeModal" @confirm="confirmModal" @search-modal="searchModal1"></DialogModal>
</template>
