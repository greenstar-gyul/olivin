<script setup>
import { ref, watch } from 'vue';
import InputDataTable from '@/components/common/InputDataTable.vue';
import { convertDate } from '@/utils/dateUtils';

/* Form Data */

// 폼 기본값
const defaultForm = {
  userId: '이창현',
  orderDate: convertDate(new Date()),
};

// 폼 스키마
const formSchema = [
  { type: 'text', label: '발주명', id: 'orderTitle', placeholder: '발주명을 입력하세요.' },
  { type: 'select', label: '발주사유', id: 'reason', placeholder: '발주사유를 선택하세요.',
    options: [
      { name: '옵션1', value: 1 },
      { name: '옵션2', value: 2 }
    ]
  },
  { type: 'data', label: '등록자', id: 'userId', data: 'text' },
  { type: 'date', label: '납기예정일', id: 'dueDate', placeholder: '납기예정일을 선택하세요.' },
  { type: 'item-search', label: '공급업체', id: 'orderFrom', placeholder: '공급업체를 입력하세요.' },
  { type: 'data', label: '발주요청일', id: 'orderDate', data: 'date' },
];


/* Input Table */

// 테이블 기본값
const defaultTable = {
  categroyMain: '화장품',
  price: 15000,
  unit: { name: '옵션1', value: 1 },
};

// 테이블 컬럼 정의
const columns = [
  { inputType: 'item-search', header: '제품명', field: 'productName', placeholder: '제품명을 입력하세요.' },
  { type: 'text', header: '제품분류', field: 'categroyMain' },
  { type: 'number', header: '단가', field: 'price' },
  { inputType: 'number', header: '수량', field: 'quantity', placeholder: '수량을 입력하세요.' },
  { inputType: 'select', header: '단위', field: 'unit', placeholder: '단위를 선택하세요.',
    options: [
      { name: '옵션1', value: 1 },
      { name: '옵션2', value: 2 }
    ]
  }
];

/* modal */

const modalVisible = ref(false);
const modelReturn = ref({});

const modalHeaders = ref([
  { field: 'id', header: 'ID' },
  { field: 'name', header: '회사명' },
  { field: 'category', header: '업종' },
  { field: 'publisher', header: '대표자' },
  { field: 'store', header: '주소' },
  { field: 'size', header: '전화번호' },
  { field: 'quantity', header: '이메일' },
  { field: 'safe', header: '비고' }
]);

const modalItems = ref([
  { id: 1, name: '회사 A', category: 'IT', publisher: '대표 A', store: '서울', size: '규모 A', quantity: 10, safe: '비고 A' },
  { id: 2, name: '회사 B', category: '제조업', publisher: '대표 B', store: '부산', size: '규모 B', quantity: 20, safe: '비고 B' },
  { id: 3, name: '회사 C', category: '서비스업', publisher: '대표 C', store: '대구', size: '규모 C', quantity: 30, safe: '비고 C' }
]);

const closeModal = () => {
  modalVisible.value = false;
};

const confirmModal = (selectedItems) => {
  const modelData = modelReturn.value;
  // modelData.item[modelData.fieldName+"Id"] = selectedItems.id;
  modelData.item[modelData.fieldName] = selectedItems.name;

  modalVisible.value = false; //모달 닫음
  modelReturn.value = {}; //문제가 생길 수 있어서 초기화
};

// 모달이벤트

const formSearch = (item, fieldName) => {
  console.log('form search', item);
  console.log('data', fieldName);
}

const tableSearch = (item, fieldName) => {
  console.log('table search', item);
  console.log('data', fieldName);
  //모달에서 넣어질 데이터 등록
  modelReturn.value = { item, fieldName };
  console.log(modelReturn.value);
  modalVisible.value = true;
}

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
    @formSearch="formSearch" @tableSearch="tableSearch"
    @submit="saveFormHandler" />
  <DialogModal title="테스트 모달 1" :display="modalVisible" :return="modelReturn" :headers="modalHeaders" :items="modalItems" :selectionMode="'single'" @close="closeModal" @confirm="confirmModal" @search-modal="searchModal1"></DialogModal>
</template>
