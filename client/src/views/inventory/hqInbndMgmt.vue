<script setup>
import InputMultiTable from '@/components/common/InputMultiTable.vue';
import { onMounted, ref } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import axios from '@/service/axios';
import DialogModal from '@/components/overray/DialogModal.vue';
import moment from 'moment';
import { useToast } from 'primevue';

// 테스트 데이터
const items = ref([]);
const toast = useToast();

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

    /* 입고 기본키 생성 */
    const inbndCode = await axios.get('/api/inbnd/getCode');
    /* 입고일 : 오늘 날짜로 설정 */
    const today = moment().format('YY/MM/DD');

    // 선택한 발주정보를 formData로 전달
    formData.value.orderId = selectedItems.orderId;
    formData.value.inbndNo = inbndCode.data;
    formData.value.orderTitle = selectedItems.orderTitle;
    formData.value.outbndFrom = selectedItems.orderTo;
    formData.value.inbndTo = selectedItems.orderFrom;
    formData.value.inbndDate = today;
    console.log('테에스으트', formData.value);

    /* 제품 상세정보 불러오기 */
    const selOrderId = formData.value.orderId;
    // console.log('id값 확인', selOrderId);
    const res = await axios.get(`/api/orders/${selOrderId}`);
    console.log('상세값확인', res.data.detail);
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
const loadPurchaseOnClick = async () => {
    try {
        await getOrderData();
        orderModalVisible.value = true;
    } catch (error) {
        console.error(error);
        toast.add({
            severity: 'error',
            summary: '오류',
            detail: '발주정보를 불러오는 중 오류가 발생했습니다.',
            life: 2000
        });
    }
};

const searchModal = async (searchValue) => {
    await getOrderData();
    console.log('Search modal with value:', searchValue);
    modalItems.value = modalItems.value.filter((e) => {
        return e.orderTitle.includes(searchValue) || e.creatorName.includes(searchValue) || e.reasonName.includes(searchValue) || e.orderDate.includes(searchValue) || e.dueDate.includes(searchValue);
    });
    // 검색 로직 구현
};

/* end of Modal function */

/* Form Data */
// 폼 기본값
const formData = ref({});

// 폼 스키마
const formSchema = [
    { type: 'data', label: '입고번호', id: 'inbndNo', data: 'text' },
    { type: 'data', label: '발주명', id: 'orderTitle', data: 'text' },
    { type: 'data', label: '출고지', id: 'outbndFrom', data: 'text' },
    { type: 'data', label: '입고지', id: 'inbndTo', data: 'text' },
    { type: 'data', label: '입고일', id: 'inbndDate', data: 'text' }

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
                orderStatus: '030003' //출고 완료
            }
        });
        result.data = result.data.filter((e) => {
            if (e.orderType === '150003' || e.orderType === '150001') {
                return e;
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
        throw Error('출고 데이터 불러오기 실패: ' + e);
    }
};

/**
 * 제품등록처리 진행 함수
 */
async function callInbndProcess(orderId) {
    if (!orderId) {
        toast.add({
            severity: 'error',
            summary: '오류',
            detail: '입고할 발주정보를 선택해주세요.',
            life: 2000
        });
        return;
    }
    try {
        const response = await axios.post('/api/inbnd/hqProcess', null, {
            params: {
                orderId: orderId
            }
        });
        toast.add({
            severity: 'success',
            summary: '성공',
            detail: '입고 처리가 완료되었습니다.',
            life: 2000
        });
        resetFormHandler();
        getOrderData();
    } catch (error) {
        console.error('입고 처리 중 오류 발생:', error);
        toast.add({
            severity: 'error',
            summary: '오류',
            detail: '입고 처리 중 오류가 발생했습니다.',
            life: 2000
        });

        // 에러 메시지 추출
        // const message = error.response?.data?.message || "입고 처리 중 오류가 발생했습니다.";
        // alert(message);
    }
}

// 입고처리 버튼 이벤트 함수
const inbndHandler = () => {
    // console.log('함수실행 전');
    const inbndOrderId = formData.value.orderId;
    console.log('전달값 확인', inbndOrderId);
    callInbndProcess(inbndOrderId);
    // console.log('함수실행 후');
};

const resetFormHandler = () => {
    formData.value = {};
    tableData.value = [];
};
</script>
<template>
    <InputMultiTable title="본사 입고정보" :defaultForm="formData" :formSchema="formSchema" :tableHeader="tableHeader" :tableData="tableData" :detailCRUD="false" @onRowSelect="onRowSelect" @resetForm="resetFormHandler">
        <template #btn>
            <Button label="발주정보불러오기" class="min-w-fit whitespace-nowrap" severity="info" @click="loadPurchaseOnClick" outlined />
        </template>
        <template #basicBtn>
            <Button label="전체입고처리" class="min-w-fit whitespace-nowrap" severity="success" @click="inbndHandler" outlined />
            <!-- <Button label="개별출고처리" class="min-w-fit whitespace-nowrap" severity="success" @click="" outlined /> -->
        </template>
    </InputMultiTable>
    <DialogModal title="발주내역" :display="orderModalVisible" :headers="modalHeaders" :items="modalItems" :selectionMode="'single'" @close="closeModal" @confirm="confirmModal" @search-modal="searchModal"></DialogModal>
</template>
