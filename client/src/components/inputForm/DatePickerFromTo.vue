<!-- DatePicker를 2개 써서 날짜 범위 입력받게 하기 -->
<script setup>
import { watch } from 'vue';

const fromValue = defineModel('fromValue');
const toValue = defineModel('toValue');

const props = defineProps({
  fromPlaceholder: {
    type: String,
    default: ''
  },
  toPlaceholder: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  }
});

watch(() => props.fromValue, (newValue) => {
  if (newValue && toValue.value && newValue > toValue.value) {
    let tmpValue = toValue.value;
    toValue.value = newValue; // fromValue가 toValue보다 크면 toValue를 fromValue로 설정
    fromValue.value = tmpValue;
  }
});

watch(() => toValue.value, (newValue) => {
  if (newValue && fromValue.value && newValue < fromValue.value) {
    let tmpValue = fromValue.value;
    fromValue.value = newValue; // toValue가 fromValue보다 작으면 fromValue를 toValue로 설정
    toValue.value = tmpValue;
  }
});

</script>
<template>
<DatePicker :showIcon="true" :showButtonBar="true" v-model="fromValue" dateFormat="yy-mm-dd" :placeholder="props.fromPlaceholder" :disabled="props.disabled" class="flex-1"></DatePicker>
<span> ~ </span>
<DatePicker :showIcon="true" :showButtonBar="true" v-model="toValue" dateFormat="yy-mm-dd" :placeholder="props.toPlaceholder" :disabled="props.disabled" class="flex-1"></DatePicker>
</template>
<style>

</style>