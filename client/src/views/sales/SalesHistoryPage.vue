<script setup>
import { onMounted, ref } from 'vue';
import SearchTable from '../../components/common/SearchTable.vue';
import axios from '@/service/axios';
import DialogModal from '@/components/overray/DialogModal.vue';

// 조회 폼의 헤더 정보 (조회 테이블 컬럼 이름)
const header = ref({
  title: '판매 이력 검색', // 조회 폼 제목
  header: { // 테이블의 헤더 정보
    productId: '제품번호', 
    productName: '제품명', 
    categoryMain: '대분류', 
    categorySub: '소분류', 
    vendorName: '공급사', 
    stockQuantity: '재고수량(박스)', 
    safetyStock: '안전 재고(박스)', 
    productSpec: '규격', 
  },
  rightAligned: ['stockQuantity', 'safetyStock'] // 오른쪽 정렬할 컬럼 리스트
});

// 조회할 데이터
const items = ref([]);

// 검색 조건 필터 설정
const filters = ref({});
filters.value.title = '매출 검색'; // 검색 조건 폼 제목
filters.value.filters = [
  { type: 'dateRange', label: '매출기간', value: '', fromPlaceholder: '', name: 'salesDates' },
  { type: 'item-search', label: '제품명', value: '', placeholder: '제품 검색', name: 'products' },
  { type: 'item-search', label: '제품분류', value: '', placeholder: '제품분류 검색', name: 'productType' },
  { type: 'item-search', label: '지점명', value: '', placeholder: '지점명 검색', name: 'store' },
];

/*
 * 모달창 관련 설정
 * 모달창의 visible 상태를 관리하는 ref 변수
 * 모달창의 헤더 정보와 아이템 목록을 관리하는 ref 변수
 */

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
  { field: 'productSpec', header: '규격' },
]);

// 모달창의 데이터 아이템
// 제품 모달창 아이템
const productItems = ref([]);

// =====
// 여러개의 모달창이 필요할 경우 여러개를 각각 정의
const typeHeaders = ref([
  { field: 'categoryMainName', header: '대분류' },
  { field: 'categorySubName', header: '소분류' },
]);

const typeItems = ref([]);

const salesHeaders = ref([
  { field: 'vendorName', header: '업체명' },
  { field: 'phone', header: '전화번호' },
]);

const salesItems = ref([]);

const storeHeaders = ref([
  { field: 'compId', header: 'ID' },
  { field: 'compName', header: '회사명' },
  { field: 'ceoName', header: '대표자' },
  { field: 'phone', header: '전화번호' },
]);

const storeItems = ref([]);

// =====

// 검색 모달이 필요할 때 선언해서 사용.
// 모달의 visible 상태를 관리하는 ref 변수
const productModalVisible = ref(false);
const typeModalVisible = ref(false);
const salesModalVisible = ref(false);
const storeModalVisible = ref(false);

