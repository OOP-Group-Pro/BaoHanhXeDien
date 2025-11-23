// src/scStaff.js

// 1. IMPORT (Đầy đủ)
import { checkAuth } from './utils/auth.js';
import { renderHeader } from './components/Header.js';
import { renderStaffSidebar } from './components/StaffSidebar.js';
import { getClaims, createClaim, getClaimDetails, getClaimHistory } from './services/warrantyService.js';
import { getCustomerNameByVin } from './services/vehicleService.js';
import { searchParts } from "./services/partService.js";
import { getUsersByRole } from "./services/userService.js";
import { api } from './services/apiClient.js';
import { setupVehicleLookup } from './JS/vehicle-lookup.js';
import { setupAppointmentsPage } from './JS/sc-appointments.js';



// --- BIẾN TOÀN CỤC ---
let selectedParts = [];
let claimListState = { currentPage: 0, size: 10, vin: '', claimCode: '', status: 'WAITING_APPROVAL' }; // ⬅️ SỬA LẠI THÀNH 'WAITING_APPROVAL'
let claimModalInstance = null;
let selectedFiles = []; // ⬅️ THÊM BIẾN NÀY

// --- 2. HÀM GLOBAL (CHO HTML 'onclick') ---
window.updatePartQuantity = (event) => {
    const index = parseInt(event.target.dataset.index, 10);
    const newQuantity = parseInt(event.target.value, 10);

    if (selectedParts[index] && newQuantity > 0) {
        selectedParts[index].quantity = newQuantity;
        console.log('LOG: Cập nhật SL:', selectedParts);
    }
};
window.removePart = (event) => {
    const index = parseInt(event.target.dataset.index, 10);

    if (typeof index !== 'undefined' && selectedParts[index]) {
        console.log('LOG: Xóa Part tại index:', index);
        selectedParts.splice(index, 1);
        renderSelectedPartsTable();
    }
};

// ⬇️ THÊM HÀM NÀY
window.removeFile = (event) => {
    const index = parseInt(event.target.dataset.index, 10);
    if (typeof index !== 'undefined' && selectedFiles[index]) {
        console.log('LOG: Xóa File tại index:', index);
        // Tạo một mảng mới từ mảng cũ, loại bỏ file tại index
        const dt = new DataTransfer();
        const newFiles = selectedFiles.filter((_, i) => i !== index);
        newFiles.forEach(file => dt.items.add(file));

        // Cập nhật lại <input> và mảng global
        document.getElementById('attached-files').files = dt.files;
        selectedFiles = Array.from(dt.files);

        renderSelectedFilesList(); // Vẽ lại danh sách
    }
};

// Hàm tải file an toàn (có Token) - Đã sửa lỗi phục hồi nút
window.downloadFile = async (url, filename) => {
    // 1. Lấy nút và lưu trạng thái TRƯỚC KHI vào try/catch
    // (Để đảm bảo biến 'button' có thể dùng được ở mọi nơi)
    const button = document.activeElement;
    const originalText = button ? button.innerHTML : 'Tải/Xem';

    try {
        // 2. Bật hiệu ứng loading
        if (button) {
            button.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang tải...';
            button.disabled = true;
        }

        // 3. Gọi API
        console.log("LOG: Bắt đầu tải file:", url);
        const blob = await api.getBlob(url);

        // 4. Tạo link ảo và click
        const objectUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = objectUrl;
        a.download = filename;
        a.target = '_blank';
        document.body.appendChild(a);
        a.click();

        // 5. Dọn dẹp
        document.body.removeChild(a);
        window.URL.revokeObjectURL(objectUrl);

    } catch (error) {
        console.error("Lỗi tải file:", error);
        // Hiện thông báo lỗi cụ thể (VD: Not Found, Unauthorized...)
        alert("Lỗi: " + error.message);

    } finally {
        // 6. KHỐI FINALLY (QUAN TRỌNG NHẤT)
        // Code trong này LUÔN LUÔN CHẠY, dù thành công hay thất bại
        if (button) {
            button.innerHTML = originalText; // Trả lại chữ "Tải/Xem" cũ
            button.disabled = false;         // Mở khóa nút
        }
    }
};


// --- 3. HÀM MAIN (ROUTER) ---
// Chạy hàm main khi DOM đã sẵn sàng
document.addEventListener('DOMContentLoaded', main);

