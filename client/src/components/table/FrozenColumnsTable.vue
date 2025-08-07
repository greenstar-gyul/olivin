<script setup>
import { ref, onBeforeMount } from 'vue';
import { CustomerService } from '@/service/CustomerService';

const customers2 = ref(null);
const balanceFrozen = ref(false);
const representatives = reactive([
    { name: 'Amy Elsner', image: 'amyelsner.png' },
    { name: 'Anna Fali', image: 'annafali.png' },
    { name: 'Asiya Javayant', image: 'asiyajavayant.png' },
    { name: 'Bernardo Dominic', image: 'bernardodominic.png' },
    { name: 'Elwin Sharvill', image: 'elwinsharvill.png' },
    { name: 'Ioni Bowcher', image: 'ionibowcher.png' },
    { name: 'Ivan Magalhaes', image: 'ivanmagalhaes.png' },
    { name: 'Onyama Limba', image: 'onyamalimba.png' },
    { name: 'Stephen Shaw', image: 'stephenshaw.png' },
    { name: 'XuXue Feng', image: 'xuxuefeng.png' }
]);
function formatCurrency(value) {
  return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

onBeforeMount(async () => {
  customers2.value = await CustomerService.getCustomersLarge();
});
</script>

<template>
  <div class="card">
    <div class="font-semibold text-xl mb-4">Frozen Columns</div>
    <ToggleButton v-model="balanceFrozen" onIcon="pi pi-lock" offIcon="pi pi-lock-open" onLabel="Balance" offLabel="Balance" />
    <!-- DataTable 포함된 Frozen Columns 렌더링 -->
  </div>
</template>
