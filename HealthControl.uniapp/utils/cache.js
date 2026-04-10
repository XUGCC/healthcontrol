const loginToken = "loginToken";

function SetLoginToken(value) {
  uni.setStorageSync(loginToken, value);
}

function GetLoginToken() {
  return uni.getStorageSync(loginToken);
}

function RemoveLoginToken() {
  uni.removeStorageSync(loginToken);
}
function GetLocation() {
  return uni.getStorageSync("location");
}
function SetLocation(value) {
  uni.setStorageSync("location", value);
}

function RemoveLocation(value) {
  uni.removeStorageSync("location");
}

export {
  GetLocation,
  GetLoginToken,
  RemoveLocation,
  RemoveLoginToken,
  SetLocation,
  SetLoginToken,
};
