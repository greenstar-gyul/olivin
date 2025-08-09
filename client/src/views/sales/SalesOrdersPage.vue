<script setup>
import { onMounted, ref } from 'vue';
import BasicTable from '@/components/table/BasicTable.vue';
import SearchForm from '@/components/inputForm/SearchForm.vue';
import DialogModal from '@/components/overray/DialogModal.vue';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Calendar from 'primevue/calendar';
import { useToast } from 'primevue/usetoast';
import { useAuth } from '@/composables/useAuth';
import axios from '@/service/axios';

const searchFormRef = ref(null);
const { user } = useAuth();
const toast = useToast();

// 일일 정산 관련 변수들
const dailySettlementVisible = ref(false);
const todaySalesData = ref({
  totalAmount: 0,
  orderCount: 0,
  settlementDate: null,  // Date 객체로 변경
  cashAmount: 0,         // 현금
  transferAmount: 0,     // 계좌이체
  cardAmount: 0,         // 카드결제
});

// 마스터 테이블
const masterHeader = ref({
  title: '주문 조회',
  header: {
    soId: '주문코드', 
    compName: '지점명', 
    totalPrice: '총 가격', 
    paymentTypeName: '결제 방식', 
    soDate: '주문일자',
    empName: '등록자',
    statusName: '상태',
  },
  rightAligned: ['totalPrice']
});

const masterItems = ref([]);

// 상세 테이블
const detailHeader = ref({
  title: '상품 목록',
  header: {
    productId: '상품코드',
    productName: '상품명', 
    unitPrice: '판매가', 
    quantity: '수량', 
    price: '합계',
  },
  rightAligned: ['unitPrice', 'quantity', 'price']
});

const detailItems = ref([]);

const onRowSelect = async (row) => {
  const res = await axios.get(`/api/sales/orders/${row.soId}`);
  if (res.data?.detail?.length > 0) {
    detailItems.value = res.data.detail.map((e) => {
      return {
        ...e,
        price: Number(e.price).toLocaleString() + '원',
        unitPrice: Number(e.unitPrice).toLocaleString() + '원',
      }
    });
  } else {
    detailItems.value = [];
  }
  // console.log('Selected Row:', row);
};

const onRowUnselect = (row) => {
  // TODO : 초기화 로직 추가
  console.log('Unselected Row:', row);
};

// 검색 조건
const filters = ref({
  title: '조회 조건',
  filters: [
    { type: 'item-search', label: '주문코드', value: '', placeholder: '주문코드를 입력하세요.', name: 'soId' },
    { type: 'select', label: '결제방식', value: '', placeholder: '결제방식을 선택하세요.', name: 'paymentType',
        options: [
        { name: '현금', value: '160001' },
        { name: '계좌이체', value: '160002' },
        { name: '카드결제', value: '160003' },
      ]
    },
    { type: 'dateRange', label: '주문일자', value: '', name: 'soDate',
      fromPlaceholder: '예: 2025-07-01', toPlaceholder: '예: 2025-07-31' },
    { type: 'item-search', label: '지점명', value: '', placeholder: '지점명을 입력하세요.', name: 'compName',
      disabled: user.value?.compId !== 'COM10001', // 본사가 아니면 비활성화
      defaultValue: '' // 동적으로 설정될 예정
    },
    { type: 'select', label: '상태', value: '', placeholder: '주문상태를 선택하세요.', name: 'status',
      options: [
        { name: '판매됨', value: '020001' },
        { name: '취소됨', value: '020002' },
      ]
    },
    { type: 'text', label: '등록자', value: '', placeholder: '등록자를 입력하세요.', name: 'empName' },
  ]
});

// 모달 관련 변수들
const orderCodeModalVisible = ref(false);
const storeModalVisible = ref(false);

// 모달창의 테이블 헤더 정보
const orderHeaders = ref([
  { field: 'soId', header: '주문코드' },
  { field: 'compName', header: '지점명' },
  { field: 'soDate', header: '주문일자' },
  { field: 'totalPrice', header: '총액' },
  { field: 'statusName', header: '상태' },
]);

