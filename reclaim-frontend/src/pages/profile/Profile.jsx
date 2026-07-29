import { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Avatar from '@mui/material/Avatar';
import Stack from '@mui/material/Stack';
import Divider from '@mui/material/Divider';
import Alert from '@mui/material/Alert';
import Snackbar from '@mui/material/Snackbar';
import Skeleton from '@mui/material/Skeleton';
import Chip from '@mui/material/Chip';

import { useAuth } from '../../context/AuthContext';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import {
    getMyProfile,
    updateProfile,
    changePassword,
    deactivateMyAccount,
} from '../../api/users';

function initials(name = '') {
    return name.split(' ').map((p) => p[0]).slice(0, 2).join('').toUpperCase();
}

export default function Profile() {
    const { logout } = useAuth();

    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [profileForm, setProfileForm] = useState({ fullName: '', phoneNumber: '' });
    const [profileSaving, setProfileSaving] = useState(false);
    const [profileError, setProfileError] = useState('');

    const [passwordForm, setPasswordForm] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
    });
    const [passwordSaving, setPasswordSaving] = useState(false);
    const [passwordError, setPasswordError] = useState('');

    const [deactivateOpen, setDeactivateOpen] = useState(false);
    const [deactivating, setDeactivating] = useState(false);
    const [toast, setToast] = useState('');

    useEffect(() => {
        let cancelled = false;
        (async () => {
            setLoading(true);
            try {
                const data = await getMyProfile();
                if (cancelled) return;
                setProfile(data);
                setProfileForm({ fullName: data.fullName, phoneNumber: data.phoneNumber || '' });
            } catch (err) {
                if (!cancelled) setToast(err.message || 'Failed to load profile.');
            } finally {
                if (!cancelled) setLoading(false);
            }
        })();
        return () => {
            cancelled = true;
        };
    }, []);

    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        setProfileSaving(true);
        setProfileError('');
        try {
            const updated = await updateProfile(profileForm);
            setProfile(updated);
            setToast('Profile updated successfully.');
        } catch (err) {
            setProfileError(err.message || 'Failed to update profile.');
        } finally {
            setProfileSaving(false);
        }
    };

    const handlePasswordSubmit = async (e) => {
        e.preventDefault();
        setPasswordSaving(true);
        setPasswordError('');
        try {
            await changePassword(passwordForm);
            setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
            setToast('Password changed successfully.');
        } catch (err) {
            setPasswordError(err.message || 'Failed to change password.');
        } finally {
            setPasswordSaving(false);
        }
    };

    const handleDeactivate = async () => {
        setDeactivating(true);
        try {
            await deactivateMyAccount();
            logout();
            window.location.href = '/login';
        } catch (err) {
            setToast(err.message || 'Failed to deactivate account.');
            setDeactivating(false);
        }
    };

    if (loading) {
        return (
            <Box sx={{ pt: 1 }}>
                <Skeleton variant="rounded" height={300} />
            </Box>
        );
    }

    return (
        <Box sx={{ pt: 1 }}>
            <Typography variant="h5" fontWeight={800} sx={{ mb: 3 }}>
                Profile
            </Typography>

            <Grid container spacing={3}>
                <Grid item xs={12} md={4}>
                    <Paper elevation={0} sx={{ p: 3, borderRadius: 3, textAlign: 'center' }}>
                        <Avatar sx={{ width: 88, height: 88, mx: 'auto', mb: 2, bgcolor: 'primary.main', fontSize: 28 }}>
                            {initials(profile?.fullName)}
                        </Avatar>
                        <Typography variant="h6" fontWeight={700}>
                            {profile?.fullName}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            {profile?.email}
                        </Typography>
                        <Stack direction="row" spacing={1} justifyContent="center" sx={{ mt: 1.5 }}>
                            <Chip label={profile?.role} size="small" color={profile?.role === 'ADMIN' ? 'secondary' : 'default'} />
                            <Chip
                                label={profile?.accountStatus}
                                size="small"
                                color={profile?.accountStatus === 'ACTIVE' ? 'success' : 'default'}
                            />
                        </Stack>
                        <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 2 }}>
                            Joined on {profile?.createdAt ? new Date(profile.createdAt).toLocaleDateString() : '—'}
                        </Typography>
                    </Paper>
                </Grid>

                <Grid item xs={12} md={8}>
                    <Stack spacing={3}>
                        <Paper elevation={0} sx={{ p: 3, borderRadius: 3 }}>
                            <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>
                                Personal Information
                            </Typography>
                            {profileError && (
                                <Alert severity="error" sx={{ mb: 2 }}>
                                    {profileError}
                                </Alert>
                            )}
                            <Box component="form" onSubmit={handleProfileSubmit}>
                                <Grid container spacing={2}>
                                    <Grid item xs={12} sm={6}>
                                        <TextField
                                            label="Full Name"
                                            value={profileForm.fullName}
                                            onChange={(e) => setProfileForm({ ...profileForm, fullName: e.target.value })}
                                            fullWidth
                                            required
                                            disabled={profileSaving}
                                        />
                                    </Grid>
                                    <Grid item xs={12} sm={6}>
                                        <TextField
                                            label="Phone Number"
                                            value={profileForm.phoneNumber}
                                            onChange={(e) => setProfileForm({ ...profileForm, phoneNumber: e.target.value })}
                                            fullWidth
                                            disabled={profileSaving}
                                            helperText="10 digits"
                                        />
                                    </Grid>
                                    <Grid item xs={12} sm={6}>
                                        <TextField label="Email" value={profile?.email || ''} fullWidth disabled />
                                    </Grid>
                                </Grid>
                                <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={profileSaving}>
                                    {profileSaving ? 'Saving…' : 'Save Changes'}
                                </Button>
                            </Box>
                        </Paper>

                        <Paper elevation={0} sx={{ p: 3, borderRadius: 3 }}>
                            <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 2 }}>
                                Change Password
                            </Typography>
                            {passwordError && (
                                <Alert severity="error" sx={{ mb: 2 }}>
                                    {passwordError}
                                </Alert>
                            )}
                            <Box component="form" onSubmit={handlePasswordSubmit}>
                                <Stack spacing={2}>
                                    <TextField
                                        label="Current Password"
                                        type="password"
                                        value={passwordForm.currentPassword}
                                        onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                                        fullWidth
                                        required
                                        disabled={passwordSaving}
                                    />
                                    <TextField
                                        label="New Password"
                                        type="password"
                                        value={passwordForm.newPassword}
                                        onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                                        fullWidth
                                        required
                                        disabled={passwordSaving}
                                        helperText="At least 6 characters"
                                    />
                                    <TextField
                                        label="Confirm New Password"
                                        type="password"
                                        value={passwordForm.confirmPassword}
                                        onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                                        fullWidth
                                        required
                                        disabled={passwordSaving}
                                    />
                                </Stack>
                                <Button type="submit" variant="contained" sx={{ mt: 2 }} disabled={passwordSaving}>
                                    {passwordSaving ? 'Updating…' : 'Change Password'}
                                </Button>
                            </Box>
                        </Paper>

                        <Paper elevation={0} sx={{ p: 3, borderRadius: 3, borderColor: 'error.main', border: '1px solid' }}>
                            <Typography variant="subtitle1" fontWeight={700} color="error.main" sx={{ mb: 1 }}>
                                Danger Zone
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                Deactivating your account will sign you out and disable access. This cannot be undone from
                                within the app.
                            </Typography>
                            <Divider sx={{ mb: 2 }} />
                            <Button variant="outlined" color="error" onClick={() => setDeactivateOpen(true)}>
                                Deactivate My Account
                            </Button>
                        </Paper>
                    </Stack>
                </Grid>
            </Grid>

            <ConfirmDialog
                open={deactivateOpen}
                title="Deactivate your account?"
                description="You will be signed out immediately and your account will be marked inactive."
                confirmLabel="Deactivate"
                confirmColor="error"
                loading={deactivating}
                onConfirm={handleDeactivate}
                onClose={() => setDeactivateOpen(false)}
            />

            <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast('')}>
                <Alert severity="info" onClose={() => setToast('')} sx={{ width: '100%' }}>
                    {toast}
                </Alert>
            </Snackbar>
        </Box>
    );
}