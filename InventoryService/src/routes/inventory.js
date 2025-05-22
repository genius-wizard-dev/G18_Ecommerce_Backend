const express = require("express");
const inventoryController = require("../controllers/inventoryController");

const router = express.Router();

router.get("/", inventoryController.getInventory);
router.get("/products/:id", inventoryController.getInventoryByProduct);
router.post("/", inventoryController.createInventory);
router.put("/:id", inventoryController.updateInventory);
router.put("/products/:id", inventoryController.updateInventoryByProduct);
router.post("/:id/reserve", inventoryController.reserveStock);
router.put("/:id/confirm", inventoryController.confirmReservation);
router.get("/check-stock", inventoryController.checkInventory);
router.get("/shops/:id", inventoryController.getInventoriesByShop);

module.exports = router;
