import { post } from '@/utils/request'

export default {
  pageList: query => post('/api/admin/exam/paper/page', query),
  edit: query => post('/api/admin/exam/paper/edit', query),
  select: id => post('/api/admin/exam/paper/select/' + id)
}
