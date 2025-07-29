<script setup>
import { FilterMatchMode, FilterOperator } from '@primevue/core/api';
import { onBeforeMount, ref } from 'vue';

// 거래처원장 데이터 ref
const customers = ref([]);
// const filters1 = ref(null);
const loading = ref(true);
const balanceFrozen = ref(false);


onBeforeMount(() => {
    ProductService.getProductsWithOrdersSmall().then((data) => (products.value = data));
    CustomerService.getCustomersLarge().then((data) => {
        customers.value = data;
        loading1.value = false;
        customers.value.forEach((customer) => (customer.date = new Date(customer.date)));
    });

    initFilters1();
});

function initFilters1() {
    filters1.value = {
        global: { value: null, matchMode: FilterMatchMode.CONTAINS },
        name: { operator: FilterOperator.AND, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
        'country.name': { operator: FilterOperator.AND, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
        representative: { value: null, matchMode: FilterMatchMode.IN },
        date: { operator: FilterOperator.AND, constraints: [{ value: null, matchMode: FilterMatchMode.DATE_IS }] },
        balance: { operator: FilterOperator.AND, constraints: [{ value: null, matchMode: FilterMatchMode.EQUALS }] },
        status: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.EQUALS }] },
        activity: { value: [0, 100], matchMode: FilterMatchMode.BETWEEN },
        verified: { value: null, matchMode: FilterMatchMode.EQUALS }
    };
}

function expandAll() {
    expandedRows.value = products.value.reduce((acc, p) => (acc[p.id] = true) && acc, {});
}

function collapseAll() {
    expandedRows.value = null;
}

function formatCurrency(value) {
    return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

function formatDate(value) {
    return value.toLocaleDateString('en-US', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

function calculateCustomerTotal(name) {
    let total = 0;
    if (customers3.value) {
        for (let customer of customers3.value) {
            if (customer.representative.name === name) {
                total++;
            }
        }
    }

    return total;
}
</script>
<template>
      <div class="card">
        <div class="font-semibold text-xl mb-4">거래처원장</div>
        <ToggleButton v-model="balanceFrozen" onIcon="pi pi-lock" offIcon="pi pi-lock-open" onLabel="Balance" offLabel="Balance" />
        <DataTable :value="customers2" scrollable scrollHeight="400px" class="mt-6">
        <Column field="accountlederId" header="거래처원장ID" style="min-width: 200px" frozen class="font-bold"></Column>
        <Column field="compId" header="거래처ID" style="min-width: 200px"></Column>
        <Column field="accountId" header="계정과목" style="min-width: 100px"></Column>
        <Column field="increase" header="차변" style="min-width: 200px"></Column>
        <Column field="decrease" header="대변" style="min-width: 200px"></Column>
        <Column field="writeDate" header="작성일" style="min-width: 200px"></Column>
        <Column field="detail" header="상세" style="min-width: 200px"></Column>
        <Column field="productId" header="제품ID" style="min-width: 200px"></Column>
        <Column field="balance" header="잔액" style="min-width: 200px"></Column>
        <Column field="balance" header="Balance" style="min-width: 200px" alignFrozen="right" :frozen="balanceFrozen">
            <template #body="{ data }">
            <span class="font-bold">{{ formatCurrency(data.balance) }}</span>
            </template>
        </Column>
        </DataTable>
    </div>
</template>