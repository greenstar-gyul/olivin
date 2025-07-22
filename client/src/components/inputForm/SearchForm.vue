<script setup>
import { ref, computed } from 'vue';
import DatePickerFromTo from './DatePickerFromTo.vue';

const props = defineProps({
  filters: {
    type: Array,
    required: true
  }
});

const emit = defineEmits(['searchData']);

// 검색 조건을 담을 객체
const searchOptions = ref({});

// filters 기반으로 기본값 초기화, 각 필터의 name을 키로 사용.
// 단, dateRange 타입의 필터는 fromValue와 toValue로 분리하여 처리
const initializeSearchOptions = () => {
  const options = {};
  props.filters.forEach(element => {
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
};

const confirm = () => {
  emit('searchData', searchOptions.value);
}

// 필터별로 그룹화 (칸 수 고려해서 배치)
const groupedFilters = computed(() => {
  const filters = props.filters;
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

</script>
<template>
  <div class="p-6 bg-gray-50 shadow-md rounded-md">
    
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
        <span class="text-lg font-medium text-gray-700 whitespace-nowrap w-28 flex-shrink-0 text-right">{{ filter.label }}</span>

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