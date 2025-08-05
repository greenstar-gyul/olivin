<script setup>
import ViewDataTable from '@/components/common/FormViewTable.vue';
import { onBeforeMount, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from '@/service/axios';

const route = useRoute();
const router = useRouter();

/* View Data */

// 폼 기본값
const defaultForm = ref({});

// 폼 스키마
const formSchema = [
  { label: '발주명', id: 'orderTitle' },
  { label: '발주사유', id: 'reasonName' },
  { label: '등록자', id: 'creatorName' },
  { label: '납기예정일', id: 'dueDate', data: 'date' },
  { label: '공급업체', id: 'orderTo', },
  { label: '발주요청일', id: 'orderDate', data: 'date' },
  { label: '발주상태', id: 'orderStatusName' },
  { label: '총 금액', id: 'totalAmount', data: 'number' },
];

/* View Table */

// 테이블 기본값
const defaultTable = ref([]);

// 테이블 컬럼 정의
const columns = {
  title: '상품 목록',
  header: {
    productId: 'ID',
    productName: '제품명',
    categoryName: '카탈로그',
    vendorName: '공급사',
    price: '단가',
    quantity: '수량',
    unitName: '단위',
    packQty: '박스당 수량',
    totalPrice: '총 가격',
  },
  rightAligned: ['price', 'quantity', 'packQty', 'totalPrice']
};

/* redirect */
const redirectToList = () => {
  switch(defaultForm.value.orderType) {
    case '150001':
      router.push('/orders/head/view');
      break;
    case '150002':
      router.push('/orders/branch/view');
      break;
    case '150003':
      router.push('/orders/supplier/view');
      break;
  }
};

onBeforeMount(async () => {
  const res = await axios.get(`/api/orders/${route.params.orderId}`);
  defaultForm.value = res.data.order

  // const totalAmount = defaultForm.value.totalAmount;
  // defaultForm.value.totalAmount = Number(totalAmount).toLocaleString()+'원';

  defaultTable.value = res.data.detail.map((e) => {
    return {
      ...e,
      quantity: Number(e.quantity).toLocaleString(),
      packQty: Number(e.packQty).toLocaleString(),
      price: Number(e.price).toLocaleString()+'원',
      totalPrice: Number(e.packQty * e.price * e.quantity).toLocaleString()+'원'
    }
  });
});
</script>
<template>
  <ViewDataTable title="발주서정보"
    :defaultForm="defaultForm" :formSchema="formSchema"
    :defaultTable="defaultTable" :columns="columns"
    @redirect="redirectToList" />
</template>
