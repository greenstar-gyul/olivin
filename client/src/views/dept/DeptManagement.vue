<script setup>
import DeptInput from './DeptInput.vue';
import FileUpload from 'primevue/fileupload';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import { ref, onMounted, nextTick, computed } from 'vue';
import { useToast } from 'primevue/usetoast';
import axios from '@/service/axios';


const fileUploadRef = ref();



</script>

<template>
  <DeptInput
    :filters="filters"
    :items="items"
    :header="header"
    :inputs="inputs"
    :checkType="'multiple'"
    @searchData="searchData"
    @saveData="saveData"
  />
</template>