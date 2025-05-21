const pickFields = (product) => {
    const requiredFields = [
        "_id",
        "name",
        "description",
        "price",
        "category",
        "ratings",
        "tags",
    ];

    const result = requiredFields.reduce((acc, field) => {
        if (field in product) {
            acc[field] = product[field];
        }
        return acc;
    }, {});

    if (product.attribute && product.attribute.brand) {
        result.brand = product.attribute.brand;
    }

    return result;
};

module.exports = pickFields;