const storeHeaders = ref([
  { field: 'compId', header: 'ID' },
  { field: 'compName', header: '회사명' },
  { field: 'ceoName', header: '대표자' },
  { field: 'phone', header: '전화번호' },
]);

// 모달창의 데이터 아이템
const orderItems = ref([]);
const storeItems = ref([]);

const filterOptions = ref({}); // 검색 조건 기본값

const searchData = async (searchValue) => {
  // 날짜를 문자열로 변환 (로컬 시간 기준)
  const params = { ...searchValue };
  if (params.soDateFrom && typeof params.soDateFrom !== 'string') {
    const date = new Date(params.soDateFrom);
    params.soDateFrom = date.getFullYear() + '-' + 
                        String(date.getMonth() + 1).padStart(2, '0') + '-' + 
                        String(date.getDate()).padStart(2, '0');
  }
  if (params.soDateTo && typeof params.soDateTo !== 'string') {
    const date = new Date(params.soDateTo);
    params.soDateTo = date.getFullYear() + '-' + 
                      String(date.getMonth() + 1).padStart(2, '0') + '-' + 
                      String(date.getDate()).padStart(2, '0');
  }
  
  getSalesOrders(params);
};

const resetSearchData = async () => {
  // 사용자 권한에 따른 지점명 재설정
  let defaultCompName = '';
  
  if (user.value && user.value.compId && user.value.compId !== 'COM10001') {
    // 지점 직원: 자신의 지점명
    defaultCompName = user.value.compName;
  } else if (user.value && user.value.compId === 'COM10001') {
    // 본사 직원: 첫 번째 지점명
    try {
      const response = await axios.get('/api/search/branches/all');
      const firstData = await response.data[0];
      defaultCompName = firstData?.compName || '';
    } catch (error) {
      console.error('Error loading branches for reset:', error);
    }
  }
  
  // 기본값에 지점명 설정
  const resetOptions = { ...filterOptions.value };
  if (defaultCompName) {
    resetOptions.compName = defaultCompName;
  }
  
  searchFormRef.value.searchOptions = resetOptions; //기본값으로 초기화
  
  // 검색폼의 지점명 필드도 업데이트
  if (searchFormRef.value.searchFormRef?.searchOptions && defaultCompName) {
    searchFormRef.value.searchFormRef.searchOptions.compName = defaultCompName;
  }
};

const getSalesOrders = async (searchValue) => {
  // 날짜를 문자열로 변환 (로컬 시간 기준)
  const params = { ...searchValue };
  if (params.soDateFrom && typeof params.soDateFrom !== 'string') {
    const date = new Date(params.soDateFrom);
    params.soDateFrom = date.getFullYear() + '-' + 
                        String(date.getMonth() + 1).padStart(2, '0') + '-' + 
                        String(date.getDate()).padStart(2, '0');
  }
  if (params.soDateTo && typeof params.soDateTo !== 'string') {
    const date = new Date(params.soDateTo);
    params.soDateTo = date.getFullYear() + '-' + 
                      String(date.getMonth() + 1).padStart(2, '0') + '-' + 
                      String(date.getDate()).padStart(2, '0');
  }
  
  const res = await axios.get('/api/sales/orders', {
    params: params
  });
  if (res.data?.length > 0) {
    masterItems.value = res.data.map((e) => {
      return {
        ...e,
        totalPrice: Number(e.totalPrice).toLocaleString() + '원',
      }
    });
  } else {
    masterItems.value = [];
  }
};

const getBranchInfo = async (empId) => {
  const req = await axios.get('/api/orders/user/compInfo', {
    params: {
      empId: empId
    }
  });

  //지점 정보
  if (req.data.compType == '100002')
    return req.data;
  return undefined;
}

