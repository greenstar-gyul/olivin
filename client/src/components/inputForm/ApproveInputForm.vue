<script setup>
import { ref, computed, watch } from 'vue';
import DatePickerFromTo from './DatePickerFromTo.vue';

const props = defineProps({
  inputs: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['saveData', 'approve', 'reject']);

// 검색 조건을 담을 객체
const inputDatas = ref({});

// inputs 기반으로 기본값 초기화, props.inputs의 value 값을 반영
const initializeInputDatas = () => {
  const options = {};
  props.inputs.inputs.forEach(element => {
    if (element.type === 'dateRange') {
      options[element.name + 'From'] = '';
      options[element.name + 'To'] = '';
      return;
    }
    // props에서 전달된 value 값을 사용, 없으면 빈 문자열
    options[element.name] = element.value || '';
  });
  inputDatas.value = options;
  console.log('📝 InputForm - inputDatas 초기화:', inputDatas.value);
};

//  props.inputs가 변경될 때마다 inputDatas 업데이트
watch(() => props.inputs, (newInputs) => {
  console.log('👀 InputForm - props.inputs 변경 감지:', newInputs);
  
  if (newInputs && newInputs.inputs) {
    const options = {};
    newInputs.inputs.forEach(element => {
      if (element.type === 'dateRange') {
        options[element.name + 'From'] = '';
        options[element.name + 'To'] = '';
        return;
      }
      //  새로운 value 값을 반영
      options[element.name] = element.value || '';
    });
    inputDatas.value = options;
    console.log('📝 InputForm - inputDatas 업데이트:', inputDatas.value);
  }
}, { deep: true, immediate: true });

// 초기화
initializeInputDatas();

const resetInputDatas = () => {
  initializeInputDatas();
};

const confirm = () => {
  emit('saveData', inputDatas.value);
}

const approve = () => {
  console.log(' InputForm - approve 이벤트, 데이터:', inputDatas.value);
  emit('approve', inputDatas.value);
};

const reject = () => {
  console.log(' InputForm - reject 이벤트, 데이터:', inputDatas.value);
  emit('reject', inputDatas.value);
};
</script>

<template>
  <div class="card flex flex-col gap-4 mt-6 h-full">
    <!-- 테이블 상단 (타이틀만) -->
    <div class="grid grid-cols-1 gap-4 mb-4">
      <div>
        <div class="font-semibold text-2xl">{{ inputs.title }}</div>
      </div>
    </div>

    <!-- 입력 필드들 (2개씩 자동 배치) -->
    <div class="grid grid-cols-2 gap-4">
      <div v-for="(input, index) in inputs.inputs" :key="input.name || index" class="grid grid-cols-12 gap-2"
        :class="(input.type === 'dateRange' || input.type === 'textarea') ? 'col-span-2' : 'col-span-1'">

        <label :for="input.label" class="flex items-center col-span-12 mb-2 md:col-span-3 md:mb-0">{{ input.label }}</label>
        <div v-if="input.type !== 'textarea'" class="col-span-12 md:col-span-9 flex">
          
          <!-- Text Input -->
          <InputText v-if="input.type === 'text'" :id="'input-' + index" type="text"
          v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" 
          :readonly="input.readonly" class="flex-1" />
          
          <!-- Date Picker -->
          <DatePicker v-else-if="input.type === 'date'" :id="'input-' + index"
          v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Select date...'"
            dateFormat="yy-mm-dd" class="flex-1" :show-icon="true" :show-button-bar="true" 
            :readonly="input.readonly" />

          <!-- Date Picker From To -->
          <DatePickerFromTo v-else-if="input.type === 'dateRange'"
          v-model:fromValue="inputDatas[input.name + 'From']" v-model:toValue="inputDatas[input.name + 'To']"
          :fromPlaceholder="input.fromPlaceholder" :toPlaceholder="input.toPlaceholder" class="flex-1" />
          
          <!-- Number Input -->
          <InputNumber v-else-if="input.type === 'number'" :id="'input-' + index"
          v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter number...'" 
          :readonly="input.readonly" class="flex-1" />

          <!-- Select Input -->
          <Select v-else-if="input.type === 'select'" :id="'input-' + index" v-model="inputDatas[input.name]" :options="input.options"
            optionLabel="name" optionValue="value" :placeholder="input.placeholder || 'Select option...'" 
            :disabled="input.readonly" class="flex-1" />
          
          <!-- Default fallback to text input -->
          <InputText v-else :id="'input-' + index" type="text" v-model="inputDatas[input.name]"
          :placeholder="input.placeholder || 'Enter text...'" :readonly="input.readonly" class="flex-1" />
        </div>
        <div v-else class="col-span-12 md:col-span-12 flex">
          <!-- Textarea Input -->
          <Textarea v-if="input.type === 'textarea'" :id="'input-' + index"
          v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" 
          :readonly="input.readonly" class="flex-1" rows="3" />
        </div>
      </div>
    </div>
    
    <!-- 승인/반려 버튼 -->
    <div class="mt-6 flex gap-2">
      <Button label="승인" severity="success" @click="approve" class="flex-1" />
      <Button label="반려" severity="danger" @click="reject" class="flex-1" />
    </div>

  </div>
</template>