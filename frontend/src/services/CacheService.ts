import axios from "axios";

const API_BASE = "https://backend.saoodahmad.com:443";

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    "Content-Type": "application/json",
  },
});

export interface CachePayload {
  namespace: string;
  key: string;
  value: string;
  ttl: number;
}

export const CacheService = {
  async getState() {
    const { data } = await api.get("/api/cache/state");

    return data;
  },

  async set(payload: CachePayload) {
    const { data } = await api.post("/api/cache/set", payload);

    return data;
  },

  async get(ns: string, key: string) {
    const { data } = await api.get(
      `/api/cache/get/${encodeURIComponent(ns)}/${encodeURIComponent(key)}`,
    );

    return data;
  },

  async delete(ns: string, key: string) {
    const { data } = await api.delete(
      `/api/cache/del/${encodeURIComponent(ns)}/${encodeURIComponent(key)}`,
    );

    return data;
  },

  async getActuatorMetrics() {
    const metrics: string[] = [
      "cacheforge.cache.hits",
      "cacheforge.cache.misses",
      "cacheforge.cache.expired",
      "cacheforge.cache.latency",
      "cacheforge.cache.set.created",
      "cacheforge.cache.set.updated",
    ];

    const results = await Promise.all<{ name: string; value: number }>(
      metrics.map(async (m: string) => {
        try {
          const { data } = await api.get(`/actuator/metrics/${m}`);

          let val = data.measurements[0].value;

          if (m == "cacheforge.cache.latency") {
            val = data.measurements[2].value;
          }

          return {
            name: m,
            value: val,
          };
        } catch {
          return { name: m, value: 0 };
        }
      }),
    );

    return results.reduce<{
      hits: number;
      misses: number;
      expired: number;
      latency: number;
      created: number;
      updated: number;
    }>(
      (
        acc: {
          hits: number;
          misses: number;
          expired: number;
          latency: number;
          created: number;
          updated: number;
        },
        curr: { name: string; value: number },
      ) => {
        if (curr.name == "cacheforge.cache.hits") {
          acc["hits"] = curr.value;
        } else if (curr.name == "cacheforge.cache.misses") {
          acc["misses"] = curr.value;
        } else if (curr.name == "cacheforge.cache.expired") {
          acc["expired"] = curr.value;
        } else if (curr.name == "cacheforge.cache.latency") {
          acc["latency"] = curr.value;
        } else if (curr.name == "cacheforge.cache.set.created") {
          acc["created"] = curr.value;
        } else if (curr.name == "cacheforge.cache.set.updated") {
          acc["updated"] = curr.value;
        }

        return acc;
      },
      { hits: 0, misses: 0, expired: 0, latency: 0, created: 0, updated: 0 },
    );
  },
};
