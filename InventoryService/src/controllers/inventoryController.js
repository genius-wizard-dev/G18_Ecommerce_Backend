const Inventory = require('../models/inventory');

const inventoryController = {

    createInventory: async (req, res) => {
        try {
            const { name, total_quantity } = req.body;

            const newItem = new Inventory({
                name,
                total_quantity,
                reserved_quantity: 0
            });

            await newItem.save();
            res.status(201).json(newItem);
        } catch (error) {
            console.error('Error creating inventory item:', error);
            res.status(500).json({ message: 'Error creating inventory item', error: error.message || error });
        }
    },

    getInventory: async (req, res) => {
        try {
            const inventory = await Inventory.find();
            res.json(inventory);
        } catch (error) {
            res.status(500).json({ message: 'Error fetching inventory', error });
        }
    },

    updateInventory: async (req, res) => {
        try {
            const { id } = req.params;
            const { total_quantity } = req.body;
            const inventory = await Inventory.findById(id);

            if (!inventory) {
                return res.status(404).json({ message: 'Inventory item not found' });
            }

            inventory.total_quantity = total_quantity;
            await inventory.save();
            res.json({ message: 'Inventory updated', inventory });
        } catch (error) {
            res.status(500).json({ message: 'Error updating inventory', error });
        }
    },

    reserveStock: async (req, res) => {
        try {
            const { id } = req.params;
            const { quantity } = req.body;
            const inventory = await Inventory.findById(id);

            if (!inventory) {
                return res.status(404).json({ message: 'Inventory item not found' });
            }

            if (inventory.total_quantity - inventory.reserved_quantity < quantity) {
                return res.status(400).json({ message: 'Not enough stock available' });
            }

            inventory.reserved_quantity += quantity;
            await inventory.save();
            res.json({ message: 'Stock reserved', inventory });
        } catch (error) {
            res.status(500).json({ message: 'Error reserving stock', error });
        }
    },

    confirmReservation: async (req, res) => {
        try {
            const { id } = req.params;
            const inventory = await Inventory.findById(id);

            if (!inventory) {
                return res.status(404).json({ message: 'Inventory item not found' });
            }

            if (inventory.reserved_quantity <= 0) {
                return res.status(400).json({ message: 'No reservation to confirm' });
            }

            inventory.total_quantity -= inventory.reserved_quantity;
            inventory.reserved_quantity = 0;
            await inventory.save();
            res.json({ message: 'Reservation confirmed, stock deducted', inventory });
        } catch (error) {
            res.status(500).json({ message: 'Error confirming reservation', error });
        }
    }
};

module.exports = inventoryController;
