<template>
  <div id="select">
    <div class="tip">
      Input SELECT SQL
    </div>
    <el-input style="margin: 10px 0;"
      v-model="sql" autosize
      type="textarea" placeholder="please input"/>
    <el-button type="primary">执行</el-button>

    <div class="res">
      <div class="tip">Result</div>
      <el-table :data="result.res" style="width: 100%">
        <el-table-column v-for="item in result.meta" :key="item.index"
         :prop="item" :label="item"/>
      </el-table>
    </div>
    
  </div>
</template>

<script>
export default {
    name: 'SELECT',
    data() {
      return {
        sql: '',
        result: {
          meta: [
            "username",
            "id",
            "password"
          ],
          res: [
            {
              username: "root",
              id: "1",
              password: "123456"
            }
          ]
        }
      }
    }
}
</script>

<style scoped>
.tip {
  font-size: 30px;
  font-weight: 600;
}

.res {
  width: 100%;
  margin-top: 20px;
}
</style>