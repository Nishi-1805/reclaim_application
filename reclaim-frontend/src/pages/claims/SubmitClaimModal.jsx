import { useEffect, useState } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Alert from '@mui/material/Alert';
import Skeleton from '@mui/material/Skeleton';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import HighlightOffIcon from '@mui/icons-material/HighlightOff';

import { getOwnershipQuestions } from '../../api/items';
import { submitClaim } from '../../api/claims';

export default function SubmitClaimModal({ open, itemMatchId, foundItemId, onClose, onResolved }) {
    const [questions, setQuestions] = useState([]);
    const [answers, setAnswers] = useState({});
    const [loadingQuestions, setLoadingQuestions] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [result, setResult] = useState(null); // ClaimResponse once submitted

    useEffect(() => {
        if (!open) {
            setResult(null);
            setError('');
            setAnswers({});
            return;
        }
        let cancelled = false;
        (async () => {
            setLoadingQuestions(true);
            try {
                const data = await getOwnershipQuestions(foundItemId);
                if (!cancelled) setQuestions(data.sort((a, b) => a.displayOrder - b.displayOrder));
            } catch (err) {
                if (!cancelled) setError(err.message || 'Failed to load verification questions.');
            } finally {
                if (!cancelled) setLoadingQuestions(false);
            }
        })();
        return () => {
            cancelled = true;
        };
    }, [open, foundItemId]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        const unanswered = questions.some((q) => !answers[q.ownershipQuestionId]?.trim());
        if (unanswered) {
            setError('Please answer every question.');
            return;
        }

        setSubmitting(true);
        setError('');
        try {
            const response = await submitClaim({
                itemMatchId,
                ownershipAnswers: questions.map((q) => ({
                    ownershipQuestionId: q.ownershipQuestionId,
                    responseText: answers[q.ownershipQuestionId].trim(),
                })),
            });
            setResult(response);
            onResolved?.(response);
        } catch (err) {
            setError(err.message || 'Failed to submit claim.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Dialog open={open} onClose={submitting ? undefined : onClose} maxWidth="sm" fullWidth>
            <DialogTitle>{result ? 'Claim Result' : 'Submit Ownership Claim'}</DialogTitle>

            {result ? (
                <>
                    <DialogContent sx={{ textAlign: 'center', py: 4 }}>
                        {result.status === 'APPROVED' ? (
                            <>
                                <CheckCircleOutlineIcon sx={{ fontSize: 56, color: 'success.main', mb: 1 }} />
                                <Typography variant="h6" fontWeight={700} sx={{ mb: 0.5 }}>
                                    Claim Approved!
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Your answers matched closely enough to verify ownership. The finder will be notified.
                                </Typography>
                            </>
                        ) : (
                            <>
                                <HighlightOffIcon sx={{ fontSize: 56, color: 'error.main', mb: 1 }} />
                                <Typography variant="h6" fontWeight={700} sx={{ mb: 0.5 }}>
                                    Claim Not Verified
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Your answers didn't match closely enough this time. You can review the item details and try
                                    submitting a claim on another match if applicable.
                                </Typography>
                            </>
                        )}
                    </DialogContent>
                    <DialogActions sx={{ px: 3, pb: 2.5 }}>
                        <Button variant="contained" onClick={onClose} fullWidth>
                            Close
                        </Button>
                    </DialogActions>
                </>
            ) : (
                <Box component="form" onSubmit={handleSubmit}>
                    <DialogContent dividers sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                        <Typography variant="body2" color="text.secondary">
                            Answer these ownership verification questions as accurately as possible. Your claim is verified
                            automatically based on how closely your answers match.
                        </Typography>

                        {error && <Alert severity="error">{error}</Alert>}

                        {loadingQuestions ? (
                            [1, 2, 3].map((i) => <Skeleton key={i} variant="rounded" height={56} />)
                        ) : (
                            questions.map((q, idx) => (
                                <TextField
                                    key={q.ownershipQuestionId}
                                    label={`${idx + 1}. ${q.questionText}`}
                                    value={answers[q.ownershipQuestionId] || ''}
                                    onChange={(e) =>
                                        setAnswers({ ...answers, [q.ownershipQuestionId]: e.target.value })
                                    }
                                    fullWidth
                                    required
                                    disabled={submitting}
                                />
                            ))
                        )}
                    </DialogContent>
                    <DialogActions sx={{ px: 3, py: 2 }}>
                        <Button onClick={onClose} disabled={submitting}>
                            Cancel
                        </Button>
                        <Button type="submit" variant="contained" disabled={submitting || loadingQuestions}>
                            {submitting ? 'Submitting…' : 'Submit Claim'}
                        </Button>
                    </DialogActions>
                </Box>
            )}
        </Dialog>
    );
}