const defaultFilterOptions = async (userInfo) => {
  const options = {};
  filters.value.filters.forEach(filter => {
    if (filter.type === 'dateRange') {
      options[`${filter.name}From`] = '';
      options[`${filter.name}To`] = '';
    } else {
      options[filter.name] = '';
    }
  }); //filtrer의 name을 key로 사용

  // 기본 날짜 설정 - 최근 일주일
  const today = new Date();
  const lastWeek = new Date();
  lastWeek.setDate(today.getDate() - 7);
  
  options.soDateFrom = lastWeek.toISOString().split('T')[0]; // YYYY-MM-DD 형식
  options.soDateTo = today.toISOString().split('T')[0];

  // 지점 정보 기져오기
  const branchInfo = await getBranchInfo(userInfo.employeeId);
  options.compName = branchInfo?.compName || '';

  // 검색조건 기본값 설정
  filterOptions.value = { ...options };
}

// 모달 관련 함수들
const loadOrderItems = async () => {
  try {
    const response = await axios.get('/api/sales/orders');
    orderItems.value = await response.data;
    console.log('Order items loaded:', orderItems.value);
  } catch (error) {
    console.error('Error loading order items:', error);
  }
};

const loadStoreItems = async () => {
  try {
    const response = await axios.get('/api/search/branches/all');
    storeItems.value = await response.data;
    console.log('Store items loaded:', storeItems.value);
  } catch (error) {
    console.error('Error loading store items:', error);
  }
};

const handleOpenModal = async (filterName) => {
  console.log('Open modal for filter:', filterName);
  
  // 지점 직원이 지점명 모달을 열려고 하면 차단
  if (filterName === 'compName' && user.value?.compId !== 'COM10001') {
    console.warn('Branch employees cannot change store filter');
    return;
  }
  
  switch (filterName) {
    case 'soId':
      loadOrderItems();
      orderCodeModalVisible.value = true;
      break;
    case 'compName':
      loadStoreItems();
      storeModalVisible.value = true;
      break;
    default:
      console.warn('No modal defined for filter:', filterName);
  }
};

// 모달창 닫기 함수
const closeOrderModal = () => {
  orderCodeModalVisible.value = false;
}

const closeStoreModal = () => {
  storeModalVisible.value = false;
}

// SearchForm의 값 업데이트 함수
const updateFilterValue = (filterName, selectedItem) => {
  if (searchFormRef.value.searchOptions) {
    searchFormRef.value.searchOptions[filterName] = selectedItem;
  }
};

// 모달창 확인 버튼 함수
const confirmOrderModal = (selectedItems) => {
  console.log('Selected items from order modal:', selectedItems);
  if (selectedItems) {
    updateFilterValue('soId', selectedItems.soId);
  }
  orderCodeModalVisible.value = false;
};

const confirmStoreModal = (selectedItems) => {
  console.log('Selected items from store modal:', selectedItems);
  if (selectedItems) {
    updateFilterValue('compName', selectedItems.compName);
  }
  storeModalVisible.value = false;
};

// 일일 정산 관련 함수들
const openDailySettlement = async () => {
  try {
    // 오늘 날짜 설정 (Date 객체로)
    const today = new Date();
    todaySalesData.value.settlementDate = today;
    
    // 초기 데이터 로드
    await loadDailySalesData(today);
    
    dailySettlementVisible.value = true;
  } catch (error) {
    console.error('Error opening daily settlement:', error);
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: '일일 정산 모달을 여는데 실패했습니다.',
      life: 3000
    });
  }
};

// 선택된 날짜의 판매 데이터 로드
const loadDailySalesData = async (selectedDate) => {
  try {
    const dateString = selectedDate.getFullYear() + '-' + 
                      String(selectedDate.getMonth() + 1).padStart(2, '0') + '-' + 
                      String(selectedDate.getDate()).padStart(2, '0');
    
    const response = await axios.get('/api/sales/daily-summary', {
      params: {
        compId: user.value.compId,
        date: dateString
      }
    });
    
    if (response.data) {
      todaySalesData.value.totalAmount = response.data.totalAmount || 0;
      todaySalesData.value.orderCount = response.data.orderCount || 0;
      todaySalesData.value.cashAmount = response.data.cashAmount || 0;
      todaySalesData.value.transferAmount = response.data.transferAmount || 0;
      todaySalesData.value.cardAmount = response.data.cardAmount || 0;
    }
  } catch (error) {
    console.error('Error loading daily sales data:', error);
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: '선택한 날짜의 판매 데이터를 불러오는데 실패했습니다.',
      life: 3000
    });
  }
};

