class ResponseAdapter {
    static success(res, status, data, message = "Success") {
        return res.status(status).json({ success: true, data, message });
    }

    static error(res, status, message) {
        return res.status(status).json({ success: false, message });
    }
}

module.exports = ResponseAdapter;
