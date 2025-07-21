<script setup>
import BasicTable from '@/components/table/BasicTable.vue';
import dateUtils from '@/utils/dateUtils';
import { ref } from 'vue';

const testItems = ref([
  {id: 1, name: 'Item 1', description: 'Description 1', date: dateUtils.convertDate('2025-01-01')},
  {id: 2, name: 'Item 2', description: 'Description 2', date: dateUtils.convertDate('2025-01-02')},
  {id: 3, name: 'Item 3', description: 'Description 3', date: dateUtils.convertDate('2025/01/03')},
]);

</script>
<template>
  <BasicTable :data="testItems" :header="{id: 'id', name: '이름', description: '요약', date: '날짜'}"></BasicTable>
</template>