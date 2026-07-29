import Chip from '@mui/material/Chip';

// Maps every status-like enum actually returned by the backend
// (ItemStatus, ItemType, ClaimStatus, MatchStatus, AccountStatus)
// to a consistent color so the same word always reads the same way.
const COLOR_MAP = {
  // ItemStatus
  OPEN: 'info',
  CLOSED: 'default',
  CANCELLED: 'error', // also ClaimStatus's "withdrawn" equivalent

  // ItemType
  LOST: 'error',
  FOUND: 'success',

  // ClaimStatus / MatchStatus
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  CONFIRMED: 'success',

  // AccountStatus (no SUSPENDED in this backend — only these three)
  ACTIVE: 'success',
  INACTIVE: 'default',
  DEACTIVATED: 'error',
};

export default function StatusBadge({ status, size = 'small' }) {
  if (!status) return null;
  const color = COLOR_MAP[status] || 'default';

  return (
    <Chip
      label={status}
      color={color}
      size={size}
      variant={color === 'default' ? 'outlined' : 'filled'}
      sx={{ letterSpacing: 0.4 }}
    />
  );
}