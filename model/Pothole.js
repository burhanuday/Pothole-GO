const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const pothole = new Schema({
  location: {
    lat: {
      type: Number,
      required: true
    },
    lng: {
      type: Number,
      required: true
    }
  },
  images: {
    original: Array,
    processed: Array
  },
  height: {
    type: Number
  },
  timestamp: {
    type: Date,
    required: true
  },
  length: {
    type: Number,
    required: true
  },
  width: {
    type: Number,
    required: true
  }
});

pothole.index({
  location: "2dsphere"
});

const Pothole = mongoose.model("pothole", pothole);
module.exports = Pothole;
