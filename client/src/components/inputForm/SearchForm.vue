<script setup>
import { ref, computed } from 'vue';
import DatePickerFromTo from './DatePickerFromTo.vue';

const props = defineProps({
  filters: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['searchData', 'openSearchModal', 'resetSearchOptions']);

// 검색 조건을 담을 객체
const searchOptions = ref({});

// 모달 관련
const openSearchModal = (filterName) => {
  emit('openSearchModal', filterName);
};

// filters 기반으로 기본값 초기화, 각 필터의 name을 키로 사용.
// 단, dateRange 타입의 필터는 fromValue와 toValue로 분리하여 처리
const initializeSearchOptions = () => {
  const options = {};
  props.filters.filters.forEach(element => {
    if (element.type === 'dateRange') {
      options[element.name + 'From'] = '';
      options[element.name + 'To'] = '';
      return;
    }
    options[element.name] = '';
  });
  searchOptions.value = options;
};

// 초기화
initializeSearchOptions();

const resetSearchOptions = () => {
  initializeSearchOptions();
  emit('resetSearchOptions');
};

const confirm = () => {
  emit('searchData', searchOptions.value);
}

// 필터별로 그룹화 (칸 수 고려해서 배치)
const groupedFilters = computed(() => {
  const filters = props.filters.filters;
  const rows = [];
  let currentRow = [];
  let currentRowCols = 0;
  const maxColsPerRow = 4;
  
  for (const filter of filters) {
    const colSpan = (filter.type === 'dateRange' || filter.type === 'textarea') ? 2 : 1;
    
    // 현재 행에 추가할 수 있는지 확인
    if (currentRowCols + colSpan <= maxColsPerRow) {
      currentRow.push(filter);
      currentRowCols += colSpan;
    } else {
      // 현재 행을 완료하고 새 행 시작
      if (currentRow.length > 0) {
        rows.push(currentRow);
      }
      currentRow = [filter];
      currentRowCols = colSpan;
    }
  }
  
  // 마지막 행 추가
  if (currentRow.length > 0) {
    rows.push(currentRow);
  }
  
  return rows;
});

// 외부에서 searchOptions에 접근할 수 있도록 노출
defineExpose({
  searchOptions
});

</script>
<template>
  <div class="card flex flex-col gap-4">
    <!-- 테이블 상단 (타이틀 + 엑셀 다운로드 버튼) -->
    <div class="grid grid-cols-1 gap-4 mb-4">
      <div class="flex justify-between">
        <div>
          <div class="font-semibold text-2xl">{{ filters.title }}</div>
        </div>
        <div class="flex items-center gap-2 flex-nowrap">
          <!-- <Button label="엑셀 다운로드" severity="success" class="min-w-fit whitespace-nowrap" outlined /> -->
        </div>
      </div>
    </div>
    
    <!-- 각 행별로 렌더링 -->
    <div 
      v-for="(row, rowIndex) in groupedFilters" 
      :key="rowIndex" 
      class="grid grid-cols-4 gap-4 mb-4"
    >
      <div 
        v-for="(filter, filterIndex) in row" 
        :key="filter.name || filterIndex"
        class="flex items-center gap-2"
        :class="(filter.type === 'dateRange' || filter.type === 'textarea') ? 'col-span-2' : 'col-span-1'"
      >
        <span class="text-lg font-medium text-gray-700 whitespace-nowrap w-28 flex-shrink-0 text-center">{{ filter.label }}</span>

        <!-- Text Input -->
        <InputText v-if="filter.type === 'text'" :id="'filter-' + rowIndex + '-' + filterIndex" type="text"
          v-model="searchOptions[filter.name]" :placeholder="filter.placeholder || 'Enter text...'" class="flex-1" />

        <!-- Textarea Input -->
        <Textarea v-else-if="filter.type === 'textarea'" :id="'filter-' + rowIndex + '-' + filterIndex"
          v-model="searchOptions[filter.name]" :placeholder="filter.placeholder || 'Enter text...'" class="flex-1" rows="3" />
        
        <!-- Date Picker -->
        <DatePicker v-else-if="filter.type === 'date'" :id="'filter-' + rowIndex + '-' + filterIndex"
          v-model="searchOptions[filter.name]" :placeholder="filter.placeholder || 'Select date...'"
          dateFormat="yy-mm-dd" class="flex-1" :show-icon="true" :show-button-bar="true"/>

        <!-- Date Picker From To -->
        <DatePickerFromTo v-else-if="filter.type === 'dateRange'" 
          v-model:fromValue="searchOptions[filter.name + 'From']"
          v-model:toValue="searchOptions[filter.name + 'To']" 
          :fromPlaceholder="filter.fromPlaceholder"
          :toPlaceholder="filter.toPlaceholder" 
          class="flex-1" />

        <!-- Number Input -->
        <InputNumber v-else-if="filter.type === 'number'" :id="'filter-' + rowIndex + '-' + filterIndex"
          v-model="searchOptions[filter.name]" :placeholder="filter.placeholder || 'Enter number...'" class="flex-1" />

        <!-- Select Input -->
        <Select v-else-if="filter.type === 'select'" :id="'filter-' + rowIndex + '-' + filterIndex"
          v-model="searchOptions[filter.name]" :options="filter.options" showClear
          optionLabel="name" optionValue="value" :placeholder="filter.placeholder || 'Select option...'" class="flex-1" />

        <!-- Item Search -->
        <InputGroup v-else-if="filter.type === 'item-search'" class="flex-1">
          <InputText :id="'filter-' + rowIndex + '-' + filterIndex" v-model="searchOptions[filter.name]" :placeholder="filter.placeholder || 'Enter item name...'" />
          <Button icon="pi pi-search" class="p-button-outlined" @click="openSearchModal(filter.name)" />
        </InputGroup>

        <!-- Default fallback to text input -->
        <InputText v-else :id="'filter-' + rowIndex + '-' + filterIndex" type="text"
          v-model="searchOptions[filter.name]" :placeholder="filter.placeholder || 'Enter text...'" class="flex-1" />
      </div>

      <!-- 빈 공간은 Grid에서 자동으로 처리됨 -->
    </div>

    <div class="flex justify-center gap-3 mt-6">
      <Button label="초기화" severity="secondary" @click="resetSearchOptions" />
      <Button label="조회" @click="confirm" />
    </div>
  </div>

</template>