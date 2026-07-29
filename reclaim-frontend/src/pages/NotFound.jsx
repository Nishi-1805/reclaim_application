import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import { Link as RouterLink } from 'react-router-dom';

export default function NotFound() {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
      }}
    >
      <Typography variant="h2" fontWeight={800}>
        404
      </Typography>
      <Typography color="text.secondary">This page doesn't exist.</Typography>
      <Button component={RouterLink} to="/dashboard" variant="contained">
        Back to Dashboard
      </Button>
    </Box>
  );
}
