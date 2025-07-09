/**
 * PageFadeIn
 * Wrapper component that animates its children with a smooth fade-in effect.
 * Use it to make any page or section enter with a professional transition.
 *
 * Props:
 * - children: ReactNode (the content to animate)
 * - duration: number (milliseconds, optional, default 700ms)
 * - className: string (optional extra Tailwind classes)
 *
 * Usage:
 * <PageFadeIn>
 *   <DashboardPage />
 * </PageFadeIn>
 */

import React from "react";

export default function PageFadeIn({
  children,
  duration = 700, // default duration in ms
  className = "",
}) {
  return (
    <div
      className={`opacity-0 animate-fadeInPage ${className}`}
      style={{
        animationDuration: `${duration}ms`,
        animationTimingFunction: "cubic-bezier(0.4, 0, 0.2, 1)",
        animationFillMode: "forwards",
      }}
    >
      {children}
      {/* Animation CSS (can go global, or keep inline for isolation) */}
      <style>{`
        @keyframes fadeInPage {
          from { opacity: 0; transform: translateY(24px);}
          to   { opacity: 1; transform: translateY(0);}
        }
        .animate-fadeInPage {
          animation-name: fadeInPage;
        }
      `}</style>
    </div>
  );
}
