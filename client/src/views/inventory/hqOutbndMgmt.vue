<script setup>
import InputMultiTable from '@/components/common/InputMultiTable.vue';
import { onMounted, ref } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import axios from '@/service/axios';
import DialogModal from '@/components/overray/DialogModal.vue';
import moment from 'moment';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();

// 테스트 데이터
const items = ref([]);

/* Modal function */

// 모달의 visible 상태를 관리하는 ref 변수
const orderModalVisible = ref(false);

// 모달창의 테이블 헤더 정보
const modalHeaders = ref([
    { field: 'orderTitle', header: '발주명' },
    { field: 'creatorName', header: '발주신청자' },
    { field: 'reasonName', header: '발주사유' },
    { field: 'orderDate', header: '발주일' },
    { field: 'dueDate', header: '납기예정일' }
    // { field: 'orderType', header: '발주유형' }, // 테스트용
]);

// 모달창의 데이터 아이템
const modalItems = ref([]);

// 모달창 닫기 함수. 필요한 만큼 생성 -> 어떤건지 테스트 필요
const closeModal = () => {
    orderModalVisible.value = false;
};

// 모달창 확인 버튼 클릭 시 호출되는 함수
// 필요한 로직 작성
const confirmModal = async (selectedItems) => {
    // console.log('Selected items from modal:', selectedItems);
    // console.log('확인용:', selectedItems.orderTitle);

    /* 출고 기본키 생성 */
    const outbndCode = await axios.get('/api/outbnd/getCode');
    /* 출고일 : 오늘 날짜로 설정 */
    const today = moment().format('YY/MM/DD');

    // 선택한 발주정보를 formData로 전달
    formData.value.orderId = selectedItems.orderId;
    formData.value.outbndNo = outbndCode.data;
    formData.value.orderTitle = selectedItems.orderTitle;
    formData.value.outbndFrom = selectedItems.orderTo;
    formData.value.inbndTo = selectedItems.orderFrom;
    formData.value.outbndDate = today;
    // console.log('테에스으트', formData.value);

    /* 제품 상세정보 불러오기 */
    const selOrderId = formData.value.orderId;
    // console.log('id값 확인', selOrderId);
    const res = await axios.get(`/api/orders/${selOrderId}`);
    // console.log('상세값확인', res.data.detail);
    const details = res.data.detail;

    for (let id = 0; id < details.length; id++) {
        details[id].id = id;
    }

    tableData.value = details;
    // console.log('전달값확인', tableData.value)
    orderModalVisible.value = false;
};

// 검색 모달을 열 때 호출되는 함수
// case 문을 사용하여 모달 이름(item-search 타입의 name을 따름)에 따라 다른 모달을 열 수 있도록 구현
const loadPurchaseOnClick = () => {
    orderModalVisible.value = true;
};

const searchModal = (searchValue) => {
    console.log('Search modal with value:', searchValue);
    // 검색 로직 구현
};

/* end of Modal function */

/* Form Data */
// 폼 기본값
const formData = ref({});

// 폼 스키마
const formSchema = [
    { type: 'data', label: '출고번호', id: 'outbndNo', data: 'text' },
    { type: 'data', label: '발주명', id: 'orderTitle', data: 'text' },
    { type: 'data', label: '출고지', id: 'outbndFrom', data: 'text' },
    { type: 'data', label: '입고지', id: 'inbndTo', data: 'text' },
    { type: 'data', label: '출고일', id: 'outbndDate', data: 'text' }

    // { type: 'text', label: '출고상태', id: 'outbndStatus',
    //   // options: [
    //   //   { name: '옵션1', value: 1 },
    //   //   { name: '옵션2', value: 2 }
    //   // ]
    // },
];

/* 제품목록 시작 */
const tableData = ref([]);

const tableHeader = {
    title: '제품 목록',
    header: {
        productName: '제품명',
        // totalOutbndQuantity  : '출고수량',

        quantity: '발주수량',
        unitName: '단위'
        // outbndStatus: '출고상태',
    },
    rightAligned: ['price']
};
/* 제품목록 끝 */

