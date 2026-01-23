import { useEffect, useState, type ReactNode } from "react";
import { Zap, Hash, PlusIcon, Trash, Binoculars, Save } from "lucide-react";

import { CacheService } from "../services/CacheService";
import type { tActutatorStats } from "../types/types";

const StatCard = ({
  label,
  value,
  icon,
  color = "text-white",
}: {
  label: string;
  value: string;
  icon: ReactNode;
  color?: string;
}) => (
  <div className="bg-slate-900 border border-white/5 p-2 rounded-xl shadow-inner">
    <div className="flex items-center gap-2">
      {icon}
      <span className="text-[10px] font-black text-slate-500 uppercase tracking-tight">
        {label}
      </span>

      <span className={`text-[13px] font-mono font-bold leading-none ${color}`}>
        {value}
      </span>
    </div>
  </div>
);

const ActuatorStats = () => {
  const [metrics, setMetrics] = useState<tActutatorStats>({
    hits: 0,
    misses: 0,
    expired: 0,
    latency: 0,
    created: 0,
    updated: 0,
  });

  useEffect(() => {
    const fetchActuator = async () => {
      const data = await CacheService.getActuatorMetrics();
      setMetrics(data);
    };

    fetchActuator();
    const interval = setInterval(fetchActuator, 5 * 1000); // Poll actuator every 5s
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="grid grid-cols-2 md:grid-cols-6 gap-4 mb-1">
      <StatCard
        label="Hits"
        value={String(metrics["hits"])}
        icon={<Zap size={15} className="text-yellow-400" />}
      />

      <StatCard
        label="Misses"
        value={String(metrics["misses"])}
        icon={<Binoculars size={15} className="text-emerald-400" />}
      />

      <StatCard
        label="Expired"
        value={String(metrics["expired"])}
        icon={<Trash size={15} className="text-red-400" />}
      />

      <StatCard
        label="Created"
        value={String(metrics["created"])}
        icon={<PlusIcon size={15} className="text-green-400" />}
      />

      <StatCard
        label="Updated"
        value={String(metrics["updated"])}
        icon={<Save size={15} className="text-green-400" />}
      />

      <StatCard
        label="Latency (ms)"
        value={Number(metrics["latency"] * 1000).toFixed(3)}
        icon={<Hash size={15} className="text-indigo-400" />}
      />
    </div>
  );
};

export default ActuatorStats;
