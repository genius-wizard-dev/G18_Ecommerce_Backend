const ROLES_LIST = require("../config/rolesList");
const Product = require("../models/product");

function normalizeVietnamese(str) {
    return str
        .toLowerCase()
        .normalize("NFD") // decompose characters
        .replace(/[\u0300-\u036f]/g, "") // remove diacritics
        .replace(/đ/g, "d")
        .replace(/Đ/g, "D");
}

class ProductRepository {
    async create(productData) {
        const product = new Product(productData);
        return await product.save();
    }

    async findAll({ page, limit, category, shopId, search }) {
        const query = { isActive: true };

        if (category) query.category = category;
        if (shopId) query.shopId = shopId;

        if (search) {
            if (search.length <= 2) {
                const normalizedSearch = normalizeVietnamese(search);
                query.$or = [
                    { name: { $regex: new RegExp(`^${search}`, "i") } },
                    {
                        name: {
                            $regex: new RegExp(`^${normalizedSearch}`, "i"),
                        },
                    },
                    { name: { $regex: new RegExp(`\\b${search}`, "i") } },
                    {
                        name: {
                            $regex: new RegExp(`\\b${normalizedSearch}`, "i"),
                        },
                    },
                    { name: { $regex: new RegExp(search, "i") } },
                    { name: { $regex: new RegExp(normalizedSearch, "i") } },
                    { tags: { $regex: new RegExp(search, "i") } },
                ];
            } else {
                query.$text = { $search: normalizeVietnamese(search) };
            }
            console.log(query);
        }

        let sortCriteria;
        if (search) {
            if (search.length <= 2) {
                const normalizedSearch = normalizeVietnamese(search);
                sortCriteria = [
                    {
                        $addFields: {
                            exactWordMatch: {
                                $cond: [
                                    {
                                        $regexMatch: {
                                            input: "$name",
                                            regex: new RegExp(
                                                `^${search}\\b`,
                                                "i"
                                            ),
                                        },
                                    },
                                    1,
                                    0,
                                ],
                            },
                            normalizedWordMatch: {
                                $cond: [
                                    {
                                        $regexMatch: {
                                            input: "$name",
                                            regex: new RegExp(
                                                `^${normalizedSearch}\\b`,
                                                "i"
                                            ),
                                        },
                                    },
                                    1,
                                    0,
                                ],
                            },
                            nameStartsWithSearch: {
                                $cond: [
                                    {
                                        $regexMatch: {
                                            input: "$name",
                                            regex: new RegExp(
                                                `^${search}`,
                                                "i"
                                            ),
                                        },
                                    },
                                    1,
                                    0,
                                ],
                            },
                            nameStartsWithNormalized: {
                                $cond: [
                                    {
                                        $regexMatch: {
                                            input: "$name",
                                            regex: new RegExp(
                                                `^${normalizedSearch}`,
                                                "i"
                                            ),
                                        },
                                    },
                                    1,
                                    0,
                                ],
                            },
                        },
                    },
                    {
                        $sort: {
                            exactWordMatch: -1,
                            normalizedWordMatch: -1,
                            nameStartsWithSearch: -1,
                            nameStartsWithNormalized: -1,
                            count: -1,
                        },
                    },
                ];
            } else {
                sortCriteria = { score: { $meta: "textScore" } };
            }
        } else {
            sortCriteria = { count: -1 };
        }

        let products;
        if (search && search.length <= 2) {
            products = await Product.aggregate([
                { $match: query },
                ...sortCriteria,
                { $skip: (page - 1) * limit },
                { $limit: limit },
            ]);
        } else {
            products = await Product.find(query)
                .skip((page - 1) * limit)
                .limit(limit)
                .sort(sortCriteria);
        }

        const total = await Product.countDocuments(query);
        return {
            products,
            total,
            page,
            pages: Math.ceil(total / limit),
        };
    }

    async findById(id) {
        return await Product.findById(id).where({ isActive: true });
    }

    async update(id, updateData, roles) {
        const product = await Product.findById(id);

        if (!product) {
            return { error: 404, message: "Product not found" };
        }

        if (!roles.includes(ROLES_LIST.Admin)) {
            if (!updateData.shopId)
                return { error: 400, message: "shopId is required for user" };

            if (updateData.shopId !== product.shopId)
                return { error: 403, message: "shopId mismatch" };
        }

        if (updateData.category && updateData.category !== product.category) {
            return { error: 400, message: "Category mismatch" };
        }
        if (updateData.attribute) {
            updateData.attribute = {
                ...product.attribute,
                ...updateData.attribute,
            };
        }
        return await Product.findByIdAndUpdate(
            id,
            { $set: updateData },
            { new: true, runValidators: true }
        );
    }

    async delete(id, shopId, roles) {
        const product = await Product.findById(id);
        if (!product) {
            return { error: 404, message: "Product not found" };
        }

        if (!roles.includes(ROLES_LIST.Admin)) {
            if (!shopId)
                return { error: 400, message: "shopId is required for user" };

            if (shopId !== product.shopId) {
                return { error: 403, message: "shopId mismatch" };
            }
        }

        return await Product.findByIdAndUpdate(
            id,
            { $set: { isActive: false } },
            { new: true }
        );
    }
}

module.exports = new ProductRepository(); // Singleton instance
