<script setup>
import { onMounted, ref } from 'vue';

const emit = defineEmits(['confirm', 'close']);
const props = defineProps({
  display: {
    type: Boolean,
    default: false
  },
  selectionMode: {
    type: String,
    default: 'single'
  },
  items: {
    type: Array,
    default: () => []
  },
  headers: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: 'Dialog Modal'
  }
});

const selectedItems = ref(null);

const initializeSelectedProduct = () => {
  if (props.selectionMode === 'single') {
    selectedItems.value = null;
  } else {
    selectedItems.value = [];
  }
};

const close = () => {
  emit('close');
  initializeSelectedProduct();
}

const confirm = () => {
  emit('confirm', selectedItems.value);
  initializeSelectedProduct();
}

const onProductSelect = (event) => {
  if (props.selectionMode === 'single') {
    selectedItems.value = event.data;
  } else {
    if (!selectedItems.value) {
      selectedItems.value = [];
    }
    if (!selectedItems.value.includes(event.data)) {
      selectedItems.value.push(event.data);
    }
  }
};

onMounted(() => {
  initializeSelectedProduct();
});

</script>
<template>
  <Dialog :header="props.title" v-model:visible="props.display" :breakpoints="{ '960px': '75vw' }"
    :style="{ width: '30vw' }" :modal="true" :closable="false">
    <div class="flex flex-col gap-4">

      <DataTable v-model:selection="selectedItems" :value="props.items" :selectionMode="props.selectionMode"
        :paginator="false" :rows="5" @row-select="onProductSelect">
        <Column v-for="col in props.headers" :key="col.field" :field="col.field" :header="col.header">
          <template #body="slotProps">
            {{ slotProps.data[col.field] }}
          </template>
        </Column>
      </DataTable>

      <div class="flex justify-center gap-2 mt-4">
        <Button label="취소" severity="secondary" @click="close" />
        <Button label="확인" @click="confirm" />
      </div>
    </div>
  </Dialog>
</template>
<style></style>