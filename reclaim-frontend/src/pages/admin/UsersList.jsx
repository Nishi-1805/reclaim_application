import { useCallback, useEffect, useMemo, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Avatar from '@mui/material/Avatar';
import Skeleton from '@mui/material/Skeleton';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import Drawer from '@mui/material/Drawer';
import IconButton from '@mui/material/IconButton';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Chip from '@mui/material/Chip';
import SearchIcon from '@mui/icons-material/Search';
import CloseIcon from '@mui/icons-material/Close';

import StatusBadge from '../../components/common/StatusBadge';
import EmptyState from '../../components/common/EmptyState';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { getAllUsers, getUsersByStatus, getUserById, updateUserStatus } from '../../api/users';

function initials(name = '') {
    return name.split(' ').map((p) => p[0]).slice(0, 2).join('').toUpperCase();
}

const STATUS_FILTERS = ['ALL', 'ACTIVE', 'INACTIVE', 'DEACTIVATED'];

export default function UsersList() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [search, setSearch] = useState('');
    const [selectedUserId, setSelectedUserId] = useState(null);
    const [selectedUser, setSelectedUser] = useState(null);
    const [detailsLoading, setDetailsLoading] = useState(false);
    const [pendingStatus, setPendingStatus] = useState(null);
    const [statusSaving, setStatusSaving] = useState(false);
    const [toast, setToast] = useState('');

    const load = useCallback(async () => {
        setLoading(true);
        try {
            const data = statusFilter === 'ALL' ? await getAllUsers() : await getUsersByStatus(statusFilter);
            setUsers(data);
        } catch (err) {
            setToast(err.message || 'Failed to load users.');
        } finally {
            setLoading(false);
        }
    }, [statusFilter]);

    useEffect(() => {
        load();
    }, [load]);

    useEffect(() => {
        if (!selectedUserId) {
            setSelectedUser(null);
            return;
        }
        let cancelled = false;
        (async () => {
            setDetailsLoading(true);
            try {
                const data = await getUserById(selectedUserId);
                if (!cancelled) setSelectedUser(data);
            } catch (err) {
                if (!cancelled) setToast(err.message || 'Failed to load user.');
            } finally {
                if (!cancelled) setDetailsLoading(false);
            }
        })();
        return () => {
            cancelled = true;
        };
    }, [selectedUserId]);

    const filtered = useMemo(() => {
        if (!search.trim()) return users;
        const q = search.trim().toLowerCase();
        return users.filter(
            (u) => u.fullName.toLowerCase().includes(q) || u.email.toLowerCase().includes(q)
        );
    }, [users, search]);

    const handleStatusChange = async () => {
        setStatusSaving(true);
        try {
            const updated = await updateUserStatus(selectedUser.userId, pendingStatus);
            setSelectedUser(updated);
            setToast(`Account status updated to ${pendingStatus}.`);
            setPendingStatus(null);
            load();
        } catch (err) {
            setToast(err.message || 'Failed to update user status.');
        } finally {
            setStatusSaving(false);
        }
    };

    return (
        <Box sx={{ pt: 1 }}>
            <Typography variant="h5" fontWeight={800}>
                Admin — Users
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Manage all registered users.
            </Typography>

            <Stack direction="row" spacing={2} sx={{ mb: 2 }} flexWrap="wrap" gap={1.5}>
                <TextField
                    size="small"
                    placeholder="Search users..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <SearchIcon fontSize="small" />
                            </InputAdornment>
                        ),
                    }}
                    sx={{ minWidth: 260 }}
                />
                <FormControl size="small" sx={{ minWidth: 160 }}>
                    <Select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                        {STATUS_FILTERS.map((s) => (
                            <MenuItem key={s} value={s}>
                                {s === 'ALL' ? 'All Status' : s}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
            </Stack>

            <Paper elevation={0} sx={{ borderRadius: 3, overflow: 'hidden' }}>
                {loading ? (
                    <Box sx={{ p: 2 }}>
                        {[1, 2, 3, 4].map((i) => (
                            <Skeleton key={i} variant="rounded" height={60} sx={{ mb: 1.5 }} />
                        ))}
                    </Box>
                ) : filtered.length === 0 ? (
                    <Box sx={{ p: 2 }}>
                        <EmptyState title="No users found" />
                    </Box>
                ) : (
                    filtered.map((u) => (
                        <Stack
                            key={u.userId}
                            direction="row"
                            alignItems="center"
                            spacing={2}
                            onClick={() => setSelectedUserId(u.userId)}
                            sx={{
                                p: 2,
                                borderBottom: '1px solid rgba(255,255,255,0.06)',
                                '&:last-of-type': { borderBottom: 'none' },
                                cursor: 'pointer',
                                '&:hover': { bgcolor: 'rgba(255,255,255,0.02)' },
                            }}
                        >
                            <Avatar sx={{ bgcolor: 'primary.main', width: 40, height: 40, fontSize: 14 }}>
                                {initials(u.fullName)}
                            </Avatar>
                            <Box sx={{ flexGrow: 1, minWidth: 0 }}>
                                <Typography variant="body2" fontWeight={600} noWrap>
                                    {u.fullName}
                                </Typography>
                                <Typography variant="caption" color="text.secondary" noWrap>
                                    {u.email}
                                </Typography>
                            </Box>
                            <Chip label={u.role} size="small" variant="outlined" />
                            <StatusBadge status={u.accountStatus} />
                        </Stack>
                    ))
                )}
            </Paper>

            <Drawer anchor="right" open={Boolean(selectedUserId)} onClose={() => setSelectedUserId(null)}>
                <Box sx={{ width: 340, p: 3 }}>
                    <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                        <Typography variant="h6" fontWeight={700}>
                            User Details
                        </Typography>
                        <IconButton size="small" onClick={() => setSelectedUserId(null)}>
                            <CloseIcon fontSize="small" />
                        </IconButton>
                    </Stack>

                    {detailsLoading || !selectedUser ? (
                        <Skeleton variant="rounded" height={300} />
                    ) : (
                        <>
                            <Stack alignItems="center" sx={{ mb: 2 }}>
                                <Avatar sx={{ width: 72, height: 72, bgcolor: 'primary.main', fontSize: 24, mb: 1 }}>
                                    {initials(selectedUser.fullName)}
                                </Avatar>
                                <Typography variant="subtitle1" fontWeight={700}>
                                    {selectedUser.fullName}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    {selectedUser.email}
                                </Typography>
                            </Stack>

                            <Divider sx={{ mb: 2 }} />

                            <Stack spacing={1.5} sx={{ mb: 3 }}>
                                <DetailRow label="Status" value={<StatusBadge status={selectedUser.accountStatus} />} />
                                <DetailRow label="Role" value={selectedUser.role} />
                                <DetailRow label="Phone" value={selectedUser.phoneNumber || '—'} />
                                <DetailRow
                                    label="Joined"
                                    value={selectedUser.createdAt ? new Date(selectedUser.createdAt).toLocaleDateString() : '—'}
                                />
                            </Stack>

                            {selectedUser.role === 'ADMIN' ? (
                                <Alert severity="info">Admin account status cannot be modified.</Alert>
                            ) : (
                                <Stack spacing={1}>
                                    <Typography variant="caption" color="text.secondary">
                                        Change account status
                                    </Typography>
                                    {['ACTIVE', 'INACTIVE', 'DEACTIVATED']
                                        .filter((s) => s !== selectedUser.accountStatus)
                                        .map((s) => (
                                            <Button
                                                key={s}
                                                variant="outlined"
                                                color={s === 'DEACTIVATED' ? 'error' : s === 'ACTIVE' ? 'success' : 'inherit'}
                                                onClick={() => setPendingStatus(s)}
                                            >
                                                Set to {s}
                                            </Button>
                                        ))}
                                </Stack>
                            )}
                        </>
                    )}
                </Box>
            </Drawer>

            <ConfirmDialog
                open={Boolean(pendingStatus)}
                title={`Set account status to ${pendingStatus}?`}
                description="The user's access will be updated immediately."
                confirmLabel="Confirm"
                confirmColor={pendingStatus === 'DEACTIVATED' ? 'error' : 'primary'}
                loading={statusSaving}
                onConfirm={handleStatusChange}
                onClose={() => setPendingStatus(null)}
            />

            <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast('')}>
                <Alert severity="info" onClose={() => setToast('')} sx={{ width: '100%' }}>
                    {toast}
                </Alert>
            </Snackbar>
        </Box>
    );
}

function DetailRow({ label, value }) {
    return (
        <Stack direction="row" justifyContent="space-between" alignItems="center">
            <Typography variant="body2" color="text.secondary">
                {label}
            </Typography>
            <Typography variant="body2" fontWeight={600}>
                {value}
            </Typography>
        </Stack>
    );
}