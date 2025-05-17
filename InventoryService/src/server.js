require("dotenv").config();
const express = require("express");
const connectDB = require("./config/database");
const morgan = require("morgan");
const app = express();

app.use(morgan("dev"));
app.use(express.json());

const inventoryRoutes = require("./routes/inventory");
const { connectKafka, consumer, createTopic } = require("./kafka/events/kafka");
const handleOrderCreated = require("./kafka/listener/orderCreatedListener");
const handlePaymentFailed = require("./kafka/listener/paymentFailedListener");
const handlePaymentCompleted = require("./kafka/listener/paymentCompletedListener");
app.use("/api/inventories", inventoryRoutes);

const PORT = process.env.PORT || 5000;
app.listen(PORT, async () => {
    await connectDB();
    await connectKafka();
    await createTopic();

    await Promise.allSettled([
        consumer.subscribe({ topic: "order-created" }),
        consumer.subscribe({ topic: "payment-failed" }),
        consumer.subscribe({ topic: "payment-completed" })
    ]);

    await consumer.run({
        eachMessage: async ({ topic, message }) => {
            const data = JSON.parse(JSON.parse(message.value?.toString()));
            console.log(data);
            console.log(topic);
            switch (topic) {
                case "order-created":
                    await handleOrderCreated(data);
                    break;
                case "payment-completed":
                    await handlePaymentCompleted(data);
                    break;
                case "payment-failed":
                    await handlePaymentFailed(data);
                    break;
            }
        }
    });
    console.log(`🚀 Server running on port ${PORT}`);
});
