module.exports = data => {
  var spawn = require("child_process").spawn,
    py = spawn("python", [
      "../Pothole-GO-python/test_sizeDetection/main_run.py"
    ]),
    dataString = "";

  py.stdout.on("data", function(data) {
      console.log(data);
      dataString += data.toString();
  });
  py.stdout.on("end", function() {
    console.log("Sum of numbers=", dataString);
  });
  py.stdin.write(JSON.stringify(data));
  py.stdin.end();
};
