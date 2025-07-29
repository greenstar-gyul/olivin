<script setup>
import SearchForm from '@/components/inputForm/SearchForm.vue';
import BasicTable from '@/components/table/BasicTable.vue';
import dateUtils from '@/utils/dateUtils';
import { ref } from 'vue';

const emit = defineEmits(['searchData', 'openSearchModal']);
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
  }
});

const searchData = (searchOptions) => {
  emit('searchData', searchOptions);
};

const openSearchModal = (filterName) => {
  emit('openSearchModal', filterName);
};

// SearchForm의 ref
const searchFormRef = ref(null);

// 외부에서 SearchForm에 접근할 수 있도록 노출
defineExpose({
  searchFormRef
});

</script>
<template>
  <SearchForm ref="searchFormRef" :filters="props.filters" @searchData="searchData" @openSearchModal="openSearchModal" ></SearchForm>
  <BasicTable :data="props.items" :header="props.header"></BasicTable>
</template>