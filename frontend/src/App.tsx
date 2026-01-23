import { useState, useMemo, useEffect } from "react";
import { Activity, RefreshCw, Github, ShieldAlert } from "lucide-react";

import { useCacheStore } from "./hooks/useCacheStore";
import type { tKey } from "./types/types.ts";

import StripeCard from "./components/StripeCard.tsx";
import ActuatorStats from "./components/ActuatorStats.tsx";
import Operations from "./components/Operations.tsx";
import EventLog from "./components/EventLog.tsx";
import SystemOverview from "./components/SystemOverview.tsx";
import KeyTable from "./components/KeyTable.tsx";

export default function App() {
  const { data, loading, logs, refresh, addLog } = useCacheStore();

  const [activeKey, setActiveKey] = useState<string | null>(null);

  const namespaces = useMemo<string[]>(
    () => Array.from(new Set(data.allKeys.map((k: tKey) => k.namespace))),
    [data.allKeys],
  );

  function scrollToElement(id: string): void {
    const element = document.getElementById(id);
    if (element) {
      setActiveKey(id);
      element.scrollIntoView({ behavior: "smooth" });
    }
  }

  useEffect(() => {
    refresh(true);

    const interval = setInterval(() => refresh(false), 50 * 1000);
    clearInterval(interval);
  }, []);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-300 font-inter selection:bg-indigo-500/30">
      <header className="sticky top-0 z-50 backdrop-blur-md bg-slate-950/80 border-b border-white/5 px-6 py-3.5 flex items-center justify-between">
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2">
            <div className="bg-indigo-600 p-1 rounded-md">
              <Activity size={16} className="text-white" />
            </div>
            <span className="text-xs font-black tracking-widest text-white uppercase">
              Cache Forge Visualizer
            </span>
          </div>
          <div className="hidden md:flex items-center gap-2 px-3 py-1 bg-amber-500/5 border border-amber-500/10 rounded-full">
            <ShieldAlert size={10} className="text-amber-500/60" />
            <span className="text-[9px] text-amber-100 font-bold uppercase tracking-wider">
              PRIVACY NOTICE: Do not enter sensitive data
            </span>
          </div>
        </div>
        <div className="flex items-center gap-4">
          <a
            href="https://github.com/saoodahmad/cache-forge"
            className="flex items-center gap-1 text-slate-300 hover:text-white transition-colors text-[10px] font-bold uppercase"
          >
            <Github size={15} /> Source
          </a>
          <button
            onClick={() => refresh(true)}
            className="bg-indigo-600 hover:bg-indigo-500 text-white px-3 py-1 rounded text-[10px] font-bold uppercase flex items-center gap-2 transition-all hover:cursor-pointer"
          >
            <RefreshCw size={12} className={loading ? "animate-spin" : ""} />{" "}
            Sync
          </button>
        </div>
      </header>

      <main className="p-4 grid grid-cols-12 gap-4 max-w-[1800px] mx-auto">
        <div className="col-span-12">
          <ActuatorStats />
        </div>

        <div className="col-span-12 lg:col-span-3 space-y-4">
          <Operations addLog={addLog} refresh={refresh} />

          <EventLog logs={logs} />
        </div>

        <div className="col-span-12 lg:col-span-9 space-y-4">
          <SystemOverview data={data} namespaces={namespaces} />

          <div className="bg-slate-900 border border-white/5 rounded-xl p-4 shadow-2xl">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-[10px] font-black text-slate-500 uppercase tracking-widest">
                Cache_Stripes (LRU Queues)
              </h2>
            </div>
            <StripeCard
              stripes={data.stripes}
              scrollToElement={scrollToElement}
            />
          </div>

          <KeyTable data={data} activeKey={activeKey} />
        </div>
      </main>
    </div>
  );
}
