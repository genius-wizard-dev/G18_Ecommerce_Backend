const Inventory = require("../models/inventory");

const inventoryController = {
    createInventory: async (req, res) => {
        try {
            const { product_id, total_quantity, product_name, shop_id } = req.body;
            const newItem = new Inventory({
                product_name,
                product_id,
                shop_id,
                total_quantity
            });

            await newItem.save();
            res.status(201).json(newItem);
        } catch (error) {
            console.error("Error creating inventory item:", error);
            res.status(500).json({ message: "Error creating inventory item", error: error.message || error });
        }
    },

    getInventory: async (req, res) => {
        try {
            const inventory = await Inventory.find();
            res.json(inventory);
        } catch (error) {
            res.status(500).json({ message: "Error fetching inventory", error });
        }
    },

    getInventoryByProduct: async (req, res) => {
        try {
            const { id } = req.params;
            const inventory = await Inventory.findOne({ product_id: id });

            res.json(inventory);
        } catch (error) {
            res.status(500).json({ message: "Error fetching inventory", error });
        }
    },

    updateInventory: async (req, res) => {
        try {
            const { id } = req.params;
            const { total_quantity } = req.body;
            const inventory = await Inventory.findById(id);

            if (!inventory) {
                return res.status(404).json({ message: "Inventory item not found" });
            }

            inventory.total_quantity = total_quantity;
            await inventory.save();
            res.json({ message: "Inventory updated", inventory });
        } catch (error) {
            res.status(500).json({ message: "Error updating inventory", error });
        }
    },

    updateInventoryByProduct: async (req, res) => {
        try {
            const { id } = req.params;
            const { total_quantity } = req.body;
            const inventory = await Inventory.findOne({ product_id: id });

            if (!inventory) {
                return res.status(404).json({ message: "Inventory item not found" });
            }

            inventory.total_quantity = total_quantity;
            await inventory.save();
            res.json({ message: "Inventory updated", inventory });
        } catch (error) {
            res.status(500).json({ message: "Error updating inventory", error });
        }
    },

    reserveStock: async (req, res) => {
        try {
            const { id } = req.params;
            const { quantity } = req.body;
            const inventory = await Inventory.findById(id);

            if (!inventory) {
                return res.status(404).json({ message: "Inventory item not found" });
            }

            if (inventory.total_quantity - inventory.reserved_quantity < quantity) {
                return res.status(400).json({ message: "Not enough stock available" });
            }

            inventory.reserved_quantity += quantity;
            await inventory.save();
            res.json({ message: "Stock reserved", inventory });
        } catch (error) {
            res.status(500).json({ message: "Error reserving stock", error });
        }
    },

    confirmReservation: async (req, res) => {
        try {
            const { id } = req.params;
            const inventory = await Inventory.findById(id);

            if (!inventory) {
                return res.status(404).json({ message: "Inventory item not found" });
            }

            if (inventory.reserved_quantity <= 0) {
                return res.status(400).json({ message: "No reservation to confirm" });
            }

            inventory.total_quantity -= inventory.reserved_quantity;
            inventory.reserved_quantity = 0;
            await inventory.save();
            res.json({ message: "Reservation confirmed, stock deducted", inventory });
        } catch (error) {
            res.status(500).json({ message: "Error confirming reservation", error });
        }
    },

    checkInventory: async (data) => {
        try {
            const params = {};
            const productIds = data.map((item) => {
                params[item.productId] = item.quantity;
                return item.productId;
            });

            const inventories = await Inventory.find({ product_id: { $in: productIds } });

            const result = inventories.map((item) => ({
                id: item.product_id,
                name: item.product_name,
                reservedQuantity: params[item.product_id],
                isInStock: item.total_quantity - item.reserved_quantity >= parseInt(params[item.product_id])
            }));

            return result;
        } catch (err) {
            throw err;
        }
    },

    getInventoriesByShop: async (req, res, next) => {
        try {
            const shopId = req.params.id;

            const inventories = await Inventory.find({ shop_id: shopId });

            res.json({
                message: "Get inventories successful",
                code: 1000,
                data: inventories
            });
        } catch (error) {
            throw error;
        }
    },
    subtractInventory: async (req, res) => {
        try {
            const items = req.body;

            for (const item of items) {
                const updated = await Inventory.updateOne(
                    { product_id: item.productId, total_quantity: { $gte: item.quantity } },
                    { $inc: { total_quantity: -item.quantity } }
                );

                if (updated.modifiedCount === 0) {
                    return res.status(400).json({
                        message: `Not enough stock for product: ${item.productId}`,
                        code: 4001
                    });
                }
            }

            res.json({
                message: "Inventory updated successfully",
                code: 1000
            });
        } catch (err) {
            console.error(err);
            res.status(500).json(ApiResponse.error("Failed to update inventory"));
        }
    }
};

module.exports = inventoryController;