function main() {
    console.log("LOG: main()"); // ⬅️ SỬA LỖI CÚ PHÁP
    // 1. Gác cổng (Luôn chạy đầu tiên)
    const userInfo = checkAuth('ROLE_SC_STAFF');
    if (!userInfo) {
        console.log("LOG: Gác cổng thất bại, dừng thực thi.");
        return;
    }
    console.log("LOG: Gác cổng OK. User:", userInfo.roles);

    // 2. Vẽ giao diện chung (Chạy 1 lần)
    try {
        renderHeader();
        renderStaffSidebar();
    } catch (e) {
        console.warn("Lỗi render Header/Sidebar:", e);
    }

    // 3. Chạy logic cho trang cụ thể (Router)
    const path = window.location.pathname;
    console.log("LOG: Đang ở path:", path);

    const bodyId = document.body.id;  // Lay body id de nhan dien trang

    // NẾU LÀ TRANG DASHBOARD / LIST
    if (path.endsWith('/scStaff/') || path.endsWith('/scStaff/index.html')) {
        console.log("LOG: Chạy logic trang Dashboard/List.");
        initClaimListPage();
        initClaimDetailsModal();
        loadSummaryCards();
    }

    // NẾU LÀ TRANG TẠO CLAIM
    if (path.endsWith('/scStaff/create-claim.html')) {
        console.log("LOG: Chạy logic trang Tạo Claim.");
        setupCreateClaimForm();
    }

    // 3. ⬇️ TRANG TRA CỨU XE (SỬ DỤNG MODULE) ⬇️
    // Kiểm tra ID body hoặc đường dẫn
    if (bodyId === 'sc-vehicle-lookup-page' || path.includes('vehicle-lookup')) {
        setupVehicleLookup(); // Gọi hàm từ file vehicleLookup.js
    }

    if (bodyId === 'sc-appointments-page' || path.includes('appointments.html')) {
            setupAppointmentsPage();
        }
}


// -------------------------------------------------------------------
// 4. LOGIC TRANG "TẠO CLAIM" (create-claim.html)
// -------------------------------------------------------------------

