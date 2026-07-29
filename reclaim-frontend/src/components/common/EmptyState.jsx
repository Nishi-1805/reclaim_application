import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import InboxIcon from '@mui/icons-material/InboxOutlined';

export default function EmptyState({ title = 'Nothing here yet', description, icon }) {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        py: 6,
        color: 'text.secondary',
        textAlign: 'center',
      }}
    >
      <Box sx={{ fontSize: 40, mb: 1.5, opacity: 0.6 }}>{icon || <InboxIcon fontSize="inherit" />}</Box>
      <Typography variant="subtitle1" color="text.primary">
        {title}
      </Typography>
      {description && (
        <Typography variant="body2" sx={{ mt: 0.5, maxWidth: 320 }}>
          {description}
        </Typography>
      )}
    </Box>
  );
}
