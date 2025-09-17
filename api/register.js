const { setCors, readJson, sendJson, sendStatus, methodNotAllowed, handleError } = require('./_lib/http')
const { issueToken } = require('./_lib/auth')
const { registerUser } = require('./_lib/data')

module.exports = async (req, res) => {
  setCors(res)
  if (req.method === 'OPTIONS') {
    res.setHeader('Allow', 'POST, OPTIONS')
    sendStatus(res, 204)
    return
  }
  if (req.method !== 'POST') {
    methodNotAllowed(res, ['POST', 'OPTIONS'])
    return
  }
  try {
    const body = await readJson(req)
    const username = typeof body.username === 'string' ? body.username.trim() : ''
    const password = typeof body.password === 'string' ? body.password : ''
    if (!username || !password) {
      sendStatus(res, 400, 'Username and password are required')
      return
    }
    const created = await registerUser(username, password)
    if (!created) {
      sendStatus(res, 409, 'User already exists')
      return
    }
    const token = issueToken(username)
    sendJson(res, 201, { token })
  } catch (err) {
    handleError(res, err)
  }
}
