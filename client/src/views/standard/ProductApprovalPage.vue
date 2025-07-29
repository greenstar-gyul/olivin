<script setup>
import StandardApproval from '@/components/common/StandardApproval.vue';
import { ref, onMounted, nextTick } from 'vue';
import axios from 'axios';

const API_BASE_URL = '/api/products';

const filters = ref({
  title: '승인 요청 조회',
  filters: [
    { type: 'text', label: '제품명', value: '', placeholder: '제품명을 입력하세요', name: 'productName' },
    { type: 'text', label: '브랜드', value: '', placeholder: '브랜드명을 입력하세요', name: 'vendorName' },
    { type: 'text', label: '카테고리', value: '', placeholder: '카테고리를 입력하세요', name: 'categoryMain' },
    { type: 'text', label: '세부카테고리', value: '', placeholder: '세부카테고리를 입력하세요', name: 'categorySub' },
    { type: 'numberRange', label: '입수량', value: '', placeholder: '입수량 범위를 입력하세요', name: 'packQtyRange' },
    { type: 'dateRange', label: '등록일 범위', value: '', placeholder: '등록일 범위를 선택하세요', name: 'regDateRange' }
  ]
});

const items = ref([]);

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

const inputs = ref({
  title: '제품 정보 및 승인 처리',
  inputs: [
    { type: 'text', label: '제품ID', value: '', name: 'productId', readonly: true },
    { type: 'text', label: '제품명', value: '', name: 'productName', readonly: true },
    { type: 'text', label: '브랜드', value: '', name: 'vendorName', readonly: true },
    { type: 'text', label: '카테고리', value: '', name: 'categoryMain', readonly: true },
    { type: 'text', label: '세부카테고리', value: '', name: 'categorySub', readonly: true },
    { type: 'text', label: '매장코드', value: '', name: 'compId', readonly: true },
    { type: 'text', label: '용량/규격', value: '', name: 'productSpec', readonly: true },
    { type: 'text', label: '단위', value: '', name: 'unit', readonly: true },
    { type: 'number', label: '입수량', value: '', name: 'packQty', readonly: true },
    { type: 'number', label: '구매가격', value: '', name: 'purchasePrice', readonly: true },
    { type: 'number', label: '판매가격', value: '', name: 'sellPrice', readonly: true },
    { type: 'text', label: '등록자', value: '', name: 'regUser', readonly: true },
    { type: 'date', label: '등록일자', value: '', name: 'regDate', readonly: true },
    { type: 'textarea', label: '비고/처리사유', value: '', name: 'note', readonly: false, placeholder: '승인 또는 반려 사유를 입력하세요' }
  ]
});

const selectedProduct = ref(null);

// 필요한 필드만 추출하는 함수
const filterProductData = (product) => {
  const {
    productId,
    productName,
    vendorName,
    categoryMain,
    categorySub,
    compId,
    productSpec,
    unit,
    packQty,
    purchasePrice,
    sellPrice,
    regUser,
    regDate,
    status,
    note
  } = product;
  
  return {
    productId,
    productName,
    vendorName,
    categoryMain,
    categorySub,
    compId,
    productSpec,
    unit,
    packQty,
    purchasePrice,
    sellPrice,
    regUser,
    regDate: regDate ? formatDate(regDate) : '',
    status,
    note
  };
};

const loadPendingProducts = async () => {
  try {
    console.log('🔍 승인 대기 제품 조회 시작');
    
    try {
      const pingResponse = await axios.get(`${API_BASE_URL}/ping`);
      console.log('✅ 서버 연결 성공:', pingResponse.data);
    } catch (pingError) {
      console.log('❌ 서버 연결 실패, 더미 데이터 사용');
      useDummyData();
      return;
    }
    
    const response = await axios.get(`${API_BASE_URL}/search`, {
      params: { status: 'ST002' }
    });
    console.log('📊 서버 응답:', response.data);
    
    if (response.data && Array.isArray(response.data)) {
      // ✅ 필요한 필드만 추출해서 전달
      items.value = response.data.map(product => filterProductData(product));
      console.log('📋 필터링된 items:', items.value);
    } else {
      console.log('📊 응답 데이터가 없거나 배열이 아님, 더미 데이터 사용');
      useDummyData();
    }
    
  } catch (error) {
    console.error('❌ 승인 대기 제품 조회 실패:', error);
    useDummyData();
  }
};

