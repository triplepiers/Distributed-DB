<template>
  <div id="drop">
    <div class="line">
      <div class="tip">DROP TABLE</div>
      <el-select v-model="tableName" placeholder="Select" size="large">
        <el-option
        v-for="(val, index) in removable" :key="index"
        :label="val" :value="val"/>
      </el-select>
    </div>
    <el-button id="btn" type="danger">
      删除表
    </el-button>
  </div>
</template>

<script>
export default {
    name: 'DropTable',
    computed: {
      removable() {
        // 仅{可写} table 才可被移除
        return this.$store.state.tables.filter(item => item.writable).map(item => item.name)
      }
    },
    data() {
      return {
        tableName: ''
      }
    }
}
</script>

<style scoped>
#drop {
  position: relative;
}

.line {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
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