async function setupCreateClaimForm() {
    const form = document.getElementById('claim-form');
    if (!form) return;
    console.log("LOG: setupCreateClaimForm()"); // ⬅️ SỬA LỖI CÚ PHÁP

    selectedParts = []; // ⬅️ Reset mảng khi vào trang
    selectedFiles = []; // ⬅️ Reset mảng file

    // Lấy các element
    const vinInput = document.getElementById('vin');
    const validateBtn = document.getElementById('validate-vin-btn');
    const resultEl = document.getElementById('vin-check-result');
    const fieldset = document.getElementById('claim-details-fieldset');
    const fileInput = document.getElementById('attached-files'); // ⬅️ THÊM VÀO
    const button = document.getElementById('create-claim-btn');
    const errorEl = document.getElementById('form-error');

    // Logic "Kiểm tra VIN"
    validateBtn.addEventListener('click', async () => {
        const vin = vinInput.value;
        if (vin.length !== 17) {
            resultEl.innerHTML = `<div class="alert alert-danger p-2">VIN phải đủ 17 ký tự.</div>`;
            return;
        }
        resultEl.innerHTML = `<div class="text-muted">Đang kiểm tra VIN...</div>`;
        validateBtn.disabled = true;
        try {
            const customerName = await getCustomerNameByVin(vin);
            resultEl.innerHTML = `<div class="alert alert-success p-2"><strong><i class="bi bi-check-circle-fill"></i> Xe hợp lệ!</strong><br>Thuộc về KH: <strong>${customerName}</strong></div>`;
            vinInput.disabled = true;
            validateBtn.innerHTML = "Đã Khóa";
            fieldset.disabled = false;
        } catch (error) {
            resultEl.innerHTML = `<div class="alert alert-danger p-2"><strong><i class="bi bi-x-circle-fill"></i> Lỗi:</strong> ${error.message} (VIN không tồn tại).</div>`;
            validateBtn.disabled = false;
        }
    });

    // Logic Modal "Thêm Phụ tùng"
    const partModalEl = document.getElementById('partSearchModal');
    if (!partModalEl) return;

    const partModalInstance = bootstrap.Modal.getOrCreateInstance(partModalEl);
    const searchInput = document.getElementById('part-search-input');
    const searchBtn = document.getElementById('part-search-btn');
    const searchResultsEl = document.getElementById('part-search-results');

    // [DEBUG MODE] Logic tìm kiếm phụ tùng
    searchBtn.addEventListener('click', async () => {
        console.log("🚀 [STEP 1] Bắt đầu ấn nút tìm kiếm...");

        const rawInput = searchInput.value;
        const searchTerm = rawInput ? rawInput.trim() : '';
        console.log(`🧐 [STEP 2] Từ khóa sau khi trim: "${searchTerm}"`);

        if (!searchTerm || searchTerm.length < 2) {
            console.warn("⚠️ [STEP 2.1] Từ khóa quá ngắn, dừng lại.");
            searchResultsEl.innerHTML = '<p class="text-danger">Vui lòng nhập tên phụ tùng (ít nhất 2 ký tự).</p>';
            return;
        }

        searchResultsEl.innerHTML = '<p class="text-muted">Đang tìm...</p>';

        try {
            console.log("📡 [STEP 3] Bắt đầu gọi API searchParts...");

            // Gọi API
            const data = await searchParts({ name: searchTerm, page: 0, size: 10 });

            console.log("✅ [STEP 4] API trả về thành công!", data);

            if (data.empty) {
                console.log("ℹ️ [STEP 5] API trả về rỗng (data.empty = true)");
                searchResultsEl.innerHTML = '<p class="text-muted">Không tìm thấy phụ tùng nào.</p>';
                return;
            }

            console.log("🎨 [STEP 6] Bắt đầu vẽ giao diện (Render HTML)...");

            // Render kết quả
            searchResultsEl.innerHTML = `
                <ul class="list-group">
                    ${data.content.map(part => `
                        <li class="list-group-item d-flex justify-content-between align-items-center">
                            <div>
                                <strong>${part.name}</strong>
                                <small class="d-block text-muted">Mã: ${part.partType} | Hãng: ${part.manufacturer}</small>
                            </div>
                            <button class="btn btn-sm btn-outline-success btn-add-part" 
                                    data-part-number="${part.partType}" 
                                    data-part-name="${part.name}">
                                Thêm
                            </button>
                        </li>
                    `).join('')}
                </ul>`;

            console.log("🏁 [STEP 7] Hoàn tất vẽ giao diện.");

        } catch (error) {
            console.error("🔥 [LỖI CHẾT NGƯỜI] Bắt được lỗi tại catch:", error);

            // In chi tiết lỗi ra để soi
            console.log("❌ Tên lỗi:", error.name);
            console.log("❌ Nội dung:", error.message);
            console.log("❌ Stack trace:", error.stack);

            searchResultsEl.innerHTML = `<p class="text-danger">Lỗi: ${error.message || 'Không thể kết nối đến server'}</p>`;
        }
    });

    searchResultsEl.addEventListener('click', (e) => {
        if (e.target.classList.contains('btn-add-part')) {
            const partNumber = e.target.dataset.partNumber;
            const partName = e.target.dataset.partName;
            if (!selectedParts.find(p => p.partNumber === partNumber)) {
                selectedParts.push({ partNumber: partNumber, partName: partName, quantity: 1 });
            }
            console.log(`🤔 PartNumber=${partNumber}  and PartName=${partName}`);
            console.log("⁉️⁉️ Selected parts:", selectedParts);
            renderSelectedPartsTable();
            partModalInstance.hide();
        }
    });

    // ⬇️ THÊM LOGIC LẮNG NGHE FILE INPUT
    fileInput.addEventListener('change', (event) => {
        // 1. Lấy file MỚI được chọn
        const newFiles = Array.from(event.target.files);

        // 2. Dùng Map để lọc trùng (dựa trên tên file và kích thước)
        const existingFilesMap = new Map();
        selectedFiles.forEach(file => {
            existingFilesMap.set(file.name + file.size, file);
        });

        // 3. Thêm các file mới (chưa có) vào Map
        newFiles.forEach(file => {
            if (!existingFilesMap.has(file.name + file.size)) {
                existingFilesMap.set(file.name + file.size, file);
            }
        });

        // 4. Chuyển Map trở lại thành mảng 'selectedFiles'
        selectedFiles = Array.from(existingFilesMap.values());

        // 5. [RẤT QUAN TRỌNG] Đồng bộ hóa mảng 'selectedFiles'
        //    ngược lại vào <input type="file">
        //    Nếu không có bước này, FormData sẽ chỉ gửi file cuối cùng.
        const dataTransfer = new DataTransfer();
        selectedFiles.forEach(file => {
            dataTransfer.items.add(file);
        });
        event.target.files = dataTransfer.files; // Gán lại vào <input>

        console.log("LOG: Đã CỘNG DỒN files. Tổng số:", selectedFiles.length);

        // 6. Vẽ lại danh sách
        renderSelectedFilesList();
    });

    // Logic "Submit Form"
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // 1. Lấy dữ liệu DTO
        const requestedPartsDTO = selectedParts.map(p => ({ partNumber: p.partNumber, quantity: p.quantity, partName: p.partName }));
        console.log("⁉️⁉️⁉️ Check requestedPartsDTO: ", requestedPartsDTO);
        if (requestedPartsDTO.length === 0) {
            errorEl.textContent = 'Lỗi: Bạn phải thêm ít nhất 1 phụ tùng.';
            return;
        }

        button.disabled = true;
        button.textContent = 'Đang gửi...';
        errorEl.textContent = '';

        try {
            // 2. Tạo đối tượng DTO (dưới dạng text)
            const vin = vinInput.value;
            const description = document.getElementById('description').value;
            const createClaimDto = {
                vin,
                description,
                technicianId: null,
                isRecall: false,
                requestedParts: requestedPartsDTO

                // Không cần 'attachedDocuments' ở đây, backend sẽ xử lý từ 'files'
            };

            // 3. XÂY DỰNG FORMDATA
            // Đây là phần thay đổi quan trọng nhất
            const formData = new FormData();

            // 3a. Thêm DTO (dưới dạng JSON string)
            // Backend sẽ cần đọc 'dto' và giải mã (deserialize) nó
            formData.append('dto', JSON.stringify(createClaimDto));

            // 3b. Thêm các file
            // 'files' phải khớp với @RequestPart("files") ở Backend
            selectedFiles.forEach((file) => {
                formData.append('files', file);
            });

            console.log("LOG: Đang gửi FormData (DTO + Files)...");

            // 4. Gọi hàm createClaim (đã được cập nhật để gửi FormData)
            const newClaimId = await createClaim(formData); // ⬅️ Gửi FormData

            alert('Tạo Claim thành công! ID mới là: ' + newClaimId);
            window.location.href = '/pages/scStaff/index.html';

        } catch (error) {
            console.error('Lỗi tạo claim:', error);
            errorEl.textContent = 'Lỗi: ' + error.message;
            button.disabled = false;
            button.textContent = 'Gửi Yêu cầu';
        }
    });

    // (Vẽ bảng lần đầu khi tải trang)
    renderSelectedPartsTable();
}

