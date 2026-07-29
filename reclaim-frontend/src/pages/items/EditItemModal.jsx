import { useEffect, useState } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Alert from '@mui/material/Alert';

import { updateItem } from '../../api/items';

export default function EditItemModal({ open, item, onClose, onUpdated }) {
    const [form, setForm] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (open && item) {
            setForm({
                title: item.title || '',
                description: item.description || '',
                category: item.category || '',
                brand: item.brand || '',
                color: item.color || '',
                locationDescription: item.locationDescription || '',
                itemDate: item.itemDate || '',
            });
            setError('');
        }
    }, [open, item]);

    if (!form) return null;

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError('');
        try {
            const updated = await updateItem(item.id, {
                title: form.title.trim(),
                description: form.description.trim(),
                category: form.category.trim(),
                brand: form.brand.trim() || null,
                color: form.color.trim() || null,
                locationDescription: form.locationDescription.trim(),
                itemDate: form.itemDate,
            });
            onUpdated?.(updated);
            onClose();
        } catch (err) {
            setError(err.message || 'Failed to update item.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Dialog open={open} onClose={submitting ? undefined : onClose} maxWidth="sm" fullWidth>
            <DialogTitle>Edit Item</DialogTitle>
            <Box component="form" onSubmit={handleSubmit}>
                <DialogContent dividers sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                    {error && <Alert severity="error">{error}</Alert>}

                    <TextField label="Title" name="title" value={form.title} onChange={handleChange} required fullWidth disabled={submitting} />
                    <TextField
                        label="Description"
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                        required
                        fullWidth
                        multiline
                        minRows={3}
                        disabled={submitting}
                    />
                    <TextField label="Category" name="category" value={form.category} onChange={handleChange} required fullWidth disabled={submitting} />

                    <Grid container spacing={2}>
                        <Grid item xs={6}>
                            <TextField label="Brand" name="brand" value={form.brand} onChange={handleChange} fullWidth disabled={submitting} />
                        </Grid>
                        <Grid item xs={6}>
                            <TextField label="Color" name="color" value={form.color} onChange={handleChange} fullWidth disabled={submitting} />
                        </Grid>
                    </Grid>

                    <Grid container spacing={2}>
                        <Grid item xs={7}>
                            <TextField
                                label="Location"
                                name="locationDescription"
                                value={form.locationDescription}
                                onChange={handleChange}
                                required
                                fullWidth
                                disabled={submitting}
                            />
                        </Grid>
                        <Grid item xs={5}>
                            <TextField
                                label="Date"
                                name="itemDate"
                                type="date"
                                value={form.itemDate}
                                onChange={handleChange}
                                required
                                fullWidth
                                disabled={submitting}
                                InputLabelProps={{ shrink: true }}
                                inputProps={{ max: new Date().toISOString().split('T')[0] }}
                            />
                        </Grid>
                    </Grid>
                </DialogContent>
                <DialogActions sx={{ px: 3, py: 2 }}>
                    <Button onClick={onClose} disabled={submitting}>
                        Cancel
                    </Button>
                    <Button type="submit" variant="contained" disabled={submitting}>
                        {submitting ? 'Saving…' : 'Save Changes'}
                    </Button>
                </DialogActions>
            </Box>
        </Dialog>
    );
}