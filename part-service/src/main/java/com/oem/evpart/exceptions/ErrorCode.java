package com.oem.evpart.exceptions;

public enum ErrorCode {

    // =========================
    // 🧱 Hệ thống chung
    // =========================
    UNCATEGORIZED_EXCEPTION(999, "Lỗi không xác định"),
    INVALID_REQUEST(1000, "Dữ liệu gửi lên không hợp lệ"),
    NOT_FOUND(1001, "Không tìm thấy dữ liệu yêu cầu"),

    // =========================
    // ⚙️ Part (Phụ tùng)
    // =========================
    PART_NOT_FOUND(2001, "Không tìm thấy phụ tùng"),
    PART_ALREADY_EXISTS(2002, "Phụ tùng đã tồn tại"),
    PART_SERIAL_DUPLICATED(2003, "Số serial của phụ tùng đã tồn tại"),
    PART_NAME_REQUIRED(2004, "Tên phụ tùng không được để trống"),
    PART_TYPE_INVALID(2005, "Loại phụ tùng không hợp lệ"),

    // =========================
    // 📜 Warranty Policy (Chính sách bảo hành)
    // =========================
    WARRANTY_POLICY_NOT_FOUND(2101, "Không tìm thấy chính sách bảo hành"),
    WARRANTY_POLICY_EXISTED(2102, "Chính sách bảo hành đã tồn tại cho phụ tùng này"),
    WARRANTY_POLICY_INVALID(2103, "Thông tin chính sách bảo hành không hợp lệ"),

    // =========================
    // 🏢 Part Inventory (Tồn kho)
    // =========================
    INVENTORY_NOT_FOUND(2201, "Không tìm thấy kho phụ tùng"),
    INVENTORY_DUPLICATED(2202, "Kho phụ tùng đã tồn tại tại vị trí này"),
    INVENTORY_NOT_ENOUGH(2203, "Số lượng tồn kho không đủ"),
    INVENTORY_STATUS_INVALID(2204, "Trạng thái kho không hợp lệ"),

    // =========================
    // 🚚 Part Allocation (Phân bổ phụ tùng)
    // =========================
    ALLOCATION_NOT_FOUND(2301, "Không tìm thấy bản ghi phân bổ phụ tùng"),
    ALLOCATION_INVALID(2302, "Thông tin phân bổ không hợp lệ"),
    ALLOCATION_QUANTITY_EXCEEDS(2303, "Số lượng phân bổ vượt quá tồn kho"),
    SERVICE_CENTER_NOT_FOUND(2304, "Không tìm thấy trung tâm dịch vụ yêu cầu"),

    // =========================
    // 🔩 Installed Part (Phụ tùng gắn vào xe)
    // =========================
    INSTALLED_PART_NOT_FOUND(2401, "Không tìm thấy phụ tùng đã gắn"),
    INSTALLED_PART_INVALID(2402, "Thông tin phụ tùng gắn không hợp lệ"),
    VEHICLE_VIN_REQUIRED(2403, "VIN của xe là bắt buộc"),
    INSTALL_STATUS_INVALID(2404, "Trạng thái lắp đặt không hợp lệ");

    // =========================
    // ⚙️ Fields & Constructors
    // =========================
    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
