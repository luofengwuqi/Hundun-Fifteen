const STATUS_MAP = {
  0: { label: '通知关闭', class: 'off' },
  1: { label: '通知开启', class: 'on' },
  2: { label: '已完成', class: 'done' }
}

export function getStatusMeta(status) {
  return STATUS_MAP[status] ?? { label: '未知', class: 'off' }
}
