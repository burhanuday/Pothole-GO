const express = require("express");
const bodyParser = require("body-parser");
const mongoose = require("mongoose");
require("dotenv").config();
const app = express();

// Mongoose middleware
mongoose.connect(process.env.MONGODB);
let db = mongoose.connection;

db.on("error", console.error.bind(console, "connection error: "));
db.once("open", () => {
  console.log("MongoDB is connected to the server....");
});

// Body Parser middleware
app.use(
  bodyParser.urlencoded({
    extended: true
  })
);
app.use(bodyParser.json());
app.use("/uploads", express.static("uploads"));

// Default route
app.get("/api/v1", (req, res) => {
  res.json("Pothole Go");
});

let port = process.env.PORT || 7000;
app.listen(port, (req, res) => {
  console.log(`Server started on port ${port}`);
});

// Import routes
const potholeRoute = require("./routes/Potholes");
const loginRoute = require("./routes/Login");
app.use("/api/v1/", potholeRoute);
app.use("/api/v1/", loginRoute);