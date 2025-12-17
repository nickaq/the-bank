package com.thebank.customers.service;

import com.thebank.common.exception.BusinessException;
import com.thebank.common.exception.ResourceNotFoundException;
import com.thebank.customers.dto.CreateCustomerRequest;
import com.thebank.customers.dto.CustomerResponse;
import com.thebank.customers.dto.UpdateCustomerRequest;
import com.thebank.customers.entity.Customer;
import com.thebank.customers.entity.CustomerStatus;
import com.thebank.customers.repository.CustomerRepository;
import com.thebank.identity.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for customer management operations.
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new BusinessException("EMAIL_EXISTS", "Customer with this email already exists");
        }

        Customer customer = new Customer(
                request.fullName(),
                request.email(),
                request.phone()
        );

        if (request.status() != null) {
            customer.setStatus(request.status());
        }

        customer = customerRepository.save(customer);
        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse createCustomerWithUser(CreateCustomerRequest request, User user) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new BusinessException("EMAIL_EXISTS", "Customer with this email already exists");
        }

        Customer customer = new Customer(
                request.fullName(),
                request.email(),
                request.phone()
        );
        customer.setUser(user);

        if (request.status() != null) {
            customer.setStatus(request.status());
        }

        customer = customerRepository.save(customer);
        return CustomerResponse.from(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id.toString()));
        return CustomerResponse.from(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByUserId(UUID userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "user:" + userId));
        return CustomerResponse.from(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(CustomerResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getCustomersByStatus(CustomerStatus status, Pageable pageable) {
        return customerRepository.findByStatus(status, pageable)
                .map(CustomerResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(String name, Pageable pageable) {
        return customerRepository.findByFullNameContainingIgnoreCase(name, pageable)
                .map(CustomerResponse::from);
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id.toString()));

        if (request.fullName() != null) {
            customer.setFullName(request.fullName());
        }
        if (request.email() != null && !request.email().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.email())) {
                throw new BusinessException("EMAIL_EXISTS", "Customer with this email already exists");
            }
            customer.setEmail(request.email());
        }
        if (request.phone() != null) {
            customer.setPhone(request.phone());
        }
        if (request.status() != null) {
            customer.setStatus(request.status());
        }

        customer = customerRepository.save(customer);
        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse blockCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id.toString()));

        customer.setStatus(CustomerStatus.BLOCKED);
        customer = customerRepository.save(customer);
        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse activateCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id.toString()));

        customer.setStatus(CustomerStatus.ACTIVE);
        customer = customerRepository.save(customer);
        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse linkCustomerToUser(UUID customerId, User user) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId.toString()));

        customer.setUser(user);
        customer = customerRepository.save(customer);
        return CustomerResponse.from(customer);
    }
}
