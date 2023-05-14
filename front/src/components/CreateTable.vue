<template>
  <div id="create">
    <div class="line">
      <div class="tip">CREATE TABLE </div>
      <el-input v-model="tableName" placeholder="Table Name" 
       style="width: 200px;"/>
      <div class="tip"> (</div>
    </div>
    <div class="line">
      <el-input style="margin: 10px 0;"
      v-model="info" autosize
      type="textarea" placeholder="Columns Info"/>
      <div class="tip"> );</div>
    </div>
    <el-button id="btn" type="primary">
      执行
    </el-button>
  </div>
</template>

<script>
export default {
    name: 'CreateTable',
    data() {
      return {
        tableName: '',
        info: ''
      }
    }
}
</script>

<style scoped>
#create {
  position: relative;
}

.line {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 30px;
}
.line .tip {
  font-size: 30px;
  font-weight: 600;
}

#btn {
  position: absolute;
  margin-top: 10px;
  right: 5px;
}
</style>