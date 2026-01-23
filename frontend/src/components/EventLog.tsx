import type { ReactElement } from "react";
import { Terminal } from "lucide-react";

import type { tEventLog } from "../types/types";

const EventLog = ({ logs }: { logs: tEventLog[] }): ReactElement => {
  return (
    <div className="bg-slate-900 border border-white/5 rounded-xl p-4 h-[340px] flex flex-col shadow-2xl">
      <div className="flex items-center gap-2 mb-3">
        <Terminal size={12} className="text-slate-500" />
        <span className="text-[10px] font-black text-slate-500 uppercase tracking-widest">
          Event_Log
        </span>
      </div>
      <div className="flex-1 overflow-y-auto font-mono text-[10px] space-y-1.5 custom-scrollbar pr-1">
        {logs.map((log, i) => (
          <div
            key={i}
            className="flex gap-2 border-b border-white/5 pb-1 opacity-70 hover:opacity-100 transition-opacity"
          >
            <span className="text-slate-600 shrink-0">{log.time}</span>
            <span className="text-indigo-300 text-wrap">{log.msg}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default EventLog;
