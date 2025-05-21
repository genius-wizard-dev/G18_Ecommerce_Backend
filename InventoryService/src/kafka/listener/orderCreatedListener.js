const { checkInventory } = require("../../controllers/inventoryController");
const inventory = require("../../models/inventory");
const { producer } = require("../events/kafka");

const handleOrderCreated = async (data) => {
    try {
        const inventoryItems = data.inventoryItems;
        const result = await checkInventory(inventoryItems);

        const SOLD_OUT_LIST = result.reduce((arr, item) => {
            if (!item.isInStock) return [...arr, item.name];
            return arr;
        }, []);

        if (SOLD_OUT_LIST.length > 0) {
            await producer.send({
                topic: "stock-reservation-failed",
                messages: [
                    {
                        key: data.orderNumber,
                        value: JSON.stringify({
                            orderNumber: data.orderNumber,
                            reason: `${SOLD_OUT_LIST.join(" and ")} SOLD OUT`
                        })
                    }
                ]
            });
            return;
        }

        const res = await Promise.allSettled(
            result.map((item) =>
                inventory.updateOne({ product_id: item.id }, { $inc: { reserved_quantity: item.reservedQuantity } })
            )
        );

        await producer.send({
            topic: "stock-reservation-success",
            messages: [
                {
                    key: data.orderNumber,
                    value: JSON.stringify({
                        userId: data.userId,
                        paymentMethod: data.paymentMethod,
                        orderId: data.orderNumber,
                        currency: data.currency,
                        amount: data.amount,
                        description: data?.description || "",
                        ipAddress: data.ipAddress,
                        inventoryItems
                    })
                }
            ]
        });
    } catch (error) {
        await producer.send({
            topic: "stock-reservation-failed",
            messages: ["stock reservation failed"]
        });
        return;
    }
};

module.exports = handleOrderCreated;
