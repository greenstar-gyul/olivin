<script setup>
import InputMultiTable from '@/components/common/InputMultiTable.vue';
import { ref } from 'vue';

/* Form Data */

// 폼 기본값
const formData = {
  data: 0.11,
};

// 폼 스키마
const formSchema = [
  { type: 'item-search', label: '상품명', id: 'itemName' },
  { type: 'text', label: '상품설명', id: 'description' },
  { type: 'date', label: '날짜', id: 'date' },
  { type: 'number-float', label: '가격', id: 'price' },
  { type: 'select', label: '콤보박스', id: 'select', 
    options: [
      { name: '옵션1', value: 1 },
      { name: '옵션2', value: 2 }
    ]
  },
  { type: 'data', label: '데이터', id: 'data', data: 'number' },
];

/* Input Table */

const tableData = [
  { id: 1, name: '상품1', price: 100, date: '2023-01-01', select: 1, text: '안녕하세요' },
  { id: 2, name: '상품2', price: 200, date: '2023-01-02', select: 2, text: '안녕하세요' }
];

const tableHeader = {
  title: '제품 목록',
  header: {
    id: 'ID',
    name: '상품명',
    date: '날짜',
    select: '선택',
    price: '가격',
    text: '설명'
  },
  rightAligned: ['price']
};

/* Input Table Detail */
const detailData = ref([]);

// 테이블 컬럼 정의
const columns = [
  { inputType: 'text', header: 'LOT', field: 'lot' },
  { inputType: 'number', header: '출고수량', field: 'quantity' },
  { inputType: 'text', header: '출고상태', field: 'status' },
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
</script>
<template>
  <InputMultiTable title="출고정보"
    :defaultForm="formData" :formSchema="formSchema"
    :tableData="tableData" :tableHeader="tableHeader"
    :detailData="detailData" :detailColumns="columns" :detailCRUD="true"
    @onRowSelect="onRowSelect">
    <template #btn>
      <Button label="불러오기" class="min-w-fit whitespace-nowrap" severity="info" @click="importHandler" outlined />
    </template>
    <template #detailBtn>
      <Button label="출고처리" class="min-w-fit whitespace-nowrap" severity="success" @click="exportHandler" outlined />
    </template>
  </InputMultiTable>
</template>
