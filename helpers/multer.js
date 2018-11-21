const format = require("util").format;
const multer = require("multer");
const { Storage } = require("@google-cloud/storage");

// Multer Functions
const storage = new Storage({ projectId: "potholego" });

const bucket = storage.bucket("potholego.appspot.com");

const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 1024 * 1024 * 5
  }
});

const uploadImageToStorage = file => {
  let prom = new Promise((resolve, reject) => {
    if (!file) {
      reject("No image file");
    }
    let newFileName = `${file.originalname}_${Date.now()}`;

    let fileUpload = bucket.file(newFileName);

    const blobStream = fileUpload.createWriteStream({
      metadata: {
        contentType: file.mimetype
      }
    });

    blobStream.on("error", error => {
      reject(error);
    });

    blobStream.on("finish", () => {
      // The public URL can be used to directly access the file via HTTP.
      const url = format(
        `https://storage.googleapis.com/${bucket.name}/${fileUpload.name}`
      );
      resolve(url);
    });

    blobStream.end(file.buffer);
  });
  return prom;
};

module.exports = {
  storage: storage,
  upload: upload,
  uploadImageToStorage: uploadImageToStorage
};
