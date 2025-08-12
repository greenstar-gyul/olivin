<script setup>
import InputDataTable from '@/components/common/InputDataTable.vue';
import { onBeforeMount, onMounted, ref, watch } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import { useAuth } from '@/composables/useAuth';
import { useConfirm, useToast } from 'primevue';
import axios from '@/service/axios';

const confirm = useConfirm(); //confirm
const toast = useToast(); //toast

/* Form Data */

const inputRef = ref(null);

// 폼 기본값
const defaultForm = ref({
    orderDate: convertDate(new Date()),
    totalAmount: '0원'
});

// 폼 스키마
const formSchema = ref([
    { type: 'text', label: '발주명', id: 'orderTitle', placeholder: '발주명을 입력하세요.' },
    {
        type: 'select',
        label: '발주사유',
        id: 'reason',
        placeholder: '발주사유를 선택하세요.',
        options: [
            { name: '정기 발주', value: 140001 },
            { name: '수요 예측 발주', value: 140002 },
            { name: '재고 부족', value: 140003 },
            { name: '신상품 발주', value: 140004 },
            { name: '긴급 발주', value: 140005 },
            { name: '행사상품 발주', value: 140006 }
        ]
    },
    { type: 'data', label: '등록자', id: 'creatorName', data: 'text' },
    { type: 'date', label: '납기예정일', id: 'dueDate', placeholder: '납기예정일을 선택하세요.' },
    { type: 'data', label: '지점명', id: 'orderFrom', data: 'text' },
    { type: 'text', label: '비고', id: 'note', placeholder: '비고를 입력하세요.' },
    { type: 'data', label: '발주요청일', id: 'orderDate', data: 'date' },
    { type: 'data', label: '총 가격', id: 'totalAmount', data: 'text' }
]);

/* Input Table */

let tableData = ref({});

// 테이블 기본값
const defaultTable = {
    // categoryMain: '화장품',
    // price: 15000,
    unit: 'ea'
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
    });
    return req.data;
};

const branchCloseModal = () => {
    branchModalVisible.value = false;
};

const branchConfirmModal = (selectedItems) => {
    const modelData = branchModalReturn.value;
    //console.log('selected Item', selectedItems);
    //console.log('data', modelData);
    modelData.item[modelData.fieldName + 'Id'] = selectedItems.compId;
    modelData.item[modelData.fieldName] = selectedItems.compName;

    branchModalVisible.value = false; //모달 닫음
    inputRef.value.resetTableHandler();
};

const branchSearchModal = async (searchValue) => {
    branchModalItems.value = await getBranchModalItems(searchValue);
};

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
    { field: 'productSpec', header: '규격' }
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
};

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
        toast.add({
            severity: 'warn',
            summary: '경고',
            detail: '이미 동일한 제품이 있습니다.',
            life: 2000
        });
    } else {
        modalData.item[modalData.fieldName] = selectedItems.productName;
        modalData.item['productId'] = selectedItems.productId;
        modalData.item['categoryMain'] = selectedItems.categoryMain;
        modalData.item['price'] = selectedItems.purchasePrice;
    }

    itemModalVisible.value = false; //모달 닫음
    itemModalReturn.value = {}; //문제가 생길 수 있어서 초기화
};

const itemSearchModal = async (searchValue) => {
    itemModalItems.value = await getItemModalItems(searchValue);
};

// 모달 이벤트

const formSearch = async (item, fieldName) => {
    // console.log('form search', item);
    // console.log('data', fieldName);
    branchModalReturn.value = { item, fieldName };

    branchModalItems.value = await getBranchModalItems('');
    branchModalVisible.value = true;
};

const tableSearch = async (item, fieldName, data) => {
    // console.log('table search', item);
    // console.log('data', fieldName);
    itemModalReturn.value = { item, fieldName, data };

    itemModalItems.value = await getItemModalItems();
    itemModalVisible.value = true;
};

const fetchOrders = async (formData, tableData) => {
    //본사 정보
    const headRes = await axios.get(`/api/search/company/head`);
    const headInfo = headRes.data[0];
    // 총 가격
    const totalAmount = parseFloat(String(formData.totalAmount).replace(/[,원]/g, ''));
    // 발주서 등록
    const res = await axios.post('/api/orders', {
        orders: {
            ...formData,
            creatorId: defaultForm.value.creatorId,
            orderType: '150002', //지점 발주
            reason: formData.reason.value + '', //발주 사유
            //수주처 정보
            orderToId: headInfo.compId,
            orderTo: headInfo.compName,
            totalAmount //총 가격
        },
        ordersDetail: tableData.map((e) => {
            return {
                ...e,
                unit: '130003' //'ea'
            };
        })
    });
    console.log(res.data);
};

