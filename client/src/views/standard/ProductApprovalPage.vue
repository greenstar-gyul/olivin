<script setup>
import StandardApproval from '@/components/common/StandardApproval.vue';
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import axios from '@/service/axios';

const API_BASE_URL = '/api/products';

const currentUser = ref({
    empId: '',
    employeeId: '',
    empName: ''
});

const formatDateForOracle = (dateInput) => {
    if (!dateInput) return null;

    try {
        let date;
        if (dateInput instanceof Date) {
            date = dateInput;
        } else if (typeof dateInput === 'string') {
            if (/^\d{4}-\d{2}-\d{2}$/.test(dateInput.trim())) {
                return dateInput.trim();
            }
            date = new Date(dateInput.trim());
        } else {
            date = new Date(dateInput);
        }

        if (isNaN(date.getTime())) {
            throw new Error('유효하지 않은 날짜');
        }

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        return `${year}-${month}-${day}`;
    } catch (error) {
        return null;
    }
};

const getCurrentUser = async () => {
    try {
        const response = await axios.get('/api/auth/me');

        if (response.data.success && response.data.data) {
            const userData = response.data.data;

            let employeeId = 'olivin10001';
            let empName = '김홍인';

            const possibleUserSources = [userData.user, userData, userData.employee, userData.userInfo, userData.loginUser];

            for (const userSource of possibleUserSources) {
                if (userSource && typeof userSource === 'object') {
                    const possibleEmployeeIds = [userSource.employeeId, userSource.employee_id, userSource.EMPLOYEE_ID];
                    const possibleEmpNames = [userSource.empName, userSource.emp_name, userSource.EMP_NAME];

                    const foundEmployeeId = possibleEmployeeIds.find((id) => id && String(id).trim() !== '');
                    const foundEmpName = possibleEmpNames.find((name) => name && String(name).trim() !== '');

                    if (foundEmployeeId) employeeId = String(foundEmployeeId).trim();
                    if (foundEmpName) empName = String(foundEmpName).trim();

                    if (foundEmployeeId && foundEmpName) break;
                }
            }

            currentUser.value = {
                empId: employeeId,
                employeeId: employeeId,
                empName: empName
            };

            return currentUser.value;
        }
    } catch (error) {
        currentUser.value = {
            empId: 'olivin10001',
            employeeId: 'olivin10001',
            empName: '김홍인'
        };
        return currentUser.value;
    }
};

const categoryMainOptions = [
    { name: '스킨케어', value: '110001' },
    { name: '메이크업', value: '110002' },
    { name: '클렌징', value: '110003' },
    { name: '헤어케어', value: '110004' },
    { name: '구강용품', value: '110005' },
    { name: '선케어', value: '110006' },
    { name: '뷰티소품', value: '110007' },
    { name: '건강/기능 식품', value: '110008' },
    { name: '푸드', value: '110009' }
];

