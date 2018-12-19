const express = require("express");
const methodOverride = require("method-override");
const potholeController = require("../controllers/pothole");
const multerFunctions = require("../helpers/multer");
const verifyToken = require("../helpers/verifyToken");
const api = express.Router();

// Middleware
api.use(methodOverride("_method"));

// Register a pothole
api.post(
  "/create",
  verifyToken,
  multerFunctions.upload.array("images", 4),
  potholeController.createAPothole
);

// Get all potholes
api.get("/potholes", verifyToken, potholeController.getAllPotholes);

// Upload processed images and potholes parameters
api.post(
  "/update/:id",
  verifyToken,
  multerFunctions.upload.array("images", 4),
  potholeController.updatePothole
);

module.exports = api;
