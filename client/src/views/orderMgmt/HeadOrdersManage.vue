<script setup>
import InputDataTable from '@/components/common/InputDataTable.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import { onBeforeMount, onMounted, ref, watch } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import { useAuth } from '../../composables/useAuth';
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
const formSchema = [
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
    { type: 'item-search', label: '공급업체', id: 'orderTo', placeholder: '공급업체를 입력하세요.' },
    { type: 'text', label: '비고', id: 'note', placeholder: '비고를 입력하세요.' },
    { type: 'data', label: '발주요청일', id: 'orderDate', data: 'date' },
    { type: 'data', label: '총 가격', id: 'totalAmount', data: 'text' }
];

/* Input Table */

let tableData = ref({});

// 테이블 기본값
const defaultTable = {
    // categoryMain: '화장품',
    // price: 15000,
    unit: 'box'
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

// 공급업체 모달
const supModalVisible = ref(false);
const supModalReturn = ref({});
const supModalItems = ref([]);

const supModalHeaders = ref([
    { field: 'compName', header: '회사명' },
    { field: 'ceoName', header: '대표자' },
    { field: 'phone', header: '전화번호' },
    { field: 'address', header: '주소' },
    { field: 'settleMgr', header: '매니저' },
    { field: 'note', header: '비고' }
]);

const getSupModalItems = async (searchValue) => {
    let req;
    try {
        req = await axios.get('/api/search/company/supplier', {
            params: {
                searchValue
            }
        });
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: '오류',
            detail: '공급업체 정보를 불러오는 중 오류가 발생했습니다.',
            life: 2000
        });
        throw Error('공급업체 모달 데이터 불러오기 실패: ' + error);
    }
    return req.data;
};

const supCloseModal = () => {
    supModalVisible.value = false;
};

const supConfirmModal = (selectedItems) => {
    const modelData = supModalReturn.value;
    // console.log('selected Item', selectedItems);
    console.log('data', modelData);
    modelData.item[modelData.fieldName + 'Id'] = selectedItems.compId;
    modelData.item[modelData.fieldName] = selectedItems.compName;

    supModalVisible.value = false; //모달 닫음
    inputRef.value.resetTableHandler();
};

const subSearchModal = async (searchValue) => {
    supModalItems.value = await getSupModalItems(searchValue);
};

// 제품 모달
const itemModalVisible = ref(false);
const itemModalReturn = ref({});
const itemModalItems = ref([]);

const itemModalHeaders = ref([
    { field: 'productName', header: '상품명' },
    { field: 'categoryMain', header: '대분류' },
    { field: 'categorySub', header: '소분류' },
    { field: 'vendorName', header: '공급사명' },
    { field: 'productSpec', header: '규격' }
]);

const getItemModalItems = async (searchValue) => {
    let req;
    try {
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
                const modelData = supModalReturn.value;
                return e.vendorName == modelData.item[modelData.fieldName];
            });
        }
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: '오류',
            detail: '제품 정보를 불러오는 중 오류가 발생했습니다.',
            life: 2000
        });
        throw Error('제품 모달 데이터 불러오기 실패: ' + error);
    }
    return req;
};

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
        toast.add({
            severity: 'warn',
            summary: '경고',
            detail: '이미 동일한 제품이 있습니다.',
            life: 2000
        });
    } else {
        const product = await axios.get(`/api/products/${selectedItems.productId}`);

        modalData.item[modalData.fieldName] = selectedItems.productName;
        modalData.item['productId'] = selectedItems.productId;
        modalData.item['categoryMain'] = selectedItems.categoryMain;
        modalData.item['price'] = selectedItems.purchasePrice;
        modalData.item['packQty'] = product.data.data.packQty;
    }

    itemModalVisible.value = false; //모달 닫음
    itemModalReturn.value = {}; //문제가 생길 수 있어서 초기화
};

const itemSearchModal = async (searchValue) => {
    itemModalItems.value = await getItemModalItems(searchValue);
};

// 모달이벤트

const formSearch = async (item, fieldName) => {
    // console.log('form search', item);
    // console.log('data', fieldName);
    supModalReturn.value = { item, fieldName };
    try {
        supModalItems.value = await getSupModalItems('');
        supModalVisible.value = true;
    } catch (error) {
        console.error(error);
    }
};

const tableSearch = async (item, fieldName, data) => {
    // console.log('table search', item);
    // console.log('data', fieldName);
    if (!supModalReturn.value?.item) {
        toast.add({
            severity: 'warn',
            summary: '경고',
            detail: '공급업체를 먼저 입력해주세요.',
            life: 2000
        });
        return;
    }

    itemModalReturn.value = { item, fieldName, data };

    // 공급업체 먼저 입력
    try {
        itemModalItems.value = await getItemModalItems();
        itemModalVisible.value = true;
    } catch (error) {
        console.error(error);
    }
};

const fetchOrders = async (formData, tableData) => {
    // 총 가격
    const totalAmount = parseFloat(String(formData.totalAmount).replace(/[,원]/g, ''));
    // 발주서 저장
    const res = await axios.post('/api/orders', {
        orders: {
            ...formData,
            creatorId: defaultForm.value.creatorId,
            orderType: '150001', //본사 발주
            reason: formData.reason.value + '', //발주 사유
            totalAmount //총 가격
        },
        ordersDetail: tableData.map((e) => {
            return {
                ...e,
                unit: '130004' //'box'
            };
        })
    });
    console.log(res.data);
};

// 폼과 테이블 데이터를 저장하는 핸들러
const saveFormHandler = async (formData, tableData) => {
    console.log('formData:', formData);
    console.log('tableData:', tableData);

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
            try {
                fetchOrders(formData, tableData);
                toast.add({
                    severity: 'success',
                    summary: '성공',
                    detail: '발주서가 등록되었습니다.',
                    life: 2000
                });
                inputRef.value.resetFormHandler();
                inputRef.value.resetTableHandler();
            } catch (error) {
                console.error('발주서 등록 중 오류 발생:', error);
                toast.add({
                    severity: 'error',
                    summary: '오류',
                    detail: '발주서 등록 중 오류가 발생했습니다.',
                    life: 2000
                });
            }
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
                total += data.price * data.packQty * quantity;
            }
            const formData = inputRef.value.getFormData();
            formData.value.totalAmount = Number(total).toLocaleString() + '원';
        }
    },
    { deep: true }
);

const getHeadInfo = async () => {
    const req = await axios.get(`/api/search/company/head`);
    return req.data[0];
};

onBeforeMount(async () => {
    const dueDate = new Date();
    dueDate.setDate(dueDate.getDate() + 8);
    defaultForm.value.dueDate = convertDate(dueDate);

    const user = useAuth().user;
    defaultForm.value.creatorId = user.value.employeeId;
    defaultForm.value.creatorName = user.value.empName;

    const headInfo = await getHeadInfo();
    // console.log(headInfo);
    defaultForm.value.orderFrom = headInfo.compName;
    defaultForm.value.orderFromId = headInfo.compId;
});

onMounted(async () => {
    tableData.value = inputRef.value.getTableData();
});
</script>
<template>
    <ConfirmDialog />
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

    <DialogModal 
        title="공급업체 정보" 
        :selectionMode="'single'" 
        :display="supModalVisible" 
        :return="supModalReturn" 
        :headers="supModalHeaders" 
        :items="supModalItems" 
        @close="supCloseModal" 
        @confirm="supConfirmModal" 
        @search-modal="subSearchModal"
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
