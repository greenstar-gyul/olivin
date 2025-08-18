<script setup>
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { ref } from 'vue';

defineProps({
    data: { type: Array, required: true },
    dataKey: { type: String, default: 'id' },
    headerInfo: { type: Object, default: () => ({ title: 'Data Table' }) },
    columns: { type: Array, required: true },
    checked: { type: Boolean, default: false },
    checkType: { type: String, default: 'single' },
    loading: { type: Boolean, default: false }
});

const emit = defineEmits(['rowSelect', 'rowUnselect']);
const selectedItems = ref(null);

const onRowSelect = (event) => emit('rowSelect', event.data);
const onRowUnselect = (event) => emit('rowUnselect', event.data);
</script>

<template>
    <div class="card flex flex-col gap-4 mt-6 h-full">
        <div class="flex justify-between items-center mb-4">
            <div class="font-semibold text-2xl">{{ headerInfo.title }}</div>
            <div class="flex items-center gap-2 flex-nowrap">
                <slot name="header-buttons"></slot>
            </div>
        </div>

        <DataTable 
            v-model:selection="selectedItems" 
            :value="data" 
            :dataKey="dataKey" 
            :loading="loading"
            showGridlines 
            scrollable 
            scrollHeight="400px" 
            tableStyle="min-width: 50rem"
            responsiveLayout="scroll"
            stripedRows
            @rowSelect="onRowSelect" 
            @rowUnselect="onRowUnselect"
        >
            <Column v-if="checked" :selectionMode="checkType" headerStyle="width: 3rem"></Column>

            <Column 
                v-for="col in columns" 
                :key="col.field" 
                :field="col.field" 
                :header="col.header" 
                :style="col.style" 
                :frozen="col.frozen"
                :class="col.class"
                :headerClass="col.headerClass"
                :alignFrozen="col.alignFrozen"
                sortable
            >
                <template #body="slotProps">
                    <slot :name="`body-${col.field}`" :data="slotProps.data">
                        {{ slotProps.data[col.field] || '' }}
                    </slot>
                </template>
            </Column>

            <!-- 데이터가 없을 때 표시할 메시지 -->
            <template #empty>
                <div class="text-center py-4">
                    <i class="pi pi-info-circle mr-2"></i>
                    조회된 데이터가 없습니다.
                </div>
            </template>
        </DataTable>
    </div>
</template>