import fs from 'node:fs/promises'
import https from 'node:https'

const BASE_URL = 'https://raw.githubusercontent.com/raqoon886/Local_HangJeongDong/master'
const SIDO_FILES = [
  '강원도',
  '경기도',
  '경상남도',
  '경상북도',
  '광주광역시',
  '대구광역시',
  '대전광역시',
  '부산광역시',
  '서울특별시',
  '세종특별자치시',
  '울산광역시',
  '인천광역시',
  '전라남도',
  '전라북도',
  '제주특별자치도',
  '충청남도',
  '충청북도',
]

const OUTPUT_PATH = new URL('../backend/src/main/resources/location/kma_location_grids.csv', import.meta.url)

function fetchText(url) {
  return new Promise((resolve, reject) => {
    https.get(url, response => {
      if (response.statusCode !== 200) {
        reject(new Error(`Failed to fetch ${url}: HTTP ${response.statusCode}`))
        response.resume()
        return
      }

      response.setEncoding('utf8')
      let body = ''
      response.on('data', chunk => {
        body += chunk
      })
      response.on('end', () => resolve(body))
    }).on('error', reject)
  })
}

function dfsGridFromLatLon(lat, lon) {
  const RE = 6371.00877
  const GRID = 5.0
  const SLAT1 = 30.0
  const SLAT2 = 60.0
  const OLON = 126.0
  const OLAT = 38.0
  const XO = 43
  const YO = 136
  const DEGRAD = Math.PI / 180.0

  const re = RE / GRID
  const slat1 = SLAT1 * DEGRAD
  const slat2 = SLAT2 * DEGRAD
  const olon = OLON * DEGRAD
  const olat = OLAT * DEGRAD

  let sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
  sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)

  let sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
  sf = (Math.pow(sf, sn) * Math.cos(slat1)) / sn

  let ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
  ro = (re * sf) / Math.pow(ro, sn)

  let ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5)
  ra = (re * sf) / Math.pow(ra, sn)

  let theta = lon * DEGRAD - olon
  if (theta > Math.PI) theta -= 2.0 * Math.PI
  if (theta < -Math.PI) theta += 2.0 * Math.PI
  theta *= sn

  return {
    nx: Math.floor(ra * Math.sin(theta) + XO + 0.5),
    ny: Math.floor(ro - ra * Math.cos(theta) + YO + 0.5),
  }
}

function ringCentroid(ring) {
  let area2 = 0
  let cx = 0
  let cy = 0

  for (let index = 0; index < ring.length - 1; index += 1) {
    const [x1, y1] = ring[index]
    const [x2, y2] = ring[index + 1]
    const cross = x1 * y2 - x2 * y1
    area2 += cross
    cx += (x1 + x2) * cross
    cy += (y1 + y2) * cross
  }

  if (area2 === 0) {
    const [sumX, sumY] = ring.reduce(([x, y], [nextX, nextY]) => [x + nextX, y + nextY], [0, 0])
    return { area: 0, lon: sumX / ring.length, lat: sumY / ring.length }
  }

  return {
    area: area2 / 2,
    lon: cx / (3 * area2),
    lat: cy / (3 * area2),
  }
}

function geometryCentroid(geometry) {
  const polygons = geometry.type === 'Polygon' ? [geometry.coordinates] : geometry.coordinates
  let totalArea = 0
  let weightedLon = 0
  let weightedLat = 0

  for (const polygon of polygons) {
    const exterior = polygon[0]
    const centroid = ringCentroid(exterior)
    const area = Math.abs(centroid.area)
    totalArea += area
    weightedLon += centroid.lon * area
    weightedLat += centroid.lat * area
  }

  if (totalArea === 0) {
    throw new Error(`Cannot calculate centroid for geometry type ${geometry.type}`)
  }

  return {
    lon: weightedLon / totalArea,
    lat: weightedLat / totalArea,
  }
}

function parseAdministrativeName(properties) {
  const sido = properties.sidonm.trim()
  const sigungu = (properties.sggnm ?? '').trim()
  let dong = properties.adm_nm.trim()

  if (dong.startsWith(sido)) {
    dong = dong.slice(sido.length).trim()
  }
  if (sigungu && dong.startsWith(sigungu)) {
    dong = dong.slice(sigungu.length).trim()
  }

  return { sido, sigungu, dong }
}

function csvEscape(value) {
  const text = String(value ?? '')
  return /[",\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text
}

const rowsByKey = new Map()

for (const sido of SIDO_FILES) {
  const fileName = `hangjeongdong_${sido}.geojson`
  const url = `${BASE_URL}/${encodeURIComponent(fileName)}`
  const geoJson = JSON.parse(await fetchText(url))

  for (const feature of geoJson.features) {
    const { sido: sidonm, sigungu, dong } = parseAdministrativeName(feature.properties)
    const { lat, lon } = geometryCentroid(feature.geometry)
    const { nx, ny } = dfsGridFromLatLon(lat, lon)
    const key = `${sidonm}|${sigungu}|${dong}`

    rowsByKey.set(key, [sidonm, sigungu, dong, nx, ny])
  }
}

const rows = [...rowsByKey.values()].sort((left, right) => {
  const leftKey = `${left[0]} ${left[1]} ${left[2]}`
  const rightKey = `${right[0]} ${right[1]} ${right[2]}`
  return leftKey.localeCompare(rightKey, 'ko')
})

const csv = [
  'sido,sigungu,dong,nx,ny',
  ...rows.map(row => row.map(csvEscape).join(',')),
].join('\n')

await fs.writeFile(OUTPUT_PATH, `${csv}\n`, 'utf8')
console.log(`Wrote ${rows.length} location grids to ${OUTPUT_PATH.pathname}`)
