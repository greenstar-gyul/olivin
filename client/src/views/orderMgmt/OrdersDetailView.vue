<script setup>
import ViewDataTable from '@/components/common/FormViewTable.vue';
import { h, onBeforeMount, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Button, useConfirm, useToast } from 'primevue';
import axios from '@/service/axios';
import { useAuth } from '@/composables/useAuth';

const route = useRoute();
const router = useRouter();

const confirm = useConfirm(); // confirm
const toast = useToast(); // toast

const approvalBtnSlot = ref(null); // 승인 버튼 슬롯 데이터
const rejectionBtnSlot = ref(null); // 반려 버튼 슬롯 데이터

const userInfo = ref({}); //사용자 정보

/* View Data */

// 폼 기본값
const defaultForm = ref({});

// 폼 스키마
const formSchema = ref([
    { label: '발주명', id: 'orderTitle' },
    { label: '발주사유', id: 'reasonName' },
    { label: '등록자', id: 'creatorName' },
    { label: '납기예정일', id: 'dueDate', data: 'date' },
    { label: '발주처', id: 'orderFrom' },
    { label: '발주요청일', id: 'orderDate', data: 'date' },
    { label: '발주상태', id: 'orderStatusName' },
    { label: '총 금액', id: 'totalAmount' }
]);

/* View Table */

// 테이블 기본값
const defaultTable = ref([]);

// 테이블 컬럼 정의
const columns = {
    title: '상품 목록',
    header: {
        productId: '제품코드',
        productName: '제품명',
        categoryName: '카탈로그',
        vendorName: '공급사',
        price: '단가',
        quantity: '수량',
        unitName: '단위',
        packQty: '박스당 수량',
        totalPrice: '총 가격'
    },
    rightAligned: ['price', 'quantity', 'packQty', 'totalPrice']
};

