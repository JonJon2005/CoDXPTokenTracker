const { setCors, readJson, sendJson, sendStatus, methodNotAllowed, handleError } = require('./_lib/http')
const { requireUser } = require('./_lib/auth')
const { getTokens, setTokens } = require('./_lib/data')

module.exports = async (req, res) => {
  setCors(res)
  if (req.method === 'OPTIONS') {
    res.setHeader('Allow', 'GET, PUT, OPTIONS')
    sendStatus(res, 204)
    return
  }
  if (req.method !== 'GET' && req.method !== 'PUT') {
    methodNotAllowed(res, ['GET', 'PUT', 'OPTIONS'])
    return
  }
  const username = requireUser(req, res)
  if (!username) return
  try {
    if (req.method === 'GET') {
      const tokens = await getTokens(username)
      if (!tokens) {
        sendStatus(res, 404, 'User not found')
        return
      }
      sendJson(res, 200, tokens)
      return
    }
    const body = await readJson(req)
    if (!body || typeof body !== 'object') {
      sendStatus(res, 400, 'Invalid payload')
      return
    }
    const ok = await setTokens(username, body)
    if (!ok) {
      sendStatus(res, 404, 'User not found')
      return
    }
    sendStatus(res, 204)
  } catch (err) {
    handleError(res, err)
  }
}
