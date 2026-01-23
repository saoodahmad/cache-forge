import type { ReactElement } from "react";
import { motion, AnimatePresence } from "framer-motion";

import type { tLRU, tStripe } from "../types/types";

const StripeCard = ({
  stripes,
  scrollToElement,
}: {
  stripes: tStripe[];
  scrollToElement: (id: string) => void;
}): ReactElement => {
  return (
    <div className="grid grid-cols-1 xl:grid-cols-2 gap-3">
      {stripes.map((stripe: tStripe) => {
        return (
          <div
            key={stripe.id}
            className="bg-slate-950/40 border border-white/5 rounded-lg p-3"
          >
            <div className="flex justify-between items-center mb-2 border-b border-white/5 pb-1">
              <span className="text-[9px] font-bold text-indigo-400 uppercase tracking-tighter">
                Stripe #{stripe.id.toString(16).padStart(2, "0")}
              </span>
              <div className="flex gap-2">
                <span className="text-[9px] text-slate-500 font-mono">
                  Occupancy : {stripe.keyCount}/{stripe.capacity}{" "}
                  {`(${Math.round((stripe.keyCount / stripe.capacity) * 100)}%)`}
                </span>
              </div>
            </div>
            <div className="flex flex-wrap gap-1 min-h-[28px]">
              <AnimatePresence mode="popLayout">
                {stripe.lru.map((elem: tLRU, idx: number) => (
                  <motion.div
                    layout
                    initial={{ opacity: 0, scale: 0.9 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={{ opacity: 0, scale: 0.9 }}
                    key={idx}
                    className={`px-1.5 py-0.5 rounded text-[10px] font-mono border cursor-pointer
                      ${
                        idx === 0
                          ? "border-red-500/30 bg-red-500/10 text-red-400 hover:border-red-500/50"
                          : idx === stripe.lru.length - 1
                            ? "border-emerald-500/30 bg-emerald-500/10 text-emerald-400 hover:border-emerald-500/50"
                            : "border-white/10 bg-slate-900 text-slate-400 hover:border-indigo-500/50"
                      }`}
                    onClick={() =>
                      scrollToElement(`${elem.namespace}-${elem.key}`)
                    }
                  >
                    {elem.namespace}:{elem.key} <br />
                    <span className="text-[8px]">
                      {idx === 0
                        ? "Next to evict"
                        : idx === stripe.lru.length - 1
                          ? "Most Recently Used"
                          : ""}
                    </span>
                  </motion.div>
                ))}
              </AnimatePresence>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default StripeCard;