// 정산 날짜 변경 시 호출
const onSettlementDateChange = async () => {
  if (todaySalesData.value.settlementDate) {
    await loadDailySalesData(todaySalesData.value.settlementDate);
  }
};

const confirmDailySettlement = async () => {
  try {
    // Date 객체를 문자열로 변환
    const dateString = todaySalesData.value.settlementDate.getFullYear() + '-' + 
                      String(todaySalesData.value.settlementDate.getMonth() + 1).padStart(2, '0') + '-' + 
                      String(todaySalesData.value.settlementDate.getDate()).padStart(2, '0');
    
    const settlementData = {
      compId: user.value.compId,
      closingDate: dateString,
      totalPrice: todaySalesData.value.totalAmount
    };
    
    await axios.post('/api/sales/dailyClosing', settlementData);
    
    toast.add({
      severity: 'success',
      summary: '정산 완료',
      detail: '일일 정산이 완료되었습니다.',
      life: 3000
    });
    
    dailySettlementVisible.value = false;
  } catch (error) {
    console.error('Error processing daily settlement:', error);
    toast.add({
      severity: 'error',
      summary: '정산 실패',
      detail: '일일 정산 처리에 실패했습니다.',
      life: 3000
    });
  }
};

const closeDailySettlement = () => {
  dailySettlementVisible.value = false;
};

