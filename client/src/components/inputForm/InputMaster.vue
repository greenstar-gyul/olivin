<script setup>
import { convertDate } from '@/utils/dateUtils';

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

const searchItem = (item, fieldName) => {
  emit('formSearch', item, fieldName);
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
              
              <!--
              선택옵션
               text
               item-search
               date
               select
               number-float
               -->
              <!-- Text Input -->
              <InputText v-if="element.type === 'text'" :id="'form-' + element.id" type="text"
                v-model="formData[element.id]" :placeholder="element.placeholder || 'Enter text...'" />

              <InputGroup v-else-if="element.type === 'item-search'">
                <InputText v-model="formData[element.id]" :placeholder="element.placeholder || 'Enter item name...'" />
                <Button icon="pi pi-search" class="p-button-outlined" @click="searchItem(formData, element.id)" />
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
                <InputText v-if="element.data === 'number'" 
                  class="text-left" 
                  :value="Number(formData[element.id]).toLocaleString()" 
                  variant="filled"
                  readonly
                />
                <InputText v-else-if="element.data === 'date'"
                  class="text-left" 
                  :value="convertDate(formData[element.id])"
                  variant="filled"
                  readonly
                />
                <InputText v-else
                  class="text-left" 
                  :value="formData[element.id]"
                  variant="filled"
                  readonly
                />
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Fluid>
</template>
