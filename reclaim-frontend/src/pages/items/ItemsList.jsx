import { useCallback, useEffect, useMemo, useState } from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import Paper from '@mui/material/Paper';
import Avatar from '@mui/material/Avatar';
import IconButton from '@mui/material/IconButton';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Skeleton from '@mui/material/Skeleton';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import { useLocation, useNavigate } from 'react-router-dom';

import { useAuth } from '../../context/AuthContext';
import StatusBadge from '../../components/common/StatusBadge';
import EmptyState from '../../components/common/EmptyState';
import ConfirmDialog from '../../components/common/ConfirmDialog';
import ReportItemModal from './ReportItemModal';
import EditItemModal from './EditItemModal';
import { getMyItems, getItemsByType, deleteItem } from '../../api/items';

const TABS = ['MY_POSTS', 'LOST', 'FOUND'];

export default function ItemsList() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [tab, setTab] = useState(0);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [reportOpen, setReportOpen] = useState(false);
  const [reportType, setReportType] = useState('LOST');
  const [editItem, setEditItem] = useState(null);
  const [cancelItem, setCancelItem] = useState(null);
  const [cancelling, setCancelling] = useState(false);
  const [menuAnchor, setMenuAnchor] = useState(null);
  const [menuItem, setMenuItem] = useState(null);
  const [toast, setToast] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const key = TABS[tab];
      const data = key === 'MY_POSTS' ? await getMyItems() : await getItemsByType(key);
      setItems(data);
    } catch (err) {
      setToast(err.message || 'Failed to load items.');
    } finally {
      setLoading(false);
    }
  }, [tab]);

  useEffect(() => {
    load();
  }, [load]);

  // Auto-open the report modal when navigated here from Dashboard quick actions
  useEffect(() => {
    if (location.state?.openReport) {
      setReportType(location.state.openReport);
      setReportOpen(true);
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  const openMenu = (e, item) => {
    setMenuAnchor(e.currentTarget);
    setMenuItem(item);
  };
  const closeMenu = () => {
    setMenuAnchor(null);
    setMenuItem(null);
  };

  const isOwner = (item) => item.ownerId === user?.userId;
  const canManage = (item) => isOwner(item) && item.status === 'OPEN';

  const handleCancelConfirm = async () => {
    setCancelling(true);
    try {
      await deleteItem(cancelItem.id);
      setToast('Item cancelled successfully.');
      setCancelItem(null);
      load();
    } catch (err) {
      setToast(err.message || 'Failed to cancel item.');
    } finally {
      setCancelling(false);
    }
  };

  const emptyCopy = useMemo(() => {
    switch (TABS[tab]) {
      case 'MY_POSTS':
        return { title: 'No posts yet', description: 'Report a lost or found item to get started.' };
      case 'LOST':
        return { title: 'No lost items reported', description: 'Nothing here right now.' };
      default:
        return { title: 'No found items reported', description: 'Nothing here right now.' };
    }
  }, [tab]);

  return (
    <Box sx={{ pt: 1 }}>
      <Stack
        direction="row"
        justifyContent="space-between"
        alignItems="center"
        sx={{ mb: 2 }}
        flexWrap="wrap"
        gap={1.5}
      >
        <Box>
          <Typography variant="h5" fontWeight={800}>
            Items
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Manage your lost and found posts.
          </Typography>
        </Box>
        <Stack direction="row" sx={{ gap: 1.5 }}>
          <Button
            variant="outlined"
            color="error"
            onClick={() => {
              setReportType('LOST');
              setReportOpen(true);
            }}
          >
            Report Lost Item
          </Button>
          <Button
            variant="contained"
            onClick={() => {
              setReportType('FOUND');
              setReportOpen(true);
            }}
          >
            Report Found Item
          </Button>
        </Stack>
      </Stack>

      <Tabs value={tab} onChange={(_e, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="My Posts" />
        <Tab label="Lost Items" />
        <Tab label="Found Items" />
      </Tabs>

      <Paper elevation={0} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        {loading ? (
          <Box sx={{ p: 2 }}>
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} variant="rounded" height={72} sx={{ mb: 1.5 }} />
            ))}
          </Box>
        ) : items.length === 0 ? (
          <Box sx={{ p: 2 }}>
            <EmptyState title={emptyCopy.title} description={emptyCopy.description} />
          </Box>
        ) : (
          <Box>
            {items.map((item) => (
              <Stack
                key={item.id}
                direction="row"
                alignItems="center"
                spacing={2}
                sx={{
                  p: 2,
                  borderBottom: '1px solid rgba(255,255,255,0.06)',
                  '&:last-of-type': { borderBottom: 'none' },
                  '&:hover': { bgcolor: 'rgba(255,255,255,0.02)' },
                  cursor: 'pointer',
                }}
                onClick={() => navigate(`/items/${item.id}`)}
              >
                <Avatar variant="rounded" src={item.imageUrls?.[0]} sx={{ width: 56, height: 56, bgcolor: 'grey.800' }}>
                  <Inventory2Icon />
                </Avatar>

                <Box sx={{ flexGrow: 1, minWidth: 0 }}>
                  <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.3 }}>
                    <StatusBadge status={item.itemType} />
                    <StatusBadge status={item.status} />
                  </Stack>
                  <Typography variant="body1" fontWeight={600} noWrap>
                    {item.title}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" noWrap>
                    {item.locationDescription} · {item.itemDate}
                    {TABS[tab] !== 'MY_POSTS' && item.ownerName ? ` · ${item.ownerName}` : ''}
                  </Typography>
                </Box>

                <IconButton
                  onClick={(e) => {
                    e.stopPropagation();
                    openMenu(e, item);
                  }}
                >
                  <MoreVertIcon />
                </IconButton>
              </Stack>
            ))}
          </Box>
        )}
      </Paper>

      <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={closeMenu}>
        <MenuItem
          onClick={() => {
            navigate(`/items/${menuItem.id}`);
            closeMenu();
          }}
        >
          View
        </MenuItem>
        {menuItem && canManage(menuItem)
          ? [
            <MenuItem
              key="edit"
              onClick={() => {
                setEditItem(menuItem);
                closeMenu();
              }}
            >
              Edit
            </MenuItem>,
            <MenuItem
              key="cancel"
              onClick={() => {
                setCancelItem(menuItem);
                closeMenu();
              }}
            >
              Cancel
            </MenuItem>,
          ]
          : null}
      </Menu>

      <ReportItemModal
        open={reportOpen}
        defaultType={reportType}
        onClose={() => setReportOpen(false)}
        onCreated={() => {
          setToast('Item reported successfully.');
          setTab(0);
          load();
        }}
      />

      <EditItemModal
        open={Boolean(editItem)}
        item={editItem}
        onClose={() => setEditItem(null)}
        onUpdated={() => {
          setToast('Item updated successfully.');
          load();
        }}
      />

      <ConfirmDialog
        open={Boolean(cancelItem)}
        title="Cancel this item?"
        description="This will mark the item as cancelled. This action cannot be undone."
        confirmLabel="Cancel Item"
        confirmColor="error"
        loading={cancelling}
        onConfirm={handleCancelConfirm}
        onClose={() => setCancelItem(null)}
      />

      <Snackbar open={Boolean(toast)} autoHideDuration={4000} onClose={() => setToast('')}>
        <Alert severity="info" onClose={() => setToast('')} sx={{ width: '100%' }}>
          {toast}
        </Alert>
      </Snackbar>
    </Box>
  );
}