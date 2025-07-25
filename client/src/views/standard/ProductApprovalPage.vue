<script setup>
import StandardInput from '@/components/common/StandardApproval.vue';
import { ref } from 'vue';

const filters = ref({
  title: '승인 요청 조회',
  filters: [
    { type: 'text', label: '제품명', value: '', name: 'name' },
    { type: 'text', label: '제품분류', value: '', name: 'category' },
    { type: 'text', label: '공급사', value: '', name: 'publisher' },
    { type: 'dateRange', label: '신청일 범위', value: '', name: 'dateRange' }
  ]
});

const items = ref([
  { id: 1, name: '블루베리 크림', category: '화장품', publisher: '제이뷰티', price: 13000, registrant: '김영희', requestDate: '2025-07-22' },
  { id: 2, name: '시카폼 클렌저', category: '화장품', publisher: '에버뷰티', price: 9500, registrant: '홍길동', requestDate: '2025-07-23' },
]);

const header = ref({
  title: '제품 승인 요청 목록',
  header: {
    id: 'ID',
    name: '제품명',
    category: '제품분류',
    publisher: '공급사',
    price: '판매단가',
    registrant: '등록자',
    requestDate: '신청일자'
  },
  rightAligned: ['price']
});

const inputs = ref({
  title: '제품 정보 및 승인 처리',
  inputs: [
    { type: 'text', label: '제품명', value: '', name: 'name', readonly: true },
    { type: 'text', label: '제품분류', value: '', name: 'category', readonly: true },
    { type: 'text', label: '공급사', value: '', name: 'publisher', readonly: true },
    { type: 'number', label: '판매단가', value: '', name: 'price', readonly: true },
    { type: 'text', label: '등록자', value: '', name: 'registrant', readonly: true },
    { type: 'date', label: '신청일자', value: '', name: 'requestDate', readonly: true },
    { type: 'textarea', label: '설명', value: '', name: 'description', placeholder: '제품 설명을 입력하세요' },
  ]
});

// 선택 시 상세 정보 입력폼에 바인딩
const onRowSelect = (row) => {
  inputs.value.inputs.forEach(input => {
    if (input.name in row) input.value = row[input.name]
  });
};

const searchData = (searchOptions) => {
  console.log('승인 제품 검색:', searchOptions);
};

const saveData = (inputData) => {
  console.log('선택 제품 승인 처리:', inputData);
};

const handleApprove = (selectedData) => {
  console.log('승인 처리:', selectedData);
  alert('제품이 승인되었습니다.');
};

const handleReject = (selectedData) => {
  console.log('반려 처리:', selectedData);
  alert('제품이 반려되었습니다.');
};
</script>

<template>
  <StandardInput
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