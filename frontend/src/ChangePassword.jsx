import { useState } from 'react'

export default function ChangePassword({ authToken, onClose }) {
  const [oldPassword, setOldPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(false)

  const handleSubmit = (e) => {
    e.preventDefault()
    setError(null)
    setSuccess(false)
    fetch('/api/change-password', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authToken}`,
      },
      body: JSON.stringify({ oldPassword, newPassword }),
    })
      .then((res) => {
        if (!res.ok) throw new Error('Failed to change password')
        setSuccess(true)
        setOldPassword('')
        setNewPassword('')
      })
      .catch((err) => setError(err.message))
  }

  return (
    <div className="modal">
      <form onSubmit={handleSubmit} className="change-password-form">
        <h2>Change Password</h2>
        {error && <p className="error">{error}</p>}
        {success && <p className="success">Password updated</p>}
        <div>
          <label>
            Current Password
            <input
              type="password"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
            />
          </label>
        </div>
        <div>
          <label>
            New Password
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
          </label>
        </div>
        <div className="change-password-actions">
          <button type="submit">Update</button>
          <button type="button" onClick={onClose}>
            Close
          </button>
        </div>
      </form>
    </div>
  )
}

