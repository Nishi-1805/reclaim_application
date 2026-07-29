import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';

export default function ConfirmDialog({
    open,
    title = 'Are you sure?',
    description,
    confirmLabel = 'Confirm',
    confirmColor = 'primary',
    loading = false,
    onConfirm,
    onClose,
}) {
    return (
        <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
            <DialogTitle>{title}</DialogTitle>
            <DialogContent>
                {description && <DialogContentText>{description}</DialogContentText>}
            </DialogContent>
            <DialogActions sx={{ px: 3, pb: 2.5 }}>
                <Button onClick={onClose} disabled={loading}>
                    Cancel
                </Button>
                <Button onClick={onConfirm} variant="contained" color={confirmColor} disabled={loading}>
                    {loading ? 'Please wait…' : confirmLabel}
                </Button>
            </DialogActions>
        </Dialog>
    );
}