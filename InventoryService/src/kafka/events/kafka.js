const { Kafka } = require("kafkajs");
const { KAFKA_CLIENT_ID, KAFKA_BROKER, KAFKA_USERNAME, KAFKA_PASSWORD, KAFKA_MECHANISM } = process.env;
const topicNameList = ["stock-reservation-success", "stock-reservation-failed"];

const kafka = new Kafka({
    clientId: KAFKA_CLIENT_ID,
    brokers: [KAFKA_BROKER],
    ssl: {},
    sasl: {
        mechanism: KAFKA_MECHANISM,
        username: KAFKA_USERNAME,
        password: KAFKA_PASSWORD
    }
});

const producer = kafka.producer();
const consumer = kafka.consumer({ groupId: "inventory-group" });

const createTopic = async () => {
    const admin = kafka.admin();

    try {
        await admin.connect();

        const nonExistTopics = [];
        const topics = await admin.listTopics();

        topicNameList.forEach((topic) => {
            if (!topics.includes(topic)) nonExistTopics.push(topic);
        });

        await admin.createTopics({
            topics: nonExistTopics.map((topicName) => ({
                topic: topicName,
                numPartitions: 1,
                replicationFactor: 1
            }))
        });
    } catch (error) {
        console.error("❌ Error creating topic:", error);
    } finally {
        await admin.disconnect();
    }
};

const connectKafka = async () => {
    await producer.connect();
    await consumer.connect();
};

module.exports = { kafka, producer, consumer, connectKafka, createTopic };