const useDummyData = () => {
  console.log('🎭 더미 데이터 설정');
  items.value = [
    { 
      productId: 'PROD009', 
      productName: '아이패드 Air', 
      categoryMain: '전자제품', 
      categorySub: '태블릿',
      vendorName: '애플', 
      compId: 'CP009',
      productSpec: '256GB',
      unit: '개',
      packQty: 15,
      purchasePrice: 750000,
      sellPrice: 950000, 
      regUser: 'USER01', 
      regDate: '2025-01-28',
      status: 'ST002',
      note: '신제품 출시 - 승인 대기'
    },
    { 
      productId: 'PROD010', 
      productName: '다이슨 에어랩', 
      categoryMain: '가전제품', 
      categorySub: '헤어케어',
      vendorName: '다이슨', 
      compId: 'CP010',
      productSpec: 'Complete',
      unit: '세트',
      packQty: 5,
      purchasePrice: 550000,
      sellPrice: 699000, 
      regUser: 'USER02', 
      regDate: '2025-01-28',
      status: 'ST002',
      note: '인기 상품 재입고 - 승인 대기'
    }
  ];
  console.log('🎭 더미 데이터 설정 완료:', items.value);
};

const searchData = async (searchOptions) => {
  try {
    console.log('🔍 검색 실행:', searchOptions);
    
    const params = { status: 'ST002' };
    
    if (searchOptions.productName && searchOptions.productName.trim() !== '') {
      params.productName = searchOptions.productName.trim();
    }
    
    if (searchOptions.vendorName && searchOptions.vendorName.trim() !== '') {
      params.vendorName = searchOptions.vendorName.trim();
    }
    
    if (searchOptions.categoryMain && searchOptions.categoryMain.trim() !== '') {
      params.categoryMain = searchOptions.categoryMain.trim();
    }
    
    if (searchOptions.categorySub && searchOptions.categorySub.trim() !== '') {
      params.categorySub = searchOptions.categorySub.trim();
    }
    
    if (searchOptions.packQtyRange) {
      if (Array.isArray(searchOptions.packQtyRange) && searchOptions.packQtyRange.length === 2) {
        if (searchOptions.packQtyRange[0] !== null && searchOptions.packQtyRange[0] !== '') {
          params.packQtyFrom = searchOptions.packQtyRange[0];
        }
        if (searchOptions.packQtyRange[1] !== null && searchOptions.packQtyRange[1] !== '') {
          params.packQtyTo = searchOptions.packQtyRange[1];
        }
      } else if (typeof searchOptions.packQtyRange === 'object') {
        if (searchOptions.packQtyRange.from !== null && searchOptions.packQtyRange.from !== '') {
          params.packQtyFrom = searchOptions.packQtyRange.from;
        }
        if (searchOptions.packQtyRange.to !== null && searchOptions.packQtyRange.to !== '') {
          params.packQtyTo = searchOptions.packQtyRange.to;
        }
      }
    }
    
    if (searchOptions.regDateRange && searchOptions.regDateRange.length === 2) {
      params.regDateFrom = searchOptions.regDateRange[0];
      params.regDateTo = searchOptions.regDateRange[1];
    }
    
    const response = await axios.get(`${API_BASE_URL}/search`, { params });
    
    // ✅ 검색 결과도 필터링해서 전달
    items.value = response.data
      .filter(product => product.status === 'ST002')
      .map(product => filterProductData(product));
    
    console.log('✅ 검색 완료, 결과 개수:', items.value.length);
    
  } catch (error) {
    console.error('❌ 검색 실패:', error);
    alert('검색에 실패했습니다.');
  }
};

