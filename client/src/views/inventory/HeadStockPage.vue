<script setup>
import { onMounted, ref } from 'vue';
import SearchTable from '../../components/common/SearchTable.vue';
import axios from '@/service/axios';
import DialogModal from '@/components/overray/DialogModal.vue';
import Button from 'primevue/button';
import { useAuth } from '@/composables/useAuth';

// 사용자 인증 정보 가져오기
const { user } = useAuth();

// 조회 폼의 헤더 정보 (조회 테이블 컬럼 이름)
const mainHeader = ref({
    title: '재고 현황', // 조회 폼 제목
    header: {
        // 테이블의 헤더 정보
        productId: '제품번호',
        productName: '제품명',
        categoryMain: '대분류',
        categorySub: '소분류',
        productSpec: '규격',
        vendorName: '공급사',
        stockQuantity: '재고수량(개)',
        safetyStock: '안전 재고(개)'
    },
    rightAligned: ['stockQuantity', 'safetyStock'] // 오른쪽 정렬할 컬럼 리스트
});

// 조회할 데이터
const mainItems = ref([]);
const allStockData = ref([]); // 전체 재고 데이터 저장용
const safetyStockFilterEnabled = ref(false); // 안전재고 필터 활성화 여부

const lotHeader = ref({
    title: 'LOT별 재고 현황', // 조회 폼 제목
    header: {
        // 테이블의 헤더 정보
        productId: '제품번호',
        productName: '제품명',
        lotNo: 'LOT',
        stockQuantity: '재고수량(개)'
    },
    rightAligned: ['stockQuantity', 'safetyStock'] // 오른쪽 정렬할 컬럼 리스트
});

const lotItems = ref([]);

// 검색 조건 필터 설정
const filters = ref({});
filters.value.title = '재고 검색'; // 검색 조건 폼 제목
filters.value.filters = [
    // 검색 조건 필터 목록
    { type: 'item-search', label: '제품명', value: '', placeholder: '제품번호 / 제품명 검색', name: 'productModal' },
    { type: 'item-search', label: '제품분류', value: '', placeholder: '제품분류 선택', name: 'productType' },
    { 
        type: 'item-search', 
        label: '공급사', 
        value: '', 
        placeholder: '공급사 검색', 
        name: 'publisher',
        disabled: user.value && user.value.compId && user.value.compId !== 'COM10001' // 공급업체인 경우 비활성화
    }
];

// 모달창의 테이블 헤더 정보
// field: 테이블의 각 컬럼에 해당하는 데이터의 키
// header: 테이블의 각 컬럼에 해당하는 헤더 이름

// 제품 모달창 헤더
const productHeaders = ref([
    { field: 'productId', header: '제품번호' },
    { field: 'productName', header: '제품명' },
    { field: 'categoryMain', header: '대분류' },
    { field: 'categorySub', header: '소분류' },
    { field: 'vendorName', header: '공급사' },
    { field: 'productSpec', header: '규격' }
]);

// 모달창의 데이터 아이템
// 제품 모달창 아이템
const productItems = ref([]);

// =====
// 여러개의 모달창이 필요할 경우 여러개를 각각 정의
const typeHeaders = ref([
    { field: 'categoryMainName', header: '대분류' },
    { field: 'categorySubName', header: '소분류' }
]);

const typeItems = ref([]);

const publisherHeaders = ref([
    { field: 'vendorName', header: '업체명' },
    { field: 'phone', header: '전화번호' }
]);

const publisherItems = ref([]);

// =====

const loadStockData = async () => {
    try {
        let searchVendor = '';
        // 공급업체이면 검색폼에 자신의 공급사명을 기본값으로 설정
        if (user.value && user.value.compId && user.value.compId !== 'COM10001') {
            if (searchFormRef.value?.searchOptions) {
                searchFormRef.value.searchOptions.publisher = user.value.compName;
                searchVendor = user.value.compName;
            }
        }

        // 서버에서 재고 데이터를 가져오기
        const response = await axios.get('/api/inventory/headStock/search', {
            params: {
                productName: '',
                categorySub: '',
                vendorName: searchVendor
            }
        });
        allStockData.value = await response.data; // 전체 데이터를 allStockData에 저장
        applyFilters(); // 필터 적용

        console.log('Stock data loaded:', allStockData.value);
    } catch (error) {
        console.error('Error loading stock data:', error);
    }
};

