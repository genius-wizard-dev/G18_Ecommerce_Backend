import Discount, { DiscountDocument } from "../models/discount.model";
import { DiscountInput, UpdatedDiscountInput } from "../validation/discount.schema";
import createError from "http-errors";

class DiscountService {
    static async createDiscount(discountData: DiscountInput["body"]): Promise<DiscountDocument> {
        try {
            const { code } = discountData;

            const discount = await Discount.findOne({ code });

            if (discount) throw createError.Conflict("The discount code exists");

            const createdDiscount: DiscountDocument = await Discount.create(discountData);

            return createdDiscount.toObject();
        } catch (error) {
            throw error;
        }
    }
    static async getDiscounts(shopId?: string, productId?: string): Promise<DiscountDocument[]> {
        try {
            const query = {
                shop: shopId,
                applied_product_list: productId
            };

            console.log(query);

            if (!shopId) delete query.shop;
            if (!productId) delete query.applied_product_list;

            const discounts = await Discount.find(query);

            return discounts;
        } catch (error) {
            throw error;
        }
    }

    static async getDiscount(discountId: String): Promise<DiscountDocument | null> {
        try {
            const discount: DiscountDocument | null = await Discount.findById(discountId);
            return discount;
        } catch (error) {
            throw error;
        }
    }

    static async addToUserUsedList(discountId: String, userIdList: String[]): Promise<boolean> {
        try {
            await Discount.findByIdAndUpdate(discountId, {
                $push: { used_user_list: userIdList }
            });
            return true;
        } catch (error) {
            throw error;
        }
    }

    static async updateDiscount(
        discountId: String,
        discountData: UpdatedDiscountInput["body"]
    ): Promise<DiscountDocument> {
        try {
            const { code } = discountData;
            const discount = await Discount.findOne({ code });

            if (discount) throw createError.Conflict("Discount have been existed");

            const updatedDiscount = await Discount.findByIdAndUpdate(discountId, discountData, { new: true });

            if (!updatedDiscount) throw createError.NotFound("Discount does not exist");

            return updatedDiscount.toObject();
        } catch (error) {
            throw error;
        }
    }

    static async deleteDiscount(discountId: String): Promise<void> {
        try {
            await Discount.findByIdAndDelete(discountId);
        } catch (error) {
            throw error;
        }
    }
}

export default DiscountService;
