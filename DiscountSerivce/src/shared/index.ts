import { Schema } from "mongoose";

export enum DBType {
    MONGODB = "MONGODB",
    REDIS = "REDIS",
    MYSQL = "MYSQL"
}

export enum Env {
    DEV = "DEV",
    PRO = "PRO",
    TEST = "TEST"
}

export interface Cart {
    discountId: string;
    userId: string;
    cartId: string;
    cartItems: [CartItem];
}

export interface CartItem {
    discountId: string;
    shopId: string;
    productId: string;
    quantity: number;
    price: number;
    finalPrice: number;
    appliedDiscount: boolean;
}
