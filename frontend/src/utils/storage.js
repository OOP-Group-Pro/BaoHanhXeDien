// Hàm lưu token sau khi login thành công:
export const saveToken = (token) => {
    localStorage.setItem('token', token);
};

// Ham lay token de gan vao API:
export const getToken = () => {
    return localStorage.getItem('token');
};

// Ham xoa token khi logout:
export const clearToken = () => {
    localStorage.removeItem('token');
};

// Ham luu User:
export const saveUser = (user) => {
    localStorage.setItem('user', JSON.stringify(user));
};

// Ham xoa User
export const clearUser = () => {
    localStorage.removeItem('user');
};

// Ham lay User:
export const getUser = () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
}

