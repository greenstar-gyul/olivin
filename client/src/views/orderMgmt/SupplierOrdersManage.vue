<script setup>
import { onBeforeMount, onMounted, ref, watch } from 'vue';
import InputDataTable from '@/components/common/InputDataTable.vue';
import { convertDate } from '@/utils/dateUtils';
import { useAuth } from '@/composables/useAuth';
import { useConfirm } from 'primevue';
import axios from '@/service/axios';

/* Form Data */

const inputRef = ref(null);
const confirm = useConfirm();

// 폼 기본값
const defaultForm = ref({
  orderDate: convertDate(new Date()),
  totalAmount: '0원',
});

// 폼 스키마
const formSchema = [
  { type: 'text', label: '제안명', id: 'orderTitle', placeholder: '제안명을 입력하세요.' },
  { type: 'select', label: '제안사유', id: 'reason', placeholder: '제안사유를 선택하세요.',
    options: [
      { name: '정기 발주', value: 140001 },
      { name: '수요 예측 발주', value: 140002 },
      { name: '재고 부족', value: 140003 },
      { name: '신상품 발주', value: 140004 },
      { name: '긴급 발주', value: 140005 },
      { name: '행사상품 발주', value: 140006 },
    ]
  },
  { type: 'data', label: '등록자', id: 'creatorName', data: 'text' },
  { type: 'date', label: '납기예정일', id: 'dueDate', placeholder: '납기예정일을 선택하세요.' },
  { type: 'data', label: '공급업체명', id: 'orderFrom', data: 'text' },
  { type: 'text', label: '비고', id: 'remark', placeholder: '비고를 입력하세요.'}, 
  { type: 'data', label: '발주요청일', id: 'orderDate', data: 'date' },
  { type: 'data', label: '총 가격', id: 'totalAmount', data: 'text' }
];

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
  { type: 'text', header: '단위', field: 'unit'},
  { type: 'number', header: '박스당 수량', field: 'packQty' }
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

  if (req?.data) {
    return req.data.filter((e) => {
      return e.vendorName == defaultForm.value.orderFrom;
    });
  } else {
    return req;
  }
}

const itemCloseModal = () => {
  itemModalVisible.value = false;
};

const itemConfirmModal = async (selectedItems) => {
  const modalData = itemModalReturn.value;
  // console.log('selected Item', selectedItems);
  // console.log('data', modelData);
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
  console.log('formData:', formData);
  console.log('tableData:', tableData);

  for (const form in formData) {
    if (!formData[form]) {
      if (form == 'remark') continue;
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
    header: '제안서를 등록',
    message: '제안서를 등록하시겠습니까?',
    rejectProps: {
      label: '취소',
      severity: 'secondary',
      outlined: true
    },
    acceptProps: {
      label: '저장'
    },
    accept: async () => {
      const req = await axios.post('/api/orders', {
        orders: {
          ...formData,
          creatorId: defaultForm.value.creatorId,
          orderType: '150003', //발주 제안
          reason: formData.reason.value+"", //발주 사유
          //수주처 정보
          orderToId: headInfo.compId,
          orderTo: headInfo.compName,
          totalAmount //총 가격
        },
        ordersDetail: tableData.map((e) => {
          return {
            ...e,
            unit: '130004' //'box'
          }
        })
      });
      console.log(req.data);
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

const getSupplierInfo = async (empId) => {
  const req = await axios.get('/api/orders/user/compInfo', {
    params: {
      empId: empId
    }
  });

  //공급업체 정보
  if (req.data.compType == '100003')
    return req.data;
  return {};
}

onBeforeMount(async () => {
  const dueDate = new Date();
  dueDate.setDate(dueDate.getDate() + 8);
  defaultForm.value.dueDate = convertDate(dueDate);

  const user = useAuth().user;
  defaultForm.value.creatorId = user.value.employeeId;
  defaultForm.value.creatorName = user.value.empName;

  const supplierInfo = await getSupplierInfo(user.value.employeeId);
  defaultForm.value.orderFrom = supplierInfo.compName;
  defaultForm.value.orderFromId = supplierInfo.compId;
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
    @tableSearch="tableSearch"
    @submit="saveFormHandler" />
    
  <DialogModal title="제품 모달" :selectionMode="'single'"
    :display="itemModalVisible" :return="itemModalReturn"
    :headers="itemModalHeaders" :items="itemModalItems"
    @close="itemCloseModal" @confirm="itemConfirmModal" @search-modal="itemSearchModal" />
</template>
