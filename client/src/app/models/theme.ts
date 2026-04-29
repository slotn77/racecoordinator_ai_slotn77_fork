import { AudioConfig } from "./driver";
import { Model } from "./model";

/**
 * A theme groups visual and audio asset assignments into logical "slots."
 * Each slot maps a purpose (e.g., "flag.green", "lamp.red_on") to an asset entity ID.
 *
 * Themes are stored server-side (MongoDB) and selected per-client via Settings.
 */
export interface Theme extends Model {
  entity_id: string;
  name: string;
  is_default: boolean;
  slots: { [key: string]: string }; // image slot key → asset entity ID
  audio_slots: { [key: string]: AudioConfig }; // audio slot key → AudioConfig
}

/** All known theme slot keys for Phase 1 (images). */
export const THEME_SLOT_KEYS = {
  // Flags
  FLAG_GREEN: "flag.green",
  FLAG_RED: "flag.red",
  FLAG_YELLOW: "flag.yellow",
  FLAG_WHITE: "flag.white",
  FLAG_YELLOWGREEN: "flag.yellowgreen",
  FLAG_CHECKERED: "flag.checkered",
  FLAG_BLACK: "flag.black",

  // Start lamps
  LAMP_RED_ON: "lamp.red.on",
  LAMP_RED_DIM: "lamp.red.dim",
  LAMP_GREEN: "lamp.green",

  // Fuel gauge
  FUEL_GAUGE: "gauge.fuel",

  // Audio (these keys map to audio_slots)
  AUDIO_YELLOW_FLAG: "audio.yellowflag",
  AUDIO_COUNTDOWN_5: "audio.countdown.5",
  AUDIO_COUNTDOWN_4: "audio.countdown.4",
  AUDIO_COUNTDOWN_3: "audio.countdown.3",
  AUDIO_COUNTDOWN_2: "audio.countdown.2",
  AUDIO_COUNTDOWN_1: "audio.countdown.1",
  AUDIO_COUNTDOWN_GO: "audio.countdown.go",
} as const;
