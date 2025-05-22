const { readConfig, produce, consume } = require("../kafka");
const productRepository = require("../repositories/productsRepository");
const pickFields = require("../utils/mapProduct");
const ResponseAdapter = require("../utils/response");

const productController = {
    async createProduct(req, res) {
        try {
            const product = await productRepository.create(req.body);
            // const config = readConfig("client.properties");
            // const producerTopic = "create-product";
            // const consumerTopic = "product-created";

            // await produce(producerTopic, config, pickFields(product));
            // await consume(consumerTopic, config);
            ResponseAdapter.success(res, 201, product, "Product created");
        } catch (error) {
            ResponseAdapter.error(res, 400, error.message);
        }
    },

    async getProducts(req, res) {
        try {
            const {
                page = 1,
                limit = 10,
                category,
                shopId,
                search,
            } = req.query;
            const products = await productRepository.findAll({
                page: parseInt(page),
                limit: parseInt(limit),
                category,
                shopId,
                search,
            });
            ResponseAdapter.success(res, 200, products);
        } catch (error) {
            ResponseAdapter.error(res, 400, error.message);
        }
    },

    async getProductById(req, res) {
        try {
            const product = await productRepository.findById(req.params.id);
            if (!product)
                return ResponseAdapter.error(res, 404, "Product not found");
            ResponseAdapter.success(res, 200, product);
        } catch (error) {
            ResponseAdapter.error(res, 400, error.message);
        }
    },

    async updateProduct(req, res) {
        try {
            const product = await productRepository.update(
                req.params.id,
                req.body,
                req.roles
            );
            if (product.error)
                return ResponseAdapter.error(
                    res,
                    product.error,
                    product.message
                );
            ResponseAdapter.success(res, 200, product);
        } catch (error) {
            ResponseAdapter.error(res, 400, error.message);
        }
    },

    async deleteProduct(req, res) {
        try {
            const product = await productRepository.delete(
                req.params.id,
                req.body.shopId,
                req.roles
            );
            if (product.error)
                return ResponseAdapter.error(
                    res,
                    product.error,
                    product.message
                );
            ResponseAdapter.success(
                res,
                200,
                null,
                "Product deleted successfully"
            );
        } catch (error) {
            ResponseAdapter.error(res, 400, error.message);
        }
    },
};

module.exports = productController;
