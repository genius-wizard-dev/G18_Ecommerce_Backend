const inventory = require("../../models/inventory");

const handlePaymentFailed = async (data) => {
    const { inventoryItems } = data;

    await Promise.allSettled(
        inventoryItems.map((item) =>
            inventory.findOneAndUpdate(
                { product_id: item.productId },
                {
                    $inc: {
                        reserved_quantity: -item.quantity
                    }
                }
            )
        )
    );
};

module.exports = handlePaymentFailed;
