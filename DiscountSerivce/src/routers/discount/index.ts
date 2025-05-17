import { Router } from "express";
import discountController from "../../controllers/discount.controller";
import validateResource from "../../middleware/validateResource";
import { DiscountSchema, UpdateDiscountSchema } from "../../validation/discount.schema";

const router: Router = Router();
router.post("/discounts", validateResource(DiscountSchema), discountController.createDiscountHandler);
router.post("/discounts/user-apply", discountController.applyDiscountHandler);
router.get("/discounts", discountController.getDiscountsHandler);
router.get("/discounts/:discountId", discountController.getDiscountByIdHandler);
router.patch(
    "/discounts/:discountId",
    validateResource(UpdateDiscountSchema),
    discountController.updateDiscountHandler
);
router.delete("/discounts/:discountId", discountController.deleteDiscountHandler);

export default router;
