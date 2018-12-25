const PYShell = require("python-shell").PythonShell;
const path = require("path");

module.exports = data => {
  console.log(data);
  let pythonScriptPth = path.resolve(__dirname, "../Pothole-GO-python/test_sizeMixed/mixed2.py");
  console.log(pythonScriptPth);

  let pyshell = new PYShell(pythonScriptPth, {
    pythonPath: '/usr/local/bin/python3.6'
  });
  // console.log(pyshell);
  pyshell.send(JSON.stringify(data));
  pyshell.on("message", function(message) {
    // received a message sent from the Python script (a simple "print" statement)
    console.log(message);
  });

  // end the input stream and allow the process to exit
  pyshell.end(function(err) {
    if (err) {
      throw err;
    }

    console.log("finished");
  });
};
