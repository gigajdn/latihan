<template>
  <div class="app-container">
    <el-form :model="form" ref="form" label-width="100px" v-loading="formLoading">
      <el-form-item label="年级：" required>
        <el-select v-model="form.level" placeholder="年级">
          <el-option v-for="item in levelEnum" :key="item.key" :value="item.key" :label="item.value"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="学科：" required>
        <el-select v-model="form.subjectId" placeholder="学科" >
          <el-option v-for="item in subjects.filter(data => data.level==form.level)" :key="item.id" :value="item.id" :label="item.name+' ( '+item.levelName+' )'"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="试卷类型：" required>
        <el-select v-model="form.paperType" placeholder="试卷类型" >
          <el-option v-for="item in paperTypeEnum" :key="item.key" :value="item.key" :label="item.value"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="时间限制：" required v-show="form.paperType===4">
        <el-date-picker v-model="form.limitDateTime" value-format="yyyy-MM-dd HH:mm:ss" type="datetimerange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="试卷名称：" required>
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item :key="index" :label="'标题'+(index+1)+'：'" required v-for="(titleItem,index) in form.titleItems">
        <el-input v-model="titleItem.name" style="width: 80%"/>
        <el-button type="text" class="link-left" style="margin-left: 20px" size="mini" @click="addQuestion(titleItem)">添加题目</el-button>
        <el-button type="text" class="link-left" size="mini" @click="form.titleItems.splice(index,1)">删除</el-button>
        <el-card class="exampaper-item-box" v-if="titleItem.questionItems.length!==0">
          <el-form-item  :key="questionIndex" :label="'题目'+(questionIndex+1)+'：'"   v-for="(questionItem,questionIndex) in titleItem.questionItems">
            <el-row>
              <el-col  :span="23">
                <QuestionShow :qType="questionItem.questionType" :question="questionItem" />
            </el-col>
              <el-col  :span="1">
                <el-button type="text" size="mini" @click="titleItem.questionItems.splice(questionIndex,1)">删除</el-button>
              </el-col>
            </el-row>
          </el-form-item>
        </el-card>
      </el-form-item>
      <el-form-item label="建议时长：" required>
        <el-input v-model="form.suggestTime"  placeholder="分钟"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
        <el-button type="success" @click="addTitle">添加标题</el-button>
      </el-form-item>
    </el-form>
    <el-dialog :visible.sync="questionPage.showDialog">
      <el-form :model="questionPage.queryParam" ref="queryForm" :inline="true">
        <el-form-item label="ID：">
          <el-input v-model="questionPage.queryParam.id" style="width: 140px;" clearable></el-input>
        </el-form-item>
        <el-form-item label="题型：">
          <el-select v-model="questionPage.queryParam.questionType" clearable>
            <el-option v-for="item in questionType" :key="item.key" :value="item.key" :label="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item >
          <el-button type="primary" @click="queryForm">查询</el-button>
          <el-button type="primary" @click="confirmQuestionSelect">确定</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="questionPage.listLoading" :data="questionPage.tableData"  @selection-change="handleSelectionChange" border fit highlight-current-row style="width: 100%">
        <el-table-column type="selection" width="35"></el-table-column>
        <el-table-column prop="id" label="Id" width="60px"/>
        <el-table-column prop="questionType" label="题型" :formatter="questionTypeFormatter" width="70px"/>
        <el-table-column prop="shortTitle" label="题干" show-overflow-tooltip/>
      </el-table>
      <pagination v-show="questionPage.total>0" :total="questionPage.total" :page.sync="questionPage.queryParam.pageIndex" :limit.sync="questionPage.queryParam.pageSize"
                  @pagination="search"/>
    </el-dialog>
  </div>
</template>

<script>

import { mapGetters, mapState, mapActions } from 'vuex'
import Pagination from '@/components/Pagination'
import QuestionShow from '../question/components/Show'
import examPaperApi from '@/api/examPaper'
import questionApi from '@/api/question'

export default {
  components: { Pagination, QuestionShow },
  data () {
    return {
      form: {
        id: null,
        level: 1,
        subjectId: 1,
        paperType: 1,
        limitDateTime: [],
        name: '',
        suggestTime: null,
        titleItems: []
      },
      formLoading: false,
      questionPage: {
        multipleSelection: [],
        showDialog: false,
        queryParam: {
          id: null,
          questionType: null,
          subjectId: 1,
          pageIndex: 1,
          pageSize: 5
        },
        listLoading: true,
        tableData: [],
        total: 0
      },
      currentTitleItem: null
    }
  },
  created () {
    this.initSubject()
    let id = this.$route.query.id
    let _this = this
    if (id && parseInt(id) !== 0) {
      _this.formLoading = true
      examPaperApi.select(id).then(re => {
        _this.form = re.response
        _this.formLoading = false
      })
    }
  },
  methods: {
    submitForm () {
      let _this = this
      this.formLoading = true
      examPaperApi.edit(this.form).then(re => {
        if (re.code === 1) {
          _this.form = re.response
          _this.$message.success(re.message)
        } else {
          _this.$message.error(re.message)
        }
        this.formLoading = false
      }).catch(e => {
        this.formLoading = false
      })
    },
    addTitle () {
      this.form.titleItems.push({
        name: '',
        questionItems: []
      })
    },
    addQuestion (titleItem) {
      this.currentTitleItem = titleItem
      this.questionPage.showDialog = true
      this.search()
    },
    removeTitleItem (titleItem) {
      this.form.titleItems.remove(titleItem)
    },
    removeQuestion (titleItem, questionItem) {
      titleItem.questionItems.remove(questionItem)
    },
    queryForm () {
      this.questionPage.queryParam.pageIndex = 1
      this.search()
    },
    confirmQuestionSelect () {
      let _this = this
      this.questionPage.multipleSelection.forEach(q => {
        questionApi.select(q.id).then(re => {
          _this.currentTitleItem.questionItems.push(re.response)
        })
      })
      this.questionPage.showDialog = false
    },
    search () {
      this.questionPage.queryParam.subjectId = this.form.subjectId
      this.questionPage.listLoading = true
      questionApi.pageList(this.questionPage.queryParam).then(data => {
        const re = data.response
        this.questionPage.tableData = re.list
        this.questionPage.total = re.total
        this.questionPage.queryParam.pageIndex = re.pageNum
        this.questionPage.listLoading = false
      })
    },
    handleSelectionChange (val) {
      this.questionPage.multipleSelection = val
    },
    questionTypeFormatter (row, column, cellValue, index) {
      return this.enumFormat(this.questionTypeEnum, cellValue)
    },
    subjectFormatter (row, column, cellValue, index) {
      return this.subjectEnumFormat(cellValue)
    },
    resetForm () {
      this.$refs['form'].resetFields()
    },
    ...mapActions('exam', { initSubject: 'initSubject' })
  },
  computed: {
    ...mapGetters('enumItem', ['enumFormat']),
    ...mapState('enumItem', {
      questionTypeEnum: state => state.exam.question.typeEnum,
      paperTypeEnum: state => state.exam.examPaper.paperTypeEnum,
      levelEnum: state => state.user.levelEnum
    }),
    ...mapState('exam', { subjects: state => state.subjects })
  }
}
</script>
