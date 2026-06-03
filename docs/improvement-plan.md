# Kitsune — Codebase Improvement Plan

A prioritized, evidence-based plan covering **code quality**, **performance**, **design/UX polish**, and **test coverage**. Items are grouped by theme and tagged with effort (S/M/L) and impact (★1–3). Phases at the end give a suggested execution order.

> Baseline (verified): Kotlin 2.2.20 · minSdk 26 / target 35 / compile 36 · Koin DI · Room · Paging 3 · Glide 5 · Retrofit/OkHttp · Algolia InstantSearch · mixed Compose + XML/ViewBinding. ~35 fragments, ~25 ViewModels, ~25 repositories, ~20 adapters.

---

## 1. Code Quality

### 1.1 Eliminate manual ViewBinding boilerplate ★★★ (M)
**Problem:** ~35 fragments repeat the same pattern:
```kotlin
private var _binding: FragmentXBinding? = null
private val binding get() = _binding!!
// ...
override fun onDestroyView() { super.onDestroyView(); _binding = null }
```
This is error-prone (`!!` crashes if accessed post-`onDestroyView`) and noisy.

**Action:**
- Add a `FragmentViewBindingDelegate` (lifecycle-aware property delegate) under `util/ui/`.
- Migrate fragments to `private val binding by viewBinding(FragmentXBinding::bind)`.
- Removes ~3 lines × 35 files and the whole class of "access after destroy" bugs.

**Files (sample):** [FeedListFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/feed/FeedListFragment.kt), [GroupsFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/groups/GroupsFragment.kt), [SearchFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/search/SearchFragment.kt), [DetailsFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/details/DetailsFragment.kt), and ~31 others.

### 1.2 Break up god classes ★★★ (L)
**Problem:** Several files own too many concerns:
| File | Lines | Concerns mixed |
|---|---|---|
| [LibraryFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/library/LibraryFragment.kt) | 632 | list, filtering, offline sync, paging, transitions |
| [ProfileFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/profile/ProfileFragment.kt) | 628 | profile, stats charts, favorites, links |
| [DetailsFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/details/DetailsFragment.kt) | 568 | details, ratings, characters, streaming, favorites |
| [DetailsViewModel.kt](../app/src/main/java/io/github/drumber/kitsune/ui/details/DetailsViewModel.kt) | 436 | media + library + favorite + reactions |
| [SearchViewModel.kt](../app/src/main/java/io/github/drumber/kitsune/ui/search/SearchViewModel.kt) | 432 | Algolia, filters, facets |

**Action:**
- Extract cohesive responsibilities into child fragments / dedicated "section" controllers (e.g. `ProfileStatsSection`, `ProfileFavoritesSection`) or delegate classes.
- Move pure state logic out of fragments into ViewModels; move multi-domain ViewModel logic into focused use cases.
- Target: no UI file > ~350 lines.

### 1.3 Replace `notifyDataSetChanged()` with DiffUtil/`ListAdapter` ★★ (M)
**Problem:** Full-rebind invalidation in 8+ places kills RecyclerView animations and wastes layout passes.

