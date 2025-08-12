<script setup>
import SearchForm from '@/components/inputForm/SearchForm.vue';
import { ref } from 'vue';
import DetailTable from '../table/DetailTable.vue';

const emit = defineEmits(['searchData', 'openSearchModal', 'resetSearchOptions', 'actionHandler']);
const props = defineProps({
    filters: {
        type: Object,
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

const resetSearchOptions = () => {
    emit('resetSearchOptions');
};

const actionHandler = (rowData) => {
    emit('actionHandler', rowData);
};

// SearchForm의 ref
const searchFormRef = ref(null);

// 외부에서 SearchForm에 접근할 수 있도록 노출
defineExpose({
    searchFormRef
});
</script>
<template>
    <SearchForm ref="searchFormRef" :filters="props.filters" @searchData="searchData" @openSearchModal="openSearchModal" @resetSearchOptions="resetSearchOptions"></SearchForm>
    <DetailTable :data="props.items" :header="props.header" :isAction="true" :actionName="'보기'" @actionHandler="actionHandler"></DetailTable>
</template>
