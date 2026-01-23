import { useState, useCallback } from "react";

import { CacheService } from "../services/CacheService.ts";
import type { tCacheStoreEntry, tEventLog, tKey } from "../types/types.ts";

export function useCacheStore() {
  const [data, setData] = useState<tCacheStoreEntry>({
    stripes: [],
    allKeys: [],
    capacity: 0,
  });

  const [loading, setLoading] = useState<boolean>(false);
  const [logs, setLogs] = useState<tEventLog[]>([]);

  const addLog = useCallback((msg: string) => {
    const time = new Date().toLocaleTimeString([], {
      hour12: false,
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    });
    setLogs((prev) => [{ msg, time }, ...prev].slice(0, 100));
  }, []);

  const refresh = useCallback(
    async (printLog: boolean) => {
      setLoading(true);
      try {
        const state = await CacheService.getState();
        const keysMap = state.keys || {};
        const lruMap = state.lru || {};
        const stripeIds = Object.keys({ ...keysMap, ...lruMap }).sort(
          (a, b) => Number(a) - Number(b),
        );

        const allKeys: tKey[] = [];

        const stripes = stripeIds.map((sid) => {
          const entries = keysMap[sid].map((k: tKey) => ({
            namespace: k.namespace,
            key: k.key,
            value: k.value,
            ttl: k.ttl,
            expired: !!k.expired,
            stripe: Number(sid),
          }));

          allKeys.push(...entries);

          return {
            id: Number(sid),
            keyCount: entries.length,
            capacity: state.capacity / (stripeIds.length || 1),
            lru: lruMap[sid],
          };
        });

        setData({ stripes, allKeys, capacity: state.capacity });

        if (printLog) {
          addLog("System state synchronized.");
        }
      } catch (e) {
        addLog(
          `Sync Error: ${e instanceof Error ? e.message : "Unknown error"}`,
        );
      } finally {
        setLoading(false);
      }
    },
    [addLog],
  );

  return { data, loading, logs, refresh, addLog };
}
