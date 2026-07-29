import { useEffect, useState } from 'react';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import { Outlet } from 'react-router-dom';
import Sidebar, { DRAWER_WIDTH } from './Sidebar';
import Topbar from './Topbar';
import { getUnreadCount } from '../../api/notifications';

export default function DashboardLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    let cancelled = false;

    const fetchCount = () => {
      getUnreadCount()
        .then((count) => {
          if (!cancelled) setUnreadCount(count || 0);
        })
        .catch(() => {});
    };

    fetchCount();
    const interval = setInterval(fetchCount, 30000);
    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, []);

  return (
    <Box sx={{ display: 'flex' }}>
      <Topbar onMenuClick={() => setMobileOpen(true)} unreadCount={unreadCount} />
      <Sidebar
        mobileOpen={mobileOpen}
        onClose={() => setMobileOpen(false)}
        unreadCount={unreadCount}
      />

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          minHeight: '100vh',
          bgcolor: 'background.default',
          px: { xs: 2, sm: 3, md: 4 },
          pb: 4,
        }}
      >
        <Toolbar />
        <Outlet context={{ unreadCount, setUnreadCount }} />
      </Box>
    </Box>
  );
}
