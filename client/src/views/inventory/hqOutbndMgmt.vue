<script setup>
import InputMultiTable from '@/components/common/InputMultiTable.vue';
import { onMounted, ref } from 'vue';
import axios from '@/service/axios';


// 테스트 데이터
const items = ref([]);

/* Form Data */
// 폼 기본값
const formData = {
  data: 0.11,
};

// 폼 스키마
const formSchema = [
  { type: 'text', label: '출고번호', id: 'outbndNo' },
  { type: 'text', label: '발주명', id: 'orderId' },
  { type: 'text', label: '출고지', id: 'outbndFrom' },
  { type: 'text', label: '입고지', id: 'inbndTo' },
  { type: 'date', label: '출고일', id: 'outbndDate'}, 
  { type: 'text', label: '출고상태', id: 'outbndStatus', 
    // options: [
    //   { name: '옵션1', value: 1 },
    //   { name: '옵션2', value: 2 }
    // ]
  },
];

/* Input Table */
const tableData = ref([]);

const tableHeader = {
  title: '제품 목록',
  header: {
    productName: '제품명',
    orderQuantity: '발주수량',
    totalOutbndQuantity  : '출고수량',
    unit: '단위',
    outbndStatus: '출고상태',
  },
  rightAligned: ['price']
};

/* Input Table Detail */
const detailData = ref([]);

// 테이블 컬럼 정의
const columns = [
  { inputType: 'text', header: 'LOT', field: 'lotNo' },
  { inputType: 'number', header: '출고수량', field: 'outbndQuantity' },
  { inputType: 'text', header: '단위', field: 'unit' },
  { inputType: 'text', header: '출고상태', field: 'outbnStatus' },
];
/* Submit */

// 폼과 테이블 데이터를 저장하는 핸들러
const onRowSelect = (select) => {
  // console.log('Selected Row:', select);
  
  // fetch 사용하는 시간때문에 250ms 지연
  // 실제로는 서버에서 데이터를 가져오는 로직이 필요합니다.
  setTimeout(() => {
    // 선택된 행의 데이터를 폼에 채우기
    // axios나 fetch를 사용하여 서버에서 데이터를 가져올 수 있습니다.
    if (select.id === 1) {
      detailData.value = [
        { id: 1, lot: 'LOT001', quantity: 10, status: '출고대기' },
        { id: 2, lot: 'LOT002', quantity: 20, status: '출고대기' }
      ];
    } else if (select.id === 2) {
      detailData.value = [
        { id: 3, lot: 'LOT003', quantity: 30, status: '출고대기' },
        { id: 4, lot: 'LOT004', quantity: 40, status: '출고완료' }
      ];
    } else {
      detailData.value = [];
    }
  }, 250);
};

const importHandler = () => {
  console.log('데이터 불러오기');
};

const exportHandler = () => {
  console.log('출고 처리');
};



// 테스트 함수
const getSampleData = async () => {
  try {
    const result = await axios.get('/api/hqOutbndMgmt');
    // const data = await result.data;
    tableData.value = await result.data;
    console.log('Loaded sample data:', tableData.value);
    console.log('test data:', result.data);
    // console.log('Loaded sample data:', data);
    // console.log('test:', data[0].inbndNo);
    // console.log('testGGG : ', tableData[0].name);
  
    // items에 데이터를 할당
    // items.value = data;
    // console.log('test items', items.value[0].inbndNo)
    // tableData[0].name = data[0].inbndNo;
  } catch (e) {
    console.error('출고 데이터 불러오기 실패:', e)
  }
};


onMounted(() => {  
  getSampleData();
});


</script>
<template>
  <InputMultiTable title="출고정보"
    :defaultForm="formData"
    :formSchema="formSchema"
    :tableData="tableData"
    :tableHeader="tableHeader"
    :detailData="detailData"
    :detailColumns="columns" :detailCRUD="true"
    @onRowSelect="onRowSelect">
    <template #btn>
      <Button label="발주정보불러오기" class="min-w-fit whitespace-nowrap" severity="info" @click="importHandler" outlined />
    </template>
    <template #detailBtn>
      <Button label="출고처리" class="min-w-fit whitespace-nowrap" severity="success" @click="exportHandler" outlined />
    </template>
  </InputMultiTable>
</template>
