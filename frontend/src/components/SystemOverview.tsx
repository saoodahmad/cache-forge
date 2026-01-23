import type { ReactElement } from "react";
import type { tCacheStoreEntry } from "../types/types";

const SystemOverview = ({
  data,
  namespaces,
}: {
  data: tCacheStoreEntry;
  namespaces: string[];
}): ReactElement => {
  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
      {[
        { l: "Total Capacity", v: data.capacity },
        { l: "Keys Count", v: data.allKeys.length },
        { l: "Stripe count", v: data.stripes.length },
        { l: "Active Namespaces", v: namespaces.length },
      ].map((s, i) => (
        <div
          key={i}
          className="bg-slate-900 border border-white/5 p-3 rounded-xl"
        >
          <p className="text-[9px] font-black text-slate-500 uppercase mb-1">
            {s.l}
          </p>
          <p className="text-lg font-mono font-bold text-white tracking-tighter">
            {s.v}
          </p>
        </div>
      ))}
    </div>
  );
};

export default SystemOverview;
