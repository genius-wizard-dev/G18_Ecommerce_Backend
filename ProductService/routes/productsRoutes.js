const express = require("express");
const router = express.Router();
const productController = require("../controllers/productsController");
const ROLES_LIST = require("../config/rolesList");
const verifyRoles = require("../middleware/verifyRoles");

// Create a new product
router.post(
    "/",
    verifyRoles(ROLES_LIST.Admin, ROLES_LIST.User),
    productController.createProduct
);

// Get all products with pagination, filtering, and search
router.get(
    "/",
    verifyRoles(ROLES_LIST.Admin, ROLES_LIST.User),
    productController.getProducts
);

// Get a single product by ID
router.get(
    "/:id",
    verifyRoles(ROLES_LIST.Admin, ROLES_LIST.User),
    productController.getProductById
);

// Update a product
router.put(
    "/:id",
    verifyRoles(ROLES_LIST.Admin, ROLES_LIST.User),
    productController.updateProduct
);

// Delete a product (soft delete)
router.delete(
    "/:id",
    verifyRoles(ROLES_LIST.Admin, ROLES_LIST.User),
    productController.deleteProduct
);

module.exports = router;
