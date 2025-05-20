import { type Request, type Response, type NextFunction } from "express";
import createError from "http-errors";
import { DiscountInput, UpdatedDiscountInput } from "../validation/discount.schema";
import DiscountService from "../services/discount.service";
import { Code } from "../shared/code";
import Discount, { DiscountDocument } from "../models/discount.model";
import { Cart, CartItem } from "../shared";
import { convertToObjectId } from "../utils";

class DiscountController {
    async createDiscountHandler(req: Request<{}, {}, DiscountInput["body"]>, res: Response, next: NextFunction) {
        try {
            const discountData: DiscountInput["body"] = req.body;

            const discount = await DiscountService.createDiscount(discountData);

            res.status(201).json({
                message: "Create discount successful",
                data: discount,
                code: Code.SUCCESS
            });
        } catch (error) {
            next(error);
        }
    }

    async applyDiscountHandler(req: Request<{}, {}, Cart>, res: Response, next: NextFunction) {
        try {
            const { discountId, userId, cartItems } = req.body;
            console.log(req.body);
            const discount = await Discount.findById(discountId);
            console.log(discount);
            let totalPrice = 0;
            let occurrences = 0;
            let discountValue = 0;
            let quantityPerUser = 0;
            let appliedDiscountNum = 0;
            let discountType = "";
            let appliedProductType = "";
            let appliedProductList: string[];
            let newCartItemList: CartItem[] = [];
            let userIdList: String[] = [];

            if (discount == null) {
                res.status(201).json({
                    message: "Discount does not exist",
                    data: null,
                    code: Code.DISCOUNT_NOT_FOUND
                });
            } else {
                discountValue = discount.discount_value;
                discountType = discount.discount_type;
                quantityPerUser = discount.quantity_per_user;
                appliedProductList = discount.applied_product_list;
                appliedProductType = discount.applied_product_type;

                occurrences = discount.used_user_list.reduce((total, user) => {
                    if (user.toString() === userId.toString()) return ++total;
                    return total;
                }, 0);

                cartItems.forEach((cartItem) => {
                    if (cartItem.appliedDiscount) appliedDiscountNum += 1;
                });

                if (occurrences + appliedDiscountNum > quantityPerUser) {
                    res.status(201).json({
                        message: "Discount does not enough",
                        data: null,
                        code: Code.DISCOUNT_NOT_ENOUGH
                    });
                } else {
                    cartItems.forEach((cartItem) => {
                        const cond1 =
                            appliedProductType === "all" && cartItem.shopId.toString() === discount.shop.toString();
                        const cond2 =
                            appliedProductList?.includes(cartItem.productId.toString()) &&
                            appliedProductType === "specific";

                        if ((cond1 || cond2) && cartItem.appliedDiscount) {
                            let finalPrice =
                                discountType === "fixed"
                                    ? cartItem.price - discountValue
                                    : cartItem.price * (1 - discountValue);

                            userIdList.push(userId);
                            if (finalPrice < 0) finalPrice = 0;

                            newCartItemList.push({
                                ...cartItem,
                                finalPrice,
                                discountId
                            });
                        } else {
                            newCartItemList.push({
                                ...cartItem,
                                finalPrice: cartItem.price,
                                discountId
                            });
                        }

                        const currentCartItem = newCartItemList[newCartItemList.length - 1];
                        totalPrice += currentCartItem.finalPrice * currentCartItem.quantity;
                    });

                    await DiscountService.addToUserUsedList(discountId, userIdList);

                    const cart = {
                        ...req.body,
                        id: req.body.cartId,
                        cartItems: newCartItemList,
                        totalPrice
                    };

                    res.status(201).json({
                        message: "Apply discount successful",
                        data: cart,
                        code: Code.SUCCESS
                    });
                }
            }
        } catch (error) {
            console.log(error);
            next(error);
        }
    }

    async getDiscountsHandler(req: Request, res: Response, next: NextFunction) {
        try {
            const shopId = req.query.shopId as string;
            const productId = req.query.productId as string;

            const discounts = await DiscountService.getDiscounts(shopId, productId);

            res.status(200).json({
                message: "Get discount successful",
                data: discounts,
                code: Code.SUCCESS
            });
        } catch (error) {
            next(error);
        }
    }

    async getDiscountByIdHandler(req: Request, res: Response, next: NextFunction) {
        try {
            const discountId: string = req.params.discountId;

            const discount = await DiscountService.getDiscount(discountId);

            res.status(200).json({
                message: "Get discount successful",
                data: discount,
                code: Code.SUCCESS
            });
        } catch (error) {
            next(error);
        }
    }

    async updateDiscountHandler(
        req: Request<UpdatedDiscountInput["params"], {}, UpdatedDiscountInput["body"]>,
        res: Response,
        next: NextFunction
    ) {
        try {
            const discountId: string = req.params.discountId;
            const updateDiscountData: UpdatedDiscountInput["body"] = req.body;

            const updatedDiscount: DiscountDocument = await DiscountService.updateDiscount(
                discountId,
                updateDiscountData
            );

            res.status(200).json({
                message: "Update discount successful",
                data: updatedDiscount,
                code: Code.SUCCESS
            });
        } catch (error) {
            next(error);
        }
    }

    async deleteDiscountHandler(req: Request, res: Response, next: NextFunction) {
        try {
            const discountId: string = req.params.discountId;

            if (!discountId) throw createError.BadRequest("Invalid discount id");

            await DiscountService.deleteDiscount(discountId);

            res.status(200).json({
                message: "Delete discount successful",
                code: Code.SUCCESS
            });
        } catch (error) {
            next(error);
        }
    }
}

export default new DiscountController();
