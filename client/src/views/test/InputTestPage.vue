<script setup>
import InputDataTable from '@/components/common/InputDataTable.vue';

/* Form Data */

// 폼 기본값
const defaultForm = {
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

// 테이블 기본값
const defaultTable = {
  name: '기본',
  price: '100',
  text: 'hello'
};

// 테이블 컬럼 정의
const columns = [
  { inputType: 'item-search', header: '상품명', field: 'name' },
  { inputType: 'date', header: '날짜', field: 'date' },
  { inputType: 'number', header: '가격', field: 'price' },
  { inputType: 'select', header: '콤보박스', field: 'select',
    options: [
      { name: '옵션1', value: 1 },
      { name: '옵션2', value: 2 }
    ]
  },
  { type: 'number', header: '텍스트', field: 'text' }
];


/* Submit */

// 폼과 테이블 데이터를 저장하는 핸들러
const saveFormHandler = (formData, tableData) => {
  console.log('formData:', formData);
  console.log('tableData:', tableData);
};
</script>
<template>
  <InputDataTable title="발주서정보"
    :defaultForm="defaultForm" :formSchema="formSchema"
    :defaultTable="defaultTable" :columns="columns"
    @submit="saveFormHandler" />
</template>