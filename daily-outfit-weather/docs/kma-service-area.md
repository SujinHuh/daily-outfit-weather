# KMA Location Grid Service Area

## Current Readiness

The KMA grid catalog is not nationwide-ready. It is an MVP allowlist used by profile location enrichment, location search, and weather recommendation calls that require `nx`/`ny`.

Supported locations are exactly the rows in `backend/src/main/resources/location/kma_location_grids.csv`:

| Sido | Sigungu | Dong | nx | ny |
| --- | --- | --- | --- | --- |
| 서울특별시 | 강남구 | 역삼동 | 61 | 125 |
| 서울특별시 | 서초구 | 서초동 | 61 | 125 |
| 서울특별시 | 성동구 | 성수동 | 61 | 127 |
| 서울특별시 | 성동구 | 성수1가1동 | 61 | 127 |
| 서울특별시 | 성동구 | 성수1가2동 | 61 | 127 |
| 서울특별시 | 성동구 | 성수2가1동 | 61 | 127 |
| 서울특별시 | 성동구 | 성수2가3동 | 61 | 127 |
| 서울특별시 | 강동구 | 성내1동 | 62 | 126 |
| 서울특별시 | 강동구 | 성내2동 | 62 | 126 |
| 서울특별시 | 강동구 | 성내3동 | 62 | 126 |
| 서울특별시 | 강동구 | 둔촌1동 | 62 | 126 |
| 서울특별시 | 강동구 | 둔촌2동 | 62 | 126 |
| 서울특별시 | 송파구 | 잠실본동 | 62 | 126 |
| 서울특별시 | 송파구 | 잠실2동 | 62 | 126 |
| 서울특별시 | 송파구 | 잠실3동 | 62 | 126 |
| 서울특별시 | 송파구 | 잠실4동 | 62 | 126 |
| 서울특별시 | 송파구 | 잠실6동 | 62 | 126 |
| 서울특별시 | 송파구 | 잠실7동 | 62 | 126 |
| 경기도 | 성남시 분당구 | 판교동 | 62 | 123 |

## Runtime Behavior

- `GET /api/locations/search?keyword=` only returns matches from the static catalog.
- Profile create/update preserves caller-provided `nx`/`ny` when both values are present.
- If `nx`/`ny` are omitted and `sido`/`sigungu`/`dong` match the catalog, the profile service fills the grid values.
- If a location is not in the catalog and no explicit `nx`/`ny` are supplied, grid values remain null. In production, weather fallback should stay disabled so unsupported grid lookups surface as `WEATHER_UNAVAILABLE` instead of silently using default weather.

## Expansion Procedure

Do not treat the app as nationwide-ready until the catalog has been expanded and validated.

To add service areas:

1. Obtain approved KMA DFS grid coordinates for the target administrative dongs through the project owner or another approved source. Do not fetch nationwide data ad hoc as part of routine code changes.
2. Add rows to `backend/src/main/resources/location/kma_location_grids.csv` using the existing header and column order: `sido,sigungu,dong,nx,ny`.
3. Keep dong names consistent with the labels used by onboarding/profile input and location search.
4. Add or update focused tests in `LocationGridCatalogTest` for exact match and keyword search behavior covering at least one new service area.
5. Run the backend test suite, then perform a KMA live smoke test only when a valid `KMA_SERVICE_KEY` is available.
6. Update this document and `docs/deployment-readiness-log.md` when the supported service area changes.

## Release Gate

Before production launch, choose one of these gates:

- Limit onboarding/profile location choices to the supported catalog above.
- Expand and validate the catalog for every location the product claims to support.

Until one gate is satisfied, location/weather readiness is partial.
