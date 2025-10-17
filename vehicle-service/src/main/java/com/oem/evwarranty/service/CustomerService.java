package com.oem.evwarranty.service;

import com.oem.evwarranty.entity.Customer;
import com.oem.evwarranty.exception.ResourceNotFoundException;
import com.oem.evwarranty.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    // 1. CREATE: Tạo khách hàng mới (Đã có)
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    // 2. READ: Lấy tất cả khách hàng (Đã có)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // 3. READ: Lấy một khách hàng theo ID
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    // 4. UPDATE: Cập nhật thông tin khách hàng
    public Customer updateCustomer(Long id, Customer customerDetails) {
        // 1. Tìm khách hàng cần cập nhật
        Customer customerToUpdate = getCustomerById(id);

        // 2. Lấy email mới từ yêu cầu
        String newEmail = customerDetails.getEmail();

        // 3. Kiểm tra xem email có bị thay đổi không và email mới có bị trùng không
        // So sánh email mới với email cũ. Nếu khác nhau, ta mới cần kiểm tra.
        if (!customerToUpdate.getEmail().equals(newEmail)) {
            // Tìm xem có khách hàng nào khác đang dùng email mới này không
            Optional<Customer> existingCustomerWithNewEmail = customerRepository.findByEmail(newEmail);

            // Nếu tìm thấy một khách hàng khác, ném ra lỗi
            if (existingCustomerWithNewEmail.isPresent()) {
                // Chúng ta có thể dùng DataIntegrityViolationException hoặc một exception tùy chỉnh khác
                throw new DataIntegrityViolationException("Email '" + newEmail + "' is already in use by another customer.");
            }
        }
        customerToUpdate.setCustomerName(customerDetails.getCustomerName());
        customerToUpdate.setPhoneNum(customerDetails.getPhoneNum());
        customerToUpdate.setEmail(newEmail);

        return customerRepository.save(customerToUpdate);
    }

    // 5. DELETE: Xóa một khách hàng
    public void deleteCustomer(Long id) {
        // Kiểm tra xem khách hàng có tồn tại không trước khi xóa
        if(!customerRepository.existsById(id)){
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    // 6. SEARCH: Tìm khách hàng theo email
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
    }
}