// 필터 적용 함수
const applyFilters = () => {
    let filteredData = [...allStockData.value];

    // 안전재고 필터가 활성화된 경우
    if (safetyStockFilterEnabled.value) {
        filteredData = filteredData.filter((item) => {
            const stock = item.stockQuantity || 0;
            const safety = item.safetyStock || 0;
            return stock <= safety; // 재고가 안전재고 이하인 항목만 필터링
        });
    }

    mainItems.value = filteredData;
    console.log('Filtered data applied:', mainItems.value.length, 'items');
};

// 안전재고 필터 토글 함수
const toggleSafetyStockFilter = () => {
    safetyStockFilterEnabled.value = !safetyStockFilterEnabled.value;
    applyFilters();
    clearSelection(); // 필터 변경 시 선택 초기화
};

// 검색 모달이 필요할 때 선언해서 사용.
// 모달의 visible 상태를 관리하는 ref 변수
const productModalVisible = ref(false);
const typeModalVisible = ref(false);
const publisherModalVisible = ref(false);

const loadProductItems = async () => {
    try {
        // 공급업체이면 자신의 제품만 조회
        const params = {};
        if (user.value && user.value.compId && user.value.compId !== 'COM10001') {
            params.vendorName = user.value.compName;
        }

        // 제품 목록을 서버에서 가져오기
        const response = await axios.get('/api/search/products/all', { params });
        productItems.value = await response.data; // 서버에서 받은 데이터를 productItems에 저장

        console.log('Product items loaded:', productItems.value);
    } catch (error) {
        console.error('Error loading product items:', error);
    }
};

const loadTypeItems = async () => {
    try {
        // 제품 분류 목록을 서버에서 가져오기
        const response = await axios.get('/api/search/product-types/all');
        typeItems.value = await response.data; // 서버에서 받은 데이터를 typeItems에 저장

        console.log('Product type items loaded:', typeItems.value);
    } catch (error) {
        console.error('Error loading product type items:', error);
    }
};

const loadPublisherItems = async () => {
    try {
        // 공급업체이면 자신의 공급사만 조회
        const params = {};
        if (user.value && user.value.compId && user.value.compId !== 'COM10001') {
            params.vendorName = user.value.compName;
        }

        // 공급사 목록을 서버에서 가져오기
        const response = await axios.get('/api/search/vendors/all', { params });
        publisherItems.value = await response.data; // 서버에서 받은 데이터를 publisherItems에 저장

        console.log('Publisher items loaded:', publisherItems.value);
    } catch (error) {
        console.error('Error loading publisher items:', error);
    }
};

// 검색 폼에서 검색 버튼 클릭 시 호출되는 함수
const searchData = async (searchOptions) => {
    console.log('Searching with options:', searchOptions);
    await searchStocks(searchOptions);
};

// 검색 모달을 열 때 호출되는 함수
// case 문을 사용하여 모달 이름(item-search 타입의 name을 따름)에 따라 다른 모달을 열 수 있도록 구현
const handleOpenModal = (filterName) => {
    console.log('Open modal for filter:', filterName);
    
    // 공급업체인 경우 공급사 모달은 열지 않음
    if (filterName === 'publisher' && user.value && user.value.compId && user.value.compId !== 'COM10001') {
        console.log('🏢 공급업체는 공급사 변경 불가');
        return;
    }
    
    switch (filterName) {
        case 'productModal':
            loadProductItems();
            productModalVisible.value = true;
            break;
        case 'productType':
            loadTypeItems();
            typeModalVisible.value = true;
            break;
        case 'publisher':
            loadPublisherItems();
            publisherModalVisible.value = true;
            break;
        default:
            console.warn('No modal defined for filter:', filterName);
    }
};

// 모달창 닫기 함수
const closeProductModal = () => {
    productModalVisible.value = false;
};
const closeTypeModal = () => {
    typeModalVisible.value = false;
};
const closePublisherModal = () => {
    publisherModalVisible.value = false;
};

