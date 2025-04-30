const mongoose = require('mongoose');

const InventorySchema = new mongoose.Schema({
    total_quantity: { type: Number, required: true, default: 0 },
    reserved_quantity: { type: Number, required: true, default: 0 },
}, { timestamps: true });

module.exports = mongoose.model('Inventory', InventorySchema);
