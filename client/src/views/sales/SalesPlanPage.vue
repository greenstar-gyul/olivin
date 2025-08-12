<script setup>
import SearchTable from '@/components/common/SearchTable.vue';
import { onMounted, ref } from 'vue';
import { convertDate } from '@/utils/dateUtils';
import axios from '@/service/axios';
import { useAuth } from '@/composables/useAuth';

const searchTableRef = ref(null);

const header = ref({
    title: '매출 계획 조회',
    header: {
        year: '연도',
        quarter: '분기',
        compName: '지점명',
        targetAmount: '목표 금액',
        regUserName: '등록자',
        reason: '사유'
    },
    rightAligned: ['year', 'quarter', 'totalAmount']
});

const items = ref([]);

const filters = ref({});
filters.value.title = '조회 조건';
filters.value.filters = [
    { type: 'text', label: '연도', value: '', placeholder: '연도를 입력하세요.', name: 'year' },
    { type: 'text', label: '분기', value: '', placeholder: '분기를 입력하세요.', name: 'quarter' },
    { type: 'text', label: '등록자', value: '', placeholder: '등록자를 입력하세요.', name: 'regUserName' },
    { type: 'text', label: '사유', value: '', placeholder: '사유를 입력하세요.', name: 'reason' },
    { type: 'text', label: '지점명', value: '', placeholder: '지점명을 입력하세요.', name: 'compName' }
];

const filterOptions = ref({}); // 검색 조건 기본값

const searchData = (searchOptions) => {
    console.log('Searching with options:', searchOptions);
    getOrdersData(searchOptions);
};

const resetSearchData = () => {
    const searchFormRef = searchTableRef.value.searchFormRef;
    searchFormRef.searchOptions = { ...filterOptions.value }; //기본값으로 초기화
};

const getOrdersData = async (options) => {
    const req = await axios.get('/api/salesPlans', {
        params: {
            ...options
        }
    });
    const result = req.data;
    const viewData = result.map((item) => {
        return {
            ...item,
            regDate: convertDate(item.regDate),
            targetAmount: Number(item.targetAmount).toLocaleString() + '원'
        };
    });
    items.value = viewData;
    // console.log(req);
};

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
    options.year = defaultOrderDateFrom.getFullYear();

    // 지점 정보 기져오기
    const branchInfo = await getBranchInfo(userInfo.employeeId);
    options.compName = branchInfo?.compName || '';

    // 검색조건 기본값 설정
    filterOptions.value = { ...options };
};

onMounted(async () => {
    //사용자 정보
    const userInfo = useAuth().user.value;
    // 검색조건 기본값 설정
    await defaultFilterOptions(userInfo);
    resetSearchData();
    // 매출 계획 정보 조회
    getOrdersData(filterOptions.value);
});
</script>
<template>
    <SearchTable ref="searchTableRef" :filters="filters" :items="items" :header="header" @searchData="searchData" @resetSearchOptions="resetSearchData" @open-search-modal="handleOpenModal" />
</template>
