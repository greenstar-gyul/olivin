<script setup>
import SearchTable from '@/components/common/SearchTable.vue';

const filters = [
  {}
];
const items = [];
const header = [];

function searchData(options) {
  
}

</script>

<template>
  <SearchTable :filters="filters" :items="items" :header="header" @searchData="searchData"></SearchTable>
</template>