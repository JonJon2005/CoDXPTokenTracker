const crypto = require('crypto')
const jwt = require('jsonwebtoken')
const { sendStatus } = require('./http')

const SECRET = (() => {
  if (!global.__CODXP_SECRET) {
    global.__CODXP_SECRET = crypto.randomBytes(32)
  }
  return global.__CODXP_SECRET
})()

function issueToken(username) {
  return jwt.sign({ sub: username }, SECRET, { expiresIn: '1h' })
}

function verifyToken(token) {
  try {
    const decoded = jwt.verify(token, SECRET)
    return decoded && decoded.sub
  } catch (err) {
    return null
  }
}

function requireUser(req, res) {
  const auth = req.headers['authorization']
  if (!auth || typeof auth !== 'string' || !auth.startsWith('Bearer ')) {
    sendStatus(res, 401, 'Unauthorized')
    return null
  }
  const token = auth.slice('Bearer '.length)
  const username = verifyToken(token)
  if (!username) {
    sendStatus(res, 401, 'Unauthorized')
    return null
  }
  return username
}

module.exports = {
  issueToken,
  verifyToken,
  requireUser,
}
