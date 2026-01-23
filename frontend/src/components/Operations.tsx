import { useState, type ReactElement } from "react";

import { CacheService } from "../services/CacheService.ts";

const Operations = ({
  addLog,
  refresh,
}: {
  addLog: (msg: string) => void;
  refresh: (flag: boolean) => void;
}): ReactElement => {
  const [form, setForm] = useState({ ns: "", key: "", val: "", ttl: -1 });

  const handleOp = async (type: "SET" | "GET" | "DEL") => {
    try {
      let data;
      if (type === "SET") {
        if (form.ns == "" || form.key == "" || form.val == "") {
          alert("Namespace, key and value required for SET");
          addLog("Namespace, key and value required for SET");
          return;
        }

        data = await CacheService.set({
          namespace: form.ns,
          key: form.key,
          value: form.val,
          ttl: form.ttl,
        });
      } else if (type === "GET") {
        if (form.ns == "" || form.key == "") {
          alert("Namespace and key required for GET");
          addLog("Namespace and key required for GET");
          return;
        }

        data = await CacheService.get(form.ns, form.key);
      } else {
        if (form.ns == "" || form.key == "") {
          alert("Namespace and key required for DEL");
          addLog("Namespace and key required for DEL");
          return;
        }

        data = await CacheService.delete(form.ns, form.key);
      }

      addLog(
        `${type} [${form.ns}:${form.key}] -> ${data.hit ? "HIT" : "OK/MISS"}`,
      );

      if (type === "GET" && data)
        setForm((f) => ({ ...f, val: JSON.stringify(data.data.val) }));
      refresh(true);
    } catch (e: unknown) {
      addLog(`Operation Failed: ${type}`);
      console.log(e);
    }
  };

  return (
    <div className="bg-slate-900 border border-white/5 rounded-xl p-4 shadow-2xl">
      <h2 className="text-[10px] font-black text-slate-500 uppercase tracking-[0.2em] mb-4">
        Operations
      </h2>

      <div className="space-y-3">
        <div>
          <label className="text-[9px] font-bold text-indigo-400 uppercase tracking-tighter">
            Namespace*
          </label>
          <input
            type="text"
            className="w-full bg-slate-950 border border-white/10 rounded-md p-2 text-xs font-mono mt-1 focus:border-indigo-500 outline-none"
            placeholder="Namespace 1"
            value={form.ns}
            onChange={(e) => setForm({ ...form, ns: e.target.value })}
          />
        </div>
        <div className="grid grid-cols-2 gap-2">
          <div>
            <label className="text-[9px] font-bold text-indigo-400 uppercase tracking-tighter">
              Key*
            </label>
            <input
              type="text"
              className="w-full bg-slate-950 border border-white/10 rounded-md p-2 text-xs font-mono mt-1 outline-none"
              placeholder="Key 1"
              value={form.key}
              onChange={(e) => setForm({ ...form, key: e.target.value })}
            />
          </div>
          <div>
            <label className="text-[9px] font-bold text-indigo-400 uppercase tracking-tighter">
              TTL(s)
            </label>
            <input
              type="number"
              className="w-full bg-slate-950 border border-white/10 rounded-md p-2 text-xs font-mono mt-1 outline-none"
              value={form.ttl}
              onChange={(e) =>
                setForm({ ...form, ttl: Number(e.target.value) })
              }
            />
          </div>
        </div>
        <textarea
          className="w-full bg-slate-950 border border-white/10 rounded-md p-2 text-xs font-mono h-20 outline-none"
          placeholder="Payload... (Required for SET)"
          value={form.val}
          onChange={(e) => setForm({ ...form, val: e.target.value })}
        />

        <div className="grid grid-cols-3 gap-2">
          <button
            onClick={() => handleOp("SET")}
            className="bg-indigo-600/10 hover:bg-indigo-600 text-indigo-400 hover:text-white border border-indigo-500/20 py-2 rounded font-bold text-[10px] uppercase transition-all hover:cursor-pointer"
          >
            Set
          </button>
          <button
            onClick={() => handleOp("GET")}
            className="bg-slate-800 hover:bg-slate-700 py-2 rounded font-bold text-[10px] uppercase transition-all text-white hover:cursor-pointer"
          >
            Get
          </button>
          <button
            onClick={() => handleOp("DEL")}
            className="bg-red-500/10 hover:bg-red-600 text-red-500 hover:text-white border border-red-500/20 py-2 rounded font-bold text-[10px] uppercase transition-all hover:cursor-pointer"
          >
            Del
          </button>
        </div>

        <p className="text-[10px] text-slate-500 italic">
          Expiry cleanup occurs on GET/SET/DEL operations.
        </p>
      </div>
    </div>
  );
};

export default Operations;
