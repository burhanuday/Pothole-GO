const router = require("express").Router();
const loginController = require("../controllers/login");

router.post("/login", loginController.loginUser);

module.exports = router;
