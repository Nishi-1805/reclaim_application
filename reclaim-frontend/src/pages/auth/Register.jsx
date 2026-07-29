import { useState } from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Alert from '@mui/material/Alert';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const initialForm = { fullName: '', email: '', phoneNumber: '', password: '', confirmPassword: '' };

export default function Register() {
  const { register, loading } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState('');

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    try {
      const { confirmPassword, ...payload } = form;
      await register(payload);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setError(err.message || 'Registration failed. Please check your details.');
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        px: 2,
        py: 4,
      }}
    >
      <Paper elevation={0} sx={{ p: 4, width: '100%', maxWidth: 460, borderRadius: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, mb: 3 }}>
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: 2,
              bgcolor: 'primary.main',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontWeight: 800,
            }}
          >
            R
          </Box>
          <Box>
            <Typography variant="h6" fontWeight={800} lineHeight={1.1}>
              Reclaim
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Lost. Found. Reunited.
            </Typography>
          </Box>
        </Box>

        <Typography variant="h5" fontWeight={700} sx={{ mb: 0.5 }}>
          Create your account
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Start reporting and tracking lost or found items.
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField label="Full name" name="fullName" value={form.fullName} onChange={handleChange} required fullWidth />
          <TextField label="Email" name="email" type="email" value={form.email} onChange={handleChange} required fullWidth />
          <TextField
            label="Phone number"
            name="phoneNumber"
            value={form.phoneNumber}
            onChange={handleChange}
            required
            fullWidth
            helperText="10-digit Indian mobile number"
          />
          <TextField
            label="Password"
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            required
            fullWidth
            helperText="8+ chars, upper & lower case, a digit and a special character"
          />
          <TextField
            label="Confirm password"
            name="confirmPassword"
            type="password"
            value={form.confirmPassword}
            onChange={handleChange}
            required
            fullWidth
          />
          <Button type="submit" variant="contained" size="large" disabled={loading} fullWidth>
            {loading ? 'Creating account…' : 'Create Account'}
          </Button>
        </Box>

        <Typography variant="body2" color="text.secondary" sx={{ mt: 3, textAlign: 'center' }}>
          Already have an account?{' '}
          <RouterLink to="/login" style={{ color: 'inherit', fontWeight: 600 }}>
            Log in
          </RouterLink>
        </Typography>
      </Paper>
    </Box>
  );
}
