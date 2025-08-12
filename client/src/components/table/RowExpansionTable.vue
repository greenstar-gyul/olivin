<script setup>
import { ref, onBeforeMount } from 'vue';
import { ProductService } from '@/service/ProductService';

const products = ref([]);
const expandedRows = ref([]);

function formatCurrency(value) {
    return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

function getStockSeverity(product) {
    switch (product.inventoryStatus) {
        case 'INSTOCK':
            return 'success';
        case 'LOWSTOCK':
            return 'warn';
        case 'OUTOFSTOCK':
            return 'danger';
        default:
            return null;
    }
}

function getOrderSeverity(order) {
    switch (order.status) {
        case 'DELIVERED':
            return 'success';
        case 'CANCELLED':
            return 'danger';
        case 'PENDING':
            return 'warn';
        case 'RETURNED':
            return 'info';
        default:
            return null;
    }
}

function expandAll() {
    expandedRows.value = products.value.reduce((acc, p) => (acc[p.id] = true) && acc, {});
}

function collapseAll() {
    expandedRows.value = null;
}

onBeforeMount(async () => {
    products.value = await ProductService.getProductsWithOrdersSmall();
});
</script>

<template>
    <div class="card">
        <div class="font-semibold text-xl mb-4">Row Expansion</div>
        <!-- DataTable 및 관련 확장 열 구성 -->
    </div>
</template>
