<script setup>
import { ref, computed } from 'vue';
import InputForm from '../inputForm/ApproveInputForm.vue';
import InputMaster from '../inputForm/InputMaster.vue';
import SearchForm from '../inputForm/SearchForm.vue';
import BasicTable from '../table/BasicTable.vue';

const emit = defineEmits(['searchData', 'approve', 'reject', 'rowSelect', 'saveData']); 
const props = defineProps({
  filters: {
    type: Array,
    required: true
  },
  items: {
    type: Array,
    default: () => []
  },
  header: {
    type: Object,
    required: true
  },
  inputs: {
    type: Object,
    required: true
  },
  checkType: {
    type: String,
    default: 'single'
  }
});

const selectedItems = ref(null);

const searchData = (searchOptions) => {
  console.log('🔍 StandardApproval - searchData 호출:', searchOptions);
  emit('searchData', searchOptions);
};

const saveData = (inputData) => {
  console.log('💾 StandardApproval - saveData 호출:', inputData);
  emit('saveData', inputData);
};

// ✅ 승인/반려 함수
const approve = () => {
  console.log('✅ StandardApproval - approve 호출:', selectedItems.value);
  emit('approve', selectedItems.value);
};

const reject = () => {
  console.log('❌ StandardApproval - reject 호출:', selectedItems.value);
  emit('reject', selectedItems.value);
};

// ✅ 행 선택 처리 함수 개선
const onRowSelect = (data) => {
  console.log('🎯 StandardApproval - 행 선택됨:', data);
  console.log('🔍 데이터 타입:', typeof data);
  console.log('🔍 데이터 내용:', JSON.stringify(data, null, 2));
  
  // 선택된 아이템 저장
  if (props.checkType === 'single') {
    selectedItems.value = data;
  } else {
    if (!selectedItems.value) {
      selectedItems.value = [];
    }
    // 중복 방지
    const existingIndex = selectedItems.value.findIndex(item => 
      item && data && item.productId === data.productId
    );
    if (existingIndex === -1) {
      selectedItems.value.push(data);
    }
  }
  
  // 부모 컴포넌트로 이벤트 전달
  console.log('📤 부모로 rowSelect 이벤트 전달');
  emit('rowSelect', data);
}

const onRowUnselect = (data) => {
  console.log('🔄 StandardApproval - 행 선택 해제됨:', data);
  
  if (props.checkType !== 'single') {
    selectedItems.value = selectedItems.value?.filter(item => 
      item.productId !== data.productId
    ) || [];
  } else {
    selectedItems.value = null;
  }
  
  // 선택 해제 시에도 부모에게 알림 (null 전달)
  emit('rowSelect', null);
};

// ✅ BasicTable 이벤트 핸들러 추가
const handleTableRowClick = (data) => {
  console.log('🖱️ StandardApproval - 테이블 행 클릭:', data);
  onRowSelect(data);
};

const handleTableItemClick = (data) => {
  console.log('🖱️ StandardApproval - 테이블 아이템 클릭:', data);
  onRowSelect(data);
};
</script>

<template>
  <SearchForm :filters="props.filters" @searchData="searchData" />
  <div class="grid grid-cols-7 gap-4 mb-4 items-stretch">
    <BasicTable 
      :data="props.items" 
      :header="props.header" 
      :checked="true" 
      @rowSelect="onRowSelect" 
      @rowUnselect="onRowUnselect"
      @rowClick="handleTableRowClick"
      @itemClick="handleTableItemClick"
      @click="handleTableItemClick"
      class="col-span-4"
    >
      <template #btn>
        <Button label="일괄승인" severity="success" class="min-w-fit whitespace-nowrap" outlined @click="approve"></Button>
        <Button label="일괄반려" severity="danger" class="min-w-fit whitespace-nowrap" outlined @click="reject"></Button>
      </template>
    </BasicTable>
    <InputForm 
      :inputs="props.inputs" 
      @saveData="saveData" 
      class="col-span-3"
    >
      <template #btn>
        <Button label="승인" severity="success" class="min-w-fit whitespace-nowrap" outlined @click="approve"></Button>
        <Button label="반려" severity="danger" class="min-w-fit whitespace-nowrap" outlined @click="reject"></Button>
      </template>
    </InputForm>
  </div>
</template>

<style scoped>
</style>