const categorySubOptions = {
    110001: [
        { name: '스킨/토너', value: '121001' },
        { name: '에센스/세럼/앰플', value: '121002' },
        { name: '크림', value: '121003' },
        { name: '로션', value: '121004' },
        { name: '미스트/오일', value: '121005' },
        { name: '스킨케어 디바이스', value: '121006' }
    ],
    110002: [
        { name: '베이스 메이크업', value: '122001' },
        { name: '아이 메이크업', value: '122002' },
        { name: '치크&컨투어', value: '122003' },
        { name: '립 메이크업', value: '122004' },
        { name: '피니시&픽서', value: '122005' },
        { name: '네일 메이크업', value: '122006' }
    ],
    110003: [
        { name: '클렌징폼/젤', value: '123001' },
        { name: '오일/밤', value: '123002' },
        { name: '워터/밀크', value: '123003' },
        { name: '필링&스크럽', value: '123004' },
        { name: '티슈/패드', value: '123005' },
        { name: '립&아이리무버', value: '123006' },
        { name: '클렌징 디바이스', value: '123007' }
    ],
    110004: [
        { name: '샴푸/린스', value: '124001' },
        { name: '트리트먼트/팩', value: '124002' },
        { name: '두피앰플/토닉', value: '124003' },
        { name: '헤어에센스', value: '124004' },
        { name: '염색약/펌', value: '124005' },
        { name: '헤어기기/브러시', value: '124006' },
        { name: '스타일링', value: '124007' }
    ],
    110005: [
        { name: '칫솔', value: '125001' },
        { name: '치약', value: '125002' },
        { name: '애프터구강케어', value: '125003' },
        { name: '구강가전', value: '125004' }
    ],
    110006: [
        { name: '선크림', value: '126001' },
        { name: '선스틱', value: '126002' },
        { name: '선쿠션', value: '126003' },
        { name: '선스프레이/선패치', value: '126004' },
        { name: '태닝/애프터선', value: '126005' }
    ],
    110007: [
        { name: '메이크업소품', value: '127001' },
        { name: '아이소품', value: '127002' },
        { name: '스킨케어소품', value: '127003' },
        { name: '헤어소품', value: '127004' },
        { name: '네일/바디소품', value: '127005' },
        { name: '뷰티잡화', value: '127006' }
    ],
    110008: [
        { name: '비타민', value: '128001' },
        { name: '영양제', value: '128002' },
        { name: '유산균', value: '128003' },
        { name: '슬리밍/이너뷰티', value: '128004' }
    ],
    110009: [
        { name: '식단관리/이너뷰티', value: '129001' },
        { name: '과자/초콜릿/디저트', value: '129002' },
        { name: '상수/음료/커피', value: '129003' },
        { name: '간편식/요리', value: '129004' },
        { name: '베이비푸드', value: '129005' }
    ]
};

const filters = ref({
    title: '승인 요청 조회',
    filters: [
        { type: 'text', label: '제품명', value: '', placeholder: '제품명을 입력하세요', name: 'productName' },
        { type: 'text', label: '브랜드', value: '', placeholder: '브랜드명을 입력하세요', name: 'vendorName' },
        { type: 'select', label: '카테고리', value: '', placeholder: '카테고리를 선택하세요', name: 'categoryMain', options: categoryMainOptions },
        { type: 'select', label: '세부카테고리', value: '', placeholder: '세부카테고리를 선택하세요', name: 'categorySub', options: [] },
        { type: 'text', label: '등록자', value: '', placeholder: '등록자를 입력하세요', name: 'regUser' },
        { type: 'dateRange', label: '등록일', value: '', fromPlaceholder: '시작일', toPlaceholder: '종료일', name: 'regDateRange' }
    ]
});

const items = ref([]);
const selectedProduct = ref(null);

const header = ref({
    title: '제품 승인 요청 목록',
    header: {
        productName: '제품명',
        vendorName: '브랜드',
        categoryMain: '카테고리',
        categorySub: '세부카테고리',
        productSpec: '용량',
        unit: '단위',
        packQty: '입수량',
        safetyStock: '안전재고'
    },
    rightAligned: ['packQty', 'sellPrice']
});