// 모달창 확인 버튼 클릭 시 호출되는 함수
// 필요한 로직 작성

// SearchForm의 ref를 추가
const searchFormRef = ref(null);
const mainTableRef = ref(null); // BasicTable의 ref 추가

const updateFilterValue = (filterName, selectedItem) => {
    // SearchForm의 searchOptions에 직접 값 설정
    if (searchFormRef.value.searchOptions) {
        searchFormRef.value.searchOptions[filterName] = selectedItem;
    }
};

const confirmProductModal = (selectedItems) => {
    console.log('Selected items from product modal:', selectedItems);
    if (selectedItems) {
        updateFilterValue('productModal', selectedItems.productName);
    }
    productModalVisible.value = false;
};

const confirmTypeModal = (selectedItems) => {
    console.log('Selected items from type modal:', selectedItems);
    if (selectedItems) {
        updateFilterValue('productType', selectedItems.categorySubName);
    }
    typeModalVisible.value = false;
};

const confirmPublisherModal = (selectedItems) => {
    console.log('Selected items from publisher modal:', selectedItems);
    if (selectedItems) {
        updateFilterValue('publisher', selectedItems.vendorName);
    }
    publisherModalVisible.value = false;
};

const searchProducts = async (searchValue) => {
    try {
        console.log('Searching products with value:', searchValue);
        const params = {
            searchValue: searchValue
        };
        
        // 공급업체이면 자신의 제품만 검색
        if (user.value && user.value.compId && user.value.compId !== 'COM10001') {
            params.vendorName = user.value.compName;
        }

        const response = await axios.get('/api/search/products', { params });
        productItems.value = await response.data; // 서버에서 받은 데이터를 items에 저장
    } catch (error) {
        console.error('Error searching products:', error);
    }
};

const searchProductTypes = async (searchValue) => {
    try {
        console.log('Searching product types with value:', searchValue);
        const response = await axios.get('/api/search/product-types', {
            params: {
                searchValue: searchValue
            }
        });
        typeItems.value = await response.data; // 서버에서 받은 데이터를 items에 저장
    } catch (error) {
        console.error('Error searching product types:', error);
    }
};

const searchPublishers = async (searchValue) => {
    try {
        console.log('Searching vendors with value:', searchValue);
        const params = {
            searchValue: searchValue
        };
        
        // 공급업체이면 자신의 공급사만 검색
        if (user.value && user.value.compId && user.value.compId !== 'COM10001') {
            params.vendorName = user.value.compName;
        }

        const response = await axios.get('/api/search/vendors', { params });
        publisherItems.value = await response.data; // 서버에서 받은 데이터를 items에 저장
    } catch (error) {
        console.error('Error searching vendors:', error);
    }
};

const searchStocks = async (searchOptions) => {
    try {
        console.log('Searching stocks with options:', searchOptions);
        
        // 검색 조건 설정
        const params = {
            productName: searchOptions.productModal || '',
            categorySub: searchOptions.productType || '',
            vendorName: searchOptions.publisher || ''
        };
        
        // 공급업체이면 자신의 공급사로 고정
        if (user.value && user.value.compId && user.value.compId !== 'COM10001') {
            params.vendorName = user.value.compName;
        }

        const response = await axios.get('/api/inventory/headStock/search', { params });
        allStockData.value = await response.data; // 검색 결과를 전체 데이터로 저장
        applyFilters(); // 필터 적용
        console.log('Stocks searched:', allStockData.value);

        // 검색 후 선택된 행과 LOT 정보 초기화
        clearSelection();
    } catch (error) {
        console.error('Error searching stocks:', error);
    }
};

const onRowSelect = (data) => {
    console.log('Row selected:', data.productId);
    loadLotStocks(data.productId);
    // 선택된 행에 대한 추가 로직 작성
};

