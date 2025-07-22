<script setup>
import { ref, reactive, onBeforeMount } from 'vue';
import { CustomerService } from '@/service/CustomerService';

const customers3 = ref([]);
const representatives = reactive([
  { name: 'Amy Elsner', image: 'amyelsner.png' },
  // ...
]);

function calculateCustomerTotal(name) {
  return customers3.value.filter(c => c.representative.name === name).length;
}

function getSeverity(status) {
  switch (status) {
    case 'unqualified': return 'danger';
    case 'qualified': return 'success';
    case 'new': return 'info';
    case 'negotiation': return 'warn';
    case 'renewal': return null;
  }
}

onBeforeMount(async () => {
  customers3.value = await CustomerService.getCustomersMedium();
});
</script>

<template>
  <div class="card">
    <div class="font-semibold text-xl mb-4">Grouping</div>
    <!-- DataTable 및 groupheader, groupfooter 구성 -->
  </div>
</template>
