<template>
  <div id="else">
    <div class="tip">
      Input SQL
    </div>
    <el-input style="margin: 10px 0;"
      v-model="sql" autosize
      type="textarea" placeholder="please input"/>
    <el-button type="primary">执行</el-button>
  </div>
</template>

<script>
export default {
    name: 'ElseChoice',
    data() {
      return {
        sql: ''
      }
    }
}
</script>

<style>
.tip {
  font-size: 30px;
  font-weight: 600;
}
</style>