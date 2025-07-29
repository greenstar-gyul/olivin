<!-- 조회 테스트 페이지 -->
<script setup>
import { onMounted, ref } from 'vue';
import SearchTable from '../../components/common/SearchTable.vue';
import { StockService } from '@/service/StockService';
import { Dialog } from 'primevue';
import axios from 'axios';
import DialogModal from '@/components/overray/DialogModal.vue';

// 조회 폼의 헤더 정보 (조회 테이블 컬럼 이름)
const header = ref({
  title: '재고 현황', // 조회 폼 제목
  header: { // 테이블의 헤더 정보
    id: 'ID', 
    productName: '제품명', 
    category: '제품분류', 
    publisher: '공급사', 
    store: '지점', 
    size: '규격', 
    quantity: '현재 재고', 
    safe: '안전 재고', 
  },
  rightAligned: ['quantity', 'safe'] // 오른쪽 정렬할 컬럼 리스트
});

// 조회할 데이터
const items = ref([
  { id: 1, productName: '제품 A', category: '분류 A', publisher: '공급사 A', store: '지점 A', size: '규격 A', quantity: 100, safe: 50 },
  { id: 2, productName: '제품 B', category: '분류 B', publisher: '공급사 B', store: '지점 B', size: '규격 B', quantity: 200, safe: 100 },
  { id: 3, productName: '제품 C', category: '분류 C', publisher: '공급사 C', store: '지점 C', size: '규격 C', quantity: 300, safe: 150 }
]);

// 검색 조건 필터 설정
const filters = ref({});
filters.value.title = '재고 검색'; // 검색 조건 폼 제목
filters.value.filters = [ // 검색 조건 필터 목록
  // type: 'text'는 일반 텍스트 입력 필드
  // type: 'dateRange'는 날짜 범위 선택 필드
  // type: 'select'는 드롭다운 선택 필드
  // type: 'item-search'는 아이템 검색 모달을 여는 필드
  // type: 'number'는 숫자 입력 필드
  // type: 'textarea'는 다중 행 텍스트 입력 필드
  // type: 'date'는 단일 날짜 선택 필드
  // label: 필드의 라벨. 사용자에게 보여지는 이름.
  // value: 필드의 초기 값. 특별한 경우가 아니면 일반적으로 빈 문자열.
  // placeholder: 필드에 대한 플레이스홀더 텍스트. 사용자가 입력하기 전에 보여지는 안내 텍스트.
  // name: 필드의 고유 이름. 데이터 바인딩에 사용됨.
  { type: 'item-search', label: '제품명', value: '', placeholder: '제품번호 / 제품명 검색', name: 'productModal' },
  { type: 'item-search', label: '제품분류', value: '', placeholder: '제품분류 선택', name: 'productType' },
  { type: 'item-search', label: '공급사', value: '', placeholder: '공급사 검색', name: 'publisher' },
  { type: 'item-search', label: '지점', value: '', placeholder: '지점명 검색', name: 'store' },
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
  { field: 'productSpec', header: '규격' },
]);

// 모달창의 데이터 아이템
// 제품 모달창 아이템
const productItems = ref([
  { id: 1, name: '제품 A', category: '분류 A', publisher: '공급사 A', size: '규격 A' },
  { id: 2, name: '제품 B', category: '분류 B', publisher: '공급사 B', size: '규격 B' },
  { id: 3, name: '제품 C', category: '분류 C', publisher: '공급사 C', size: '규격 C' }
]);

// =====
// 여러개의 모달창이 필요할 경우 여러개를 각각 정의
const typeHeaders = ref([
  { field: 'categoryMain', header: '대분류' },
  { field: 'categorySub', header: '하위분류' },
]);

const typeItems = ref([
  { categoryMain: '전자제품', categorySub: '스마트폰' },
  { categoryMain: '전자제품', categorySub: '노트북' },
  { categoryMain: '가전제품', categorySub: '냉장고' }
]);

const publisherHeaders = ref([
  { field: 'compId', header: 'ID' },
  { field: 'compName', header: '회사명' },
  { field: 'ceoName', header: '대표자' },
  { field: 'phone', header: '전화번호' },
]);

const publisherItems = ref([
  { compId: 1, compName: '회사 A', ceoName: '대표 A', phone: '010-1234-5678' },
  { compId: 2, compName: '회사 B', ceoName: '대표 B', phone: '010-2345-6789' },
  { compId: 3, compName: '회사 C', ceoName: '대표 C', phone: '010-3456-7890' }
]);

const storeHeaders = ref([
  { field: 'compId', header: 'ID' },
  { field: 'compName', header: '회사명' },
  { field: 'ceoName', header: '대표자' },
  { field: 'phone', header: '전화번호' },
]);

