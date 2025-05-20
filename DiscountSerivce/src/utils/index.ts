import mongoose, { Schema } from "mongoose";

export const validateObjectId = (value: string): boolean => {
    const ObjectId = mongoose.Types.ObjectId;
    return ObjectId.isValid(value);
};

export const convertToObjectId = (value: string): Schema.Types.ObjectId => {
    return new mongoose.Schema.ObjectId(value);
};
