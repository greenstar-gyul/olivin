<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import InputNumber from 'primevue/inputnumber';
import Textarea from 'primevue/textarea';
import DatePicker from 'primevue/datepicker';
import Select from 'primevue/select';
import InputGroup from 'primevue/inputgroup';

const props = defineProps({
  inputs: {
    type: Object,
    required: true
  },
  init: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['saveData', 'openSearchModal']);

// 입력 데이터를 담을 객체
const inputDatas = ref({});
const currentMode = ref('insert'); // insert, update

console.log('🏗️ DeptEmpInputForm 컴포넌트 초기화');

// ✅ 초기화 함수 개선
const initializeInputDatas = () => {
  console.log('🔄 DeptEmpInputForm - 데이터 초기화');
  
  const options = {};
  if (props.inputs && props.inputs.inputs) {
    props.inputs.inputs.forEach(element => {
      if (element.type === 'dateRange') {
        options[element.name + 'From'] = '';
        options[element.name + 'To'] = '';
        return;
      }
      // props에서 전달된 value 값을 사용
      options[element.name] = element.value || '';
    });
  }
  
  inputDatas.value = options;
  currentMode.value = 'insert';
  console.log('✅ DeptEmpInputForm - 초기화 완료:', inputDatas.value);
};

// ✅ props.init이 변경될 때 업데이트 (선택된 행 데이터)
watch(() => props.init, (newInit) => {
  if (newInit && Object.keys(newInit).length > 0) {
    console.log('📥 DeptEmpInputForm - 선택된 데이터:', newInit);
    
    const options = {};
    if (props.inputs && props.inputs.inputs) {
      props.inputs.inputs.forEach(element => {
        if (element.type === 'dateRange') {
          options[element.name + 'From'] = '';
          options[element.name + 'To'] = '';
          return;
        }
        // 선택된 데이터에서 값을 가져오되, 없으면 빈 문자열
        options[element.name] = newInit[element.name] || '';
      });
    }
    
    inputDatas.value = options;
    currentMode.value = 'update';
    console.log('🔄 DeptEmpInputForm - 선택된 데이터로 업데이트됨:', inputDatas.value);
  } else {
    console.log('📝 DeptEmpInputForm - 선택 해제됨');
    initializeInputDatas();
  }
}, { deep: true, immediate: true });

// ✅ props.inputs가 변경될 때만 업데이트 (무한루프 방지)
watch(() => props.inputs, (newInputs) => {
  if (newInputs && newInputs.inputs && !props.init) {
    console.log('📥 DeptEmpInputForm - props.inputs 변경됨:', newInputs);
    initializeInputDatas();
  }
}, { deep: true, immediate: true });

const resetInputDatas = () => {
  console.log('🔄 DeptEmpInputForm - 수동 초기화');
  initializeInputDatas();
};

const saveData = () => {
  console.log(`💾 DeptEmpInputForm - ${currentMode.value} 요청:`, inputDatas.value);
  emit('saveData', { ...inputDatas.value }, currentMode.value);
};

const openSearchModal = (inputName) => {
  console.log('🔍 DeptEmpInputForm - 검색 모달 요청:', inputName);
  emit('openSearchModal', inputName);
};

// 외부에서 접근할 수 있도록 노출
defineExpose({
  inputDatas,
  resetInputDatas,
  currentMode
});
</script>

<template>
  <div class="card flex flex-col gap-4 mt-6 h-full">
    <!-- 테이블 상단 (타이틀 + 버튼) -->
    <div class="grid grid-cols-1 gap-4 mb-4">
      <div class="flex justify-between">
        <div>
          <div class="font-semibold text-2xl">
            {{ inputs.title || '사원 정보' }}
            <span class="text-sm text-gray-500 ml-2">
              ({{ currentMode === 'insert' ? '등록' : '수정' }} 모드)
            </span>
          </div>
        </div>
        <div class="flex items-center gap-2 flex-nowrap">
          <Button 
            label="초기화" 
            severity="secondary" 
            @click="resetInputDatas" 
            outlined 
          />
          <Button 
            :label="currentMode === 'insert' ? '등록' : '수정'" 
            @click="saveData" 
            outlined 
          />
        </div>
      </div>
    </div>

    <!-- 입력 필드들 (세로 배치) -->
    <div class="grid grid-cols-1 gap-4" v-if="inputs && inputs.inputs && inputs.inputs.length > 0">
      <div v-for="(input, index) in inputs.inputs" :key="input.name || index" class="grid grid-cols-12 gap-2">

        <label :for="input.label" class="flex items-center col-span-12 mb-2 md:col-span-3 md:mb-0">
          {{ input.label }}
          <span v-if="input.required" class="text-red-500 ml-1">*</span>
        </label>
        
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
          
          <!-- Number Input -->
          <InputNumber v-else-if="input.type === 'number'" :id="'input-' + index"
          v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter number...'" 
          :readonly="input.readonly" class="flex-1" />

          <!-- Select Input -->
          <Select v-else-if="input.type === 'select'" :id="'input-' + index" v-model="inputDatas[input.name]"
            :options="input.options" optionLabel="name" optionValue="value"
            :placeholder="input.placeholder || 'Select option...'" 
            :disabled="input.readonly" class="flex-1" />

          <!-- Item Search -->
          <InputGroup v-else-if="input.type === 'item-search'" class="flex-1">
            <InputText :id="'input-' + index" v-model="inputDatas[input.name]" 
              :placeholder="input.placeholder || 'Enter item name...'" 
              :readonly="input.readonly" />
            <Button icon="pi pi-search" class="p-button-outlined" 
              @click="openSearchModal(input.name)" 
              :disabled="input.readonly" />
          </InputGroup>
          
          <!-- Default fallback to text input -->
          <InputText v-else :id="'input-' + index" type="text" v-model="inputDatas[input.name]"
          :placeholder="input.placeholder || 'Enter text...'" :readonly="input.readonly" class="flex-1" />
        </div>
        
        <div v-else class="col-span-12 md:col-span-9 flex">
          <!-- Textarea Input -->
          <Textarea v-if="input.type === 'textarea'" :id="'input-' + index"
          v-model="inputDatas[input.name]" :placeholder="input.placeholder || 'Enter text...'" 
          :readonly="input.readonly" class="flex-1" rows="3" />
        </div>
      </div>
    </div>
    
    <!-- ✅ inputs가 없을 때를 위한 대체 메시지 -->
    <div v-else class="text-center text-gray-500 py-8">
      사원을 선택하면 정보가 표시됩니다.
    </div>
    
  </div>
</template>