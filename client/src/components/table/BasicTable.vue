<script setup>
import { ref, watch } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

// emit 정의, props
const emit = defineEmits(['rowSelect', 'rowUnselect']);
const props = defineProps({
  data: {
    type: Array,
    required: true
  },
  dataKey: {
    type: String,
    default: 'id'
  },
  header: {
    type: Object,
    required: true
  },
  columns: {
    type: Array,
    default: []
  },
  checked: {
    type: Boolean,
    default: false
  },
  checkType: {
    type: String,
    default: 'single'
  }
});

const items = ref([]);

// checkType에 따라 selectedItems 초기값 설정
const selectedItems = ref(props.checkType === 'single' ? null : []);

// 여러 개의 watch 대신 computed 사용
const tableColumns = computed(() => {
  // 1. props.columns가 명시적으로 제공되면 최우선으로 사용
  if (props.columns && props.columns.length > 0) {
    return props.columns;
  }
  // 2. props.header.header 객체가 있으면 키 목록을 사용
  if (props.header && props.header.header) {
    return Object.keys(props.header.header);
  }
  // 3. 데이터(props.data)가 있고 첫 번째 항목이 존재하면 그 객체의 키를 사용
  if (Array.isArray(props.data) && props.data.length > 0) {
    return Object.keys(props.data[0]);
  }
  // 모든 조건이 충족되지 않으면 빈 배열 반환
  return [];
});


const onRowSelect = (event) => {
  // 선택된 행이 있을 때만 처리
  if (props.checkType === 'single') {
    selectedItems.value = event.data;
  } else {
    // 다중 선택 모드에서는 선택된 아이템을 배열로 유지
    if (!selectedItems.value.includes(event.data)) {
      selectedItems.value.push(event.data);
    }
  }
  emit('rowSelect', event.data);
};

const onRowUnselect = (event) => {
  // 다중 선택 모드에서 행이 선택 해제되면 해당 아이템 제거
  if (props.checkType !== 'single') {
    selectedItems.value = selectedItems.value.filter(item => item !== event.data);
  }
  emit('rowUnselect', event.data);
};

</script>
<template>
  <!-- 📋 검색 조회 테이블 영역 -->
  <div class="card flex flex-col gap-4 mt-6 h-full">
    <!-- 테이블 상단 (타이틀 + 엑셀 다운로드 버튼) -->
    <div class="grid grid-cols-1 gap-4 mb-4">
      <div class="flex justify-between">
        <div>
          <div class="font-semibold text-2xl">{{ header.title }}</div>
        </div>
        <div class="flex items-center gap-2 flex-nowrap">
          <slot name="btn"></slot>
          <!-- <Button label="엑셀 다운로드" severity="success" class="min-w-fit whitespace-nowrap" outlined /> -->
        </div>
      </div>
    </div>
    
    <!-- DataTable (PrimeVue) -->
    <DataTable v-model:selection="selectedItems" :value="props.data" :dataKey="props.dataKey" showGridlines scrollable
      scrollHeight="400px" tableStyle="min-width: 50rem" @rowSelect="onRowSelect" @rowUnselect="onRowUnselect"
      :selectionMode="props.checked ? props.checkType : null">

      <Column v-if="props.checked" :selectionMode="props.checkType" headerStyle="width: 3rem"></Column>

      <!-- 동적 컬럼 생성 -->
      <Column v-for="colKey in tableColumns" :key="colKey" :field="colKey" :header="header.header[colKey] ?? colKey">
        <template #body="slotProps">
            <span v-if="header.rightAligned && header.rightAligned.includes(colKey)" class="text-right block">
                {{ Number(slotProps.data[colKey]).toLocaleString() }}
            </span>
            <span v-else>
                {{ slotProps.data[colKey] }}
            </span>
        </template>
      </Column>
    </DataTable>
  </div>
</template>
<style scoped>
/* 필요시 커스텀 스타일 여기에 추가 */
</style>