export function formatDateTime(value) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function formatRange(begin, end) {
  if (!begin && !end) return '—'
  return `${formatDateTime(begin)} → ${formatDateTime(end)}`
}

/** 与后端 @JsonFormat 一致：yyyy-MM-dd HH:mm */
export function toApiDateTime(value) {
  if (!value) return value
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function nowApiDateTime() {
  return toApiDateTime(new Date())
}