/* Input Table Detail 
const detailData = ref([]);

// 테이블 컬럼 정의
const columns = [
  { inputType: 'text', header: 'LOT', field: 'lotNo' },
  { inputType: 'number', header: '출고수량', field: 'outbndQuantity' },
  { inputType: 'text', header: '단위', field: 'unit' },
  // { inputType: 'text', header: '출고상태', field: 'outbnStatus' },
];
*/

// // 폼과 테이블 데이터를 저장하는 핸들러
// const onRowSelect = async (select) => {
//   console.log('Selected Row:', select.productId);

//   const test = await axios.get(`/api/inbnd/test/${select.productId}`);
//   console.log(test.data);
//   // fetch 사용하는 시간때문에 250ms 지연
//   // 실제로는 서버에서 데이터를 가져오는 로직이 필요합니다.
//   setTimeout(() => {
//     // 선택된 행의 데이터를 폼에 채우기
//     // axios나 fetch를 사용하여 서버에서 데이터를 가져올 수 있습니다.
//     if (select.id === 0) {
//       detailData.value = [
//         { id: 1, lot: 'LOT001', quantity: 10, status: '출고대기' },
//         { id: 2, lot: 'LOT002', quantity: 20, status: '출고대기' }
//       ];
//     } else if (select.id === 2) {
//       detailData.value = [
//         { id: 3, lot: 'LOT003', quantity: 30, status: '출고대기' },
//         { id: 4, lot: 'LOT004', quantity: 40, status: '출고완료' }
//       ];
//     } else {
//       detailData.value = [];
//     }
//   }, 250);
// };

// 발주정보 불러오기
const getOrderData = async () => {
    try {
        const result = await axios.get('/api/orders', {
            params: {
                orderType: '150002',
                orderStatus: '030002'
            }
        });
        modalItems.value = await result.data.map((item) => {
            return {
                ...item,
                orderDate: convertDate(item.orderDate)
            };
        });
        // const data = await result.data;
        // console.log('발주정보데이터 확인1 : ', modalItems.value);
        // console.log('발주정보데이터 확인2 : ', result.data);
    } catch (e) {
        console.error('출고 데이터 불러오기 실패:', e);
    }
};

/**
 * 제품출고처리 진행 함수 
 */
async function callOutbndProcess(orderId) {
    try {
        const response = await axios.post('/api/outbnd/hqProcess', null, {
            params: {
                orderId: orderId
            }
        });
        alert('출고 처리가 완료되었습니다.');
        resetFormHandler(); // 전체 폼 초기화
        getOrderData(); // 처리한 발주서 정보 초기화
    } catch (error) {
        console.error('서버 오류 발생:', error);

        // 에러 메시지 추출
        const message = error.response?.data?.message || '서버 오류가 발생했습니다.';
        alert(message);
    }
}

// 출고처리 버튼 이벤트 함수
const outbndHandler = () => {
    // console.log('함수실행 전');
    const outbndOrderId = formData.value.orderId;
    console.log('전달값 확인', outbndOrderId);
    callOutbndProcess(outbndOrderId);
    // console.log('함수실행 후');
};

// 초기화버튼 이벤트 함수
const resetFormHandler = () => {
    formData.value = {};
    tableData.value = [];
    modalItems.value = [];
};

onMounted(() => {
    getOrderData();
});
</script>
<template>
    <InputMultiTable title="출고정보" :defaultForm="formData" :formSchema="formSchema" :tableHeader="tableHeader" :tableData="tableData" :detailCRUD="false" @onRowSelect="onRowSelect" @resetForm="resetFormHandler">
        <template #btn>
            <Button label="발주정보불러오기" class="min-w-fit whitespace-nowrap" severity="info" @click="loadPurchaseOnClick" outlined />
        </template>
        <template #basicBtn>
            <Button label="전체출고처리" class="min-w-fit whitespace-nowrap" severity="success" @click="outbndHandler" outlined />
            <!-- <Button label="개별출고처리" class="min-w-fit whitespace-nowrap" severity="success" @click="" outlined /> -->
        </template>
    </InputMultiTable>
    <DialogModal title="발주내역" :display="orderModalVisible" :headers="modalHeaders" :items="modalItems" :selectionMode="'single'" @close="closeModal" @confirm="confirmModal" @search-modal="searchModal"></DialogModal>
</template>
