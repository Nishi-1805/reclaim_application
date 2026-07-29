import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import { Link as RouterLink } from 'react-router-dom';

export default function StatCard({ icon, iconBg, label, value, sublabel, linkTo, linkLabel }) {
  return (
    <Paper
      elevation={0}
      sx={{
        p: 2.5,
        borderRadius: 3,
        display: 'flex',
        flexDirection: 'column',
        gap: 1.5,
        height: '100%',
      }}
    >
      <Box
        sx={{
          width: 44,
          height: 44,
          borderRadius: 2.5,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: iconBg || 'primary.main',
          color: '#fff',
        }}
      >
        {icon}
      </Box>

      <Box>
        <Typography variant="body2" color="text.secondary">
          {label}
        </Typography>
        <Typography variant="h4" sx={{ mt: 0.5 }}>
          {value}
        </Typography>
        {sublabel && (
          <Typography variant="caption" color="text.secondary">
            {sublabel}
          </Typography>
        )}
      </Box>

      {linkTo && (
        <Button
          component={RouterLink}
          to={linkTo}
          size="small"
          endIcon={<ArrowForwardIcon fontSize="small" />}
          sx={{ alignSelf: 'flex-start', px: 0 }}
        >
          {linkLabel || 'View all'}
        </Button>
      )}
    </Paper>
  );
}
