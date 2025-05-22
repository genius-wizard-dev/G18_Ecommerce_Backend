require("dotenv").config();
const jwt = require("jsonwebtoken");
const ResponseAdapter = require("../utils/response");

const verifyJWT = (req, res, next) => {
    const authHeader = req.headers.authorization || req.headers.Authorization;
    if (!authHeader?.startsWith("Bearer "))
        ResponseAdapter.error(res, 401, "Missing Bearer");

    const token = authHeader.split(" ")[1];

    jwt.verify(token, process.env.SECRET_KEY, (err, decoded) => {
        if (err) ResponseAdapter.error(res, 403, "Invalid token");
        // console.log(decoded);
        req.user = decoded.sub;
        const [role, _] = decoded.scope.split(" ");

        req.roles = [role];
        next();
    });
};

module.exports = verifyJWT;
