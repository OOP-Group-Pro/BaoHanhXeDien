const loginForm = document.getElementById('login-form');
const apiSection = document.getElementById('api-section');
const loginSection = document.getElementById('login-section');
const loginMessage = document.getElementById('login-message');
const callApiButton = document.getElementById('call-api-button');
const logoutButton = document.getElementById('logout-button');
const apiResult = document.getElementById('api-result');
const displayToken = document.getElementById('display-token');

// Thay đổi URL API cho phù hợp với backend của bạn
const API_BASE_URL = 'http://localhost:8080/api/v1';
const LOGIN_URL = `${API_BASE_URL}/auth/login`;
const LOGOUT_URL = `${API_BASE_URL}/auth/logout`;
// URL API mẫu cần xác thực, bạn cần thay đổi/tạo API này trong backend
const PROTECTED_API_URL = 'http://localhost:8080/api/v1/protected/data';

let authToken = localStorage.getItem('jwtToken');

// --- HÀM XỬ LÝ GIAO DIỆN ---

function updateUI() {
    if (authToken) {
        // Đã đăng nhập
        loginSection.style.display = 'none';
        apiSection.style.display = 'block';
        displayToken.textContent = authToken.substring(0, 30) + '...'; // Hiển thị rút gọn
        loginMessage.textContent = '';
    } else {
        // Chưa đăng nhập
        loginSection.style.display = 'block';
        apiSection.style.display = 'none';
        displayToken.textContent = '';
        apiResult.textContent = '...';
    }
}

// --- HÀM XỬ LÝ ĐĂNG NHẬP ---

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    loginMessage.textContent = 'Đang đăng nhập...';

    try {
        const response = await fetch(LOGIN_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            // Đăng nhập thành công, lưu token
            authToken = data.token;
            localStorage.setItem('jwtToken', authToken);
            loginMessage.textContent = 'Đăng nhập thành công!';
            updateUI();
        } else {
            // Đăng nhập thất bại
            loginMessage.textContent = data.error || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.';
        }
    } catch (error) {
        console.error('Lỗi khi gọi API Đăng nhập:', error);
        loginMessage.textContent = 'Lỗi kết nối đến server.';
    }
});

// --- HÀM XỬ LÝ GỌI API CẦN TOKEN ---

callApiButton.addEventListener('click', async () => {
    if (!authToken) {
        apiResult.textContent = 'Lỗi: Không tìm thấy Token. Vui lòng đăng nhập lại.';
        return;
    }

    apiResult.textContent = 'Đang gọi API bảo vệ...';

    try {
        const response = await fetch(PROTECTED_API_URL, {
            method: 'GET',
            headers: {
                // Thêm Header "Authorization" với giá trị "Bearer [Token]"
                'Authorization': `Bearer ${authToken}`
            }
        });

        const data = await response.json();

        if (response.ok) {
            // Gọi API thành công
            apiResult.textContent = JSON.stringify(data, null, 2);
        } else if (response.status === 401 || response.status === 403) {
            // Lỗi xác thực (Token hết hạn, sai...)
            apiResult.textContent = `Lỗi ${response.status}: Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.`;
            // Xóa token cũ để buộc người dùng đăng nhập lại
            authToken = null;
            localStorage.removeItem('jwtToken');
            updateUI();
        } else {
            // Lỗi khác
            apiResult.textContent = `Lỗi ${response.status}: ${JSON.stringify(data)}`;
        }

    } catch (error) {
        console.error('Lỗi khi gọi API bảo vệ:', error);
        apiResult.textContent = 'Lỗi kết nối hoặc network khi gọi API.';
    }
});


// --- HÀM XỬ LÝ ĐĂNG XUẤT ---

logoutButton.addEventListener('click', async () => {
    // Gọi API logout (nếu có, thường chỉ để thông báo)
    try {
        await fetch(LOGOUT_URL, {
            method: 'POST'
            // Không cần gửi token vì logout chỉ là thao tác client
        });
    } catch (e) {
        console.warn('Lỗi khi gọi API logout, nhưng vẫn tiếp tục logout client-side.');
    }

    // Xóa token ở client-side
    authToken = null;
    localStorage.removeItem('jwtToken');

    // Cập nhật giao diện
    updateUI();
});


// Khởi tạo giao diện khi trang load
updateUI();