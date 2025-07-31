<script setup>
import InputDataTable from '@/components/common/InputDataTable.vue';
import { onBeforeMount, ref } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import { useAuth } from '@/composables/useAuth';
import { useConfirm } from 'primevue';
import axios from '@/service/axios';

/* Form Data */

const inputRef = ref(null);
const confirm = useConfirm(); //confirm

// 폼 기본값
const defaultForm = ref({
  orderDate: convertDate(new Date()),
});

// 폼 스키마
const formSchema = ref([
  { type: 'text', label: '발주명', id: 'orderTitle', placeholder: '발주명을 입력하세요.' },
  { type: 'select', label: '발주사유', id: 'reason', placeholder: '발주사유를 선택하세요.',
    options: [
      { name: '140001', value: 140001 },
      { name: '140002', value: 140002 },
      { name: '140003', value: 140003 },
      { name: '140004', value: 140004 },
      { name: '140005', value: 140005 },
      { name: '140006', value: 140006 },
    ]
  },
  { type: 'data', label: '등록자', id: 'userId', data: 'text' },
  { type: 'date', label: '납기예정일', id: 'dueDate', placeholder: '납기예정일을 선택하세요.' },
  { type: 'data', label: '지점명', id: 'orderFrom', data: 'text' },
  { type: 'data', label: '발주요청일', id: 'orderDate', data: 'date' },
]);

// TODO : 폼 총 가격 출력

/* Input Table */

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
  { type: 'text', header: '단위', field: 'unit' }
];

/* modal */

// 지점 모달
const branchModalVisible = ref(false);
const branchModalReturn = ref({});
const branchModalItems = ref([]);

const branchModalHeaders = ref([
  { field: 'compName', header: '회사명' },
  { field: 'ceoName', header: '대표자' },
  { field: 'phone', header: '전화번호' },
  { field: 'address', header: '주소' },
  { field: 'settleMgr', header: '매니저' }
]);

const getBranchModalItems = async (searchValue) => {
  const req = await axios.get('/api/search/company/branch', {
      params: {
        searchValue
      }
    }
  );
  return req.data;
}

const branchCloseModal = () => {
  branchModalVisible.value = false;
}

const branchConfirmModal = (selectedItems) => {
  const modelData = branchModalReturn.value;
  //console.log('selected Item', selectedItems);
  //console.log('data', modelData);
  modelData.item[modelData.fieldName+"Id"] = selectedItems.compId;
  modelData.item[modelData.fieldName] = selectedItems.compName;

  branchModalVisible.value = false; //모달 닫음
  inputRef.value.resetTableHandler();
}

const branchSearchModal = async (searchValue) => {
  branchModalItems.value = await getBranchModalItems(searchValue);
}

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
  // console.log('data', modalData);
  const same = modalData.data.filter((e) => {
    return e[modalData.fieldName] == selectedItems.productName;
  });

  if (same.length > 0) {
    // TODO : 다른 alert() 함수를 사용하면 변경
    alert("이미 동일한 제품이 있습니다.");
  } else {
    modalData.item[modalData.fieldName] = selectedItems.productName;
  
    modalData.item["productId"] = selectedItems.productId;
    modalData.item["categoryMain"] = selectedItems.categoryMain;
    modalData.item["price"] = selectedItems.purchasePrice;
  }

  itemModalVisible.value = false; //모달 닫음
  itemModalReturn.value = {}; //문제가 생길 수 있어서 초기화
};

const itemSearchModal = async (searchValue) => {
  itemModalItems.value = await getItemModalItems(searchValue);
}

// 모달 이벤트

const formSearch = async (item, fieldName) => {
  // console.log('form search', item);
  // console.log('data', fieldName);
  branchModalReturn.value = { item, fieldName };

  branchModalItems.value = await getBranchModalItems("");
  branchModalVisible.value = true;
}

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
      // TODO : 다른 alert() 함수를 사용하면 변경
      alert("폼에 정보에 비어있는 데이터가 있습니다.");
      return;
    }
  }

  let totalAmount = 0;
  for (const table of tableData) {
    for (const data in table) {
      if (!table[data]) {
        // TODO : 다른 alert() 함수를 사용하면 변경
        alert("테이블에 비어있는 데이터가 있습니다.");
        return;
      }
    }
    const product = await axios.get(`/api/products/${table.productId}`);
    totalAmount += table.price * table.quantity * product.data.packQty;
  }

  confirm.require({
    icon: 'pi pi-info-circle',
    header: '발주서를 등록',
    message: '발주서를 최종 등록하시겠습니까?',
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
            reason: formData.reason.value+"",
            totalAmount
        },
        ordersDetail: tableData.map((e) => {
          return {
            ...e,
            unit: '130004'
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
  const dueDate = new Date();
  dueDate.setDate(dueDate.getDate() + 8);
  defaultForm.value.dueDate = convertDate(dueDate);

  const user = useAuth().user;
  defaultForm.value.userId = user.value.empName;
  // console.log(user);

  const branchInfo = await getBranchInfo(user.value.employeeId);
  if (branchInfo) {
    defaultForm.value.orderFrom = branchInfo.compName;
    defaultForm.value.orderFromId = branchInfo.compId;
  } else {
    // TODO : 임시적으로 추가 했기 때문에 삭제 요함.
    formSchema.value.splice(-2, 1, {
      type: 'item-search',
      label: '지점명',
      id: 'orderFrom',
      placeholder: '지점을 입력하세요.'
    });
  }
});
</script>
<template>
  <ConfirmDialog />
  <InputDataTable ref="inputRef" title="발주서정보" tableTitle="제품 목록"
    :defaultForm="defaultForm" :formSchema="formSchema"
    :defaultTable="defaultTable" :columns="columns"
    @formSearch="formSearch" @tableSearch="tableSearch"
    @submit="saveFormHandler" />

  <DialogModal title="지점 모달" :selectionMode="'single'"
    :display="branchModalVisible" :return="branchModalReturn" 
    :headers="branchModalHeaders" :items="branchModalItems" 
     @close="branchCloseModal" @confirm="branchConfirmModal" @search-modal="branchSearchModal" />
  <DialogModal title="제품 모달" :selectionMode="'single'"
    :display="itemModalVisible" :return="itemModalReturn"
    :headers="itemModalHeaders" :items="itemModalItems"
    @close="itemCloseModal" @confirm="itemConfirmModal" @search-modal="itemSearchModal" />
</template>
