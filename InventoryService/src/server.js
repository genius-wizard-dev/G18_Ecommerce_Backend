require('dotenv').config();
const express = require('express');
const connectDB = require('./config/database');

const app = express();
connectDB();

app.use(express.json());

// Import routes
const inventoryRoutes = require('./routes/inventory');
app.use('/api/inventory', inventoryRoutes);

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`🚀 Server running on port ${PORT}`));