const inputs = computed(() => ({
    title: '제품 정보 및 승인 처리',
    inputs: [
        { type: 'text', label: '제품ID', value: selectedProduct.value?.productId || '', name: 'productId', readonly: true },
        { type: 'text', label: '제품명', value: selectedProduct.value?.productName || '', name: 'productName', readonly: true },
        { type: 'text', label: '매장코드', value: selectedProduct.value?.compId || '', name: 'compId', readonly: true },
        { type: 'text', label: '브랜드', value: selectedProduct.value?.vendorName || '', name: 'vendorName', readonly: true },
        { type: 'text', label: '카테고리', value: selectedProduct.value?.categoryMain || '', name: 'categoryMain', readonly: true },
        { type: 'text', label: '세부카테고리', value: selectedProduct.value?.categorySub || '', name: 'categorySub', readonly: true },
        { type: 'text', label: '용량', value: selectedProduct.value?.productSpec || '', name: 'productSpec', readonly: true },
        { type: 'text', label: '단위', value: selectedProduct.value?.unit || '', name: 'unit', readonly: true },
        { type: 'number', label: '입수량', value: selectedProduct.value?.packQty || '', name: 'packQty', readonly: true },
        { type: 'number', label: '안전재고', value: selectedProduct.value?.safetyStock || '', name: 'safetyStock', readonly: true },
        { type: 'number', label: '구매가격', value: selectedProduct.value?.purchasePrice || '', name: 'purchasePrice', readonly: true },
        { type: 'number', label: '판매가격', value: selectedProduct.value?.sellPrice || '', name: 'sellPrice', readonly: true },
        { type: 'text', label: '등록자', value: selectedProduct.value?.regUserName || '', name: 'regUser', readonly: true },
        { type: 'date', label: '등록일자', value: selectedProduct.value?.regDate || '', name: 'regDate', readonly: true },
        { type: 'textarea', label: '비고/처리사유', value: '', name: 'note', readonly: false, placeholder: '승인 또는 반려 사유를 입력하세요' }
    ]
}));

const standardApprovalRef = ref(null);

const handleSearchCategoryMainChange = (categoryMainValue) => {
    const categorySubFilter = filters.value.filters.find((f) => f.name === 'categorySub');
    if (categorySubFilter) {
        categorySubFilter.options = categorySubOptions[categoryMainValue] || [];
    }

    if (standardApprovalRef.value?.searchFormRef) {
        standardApprovalRef.value.searchFormRef.searchOptions.categorySub = '';
    }
};

const getCategoryMainName = (code) => {
    const categoryMap = {
        110001: '스킨케어',
        110002: '메이크업',
        110003: '클렌징',
        110004: '헤어케어',
        110005: '구강용품',
        110006: '선케어',
        110007: '뷰티소품',
        110008: '건강/기능 식품',
        110009: '푸드'
    };
    return categoryMap[code] || code;
};

const getCategorySubName = (code) => {
    const categorySubMap = {
        121001: '스킨/토너',
        121002: '에센스/세럼/앰플',
        121003: '크림',
        121004: '로션',
        121005: '미스트/오일',
        121006: '스킨케어 디바이스',
        122001: '베이스 메이크업',
        122002: '아이 메이크업',
        122003: '치크&컨투어',
        122004: '립 메이크업',
        122005: '피니시&픽서',
        122006: '네일 메이크업',
        123001: '클렌징폼/젤',
        123002: '오일/밤',
        123003: '워터/밀크',
        123004: '필링&스크럽',
        123005: '티슈/패드',
        123006: '립&아이리무버',
        123007: '클렌징 디바이스',
        124001: '샴푸/린스',
        124002: '트리트먼트/팩',
        124003: '두피앰플/토닉',
        124004: '헤어에센스',
        124005: '염색약/펌',
        124006: '헤어기기/브러시',
        124007: '스타일링',
        125001: '칫솔',
        125002: '치약',
        125003: '애프터구강케어',
        125004: '구강가전',
        126001: '선크림',
        126002: '선스틱',
        126003: '선쿠션',
        126004: '선스프레이/선패치',
        126005: '태닝/애프터선',
        127001: '메이크업소품',
        127002: '아이소품',
        127003: '스킨케어소품',
        127004: '헤어소품',
        127005: '네일/바디소품',
        127006: '뷰티잡화',
        128001: '비타민',
        128002: '영양제',
        128003: '유산균',
        128004: '슬리밍/이너뷰티',
        129001: '식단관리/이너뷰티',
        129002: '과자/초콜릿/디저트',
        129003: '상수/음료/커피',
        129004: '간편식/요리',
        129005: '베이비푸드'
    };
    return categorySubMap[code] || code;
};

const getUnitName = (code) => {
    const unitMap = {
        130001: 'ml',
        130002: 'g',
        130003: 'ea',
        130004: 'box',
        130005: 'pack'
    };
    return unitMap[code] || code;
};