const storeItems = ref([
  { compId: 1, compName: '지점 A', ceoName: '대표 A', phone: '010-1234-5678' },
  { compId: 2, compName: '지점 B', ceoName: '대표 B', phone: '010-2345-6789' },
  { compId: 3, compName: '지점 C', ceoName: '대표 C', phone: '010-3456-7890' }
]);

// =====

// 검색 모달이 필요할 때 선언해서 사용.
// 모달의 visible 상태를 관리하는 ref 변수
const productModalVisible = ref(false);
const typeModalVisible = ref(false);
const publisherModalVisible = ref(false);
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

  } catch (error) {
    console.error('Error loading product type items:', error);
  }
};

const loadPublisherItems = async () => {
  try {
    // 공급사 목록을 서버에서 가져오기

  } catch (error) {
    console.error('Error loading publisher items:', error);
  }
};

const loadStoreItems = async () => {
  try {
    // 지점 목록을 서버에서 가져오기

  } catch (error) {
    console.error('Error loading store items:', error);
  }
};

// 검색 폼에서 검색 버튼 클릭 시 호출되는 함수
const searchData = (searchOptions) => {
  console.log('Searching with options:', searchOptions);
};

// 검색 모달을 열 때 호출되는 함수
// case 문을 사용하여 모달 이름(item-search 타입의 name을 따름)에 따라 다른 모달을 열 수 있도록 구현
const handleOpenModal = (filterName) => {
  console.log('Open modal for filter:', filterName);
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
    case 'store':
      loadStoreItems();
      storeModalVisible.value = true;
      break;
    default:
      console.warn('No modal defined for filter:', filterName);
  }
};

// 필요한 함수를 자유롭게 선언하는 공간 ======
const getSampleData = async () => {
  const result = await axios.get('/api/test');
  const data = await result.data;
  console.log('Sample data:', data);
}

// ======

// 모달창 닫기 함수. 필요한 만큼 생성
const closeProductModal = () => {
  productModalVisible.value = false;
}
const closeTypeModal = () => {
  typeModalVisible.value = false;
}
const closePublisherModal = () => {
  publisherModalVisible.value = false;
}
const closeStoreModal = () => {
  storeModalVisible.value = false;
}

// 모달창 확인 버튼 클릭 시 호출되는 함수
// 필요한 로직 작성

// SearchForm의 ref를 추가
const searchFormRef = ref(null);

const updateFilterValue = (filterName, selectedItem) => {
  // SearchForm의 searchOptions를 직접 업데이트
  if (searchFormRef.value && searchFormRef.value.searchFormRef && selectedItem) {
    const displayValue = selectedItem.productName || selectedItem.compName || selectedItem.categoryMain || '';
    // SearchForm의 searchOptions에 직접 값 설정
    if (searchFormRef.value.searchFormRef.searchOptions) {
      searchFormRef.value.searchFormRef.searchOptions[filterName] = displayValue;
    }
  }
};

const confirmProductModal = (selectedItems) => {
  console.log('Selected items from product modal:', selectedItems);
  if (selectedItems) {
    updateFilterValue('productModal', selectedItems);
  }
  productModalVisible.value = false;
};

const confirmTypeModal = (selectedItems) => {
  console.log('Selected items from type modal:', selectedItems);
  if (selectedItems) {
    updateFilterValue('productType', selectedItems);
  }
  typeModalVisible.value = false;
};

const confirmPublisherModal = (selectedItems) => {
  console.log('Selected items from publisher modal:', selectedItems);
  if (selectedItems) {
    updateFilterValue('publisher', selectedItems);
  }
  publisherModalVisible.value = false;
};

const confirmStoreModal = (selectedItems) => {
  console.log('Selected items from store modal:', selectedItems);
  if (selectedItems) {
    updateFilterValue('store', selectedItems);
  }
  storeModalVisible.value = false;
};

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

onMounted(() => {
  getSampleData();
});

</script>
<template>
  <SearchTable ref="searchFormRef" :filters="filters" :items="items" :header="header" @searchData="searchData" @open-search-modal="handleOpenModal"></SearchTable>
  <DialogModal v-model:display="productModalVisible" :items="productItems" :headers="productHeaders" title="제품 검색"
    selectionMode="single" @close="closeProductModal" @confirm="confirmProductModal" @search-modal="searchProducts" />
  <DialogModal v-model:display="typeModalVisible" :items="typeItems" :headers="typeHeaders" title="제품 분류 검색"
    selectionMode="single" @close="closeTypeModal" @confirm="confirmTypeModal" />
  <DialogModal v-model:display="publisherModalVisible" :items="publisherItems" :headers="publisherHeaders" title="공급사 검색"
    selectionMode="single" @close="closePublisherModal" @confirm="confirmPublisherModal" />
  <DialogModal v-model:display="storeModalVisible" :items="storeItems" :headers="storeHeaders" title="지점 검색"
    selectionMode="single" @close="closeStoreModal" @confirm="confirmStoreModal" />
</template>