const router = require("express").Router();
const jwt = require("jsonwebtoken");
const UserModel = require("../model/User");

// Check if the user already exists in the database. If not, create a document and release a new token. Else, just release a new token
router.post("/login", (req, res) => {
    console.log(req.query);

  let user = {
    username: req.query.username,
    email: req.query.email
  };

  UserModel.findOne({
    username: user.username,
    email: user.email
  })
    .then(user => {
      if (user) {
        console.log("existing user")
        jwt.sign(
          { user },
          "secretkey",
          { expiresIn: "365d" },
          (err, token) => {
            res.json({
              success: 2,
              user: user,
              token: token
            });
          }
        );
      } else {
        const newUser = new UserModel({
          username: req.query.username,
          email: req.query.email
        });

        newUser
          .save()
          .then(success => {
            console.log("new user")
            jwt.sign(
              { user },
              "secretkey",
              { expiresIn: "365d" },
              (err, token) => {
                res.json({
                  success: 1,
                  user: user,
                  token: token
                });
              }
            );
          })
          .catch(err => console.log(err));
      }
    })
    .catch(err => {
      console.log(err);
    });
});

module.exports = router;
