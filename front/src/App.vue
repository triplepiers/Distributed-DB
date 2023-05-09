<template>
  <div id="app">
      <div class="master">
        <div class="line">
          <h1>Master 连接状态</h1>
          <Fragment>
            <div class="connect" v-show="master.status">CONNECT</div>
            <div class="disconnect" v-show="!master.status">DISCONNECT</div>
          </Fragment>
        </div>
        <div class="button" @click="handleClick">连接服务器</div>
      </div>

      <div class="body">
        <div class="button-wrap">
          <div class="button">新建表格</div>
        </div>
        <h2>可用 Table 列表</h2>
        <div class="table-list">
          <div class="table-info">
            <div class="table-name">TABLE userinfo</div>
            <div class="meta">
              <div class="key-info">
                <div class="key-name">username</div>
                <div class="key-type">varchar 50</div>
              </div>
              <div class="key-info">
                <div class="key-name">password</div>
                <div class="key-type">varchar 50</div>
              </div>
              <div class="key-info">
                <div class="key-name">nickName</div>
                <div class="key-type">varchar 30</div>
              </div>
            </div>
            <div class="options">
              <div class="button warn">删除</div>
              <div class="button">查询</div>
            </div>
          </div>
          <div class="table-info">
            <div class="table-name">Table 2</div>
            <div class="meta">
              <div class="key-info">
                <div class="key-name">字段名</div>
                <div class="key-type">字段信息</div>
              </div>
              <div class="key-info">
                <div class="key-name">字段名</div>
                <div class="key-type">字段信息</div>
              </div>
              <div class="key-info">
                <div class="key-name">字段名</div>
                <div class="key-type">字段信息</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 网页 bash -->
      <div id="CodeArea">
        <h2>使用 <a href="https://highlightjs.org/download/" target="_blank">hilightjs</a> 实现高亮，编辑您的代码：</h2>
        高亮样例：<pre><code class="language-sql">SELECT key FROM table_t;</code></pre>
        输入：<input id="TestCode" v-model="input" ref="child" @input="refresh" /><br/>
        高亮<div contenteditable="true" id="result"  @click="inputMessage" style=" font-family: 'Courier New', Courier, monospace; font-size: 14px; border: 1px solid #ccc; display: block;"></div>
      </div>
  </div>
</template>

<script>
import hljs from 'highlight.js'
// 指定的样式（可以再挑一下）
import "highlight.js/styles/xcode.css"

export default {
  name: 'App',
  mounted() {
    hljs.highlightAll();
  },
  data() {
    return {
      master: {
        status: false
      }
    }
  },
  methods: {
    handleClick() {
      let s = this.master.status
      this.master.status = !s
    },
    inputMessage() {
      let childMessage = this.$refs.child
      setTimeout(function () {   //因为vue页面使用jquery在DOM未渲染完成之前事件绑定不  上，所有需要延时（jquery不适合在vue页面中使用，但查了好多资料，没找到合适的vux获取input焦点的代码，就果断选择了这个）
        childMessage.focus()
      }, 1)
    },
    refresh() {
      //获取input内容处理之后给到div显示
      var highlightDiv = document.getElementById('result');
      // var inputTest = document.getElementById('inputTest')
      var str_sql = document.getElementById('TestCode').value;
      var format_sql = hljs.highlight(str_sql, {language: 'sql'}).value;
      console.log(format_sql)
      highlightDiv.innerHTML = "<pre><code class='sql'>" + format_sql + "</code></pre>";
    },
    created(){     
      //插入script
      const oScript = document.createElement('script');
      oScript.type = 'text/javascript';
      oScript.src = '//cdnjs.cloudflare.com/ajax/libs/highlight.js/11.5.1/highlight.min.js';
      document.body.appendChild(oScript);
      // this.refresh()
    }
  },
}
</script>

<style scoped>
/* SQL 语法高亮 */
/* @import 'https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.6.0/styles/arduino-light.min.css'; */

.title-wrap {
  display: flex;
  justify-content: center;
  gap: 10px;
}

#app {
  user-select: none;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
.master {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #333;
}
.master .line {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.connect, .disconnect {
  width: fit-content;
  padding: 5px 15px;
  border-radius: 5px;
  color: #fff;
  background-color: olivedrab;
}
.disconnect {
  background-color: #777;
}

.body {
  margin-top: 20px;
}

.button {
  flex: 0;
  cursor: pointer;
  width: max-content;
  border-radius: 5px;
  padding: 5px 15px;
  color: #fff;
  background-color: blue;
}
.button.warn {
  background-color: red;
}


.table-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.table-info {
  flex-shrink: 0;
  width: 40vw;
  padding: 10px 20px;
  min-width: 200px;
  max-width: 280px;
  box-shadow: 0 0 15px rgba(0,0,0,.3);
  border-radius: 10px;
}

.table-info .table-name {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 5px;
  border-bottom: 1px solid gainsboro;
}

.table-info .key-info {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

.table-info .key-info .key-type {
  color: blue;
}

.table-info .options {
  margin-top: 5px;
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.table-info .options .button {
  white-space: nowrap;
}

</style>