const loadLotStocks = async (productId) => {
    try {
        console.log('Loading lot stocks for productId:', productId);
        const params = {};
        
        // 공급업체이면 자신의 공급사 제품만 조회
        if (user.value && user.value.compId && user.value.compId !== 'COM10001') {
            params.vendorName = user.value.compName;
        }

        const response = await axios.get(`/api/inventory/headStock/lot/${productId}`, { params });
        lotItems.value = await response.data; // 서버에서 받은 데이터를 items에 저장
        console.log('Lot stocks loaded:', lotItems.value);
    } catch (error) {
        console.error('Error loading lot stocks:', error);
    }
};

const resetList = () => {
    loadStockData();
    // 초기화 시 선택된 행과 LOT 정보 초기화
    clearSelection();
};

// 선택된 행과 LOT 정보를 초기화하는 함수
const clearSelection = () => {
    lotItems.value = []; // LOT 테이블 초기화

    // BasicTable의 선택된 행 초기화
    if (mainTableRef.value && typeof mainTableRef.value.clearSelection === 'function') {
        mainTableRef.value.clearSelection();
    }

    console.log('Selection cleared');
};

// 재고 수량에 따른 조건부 스타일링을 적용하는 함수
const getStockTag = (rowData, fieldName) => {
    if (fieldName === 'stockQuantity') {
        const stock = rowData.stockQuantity || 0;
        const safety = rowData.safetyStock || 0;

        if (stock <= 0) {
            return {
                value: `${stock}`,
                severity: 'danger'
            };
        } else if (stock <= safety * 1.2) {
            return {
                value: `${stock.toLocaleString()}`,
                severity: 'warn'
            };
        } else {
            return {
                value: `${stock.toLocaleString()}`,
                severity: 'success'
            };
        }
    }
    return null; // null을 반환하면 기본 렌더링 사용
};

onMounted(() => {
    // 공급업체인 경우 공급사 필터에 자기 회사명 미리 설정
    if (user.value && user.value.compId && user.value.compId !== 'COM10001') {
        const publisherFilter = filters.value.filters.find(f => f.name === 'publisher');
        if (publisherFilter) {
            publisherFilter.value = user.value.compName;
            publisherFilter.placeholder = user.value.compName || '공급사 정보 없음';
            console.log('🏢 공급업체 필터 초기값 설정:', publisherFilter.value);
        }
    }
    
    loadStockData();
});
</script>
<template>
    <!-- <SearchTable ref="searchFormRef" :filters="filters" :items="items" :header="header" @searchData="searchData" @open-search-modal="handleOpenModal" @resetSearchOptions="resetList"></SearchTable> -->

    <SearchForm ref="searchFormRef" :filters="filters" @searchData="searchData" @openSearchModal="handleOpenModal" @resetSearchOptions="resetList"></SearchForm>
    <BasicTable ref="mainTableRef" :data="mainItems" :header="mainHeader" :dataKey="'productId'" :checked="true" :scrollHeight="'200px'" @row-select="onRowSelect" :tagRenderer="getStockTag">
        <template #btn>
            <Button
                :label="safetyStockFilterEnabled ? '전체 재고 보기' : '안전재고 이하만 보기'"
                :icon="safetyStockFilterEnabled ? 'pi pi-eye' : 'pi pi-exclamation-triangle'"
                :class="safetyStockFilterEnabled ? 'p-button-secondary' : 'p-button-warning'"
                @click="toggleSafetyStockFilter"
            />
        </template>
    </BasicTable>
    <BasicTable :data="lotItems" :header="lotHeader" :scrollHeight="'150px'"></BasicTable>

    <DialogModal v-model:display="productModalVisible" :items="productItems" :headers="productHeaders" title="제품 검색" selectionMode="single" @close="closeProductModal" @confirm="confirmProductModal" @search-modal="searchProducts" />
    <DialogModal v-model:display="typeModalVisible" :items="typeItems" :headers="typeHeaders" title="제품 분류 검색" selectionMode="single" @close="closeTypeModal" @confirm="confirmTypeModal" @search-modal="searchProductTypes" />
    <DialogModal v-model:display="publisherModalVisible" :items="publisherItems" :headers="publisherHeaders" title="공급사 검색" selectionMode="single" @close="closePublisherModal" @confirm="confirmPublisherModal" @search-modal="searchPublishers" />
</template>
