import { useEffect, useMemo, useState } from 'react';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Stack from '@mui/material/Stack';
import Skeleton from '@mui/material/Skeleton';
import { Link as RouterLink, useNavigate } from 'react-router-dom';

import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined';
import JoinInnerOutlinedIcon from '@mui/icons-material/JoinInnerOutlined';
import AssignmentOutlinedIcon from '@mui/icons-material/AssignmentOutlined';
import NotificationsNoneOutlinedIcon from '@mui/icons-material/NotificationsNoneOutlined';
import ReportGmailerrorredIcon from '@mui/icons-material/ReportGmailerrorredOutlined';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import SearchIcon from '@mui/icons-material/Search';
import NotificationsActiveOutlinedIcon from '@mui/icons-material/NotificationsActiveOutlined';

import StatCard from '../components/common/StatCard';
import StatusBadge from '../components/common/StatusBadge';
import EmptyState from '../components/common/EmptyState';
import { useAuth } from '../context/AuthContext';
import { getDashboard } from '../api/users';
import { getMyItems } from '../api/items';
import { getMatchesForLostItem, getMatchesForFoundItem } from '../api/matches';
import { getMyNotifications } from '../api/notifications';

function timeAgo(dateString) {
  if (!dateString) return '';
  const diffMs = Date.now() - new Date(dateString).getTime();
  const mins = Math.floor(diffMs / 60000);
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [stats, setStats] = useState(null);
  const [myItems, setMyItems] = useState([]);
  const [recentMatches, setRecentMatches] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setLoading(true);
      try {
        const [dashboardStats, items, myNotifications] = await Promise.all([
          getDashboard(),
          getMyItems(),
          getMyNotifications(),
        ]);

        if (cancelled) return;
        setStats(dashboardStats);
        setMyItems(items);
        setNotifications(myNotifications.slice(0, 4));

        // Aggregate matches across the user's own items (open items only,
        // capped to keep the number of requests small).
        const candidateItems = items.filter((i) => i.status === 'OPEN').slice(0, 6);
        const matchLists = await Promise.all(
          candidateItems.map((item) =>
            (item.itemType === 'LOST'
              ? getMatchesForLostItem(item.id)
              : getMatchesForFoundItem(item.id)
            )
              .then((matches) => matches.map((m) => ({ ...m, sourceItemTitle: item.title })))
              .catch(() => [])
          )
        );

        if (cancelled) return;
        const merged = matchLists
          .flat()
          .sort((a, b) => (b.matchScore || 0) - (a.matchScore || 0))
          .slice(0, 3);
        setRecentMatches(merged);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  const recentPosts = useMemo(() => myItems.slice(0, 3), [myItems]);

  return (
    <Box sx={{ pt: 1 }}>
      <Typography variant="h5" fontWeight={800}>
        Welcome back, {user?.fullName?.split(' ')[0] || 'there'}! 👋
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Here's what's happening with your account today.
      </Typography>

      {/* Stat cards */}
      <Grid container spacing={2.5} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          {loading ? (
            <Skeleton variant="rounded" height={150} />
          ) : (
            <StatCard
              icon={<Inventory2OutlinedIcon />}
              iconBg="#6C5CE7"
              label="My Posts"
              value={stats?.totalItems ?? 0}
              sublabel="Active"
              linkTo="/items"
            />
          )}
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          {loading ? (
            <Skeleton variant="rounded" height={150} />
          ) : (
            <StatCard
              icon={<JoinInnerOutlinedIcon />}
              iconBg="#22C55E"
              label="Matches Found"
              value={recentMatches.length}
              sublabel="New"
              linkTo="/matches"
            />
          )}
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          {loading ? (
            <Skeleton variant="rounded" height={150} />
          ) : (
            <StatCard
              icon={<AssignmentOutlinedIcon />}
              iconBg="#F5A524"
              label="Claims"
              value={stats?.activeClaims ?? 0}
              sublabel="Pending"
              linkTo="/claims"
            />
          )}
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          {loading ? (
            <Skeleton variant="rounded" height={150} />
          ) : (
            <StatCard
              icon={<NotificationsNoneOutlinedIcon />}
              iconBg="#3B82F6"
              label="Notifications"
              value={notifications.filter((n) => !n.isRead).length}
              sublabel="Unread"
              linkTo="/notifications"
            />
          )}
        </Grid>
      </Grid>

      <Grid container spacing={2.5} sx={{ mb: 3 }}>
        {/* Recent matches */}
        <Grid item xs={12} md={6}>
          <Paper elevation={0} sx={{ p: 2.5, borderRadius: 3, height: '100%' }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1.5 }}>
              <Typography variant="subtitle1" fontWeight={700}>
                Recent Matches
              </Typography>
              <Button component={RouterLink} to="/matches" size="small">
                View all matches
              </Button>
            </Stack>

            {loading ? (
              <Stack spacing={1.5}>
                {[1, 2, 3].map((i) => (
                  <Skeleton key={i} variant="rounded" height={56} />
                ))}
              </Stack>
            ) : recentMatches.length === 0 ? (
              <EmptyState
                title="No matches yet"
                description="We'll notify you here as soon as a potential match is found for one of your items."
              />
            ) : (
              <Stack spacing={1.5}>
                {recentMatches.map((m) => (
                  <Stack
                    key={m.itemMatchId}
                    direction="row"
                    alignItems="center"
                    spacing={1.5}
                    sx={{ p: 1, borderRadius: 2, '&:hover': { bgcolor: 'rgba(255,255,255,0.03)' } }}
                  >
                    <Avatar variant="rounded" sx={{ bgcolor: 'grey.800', width: 44, height: 44 }}>
                      <Inventory2Icon fontSize="small" />
                    </Avatar>
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography variant="body2" fontWeight={600}>
                        {m.itemTitle}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Matched with {m.sourceItemTitle}
                      </Typography>
                    </Box>
                    <Typography variant="body2" fontWeight={700} color="success.main">
                      {Math.round(m.matchScore)}% Match
                    </Typography>
                  </Stack>
                ))}
              </Stack>
            )}
          </Paper>
        </Grid>

        {/* Recent notifications */}
        <Grid item xs={12} md={6}>
          <Paper elevation={0} sx={{ p: 2.5, borderRadius: 3, height: '100%' }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1.5 }}>
              <Typography variant="subtitle1" fontWeight={700}>
                Recent Notifications
              </Typography>
              <Button component={RouterLink} to="/notifications" size="small">
                View all notifications
              </Button>
            </Stack>

            {loading ? (
              <Stack spacing={1.5}>
                {[1, 2, 3].map((i) => (
                  <Skeleton key={i} variant="rounded" height={48} />
                ))}
              </Stack>
            ) : notifications.length === 0 ? (
              <EmptyState title="You're all caught up" description="New notifications will show up here." />
            ) : (
              <Stack spacing={0.5}>
                {notifications.map((n) => (
                  <Stack
                    key={n.notificationId}
                    direction="row"
                    alignItems="flex-start"
                    spacing={1.5}
                    sx={{ p: 1, borderRadius: 2 }}
                  >
                    <NotificationsActiveOutlinedIcon
                      fontSize="small"
                      sx={{ mt: 0.3, color: n.isRead ? 'text.secondary' : 'primary.light' }}
                    />
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography variant="body2">{n.message}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {timeAgo(n.createdAt)}
                      </Typography>
                    </Box>
                    {!n.isRead && (
                      <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: 'info.main', mt: 0.7 }} />
                    )}
                  </Stack>
                ))}
              </Stack>
            )}
          </Paper>
        </Grid>
      </Grid>

      <Grid container spacing={2.5}>
        {/* My recent posts */}
        <Grid item xs={12} md={7}>
          <Paper elevation={0} sx={{ p: 2.5, borderRadius: 3, height: '100%' }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1.5 }}>
              <Typography variant="subtitle1" fontWeight={700}>
                My Recent Posts
              </Typography>
              <Button component={RouterLink} to="/items" size="small">
                View all posts
              </Button>
            </Stack>

            {loading ? (
              <Grid container spacing={1.5}>
                {[1, 2, 3].map((i) => (
                  <Grid item xs={12} sm={4} key={i}>
                    <Skeleton variant="rounded" height={140} />
                  </Grid>
                ))}
              </Grid>
            ) : recentPosts.length === 0 ? (
              <EmptyState
                title="No posts yet"
                description="Report a lost or found item to get started."
              />
            ) : (
              <Grid container spacing={1.5}>
                {recentPosts.map((item) => (
                  <Grid item xs={12} sm={4} key={item.id}>
                    <Paper
                      variant="outlined"
                      sx={{ p: 1.5, borderRadius: 2.5, height: '100%', cursor: 'pointer' }}
                      onClick={() => navigate(`/items/${item.id}`)}
                    >
                      <Stack direction="row" justifyContent="space-between" sx={{ mb: 1 }}>
                        <StatusBadge status={item.itemType} />
                        <StatusBadge status={item.status} />
                      </Stack>
                      <Typography variant="body2" fontWeight={600} noWrap>
                        {item.title}
                      </Typography>
                      <Typography variant="caption" color="text.secondary" noWrap>
                        {item.locationDescription}
                      </Typography>
                    </Paper>
                  </Grid>
                ))}
              </Grid>
            )}
          </Paper>
        </Grid>

        {/* Quick actions */}
        <Grid item xs={12} md={5}>
          <Paper elevation={0} sx={{ p: 2.5, borderRadius: 3, height: '100%' }}>
            <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1.5 }}>
              Quick Actions
            </Typography>
            <Grid container spacing={1.5}>
              <Grid item xs={6}>
                <QuickAction
                  icon={<ReportGmailerrorredIcon />}
                  color="#6C5CE7"
                  label="Report Lost Item"
                  onClick={() => navigate('/items', { state: { openReport: 'LOST' } })}
                />
              </Grid>
              <Grid item xs={6}>
                <QuickAction
                  icon={<Inventory2Icon />}
                  color="#22C55E"
                  label="Report Found Item"
                  onClick={() => navigate('/items', { state: { openReport: 'FOUND' } })}
                />
              </Grid>
              <Grid item xs={6}>
                <QuickAction
                  icon={<SearchIcon />}
                  color="#F5A524"
                  label="Browse Items"
                  onClick={() => navigate('/items')}
                />
              </Grid>
              <Grid item xs={6}>
                <QuickAction
                  icon={<NotificationsNoneOutlinedIcon />}
                  color="#3B82F6"
                  label="View Notifications"
                  onClick={() => navigate('/notifications')}
                />
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}

function QuickAction({ icon, color, label, onClick }) {
  return (
    <Paper
      variant="outlined"
      onClick={onClick}
      sx={{
        p: 2,
        borderRadius: 2.5,
        cursor: 'pointer',
        display: 'flex',
        flexDirection: 'column',
        gap: 1,
        height: '100%',
        transition: 'transform 0.15s ease, border-color 0.15s ease',
        '&:hover': { borderColor: color, transform: 'translateY(-2px)' },
      }}
    >
      <Box
        sx={{
          width: 36,
          height: 36,
          borderRadius: 2,
          bgcolor: color,
          color: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {icon}
      </Box>
      <Typography variant="body2" fontWeight={600}>
        {label}
      </Typography>
    </Paper>
  );
}
