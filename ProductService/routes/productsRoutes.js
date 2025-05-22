const express = require("express");
const router = express.Router();
const productController = require("../controllers/productsController");
const ROLES_LIST = require("../config/rolesList");
const verifyRoles = require("../middleware/verifyRoles");
const verifyJWT = require("../middleware/verifyJWT");

// Create a new product
router.post(
    "/",
    verifyRoles(ROLES_LIST.Admin, ROLES_LIST.User),
    productController.createProduct
);

// Get all products with pagination, filtering, and search
router.get("/", productController.getProducts);

// Get a single product by ID
router.get("/:id", productController.getProductById);

// Update a product
router.put(
    "/:id",
    verifyJWT,
    verifyRoles(ROLES_LIST.Admin, ROLES_LIST.User),
    productController.updateProduct
);

// Delete a product (soft delete)
router.delete(
    "/:id",
    verifyJWT,
    verifyRoles(ROLES_LIST.Admin, ROLES_LIST.User),
    productController.deleteProduct
);

module.exports = router;
