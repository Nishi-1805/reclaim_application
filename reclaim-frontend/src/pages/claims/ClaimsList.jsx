import { useCallback, useEffect, useMemo, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import Button from '@mui/material/Button';
import Skeleton from '@mui/material/Skeleton';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Divider from '@mui/material/Divider';

import StatusBadge from '../../components/common/StatusBadge';
import EmptyState from '../../components/common/EmptyState';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import { getMyClaims, getClaimById, withdrawClaim } from '../../api/claims';

const TABS = ['ALL', 'PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'];

export default function ClaimsList() {
  const [tab, setTab] = useState(0);
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [detailsId, setDetailsId] = useState(null);
  const [details, setDetails] = useState(null);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [withdrawId, setWithdrawId] = useState(null);
  const [withdrawing, setWithdrawing] = useState(false);
  const [toast, setToast] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getMyClaims();
      setClaims(data);
    } catch (err) {
      setToast(err.message || 'Failed to load claims.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (!detailsId) {
      setDetails(null);
      return;
    }
    let cancelled = false;
    (async () => {
      setDetailsLoading(true);
      try {
        const data = await getClaimById(detailsId);
        if (!cancelled) setDetails(data);
      } catch (err) {
        if (!cancelled) setToast(err.message || 'Failed to load claim.');
      } finally {
        if (!cancelled) setDetailsLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [detailsId]);

  const filtered = useMemo(() => {
    const status = TABS[tab];
    if (status === 'ALL') return claims;
    return claims.filter((c) => c.status === status);
  }, [claims, tab]);

  const handleWithdraw = async () => {
    setWithdrawing(true);
    try {
      await withdrawClaim(withdrawId);
      setToast('Claim withdrawn successfully.');
      setWithdrawId(null);
      load();
    } catch (err) {
      setToast(err.message || 'Failed to withdraw claim.');
    } finally {
      setWithdrawing(false);
    }
  };

  return (
    <Box sx={{ pt: 1 }}>
      <Typography variant="h5" fontWeight={800}>
        Claims
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Track all your ownership claims.
      </Typography>

      <Tabs value={tab} onChange={(_e, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="All" />
        <Tab label="Pending" />
        <Tab label="Approved" />
        <Tab label="Rejected" />
        <Tab label="Cancelled" />
      </Tabs>

      <Paper elevation={0} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        {loading ? (
          <Box sx={{ p: 2 }}>
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} variant="rounded" height={64} sx={{ mb: 1.5 }} />
            ))}
          </Box>
        ) : filtered.length === 0 ? (
          <Box sx={{ p: 2 }}>
            <EmptyState title="No claims here" description="Claims you submit on matches will show up here." />
          </Box>
        ) : (
          filtered.map((claim) => (
            <Stack
              key={claim.claimId}
              direction="row"
              alignItems="center"
              spacing={2}
              sx={{
                p: 2,
                borderBottom: '1px solid rgba(255,255,255,0.06)',
                '&:last-of-type': { borderBottom: 'none' },
              }}
            >
              <Box sx={{ flexGrow: 1 }}>
                <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.3 }}>
                  <StatusBadge status={claim.status} />
                  <Typography variant="caption" color="text.secondary">
                    Claim #{claim.claimId}
                  </Typography>
                </Stack>
                <Typography variant="body2" color="text.secondary">
                  Match score at claim time: {Math.round(claim.matchScoreAtClaimTime)}% · Submitted{' '}
                  {new Date(claim.createdAt).toLocaleDateString()}
                </Typography>
              </Box>

              <Stack direction="row" spacing={1}>
                <Button size="small" variant="outlined" onClick={() => setDetailsId(claim.claimId)}>
                  View Details
                </Button>
                {claim.status === 'PENDING' && (
                  <Button size="small" color="error" onClick={() => setWithdrawId(claim.claimId)}>
                    Withdraw
                  </Button>
                )}
              </Stack>
            </Stack>
          ))
        )}
      </Paper>

      <Dialog open={Boolean(detailsId)} onClose={() => setDetailsId(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Claim Details</DialogTitle>
        <DialogContent dividers>
          {detailsLoading || !details ? (
            <Skeleton variant="rounded" height={160} />
          ) : (
            <Box>
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <StatusBadge status={details.status} />
                <Typography variant="body2" color="text.secondary">
                  Match score: {Math.round(details.matchScoreAtClaimTime)}%
                </Typography>
              </Stack>
              <Typography variant="caption" color="text.secondary" display="block" sx={{ mb: 2 }}>
                Submitted {new Date(details.createdAt).toLocaleString()}
              </Typography>

              <Divider sx={{ mb: 2 }} />

              <Typography variant="subtitle2" fontWeight={700} sx={{ mb: 1 }}>
                Your Answers
              </Typography>
              <Stack spacing={1.5}>
                {details.ownershipAnswers?.map((a) => (
                  <Box key={a.ownershipQuestionId}>
                    <Typography variant="body2" fontWeight={600}>
                      {a.questionText}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {a.responseText}
                    </Typography>
                  </Box>
                ))}
              </Stack>
            </Box>
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <Button onClick={() => setDetailsId(null)}>Close</Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog
        open={Boolean(withdrawId)}
        title="Withdraw this claim?"
        description="This action cannot be undone."
        confirmLabel="Withdraw"
        confirmColor="error"
        loading={withdrawing}
        onConfirm={handleWithdraw}
        onClose={() => setWithdrawId(null)}
      />

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast('')}>
        <Alert severity="info" onClose={() => setToast('')} sx={{ width: '100%' }}>
          {toast}
        </Alert>
      </Snackbar>
    </Box>
  );
}