**Sites:** [UserProfileFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/profile/UserProfileFragment.kt#L430), [ProfileFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/profile/ProfileFragment.kt#L413), [DetailsFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/details/DetailsFragment.kt#L496), [PostImageThumbnailAdapter.kt](../app/src/main/java/io/github/drumber/kitsune/ui/createpost/PostImageThumbnailAdapter.kt#L22), [CharacterSearchResultAdapter.kt](../app/src/main/java/io/github/drumber/kitsune/ui/profile/editprofile/CharacterSearchResultAdapter.kt#L42), [ExploreSection.kt](../app/src/main/java/io/github/drumber/kitsune/ui/component/ExploreSection.kt#L58), [MediaMappingsBottomSheet.kt](../app/src/main/java/io/github/drumber/kitsune/ui/details/MediaMappingsBottomSheet.kt#L50), [CharacterDetailsBottomSheet.kt](../app/src/main/java/io/github/drumber/kitsune/ui/details/characters/CharacterDetailsBottomSheet.kt#L199).

**Action:** Convert these adapters to `ListAdapter<T, VH>` with `DiffUtil.ItemCallback`, or use `AsyncListDiffer`.

### 1.4 Tame the 13-lambda adapter constructor ★★ (M)
**Problem:** [PostPagingAdapter.kt](../app/src/main/java/io/github/drumber/kitsune/ui/adapter/paging/PostPagingAdapter.kt) takes ~13 lambda parameters — hard to read, call, and test.

**Action:** Introduce a single `PostInteractionListener` interface (or a sealed `PostAction` + one callback). The fragment implements it once. Reduces coupling and makes the adapter unit-testable.

### 1.5 Narrow broad exception handling ★★ (M)
**Problem:** ~30 `catch (e: Exception)` sites swallow everything, including programming errors and cancellation.

**Action:**
- Catch the narrowest meaningful type (`IOException`, `HttpException`, `retrofit2.HttpException`).
- Always rethrow `CancellationException` inside coroutines (`if (e is CancellationException) throw e`).
- Funnel network errors through a shared `Result`/`ApiResponse` wrapper so UI can show typed errors.

**Key sites:** [LibraryRepository.kt](../app/src/main/java/io/github/drumber/kitsune/data/repository/LibraryRepository.kt#L136), [SettingsViewModel.kt](../app/src/main/java/io/github/drumber/kitsune/ui/settings/SettingsViewModel.kt), [CreatePostViewModel.kt](../app/src/main/java/io/github/drumber/kitsune/ui/createpost/CreatePostViewModel.kt#L235), [BasePagingDataSource.kt](../app/src/main/java/io/github/drumber/kitsune/data/source/network/BasePagingDataSource.kt#L23).

### 1.6 Reduce non-null assertions (`!!`) ★ (S)
Replace `!!` in mapping/UI hot paths with safe handling or `requireNotNull(...) { "msg" }` for clearer crash messages. E.g. [GroupsRepository.kt](../app/src/main/java/io/github/drumber/kitsune/data/repository/GroupsRepository.kt#L67), [FollowRepository.kt](../app/src/main/java/io/github/drumber/kitsune/data/repository/FollowRepository.kt#L43), [CategoriesViewModel.kt](../app/src/main/java/io/github/drumber/kitsune/ui/search/categories/CategoriesViewModel.kt#L70).

### 1.7 Static analysis & formatting gate ★★★ (S)
**Problem:** No automated lint/format gate beyond ArchUnit.

**Action:** Add to the Gradle build:
- **detekt** (Kotlin static analysis) with a tuned ruleset.
- **ktlint** (or detekt-formatting) for consistent style.
- Wire both into CI and a `./gradlew check` pre-merge gate.

---

## 2. Performance

### 2.1 Add a JankStats / StrictMode baseline ★★ (S)
- Enable `StrictMode` (thread + VM policy) in debug `KitsuneApplication` to surface disk/network on main thread and leaks early.
- Optionally add `androidx.metrics:metrics-performance` (JankStats) to log dropped frames in debug.

### 2.2 Audit `runBlocking` on the network path ★★ (M)
[AuthenticationInterceptor.kt](../app/src/main/java/io/github/drumber/kitsune/util/network/AuthenticationInterceptor.kt#L54) calls `runBlocking { refresh... }` inside an OkHttp interceptor. This blocks the OkHttp dispatcher thread for the whole refresh.
**Action:** Keep it synchronous (interceptors must be), but (a) ensure refresh is single-flight (mutex) so concurrent 401s don't trigger N refreshes, and (b) cap with a timeout. Verify it never runs on the main dispatcher.

### 2.3 Baseline Profiles + R8 full mode ★★★ (M)
- Add a **Baseline Profile** module (`androidx.baselineprofile`) to improve cold-start and scroll jank on first runs.
- Confirm R8 full mode is on for release and add startup-critical keep rules only where needed.

### 2.4 Image loading & list performance ★★ (M)
- Verify Glide requests in list adapters set explicit `override(w,h)` to avoid decoding full-res into small `ImageView`s; reuse `RequestBuilder` thumbnails.
- Ensure all RecyclerViews that have fixed item sizes set `setHasFixedSize(true)`.
- Audit nested layouts in `item_*.xml` for deep hierarchies; flatten with `ConstraintLayout` where `LinearLayout` nesting is deep.

### 2.5 Paging configuration review ★ (S)
Review `PagingConfig` (pageSize, prefetchDistance, `enablePlaceholders`) across data sources; align with item heights to minimize redundant fetches.

### 2.6 Room query/index audit ★ (S)
- Verify indices on columns used in `WHERE`/`ORDER BY` for library queries.
- Confirm DAO `Flow` queries are `distinctUntilChanged` where appropriate to avoid redundant UI rebinds.

---

## 3. Design Beauty (UX / Visual Polish)

### 3.1 Converge on one UI toolkit per surface ★★ (L, strategic)
The app mixes Compose and XML/ViewBinding. Define a clear rule (e.g. "new screens in Compose, legacy stays XML until touched") and document it, so the codebase trends toward consistency instead of drifting. Track migration candidates (small leaf screens first: bottom sheets, dialogs).

### 3.2 Motion & shared-element consistency ★★ (M)
- Standardize list→detail shared-element transitions (already used in [LibraryFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/library/LibraryFragment.kt#L463)) across Feed, Search, Profile for a cohesive feel.
- Restore item animations once DiffUtil replaces `notifyDataSetChanged()` (§1.3) — this alone visibly improves perceived quality.

### 3.3 Loading / empty / error states ★★ (M)
- Add skeleton/shimmer placeholders for list-heavy screens (Feed, Library, Search) instead of blank → pop.
- Provide friendly empty-states with illustrations + a retry CTA, driven by Paging `LoadState`.

### 3.4 Theming & Material 3 polish ★ (S)
- Audit color usage against the M3 token set; ensure dynamic color (Material You) is honored where the user opts in.
- Verify content respects `WindowInsets` edge-to-edge consistently (some screens already use insets listeners; make it uniform).

### 3.5 Accessibility pass ★★ (M)
- Add `contentDescription` to all icon-only controls (like/comment/send buttons).
- Verify touch targets ≥ 48dp and text contrast ratios.
- Test with TalkBack on the Feed and Details flows.

---

## 4. Test Coverage

Current state: **24 unit tests + 1 instrumented test**. Repos ~25% covered, **ViewModels 0%**, **Network/Local DataSources 0%**, **UI 0%**. No coverage tooling.

### 4.1 Add coverage measurement ★★★ (S)
- Integrate **Kover** (Kotlin-native) or Jacoco; publish HTML + XML reports and a CI summary.
- Set a non-blocking baseline now, then ratchet a minimum threshold upward over time.

### 4.2 ViewModel tests (highest value gap) ★★★ (L)
ViewModels hold the most untested business logic. Add tests with `runTest` + `Turbine` (add dependency) for Flow assertions. Priority order:
1. [DetailsViewModel.kt](../app/src/main/java/io/github/drumber/kitsune/ui/details/DetailsViewModel.kt)
2. [LibraryViewModel.kt](../app/src/main/java/io/github/drumber/kitsune/ui/library/LibraryViewModel.kt)
3. [SearchViewModel.kt](../app/src/main/java/io/github/drumber/kitsune/ui/search/SearchViewModel.kt)
4. [CreatePostViewModel.kt](../app/src/main/java/io/github/drumber/kitsune/ui/createpost/CreatePostViewModel.kt)
5. Feed/Notifications/Profile ViewModels.

Cover: initial state, success/empty/error branches, event emission, and the `catch` paths from §1.5.

### 4.3 Repository & DataSource tests ★★ (M)
- Use **MockWebServer** to test network data sources + JSON:API deserialization end-to-end (catches mapper/registration bugs like the feed-subject polymorphism gotcha).
- Add Room DAO tests with in-memory database (Robolectric already available) for `LibraryEntryDao` and modification sync queries.
- Fill remaining repo gaps: Anime/Manga/Media, Comment, Feed, Reaction, Group, Notification, Search.

### 4.4 Mapper tests ★★ (S)
Cover untested mappers: `FeedMapper`, `CommentMapper`, `ReactionMapper`, `NotificationMapper`, `GroupMapper`, `CastingMapper`. Mappers are pure functions — cheap, high-value tests.

### 4.5 Paging tests ★ (M)
Add tests for `PagingSource.load()` implementations (refresh/append/error keys) and the `LibraryEntryRemoteMediator` (initial/append/refresh + end-of-pagination).

### 4.6 UI / instrumented expansion ★ (L)
- Add Espresso smoke tests for critical flows: login, library browse, details open, post create.
- Add Compose UI tests for Compose surfaces.
- Keep these in `androidTest`; they complement (not replace) the unit layer.

---

## 5. Cross-Cutting / Tooling

- **CI pipeline:** run `detekt`, `ktlint`, unit tests + coverage, and ArchUnit on every PR.
- **Dependency hygiene:** add Gradle version-catalog update checks; the catalog is already centralized in [gradle/libs.versions.toml](../gradle/libs.versions.toml).
- **TODO cleanup:** resolve the lingering domain-migration TODO in `KitsuUrlReplacer.kt`.
- **Logging:** ensure no `printStackTrace`/stdout logging leaks into release (ArchUnit already forbids standard streams — extend to `printStackTrace`).

---

## Suggested Execution Phases

**Phase 1 — Foundations (low risk, high leverage)**
- 1.7 detekt/ktlint gate · 4.1 Kover coverage · 2.1 StrictMode/JankStats · 1.7 CI wiring.

**Phase 2 — Safe refactors**
- 1.1 ViewBinding delegate migration · 1.3 DiffUtil/ListAdapter · 1.4 adapter listener · 1.6 `!!` cleanup.

**Phase 3 — Test the core**
- 4.4 mappers · 4.3 repos/DataSources (MockWebServer + Room) · 4.2 ViewModels (top 5).

**Phase 4 — Structural & performance**
- 1.2 god-class decomposition · 1.5 typed error handling · 2.2 auth refresh single-flight · 2.3 Baseline Profiles · 2.4 image/list perf.

**Phase 5 — Polish**
- 3.2 motion · 3.3 loading/empty/error states · 3.5 accessibility · 4.5/4.6 paging + UI tests · 3.1 toolkit convergence (ongoing).

---

### Quick-win shortlist (do first)
1. Add Kover + detekt/ktlint (§4.1, §1.7) — measurable baseline in an afternoon.
2. `FragmentViewBindingDelegate` + migrate 5 fragments as a template (§1.1).
3. Convert the 8 `notifyDataSetChanged()` adapters to `ListAdapter` (§1.3).
4. Mapper unit tests for Feed/Comment/Reaction (§4.4).
5. Single-flight the token refresh in `AuthenticationInterceptor` (§2.2).
