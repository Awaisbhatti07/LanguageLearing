# Known issues and blockers

| ID | State | Issue | Impact / next action |
| --- | --- | --- | --- |
| ENV-01 | Open | Sandbox DNS/network restrictions require approved network execution for Git/dependency downloads | Use scoped network access; preserve exact failures if downloads are unavailable |
| CONTENT-01 | Pending | No qualified language review yet | Mark authored learning material pending review; do not claim certified A0–B1 coverage |
| QURAN-01 | Pending M6 | Quranic datasets, redistribution rights and linguistic/religious review not acquired | Do not fabricate or bundle religious material; evaluate sources before import |
| VOICE-01 | Pending M8/M9 | No paid voice credentials/backend configured | Offline learning independent; no fake AI or embedded secret |
| DEVICE-01 | Pending authorization | No device/emulator verification performed | Request scope before launches; compilation is not device evidence |
| BUILD-01 | Blocked on user decision | Stable Compose 1.12.1, Lifecycle 2.11.0 and Navigation 2.10.2 require compile SDK 37; SDK 36 rejected 14 AARs | Compile SDK corrected; official stable package is `platforms;android-37.0` in repository2-3.xml. Required license hash `9002c006f4b8d9a16e715a9fa4df30ddb8abf9d9` is absent; only `24333f8a63b6825ea9c5514f83c2829b004d1fee` is accepted locally. Terms: `/private/tmp/lingomate-sdk-license.txt`. Pending explicit consent; no SDK download or new acceptance performed |
