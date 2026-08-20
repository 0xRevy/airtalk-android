# AirTALK Android — voice chat client

Native Android client for the AirTALK random voice-chat backend (`wss://api.airtalk.live/signaling`), built from the live-discovered protocol. No local build tools needed on your PC — the APK is compiled for free on GitHub's servers.

## Build the APK (zero local installs)

1. Create a GitHub repo (or use an existing one)
2. Push this folder's contents to it (branch `main`)
3. Open **Actions** tab → the **Build APK** workflow runs automatically → open the run → **Artifacts** → download `airtalk-apk`
4. Sideload `app-debug.apk` on your phone (enable "Install unknown apps" for your file manager)
5. Grant microphone permission, set filters, tap **Start Voice Chat**

Rebuilds happen automatically on every push.

## What's implemented

- Guest JWT auth (token in `auth/TokenManager.kt` — swap `GUEST_MINT_URL` for your backend's mint endpoint to auto-refresh)
- Full signaling protocol: FILTER_UPDATE, STATUS_UPDATE FREE/STALE, PING→PONG keepalive, INIT/SDP/CANDIDATE relay, ESTABLISHED, HANG_UP, PAGE_REFRESH handling with token refresh + auto-reconnect
- Audio-only WebRTC (matches the live backend — no video), echo cancellation on
- Per-call TURN credentials from `turnCredential` (`turn:5.75.164.144:3478`) + Google STUN
- `message_channel` data channel: `#kplv#` keepalive, `#hang_up#`, `#mute_enabled#`/`#mute_disabled#`
- Filters (gender, interests, strict, callbacks), mute, hang up, report (5 categories)
- Reconnect with backoff; polite auto-decline of incoming callback `CALL_REQUEST`s (v1 has no callback UI)

## Protocol notes

Connection requires: browser-like `User-Agent`, `Cookie: artlk_ui_version=0.0.2`, and a valid JWT in `?token=` — all handled by `net/SignalingClient.kt`.

## Roadmap (phase 2)

- Text chat mode (`&mode=text`, TEXT_CHAT WAITING/MATCHED/MESSAGE/TYPING)
- Friends + callback calls (CALL_REQUEST/CALL_RESPONSE, FRIEND_CHAT)
- Country filters, pictures (`picture_channel`), games (`game_channel`)
- Google sign-in → `POST /login/tokensignin` for premium features