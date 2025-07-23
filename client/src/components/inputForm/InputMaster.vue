<script setup>
import InputText from 'primevue/inputtext';
import { ref } from 'vue';

const props = defineProps({
  masterInputs: {
    type: Array,
    required: true
  }
});

const inputValues = ref({});

const initializeInputValues = () => {
  const values = {};
  props.masterInputs.inputs.forEach(input => {
    values[input.name] = '';
  });
  inputValues.value = values;
}

initializeInputValues();

const resetInputValues = () => {
  initializeInputValues();
  console.log('Input values reset to initial state:', inputValues.value);
}

</script>
<template>
  <div class="card flex flex-col gap-4">
    <!-- 테이블 상단 (타이틀 + 엑셀 다운로드 버튼) -->
    <div class="grid grid-cols-1 gap-4 mb-4">
      <div class="flex justify-between">
        <div>
          <div class="font-semibold text-2xl">{{ props.masterInputs.title }}</div>
        </div>
        <div class="text-right mt-6">
          <div class="inline-flex gap-3">
            <Button label="초기화" severity="secondary" @click="resetInputValues" class="whitespace-nowrap" />
            <Button label="저장" class="whitespace-nowrap" />
          </div>
        </div>
      </div>
    </div>
    <div class="grid grid-cols-2 gap-6">
      <div v-for="(input, index) in props.masterInputs.inputs" :key="index" class="flex items-center gap-2">
        <span class="text-lg font-medium text-gray-700 whitespace-nowrap w-28 flex-shrink-0 text-center">{{
          input.label }}</span>
        <!-- Text Input -->
        <InputText v-if="input.type === 'text'" :id="'input-' + index" type="text"
          v-model="inputValues[input.name]" :placeholder="input.placeholder || 'Enter text...'" class="flex-1" />

        <!-- Textarea Input -->
        <Textarea v-else-if="input.type === 'textarea'" :id="'input-' + index"
          v-model="inputValues[input.name]" :placeholder="input.placeholder || 'Enter text...'" class="flex-1"
          rows="3" />

        <!-- Date Picker -->
        <DatePicker v-else-if="input.type === 'date'" :id="'input-' + index"
          v-model="inputValues[input.name]" :placeholder="input.placeholder || 'Select date...'" dateFormat="yy-mm-dd"
          class="flex-1" :show-icon="true" :show-button-bar="true" />

        <!-- Number Input -->
        <InputNumber v-else-if="input.type === 'number'" :id="'input-' + index"
          v-model="inputValues[input.name]" :placeholder="input.placeholder || 'Enter number...'" class="flex-1" />

        <!-- Default fallback to text input -->
        <InputText v-else :id="'input-' + index" type="text" v-model="inputValues[input.name]"
          :placeholder="input.placeholder || 'Enter text...'" class="flex-1" />
      </div>
    </div>
    
  </div>
</template>
<style scoped></style>