/* redirect */
const redirectToList = () => {
  if (defaultForm.value?.compType === '100001') {
    const move = route.query.move || 'head';
    router.push(`/orders/${move}/view`);
  } else {
        switch (defaultForm.value.orderType) {
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
    }
};

/* Dialog visibility */

const rejectionVisible = ref(false);
const rejectionReason = ref('');

const approvalHandler = async () => {
  const res = await axios.post(`/api/orders/${route.params.orderId}/approval`, {
    employeeId: userInfo.value.employeeId,
    orderId: route.params.orderId
  });
  toast.add({
    severity: 'success',
    summary: '성공',
    detail: '발주서가 승인되었습니다.',
    life: 2000
  });
  approvalBtnSlot.value = null; // 승인 버튼 제거
  rejectionBtnSlot.value = null; // 반려 버튼 제거
  // redirectToList(); //목록으로 이동
}

const approvalDialogHandler = async () => {
    confirm.require({
        icon: 'pi pi-info-circle',
        header: '발주서 승인',
        message: '발주서를 승인하시겠습니까?',
        rejectProps: {
            label: '취소',
            severity: 'secondary',
            outlined: true
        },
        acceptProps: {
            label: '승인'
        },
        accept: async () => {
            approvalHandler();
        },
        reject: () => {
            toast.add({
                severity: 'error',
                summary: '오류',
                detail: '발주서 승인이 취소되었습니다.',
                life: 2000
            });
            return;
        }
    });
};

const rejectionHandler = async () => {
    if (rejectionReason.value.trim() === '') {
        toast.add({
            severity: 'warn',
            summary: '경고',
            detail: '반려사유를 입력하세요.',
            life: 2000
        });
        rejectionVisible.value = false;
        rejectionReason.value = ''; //초기화
        return;
    }

    const res = await axios.post(`/api/orders/${route.params.orderId}/rejection`, {
        orderId: route.params.orderId,
        rejectionUser: userInfo.value.empName,
        rejectionReson: rejectionReason.value
    });
    rejectionVisible.value = false;
    rejectionReason.value = ''; //초기화

    approvalBtnSlot.value = null; // 승인 버튼 제거
    rejectionBtnSlot.value = null; // 반려 버튼 제거
    // redirectToList(); //목록으로 이동
}

onBeforeMount(async () => {
    //사용자 정보
    userInfo.value = useAuth().user.value;

    // 주문 정보 조회
    const res = await axios.get(`/api/orders/${route.params.orderId}`);
    defaultForm.value = {
        ...res.data.order,
        totalAmount: Number(res.data.order.totalAmount).toLocaleString() + '원'
    };
    if (defaultForm.value.orderType === '150002') {
        delete columns.header.packQty; // 속성 제거
    }

    // 테이블 데이터
    defaultTable.value = res.data.detail.map((e) => {
        if (defaultForm.value.orderType === '150002') {
            e.packQty = 1; // 지점 발주서의 경우 packQty는 항상 1로 설정
        }
        return {
            ...e,
            quantity: Number(e.quantity).toLocaleString(),
            packQty: Number(e.packQty).toLocaleString(),
            price: Number(e.price).toLocaleString() + '원',
            totalPrice: Number(e.packQty * e.price * e.quantity).toLocaleString() + '원'
        };
    });

    // 승인, 반려 버튼 컴포넌트 주입
    if (defaultForm.value.orderStatus === '030001') {
        // 승인대기
        // 사용자 정보
        const userCompInfo = await axios.get(`/api/orders/user/compInfo`, {
            params: {
                empId: userInfo.value.employeeId
            }
        });

        if (userCompInfo.data?.compType === '100001') {
            // 사용자가 본사인 경우
            defaultForm.value.compType = userCompInfo.data.compType;
            // 버튼
            approvalBtnSlot.value = h(
                Button,
                {
                    severity: 'info',
                    variant: 'outlined',
                    onClick: () => approvalDialogHandler()
                },
                '승인'
            );
            rejectionBtnSlot.value = h(
                Button,
                {
                    severity: 'danger',
                    variant: 'outlined',
                    onClick: () => (rejectionVisible.value = true)
                },
                '반려'
            );
        }
    } else if (defaultForm.value.orderStatus === '030004') {
        // 반려
        const res = await axios.get(`/api/orders/${route.params.orderId}/rejection`);
        formSchema.value.push({ label: '반려사유', id: 'rejectionReson' }, { label: '반려일자', id: 'rejectionDate', data: 'date' });

        defaultForm.value.rejectionReson = res.data.rejectionReson;
        defaultForm.value.rejectionDate = res.data.rejectionDate;
    }
});
</script>
<template>
    <Toast />
    <!-- 알림 -->
    <ConfirmDialog />
    <!-- 컴펌 다이얼로그 -->
    <!-- 테이블 -->
    <ViewDataTable title="발주서정보" :defaultForm="defaultForm" :formSchema="formSchema" :defaultTable="defaultTable" :columns="columns" @redirect="redirectToList">
        <template #btn>
            <component :is="approvalBtnSlot" />
            <component :is="rejectionBtnSlot" />
        </template>
    </ViewDataTable>
    <!-- 다이얼 로그 -->
    <Dialog v-model:visible="rejectionVisible" modal header="발주서 반려" :style="{ width: '25rem' }">
        <span class="text-surface-500 dark:text-surface-400 block mb-6">발주서를 반려하시겠습니까?</span>
        <div class="flex items-center gap-4 mb-8">
            <label for="username" class="font-semibold w-24">반려사유</label>
            <InputText id="username" class="flex-auto" v-model="rejectionReason" placeholder="반려사유를 입력하세요." autocomplete="off" />
        </div>
        <div class="flex justify-end gap-2">
            <Button type="button" label="취소" severity="secondary" @click="rejectionVisible = false"></Button>
            <Button type="button" label="저장" @click="rejectionHandler"></Button>
        </div>
    </Dialog>
</template>