const getStatusName = (code) => {
    const statusMap = {
        '040001': '완료',
        '040002': '대기',
        '040003': '반려',
        '040004': '중단'
    };
    return statusMap[code] || code;
};

const formatDate = (dateString) => {
    if (!dateString) return '';
    try {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');

        return `${year}.${month}.${day}`;
    } catch (error) {
        return dateString;
    }
};

const filterProductData = (product, index = 0) => {
    return {
        id: product.productId || `temp_product_${Date.now()}_${index}`,
        productId: product.productId,
        productName: product.productName,
        vendorName: product.vendorName,
        compId: product.compId,
        productSpec: product.productSpec,
        packQty: product.packQty,
        safetyStock: product.safetyStock,
        purchasePrice: product.purchasePrice,
        sellPrice: product.sellPrice,
        note: product.note,
        categoryMain: getCategoryMainName(product.categoryMain),
        categorySub: getCategorySubName(product.categorySub),
        unit: getUnitName(product.unit),
        status: getStatusName(product.status),
        regUserName: product.regUserName || product.regUser,
        regDate: product.regDate ? formatDate(product.regDate) : '',
        categoryMainCode: product.categoryMain,
        categorySubCode: product.categorySub,
        unitCode: product.unit,
        statusCode: product.status,
        regUserCode: product.regUser
    };
};

const loadPendingProducts = async () => {
    try {
        const response = await axios.get(`${API_BASE_URL}/pending`);

        let products = [];
        
        if (response.data.result_code === 'SUCCESS' && response.data.data) {
            products = response.data.data;
        } else if (Array.isArray(response.data)) {
            products = response.data;
        } else {
            products = [];
        }

        items.value = products.map((product, index) => filterProductData(product, index));
        
    } catch (error) {
        try {
            const fallbackResponse = await axios.get(`${API_BASE_URL}`);

            let allProducts = [];
            if (fallbackResponse.data.result_code === 'SUCCESS' && fallbackResponse.data.data) {
                allProducts = fallbackResponse.data.data;
            } else if (Array.isArray(fallbackResponse.data)) {
                allProducts = fallbackResponse.data;
            }

            if (allProducts.length > 0) {
                const pendingProducts = allProducts.filter((product) => 
                    product.status === '040002' || product.statusCode === '040002'
                );
                items.value = pendingProducts.map((product, index) => filterProductData(product, index));
            } else {
                items.value = [];
            }
        } catch (fallbackError) {
            alert('제품 목록을 불러오는데 실패했습니다.');
            items.value = [];
        }
    }
};