onMounted(async () => {
  //사용자 정보
  const userInfo = useAuth().user.value;
  
  // 지점 정보에 따른 필터 설정
  let defaultCompName = '';
  
  if (userInfo && userInfo.compId && userInfo.compId !== 'COM10001') {
    // 지점 직원: 자신의 지점명으로 설정하고 비활성화
    defaultCompName = userInfo.compName;
    const compNameFilterIndex = filters.value.filters.findIndex(f => f.name === 'compName');
    if (compNameFilterIndex !== -1) {
      filters.value.filters[compNameFilterIndex].disabled = true;
      filters.value.filters[compNameFilterIndex].value = defaultCompName;
      filters.value.filters[compNameFilterIndex].defaultValue = defaultCompName;
    }
  } else if (userInfo && userInfo.compId === 'COM10001') {
    // 본사 직원: 첫 번째 지점명으로 설정하고 활성화
    try {
      const response = await axios.get('/api/search/branches/all');
      const firstData = await response.data[0];
      defaultCompName = firstData?.compName || '';
      
      const compNameFilterIndex = filters.value.filters.findIndex(f => f.name === 'compName');
      if (compNameFilterIndex !== -1) {
        filters.value.filters[compNameFilterIndex].disabled = false;
        filters.value.filters[compNameFilterIndex].defaultValue = defaultCompName;
      }
    } catch (error) {
      console.error('Error loading branches for headquarters:', error);
    }
  }
  
  // 검색조건 기본값 설정
  await defaultFilterOptions(userInfo);
  
  // 지점명 기본값 추가 설정
  if (defaultCompName) {
    filterOptions.value.compName = defaultCompName;
  }
  
  resetSearchData();
  // 주문서 정보 조회
  getSalesOrders(filterOptions.value);
});
</script>
<template>
  <SearchForm ref="searchFormRef" :filters="filters" 
    @searchData="searchData" @resetSearchOptions="resetSearchData"
    @open-search-modal="handleOpenModal"
    :defaultValues="filterOptions"
  />
  
  <!-- 지점 직원용 일일 정산 버튼 -->
  <div v-if="user?.compId !== 'COM10001'" class="mb-3">
    <Button 
      label="일일 정산" 
      icon="pi pi-calculator" 
      class="p-button-info"
      @click="openDailySettlement"
    />
  </div>
  
  <BasicTable :data="masterItems" :header="masterHeader" :checked="true" :dataKey="'soId'" @rowSelect="onRowSelect" @rowUnselect="onRowUnselect" :scrollHeight="'200px'"></BasicTable>
  <BasicTable :data="detailItems" :header="detailHeader" :scrollHeight="'200px'"></BasicTable>
  
  <!-- 주문코드 선택 모달 -->
  <DialogModal v-model:display="orderCodeModalVisible" :items="orderItems" :headers="orderHeaders" title="주문코드 검색"
    selectionMode="single" @close="closeOrderModal" @confirm="confirmOrderModal" />
  
  <!-- 지점 선택 모달 -->
  <DialogModal v-model:display="storeModalVisible" :items="storeItems" :headers="storeHeaders" title="지점 검색"
    selectionMode="single" @close="closeStoreModal" @confirm="confirmStoreModal" />
  
  <!-- 일일 정산 모달 -->
  <Dialog 
    v-model:visible="dailySettlementVisible" 
    header="일일 정산" 
    :modal="true" 
    :closable="true"
    :style="{ width: '400px' }"
    @hide="closeDailySettlement"
  >
    <div class="p-4">
      <!-- 정산 날짜 선택 -->
      <div class="mb-4">
        <label class="block text-sm font-medium mb-2">정산 날짜</label>
        <Calendar 
          v-model="todaySalesData.settlementDate" 
          :showIcon="true"
          dateFormat="yy-mm-dd"
          :maxDate="new Date()"
          @date-select="onSettlementDateChange"
          placeholder="정산할 날짜를 선택하세요"
          class="w-full"
        />
      </div>
      
      <div class="mb-4" v-if="todaySalesData.settlementDate">
        <h4 class="text-lg font-semibold mb-2">
          {{ todaySalesData.settlementDate.toLocaleDateString('ko-KR') }} 판매 현황
        </h4>
      </div>
      
      <div class="grid grid-cols-1 gap-4 mb-6" v-if="todaySalesData.settlementDate">
        <div class="p-3 border rounded-lg bg-gray-50">
          <div class="text-sm text-gray-600">총 주문 건수</div>
          <div class="text-xl font-bold">{{ todaySalesData.orderCount }}건</div>
        </div>
        
        <div class="p-3 border rounded-lg bg-blue-50">
          <div class="text-sm text-gray-600">총 판매 금액</div>
          <div class="text-2xl font-bold text-blue-600">
            {{ Number(todaySalesData.totalAmount).toLocaleString() }}원
          </div>
        </div>
        
        <!-- 결제 방식별 금액 -->
        <div class="grid grid-cols-3 gap-2">
          <div class="p-2 border rounded bg-green-50">
            <div class="text-xs text-gray-600">현금</div>
            <div class="text-sm font-semibold text-green-600">
              {{ Number(todaySalesData.cashAmount).toLocaleString() }}원
            </div>
          </div>
          
          <div class="p-2 border rounded bg-yellow-50">
            <div class="text-xs text-gray-600">계좌이체</div>
            <div class="text-sm font-semibold text-yellow-600">
              {{ Number(todaySalesData.transferAmount).toLocaleString() }}원
            </div>
          </div>
          
          <div class="p-2 border rounded bg-purple-50">
            <div class="text-xs text-gray-600">카드결제</div>
            <div class="text-sm font-semibold text-purple-600">
              {{ Number(todaySalesData.cardAmount).toLocaleString() }}원
            </div>
          </div>
        </div>
      </div>
      
      <div class="text-center" v-if="todaySalesData.settlementDate">
        <p class="text-gray-600 mb-4">위 금액으로 정산을 진행하시겠습니까?</p>
        
        <div class="flex justify-center gap-2">
          <Button 
            label="취소" 
            icon="pi pi-times" 
            class="p-button-secondary"
            @click="closeDailySettlement"
          />
          <Button 
            label="정산하기" 
            icon="pi pi-check" 
            class="p-button-success"
            @click="confirmDailySettlement"
          />
        </div>
      </div>
    </div>
  </Dialog>
</template>