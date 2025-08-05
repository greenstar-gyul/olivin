<script setup>
import InputDataTable from '@/components/common/InputDataTable.vue';
import { onBeforeMount, onMounted, ref, watch } from 'vue';
import { convertDate, convertDateTime } from '@/utils/dateUtils';
import { useAuth } from '@/composables/useAuth';
import { useConfirm } from 'primevue';
import axios from '@/service/axios';

/* Form Data */

const inputRef = ref(null);
const confirm = useConfirm(); //confirm

// 폼 기본값
const defaultForm = ref({
  soDate: convertDate(new Date()),
  totalPrice: '0원',
});

// 폼 스키마
const formSchema = ref([
  { type: 'data', label: '발주요청일', id: 'soDate', data: 'date' },
  { type: 'select', label: '결제수단', id: 'paymentType', placeholder: '결제수단을 선택하세요.',
    options: [
      { name: '정기 발주', value: 140001 },
      { name: '수요 예측 발주', value: 140002 },
      { name: '재고 부족', value: 140003 },
      { name: '신상품 발주', value: 140004 },
      { name: '긴급 발주', value: 140005 },
      { name: '행사상품 발주', value: 140006 },
    ]
  },
  { type: 'data', label: '지점명', id: 'compName', data: 'text' },
  { type: 'data', label: '등록자', id: 'empName', data: 'text' },
  { type: 'data', label: '총 가격', id: 'totalPrice', data: 'text' },
]);

/* Input Table */

let tableData = ref({});

// 테이블 기본값
const defaultTable = {
  // categoryMain: '화장품',
  // price: 15000,
  unit: 'box',
};

// 테이블 컬럼 정의
const columns = [
  { inputType: 'item-search', header: '제품명', field: 'productName', placeholder: '제품명을 입력하세요.' },
  { type: 'text', header: '제품분류', field: 'categoryMain' },
  { type: 'number', header: '단가', field: 'price' },
  { inputType: 'number', header: '수량', field: 'quantity', placeholder: '수량을 입력하세요.' },
  { type: 'text', header: '단위', field: 'unit' },
  { type: 'number', header: '박스당 수량', field: 'packQty' }
];

/* modal */

// 제품 모달
const itemModalVisible = ref(false);
const itemModalReturn = ref({});
const itemModalItems = ref([]);

const itemModalHeaders = ref([
  { field: 'productName', header: '상품명' },
  { field: 'categoryMain', header: '대분류' },
  { field: 'categorySub', header: '소분류' },
  { field: 'purchasePrice', header: '단가' },
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

const itemConfirmModal = async (selectedItems) => {
  const modalData = itemModalReturn.value;
  // console.log('selected Item', selectedItems);
  console.log('data', modalData);
  const same = modalData.data.filter((e) => {
    return e[modalData.fieldName] == selectedItems.productName;
  });

  if (same.length > 0) {
    // TODO : 다른 alert() 함수를 사용하면 변경
    alert("이미 동일한 제품이 있습니다.");
  } else {
    const product = await axios.get(`/api/products/${selectedItems.productId}`);

    modalData.item[modalData.fieldName] = selectedItems.productName;
    modalData.item['productId'] = selectedItems.productId;
    modalData.item['categoryMain'] = selectedItems.categoryMain;
    modalData.item['price'] = selectedItems.purchasePrice;
    modalData.item['packQty'] = product.data.packQty;
  }

  itemModalVisible.value = false; //모달 닫음
  itemModalReturn.value = {}; //문제가 생길 수 있어서 초기화
};

const itemSearchModal = async (searchValue) => {
  itemModalItems.value = await getItemModalItems(searchValue);
}

// 모달 이벤트

const tableSearch = async (item, fieldName, data) => {
  // console.log('table search', item);
  // console.log('data', fieldName);
  itemModalReturn.value = { item, fieldName, data };

  itemModalItems.value = await getItemModalItems();
  itemModalVisible.value = true;
}

// 폼과 테이블 데이터를 저장하는 핸들러
const saveFormHandler = async (formData, tableData) => {
  console.log('orders:', formData);
  console.log('ordersDetail:', tableData);

  for (const form in formData) {
    if (!formData[form]) {
      if (form == 'note') continue;
      // TODO : 다른 alert() 함수를 사용하면 변경
      alert("폼에 정보에 비어있는 데이터가 있습니다.");
      return;
    }
  }

  for (const table of tableData) {
    for (const data in table) {
      if (!table[data]) {
        // TODO : 다른 alert() 함수를 사용하면 변경
        alert("테이블에 비어있는 데이터가 있습니다.");
        return;
      }
    }
  }

  //본사 정보
  const req = await axios.get(`/api/search/company/head`, {
    params : {
      searchValue: ''
    }
  });
  const headInfo = req.data[0];

  const totalAmount = parseFloat(String(formData.totalAmount).replace(/[,원]/g, ''));
  confirm.require({
    icon: 'pi pi-info-circle',
    header: '발주서를 등록',
    message: '발주서를 등록하시겠습니까?',
    rejectProps: {
      label: '취소',
      severity: 'secondary',
      outlined: true
    },
    acceptProps: {
      label: '저장'
    },
    accept: async () => {
     
    },
    reject: () => {
      return;
    }
  });
};

watch(
  () => tableData.value,
  (newVal) => {
    if (tableData.value.value) {
      let total = 0;
      for (const data of tableData.value.value) {
        const quantity = data.quantity ? data.quantity : 0;
        total += data.price * data.packQty * quantity;
      }
      const formData = inputRef.value.getFormData();
      formData.value.totalAmount = Number(total).toLocaleString()+'원';
    }
  },
  { deep: true }
);

const getBranchInfo = async (empId) => {
  const req = await axios.get('/api/orders/user/compInfo', {
    params: {
      empId: empId
    }
  });

  //지점 정보
  if (req.data.compType == '100002')
    return req.data;
  return undefined;
}

onBeforeMount(async () => {
  const user = useAuth().user;
  defaultForm.value.empId = user.value.employeeId;
  defaultForm.value.empName = user.value.empName;
  // console.log(user);

  const branchInfo = await getBranchInfo(user.value.employeeId);
  if (branchInfo) {
    defaultForm.value.compName = branchInfo.compName;
    defaultForm.value.compId = branchInfo.compId;
  } else {
    // TODO : 다른 alert() 함수를 사용하면 변경
    alert("지점 정보가 없습니다. 관리자에게 문의하세요.");
  }
});

onMounted(() => {
  tableData.value = inputRef.value.getTableData();
});
</script>
<template>
  <ConfirmDialog />
  <InputDataTable ref="inputRef" title="발주서정보" tableTitle="제품 목록"
    :defaultForm="defaultForm" :formSchema="formSchema"
    :defaultTable="defaultTable" :columns="columns"
    @formSearch="formSearch" @tableSearch="tableSearch"
    @submit="saveFormHandler" />

  <DialogModal title="제품 모달" :selectionMode="'single'"
    :display="itemModalVisible" :return="itemModalReturn"
    :headers="itemModalHeaders" :items="itemModalItems"
    @close="itemCloseModal" @confirm="itemConfirmModal" @search-modal="itemSearchModal" />
</template>
