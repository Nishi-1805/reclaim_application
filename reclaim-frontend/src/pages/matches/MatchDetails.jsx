import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Stack from '@mui/material/Stack';
import Chip from '@mui/material/Chip';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Skeleton from '@mui/material/Skeleton';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import Divider from '@mui/material/Divider';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import HighlightOffIcon from '@mui/icons-material/HighlightOff';

import { useAuth } from '../../context/AuthContext';
import StatusBadge from '../../components/common/StatusBadge';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import SubmitClaimModal from '../claims/SubmitClaimModal';
import { getMatchById, confirmMatch, rejectMatch } from '../../api/matches';
import { getItemById } from '../../api/items';

export default function MatchDetails() {
    const { matchId } = useParams();
    const { user, isAdmin } = useAuth();
    const navigate = useNavigate();

    const [match, setMatch] = useState(null);
    const [lostItem, setLostItem] = useState(null);
    const [foundItem, setFoundItem] = useState(null);
    const [loading, setLoading] = useState(true);
    const [claimOpen, setClaimOpen] = useState(false);
    const [confirmAction, setConfirmAction] = useState(null); // 'confirm' | 'reject'
    const [actionLoading, setActionLoading] = useState(false);
    const [toast, setToast] = useState('');

    const load = useCallback(async () => {
        setLoading(true);
        try {
            const m = await getMatchById(matchId);
            setMatch(m);
            const [lost, found] = await Promise.all([
                getItemById(m.lostItemId),
                getItemById(m.foundItemId),
            ]);
            setLostItem(lost);
            setFoundItem(found);
        } catch (err) {
            setToast(err.message || 'Failed to load match.');
        } finally {
            setLoading(false);
        }
    }, [matchId]);

    useEffect(() => {
        load();
    }, [load]);

    const handleAdminAction = async () => {
        setActionLoading(true);
        try {
            if (confirmAction === 'confirm') {
                await confirmMatch(matchId);
                setToast('Match confirmed. Both items have been closed.');
            } else {
                await rejectMatch(matchId);
                setToast('Match rejected.');
            }
            setConfirmAction(null);
            load();
        } catch (err) {
            setToast(err.message || 'Action failed.');
        } finally {
            setActionLoading(false);
        }
    };

    if (loading) {
        return (
            <Box sx={{ pt: 1 }}>
                <Skeleton variant="rounded" height={400} />
            </Box>
        );
    }

    if (!match || !lostItem || !foundItem) {
        return (
            <Box sx={{ pt: 1 }}>
                <Typography color="text.secondary">Match not found.</Typography>
            </Box>
        );
    }

    const reasons = (match.matchReason || '')
        .split(',')
        .map((r) => r.trim())
        .filter(Boolean);

    const canClaim =
        user?.userId === lostItem.ownerId &&
        lostItem.status === 'OPEN' &&
        foundItem.status === 'OPEN';

    const canAdminAct = isAdmin && match.matchStatus === 'PENDING';

    return (
        <Box sx={{ pt: 1 }}>
            <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 2 }}>
                <IconButton onClick={() => navigate(-1)} size="small">
                    <ArrowBackIcon fontSize="small" />
                </IconButton>
                <Typography variant="h5" fontWeight={800}>
                    Match Details
                </Typography>
            </Stack>

            <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }} flexWrap="wrap" gap={1.5}>
                <StatusBadge status={match.matchStatus} />
                <Chip
                    label={`${Math.round(match.matchScore)}% Match`}
                    color="success"
                    sx={{ fontWeight: 700, fontSize: '0.85rem', px: 1 }}
                />
            </Stack>

            <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                    <ItemCard label="Lost Item" item={lostItem} />
                </Grid>
                <Grid item xs={12} md={6}>
                    <ItemCard label="Found Item" item={foundItem} />
                </Grid>

                <Grid item xs={12}>
                    <Paper elevation={0} sx={{ p: 3, borderRadius: 3 }}>
                        <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1.5 }}>
                            Match Reason
                        </Typography>
                        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                            {reasons.length ? (
                                reasons.map((r) => <Chip key={r} label={r} size="small" />)
                            ) : (
                                <Typography variant="body2" color="text.secondary">
                                    No specific matching criteria recorded.
                                </Typography>
                            )}
                        </Stack>

                        <Divider sx={{ my: 2.5 }} />

                        <Stack direction="row" spacing={1.5} flexWrap="wrap" gap={1.5}>
                            {canClaim && (
                                <Button variant="contained" onClick={() => setClaimOpen(true)}>
                                    Submit Claim
                                </Button>
                            )}

                            {canAdminAct && (
                                <>
                                    <Button
                                        variant="outlined"
                                        color="success"
                                        startIcon={<CheckCircleOutlineIcon />}
                                        onClick={() => setConfirmAction('confirm')}
                                    >
                                        Confirm Match
                                    </Button>
                                    <Button
                                        variant="outlined"
                                        color="error"
                                        startIcon={<HighlightOffIcon />}
                                        onClick={() => setConfirmAction('reject')}
                                    >
                                        Reject Match
                                    </Button>
                                </>
                            )}

                            {!canClaim && !canAdminAct && (
                                <Typography variant="body2" color="text.secondary">
                                    No actions available for this match right now.
                                </Typography>
                            )}
                        </Stack>
                    </Paper>
                </Grid>
            </Grid>

            <SubmitClaimModal
                open={claimOpen}
                itemMatchId={match.itemMatchId}
                foundItemId={foundItem.id}
                onClose={() => setClaimOpen(false)}
                onResolved={() => load()}
            />

            <ConfirmDialog
                open={Boolean(confirmAction)}
                title={confirmAction === 'confirm' ? 'Confirm this match?' : 'Reject this match?'}
                description={
                    confirmAction === 'confirm'
                        ? 'Both items will be marked as closed.'
                        : 'This match will be marked as rejected.'
                }
                confirmLabel={confirmAction === 'confirm' ? 'Confirm Match' : 'Reject Match'}
                confirmColor={confirmAction === 'confirm' ? 'success' : 'error'}
                loading={actionLoading}
                onConfirm={handleAdminAction}
                onClose={() => setConfirmAction(null)}
            />

            <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast('')}>
                <Alert severity="info" onClose={() => setToast('')} sx={{ width: '100%' }}>
                    {toast}
                </Alert>
            </Snackbar>
        </Box>
    );
}

function ItemCard({ label, item }) {
    return (
        <Paper elevation={0} sx={{ p: 3, borderRadius: 3, height: '100%' }}>
            <Typography variant="overline" color="text.secondary">
                {label}
            </Typography>
            <Stack direction="row" spacing={2} sx={{ mt: 1 }}>
                <Box
                    sx={{
                        width: 88,
                        height: 88,
                        borderRadius: 2,
                        bgcolor: 'grey.900',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        overflow: 'hidden',
                        flexShrink: 0,
                    }}
                >
                    {item.imageUrls?.[0] ? (
                        <Box component="img" src={item.imageUrls[0]} alt={item.title} sx={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    ) : (
                        <Inventory2Icon sx={{ color: 'text.secondary' }} />
                    )}
                </Box>
                <Box sx={{ minWidth: 0 }}>
                    <Typography variant="subtitle1" fontWeight={700} noWrap>
                        {item.title}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block">
                        Category: {item.category}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block">
                        Color: {item.color || '—'}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block">
                        Location: {item.locationDescription}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" display="block">
                        Date: {item.itemDate}
                    </Typography>
                </Box>
            </Stack>
        </Paper>
    );
}