import React from 'react';
import { Skeleton } from '../Skeleton';

export const CheckoutSkeleton: React.FC = () => {
  return (
    <div className="page-content">
      <div className="container">
        <Skeleton variant="text" width="220px" height="32px" style={{ marginBottom: '8px' }} />
        <Skeleton variant="text" width="340px" height="18px" style={{ marginBottom: '32px' }} />

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: '32px' }}>
          {/* Main Form Col */}
          <div>
            {/* Address Card */}
            <div className="card" style={{ marginBottom: '24px' }}>
              <Skeleton variant="text" width="200px" height="24px" style={{ marginBottom: '16px' }} />
              <Skeleton variant="rectangular" width="100%" height="80px" borderRadius={8} />
            </div>

            {/* Payment Method Card */}
            <div className="card">
              <Skeleton variant="text" width="180px" height="24px" style={{ marginBottom: '16px' }} />
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '12px', marginBottom: '20px' }}>
                <Skeleton variant="rectangular" height="52px" borderRadius={8} />
                <Skeleton variant="rectangular" height="52px" borderRadius={8} />
                <Skeleton variant="rectangular" height="52px" borderRadius={8} />
                <Skeleton variant="rectangular" height="52px" borderRadius={8} />
              </div>
              <Skeleton variant="rectangular" height="42px" borderRadius={6} style={{ marginBottom: '12px' }} />
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <Skeleton variant="rectangular" height="42px" borderRadius={6} />
                <Skeleton variant="rectangular" height="42px" borderRadius={6} />
              </div>
            </div>
          </div>

          {/* Sidebar Summary */}
          <div>
            <div className="card">
              <Skeleton variant="text" width="160px" height="24px" style={{ marginBottom: '20px' }} />
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Skeleton variant="text" width="120px" height="18px" />
                  <Skeleton variant="text" width="60px" height="18px" />
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Skeleton variant="text" width="140px" height="18px" />
                  <Skeleton variant="text" width="60px" height="18px" />
                </div>
              </div>
              <div style={{ height: '1px', background: 'var(--color-border)', margin: '16px 0' }} />
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
                <Skeleton variant="text" width="100px" height="22px" />
                <Skeleton variant="text" width="90px" height="24px" />
              </div>
              <Skeleton variant="rectangular" width="100%" height="48px" borderRadius={8} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
