<script setup>
const props = defineProps({
  title: { 
    type: String,
    default: '기본 테이블 폼'
  },
  formData: {
    type: Object,
    required: true
  },
  formSchema: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['formSearch']);

const searchItem = () => {
  emit('formSearch');
};
</script>
<template>
  <Fluid>
    <div class="card flex flex-col gap-4">
      <div class="flex justify-between items-center">
        <div class="font-semibold text-2xl">{{ title }}</div>
        <div class="flex items-center gap-2 flex-nowrap">
          <slot name="btn"></slot>
        </div>
      </div>
      <div class="grid grid-cols-2 gap-6">
        <div v-for="(element, idx) in formSchema" :key="idx" class="flex flex-col gap-4">
          <div class="grid grid-cols-12 gap-2">
            <label :for="'form-' + element.id" class="flex items-center justify-center col-span-12 mb-2 sm:col-span-3 sm:mb-0">{{ element.label }}</label>
            <div class="col-span-12 sm:col-span-9">
              <!-- 입력 : text, combobox, number(float), data, searchInput(공급) -->
              
              <!-- Text Input -->
              <InputText v-if="element.type === 'text'" :id="'form-' + element.id" type="text"
                v-model="formData[element.id]" :placeholder="element.placeholder || 'Enter text...'" />

              <InputGroup v-else-if="element.type === 'item-search'">
                <InputText v-model="formData[element.id]" :placeholder="element.placeholder || 'Enter item name...'" />
                <Button icon="pi pi-search" class="p-button-outlined" @click="searchItem" />
              </InputGroup>

              <!-- Textarea Input -->
              <!-- <Textarea v-else-if="element.type === 'textarea'" :id="'form-' + element.id"
                v-model="formData[element.id]" :placeholder="element.placeholder || ''" rows="3" /> -->
              
              <!-- Date Picker -->
              <DatePicker v-else-if="element.type === 'date'" :id="'form-' + element.id"
                v-model="formData[element.id]" :placeholder="element.placeholder || 'Enter date...'"
                dateFormat="yy-mm-dd" class="flex-1" :show-icon="true" :show-button-bar="true" />

              <!-- Select Input -->
              <Select v-else-if="element.type === 'select'" :id="'form-' + element.id" v-model="formData[element.id]" :options="element.options"
                optionLabel="name" :placeholder="element.placeholder || 'Select option...'" />

              <!-- Number Input -->
              <InputNumber v-else-if="element.type === 'number-float'" :id="'form-' + element.id" v-model="formData[element.id]" inputId="minmaxfraction"
                :placeholder="element.placeholder || 'Enter number...'" :fluid="true" :minFractionDigits="2" />
              <InputNumber v-else-if="element.type === 'number'" :id="'form-' + element.id" v-model="formData[element.id]" inputId="integeronly"
                :placeholder="element.placeholder || 'Enter number...'" :fluid="true" />
              
              <template v-else>
                <span v-if="element.data === 'number'" class="text-right">
                  {{ Number(formData[element.id]).toLocaleString() }}
                </span>
                <span v-else-if="element.data === 'date'" class="text-right">
                  {{ new Date(formData[element.id]).toLocaleDateString() }}
                </span>
                <span v-else class="text-left">
                  {{ formData[element.id] }}
                </span>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Fluid>
</template>
