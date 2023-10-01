package com.example.testassignment.controller;

import com.example.testassignment.configuration.UserConfiguration;
import com.example.testassignment.model.User;
import com.example.testassignment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("/users")
@ResponseStatus(HttpStatus.CREATED)
public class UserController {

    private final UserRepository userRepository;
    private final UserConfiguration userConfig;

    @Autowired
    public UserController(UserRepository userRepository, UserConfiguration userConfig) {
        this.userRepository = userRepository;
        this.userConfig = userConfig;
    }

    @PostMapping("/createUser")
    public ResponseEntity<String> createUser(@RequestBody User user){
        LocalDate currentDate = LocalDate.now();
        LocalDate minBirthDate = currentDate.minusYears(userConfig.getMinAge());

        if(user.getBirthDate().isAfter(minBirthDate))
            throw new CustomValidationException("User must be older than 18");

        userRepository.save(user);
        return ResponseEntity.ok("User created successfully");
    }

    @PatchMapping("/updateUserFields/{userId}")
    public ResponseEntity<String> updateFields(@PathVariable Long userId, @RequestBody User updatedUser){
        User user = userRepository.findById(userId).orElse(null);
        if(user != null && updatedUser != null){
            updateUserFieldIfNotNull(updatedUser.getEmail(), user::setEmail);
            updateUserFieldIfNotNull(updatedUser.getFirstName(), user::setFirstName);
            updateUserFieldIfNotNull(updatedUser.getLastName(), user::setLastName);
            updateUserFieldIfNotNull(updatedUser.getBirthDate(), user::setBirthDate);
            updateUserFieldIfNotNull(updatedUser.getAddress(), user::setAddress);
            updateUserFieldIfNotNull(updatedUser.getPhoneNumber(), user::setPhoneNumber);

            userRepository.save(user);
        } else {
            throw new CustomValidationException("The user cannot be null");
        }

        return ResponseEntity.ok("The user has been updated");
    }

    private <T> void updateUserFieldIfNotNull(T value, Consumer<T> updater){
        if(value != null)
            updater.accept(value);
    }

    @PutMapping("/updateAllUserFields/{userId}")
    public ResponseEntity<String> updateAllFields(@PathVariable Long userId, @RequestBody User updatedUser){
        User user = userRepository.findById(userId).orElse(null);
        if(user != null && updatedUser != null){
            user.setEmail(updatedUser.getEmail());
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setBirthDate(updatedUser.getBirthDate());
            user.setAddress(updatedUser.getAddress());
            user.setPhoneNumber(updatedUser.getPhoneNumber());

            try {
                userRepository.save(user);
                return ResponseEntity.ok("The user has been updated");
            } catch (TransactionSystemException e){
                throw new CustomValidationException("Enter valid fields");
            }
        } else {
            throw new CustomValidationException("The user cannot be null");
        }
    }

    @DeleteMapping("/deleteUserById/{userId}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long userId){
        if(userRepository.findById(userId).orElse(null) != null) {
            userRepository.deleteById(userId);
        } else {
            throw new CustomValidationException("User not found");
        }
        return ResponseEntity.ok("The user has been deleted");
    }

    @DeleteMapping("/deleteUserByEmail/{email}")
    @Transactional
    public ResponseEntity<String> deleteUserByEmail(@PathVariable String email){
        if(userRepository.findAllByEmail(email) != null && !userRepository.findAllByEmail(email).isEmpty()) {
            userRepository.deleteByEmail(email);
        } else {
            throw new CustomValidationException("User not found");
        }
        return ResponseEntity.ok("The user has been deleted");
    }

    @GetMapping("/searchUsersByBirthDayRange")
    public ResponseEntity<List<User>> getUsersByBirthDayRange(@RequestParam("from") String from, @RequestParam("to") String to){
        LocalDate dateFrom = LocalDate.parse(from);
        LocalDate dateTo = LocalDate.parse(to);

        if(dateFrom.isBefore(dateTo)){
            return ResponseEntity.ok(userRepository.findAllByBirthDateBetween(dateFrom, dateTo));
        } else {
            throw new CustomValidationException("The parameter 'from' must be less than the parameter 'to'");
        }
    }
}
