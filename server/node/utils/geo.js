function toRadians(value) {
  return (value * Math.PI) / 180;
}

function offsetCoordinates(lat, lng, dxMeters, dyMeters) {
  const cosLat = Math.max(0.15, Math.cos(toRadians(lat)));
  const outLat = lat + dyMeters / 111111;
  const outLng = lng + dxMeters / (111111 * cosLat);
  return { lat: outLat, lng: outLng };
}

module.exports = {
  toRadians,
  offsetCoordinates
};
