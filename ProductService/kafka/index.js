const fs = require("fs");
const { Kafka } = require("@confluentinc/kafka-javascript").KafkaJS;

function readConfig(fileName) {
    const data = fs.readFileSync(fileName, "utf8").toString().split("\n");
    return data.reduce((config, line) => {
        const [key, value] = line.split("=");
        if (key && value) {
            config[key] = value;
        }
        return config;
    }, {});
}

async function produce(topic, config, newProduct) {
    const producer = new Kafka().producer(config);

    await producer.connect();

    const produceRecord = await producer.send({
        topic,
        messages: [
            {
                key: String(newProduct._id),
                value: JSON.stringify({
                    id: newProduct._id,
                    name: newProduct.name,
                    description: newProduct.description,
                    price: newProduct.price,
                    category: newProduct.category,
                    ratings: newProduct.ratings,
                    tags: newProduct.tags,
                    brand: newProduct.brand,
                }),
            },
        ],
    });
    console.log(
        `Produced message to topic ${topic}:\n${JSON.stringify(
            newProduct,
            null,
            2
        )}\nKafka result: ${JSON.stringify(produceRecord, null, 2)}`
    );

    await producer.disconnect();
}

async function consume(topic, config) {
    const disconnect = () => {
        consumer.commitOffsets().finally(() => {
            consumer.disconnect();
        });
    };
    process.on("SIGTERM", disconnect);
    process.on("SIGINT", disconnect);

    config["group.id"] = "nodejs-group-1";
    config["auto.offset.reset"] = "earliest";
    const consumer = new Kafka().consumer(config);

    await consumer.connect();

    await consumer.subscribe({ topics: [topic] });

    consumer.run({
        eachMessage: async ({ topic, partition, message }) => {
            console.log(
                `Consumed message from topic ${topic}, partition ${partition}: key = ${message.key.toString()}, value = ${message.value.toString()}`
            );
        },
    });
}

module.exports = { readConfig, produce, consume };
