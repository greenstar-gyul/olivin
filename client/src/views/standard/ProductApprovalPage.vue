<script setup>
import StandardApproval from '@/components/common/StandardApproval.vue';
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

const API_BASE_URL = '/api/products';

const filters = ref([
  { type: 'text', label: '제품명', value: '', placeholder: '제품명을 입력하세요', name: 'productName' },
  { type: 'text', label: '브랜드', value: '', placeholder: '브랜드명을 입력하세요', name: 'vendorName' },
  { type: 'text', label: '카테고리', value: '', placeholder: '카테고리를 입력하세요', name: 'categoryMain' },
  { type: 'text', label: '세부카테고리', value: '', placeholder: '세부카테고리를 입력하세요', name: 'categorySub' },
  { type: 'numberRange', label: '입수량', value: '', placeholder: '입수량 범위를 입력하세요', name: 'packQtyRange' },
  { type: 'dateRange', label: '등록일 범위', value: '', placeholder: '등록일 범위를 선택하세요', name: 'regDateRange' }
]);

const items = ref([]);
const selectedProduct = ref(null);
const selectedProductId = ref(null);

const header = ref({
  title: '제품 승인 요청 목록',
  header: {
    productId: '제품ID',
    productName: '제품명',
    vendorName: '브랜드',
    categoryMain: '카테고리',
    categorySub: '세부카테고리',
    compId: '매장코드',
    productSpec: '용량/규격',
    unit: '단위',
    packQty: '입수량',
    purchasePrice: '구매단가',
    sellPrice: '판매단가',
    regUser: '등록자',
    regDate: '등록일자',
    status: '상태'
  },
  rightAligned: ['packQty', 'sellPrice']
});

// 폼 데이터 관리
const formData = ref({
  productId: '',
  productName: '',
  vendorName: '',
  categoryMain: '',
  categorySub: '',
  compId: '',
  productSpec: '',
  unit: '',
  packQty: '',
  purchasePrice: '',
  sellPrice: '',
  regUser: '',
  regDate: '',
  note: ''
});

// inputs를 computed로 만들어서 formData와 동기화
const inputs = computed(() => ({
  title: '제품 정보 및 승인 처리',
  inputs: [
    { type: 'text', label: '제품ID', value: formData.value.productId, name: 'productId', readonly: true },
    { type: 'text', label: '제품명', value: formData.value.productName, name: 'productName', readonly: true },
    { type: 'text', label: '브랜드', value: formData.value.vendorName, name: 'vendorName', readonly: true },
    { type: 'text', label: '카테고리', value: formData.value.categoryMain, name: 'categoryMain', readonly: true },
    { type: 'text', label: '세부카테고리', value: formData.value.categorySub, name: 'categorySub', readonly: true },
    { type: 'text', label: '매장코드', value: formData.value.compId, name: 'compId', readonly: true },
    { type: 'text', label: '용량/규격', value: formData.value.productSpec, name: 'productSpec', readonly: true },
    { type: 'text', label: '단위', value: formData.value.unit, name: 'unit', readonly: true },
    { type: 'number', label: '입수량', value: formData.value.packQty, name: 'packQty', readonly: true },
    { type: 'number', label: '구매가격', value: formData.value.purchasePrice, name: 'purchasePrice', readonly: true },
    { type: 'number', label: '판매가격', value: formData.value.sellPrice, name: 'sellPrice', readonly: true },
    { type: 'text', label: '등록자', value: formData.value.regUser, name: 'regUser', readonly: true },
    { type: 'date', label: '등록일자', value: formData.value.regDate, name: 'regDate', readonly: true },
    { type: 'textarea', label: '비고/처리사유', value: formData.value.note, name: 'note', readonly: false, placeholder: '승인 또는 반려 사유를 입력하세요' }
  ]
}));

// 필요한 필드만 추출하는 함수
const filterProductData = (product) => {
  const {
    productId, productName, vendorName, categoryMain, categorySub,
    compId, productSpec, unit, packQty, purchasePrice, sellPrice,
    regUser, regDate, status, note
  } = product;
  
  return {
    productId, productName, vendorName, categoryMain, categorySub,
    compId, productSpec, unit, packQty, purchasePrice, sellPrice,
    regUser, regDate: regDate ? formatDate(regDate) : '', status, note
  };
};

