import { useRef } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import CircularProgress from '@mui/material/CircularProgress';
import AddPhotoAlternateOutlinedIcon from '@mui/icons-material/AddPhotoAlternateOutlined';
import CloseIcon from '@mui/icons-material/Close';

const MAX_IMAGES = 3;
const ACCEPTED_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif'];
const MAX_SIZE_BYTES = 5 * 1024 * 1024;

/**
 * files: array of { id, file?, previewUrl, uploading?, uploaded? (existing image with imageUrl+itemImageId) }
 * onAdd(fileList), onRemove(index)
 */
export default function ImageUploader({ files, onAdd, onRemove, error, disabled }) {
    const inputRef = useRef(null);

    const handleFiles = (fileList) => {
        const incoming = Array.from(fileList);
        const validationErrors = [];
        const accepted = [];

        for (const file of incoming) {
            if (!ACCEPTED_TYPES.includes(file.type)) {
                validationErrors.push(`${file.name}: unsupported file type`);
                continue;
            }
            if (file.size > MAX_SIZE_BYTES) {
                validationErrors.push(`${file.name}: exceeds 5MB`);
                continue;
            }
            accepted.push(file);
        }

        if (files.length + accepted.length > MAX_IMAGES) {
            accepted.splice(MAX_IMAGES - files.length);
        }

        if (accepted.length) onAdd(accepted);
        if (validationErrors.length) {
            // Surfaced via parent's error prop is preferred, but as a fallback:
            // eslint-disable-next-line no-alert
            console.warn(validationErrors.join('\n'));
        }
    };

    return (
        <Box>
            <Typography variant="body2" fontWeight={600} sx={{ mb: 1 }}>
                Upload Images
                <Typography component="span" variant="caption" color="text.secondary" sx={{ ml: 1 }}>
                    (Max {MAX_IMAGES} images, 5MB each)
                </Typography>
            </Typography>

            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5 }}>
                {files.map((f, idx) => (
                    <Box
                        key={f.id}
                        sx={{
                            position: 'relative',
                            width: 92,
                            height: 92,
                            borderRadius: 2,
                            overflow: 'hidden',
                            border: '1px solid rgba(255,255,255,0.1)',
                        }}
                    >
                        <Box
                            component="img"
                            src={f.previewUrl}
                            alt=""
                            sx={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
                        />
                        {f.uploading && (
                            <Box
                                sx={{
                                    position: 'absolute',
                                    inset: 0,
                                    bgcolor: 'rgba(0,0,0,0.55)',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                }}
                            >
                                <CircularProgress size={22} />
                            </Box>
                        )}
                        {!disabled && (
                            <IconButton
                                size="small"
                                onClick={() => onRemove(idx)}
                                sx={{
                                    position: 'absolute',
                                    top: 2,
                                    right: 2,
                                    bgcolor: 'rgba(0,0,0,0.6)',
                                    '&:hover': { bgcolor: 'rgba(0,0,0,0.8)' },
                                    width: 22,
                                    height: 22,
                                }}
                            >
                                <CloseIcon sx={{ fontSize: 14, color: '#fff' }} />
                            </IconButton>
                        )}
                    </Box>
                ))}

                {files.length < MAX_IMAGES && !disabled && (
                    <Box
                        onClick={() => inputRef.current?.click()}
                        sx={{
                            width: 92,
                            height: 92,
                            borderRadius: 2,
                            border: '1px dashed rgba(255,255,255,0.25)',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: 0.5,
                            cursor: 'pointer',
                            color: 'text.secondary',
                            '&:hover': { borderColor: 'primary.main', color: 'primary.light' },
                        }}
                    >
                        <AddPhotoAlternateOutlinedIcon fontSize="small" />
                        <Typography variant="caption">Add Images</Typography>
                    </Box>
                )}

                <input
                    ref={inputRef}
                    type="file"
                    accept={ACCEPTED_TYPES.join(',')}
                    multiple
                    hidden
                    onChange={(e) => {
                        if (e.target.files?.length) handleFiles(e.target.files);
                        e.target.value = '';
                    }}
                />
            </Box>

            {error && (
                <Typography variant="caption" color="error" sx={{ mt: 1, display: 'block' }}>
                    {error}
                </Typography>
            )}
        </Box>
    );
}