// ✅ 수정된 검색 함수 - 디버깅 및 응답 구조 처리 개선
const searchData = async (searchOptions) => {
    console.log('🔍 검색 조건:', searchOptions);
    
    try {
        const params = {};

        // ✅ 검색 조건 파라미터 구성 - 더 정확한 처리
        if (searchOptions.productName?.trim()) {
            params.productName = searchOptions.productName.trim();
        }
        if (searchOptions.vendorName?.trim()) {
            params.vendorName = searchOptions.vendorName.trim();
        }
        if (searchOptions.categoryMain?.trim()) {
            params.categoryMain = searchOptions.categoryMain.trim();
        }
        if (searchOptions.categorySub?.trim()) {
            params.categorySub = searchOptions.categorySub.trim();
        }
        if (searchOptions.regUser?.trim()) {
            params.regUser = searchOptions.regUser.trim();
        }

        // ✅ 날짜 범위 처리 개선
        if (searchOptions.regDateRangeFrom && searchOptions.regDateRangeTo) {
            try {
                const fromDate = new Date(searchOptions.regDateRangeFrom);
                const toDate = new Date(searchOptions.regDateRangeTo);

                if (!isNaN(fromDate.getTime()) && !isNaN(toDate.getTime())) {
                    params.regDateFrom = fromDate.toISOString().split('T')[0];
                    params.regDateTo = toDate.toISOString().split('T')[0];
                    console.log('📅 날짜 범위:', { from: params.regDateFrom, to: params.regDateTo });
                }
            } catch (dateError) {
                console.warn('⚠️ 날짜 변환 오류:', dateError);
            }
        }

        // ✅ 승인 페이지에서는 항상 승인 대기 상태만 조회
        params.status = '040002';

        console.log('📤 최종 검색 파라미터:', params);

        // ✅ API 호출
        const response = await axios.get(`${API_BASE_URL}/search`, { params });
        console.log('📥 API 응답:', response.data);

        // ✅ 응답 데이터 처리 개선
        let searchResults = [];
        
        if (response.data?.result_code === 'SUCCESS' && response.data.data) {
            searchResults = response.data.data;
        } else if (response.data?.success && response.data.data) {
            searchResults = response.data.data;
        } else if (Array.isArray(response.data)) {
            searchResults = response.data;
        } else if (response.data?.products && Array.isArray(response.data.products)) {
            searchResults = response.data.products;
        } else {
            console.warn('⚠️ 예상하지 못한 응답 구조:', response.data);
            searchResults = [];
        }

        console.log('🔍 검색 결과:', searchResults);

        // ✅ 검색 결과를 테이블에 표시
        if (Array.isArray(searchResults)) {
            items.value = searchResults.map((product, index) => filterProductData(product, index));
            console.log(`✅ ${items.value.length}개의 제품이 검색되었습니다.`);
        } else {
            console.warn('⚠️ 검색 결과가 배열이 아닙니다:', searchResults);
            items.value = [];
        }

    } catch (error) {
        console.error('❌ 검색 실패:', error);

        let errorMessage = '검색 중 오류가 발생했습니다.';
        if (error.response?.status === 500) {
            errorMessage = '서버 내부 오류가 발생했습니다. 검색 조건을 확인해주세요.';
        } else if (error.response?.status === 400) {
            errorMessage = '검색 조건이 올바르지 않습니다.';
        } else if (error.response?.status === 404) {
            errorMessage = '검색 결과가 없습니다.';
        }

        console.error('❌ 에러 상세:', {
            status: error.response?.status,
            data: error.response?.data,
            message: error.message
        });

        alert(errorMessage);
        items.value = [];
    }

    // ✅ 선택된 제품 초기화
    selectedProduct.value = null;
};

const onRowSelect = (row) => {
    selectedProduct.value = row;
};

const handleApprove = async (approvalData) => {
    const targetProduct = approvalData.selectedItem || selectedProduct.value;

    if (!targetProduct) {
        alert('승인할 제품을 선택해주세요.');
        return;
    }

    const reason = approvalData?.note || '승인 완료';

    if (!confirm(`제품 "${targetProduct.productName}"을(를) 승인하시겠습니까?`)) {
        return;
    }

    try {
        const currentUserData = await getCurrentUser();

        const response = await axios.post(`${API_BASE_URL}/${targetProduct.productId}/approve`, {
            approver: currentUserData.employeeId,
            reason: reason,
            status: '040001',
            approveDate: formatDateForOracle(new Date())
        });

        const isSuccess = response.status === 200 && (
            response.data?.success === true ||
            response.data?.result_code === 'SUCCESS' ||
            response.data?.status === 'success' ||
            (response.data?.message && response.data.message.includes('승인'))
        );

        if (isSuccess) {
            const successMessage = response.data?.message || 
                                 `제품 "${targetProduct.productName}"이 승인되었습니다. (승인자: ${currentUserData.empName})`;
            
            alert(successMessage);

            items.value = items.value.filter((item) => item.productId !== targetProduct.productId);
            selectedProduct.value = null;

            if (standardApprovalRef.value) {
                await nextTick();
                
                if (standardApprovalRef.value.selectedItems) {
                    standardApprovalRef.value.selectedItems = null;
                }
                if (standardApprovalRef.value.selectedProductId) {
                    standardApprovalRef.value.selectedProductId = null;
                }
                
                if (standardApprovalRef.value.clearAllRadioSelections) {
                    standardApprovalRef.value.clearAllRadioSelections();
                }
            }
            
        } else {
            const errorMessage = response.data?.message || 
                               response.data?.error || 
                               '알 수 없는 오류가 발생했습니다.';
            alert('승인에 실패했습니다: ' + errorMessage);
        }
    } catch (error) {
        const errorMessage = error.response?.data?.message || 
                           error.response?.data?.error || 
                           error.message || 
                           '승인 처리 중 오류가 발생했습니다.';
        alert(`승인 처리 실패: ${errorMessage}`);
    }
};

