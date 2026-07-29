import Box from '@mui/material/Box';
import Drawer from '@mui/material/Drawer';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import DashboardIcon from '@mui/icons-material/SpaceDashboardOutlined';
import ItemsIcon from '@mui/icons-material/Inventory2Outlined';
import MatchesIcon from '@mui/icons-material/JoinInnerOutlined';
import ClaimsIcon from '@mui/icons-material/AssignmentOutlined';
import NotificationsIcon from '@mui/icons-material/NotificationsNoneOutlined';
import ProfileIcon from '@mui/icons-material/PersonOutline';
import AdminIcon from '@mui/icons-material/AdminPanelSettingsOutlined';
import LogoutIcon from '@mui/icons-material/LogoutOutlined';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export const DRAWER_WIDTH = 260;

const NAV_ITEMS = [
  { label: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
  { label: 'Items', icon: <ItemsIcon />, path: '/items' },
  { label: 'Matches', icon: <MatchesIcon />, path: '/matches' },
  { label: 'Claims', icon: <ClaimsIcon />, path: '/claims' },
  { label: 'Notifications', icon: <NotificationsIcon />, path: '/notifications' },
  { label: 'Profile', icon: <ProfileIcon />, path: '/profile' },
];

export default function Sidebar({ mobileOpen, onClose, unreadCount = 0 }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { isAdmin, logout } = useAuth();

  const content = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ px: 3, py: 3, display: 'flex', alignItems: 'center', gap: 1.25 }}>
        <Box
          sx={{
            width: 36,
            height: 36,
            borderRadius: 2,
            bgcolor: 'primary.main',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 800,
            fontFamily: '"Plus Jakarta Sans", sans-serif',
          }}
        >
          R
        </Box>
        <Box>
          <Typography variant="subtitle1" fontWeight={800} lineHeight={1.1}>
            Reclaim
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Lost. Found. Reunited.
          </Typography>
        </Box>
      </Box>

      <Divider />

      <List sx={{ px: 1.5, py: 1.5, flexGrow: 1 }}>
        {NAV_ITEMS.map((item) => {
          const selected = location.pathname.startsWith(item.path);
          return (
            <ListItemButton
              key={item.path}
              selected={selected}
              onClick={() => {
                navigate(item.path);
                onClose?.();
              }}
              sx={{
                borderRadius: 2,
                mb: 0.5,
                '&.Mui-selected': {
                  bgcolor: 'primary.main',
                  color: '#fff',
                  '& .MuiListItemIcon-root': { color: '#fff' },
                  '&:hover': { bgcolor: 'primary.dark' },
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 38, color: 'text.secondary' }}>
                {item.icon}
              </ListItemIcon>
              <ListItemText primary={item.label} />
              {item.path === '/notifications' && unreadCount > 0 && !selected && (
                <Box
                  sx={{
                    bgcolor: 'error.main',
                    color: '#fff',
                    borderRadius: '50%',
                    minWidth: 20,
                    height: 20,
                    fontSize: 11,
                    fontWeight: 700,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    px: 0.5,
                  }}
                >
                  {unreadCount}
                </Box>
              )}
            </ListItemButton>
          );
        })}

        {isAdmin && (
          <>
            <Divider sx={{ my: 1.5 }} />
            <Typography
              variant="caption"
              color="text.secondary"
              sx={{ px: 2, textTransform: 'uppercase', letterSpacing: 1 }}
            >
              Admin
            </Typography>
            <ListItemButton
              selected={location.pathname.startsWith('/admin')}
              onClick={() => {
                navigate('/admin/users');
                onClose?.();
              }}
              sx={{
                borderRadius: 2,
                mt: 0.5,
                '&.Mui-selected': {
                  bgcolor: 'primary.main',
                  color: '#fff',
                  '& .MuiListItemIcon-root': { color: '#fff' },
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 38, color: 'text.secondary' }}>
                <AdminIcon />
              </ListItemIcon>
              <ListItemText primary="Users" />
            </ListItemButton>
          </>
        )}
      </List>

      <Divider />
      <List sx={{ px: 1.5, py: 1.5 }}>
        <ListItemButton onClick={logout} sx={{ borderRadius: 2 }}>
          <ListItemIcon sx={{ minWidth: 38, color: 'text.secondary' }}>
            <LogoutIcon />
          </ListItemIcon>
          <ListItemText primary="Logout" />
        </ListItemButton>
      </List>
    </Box>
  );

  return (
    <>
      {/* Permanent sidebar on desktop */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' },
        }}
        open
      >
        {content}
      </Drawer>

      {/* Temporary sidebar on mobile */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onClose}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' },
        }}
      >
        {content}
      </Drawer>
    </>
  );
}
