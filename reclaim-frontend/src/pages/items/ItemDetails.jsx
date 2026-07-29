import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Stack from '@mui/material/Stack';
import Button from '@mui/material/Button';
import Divider from '@mui/material/Divider';
import Skeleton from '@mui/material/Skeleton';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import IconButton from '@mui/material/IconButton';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';

import { useAuth } from '../../context/AuthContext';
import StatusBadge from '../../components/common/StatusBadge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import EditItemModal from './EditItemModal';
import { getItemById, deleteItem } from '../../api/items';

export default function ItemDetails() {
    const { itemId } = useParams();
    const { user } = useAuth();
    const navigate = useNavigate();

    const [item, setItem] = useState(null);
    const [loading, setLoading] = useState(true);
    const [activeImage, setActiveImage] = useState(0);
    const [editOpen, setEditOpen] = useState(false);
    const [cancelOpen, setCancelOpen] = useState(false);
    const [cancelling, setCancelling] = useState(false);
    const [toast, setToast] = useState('');

    const load = useCallback(async () => {
        setLoading(true);
        try {
            const data = await getItemById(itemId);
            setItem(data);
            setActiveImage(0);
        } catch (err) {
            setToast(err.message || 'Failed to load item.');
        } finally {
            setLoading(false);
        }
    }, [itemId]);

    useEffect(() => {
        load();
    }, [load]);

    const isOwner = item?.ownerId === user?.userId;
    const canManage = isOwner && item?.status === 'OPEN';

    const handleCancel = async () => {
        setCancelling(true);
        try {
            await deleteItem(item.id);
            setToast('Item cancelled successfully.');
            setCancelOpen(false);
            load();
        } catch (err) {
            setToast(err.message || 'Failed to cancel item.');
        } finally {
            setCancelling(false);
        }
    };

    if (loading) {
        return (
            <Box sx={{ pt: 1 }}>
                <Skeleton variant="rounded" height={400} />
            </Box>
        );
    }

    if (!item) {
        return (
            <Box sx={{ pt: 1 }}>
                <Typography color="text.secondary">Item not found.</Typography>
            </Box>
        );
    }

    return (
        <Box sx={{ pt: 1 }}>
            <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
                <IconButton onClick={() => navigate(-1)} size="small">
                    <ArrowBackIcon fontSize="small" />
                </IconButton>
                <Typography variant="h5" fontWeight={800}>
                    Item Details
                </Typography>
            </Stack>

            <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                    <Paper elevation={0} sx={{ borderRadius: 3, overflow: 'hidden' }}>
                        <Box
                            sx={{
                                aspectRatio: '4/3',
                                bgcolor: 'grey.900',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                            }}
                        >
                            {item.imageUrls?.length ? (
                                <Box
                                    component="img"
                                    src={item.imageUrls[activeImage]}
                                    alt={item.title}
                                    sx={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                />
                            ) : (
                                <Inventory2Icon sx={{ fontSize: 64, color: 'text.secondary' }} />
                            )}
                        </Box>
                        {item.imageUrls?.length > 1 && (
                            <Stack direction="row" spacing={1} sx={{ p: 1.5 }}>
                                {item.imageUrls.map((url, idx) => (
                                    <Box
                                        key={url}
                                        component="img"
                                        src={url}
                                        onClick={() => setActiveImage(idx)}
                                        sx={{
                                            width: 56,
                                            height: 56,
                                            objectFit: 'cover',
                                            borderRadius: 1.5,
                                            cursor: 'pointer',
                                            opacity: idx === activeImage ? 1 : 0.5,
                                            border: idx === activeImage ? '2px solid' : '2px solid transparent',
                                            borderColor: idx === activeImage ? 'primary.main' : 'transparent',
                                        }}
                                    />
                                ))}
                            </Stack>
                        )}
                    </Paper>
                </Grid>

                <Grid item xs={12} md={6}>
                    <Paper elevation={0} sx={{ p: 3, borderRadius: 3, height: '100%' }}>
                        <Stack direction="row" spacing={1} sx={{ mb: 1.5 }}>
                            <StatusBadge status={item.itemType} />
                            <StatusBadge status={item.status} />
                        </Stack>

                        <Typography variant="h5" fontWeight={800} sx={{ mb: 0.5 }}>
                            {item.title}
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                            Reported by {item.ownerName}
                        </Typography>

                        <Typography variant="body2" sx={{ mb: 2, whiteSpace: 'pre-wrap' }}>
                            {item.description}
                        </Typography>

                        <Divider sx={{ mb: 2 }} />

                        <Grid container spacing={2} sx={{ mb: 2 }}>
                            <InfoRow label="Category" value={item.category} />
                            <InfoRow label="Brand" value={item.brand || '—'} />
                            <InfoRow label="Color" value={item.color || '—'} />
                            <InfoRow label="Location" value={item.locationDescription} />
                            <InfoRow label="Date" value={item.itemDate} />
                        </Grid>

                        {canManage && (
                            <Stack direction="row" spacing={1.5} sx={{ mt: 1 }}>
                                <Button variant="outlined" onClick={() => setEditOpen(true)}>
                                    Edit
                                </Button>
                                <Button variant="outlined" color="error" onClick={() => setCancelOpen(true)}>
                                    Cancel Item
                                </Button>
                                <Button
                                    variant="contained"
                                    onClick={() =>
                                        navigate('/matches', {
                                            state: { itemId: item.id, itemType: item.itemType },
                                        })
                                    }
                                >
                                    View Matches
                                </Button>
                            </Stack>
                        )}
                    </Paper>
                </Grid>

                {item.itemType === 'FOUND' && item.ownershipQuestions?.length > 0 && (
                    <Grid item xs={12}>
                        <Paper elevation={0} sx={{ p: 3, borderRadius: 3 }}>
                            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1.5 }}>
                                <HelpOutlineIcon fontSize="small" color="action" />
                                <Typography variant="subtitle1" fontWeight={700}>
                                    Ownership Verification Questions
                                </Typography>
                            </Stack>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
                                A claimant will need to correctly answer these to verify ownership.
                            </Typography>
                            <Stack spacing={1}>
                                {item.ownershipQuestions
                                    .slice()
                                    .sort((a, b) => a.displayOrder - b.displayOrder)
                                    .map((q, idx) => (
                                        <Typography key={q.ownershipQuestionId} variant="body2">
                                            {idx + 1}. {q.questionText}
                                        </Typography>
                                    ))}
                            </Stack>
                        </Paper>
                    </Grid>
                )}
            </Grid>

            <EditItemModal
                open={editOpen}
                item={item}
                onClose={() => setEditOpen(false)}
                onUpdated={() => {
                    setToast('Item updated successfully.');
                    load();
                }}
            />

            <ConfirmDialog
                open={cancelOpen}
                title="Cancel this item?"
                description="This will mark the item as cancelled. This action cannot be undone."
                confirmLabel="Cancel Item"
                confirmColor="error"
                loading={cancelling}
                onConfirm={handleCancel}
                onClose={() => setCancelOpen(false)}
            />

            <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast('')}>
                <Alert severity="info" onClose={() => setToast('')} sx={{ width: '100%' }}>
                    {toast}
                </Alert>
            </Snackbar>
        </Box>
    );
}

function InfoRow({ label, value }) {
    return (
        <Grid item xs={6}>
            <Typography variant="caption" color="text.secondary" display="block">
                {label}
            </Typography>
            <Typography variant="body2" fontWeight={600}>
                {value}
            </Typography>
        </Grid>
    );
}