const handleReject = async (rejectionData) => {
    const targetProduct = rejectionData.selectedItem || selectedProduct.value;

    if (!targetProduct) {
        alert('반려할 제품을 선택해주세요.');
        return;
    }

    const reason = rejectionData?.note;

    if (!reason || reason.trim() === '') {
        alert('반려 사유를 입력해주세요.');
        return;
    }

    if (!confirm(`제품 "${targetProduct.productName}"을(를) 반려하시겠습니까?`)) {
        return;
    }

    try {
        const currentUserData = await getCurrentUser();

        const response = await axios.post(`${API_BASE_URL}/${targetProduct.productId}/reject`, {
            approver: currentUserData.employeeId,
            reason: reason,
            status: '040003',
            rejectDate: formatDateForOracle(new Date())
        });

        const isSuccess = response.status === 200 && (
            response.data?.success === true ||
            response.data?.result_code === 'SUCCESS' ||
            response.data?.status === 'success' ||
            (response.data?.message && response.data.message.includes('반려'))
        );

        if (isSuccess) {
            const successMessage = response.data?.message || 
                                 `제품 "${targetProduct.productName}"이 반려되었습니다. (반려자: ${currentUserData.empName})`;
            
            alert(successMessage);

            items.value = items.value.filter((item) => item.productId !== targetProduct.productId);
            selectedProduct.value = null;

            if (standardApprovalRef.value) {
                await nextTick();
                
                if (standardApprovalRef.value.selectedItems) {
                    standardApprovalRef.value.selectedItems = null;
                }
                if (standardApprovalRef.value.selectedProductId) {
                    standardApprovalRef.value.selectedProductId = null;
                }
                
                if (standardApprovalRef.value.clearAllRadioSelections) {
                    standardApprovalRef.value.clearAllRadioSelections();
                }
            }
            
        } else {
            const errorMessage = response.data?.message || 
                               response.data?.error || 
                               '알 수 없는 오류가 발생했습니다.';
            alert('반려에 실패했습니다: ' + errorMessage);
        }
    } catch (error) {
        const errorMessage = error.response?.data?.message || 
                           error.response?.data?.error || 
                           error.message || 
                           '반려 처리 중 오류가 발생했습니다.';
        alert(`반려 처리 실패: ${errorMessage}`);
    }
};

const saveData = (inputData) => {
    // 필요한 경우 추가 처리
};

onMounted(async () => {
    try {
        await getCurrentUser();
        await loadPendingProducts();

        setTimeout(() => {
            if (standardApprovalRef.value?.searchFormRef) {
                watch(
                    () => {
                        const searchFormRef = standardApprovalRef.value.searchFormRef;
                        return searchFormRef?.searchOptions?.categoryMain;
                    },
                    (newValue, oldValue) => {
                        if (newValue && newValue !== oldValue) {
                            handleSearchCategoryMainChange(newValue);
                        }
                    },
                    { immediate: false }
                );
            }
        }, 200);
    } catch (error) {
        console.error('초기 데이터 로드 실패:', error);
    }
});
</script>

<template>
    <StandardApproval
        ref="standardApprovalRef"
        :filters="filters"
        :items="items"
        :header="header"
        :inputs="inputs"
        :scrollHeight="'600px'"
        @searchData="searchData"
        @saveData="saveData"
        @rowSelect="onRowSelect"
        @approve="handleApprove"
        @reject="handleReject"
    />
</template>