// 폼과 테이블 데이터를 저장하는 핸들러
const saveFormHandler = async (formData, tableData) => {
    console.log('orders:', formData);
    console.log('ordersDetail:', tableData);

    for (const form in formData) {
        if (!formData[form]) {
            if (form == 'note') continue;
            toast.add({
                severity: 'error',
                summary: '오류',
                detail: '폼에 정보에 비어있는 데이터가 있습니다.',
                life: 2000
            });
            return;
        }
    }

    if (tableData.length === 0) {
        toast.add({
            severity: 'error',
            summary: '오류',
            detail: '테이블 정보가 없습니다.',
            life: 2000
        });
        return;
    }

    for (const table of tableData) {
        for (const data in table) {
            if (!table[data]) {
                toast.add({
                    severity: 'error',
                    summary: '오류',
                    detail: '테이블에 비어있는 데이터가 있습니다.',
                    life: 2000
                });
                return;
            }
        }
    }

    confirm.require({
        icon: 'pi pi-info-circle',
        header: '발주서 등록',
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
            fetchOrders(formData, tableData);
            toast.add({
                severity: 'success',
                summary: '성공',
                detail: '발주서가 등록되었습니다.',
                life: 2000
            });
            inputRef.value.resetFormHandler();
            inputRef.value.resetTableHandler();
        },
        reject: () => {
            toast.add({
                severity: 'error',
                summary: '오류',
                detail: '발주서 등록이 취소되었습니다.',
                life: 2000
            });
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
                total += data.price * quantity;
            }
            const formData = inputRef.value.getFormData();
            formData.value.totalAmount = Number(total).toLocaleString() + '원';
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
    if (req.data.compType == '100002') return req.data;
    return undefined;
};

onBeforeMount(async () => {
    const dueDate = new Date();
    dueDate.setDate(dueDate.getDate() + 8);
    defaultForm.value.dueDate = convertDate(dueDate);

    const user = useAuth().user;
    defaultForm.value.creatorId = user.value.employeeId;
    defaultForm.value.creatorName = user.value.empName;
    // console.log(user);

    const branchInfo = await getBranchInfo(user.value.employeeId);
    if (branchInfo) {
        defaultForm.value.orderFrom = branchInfo.compName;
        defaultForm.value.orderFromId = branchInfo.compId;
    } else {
        // TODO : 임시적으로 추가 했기 때문에 삭제 요함.
        formSchema.value.splice(-4, 1, {
            type: 'item-search',
            label: '지점명',
            id: 'orderFrom',
            placeholder: '지점을 입력하세요.'
        });
    }
});

onMounted(() => {
    tableData.value = inputRef.value.getTableData();
});
</script>
<template>
    <Toast />
    <!-- 알림 -->
    <ConfirmDialog />
    <!-- 컴펌 다이얼로그 -->
    <!-- 테이블 -->
    <InputDataTable
        ref="inputRef"
        title="발주서정보"
        tableTitle="제품 목록"
        :defaultForm="defaultForm"
        :formSchema="formSchema"
        :defaultTable="defaultTable"
        :columns="columns"
        @formSearch="formSearch"
        @tableSearch="tableSearch"
        @submit="saveFormHandler"
    />
    <!-- 다이얼 로그 -->
    <DialogModal
        title="지점 정보"
        :selectionMode="'single'"
        :display="branchModalVisible"
        :return="branchModalReturn"
        :headers="branchModalHeaders"
        :items="branchModalItems"
        @close="branchCloseModal"
        @confirm="branchConfirmModal"
        @search-modal="branchSearchModal"
    />
    <DialogModal
        title="제품 정보"
        :selectionMode="'single'"
        :display="itemModalVisible"
        :return="itemModalReturn"
        :headers="itemModalHeaders"
        :items="itemModalItems"
        @close="itemCloseModal"
        @confirm="itemConfirmModal"
        @search-modal="itemSearchModal"
    />
</template>
