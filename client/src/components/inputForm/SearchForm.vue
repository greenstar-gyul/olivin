<script setup>
import { ref } from 'vue';

const props = defineProps({
    filters: {
      type: Array,
      required: true
    }
})

</script>
<template>
  <div class="p-6 bg-gray-50 shadow-md rounded-md space-y-6">
    <div v-for="(filter, index) in filters" :key="index">
      <label :for="'filter-' + index">{{ filter.label }}</label>
      <input
        :id="'filter-' + index"
        type="text"
        v-model="filter.value"
        :placeholder="filter.placeholder"
      />
    </div>
  </div>

</template>