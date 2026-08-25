import React from 'react';
import { Skeleton } from '../Skeleton';

export const ProfileSkeleton: React.FC = () => {
  return (
    <div style={{ maxWidth: '1080px', margin: '0 auto', padding: '100px 24px 48px' }}>
      <div style={{ display: 'grid', gridTemplateColumns: '260px 1fr', gap: '32px' }}>
        {/* Sidebar */}
        <div style={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: '12px', padding: '24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '24px' }}>
            <Skeleton variant="circular" width={52} height={52} />
            <div>
              <Skeleton variant="text" width="60px" height="14px" />
              <Skeleton variant="text" width="120px" height="20px" />
            </div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <Skeleton variant="rectangular" width="100%" height="40px" borderRadius={8} />
            <Skeleton variant="rectangular" width="100%" height="40px" borderRadius={8} />
            <Skeleton variant="rectangular" width="100%" height="40px" borderRadius={8} />
          </div>
        </div>

        {/* Main Content */}
        <div>
          <Skeleton variant="text" width="200px" height="28px" style={{ marginBottom: '8px' }} />
          <Skeleton variant="text" width="300px" height="16px" style={{ marginBottom: '24px' }} />

          <div style={{ background: 'var(--color-surface)', border: '1px solid var(--color-border)', borderRadius: '12px', padding: '24px' }}>
            <Skeleton variant="text" width="160px" height="20px" style={{ marginBottom: '20px' }} />
            <Skeleton variant="rectangular" width="100%" height="44px" borderRadius={6} style={{ marginBottom: '16px' }} />
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
              <Skeleton variant="rectangular" height="44px" borderRadius={6} />
              <Skeleton variant="rectangular" height="44px" borderRadius={6} />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '24px' }}>
              <Skeleton variant="rectangular" height="44px" borderRadius={6} />
              <Skeleton variant="rectangular" height="44px" borderRadius={6} />
            </div>
            <Skeleton variant="rectangular" width="160px" height="42px" borderRadius={8} />
          </div>
        </div>
      </div>
    </div>
  );
};
