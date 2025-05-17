const { Kafka } = require("kafkajs");
const inventory = require("../models/inventory");

const kafka = new Kafka({
    clientId: "inventory-service",
    brokers: ["d09jle9qploprptlaneg.any.ap-south-1.mpx.prd.cloud.redpanda.com:9092"], // ✅ dùng 9093 nếu sử dụng TLS
    ssl: {},
    sasl: {
        mechanism: "scram-sha-512",
        username: "g18ecommerce",
        password: "tTozU3DovkwkLqTGSBbaUNrEC2Dy0B"
    }
});

const consumer = kafka.consumer({ groupId: "inventory-group" });

const createTopic = async () => {
    const admin = kafka.admin();

    try {
        await admin.connect();
        console.log("✅ Kafka connected successfully");

        const topicName = "subtract-inventory-topic";
        const topics = await admin.listTopics();

        if (!topics.includes(topicName)) {
            await admin.createTopics({
                topics: [
                    {
                        topic: topicName,
                        numPartitions: 1,
                        replicationFactor: 1
                    }
                ]
            });
            console.log(`✅ Kafka topic '${topicName}' created.`);
        } else {
            console.log(`ℹ️ Kafka topic '${topicName}' already exists.`);
        }
    } catch (error) {
        console.error("❌ Error creating topic:", error);
    } finally {
        await admin.disconnect();
    }
};

const runKafka = async () => {
    await createTopic();
    await consumer.connect();
    await consumer.subscribe({
        topic: "subtract-inventory-topic",
        fromBeginning: false
    });

    await consumer.run({
        eachMessage: async ({ topic, partition, message }) => {
            try {
                const rawValue = message.value?.toString();
                const data = JSON.parse(JSON.parse(rawValue));
                const { productId, quantity } = data;

                const quantityNum = Number(quantity);
                if (isNaN(quantityNum) || quantityNum <= 0) {
                    console.error(`[Inventory] Invalid quantity value: ${quantity} for product ${productId}`);
                    return;
                }

                console.log(`[Inventory] Attempting to subtract ${quantityNum} from product ${productId}`);

                const updated = await inventory.updateOne(
                    { product_id: productId, total_quantity: { $gte: quantityNum } },
                    { $inc: { total_quantity: -quantityNum } }
                );

                if (updated.modifiedCount === 0) {
                    console.warn(`[Inventory] Not enough stock for product ${productId}`);
                } else {
                    console.log(`[Inventory] Successfully subtracted ${quantityNum} from product ${productId}`);
                }
            } catch (error) {
                console.error("[Inventory] Error processing message:", error);
            }
        }
    });
};

module.exports = runKafka;
