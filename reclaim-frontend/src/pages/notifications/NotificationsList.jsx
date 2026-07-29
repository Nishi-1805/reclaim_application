import { useCallback, useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Paper from '@mui/material/Paper';
import Stack from '@mui/material/Stack';
import Button from '@mui/material/Button';
import Skeleton from '@mui/material/Skeleton';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import NotificationsActiveOutlinedIcon from '@mui/icons-material/NotificationsActiveOutlined';
import NotificationsNoneOutlinedIcon from '@mui/icons-material/NotificationsNoneOutlined';

import EmptyState from '../../components/common/EmptyState';
import { getMyNotifications, markAsRead, markAllAsRead } from '../../api/notifications';

export default function NotificationsList() {
  const outletContext = useOutletContext?.() || {};
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [toast, setToast] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getMyNotifications();
      setNotifications(data);
    } catch (err) {
      setToast(err.message || 'Failed to load notifications.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleMarkRead = async (id) => {
    setNotifications((prev) => prev.map((n) => (n.notificationId === id ? { ...n, isRead: true } : n)));
    try {
      await markAsRead(id);
      outletContext.setUnreadCount?.((c) => Math.max(0, c - 1));
    } catch (err) {
      setToast(err.message || 'Failed to mark as read.');
    }
  };

  const handleMarkAll = async () => {
    const previous = notifications;
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    try {
      await markAllAsRead();
      outletContext.setUnreadCount?.(0);
    } catch (err) {
      setNotifications(previous);
      setToast(err.message || 'Failed to mark all as read.');
    }
  };

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <Box sx={{ pt: 1 }}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }} flexWrap="wrap" gap={1.5}>
        <Box>
          <Typography variant="h5" fontWeight={800}>
            Notifications
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Stay updated with the latest activity.
          </Typography>
        </Box>
        {unreadCount > 0 && (
          <Button size="small" onClick={handleMarkAll}>
            Mark all as read
          </Button>
        )}
      </Stack>

      <Paper elevation={0} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        {loading ? (
          <Box sx={{ p: 2 }}>
            {[1, 2, 3, 4].map((i) => (
              <Skeleton key={i} variant="rounded" height={56} sx={{ mb: 1.5 }} />
            ))}
          </Box>
        ) : notifications.length === 0 ? (
          <Box sx={{ p: 2 }}>
            <EmptyState title="You're all caught up" description="New notifications will show up here." />
          </Box>
        ) : (
          notifications.map((n) => (
            <Stack
              key={n.notificationId}
              direction="row"
              alignItems="flex-start"
              spacing={2}
              onClick={() => !n.isRead && handleMarkRead(n.notificationId)}
              sx={{
                p: 2,
                borderBottom: '1px solid rgba(255,255,255,0.06)',
                '&:last-of-type': { borderBottom: 'none' },
                cursor: n.isRead ? 'default' : 'pointer',
                bgcolor: n.isRead ? 'transparent' : 'rgba(108,92,231,0.06)',
              }}
            >
              {n.isRead ? (
                <NotificationsNoneOutlinedIcon fontSize="small" sx={{ mt: 0.3, color: 'text.secondary' }} />
              ) : (
                <NotificationsActiveOutlinedIcon fontSize="small" sx={{ mt: 0.3, color: 'primary.light' }} />
              )}
              <Box sx={{ flexGrow: 1 }}>
                <Typography variant="body2" fontWeight={n.isRead ? 400 : 600}>
                  {n.message}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {new Date(n.createdAt).toLocaleString()}
                </Typography>
              </Box>
              {!n.isRead && <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: 'info.main', mt: 0.7 }} />}
            </Stack>
          ))
        )}
      </Paper>

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast('')}>
        <Alert severity="info" onClose={() => setToast('')} sx={{ width: '100%' }}>
          {toast}
        </Alert>
      </Snackbar>
    </Box>
  );
}