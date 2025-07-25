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

</script>
<template>
  <SearchForm :filters="props.filters" @searchData="searchData" @openSearchModal="openSearchModal" ></SearchForm>
  <BasicTable :data="props.items" :header="props.header"></BasicTable>
</template>