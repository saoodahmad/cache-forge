export interface tLRU {
  namespace: string;
  key: string;
}

export interface tStripe {
  id: number;
  keyCount: number;
  capacity: number;
  lru: tLRU[];
}

export interface tKey {
  namespace: string;
  key: string;
  value: string;
  ttl: number;
  expired: boolean;
  stripe: number;
}

export interface tEventLog {
  msg: string;
  time: string;
}

export interface tCacheStoreEntry {
  stripes: tStripe[];
  allKeys: tKey[];
  capacity: number;
}

export interface tActutatorStats {
  hits: number;
  misses: number;
  expired: number;
  latency: number;
  created: number;
  updated: number;
}
