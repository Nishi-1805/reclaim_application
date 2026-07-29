import { useEffect, useState } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import IconButton from '@mui/material/IconButton';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Alert from '@mui/material/Alert';
import ToggleButton from '@mui/material/ToggleButton';
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup';
import CloseIcon from '@mui/icons-material/Close';
import AddIcon from '@mui/icons-material/Add';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';

import ImageUploader from '../../components/forms/ImageUploader';
import { createItem, uploadItemImage } from '../../api/items';

const emptyQuestion = () => ({
    key: crypto.randomUUID(),
    questionText: '',
    expectedAnswer: '',
});

const initialForm = {
    itemType: 'LOST',
    title: '',
    description: '',
    category: '',
    brand: '',
    color: '',
    locationDescription: '',
    itemDate: '',
};

export default function ReportItemModal({ open, defaultType = 'LOST', onClose, onCreated }) {
    const [form, setForm] = useState({ ...initialForm, itemType: defaultType });
    const [questions, setQuestions] = useState([emptyQuestion(), emptyQuestion(), emptyQuestion()]);
    const [images, setImages] = useState([]);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (open) {
            setForm({ ...initialForm, itemType: defaultType });
            setQuestions([emptyQuestion(), emptyQuestion(), emptyQuestion()]);
            setImages([]);
            setError('');
        }
    }, [open, defaultType]);

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    const handleTypeChange = (_e, value) => {
        if (value) setForm({ ...form, itemType: value });
    };

    const addQuestion = () => {
        if (questions.length >= 5) return;
        setQuestions([...questions, emptyQuestion()]);
    };

    const removeQuestion = (key) => {
        if (questions.length <= 3) return;
        setQuestions(questions.filter((q) => q.key !== key));
    };

    const updateQuestion = (key, field, value) => {
        setQuestions(questions.map((q) => (q.key === key ? { ...q, [field]: value } : q)));
    };

    const handleAddImages = (files) => {
        const mapped = files.map((file) => ({
            id: crypto.randomUUID(),
            file,
            previewUrl: URL.createObjectURL(file),
        }));
        setImages((prev) => [...prev, ...mapped]);
    };

    const handleRemoveImage = (idx) => {
        setImages((prev) => prev.filter((_, i) => i !== idx));
    };

    const validate = () => {
        if (!form.title.trim()) return 'Title is required.';
        if (!form.description.trim()) return 'Description is required.';
        if (!form.category.trim()) return 'Category is required.';
        if (!form.locationDescription.trim()) return 'Location is required.';
        if (!form.itemDate) return 'Date is required.';

        if (form.itemType === 'FOUND') {
            if (questions.length < 3 || questions.length > 5) {
                return 'Found items require between 3 and 5 ownership questions.';
            }
            for (const q of questions) {
                if (!q.questionText.trim() || !q.expectedAnswer.trim()) {
                    return 'Every ownership question needs both a question and an expected answer.';
                }
            }
        }

        return '';
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const validationError = validate();
        if (validationError) {
            setError(validationError);
            return;
        }

        setSubmitting(true);
        setError('');

        try {
            const payload = {
                title: form.title.trim(),
                description: form.description.trim(),
                category: form.category.trim(),
                brand: form.brand.trim() || null,
                color: form.color.trim() || null,
                locationDescription: form.locationDescription.trim(),
                itemDate: form.itemDate,
                itemType: form.itemType,
                ownershipQuestions:
                    form.itemType === 'FOUND'
                        ? questions.map((q) => ({
                            questionText: q.questionText.trim(),
                            expectedAnswer: q.expectedAnswer.trim(),
                        }))
                        : null,
            };

            const created = await createItem(payload);

            // Upload images sequentially against the newly created item's id
            for (const img of images) {
                // eslint-disable-next-line no-await-in-loop
                await uploadItemImage(created.id, img.file);
            }

            onCreated?.(created);
            onClose();
        } catch (err) {
            setError(err.message || 'Failed to submit report.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Dialog open={open} onClose={submitting ? undefined : onClose} maxWidth="sm" fullWidth>
            <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                Report {form.itemType === 'LOST' ? 'Lost' : 'Found'} Item
                <IconButton onClick={onClose} disabled={submitting} size="small">
                    <CloseIcon fontSize="small" />
                </IconButton>
            </DialogTitle>

            <Box component="form" onSubmit={handleSubmit}>
                <DialogContent dividers sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                    {error && <Alert severity="error">{error}</Alert>}

                    <ToggleButtonGroup
                        value={form.itemType}
                        exclusive
                        onChange={handleTypeChange}
                        fullWidth
                        disabled={submitting}
                    >
                        <ToggleButton value="LOST" color="error">
                            Lost Item
                        </ToggleButton>
                        <ToggleButton value="FOUND" color="success">
                            Found Item
                        </ToggleButton>
                    </ToggleButtonGroup>

                    <TextField
                        label="Title"
                        name="title"
                        value={form.title}
                        onChange={handleChange}
                        placeholder="e.g. Black Wallet"
                        fullWidth
                        required
                        disabled={submitting}
                    />

                    <TextField
                        label="Category"
                        name="category"
                        value={form.category}
                        onChange={handleChange}
                        placeholder="e.g. Wallet, Backpack, Electronics"
                        fullWidth
                        required
                        disabled={submitting}
                    />

                    <TextField
                        label="Description"
                        name="description"
                        value={form.description}
                        onChange={handleChange}
                        placeholder="Describe the item in detail…"
                        fullWidth
                        required
                        multiline
                        minRows={3}
                        disabled={submitting}
                    />

                    <Grid container spacing={2}>
                        <Grid item xs={6}>
                            <TextField
                                label="Brand"
                                name="brand"
                                value={form.brand}
                                onChange={handleChange}
                                placeholder="e.g. Tommy Hilfiger"
                                fullWidth
                                disabled={submitting}
                            />
                        </Grid>
                        <Grid item xs={6}>
                            <TextField
                                label="Color"
                                name="color"
                                value={form.color}
                                onChange={handleChange}
                                placeholder="e.g. Black"
                                fullWidth
                                disabled={submitting}
                            />
                        </Grid>
                    </Grid>

                    <Grid container spacing={2}>
                        <Grid item xs={7}>
                            <TextField
                                label="Location"
                                name="locationDescription"
                                value={form.locationDescription}
                                onChange={handleChange}
                                placeholder="e.g. FC Road, Pune"
                                fullWidth
                                required
                                disabled={submitting}
                            />
                        </Grid>
                        <Grid item xs={5}>
                            <TextField
                                label={form.itemType === 'LOST' ? 'Lost Date' : 'Found Date'}
                                name="itemDate"
                                type="date"
                                value={form.itemDate}
                                onChange={handleChange}
                                fullWidth
                                required
                                disabled={submitting}
                                InputLabelProps={{ shrink: true }}
                                inputProps={{ max: new Date().toISOString().split('T')[0] }}
                            />
                        </Grid>
                    </Grid>

                    {form.itemType === 'FOUND' && (
                        <Box>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                <Typography variant="body2" fontWeight={600}>
                                    Ownership Verification Questions
                                    <Typography component="span" variant="caption" color="text.secondary" sx={{ ml: 1 }}>
                                        (3–5 required — used to verify the claimant)
                                    </Typography>
                                </Typography>
                                <Button
                                    size="small"
                                    startIcon={<AddIcon />}
                                    onClick={addQuestion}
                                    disabled={questions.length >= 5 || submitting}
                                >
                                    Add
                                </Button>
                            </Box>

                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                                {questions.map((q, idx) => (
                                    <Box key={q.key} sx={{ display: 'flex', gap: 1, alignItems: 'flex-start' }}>
                                        <Typography variant="body2" color="text.secondary" sx={{ pt: 1.5, minWidth: 18 }}>
                                            {idx + 1}.
                                        </Typography>
                                        <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', gap: 1 }}>
                                            <TextField
                                                size="small"
                                                placeholder="Question (e.g. What was inside the wallet?)"
                                                value={q.questionText}
                                                onChange={(e) => updateQuestion(q.key, 'questionText', e.target.value)}
                                                disabled={submitting}
                                                fullWidth
                                            />
                                            <TextField
                                                size="small"
                                                placeholder="Expected answer"
                                                value={q.expectedAnswer}
                                                onChange={(e) => updateQuestion(q.key, 'expectedAnswer', e.target.value)}
                                                disabled={submitting}
                                                fullWidth
                                            />
                                        </Box>
                                        <IconButton
                                            size="small"
                                            onClick={() => removeQuestion(q.key)}
                                            disabled={questions.length <= 3 || submitting}
                                            sx={{ mt: 0.5 }}
                                        >
                                            <DeleteOutlineIcon fontSize="small" />
                                        </IconButton>
                                    </Box>
                                ))}
                            </Box>
                        </Box>
                    )}

                    <ImageUploader files={images} onAdd={handleAddImages} onRemove={handleRemoveImage} disabled={submitting} />
                </DialogContent>

                <DialogActions sx={{ px: 3, py: 2 }}>
                    <Button onClick={onClose} disabled={submitting}>
                        Cancel
                    </Button>
                    <Button type="submit" variant="contained" disabled={submitting}>
                        {submitting ? 'Submitting…' : 'Submit'}
                    </Button>
                </DialogActions>
            </Box>
        </Dialog>
    );
}