// 승인 대기 제품 조회
const loadPendingProducts = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/search`, {
      params: { status: '040002' }
    });
    
    if (response.data && Array.isArray(response.data)) {
      items.value = response.data.map(product => filterProductData(product));
    }
  } catch (error) {
    console.error('승인 대기 제품 조회 실패:', error);
    alert('제품 목록을 불러오는데 실패했습니다.');
  }
};

// 검색 기능
const searchData = async (searchOptions) => {
  try {
    const params = { status: '040002' };
    
    // 검색 조건 추가
    if (searchOptions.productName?.trim()) params.productName = searchOptions.productName.trim();
    if (searchOptions.vendorName?.trim()) params.vendorName = searchOptions.vendorName.trim();
    if (searchOptions.categoryMain?.trim()) params.categoryMain = searchOptions.categoryMain.trim();
    if (searchOptions.categorySub?.trim()) params.categorySub = searchOptions.categorySub.trim();
    
    // 날짜 범위 처리
    if (searchOptions.regDateRange && searchOptions.regDateRange.length === 2) {
      params.regDateFrom = searchOptions.regDateRange[0];
      params.regDateTo = searchOptions.regDateRange[1];
    }
    
    const response = await axios.get(`${API_BASE_URL}/search`, { params });
    
    items.value = response.data
      .filter(product => product.status === '040002')
      .map(product => filterProductData(product));
    
  } catch (error) {
    console.error('검색 실패:', error);
    alert('검색에 실패했습니다.');
  }
  
  resetForm();
};

// 제품 선택 처리
const onRowSelect = (row) => {
  if (!row) {
    resetForm();
    return;
  }
  
  // 중복 선택 방지
  if (selectedProductId.value === row.productId) {
    return;
  }
  
  selectedProductId.value = row.productId;
  selectedProduct.value = { ...row };
  
  // formData 업데이트
  Object.keys(formData.value).forEach(key => {
    if (key in row) {
      formData.value[key] = String(row[key] || '');
    }
  });
  
  formData.value.note = '';
};

// 승인 처리
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
    const response = await axios.post(
      `${API_BASE_URL}/${targetProduct.productId}/approve`, 
      {
        approver: 'ADMIN',
        reason: reason,
        status: '040001'
      },
      {
        headers: { 'Content-Type': 'application/json' }
      }
    );
    
    if (response.status === 200) {
      alert(`제품 "${targetProduct.productName}"이 승인되었습니다.`);
      
      // 로컬에서 제거 후 새로고침
      items.value = items.value.filter(item => item.productId !== targetProduct.productId);
      resetForm();
      await loadPendingProducts();
    } else {
      alert('승인에 실패했습니다.');
    }
    
  } catch (error) {
    console.error('승인 처리 실패:', error);
    const errorMessage = error.response?.data?.message || error.message || '알 수 없는 오류';
    alert(`승인 처리 실패: ${errorMessage}`);
  }
};

// 반려 처리
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
    const response = await axios.post(
      `${API_BASE_URL}/${targetProduct.productId}/reject`, 
      {
        approver: 'ADMIN',
        reason: reason,
        status: '040003'
      },
      {
        headers: { 'Content-Type': 'application/json' }
      }
    );
    
    if (response.status === 200) {
      alert(`제품 "${targetProduct.productName}"이 반려되었습니다.`);
      
      // 로컬에서 제거 후 새로고침
      items.value = items.value.filter(item => item.productId !== targetProduct.productId);
      resetForm();
      await loadPendingProducts();
    } else {
      alert('반려에 실패했습니다.');
    }
    
  } catch (error) {
    console.error('반려 처리 실패:', error);
    const errorMessage = error.response?.data?.message || error.message || '알 수 없는 오류';
    alert(`반려 처리 실패: ${errorMessage}`);
  }
};

// 폼 초기화
const resetForm = () => {
  selectedProduct.value = null;
  selectedProductId.value = null;
  
  Object.keys(formData.value).forEach(key => {
    formData.value[key] = '';
  });
};

// InputForm에서 받은 데이터로 formData 업데이트
const saveData = (inputData) => {
  Object.keys(inputData).forEach(key => {
    if (formData.value.hasOwnProperty(key)) {
      formData.value[key] = inputData[key];
    }
  });
};

// 날짜 포맷 함수
const formatDate = (dateString) => {
  if (!dateString) return '';
  try {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  } catch (error) {
    console.error('날짜 포맷 오류:', error);
    return dateString;
  }
};

onMounted(() => {
  loadPendingProducts();
});
</script>

<template>
  <StandardApproval
    :filters="filters"
    :items="items"
    :header="header"
    :inputs="inputs"
    @searchData="searchData"
    @saveData="saveData"
    @rowSelect="onRowSelect"
    @approve="handleApprove"
    @reject="handleReject"
  />
</template>