# Engineering decisions

- **2026-10-20 D001:** Preserve remote README/history and develop on local `develop/lingomate`; never push without authorization.
- **D002:** Start with one Android app and one pure Kotlin `core:learning` module. Use feature packages rather than numerous empty Gradle modules. This keeps offline logic independently testable with a small build graph.
- **D003:** Manual application-scoped dependency injection; ViewModels receive explicit dependencies. Avoid an annotation-driven DI dependency until complexity justifies it.
- **D004:** DataStore holds UI/onboarding preferences; Room holds separately keyed content and learner progress. No destructive migration fallback. Export versioned schemas.
- **D005:** No bundled Quranic text/meaning/frequency until actual source and redistribution checks pass. Unavailable content and AI capabilities must be explained honestly in UI.
- **D006:** Dark theme defaults; light and system modes are explicit. System font scaling, scrolling layouts and touch targets are mandatory.
- **D007:** First verify current stable dependency metadata and compatibility using official sources. Pin versions; no dynamic versions or preview dependencies. Record selected versions and build evidence separately.