const loadProductItems = async () => {
  try {
    // 제품 목록을 서버에서 가져오기
    const response = await axios.get('/api/search/products/all');
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

const loadStoreItems = async () => {
  try {
    // 지점 목록을 서버에서 가져오기
    const response = await axios.get('/api/search/branches/all');
    storeItems.value = await response.data; // 서버에서 받은 데이터를 storeItems에 저장

    console.log('Store items loaded:', storeItems.value);

  } catch (error) {
    console.error('Error loading store items:', error);
  }
};

const loadSalesItems = async () => {
  try {
    // 매출 목록을 서버에서 가져오기
    const response = await axios.get('/api/search/sales/all');
    salesItems.value = await response.data; // 서버에서 받은 데이터를 salesItems에 저장

    console.log('Sales items loaded:', salesItems.value);

  } catch (error) {
    console.error('Error loading sales items:', error);
  }
};

// 검색 폼에서 검색 버튼 클릭 시 호출되는 함수
const searchData = async (searchOptions) => {
  console.log('Searching with options:', searchOptions);
  await searchSalesHistory(searchOptions);
  
};

// 검색 모달을 열 때 호출되는 함수
// case 문을 사용하여 모달 이름(item-search 타입의 name을 따름)에 따라 다른 모달을 열 수 있도록 구현
const handleOpenModal = (filterName) => {
  console.log('Open modal for filter:', filterName);
  switch (filterName) {
    case 'products':
      loadProductItems();
      productModalVisible.value = true;
      break;
    case 'productType':
      loadTypeItems();
      typeModalVisible.value = true;
      break;
    case 'salesSearchModal':
      loadSalesItems();
      salesModalVisible.value = true;
      break;
    case 'store':
      loadStoreItems();
      storeModalVisible.value = true;
      break;
    default:
      console.warn('No modal defined for filter:', filterName);
  }
};

/*************************
 * 모달창 닫기 함수 영역 *
 *************************/
const closeProductModal = () => {
  productModalVisible.value = false;
}
const closeTypeModal = () => {
  typeModalVisible.value = false;
}
const closeSalesModal = () => {
  salesModalVisible.value = false;
}
const closeStoreModal = () => {
  storeModalVisible.value = false;
}

/******************************
 * 모달창 확인 버튼 함수 영역 *
 ******************************/

// SearchForm의 ref를 추가
const searchFormRef = ref(null);

const updateFilterValue = (filterName, selectedItem) => {
  // SearchForm의 searchOptions에 직접 값 설정
  if (searchFormRef.value.searchFormRef.searchOptions) {
    searchFormRef.value.searchFormRef.searchOptions[filterName] = selectedItem;
  }
};

// 제품 선택 모달 선택 버튼
const confirmProductModal = (selectedItems) => {
  console.log('Selected items from product modal:', selectedItems);
  if (selectedItems) {
    updateFilterValue('products', selectedItems.productName);
  }
  productModalVisible.value = false;
};

// 제품 분류 선택 모달 선택 버튼
const confirmTypeModal = (selectedItems) => {
  console.log('Selected items from type modal:', selectedItems);
  if (selectedItems) {
    updateFilterValue('productType', selectedItems.categorySubName);
  }
  typeModalVisible.value = false;
};

// 지점 선택 모달 선택 버튼
const confirmStoreModal = (selectedItems) => {
  console.log('Selected items from store modal:', selectedItems);
  if (selectedItems) {
    updateFilterValue('store', selectedItems.compName);
  }
  storeModalVisible.value = false;
};

// 매출 검색 모달 선택 버튼
const confirmSalesModal = (selectedItems) => {
  console.log('Selected items from sales modal:', selectedItems);
  if (selectedItems) {
    updateFilterValue('salesSearchModal', selectedItems.salesId);
  }
  salesModalVisible.value = false;
};

/****************************
 * 모달 내부 검색 함수 영역 *
 ****************************/

// 제품 모달 내부 검색 함수
const searchProducts = async (searchValue) => {
  try {
    console.log('Searching products with value:', searchValue);
    const response = await axios.get('/api/search/products', {
      params: {
        searchValue: searchValue
      }
    });
    productItems.value = await response.data; // 서버에서 받은 데이터를 items에 저장
  } catch (error) {
    console.error('Error searching products:', error);
  }
};

// 제품 분류 모달 내부 검색 함수
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

// 매출 모달 내부 검색 함수
const searchSales = async (searchValue) => {
  try {
    console.log('Searching sales with value:', searchValue);
    const response = await axios.get('/api/search/sales', {
      params: {
        searchValue: searchValue
      }
    });
    salesItems.value = await response.data; // 서버에서 받은 데이터를 items에 저장
  } catch (error) {
    console.error('Error searching sales:', error);
  }
};

/***********************
 * 판매 이력 검색 함수 *
 ***********************/

// 조회 버튼 클릭했을 때 실제 실행되는 함수
const searchSalesHistory = async (searchOptions) => {
  try {
    console.log('Searching stocks with options:', searchOptions);
    const response = await axios.get('/api/inventory/headStock/search', {
      params: {
        productName: searchOptions.productModal || '',
        categorySub: searchOptions.productType || '',
        compName: searchOptions.compName || '',
        salesDatesFrom: searchOptions.salesDatesFrom || '',
        salesDatesTo: searchOptions.salesDatesTo || ''
      }
    });
    items.value = await response.data; // 서버에서 받은 데이터를 items에 저장
    console.log('Stocks searched:', items.value);
  } catch (error) {
    console.error('Error searching stocks:', error);
  }
};

const resetList = () => {

}

onMounted(() => {

});

</script>
<template>
  <SearchTable ref="searchFormRef" :filters="filters" :items="items" :header="header" @searchData="searchData" @open-search-modal="handleOpenModal" @resetSearchOptions="resetList"></SearchTable>
  <DialogModal v-model:display="productModalVisible" :items="productItems" :headers="productHeaders" title="제품 검색"
    selectionMode="single" @close="closeProductModal" @confirm="confirmProductModal" @search-modal="searchProducts" />
  <DialogModal v-model:display="typeModalVisible" :items="typeItems" :headers="typeHeaders" title="제품 분류 검색"
    selectionMode="single" @close="closeTypeModal" @confirm="confirmTypeModal" @search-modal="searchProductTypes" />
  <DialogModal v-model:display="storeModalVisible" :items="storeItems" :headers="storeHeaders" title="지점 검색"
    selectionMode="single" @close="closeStoreModal" @confirm="confirmStoreModal" />
</template>