# JAKIM and METMalaysia widget contract

Requested sources: **JAKIM e-Solat** for prayer times; **METMalaysia** for weather. These replace demo numbers in the design. The prototype currently displays unconfigured placeholders. Native Android wiring has not been implemented in this design branch.

## Prayer times: direct JAKIM portal endpoint

Official portal: https://www.e-solat.gov.my/

The portal's own JavaScript currently requests:

```
GET https://www.e-solat.gov.my/index.php?r=esolatApi/TakwimSolat&period=today&zone={JAKIM_ZONE}
```

Verified with HTTP 200 on 2026-10-30 using **SBH07 as an explicit research example, not a selected user zone**. The portal lists SBH07 for Kota Kinabalu, Ranau, Kota Belud, Tuaran, Penampang, Papar, Putatan and Bahagian Pantai Barat.

Map `fajr` → Subuh, `dhuhr` → Zohor, `asr` → Asar, `maghrib` → Maghrib, `isha` → Isyak. Validate `status`, returned `zone`, `serverTime` and prayer date before display. Times use Malaysia's calendar/timezone; do not select a day using UTC midnight. This is an endpoint used by the official portal, not a versioned service with a guaranteed SLA.

## Weather: official government API, METMalaysia as the data provider

Provider documentation: https://developer.data.gov.my/realtime-api/weather

```
GET https://api.data.gov.my/weather/forecast
```

The official documentation explicitly identifies METMalaysia as the data provider. Requests without an access token succeeded. Use this official channel for the app, avoiding a secret MET service token embedded in a distributable APK.

Example tested filters for **Papar (Ds548), research only**:

```
filter=Ds548@location__location_id
date_start=2026-10-30@date
date_end=2026-10-30@date
limit=7
```

Filter syntax: https://developer.data.gov.my/request-query

Verified HTTP 200 for the selected Malaysia date. A bare `limit=1` can return a later forecast day; always validate date and location rather than treating the first row as today.

This feed provides daily forecasts, period forecasts and `min_temp`/`max_temp`. **It does not provide an observed current temperature or hourly +3h/+6h temperatures.** Render a labelled forecast range and morning/afternoon/night periods, not an invented live `29°` reading. Forecast data is updated daily; warnings are updated as required. Attribute METMalaysia/data.gov.my under CC BY 4.0.

Direct MET service alternative: https://api.met.gov.my/ requires obtaining an access token. No token has been requested, committed or embedded. The official open-government route above is the selected token-free design proposal; if the user specifically requires the direct MET host, resolve that before implementation.

## Location and failure policy

- Ask the user to confirm their prayer zone and weather location. The reference's Kinarut/Sabah text is not proof of the user's actual location.
- Do not silently treat district-level Papar forecast as a point observation for Kinarut.
- Until configured, show `--:--`, `--°` and a location/zone prompt.
- On network errors, show unavailable status or a clearly dated valid cache, never replace data with demo values.
- Cache prayer data by zone and Malaysia calendar date; refresh after date/zone changes.
- Store forecast date, fetch timestamp and selected location. Do not present expired or mismatched data as current.
- Use HTTPS, bounded timeouts and background work. Do not add certificate bypasses or continuously poll these endpoints.

## Reproduce the connectivity check

`api/probe.py` is a standard-library Python engineering probe, not Android production code. It requires explicit test identifiers and checks status, date and location:

```sh
python api/probe.py --zone SBH07 --weather Ds548
```

Sample successful responses under `api/samples/` are dated research fixtures, not automatic user configuration or live UI data.
