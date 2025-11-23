// src/js/admin-inventory.js
import {
    getAllInventory,
    addOrUpdateStock,
    addOrUpdateStock as addStock, // có thể dùng addStock
    updateInventoryQuantity
} from '../services/userService.js'; // lấy từ userService.js

document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.getElementById('inventory-table-body');

    // MODALS
    const addStockModal = document.getElementById('add-stock-modal');
    const addStockBtn = document.getElementById('add-stock-btn');
    const closeStockModalBtn = document.getElementById('close-stock-modal');
    const cancelStockModalBtn = document.getElementById('cancel-stock-modal');
    const addStockForm = document.getElementById('add-stock-form');
    const stockErrorMessage = document.getElementById('stock-error-message');

    const addPartModal = document.getElementById('add-part-modal');
    const addPartBtn = document.getElementById('add-new-part-btn');
    const closePartModalBtn = document.getElementById('close-part-modal');
    const cancelPartModalBtn = document.getElementById('cancel-part-modal');
    const addPartForm = document.getElementById('add-part-form');
    const partErrorMessage = document.getElementById('part-error-message');

    // ================= LOAD DATA =================
    async function loadInventory() {
        tableBody.innerHTML = `<tr><td colspan="3" style="text-align:center;padding:40px;">Đang tải dữ liệu...</td></tr>`;
        try {
            const { data } = await getAllInventory();
            populateTable(data);
        } catch (err) {
            tableBody.innerHTML = `<tr><td colspan="3" style="text-align:center;color:red;padding:40px;">Lỗi tải dữ liệu: ${err.message}</td></tr>`;
        }
    }

    function populateTable(data) {
        if (!data || data.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="3" style="text-align:center;padding:40px;">Không có dữ liệu tồn kho.</td></tr>`;
            return;
        }
        tableBody.innerHTML = '';
        data.forEach(item => {
            const row = document.createElement('tr');
            row.setAttribute('data-id', item.inventoryId);
            row.innerHTML = `
                <td>
                    <span class="part-name">${item.partName}</span>
                    <span class="part-id">ID: ${item.partId}</span>
                </td>
                <td>${item.location}</td>
                <td>
                    <div class="quantity-cell">
                        <input type="number" class="quantity-input" value="${item.quantity}" readonly>
                        <button class="edit-btn" title="Chỉnh sửa số lượng">
                            <i class="fa-solid fa-pencil"></i>
                        </button>
                    </div>
                </td>
            `;
            tableBody.appendChild(row);
        });
    }

    // ================= EDIT QUANTITY =================
    tableBody.addEventListener('click', async (e) => {
        const btn = e.target.closest('.edit-btn');
        if (!btn) return;

        const row = btn.closest('tr');
        const input = row.querySelector('.quantity-input');
        const icon = btn.querySelector('i');
        const inventoryId = row.getAttribute('data-id');

        if (input.hasAttribute('readonly')) {
            input.removeAttribute('readonly');
            input.focus();
            input.select();
            icon.classList.replace('fa-pencil', 'fa-check');
            btn.title = 'Lưu thay đổi';
        } else {
            const newQuantity = parseInt(input.value);
            try {
                await updateInventoryQuantity(inventoryId, newQuantity);
                input.setAttribute('readonly', true);
                icon.classList.replace('fa-check', 'fa-pencil');
                btn.title = 'Chỉnh sửa số lượng';
            } catch (err) {
                alert('Cập nhật thất bại: ' + err.message);
                loadInventory();
            }
        }
    });

    // ================= ADD STOCK =================
    const openAddStockModal = () => {
        if (stockErrorMessage) stockErrorMessage.style.display = 'none';
        addStockModal.classList.add('show');
    };
    addStockBtn?.addEventListener('click', openAddStockModal);
    closeStockModalBtn?.addEventListener('click', () => addStockModal.classList.remove('show'));
    cancelStockModalBtn?.addEventListener('click', () => addStockModal.classList.remove('show'));
    window.addEventListener('click', e => { if (e.target === addStockModal) addStockModal.classList.remove('show'); });

    addStockForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (stockErrorMessage) stockErrorMessage.style.display = 'none';

        const formData = new FormData(addStockForm);
        const stockData = Object.fromEntries(formData.entries());
        stockData.partId = parseInt(stockData.partId);
        stockData.quantity = parseInt(stockData.quantity);

        try {
            await addOrUpdateStock(stockData);
            addStockModal.classList.remove('show');
            addStockForm.reset();
            loadInventory();
            alert('Nhập kho thành công!');
        } catch (err) {
            if (stockErrorMessage) {
                stockErrorMessage.textContent = `Lỗi: ${err.message}`;
                stockErrorMessage.style.display = 'block';
            }
        }
    });

    // ================= ADD PART =================
    const openAddPartModal = () => {
        if (partErrorMessage) partErrorMessage.style.display = 'none';
        addPartModal.classList.add('show');
    };
    addPartBtn?.addEventListener('click', openAddPartModal);
    closePartModalBtn?.addEventListener('click', () => addPartModal.classList.remove('show'));
    cancelPartModalBtn?.addEventListener('click', () => addPartModal.classList.remove('show'));
    window.addEventListener('click', e => { if (e.target === addPartModal) addPartModal.classList.remove('show'); });

    addPartForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (partErrorMessage) partErrorMessage.style.display = 'none';

        const formData = new FormData(addPartForm);
        const partData = Object.fromEntries(formData.entries());

        try {
            await fetch('/api/v1/parts', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(partData)
            });
            addPartModal.classList.remove('show');
            addPartForm.reset();
            alert(`Thêm Part "${partData.name}" thành công!`);
        } catch (err) {
            if (partErrorMessage) {
                partErrorMessage.textContent = `Lỗi: ${err.message}`;
                partErrorMessage.style.display = 'block';
            }
        }
    });

    // ================= INITIAL LOAD =================
    loadInventory();
});
