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
    title: '발주서 조회',
    header: {
        orderId: '발주서코드',
        orderDate: '발주요청일',
        orderTypeName: '발주유형',
        orderTitle: '발주명',
        orderFrom: '발주처',
        reasonName: '발주사유',
        totalAmount: '총 금액',
        dueDate: '납기예정일',
        orderStatusName: '발주상태',
        creatorName: '등록자'
    },
    rightAligned: ['totalAmount']
});

const items = ref([]);

const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
    { type: 'text', label: '발주서코드', value: '', placeholder: '발주서코드를 입력하세요.', name: 'orderId' },
    {
        type: 'select',
        label: '발주상태',
        value: '',
        placeholder: '발주상태를 선택하세요.',
        name: 'orderStatus',
        options: [
            { name: '승인대기', value: '030001' },
            { name: '승인', value: '030002' },
            { name: '출고완료', value: '030003' },
            { name: '반려', value: '030004' }
        ]
    },
    { type: 'dateRange', label: '발주요청일', value: '', name: 'orderDate', fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
    { type: 'text', label: '발주명', value: '', placeholder: '발주명을 입력하세요.', name: 'orderTitle' },
    {
        type: 'select',
        label: '발주사유',
        value: '',
        placeholder: '발주사유를 선택하세요.',
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
    { type: 'dateRange', label: '납기예정일', value: '2025-07-01', name: 'dueDate', fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
    {
        type: 'select',
        label: '발주유형',
        value: '',
        placeholder: '발주유형을 선택하세요.',
        name: 'orderType',
        options: [
            { name: '발주 요청(본사발주)', value: '150001' },
            { name: '발주 요청(지점발주)', value: '150002' },
            { name: '제안 발주', value: '150003' }
        ]
    },
    { type: 'text', label: '발주처', value: '', placeholder: '발주처를 입력하세요.', name: 'orderFrom' }
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
    const req = await axios.get('/api/orders/head', {
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
  let move = "?move=";
  if (useAuth().user.value.compId === 'COM10001') {
    move += "head";
  }
  router.push(`/orders/view/${rowData.orderId}${move}`);
}

const defaultFilterOptions = async () => {
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

    // 승인 대기
    options.orderStatus = '030001';

    // 검색조건 기본값 설정
    filterOptions.value = { ...options };
};

onMounted(async () => {
    // 검색조건 기본값 설정
    await defaultFilterOptions();
    resetSearchData();
    // 지점 발주서 정보 조회
    getOrdersData(filterOptions.value);
});
</script>
<template>
    <SearchDetailTable ref="searchDetailTableRef" :filters="filters" :items="items" :header="header" @searchData="searchData" @resetSearchOptions="resetSearchData" @actionHandler="actionHandler" />
</template>
