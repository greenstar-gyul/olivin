<script setup>
import InputDataTable from '@/components/common/InputDataTable.vue';
import { onBeforeMount, ref } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import { useAuth } from '@/composables/useAuth';
import axios from '@/service/axios';

/* Form Data */

const inputRef = ref(null);

// 폼 기본값
const defaultForm = ref({
  orderDate: convertDate(new Date()),
});

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
  { type: 'data', label: '지점명', id: 'orderFrom', data: 'text' },
  { type: 'data', label: '발주요청일', id: 'orderDate', data: 'date' },
];

/* Input Table */

// 테이블 기본값
// const defaultTable = {
//   categoryMain: '화장품',
//   price: 15000,
//   unit: { name: '옵션1', value: 1 },
// };

// 테이블 컬럼 정의
const columns = [
  { inputType: 'item-search', header: '제품명', field: 'productName', placeholder: '제품명을 입력하세요.' },
  { type: 'text', header: '제품분류', field: 'categoryMain' },
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

const itemModalVisible = ref(false);
const itemModalReturn = ref({});
const itemModalItems = ref([]);

const itemModalHeaders = ref([
  { field: 'productName', header: '상품명' },
  { field: 'categoryMain', header: '대분류' },
  { field: 'categorySub', header: '소분류' },
  { field: 'vendorName', header: '공급사명' },
  { field: 'productSpec', header: '규격' },
]);

const getItemModalItems = async (searchValue) => {
  let req;
  if (searchValue) {
    req = await axios.get('/api/search/products', {
      params: {
        searchValue
      }
    });
  } else {
    req = await axios.get('/api/search/products/all');
  }
  return req?.data ? req.data : req;
}

const itemCloseModal = () => {
  itemModalVisible.value = false;
};

const itemConfirmModal = (selectedItems) => {
  const modelData = itemModalReturn.value;
  console.log('selected Item', selectedItems);
  console.log('data', modelData);
  modelData.item[modelData.fieldName+"Id"] = selectedItems.productId;
  modelData.item[modelData.fieldName] = selectedItems.productName;

  modelData.item["categoryMain"] = selectedItems.categoryMain;

  itemModalVisible.value = false; //모달 닫음
  itemModalReturn.value = {}; //문제가 생길 수 있어서 초기화
};

const itemSearchModal = async (searchValue) => {
  itemModalItems.value = await getItemModalItems(searchValue);
}

// 모달 이벤트

const tableSearch = async (item, fieldName) => {
  console.log('table search', item);
  console.log('data', fieldName);
  itemModalReturn.value = { item, fieldName };

  itemModalItems.value = await getItemModalItems();
  itemModalVisible.value = true;
}

// 폼과 테이블 데이터를 저장하는 핸들러
const saveFormHandler = (formData, tableData) => {
  console.log('formData:', formData);
  console.log('tableData:', tableData);
};

const getBranchInfo = async (empId) => {
  const req = await axios.get('/api/search/branch', {
    params: {
      empId: empId
    }
  });
  return req.data;
}

onBeforeMount(async () => {
  const user = useAuth().user;
  defaultForm.value.userId = user.value.empName;
  console.log(user);

  const branchInfo = await getBranchInfo(user.value.employeeId);
  defaultForm.value.orderFrom = branchInfo.compName;
  defaultForm.value.orderFormId = branchInfo.compId;
  console.log(defaultForm.value);
});
</script>
<template>
  <InputDataTable ref="inputRef" title="발주서정보" tableTitle="제품 목록"
    :defaultForm="defaultForm" :formSchema="formSchema"
    :defaultTable="defaultTable" :columns="columns"
    @tableSearch="tableSearch"
    @submit="saveFormHandler" />

    
  <DialogModal title="제품 모달" :selectionMode="'single'"
    :display="itemModalVisible" :return="itemModalReturn"
    :headers="itemModalHeaders" :items="itemModalItems"
    @close="itemCloseModal" @confirm="itemConfirmModal" @search-modal="itemSearchModal" />
</template>
