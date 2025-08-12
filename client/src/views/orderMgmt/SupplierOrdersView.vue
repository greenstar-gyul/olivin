<script setup>
import SearchDetailTable from '@/components/common/SearchDetailTable.vue';
import { onMounted, ref } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import { useRouter } from 'vue-router';
import axios from '@/service/axios';
import { useAuth } from '@/composables/useAuth';

const router = useRouter();

const searchDetailTableRef = ref(null);

const header = ref({
    title: '발주제안 조회',
    header: {
        orderId: '제안코드',
        orderTitle: '제안명',
        reasonName: '제안사유',
        orderDate: '제안요청일',
        creatorName: '등록자',
        orderTo: '공급업체명',
        totalAmount: '총 금액',
        dueDate: '납기예정일',
        orderStatusName: '제안상태'
    },
    rightAligned: ['totalAmount']
});

const items = ref([]);

const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
    { type: 'text', label: '제안코드', value: '', placeholder: '제안코드를 입력하세요.', name: 'orderId' },
    {
        type: 'select',
        label: '제안상태',
        value: '',
        placeholder: '제안상태를 선택하세요.',
        name: 'orderStatus',
        options: [
            { name: '승인대기', value: '030001' },
            { name: '승인', value: '030002' },
            { name: '출고완료', value: '030003' },
            { name: '반려', value: '030004' }
        ]
    },
    { type: 'dateRange', label: '제안요청일', value: '', name: 'orderDate', fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
    { type: 'text', label: '제안명', value: '', placeholder: '제안명을 입력하세요.', name: 'orderTitle' },
    {
        type: 'select',
        label: '제안사유',
        value: '',
        placeholder: '제안사유를 선택하세요.',
        name: 'reason',
        options: [
            { name: '정기 발주', value: '140001' },
            { name: '수요 예측 발주', value: '140002' },
            { name: '재고 부족', value: '140003' },
            { name: '신상품 발주', value: '140004' },
            { name: '긴급 발주', value: '140005' },
            { name: '행사상품 발주', value: '140006' }
        ]
    },
    { type: 'dateRange', label: '납기예정일', value: '', name: 'dueDate', fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
    { type: 'text', label: '공급업체명', value: '', placeholder: '공급업체명을 입력하세요.', name: 'orderTo' }
];

const filterOptions = ref({}); // 검색 조건 기본값

const searchData = (searchOptions) => {
    console.log('Searching with options:', searchOptions);
    getOrdersData(searchOptions);
};

const resetSearchData = () => {
    const searchFormRef = searchDetailTableRef.value.searchFormRef;
    searchFormRef.searchOptions = { ...filterOptions.value }; //기본값으로 초기화
};

const getOrdersData = async (options) => {
    const req = await axios.get('/api/orders/supplier', {
        params: {
            ...options
        }
    });
    const result = req.data;
    const viewData = result.map((item) => {
        return {
            ...item,
            orderDate: convertDate(item.orderDate),
            totalAmount: Number(item.totalAmount).toLocaleString()
        };
    });
    items.value = viewData;
    // console.log(req);
};

const actionHandler = (rowData) => {
    router.push(`/orders/view/${rowData.orderId}`);
};

const getSupplierInfo = async (empId) => {
    const req = await axios.get('/api/orders/user/compInfo', {
        params: {
            empId: empId
        }
    });

    //지점 정보
    if (req.data.compType == '100003') return req.data;
    return undefined;
};

const defaultFilterOptions = async (userInfo) => {
    const options = {};
    filters.value.filters.forEach((filter) => {
        if (filter.type === 'dateRange') {
            options[`${filter.name}From`] = '';
            options[`${filter.name}To`] = '';
        } else {
            options[filter.name] = '';
        }
    }); //filtrer의 name을 key로 사용

    // 기본 날짜 설정
    const defaultOrderDateFrom = new Date();
    // 1년 전부터 조회하기 위해 기본 날짜 설정
    defaultOrderDateFrom.setFullYear(defaultOrderDateFrom.getFullYear() - 1);
    options.orderDateFrom = defaultOrderDateFrom;

    // 지점 정보 기져오기
    const supplierInfo = await getSupplierInfo(userInfo.employeeId);
    options.orderTo = supplierInfo?.compName || '';

    options.orderStatus = '030002'; // 기본 상태: 승인

    // 검색조건 기본값 설정
    filterOptions.value = { ...options };
};

onMounted(async () => {
    //사용자 정보
    const userInfo = useAuth().user.value;
    // 검색조건 기본값 설정
    await defaultFilterOptions(userInfo);
    resetSearchData();
    // 지점 발주서 정보 조회
    getOrdersData(filterOptions.value);
});
</script>
<template>
    <SearchDetailTable ref="searchDetailTableRef" :filters="filters" :items="items" :header="header" @searchData="searchData" @resetSearchOptions="resetSearchData" @actionHandler="actionHandler" />
</template>