// Hàm "vẽ" bảng phụ tùng đã chọn (PHIÊN BẢN SỬA LỖI)
function renderSelectedPartsTable() {
    const tbody = document.getElementById('requested-parts-tbody');
    if (!tbody) {
        console.error("LỖI: Không tìm thấy #requested-parts-tbody");
        return;
    }

    // 1. Tìm 'no-parts-row' GỐC trong HTML
    let noPartsRowEl = document.getElementById('no-parts-row');
    let noPartsRowHtml = ''; // ⬅️ Chúng ta sẽ lưu HTML của nó ở đây

    if (noPartsRowEl) {
        // 2. Nếu tìm thấy (như lúc tải trang), lưu lại HTML của nó
        noPartsRowHtml = noPartsRowEl.outerHTML;
    } else {
        // 3. Nếu không tìm thấy (vì đã bị xóa ở lần render trước),
        //    chúng ta tự tạo lại HTML dự phòng
        console.log("LOG: renderTable: Không tìm thấy #no-parts-row, dùng dự phòng.");
        noPartsRowHtml = '<tr id="no-parts-row"><td colspan="4" class="text-center text-muted">Chưa chọn phụ tùng nào.</td></tr>';
    }

    // 4. Bây giờ mới xóa sạch tbody
    tbody.innerHTML = '';

    // 5. Kiểm tra mảng
    if (selectedParts.length === 0) {
        // 6. Nếu mảng rỗng, trả lại cái HTML 'no-parts-row' đã lưu
        tbody.innerHTML = noPartsRowHtml;
        return;
    }

    // 7. Nếu mảng có dữ liệu, vẽ các dòng mới
    selectedParts.forEach((part, index) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${part.partNumber}</td>
            <td>${part.partName}</td>
            <td>
                <input type="number" class="form-control form-control-sm" 
                       min="1" value="${part.quantity}" 
                       data-index="${index}" 
                       onchange="updatePartQuantity(event)">
            </td>
            <td>
                <button type="button" class="btn btn-sm btn-outline-danger" 
                        data-index="${index}"
                        onclick="removePart(event)">
                    Xóa
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}
// (Các hàm window.update... và window.remove... đã được chuyển lên top-level)

// ⬇️ THÊM HÀM MỚI (để "vẽ" danh sách file đã chọn)
function renderSelectedFilesList() {
    const previewListEl = document.getElementById('file-preview-list');
    if (!previewListEl) return;

    if (selectedFiles.length === 0) {
        previewListEl.innerHTML = ''; // Xóa danh sách nếu không có file
        return;
    }

    // Vẽ danh sách file
    previewListEl.innerHTML = `
        <p class="mb-1"><strong>File đã chọn:</strong></p>
        <ul class="list-group list-group-flush">
            ${selectedFiles.map((file, index) => `
                <li class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
                    <div>
                        <i class="bi bi-file-earmark-zip"></i> ${file.name}
                        <small class="text-muted d-block">${(file.size / 1024 / 1024).toFixed(2)} MB</small>
                    </div>
                    <button type="button" class="btn btn-sm btn-outline-danger" 
                            data-index="${index}"
                            onclick="removeFile(event)">
                        Xóa
                    </button>
                </li>
            `).join('')}
        </ul>
    `;
}

// -------------------------------------------------------------------
// 5. LOGIC TRANG "DASHBOARD & LIST" (index.html)
// -------------------------------------------------------------------

async function loadSummaryCards() {
    console.log("LOG: loadSummaryCards()"); // ⬅️ SỬA LỖI CÚ PHÁP
    try {
        const [pendingData, approvedData] = await Promise.all([
            getClaims({ status: 'WAITING_APPROVAL', size: 1, page: 0 }),
            getClaims({ status: 'APPROVED', size: 1, page: 0 })
        ]);

        console.log("LOG: Thẻ Tóm tắt: Chờ xử lý:", pendingData.totalElements, "| Đã duyệt:", approvedData.totalElements);
        const pendingEl = document.getElementById('claims-pending-count');
        const approvedEl = document.getElementById('claims-approved-count');

        if (pendingEl) pendingEl.textContent = pendingData.totalElements;
        if (approvedEl) approvedEl.textContent = approvedData.totalElements;

    } catch (error) {
        console.error("LỖI (loadSummaryCards):", error);
    }
}

function initClaimListPage() {
    console.log("LOG: initClaimListPage()"); // ⬅️ SỬA LỖI CÚ PHÁP

    // ⬇️ THÊM DÒNG NÀY NGAY ĐÂY ⬇️
    // Tự động chọn giá trị 'WAITING_APPROVAL' cho ô dropdown khi tải trang
    document.getElementById('filter-status').value = claimListState.status;

    const filterBtn = document.getElementById('filter-apply-btn');
    const clearBtn = document.getElementById('filter-clear-btn');

    // (Kiểm tra an toàn: Đảm bảo các nút này tồn tại)
    if (filterBtn) {
        console.log("LOG: Đã tìm thấy nút Lọc, gán sự kiện.");
        filterBtn.addEventListener('click', applyFilters);
    } else {
        console.error("LỖI (initClaimListPage): Không tìm thấy nút #filter-apply-btn. HTML của bạn bị thiếu.");
    }

    if (clearBtn) {
        console.log("LOG: Đã tìm thấy nút Xóa Lọc, gán sự kiện.");
        clearBtn.addEventListener('click', clearFilters);
    } else {
        console.error("LỖI (initClaimListPage): Không tìm thấy nút #filter-clear-btn. HTML của bạn bị thiếu.");
    }

    // Tải dữ liệu lần đầu
    console.log("LOG: Bắt đầu gọi fetchAndRenderClaims() lần đầu.");
    fetchAndRenderClaims();
}

async function fetchAndRenderClaims() {
    console.log("LOG: fetchAndRenderClaims()"); // ⬅️ SỬA LỖI CÚ PHÁP
    const loadingEl = document.getElementById('claims-loading');
    const errorEl = document.getElementById('claims-error');
    const tableContainerEl = document.getElementById('claims-table-container');
    const noClaimsEl = document.getElementById('no-claims-message');
    const paginationEl = document.getElementById('pagination-container');

    if (!loadingEl || !errorEl || !tableContainerEl || !noClaimsEl || !paginationEl) {
        console.error("LỖI (fetchAndRenderClaims): Thiếu 1 trong các DIV (loading, error, table, noClaims, pagination).");
        return;
    }

    try {
        console.log("LOG: 1. Hiển thị Loading, ẩn Bảng...");
        loadingEl.classList.remove('d-none');
        errorEl.classList.add('d-none');
        tableContainerEl.classList.add('d-none');
        noClaimsEl.classList.add('d-none');
        paginationEl.classList.add('d-none');

        // 2. Lấy tham số (SỬA LẠI THÔNG MINH HƠN)
        const params = {
            page: claimListState.currentPage,
            size: claimListState.size,
            status: claimListState.status
            // ⬅️ Khởi tạo với các giá trị luôn cần
        };

        // Chỉ thêm các key lọc NẾU chúng có giá trị (không phải rỗng)
        if (claimListState.vin) {
            params.vin = claimListState.vin;
        }
        if (claimListState.claimCode) {
            params.claimCode = claimListState.claimCode;
        }
        // (Chúng ta vẫn muốn gửi status ngay cả khi nó rỗng, nếu người dùng "Xóa lọc")

        console.log("LOG: 2. Gọi API getClaims với params:", params); // ⬅️ Dòng log này giờ sẽ khác

        // 3. Gọi API
        const claimsPage = await getClaims(params);
        console.log("LOG: 3. Gọi API thành công, trả về Page:", claimsPage);

        // 4. Ẩn loading
        loadingEl.classList.add('d-none');

        // 5. Render Bảng & Phân trang
        renderClaimsTable(claimsPage.content);
        renderPagination(claimsPage);

        // 6. Xử lý UI (Hiển thị bảng hoặc thông báo rỗng)
        if (claimsPage.empty) {
            console.log("LOG: 4. API trả về RỖNG ('empty: true'). Hiển thị 'Không tìm thấy'.");
            noClaimsEl.classList.remove('d-none');
        } else {
            console.log("LOG: 4. API trả về DỮ LIỆU. Hiển thị Bảng.");
            tableContainerEl.classList.remove('d-none');
            paginationEl.classList.remove('d-none');
        }
    } catch (error) {
        console.error('LỖI (fetchAndRenderClaims):', error);
        loadingEl.classList.add('d-none');
        errorEl.textContent = `Không thể tải dữ liệu. Lỗi: ${error.message}`;
        errorEl.classList.remove('d-none');
    }
}

function renderClaimsTable(claims = []) {
    const tbodyEl = document.getElementById('claims-tbody');
    if (!tbodyEl) return;
    tbodyEl.innerHTML = '';
    if (claims.length === 0) {
        tbodyEl.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Không tìm thấy claim nào.</td></tr>';
        return;
    }
    claims.forEach(claim => {
        const tr = document.createElement('tr');
        const dateCreated = new Date(claim.dateCreated).toLocaleString('vi-VN');
        const statusClass = `badge status-${(claim.currentStatus || 'default').toLowerCase()}`;
        tr.innerHTML = `
            <td>
                <a href="#" class="btn-view-details fw-bold" data-id="${claim.claimCode}" title="Xem chi tiết">
                    ${claim.claimCode}
                </a>
            </td>
            <td>${claim.vin}</td>
            <td>${claim.customerName || '(Chưa có)'}</td>
            <td>${dateCreated}</td>
            <td><span class="${statusClass}">${claim.currentStatus}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-primary btn-view-details" data-id="${claim.claimCode}">
                   <i class="bi bi-eye"></i> Xem
                </button>
            </td>`;
        tbodyEl.appendChild(tr);
    });
}
function renderPagination(pageData) {
    const paginationControls = document.getElementById('pagination-controls');
    if (!paginationControls) return;
    paginationControls.innerHTML = '';
    const { totalPages, number: currentPage, first, last } = pageData;
    if (totalPages <= 1) return;
    paginationControls.innerHTML += `<li class="page-item ${first ? 'disabled' : ''}"><a class="page-link" href="#" data-page="${currentPage - 1}">Trang trước</a></li>`;
    for (let i = 0; i < totalPages; i++) {
        paginationControls.innerHTML += `<li class="page-item ${i === currentPage ? 'active' : ''}"><a class="page-link" href="#" data-page="${i}">${i + 1}</a></li>`;
    }
    paginationControls.innerHTML += `<li class="page-item ${last ? 'disabled' : ''}"><a class="page-link" href="#" data-page="${currentPage + 1}">Trang sau</a></li>`;
    paginationControls.querySelectorAll('a.page-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            if (link.parentElement.classList.contains('disabled')) return;
            const newPage = parseInt(link.getAttribute('data-page'));
            claimListState.currentPage = newPage;
            fetchAndRenderClaims();
        });
    });
}

