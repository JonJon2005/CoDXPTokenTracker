const { STATUS_CODES } = require('http')

function setCors(res) {
  res.setHeader('Access-Control-Allow-Origin', '*')
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization')
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,OPTIONS')
}

async function readJson(req) {
  const chunks = []
  for await (const chunk of req) {
    chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk))
  }
  const raw = Buffer.concat(chunks).toString('utf8').trim()
  if (!raw) return {}
  try {
    return JSON.parse(raw)
  } catch (err) {
    const error = new Error('Invalid JSON body')
    error.statusCode = 400
    throw error
  }
}

function sendJson(res, statusCode, payload) {
  res.statusCode = statusCode
  res.setHeader('Content-Type', 'application/json; charset=utf-8')
  res.end(JSON.stringify(payload))
}

function sendStatus(res, statusCode, message) {
  res.statusCode = statusCode
  if (message) {
    res.setHeader('Content-Type', 'application/json; charset=utf-8')
    res.end(JSON.stringify({ error: message }))
  } else {
    res.end()
  }
}

function methodNotAllowed(res, allowed) {
  res.setHeader('Allow', allowed.join(', '))
  sendStatus(res, 405, 'Method Not Allowed')
}

function handleError(res, err) {
  if (err && typeof err.statusCode === 'number') {
    sendStatus(res, err.statusCode, err.message)
  } else {
    console.error(err)
    sendStatus(res, 500, STATUS_CODES[500])
  }
}

module.exports = {
  setCors,
  readJson,
  sendJson,
  sendStatus,
  methodNotAllowed,
  handleError,
}
