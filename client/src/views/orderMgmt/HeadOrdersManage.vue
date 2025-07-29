<script setup>
import { ref, watch } from 'vue';
import InputDataTable from '@/components/common/InputDataTable.vue';
import { convertDate } from '@/utils/dateUtils';

/* Form Data */

// 폼 기본값
const defaultForm = {
  user_id: '이창현',
  order_date: convertDate(new Date()),
};

// 폼 스키마
const formSchema = [
  { type: 'text', label: '발주명', id: 'order_title', placeholder: '발주명을 입력하세요.' },
  { type: 'select', label: '발주사유', id: 'reason', placeholder: '발주사유를 선택하세요.',
    options: [
      { name: '옵션1', value: 1 },
      { name: '옵션2', value: 2 }
    ]
  },
  { type: 'data', label: '등록자', id: 'user_id', data: 'text' },
  { type: 'date', label: '납기예정일', id: 'due_date', placeholder: '납기예정일을 선택하세요.' },
  { type: 'item-search', label: '공급업체', id: 'order_from', placeholder: '공급업체를 입력하세요.' },
  { type: 'data', label: '발주요청일', id: 'order_date', data: 'date' },
];

//검색 이벤트
const formSearch = () => {
  console.log('form search');
  // 모달창이 나와서 검색할 수 있는 로직을 구현
}

/* Input Table */

// 테이블 기본값
const defaultTable = {
  categroy_main: '화장품',
  price: 15000,
  unit: { name: '옵션1', value: 1 },
};

// 테이블 컬럼 정의
const columns = [
  { inputType: 'item-search', header: '제품명', field: 'product_name', placeholder: '제품명을 입력하세요.' },
  { type: 'text', header: '제품분류', field: 'categroy_main' },
  { type: 'number', header: '단가', field: 'price' },
  { inputType: 'number', header: '수량', field: 'quantity', placeholder: '수량을 입력하세요.' },
  { inputType: 'select', header: '단위', field: 'unit', placeholder: '단위를 선택하세요.',
    options: [
      { name: '옵션1', value: 1 },
      { name: '옵션2', value: 2 }
    ]
  }
];

// 폼과 테이블 데이터를 저장하는 핸들러
const saveFormHandler = (formData, tableData) => {
  console.log('formData:', formData);
  console.log('tableData:', tableData);
};
</script>
<template>
  <InputDataTable title="발주서정보" tableTitle="제품 목록"
    :defaultForm="defaultForm" :formSchema="formSchema"
    :defaultTable="defaultTable" :columns="columns"
    @formSearch="formSearch"
    @submit="saveFormHandler" />
</template>