function applyFilters() {
    console.log("LOG: applyFilters()"); // ⬅️ SỬA LỖI CÚ PHÁP

    // 1. Đọc giá trị từ HTML
    const statusValue = document.getElementById('filter-status').value;
    console.log("LOG: Giá trị 'Trạng thái' (Tiếng Anh) đọc từ <select> là:", statusValue);

    claimListState.vin = document.getElementById('filter-vin').value.trim();
    claimListState.claimCode = document.getElementById('filter-claim-code').value.trim();
    claimListState.status = statusValue;
    claimListState.currentPage = 0;

    console.log("LOG: State lọc đã cập nhật. Gọi lại fetchAndRenderClaims...");
    fetchAndRenderClaims();
}
function clearFilters() {
    console.log("LOG: clearFilters()"); // ⬅️ SỬA LỖI CÚ PHÁP

    claimListState.vin = '';
    claimListState.claimCode = '';
    claimListState.status = '';
    claimListState.currentPage = 0;
    document.getElementById('filter-vin').value = '';
    document.getElementById('filter-claim-code').value = '';
    document.getElementById('filter-status').value = '';
    fetchAndRenderClaims();
}

/**
 * 5. LOGIC MODAL "CHI TIẾT CLAIM"
 */
function initClaimDetailsModal() {
    const tableBody = document.getElementById('claims-tbody');
    const modalEl = document.getElementById('claimDetailsModal');
    if (!tableBody || !modalEl) return;

    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap JS chưa được tải!');
        return;
    }

    claimModalInstance = new bootstrap.Modal(modalEl);

    tableBody.addEventListener('click', async (e) => {
        const viewButton = e.target.closest('.btn-view-details');
        if (viewButton) {
            e.preventDefault();
            const claimCode = viewButton.dataset.id;
            await openClaimModal(claimCode);
        }
    });
}

