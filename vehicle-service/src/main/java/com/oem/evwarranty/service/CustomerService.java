package com.oem.evwarranty.service;

import com.oem.evwarranty.dto.request.CustomerRequestDTO;
import com.oem.evwarranty.dto.response.CustomerResponseDTO;
import com.oem.evwarranty.entity.Customer;
import com.oem.evwarranty.exception.ResourceNotFoundException;
import com.oem.evwarranty.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    // SỬA 1: Nhận RequestDTO, trả về ResponseDTO
    @CacheEvict(value = "customers_list", allEntries = true)
    public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {
        // Kiểm tra email trùng lặp
        if (customerRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new DataIntegrityViolationException("Email '" + requestDTO.getEmail() + "' is already in use.");
        }

        Customer newCustomer = new Customer();
        newCustomer.setCustomerName(requestDTO.getCustomerName());
        newCustomer.setPhoneNum(requestDTO.getPhoneNum());
        newCustomer.setEmail(requestDTO.getEmail());

        Customer savedCustomer = customerRepository.save(newCustomer);
        return convertToDTO(savedCustomer);
    }

    // SỬA 2: Trả về List<ResponseDTO>
    @Cacheable(value = "customers_list")
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // SỬA 3: Trả về ResponseDTO
    @Cacheable(value = "customers", key = "#id")
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return convertToDTO(customer);
    }

    // SỬA 4: Nhận RequestDTO, trả về ResponseDTO
    @CacheEvict(value = "customers", key = "#id")
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO) {
        Customer customerToUpdate = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        String newEmail = requestDTO.getEmail();

        // (Logic kiểm tra email trùng lặp của bạn rất tốt, giữ nguyên)
        if (!customerToUpdate.getEmail().equals(newEmail)) {
            Optional<Customer> existing = customerRepository.findByEmail(newEmail);
            if (existing.isPresent()) {
                throw new DataIntegrityViolationException("Email '" + newEmail + "' is already in use.");
            }
        }

        customerToUpdate.setCustomerName(requestDTO.getCustomerName());
        customerToUpdate.setPhoneNum(requestDTO.getPhoneNum());
        customerToUpdate.setEmail(newEmail);

        Customer updatedCustomer = customerRepository.save(customerToUpdate);
        return convertToDTO(updatedCustomer);
    }

    @CacheEvict(value = "customers", key = "#id")
    public void deleteCustomer(Long id) {
        if(!customerRepository.existsById(id)){
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Cacheable(value = "customers_email", key = "#email")
    public CustomerResponseDTO getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
        return convertToDTO(customer);
    }

    // --- HÀM HELPER CHUYỂN ĐỔI ---
    private CustomerResponseDTO convertToDTO(Customer customer) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setCustomerId(customer.getCustomerId());
        dto.setCustomerName(customer.getCustomerName());
        dto.setPhoneNum(customer.getPhoneNum());
        dto.setEmail(customer.getEmail());
        return dto;
    }
}