// ✅ 핵심 문제 해결: 단순하고 확실한 방법으로 폼 업데이트
const onRowSelect = (row) => {
  console.log('🎯 메인 컴포넌트 - 행 선택됨:', row);
  
  if (!row) {
    console.log('❌ row가 null 또는 undefined');
    resetForm();
    return;
  }
  
  selectedProduct.value = row;
  
  // ✅ 가장 단순한 방법: 각 input의 value를 직접 업데이트
  inputs.value.inputs.forEach((input, index) => {
    if (input.name in row) {
      const newValue = row[input.name] || '';
      console.log(`📝 ${input.name}: "${newValue}"`);
      
      // 직접 할당으로 Vue 반응성 확보
      inputs.value.inputs[index] = {
        ...input,
        value: String(newValue)
      };
    }
  });
  
  // 강제 업데이트
  inputs.value = { ...inputs.value };
  
  console.log('✅ 폼 업데이트 완료');
};

const handleApprove = async (selectedData) => {
  console.log('✅ 승인 처리 시작:', selectedData);
  
  if (!selectedProduct.value) {
    alert('승인할 제품을 선택해주세요.');
    return;
  }
  
  const reason = selectedData?.note || '승인 완료';
  
  if (!confirm(`제품 "${selectedProduct.value.productName}"을(를) 승인하시겠습니까?`)) {
    return;
  }
  
  try {
    const response = await axios.post(
      `${API_BASE_URL}/${selectedProduct.value.productId}/approve`, 
      null,
      {
        params: { 
          approver: 'ADMIN',
          reason: reason
        }
      }
    );
    
    if (response.data.success) {
      alert('제품이 승인되었습니다.');
      resetForm();
      await loadPendingProducts();
    } else {
      alert(response.data.message || '승인에 실패했습니다.');
    }
    
  } catch (error) {
    console.error('❌ 승인 처리 실패:', error);
    
    if (error.code === 'ERR_NETWORK' || error.code === 'ERR_BAD_RESPONSE') {
      alert('제품이 승인되었습니다. (더미 처리)');
      resetForm();
      await loadPendingProducts();
    } else {
      alert('승인 처리에 실패했습니다: ' + (error.response?.data?.message || error.message));
    }
  }
};

const handleReject = async (selectedData) => {
  console.log('❌ 반려 처리 시작:', selectedData);
  
  if (!selectedProduct.value) {
    alert('반려할 제품을 선택해주세요.');
    return;
  }
  
  const reason = selectedData?.note;
  if (!reason || reason.trim() === '') {
    alert('반려 사유를 입력해주세요.');
    return;
  }
  
  if (!confirm(`제품 "${selectedProduct.value.productName}"을(를) 반려하시겠습니까?`)) {
    return;
  }
  
  try {
    const response = await axios.post(
      `${API_BASE_URL}/${selectedProduct.value.productId}/reject`, 
      null,
      {
        params: { 
          approver: 'ADMIN',
          reason: reason
        }
      }
    );
    
    if (response.data.success) {
      alert('제품이 반려되었습니다.');
      resetForm();
      await loadPendingProducts();
    } else {
      alert(response.data.message || '반려에 실패했습니다.');
    }
    
  } catch (error) {
    console.error('❌ 반려 처리 실패:', error);
    
    if (error.code === 'ERR_NETWORK' || error.code === 'ERR_BAD_RESPONSE') {
      alert('제품이 반려되었습니다. (더미 처리)');
      resetForm();
      await loadPendingProducts();
    } else {
      alert('반려 처리에 실패했습니다: ' + (error.response?.data?.message || error.message));
    }
  }
};

const resetForm = () => {
  console.log('🔄 폼 초기화');
  selectedProduct.value = null;
  inputs.value.inputs.forEach((input, index) => {
    inputs.value.inputs[index] = { ...input, value: '' };
  });
  inputs.value = { ...inputs.value };
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  try {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  } catch (error) {
    console.error('❌ 날짜 포맷 오류:', error);
    return dateString;
  }
};

const saveData = (inputData) => {
  console.log('💾 선택 제품 정보 저장:', inputData);
};


onMounted(() => {
  console.log('🚀 컴포넌트 마운트됨');
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
      @itemClick="onRowSelect"
      @rowClick="onRowSelect"
      @select="onRowSelect"
    />
</template>