async function openClaimModal(claimId) {
    const modalTitle = document.getElementById('claimModalTitle');
    const modalBody = document.getElementById('claimModalBody');
    if (!modalTitle || !modalBody) return;

    modalTitle.textContent = `Chi tiết Claim (ID: ${claimId})`;
    modalBody.innerHTML = '<div class="text-center p-4"><div class="spinner-border" role="status"></div><p class="mt-2">Đang tải dữ liệu...</p></div>';

    claimModalInstance.show();

    try {
        const [details, history] = await Promise.all([
            getClaimDetails(claimId),
            getClaimHistory(claimId)
        ]);
        renderModalContent(details, history);
    } catch (error) {
        console.error("Lỗi tải chi tiết claim:", error);
        modalBody.innerHTML = `<div class="alert alert-danger">Lỗi tải dữ liệu: ${error.message}</div>`;
    }
}

function renderModalContent(details, history) {
    const modalBody = document.getElementById('claimModalBody');
    const statusClass = `badge status-${(details.currentStatus || 'default').toLowerCase()}`;

    let partsHtml = '';
    if (!details.partList || details.partList.length === 0) {
        partsHtml = '<li class="list-group-item">Không có phụ tùng nào được yêu cầu.</li>';
    } else {
        partsHtml = details.partList.map(part => `
            <li class="list-group-item d-flex justify-content-between align-items-center">
                <div>
                    <strong>${part.partName || part.partNumber}</strong>
                    <small class="d-block text-muted">Số lượng: ${part.quantityRequired}</small>
                </div>
                <span class="badge ${part.isApproved ? 'bg-success' : 'bg-secondary'}">
                    ${part.isApproved ? 'Đã duyệt' : 'Chờ duyệt'}
                </span>
            </li>
        `).join('');
    }

    const historyHtml = history.map(log => `
        <li class="list-group-item">
            <strong>${log.status}</strong> 
            <small class="text-muted d-block">${new Date(log.timestamp).toLocaleString('vi-VN')}</small>
            <p class="mb-0">${log.notes || '(Không có ghi chú)'}</p>
            <small class="text-muted">Xử lý bởi: ${log.processorName || 'N/A'}</small>
        </li>
    `).join('');

    // 1. Xử lý HTML cho danh sách tài liệu
    let documentsHtml = '';
    if (!details.documents || details.documents.length === 0) {
        documentsHtml = '<li class="list-group-item">Không có tài liệu đính kèm.</li>';
    } else {
        documentsHtml = details.documents.map(doc => {
            // Kiểm tra xem có phải ảnh không để hiện thumbnail
            const isImage = doc.fileType.startsWith('image/');
            // Lưu ý: doc.url là đường dẫn API backend trả về (vd: /api/v1/claims/documents/5)

            return `
            <li class="list-group-item d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center">
                    ${isImage
                ? '<i class="bi bi-image fs-3 me-3 text-primary"></i>'
                : '<i class="bi bi-file-earmark-text fs-3 me-3"></i>'}
                    
                    <div>
                        <strong>${doc.fileName}</strong>
                        <small class="d-block text-muted">${doc.fileType}</small>
                    </div>
                </div>
                
                <button class="btn btn-sm btn-outline-primary" 
                        onclick="downloadFile('${doc.url}', '${doc.fileName}')">
                    <i class="bi bi-download"></i> Tải/Xem
                </button>
            </li>
            `;
        }).join('');
    }



    modalBody.innerHTML = `
        <ul class="nav nav-tabs" id="myTab" role="tablist">
            <li class="nav-item" role="presentation"><button class="nav-link active" id="info-tab" data-bs-toggle="tab" data-bs-target="#info-tab-pane" type="button" role="tab">Thông tin chung</button></li>
            <li class="nav-item" role="presentation"><button class="nav-link" id="parts-tab" data-bs-toggle="tab" data-bs-target="#parts-tab-pane" type="button" role="tab">Phụ tùng (${details.partList ? details.partList.length : 0})</button></li>
            <li class="nav-item" role="presentation"><button class="nav-link" id="history-tab" data-bs-toggle="tab" data-bs-target="#history-tab-pane" type="button" role="tab">Lịch sử (${history ? history.length : 0})</button></li>
            <li class="nav-item" role="presentation"><button class="nav-link" id="docs-tab" data-bs-toggle="tab" data-bs-target="#docs-tab-pane" type="button" role="tab">Tài liệu (${details.documents ? details.documents.length : 0})</button></li>
        </ul>
        <div class="tab-content p-3 border border-top-0" id="myTabContent">
            <div class="tab-pane fade show active" id="info-tab-pane" role="tabpanel">
                <h5>Thông tin Claim: ${details.claimCode}</h5>
                <div class="row">
                    <div class="col-md-6">
                        <p><strong>Trạng thái:</strong> <span class="${statusClass}">${details.currentStatus}</span></p>
                        <p><strong>Số VIN:</strong> ${details.vin}</p>
                        <p><strong>Khách hàng:</strong> ${details.customerName || '(Chưa có)'}</p>
                        <p><strong>Kỹ thuật viên:</strong> ${details.technicalName || '(Chưa gán)'}</p>
                    </div>
                    <div class="col-md-6">
                        <p><strong>Mô tả lỗi:</strong></p>
                        <p class="bg-light p-2 rounded">${details.description}</p>
                    </div>
                </div>
            </div>
            <div class="tab-pane fade" id="parts-tab-pane" role="tabpanel">
                <h5>Phụ tùng Yêu cầu</h5>
                <ul class="list-group">${partsHtml}</ul>
            </div>
            <div class="tab-pane fade" id="history-tab-pane" role="tabpanel">
                <h5>Lịch sử Trạng thái</h5>
                <ul class="list-group">${historyHtml || '<li class="list-group-item">Chưa có lịch sử.</li>'}</ul>
            </div>
            <div class="tab-pane fade" id="docs-tab-pane" role="tabpanel">
                <h5>Tài liệu đính kèm</h5>
                <ul class="list-group">${documentsHtml}</ul>
            </div>
        </div>
    `;


}