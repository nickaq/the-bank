"use client";

import { motion } from "framer-motion";

interface LogoProps {
  size?: "sm" | "md" | "lg" | "xl";
  animated?: boolean;
}

const sizeMap = {
  sm: { container: 60, text: "text-2xl" },
  md: { container: 80, text: "text-3xl" },
  lg: { container: 120, text: "text-5xl" },
  xl: { container: 160, text: "text-7xl" },
};

export default function Logo({ size = "lg", animated = true }: LogoProps) {
  const { container, text } = sizeMap[size];

  const logoVariants = {
    hidden: { scale: 0.8, opacity: 0 },
    visible: {
      scale: 1,
      opacity: 1,
      transition: {
        duration: 0.8,
        ease: "easeOut" as const,
      },
    },
  };

  const Wrapper = animated ? motion.div : "div";
  const wrapperProps = animated
    ? { variants: logoVariants, initial: "hidden", animate: "visible" }
    : {};

  return (
    <Wrapper {...wrapperProps} className="relative">
      {/* Glow effect */}
      <motion.div
        className="absolute inset-0 rounded-2xl bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 blur-xl opacity-50"
        animate={{
          scale: [1, 1.1, 1],
          opacity: [0.3, 0.5, 0.3],
        }}
        transition={{
          duration: 3,
          repeat: Infinity,
          ease: "easeInOut",
        }}
      />

      {/* Main logo container */}
      <div
        className="relative flex items-center justify-center rounded-2xl bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 border border-slate-700/50 backdrop-blur-sm"
        style={{ width: container, height: container }}
      >
        {/* Inner gradient */}
        <div className="absolute inset-[2px] rounded-xl bg-gradient-to-br from-slate-800/80 to-slate-900/80" />

        {/* Z letter */}
        <span
          className={`relative ${text} font-black bg-gradient-to-r from-indigo-400 via-purple-400 to-pink-400 bg-clip-text text-transparent drop-shadow-lg`}
        >
          Z
        </span>

        {/* Decorative corner accents */}
        <div className="absolute top-2 left-2 w-3 h-3 border-l-2 border-t-2 border-indigo-500/50 rounded-tl-sm" />
        <div className="absolute top-2 right-2 w-3 h-3 border-r-2 border-t-2 border-purple-500/50 rounded-tr-sm" />
        <div className="absolute bottom-2 left-2 w-3 h-3 border-l-2 border-b-2 border-purple-500/50 rounded-bl-sm" />
        <div className="absolute bottom-2 right-2 w-3 h-3 border-r-2 border-b-2 border-pink-500/50 rounded-br-sm" />
      </div>
    </Wrapper>
  );
}
