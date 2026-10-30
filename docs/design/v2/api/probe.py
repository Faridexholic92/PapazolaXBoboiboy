"""Official endpoint connectivity probe, not a wired Android widget.
Supply explicit sample identifiers; there is intentionally no default location.
"""
import argparse
import datetime as dt
import json
import re
import urllib.parse
import urllib.request
from zoneinfo import ZoneInfo


def get_json(url):
    req = urllib.request.Request(url, headers={'User-Agent': 'DuniaAyahFayyadh-api-probe/0.2'})
    with urllib.request.urlopen(req, timeout=20) as response:
        return json.load(response)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--zone', required=True, help='Explicit JAKIM zone, e.g. SBH07 (sample only)')
    parser.add_argument('--weather', required=True, help='Explicit MET/data.gov.my location ID, e.g. Ds548 (sample only)')
    args = parser.parse_args()
    if not re.fullmatch(r'[A-Z]{3}[0-9]{2}', args.zone):
        parser.error('Invalid JAKIM zone format')
    if not re.fullmatch(r'[A-Z][a-z][0-9]{3}', args.weather):
        parser.error('Invalid weather location ID format')
    today = dt.datetime.now(ZoneInfo('Asia/Kuala_Lumpur')).date().isoformat()
    prayer_url = 'https://www.e-solat.gov.my/index.php?' + urllib.parse.urlencode({
        'r': 'esolatApi/TakwimSolat', 'period': 'today', 'zone': args.zone})
    prayer = get_json(prayer_url)
    if prayer.get('status') != 'OK!' or prayer.get('zone') != args.zone:
        raise RuntimeError('JAKIM returned an invalid status or zone; no fallback sample is allowed')
    if not str(prayer.get('serverTime', '')).startswith(today):
        raise RuntimeError('JAKIM server date does not match the Malaysia calendar date')
    rows = prayer.get('prayerTime', [])
    if len(rows) != 1:
        raise RuntimeError('Expected exactly one today prayer record')
    for key in ['fajr', 'dhuhr', 'asr', 'maghrib', 'isha']:
        if not re.fullmatch(r'\d{2}:\d{2}:\d{2}', rows[0].get(key, '')):
            raise RuntimeError(f'Invalid prayer time: {key}')
    weather_url = 'https://api.data.gov.my/weather/forecast?' + urllib.parse.urlencode({
        'filter': args.weather + '@location__location_id',
        'date_start': today + '@date', 'date_end': today + '@date', 'limit': 7})
    forecasts = get_json(weather_url)
    if not isinstance(forecasts, list) or len(forecasts) != 1:
        raise RuntimeError('Expected exactly one forecast for the explicit location and today')
    forecast = forecasts[0]
    if forecast.get('date') != today or forecast.get('location', {}).get('location_id') != args.weather:
        raise RuntimeError('Weather date or location mismatch')
    if not isinstance(forecast.get('min_temp'), (int, float)) or not isinstance(forecast.get('max_temp'), (int, float)):
        raise RuntimeError('Missing forecast temperature range')
    print(json.dumps({
        'date_malaysia': today, 'prayer_source': prayer_url, 'prayer': prayer,
        'weather_source': weather_url, 'forecast_not_observation': forecast,
        'note': 'Explicit test identifiers only; this does not configure a user location or wire the APK.'
    }, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
