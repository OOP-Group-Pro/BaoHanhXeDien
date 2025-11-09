import './style.css'
import javascriptLogo from './javascript.svg'
import viteLogo from '/vite.svg'
import { setupCounter } from './counter.js'

/*
document.querySelector('#app').innerHTML = `
  <div>
    <a href="https://vite.dev" target="_blank">
      <img src="${viteLogo}" class="logo" alt="Vite logo" />
    </a>
    <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript" target="_blank">
      <img src="${javascriptLogo}" class="logo vanilla" alt="JavaScript logo" />
    </a>
    <h1>Hello Vite!</h1>
    <div class="card">
      <button id="counter" type="button"></button>
    </div>
    <p class="read-the-docs">
      Click on the Vite logo to learn more
    </p>
  </div>
`
setupCounter(document.querySelector('#counter'))

*/

// Dán vào file frontend/main.js

/**
 * Hàm gọi API login
 */
async function attemptLogin() {
    // 1. Thông tin đăng nhập
    const loginData = {
        username: "Admin", // Thay bằng username đúng
        password: "admin123"  // Thay bằng password đúng
    };

    try {
        // 2. Gọi API
        // Chúng ta gọi /api/v1/... (URL tương đối)
        // Vite sẽ tự động chuyển nó đến http://localhost:80
        const response = await fetch('/api/v1/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData) // Chuyển object sang chuỗi JSON
        });

        // 3. Xử lý kết quả
        if (!response.ok) {
            // Nếu API trả về lỗi (401, 500, v.v.)
            const errorData = await response.json();
            console.error('Login thất bại:', errorData);
            alert('Login thất bại! ' + (errorData.message || ''));
        } else {
            // Nếu API trả về 200 OK
            const data = await response.json();
            console.log('Login thành công! Token:', data.token);
            alert('Login thành công! Hãy kiểm tra Console (F12) để xem token.');
        }

    } catch (error) {
        // Lỗi mạng (ví dụ: backend chưa chạy)
        console.error('Lỗi mạng:', error);
        alert('Không thể kết nối tới server. Backend đã chạy chưa?');
    }
}

// Thêm một nút bấm vào trang web để chạy thử
document.querySelector('#app').innerHTML = `
  <div>
    <h1>Test API Login</h1>
    <button id="loginButton">Click để Login (Admin/admin123)</button>
  </div>
`;

// Gán sự kiện click cho nút bấm
document.getElementById('loginButton').addEventListener('click', attemptLogin);
