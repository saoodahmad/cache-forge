import type { ReactElement } from "react";
import { motion } from "framer-motion";

import type { tCacheStoreEntry, tKey } from "../types/types";

const KeyTable = ({
  data,
  activeKey,
}: {
  data: tCacheStoreEntry;
  activeKey: string | null;
}): ReactElement => {
  return (
    <div className="bg-slate-900 border border-white/5 rounded-xl shadow-2xl overflow-hidden">
      <div className="overflow-y-auto ">
        <table className="w-full text-left border-collapse">
          <thead className="sticky top-0 bg-slate-900 z-10 shadow-sm">
            <tr>
              {["NS", "Key", "Value", "Expired", "TTL", "Stripe"].map((h) => (
                <th
                  key={h}
                  className="px-4 py-2.5 text-[10px] font-black text-slate-500 uppercase tracking-widest border-b border-white/10"
                >
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="font-mono text-[11px]">
            {data.allKeys.length == 0 && (
              <motion.tr
                id={"empty"}
                key={`1`}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                className="hover:bg-white/5 transition-colors group"
              >
                <td className="px-4 py-2 border-b border-white/5 text-indigo-400">
                  {""}
                </td>
                <td className="px-4 py-2 border-b border-white/5 text-slate-200">
                  {""}
                </td>
                <td className="px-4 py-2 border-b border-white/5 text-slate-500 group-hover:text-slate-300 transition-colors truncate max-w-[200px]">
                  {""}
                </td>
                <td className="px-4 py-2 border-b border-white/5 text-slate-200 font-bold">
                  {""}
                </td>
                <td className="px-4 py-2 border-b border-white/5 text-slate-500">
                  {""}
                </td>
                <td className="px-4 py-2 border-b border-white/5 text-slate-600 font-bold">
                  {""}
                </td>
              </motion.tr>
            )}
            {data.allKeys.length != 0 &&
              data.allKeys.map((k: tKey) => (
                <motion.tr
                  id={`${k.namespace}-${k.key}`}
                  key={`${k.namespace}:${k.key}`}
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  className={
                    activeKey == `${k.namespace}-${k.key}`
                      ? "bg-white/10 transition-colors group"
                      : "hover:bg-white/5 transition-colors group"
                  }
                >
                  <td className="px-4 py-2 border-b border-white/5 text-indigo-400">
                    {k.namespace}
                  </td>
                  <td className="px-4 py-2 border-b border-white/5 text-slate-200">
                    {k.key}
                  </td>
                  <td className="px-4 py-2 border-b border-white/5 text-slate-500 group-hover:text-slate-300 transition-colors truncate max-w-[200px]">
                    {k.value}
                  </td>
                  <td className="px-4 py-2 border-b border-white/5 text-slate-200 font-bold">
                    {k.expired ? (
                      <span className="text-red-500">Yes</span>
                    ) : (
                      <span className="text-green-400">No</span>
                    )}
                  </td>
                  <td className="px-4 py-2 border-b border-white/5 text-slate-500">
                    {k.ttl === -1 ? "∞" : `${k.ttl}s`}
                  </td>
                  <td className="px-4 py-2 border-b border-white/5 text-slate-600 font-bold">
                    {k.stripe}
                  </td>
                </motion.tr>
              ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default KeyTable;
